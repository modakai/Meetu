package com.sakura.meetu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sakura.meetu.entity.Role;
import com.sakura.meetu.entity.RolePermission;
import com.sakura.meetu.service.IRolePermissionService;
import com.sakura.meetu.service.IRoleService;
import com.sakura.meetu.utils.Result;
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
 * @since 2023-08-02
 */
@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final IRoleService roleService;
    private final IRolePermissionService rolePermissionService;

    public RoleController(IRoleService roleService, IRolePermissionService rolePermissionService) {
        this.roleService = roleService;
        this.rolePermissionService = rolePermissionService;
    }

    @PostMapping
    @SaCheckPermission("role.add")
    public Result save(@RequestBody Role role) {
        return roleService.saveRole(role);
    }

    @PutMapping
    @SaCheckPermission("role.edit")
    public Result update(@RequestBody Role role) {
        return roleService.updateRole(role);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("role.delete")
    public Result delete(@PathVariable Integer id) {
        roleService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    @SaCheckPermission("role.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        roleService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    @SaCheckPermission("role.list")
    public Result findAll() {
        return Result.success(roleService.list());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("role.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(roleService.getById(id));
    }

    @GetMapping("/page")
    @SaCheckPermission("role.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<Role>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        Page<Role> page = roleService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Role> roles = page.getRecords();
        // 查询对应的权限
        List<RolePermission> rolePermissionList = rolePermissionService.list();
        roles.forEach(role -> {
            role.setPermissionIds(
                    rolePermissionList.stream()
                            .filter(rolePermission -> rolePermission.getRoleId().equals(role.getId()))
                            .map(RolePermission::getPermissionId)
                            .collect(Collectors.toList())
            );
        });
        return Result.success(page);
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    @SaCheckPermission("role.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Role> list = roleService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Role信息表", StandardCharsets.UTF_8);
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
    @SaCheckPermission("role.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Role> list = reader.readAll(Role.class);

        roleService.saveBatch(list);
        return Result.success();
    }

}
