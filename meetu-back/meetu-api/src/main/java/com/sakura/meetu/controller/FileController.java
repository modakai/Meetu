package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sakura.meetu.dto.FileDto;
import com.sakura.meetu.entity.File;
import com.sakura.meetu.service.IFileService;
import com.sakura.meetu.service.OssService;
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
 * @author sakura
 * @date 2023/7/15 16:16:10 周六
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final OssService ossService;

    private final IFileService fileService;

    public FileController(OssService ossService, IFileService fileService) {
        this.ossService = ossService;
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (ObjectUtil.isEmpty(file)) {
            return Result.error(Result.CODE_ERROR_400, "请选择上传文件!");
        }
        File resultFile = ossService.uploadImg(file);
        // 报错图片信息
        fileService.save(resultFile);
        return Result.success(resultFile.getUrl());
    }

    @GetMapping("/download/before")
    public Result beforeDownLoad(@RequestParam String fileName) {
        return fileService.beforeDownLoadFile(fileName);
    }

    @GetMapping("/download/file")
    public void download(@RequestParam String fileName, HttpServletResponse response) {
        ossService.downloadFile(fileName, response);
    }


    @DeleteMapping
    @SaCheckPermission("file.delete")
    public Result delete(@RequestBody FileDto fileDto) {
        return fileService.removeFileAndMq(fileDto);
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("file.deleteBatch")
    public Result deleteBatch(@RequestBody FileDto fileDto) {

        return fileService.removeFileBatch(fileDto);
    }

    @GetMapping
    @SaCheckPermission("file.list")
    public Result findAll() {
        return Result.success(fileService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("file.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(fileService.getById(id));
    }

    @GetMapping("/page")
    @SaCheckPermission("file.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<File> queryWrapper = new QueryWrapper<File>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(fileService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("file.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<File> list = fileService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("File信息表", StandardCharsets.UTF_8);
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
    @SaCheckPermission("file.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<File> list = reader.readAll(File.class);

        fileService.saveBatch(list);
        return Result.success();
    }
}
