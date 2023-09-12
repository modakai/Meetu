package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sakura.meetu.entity.Collect;
import com.sakura.meetu.entity.Dynamic;
import com.sakura.meetu.entity.Praise;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.service.ICollectService;
import com.sakura.meetu.service.IDynamicService;
import com.sakura.meetu.service.IPraiseService;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.utils.Result;
import com.sakura.meetu.utils.SessionUtils;
import com.sakura.meetu.vo.DynamicVo;
import com.sakura.meetu.vo.UserVo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sakura
 * @since 2023-09-04
 */
@RestController
@RequestMapping("/api/dynamic")
public class DynamicController {

    private final IDynamicService dynamicService;
    private final IUserService userService;

    private final IPraiseService praiseService;
    private final ICollectService collectService;


    public DynamicController(IDynamicService dynamicService, IUserService userService, IPraiseService praiseService, ICollectService collectService) {
        this.dynamicService = dynamicService;
        this.userService = userService;
        this.praiseService = praiseService;
        this.collectService = collectService;
    }

    @GetMapping("/hot")
    @SaIgnore
    public Result hot() {
        return Result.success(dynamicService.listHotDynamic());
    }

    @PostMapping
    @SaCheckPermission("dynamic.add")
    public Result save(@RequestBody Dynamic dynamic) {
        User user = SessionUtils.getUser();
        dynamic.setUserId(user.getId());
        dynamic.setTime(DateUtil.now());

        try {
            dynamicService.save(dynamic);
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException(e.getMessage());
        }

        // 更新用户的积分
        userService.updateScore(5, user.getId());
        return Result.success();
    }

    @PutMapping
    @SaCheckPermission("dynamic.edit")
    public Result update(@RequestBody Dynamic dynamic) {
        dynamicService.updateById(dynamic);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        dynamicService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("dynamic.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        dynamicService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaCheckPermission("dynamic.list")
    public Result findAll() {
        return Result.success(dynamicService.list());
    }

    @GetMapping("/{id}")
    @SaIgnore
    public Result findOne(@PathVariable Integer id) {
        // 查找 动态 并且更新访问
        Dynamic dynamic = dynamicService.findOneById(id);

        // 添加表单用户数据
        Optional.of(userService.getById(dynamic.getUserId())).ifPresent(user -> {
            UserVo userVo = UserVo.builder()
                    .username(user.getUsername())
                    .uid(user.getUid())
                    .avatar(user.getAvatar())
                    .age(user.getAge())
                    .name(user.getName())
                    .gender(user.getGender())
                    .intro(user.getIntro())
                    .id(user.getId())
                    .email(user.getEmail())
                    .score(user.getScore()).build();
            dynamic.setUser(userVo);
        });

        User user = SessionUtils.getUser();

        if (user != null) {
            Integer userId = user.getId();
            // 判断是否点赞
            Praise praise = praiseService.getOne(new LambdaQueryWrapper<Praise>().eq(Praise::getUserId, userId)
                    .eq(Praise::getFid, dynamic.getId())
                    .eq(Praise::getType, "dynamic"));
            dynamic.setHasPraise(praise != null);

            // 判断是否收藏
            List<Collect> collectList = collectService.list(new LambdaQueryWrapper<Collect>().eq(Collect::getUserId, userId));
            boolean hasCollect = collectList.stream().anyMatch(collect -> collect.getDynamicId().equals(dynamic.getId()));
            dynamic.setHasCollect(hasCollect);
        }

        return Result.success(dynamic);
    }

    @GetMapping("/page")
    @SaIgnore
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String type,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {

        Map<String, Object> map = dynamicService.listPage(name, type, pageNum, pageSize);
        List<DynamicVo> records = (List<DynamicVo>) map.get("records");

        List<User> userList = userService.list();

        for (DynamicVo record : records) {
            // 查出用户信息
            userList.stream().filter(user -> user.getId().equals(record.getUserId())).findFirst().ifPresent(user -> {
                UserVo userVo = UserVo.builder()
                        .username(user.getUsername())
                        .uid(user.getUid())
                        .avatar(user.getAvatar())
                        .age(user.getAge())
                        .name(user.getName())
                        .gender(user.getGender())
                        .intro(user.getIntro())
                        .id(user.getId())
                        .email(user.getEmail())
                        .score(user.getScore()).build();
                record.setUser(userVo);
            });

        }

        return Result.success(map);
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("dynamic.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Dynamic> list = dynamicService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Dynamic信息表", StandardCharsets.UTF_8);
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
    @SaCheckPermission("dynamic.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Dynamic> list = reader.readAll(Dynamic.class);

        dynamicService.saveBatch(list);
        return Result.success();
    }

}
