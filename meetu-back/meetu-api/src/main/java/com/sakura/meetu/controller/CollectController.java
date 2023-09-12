package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.sakura.meetu.constants.Constant;
import com.sakura.meetu.entity.Collect;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.service.ICollectService;
import com.sakura.meetu.service.IMessagesService;
import com.sakura.meetu.utils.Result;
import com.sakura.meetu.utils.SessionUtils;
import org.springframework.dao.DuplicateKeyException;
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
 * @since 2023-09-09
 */
@RestController
@RequestMapping("/api/collect")
public class CollectController {

    private final ICollectService collectService;

    private final IMessagesService messagesService;

    public CollectController(ICollectService collectService, IMessagesService messagesService) {
        this.collectService = collectService;
        this.messagesService = messagesService;
    }

    @PostMapping
    public Result save(@RequestBody Collect collect) {
        User user = SessionUtils.getUser();
        if (user == null) {
            return Result.error(Result.CODE_SYS_ERROR, "未登入");
        }
        collect.setUserId(user.getId());

        try {
            collectService.save(collect);
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException(e.getMessage());
        }

        ThreadUtil.execute(() -> {
            messagesService.createMessages(user, collect.getDynamicId(), collect.getUserId(), Constant.OPERATION_COLLECT);
        });

        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody Collect collect) {
        collectService.updateById(collect);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        collectService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("collect.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        collectService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaCheckPermission("collect.list")
    public Result findAll() {
        return Result.success(collectService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("collect.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(collectService.getById(id));
    }

    @GetMapping("/page")
    @SaCheckPermission("collect.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        User user = SessionUtils.getUser();
        Integer userId = null;
        if (user != null) {
            String role = user.getRole();
            if ("USER".equals(role)) {
                userId = user.getId();
            }
        }

        return Result.success(collectService.listPage(name, userId, pageNum, pageSize));
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("collect.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Collect> list = collectService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Collect信息表", StandardCharsets.UTF_8);
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
    @SaCheckPermission("collect.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Collect> list = reader.readAll(Collect.class);

        collectService.saveBatch(list);
        return Result.success();
    }

}
