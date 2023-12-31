package com.sakura.meetu.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sakura.meetu.config.annotation.AutoLog;
import com.sakura.meetu.dto.PasswordDto;
import com.sakura.meetu.dto.UserDto;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.exception.ServiceException;
import com.sakura.meetu.service.IUserService;
import com.sakura.meetu.utils.PasswordEncoderUtil;
import com.sakura.meetu.utils.Result;
import com.sakura.meetu.utils.SessionUtils;
import com.sakura.meetu.validation.UserInfoUpdateGroup;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sakura
 * @since 2023-05-21
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PutMapping("/modify")
    public Result modifyUser(@RequestBody @Validated(UserInfoUpdateGroup.class) UserDto userDto) {
        // 修改用户的个人信息 头像 name age gender intro
        return userService.modifyUser(userDto);
    }

    @PutMapping("/password")
    public Result changePassword(@RequestBody PasswordDto passwordDto) {
        if (StrUtil.isBlank(passwordDto.getNewPassword()) || StrUtil.isBlank(passwordDto.getOldPassword()))
            return Result.error(Result.CODE_ERROR_401, "用户未登入");

        User loginUser = SessionUtils.getUser();
        User user = userService.getById(loginUser.getId());
        if (user != null) {
            // 匹配密码
            String password = user.getPassword();
            String oldPassword = passwordDto.getOldPassword();
            boolean matches = PasswordEncoderUtil.matches(oldPassword, password);
            if (!matches) {
                return Result.error(Result.CODE_ERROR_400, "旧密码错误");
            }

            // 修改密码
            UpdateWrapper<User> wrapper = new UpdateWrapper<User>()
                    .set("password", PasswordEncoderUtil.encodePassword(passwordDto.getNewPassword()))
                    .eq("id", user.getId());
            userService.update(wrapper);
            return Result.success();
        } else {
            return Result.error(Result.CODE_ERROR_401, "用户未登入");
        }
    }

    @PutMapping
    public Result updateUser(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    @ApiOperation(value = "批量删除用户")
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        return userService.removeBatch(ids);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("user.delete")
    public Result deleteOne(@PathVariable Integer id) {
        userService.removeById(id);
        return Result.success();
    }

    @ApiOperation(value = "查询全部用户")
    @GetMapping
    public Result findAll() {
        return Result.success(userService.list());
    }

    @ApiOperation(value = "根据ID查询全部用户")
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return userService.listOne(id);
    }

    @ApiOperation(value = "分页查询")
    @AutoLog("分页获取用户")
    @GetMapping("/page")
    public Result findPage(@RequestParam(defaultValue = "") String username,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().orderByDesc("id");
        queryWrapper.likeRight(!"".equals(username), "username", username);
        return Result.success(userService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 导出接口
     */
    @ApiOperation(value = "导出用户数据")
    @GetMapping("/export")
    @CrossOrigin
    public void export(HttpServletResponse response) {
        ExcelWriter writer = null;
        ServletOutputStream out = null;
        try {
            // 从数据库查询出所有的数据
            List<User> list = userService.list();
            // 在内存操作，写出到浏览器
            writer = ExcelUtil.getWriter(true);

            // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
            writer.write(list, true);

            // 设置浏览器响应的格式
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String fileName = URLEncoder.encode("User信息表-" + formatter.format(now), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            out = response.getOutputStream();
            writer.flush(out, true);
        } catch (Exception e) {
            log.error("导出用户接口出现异常 ", e);
            throw new ServiceException(Result.CODE_SYS_ERROR, "导出失败");
        } finally {
            // 关闭资源
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * excel 导入
     *
     * @param file
     * @throws Exception
     */
    @ApiOperation(value = "导入用户数据")
    @PostMapping("/import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<User> list = reader.readAll(User.class);

        return userService.insertUserBatch(list);
    }

}
