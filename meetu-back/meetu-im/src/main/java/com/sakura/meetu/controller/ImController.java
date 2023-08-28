package com.sakura.meetu.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.sakura.meetu.entity.Im;
import com.sakura.meetu.service.IImService;
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
 * 聊天信息表 前端控制器
 * </p>
 *
 * @author sakura
 * @since 2023-08-20
 */
@RestController
@RequestMapping("/api/im")
public class ImController {

    private final IImService imService;

    public ImController(IImService imService) {
        this.imService = imService;
    }

    @PostMapping
    public Result save(@RequestBody Im im) {
        imService.save(im);
        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody Im im) {
        imService.updateById(im);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        imService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        imService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    public Result findAll() {
        return Result.success(imService.list());
    }

    @GetMapping("/init/{limit}")
    public Result findAllInit(@PathVariable Integer limit) {
        return imService.initMessageList(limit);
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(imService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String username,
                           @RequestParam(defaultValue = "") String text,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        return imService.queryPage(username, text, pageNum, pageSize);
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Im> list = imService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Im信息表", StandardCharsets.UTF_8);
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
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Im> list = reader.readAll(Im.class);

        imService.saveBatch(list);
        return Result.success();
    }

}
