package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sakura.meetu.entity.Goods;
import com.sakura.meetu.entity.Orders;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.service.IGoodsService;
import com.sakura.meetu.service.IOrdersService;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.utils.Result;
import com.sakura.meetu.utils.SessionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sakura
 * @since 2023-09-14
 */
@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final IOrdersService ordersService;

    private final IGoodsService goodsService;
    private final IUserService userService;

    public OrdersController(IOrdersService ordersService, IGoodsService goodsService, IUserService userService) {
        this.ordersService = ordersService;
        this.goodsService = goodsService;
        this.userService = userService;
    }

    @PostMapping
    public Result save(@RequestBody Orders orders) {
        User user = SessionUtils.getUser();
        user = userService.getById(user.getId());  // 从数据库查询出用户的真实信息
        Integer score = user.getScore();
        Integer goodsId = orders.getGoodsId();
        Goods goods = goodsService.getById(goodsId);
        // 计算所需要的积分
        int requiredPoints = orders.getNum() * orders.getScore();
        if (score < requiredPoints) { // 用户积分不足
            return Result.error("-1", "您的积分不足");
        }

        if (goods.getNum() <= 0 || goods.getNum() < orders.getNum()) { // 库存不足
            return Result.error("-1", "商品库存不足");
        }

        orders.setUserId(user.getId());
        orders.setScore(requiredPoints);
        orders.setTime(DateUtil.now());
        orders.setCode("MEETU-" + DateUtil.format(new Date(), "yyyyMMddd") + (String.valueOf(System.currentTimeMillis())).substring(7));
        ordersService.save(orders);

        // 更新商品库存
        goods.setNum(goods.getNum() - orders.getNum());
        goodsService.updateById(goods);

        user.setScore(user.getScore() - requiredPoints);  // 扣除用户积分
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping
    @SaCheckPermission("orders.edit")
    public Result update(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("orders.delete")
    public Result delete(@PathVariable Integer id) {
        ordersService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("orders.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        ordersService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaCheckPermission("orders.list")
    public Result findAll() {
        return Result.success(ordersService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("orders.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(ordersService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<Orders>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "code", name);
        User currentUser = SessionUtils.getUser();
        String role = currentUser.getRole();
        if ("USER".equals(role)) {  // 用户
            queryWrapper.eq("user_id", currentUser.getId());  // select * from  dynamic where user_id = xxx
        }
        return Result.success(ordersService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("orders.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Orders> list = ordersService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Orders信息表", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }

    /**
     * excel 导入
     *
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    @SaCheckPermission("orders.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Orders> list = reader.readAll(Orders.class);

        ordersService.saveBatch(list);
        return Result.success();
    }

}
