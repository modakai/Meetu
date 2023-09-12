package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sakura.meetu.config.annotation.AutoLog;
import com.sakura.meetu.entity.Messages;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.service.IMessagesService;
import com.sakura.meetu.utils.Result;
import com.sakura.meetu.utils.SessionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sakura
 * @since 2023-09-13
 */
@RestController
@RequestMapping("/api/messages")
public class MessagesController {

    private final IMessagesService messagesService;

    public MessagesController(IMessagesService messagesService) {
        this.messagesService = messagesService;
    }

    @PostMapping
    @SaCheckPermission("messages.add")
    public Result save(@RequestBody Messages messages) {
        messagesService.save(messages);
        return Result.success();
    }

    @PutMapping
    @SaCheckPermission("messages.edit")
    public Result update(@RequestBody Messages messages) {
        messagesService.updateById(messages);
        return Result.success();
    }

    @AutoLog("更新消息已读")
    @PutMapping("/read")
    public Result updateReadStatus(@RequestParam(value = "messageId", required = false, defaultValue = "-1") Integer messageId) {
        User user = SessionUtils.getUser();
        if (messageId == -1) {
            // 设置当前的用户所有的消息通知为已读状态
            messagesService.update(new UpdateWrapper<Messages>().set("isread", 1).eq("user_id", user.getId()));
        } else {
            // 设置当前的用户所有的消息通知为已读状态
            messagesService.update(new UpdateWrapper<Messages>().set("isread", 1).eq("id", messageId).eq("user_id", user.getId()));
        }
        return Result.success();
    }

    @GetMapping("/unread")
    @SaIgnore
    public Result unread() {
        User user = SessionUtils.getUser();
        if (user != null) {
            // 查询当前登录用户所有未读的消息
            return Result.success(messagesService.list(new QueryWrapper<Messages>().eq("user_id", user.getId()).eq("isread", 0)));
        } else {
            return Result.success(Collections.EMPTY_LIST);
        }
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        messagesService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("messages.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        messagesService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaCheckPermission("messages.list")
    public Result findAll() {
        return Result.success(messagesService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("messages.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(messagesService.getById(id));
    }

    @GetMapping("/page")
    @SaCheckPermission("messages.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Messages> queryWrapper = new QueryWrapper<Messages>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        User currentUser = SessionUtils.getUser();
        String role = currentUser.getRole();
        if ("USER".equals(role)) {  // 用户
            queryWrapper.eq("user_id", currentUser.getId());  // select * from  dynamic where user_id = xxx
        }
        return Result.success(messagesService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("messages.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Messages> list = messagesService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Messages信息表", StandardCharsets.UTF_8);
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
    @SaCheckPermission("messages.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Messages> list = reader.readAll(Messages.class);

        messagesService.saveBatch(list);
        return Result.success();
    }

}
