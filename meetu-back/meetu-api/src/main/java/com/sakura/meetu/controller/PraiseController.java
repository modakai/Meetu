package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sakura.meetu.entity.Praise;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.service.IPraiseService;
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
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sakura
 * @since 2023-09-06
 */
@RestController
@RequestMapping("/api/praise")
public class PraiseController {

    private final IPraiseService praiseService;
    private final IUserService userService;

    public PraiseController(IPraiseService praiseService, IUserService userService) {
        this.praiseService = praiseService;
        this.userService = userService;
    }

    @PostMapping
    public Result save(@RequestBody Praise praise) {

        User user = SessionUtils.getUser();
        praise.setUserId(user.getId());
        praiseService.save(praise);

        // 更新用户积分
        user.setScore(user.getScore() + 1);
        userService.updateById(user);

        return Result.success();
    }

    @PutMapping
    @SaCheckPermission("praise.edit")
    public Result update(@RequestBody Praise praise) {
        praiseService.updateById(praise);
        return Result.success();
    }

    @DeleteMapping
    public Result delete(@RequestBody Praise praise) {
        praiseService.removeById(praise.getId());
        // 扣除用户积分
        Integer userId = praise.getUserId();
        User user = userService.getById(userId);
        user.setScore(user.getScore() - 1);
        userService.updateById(user);

        return Result.success();
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("praise.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        praiseService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaCheckPermission("praise.list")
    public Result findAll() {
        return Result.success(praiseService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("praise.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(praiseService.getById(id));
    }

    @GetMapping("/page")
    @SaCheckPermission("praise.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Praise> queryWrapper = new QueryWrapper<Praise>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(praiseService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("praise.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Praise> list = praiseService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Praise信息表", StandardCharsets.UTF_8);
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
    @SaCheckPermission("praise.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Praise> list = reader.readAll(Praise.class);

        praiseService.saveBatch(list);
        return Result.success();
    }

}