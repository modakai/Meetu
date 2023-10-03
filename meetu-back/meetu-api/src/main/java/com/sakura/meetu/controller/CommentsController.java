package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sakura.meetu.constants.Constant;
import com.sakura.meetu.entity.Comments;
import com.sakura.meetu.entity.Dynamic;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.service.ICommentsService;
import com.sakura.meetu.service.IMessagesService;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.service.impl.DynamicServiceImpl;
import com.sakura.meetu.utils.Result;
import com.sakura.meetu.utils.SessionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sakura
 * @since 2023-09-10
 */
@RestController
@RequestMapping("/api/comments")
public class CommentsController {

    private final ICommentsService commentsService;
    private final IUserService userService;

    private final IMessagesService messagesService;

    public CommentsController(ICommentsService commentsService, IUserService userService, IMessagesService messagesService) {
        this.commentsService = commentsService;
        this.userService = userService;
        this.messagesService = messagesService;
    }

    @PostMapping
    @Transactional(rollbackFor = RuntimeException.class)
    public Result save(@RequestBody Comments comments) {
        User user = SessionUtils.getUser();
        comments.setUserId(user.getId());
        comments.setTime(DateUtil.now());
        // 读取IP
//        Dict ipAndCity = IpUtils.getIPAndCity();
//        comments.setLocation(ipAndCity.get("city").toString());
        // 添加评论
        commentsService.save(comments);

        // 更新用户积分
        user.setScore(user.getScore() + 2);
        userService.updateById(user);

        ThreadUtil.execute(() -> {
            DynamicServiceImpl dynamicService = SpringUtil.getBean(DynamicServiceImpl.class);
            Dynamic dynamic = dynamicService.getById(comments.getDynamicId());
            Integer userId = dynamic.getUserId();
            Integer puserId = comments.getPuserId();
            Integer toUserId = null;
            // 说明是一级评论
            if (puserId == null) {
                // 一级评论要将构建的消息发给动态 所属用户的账号
                toUserId = userId;
            } else {
                toUserId = puserId;
            }

            // 不发给用户自己本身
            if (!toUserId.equals(user.getId())) {
                String builder = dynamic.getName() + "," + comments.getContent();
                messagesService.createMessages(user, comments.getDynamicId(), toUserId, builder, Constant.OPERATION_COMMENTS);
            }

        });

        return Result.success();
    }

    @PutMapping
    public Result update(@RequestBody Comments comments) {
        commentsService.updateById(comments);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = RuntimeException.class)
    public Result delete(@PathVariable Integer id) {
        // 删除 子节点
        List<Integer> list = commentsService
                .list(new LambdaQueryWrapper<Comments>().eq(Comments::getPid, id))
                .stream().map(Comments::getId).collect(Collectors.toList());
        list.add(id);
        commentsService.removeByIds(list);
        return Result.success();
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("comments.deleteBatch")
    @Transactional(rollbackFor = RuntimeException.class)
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        commentsService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping("/tree")
    @SaIgnore
    public Result treeComments(@RequestParam Integer dynamicId) {
        return Result.success(commentsService.listCommentsAndUser(dynamicId));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(commentsService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(commentsService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        return commentsService.listPage(name, pageNum, pageSize);
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("comments.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Comments> list = commentsService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Comments信息表", StandardCharsets.UTF_8);
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
    @SaCheckPermission("comments.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Comments> list = reader.readAll(Comments.class);

        commentsService.saveBatch(list);
        return Result.success();
    }

}
