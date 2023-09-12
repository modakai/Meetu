package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sakura.meetu.entity.News;
import com.sakura.meetu.service.INewsService;
import com.sakura.meetu.utils.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sakura
 * @since 2023-09-11
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final INewsService newsService;

    public NewsController(INewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping
    @SaCheckPermission("news.add")
    public Result save(@RequestBody News news) {
        news.setTime(DateUtil.now());
        newsService.save(news);
        return Result.success();
    }

    @PutMapping
    @SaCheckPermission("news.edit")
    public Result update(@RequestBody News news) {
        newsService.updateById(news);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("news.delete")
    public Result delete(@PathVariable Integer id) {
        newsService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("news.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        newsService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaIgnore
    public Result findAll() {
        // 固定返回小于等于5的个数
        List<News> list = newsService.list();
        Set<News> set = new HashSet<>();  // set数据会自动去重
        if (list.size() <= 5) {
            return Result.success(list);
        } else {
            while (set.size() < 5) {
//                [1,2,3,4,5,6,7,8,9]
                int num = RandomUtil.randomInt(0, list.size());  // 获取一个随机的序号
                set.add(list.get(num));
            }
            return Result.success(set);
        }
    }

    @GetMapping("/{id}")
    @SaIgnore
    public Result findOne(@PathVariable Integer id) {
        News news = newsService.getById(id);
        if (news != null) {
            news.setView(news.getView() + 1);
            newsService.updateById(news);
        } else {
            return Result.error(Result.CODE_ERROR_404, "没有该新闻资讯");
        }

        return Result.success(news);
    }

    @GetMapping("/page")
    @SaCheckPermission("news.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<News> queryWrapper = new QueryWrapper<News>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(newsService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("news.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<News> list = newsService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("News信息表", StandardCharsets.UTF_8);
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
    @SaCheckPermission("news.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<News> list = reader.readAll(News.class);

        newsService.saveBatch(list);
        return Result.success();
    }

}
