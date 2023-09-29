package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sakura.meetu.entity.Tag;
import com.sakura.meetu.service.ITagService;
import com.sakura.meetu.utils.Result;
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
 * @since 2023-09-12
 */
@RestController
@RequestMapping("/api/tag")
public class TagController {

    private final ITagService tagService;

    public TagController(ITagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public Result save(@RequestBody Tag tag) {
        tagService.save(tag);
        return Result.success();
    }

    @PutMapping
    @SaCheckPermission("tag.edit")
    public Result update(@RequestBody Tag tag) {
        tagService.updateById(tag);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("tag.delete")
    public Result delete(@PathVariable Integer id) {
        tagService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("tag.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        tagService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaIgnore
    public Result findAll() {
        return Result.success(tagService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(tagService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<Tag>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(tagService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("tag.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Tag> list = tagService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Tag信息表", StandardCharsets.UTF_8);
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
    @SaCheckPermission("tag.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Tag> list = reader.readAll(Tag.class);

        tagService.saveBatch(list);
        return Result.success();
    }

}
