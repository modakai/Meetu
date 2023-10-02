package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sakura.meetu.config.annotation.AutoLog;
import com.sakura.meetu.entity.Notice;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.service.INoticeService;
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
 * @since 2023-08-31
 */
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final INoticeService noticeService;

    public NoticeController(INoticeService noticeService) {
        this.noticeService = noticeService;
    }


    @AutoLog("获取已发布的系统公告")
    @GetMapping("/release/list")
    @SaIgnore
    public Result getReleaseNoticeList() {
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1);
        return Result.success(noticeService.list(queryWrapper));
    }

    @PostMapping
    @SaCheckPermission("notice.add")
    public Result save(@RequestBody Notice notice) {
        User user = SessionUtils.getUser();
        notice.setUserid(user.getId());
        notice.setUser(user.getName());
        noticeService.save(notice);

        return Result.success();
    }

    @PutMapping
    @SaCheckPermission("notice.edit")
    public Result update(@RequestBody Notice notice) {
        noticeService.updateById(notice);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("notice.delete")
    public Result delete(@PathVariable Integer id) {
        noticeService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("notice.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        noticeService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaCheckPermission("notice.list")
    public Result findAll() {
        return Result.success(noticeService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("notice.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(noticeService.getById(id));
    }

    @GetMapping("/page")
    @SaCheckPermission("notice.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<Notice>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(noticeService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("notice.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Notice> list = noticeService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Notice信息表", StandardCharsets.UTF_8);
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
    @SaCheckPermission("notice.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Notice> list = reader.readAll(Notice.class);

        noticeService.saveBatch(list);
        return Result.success();
    }

}
