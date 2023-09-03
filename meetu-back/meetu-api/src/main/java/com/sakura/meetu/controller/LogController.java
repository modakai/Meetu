package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sakura.meetu.entity.Log;
import com.sakura.meetu.service.ILogService;
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
 * @since 2023-09-03
 */
@RestController
@RequestMapping("/api/log")
public class LogController {

    private final ILogService logService;

    public LogController(ILogService logService) {
        this.logService = logService;
    }

    @PostMapping
    @SaCheckPermission("log.add")
    public Result save(@RequestBody Log log) {
        logService.save(log);
        return Result.success();
    }

    @PutMapping
    @SaCheckPermission("log.edit")
    public Result update(@RequestBody Log log) {
        logService.updateById(log);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("log.delete")
    public Result delete(@PathVariable Integer id) {
        logService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("log.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        logService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaCheckPermission("log.list")
    public Result findAll() {
        return Result.success(logService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("log.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(logService.getById(id));
    }

    @GetMapping("/page")
    @SaCheckPermission("log.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Log> queryWrapper = new QueryWrapper<Log>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(logService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("log.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Log> list = logService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Log信息表", StandardCharsets.UTF_8);
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
    @SaCheckPermission("log.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Log> list = reader.readAll(Log.class);

        logService.saveBatch(list);
        return Result.success();
    }

}
