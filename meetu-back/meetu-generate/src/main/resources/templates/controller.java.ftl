package ${package.Controller};

<#--import cn.dev33.satoken.annotation.SaCheckPermission;-->
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import ${package.Parent}.utils.Result;
import org.springframework.web.multipart.MultipartFile;
import ${package.Service}.${table.serviceName};
import ${package.Entity}.${entity};

<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>

/**
* <p>
* ${table.comment!} 前端控制器
* </p>
*
* @author ${author}
* @since ${date}
*/
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@RequestMapping("<#if package.ModuleName?? && package.ModuleName != "">/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
    <#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass} {
    <#else>
public class ${table.controllerName} {
    </#if>

<#--    @Resource-->
    private final ${table.serviceName} ${table.entityPath}Service;

    public ${table.controllerName}(${table.serviceName} ${table.entityPath}Service) {
        this.${table.entityPath}Service = ${table.entityPath}Service;
    }

    @PostMapping
<#--    @SaCheckPermission("${table.entityPath}.add")-->
    public Result save(@RequestBody ${entity} ${table.entityPath}) {
        ${table.entityPath}Service.save(${table.entityPath});
        return Result.success();
    }

    @PutMapping
<#--    @SaCheckPermission("${table.entityPath}.edit")-->
    public Result update(@RequestBody ${entity} ${table.entityPath}) {
        ${table.entityPath}Service.updateById(${table.entityPath});
        return Result.success();
    }

    @DeleteMapping("/{id}")
<#--    @SaCheckPermission("${table.entityPath}.delete")-->
    public Result delete(@PathVariable Integer id) {
        ${table.entityPath}Service.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
<#--    @SaCheckPermission("${table.entityPath}.deleteBatch")-->
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        ${table.entityPath}Service.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
<#--    @SaCheckPermission("${table.entityPath}.list")-->
    public Result findAll() {
        return Result.success(${table.entityPath}Service.list());
    }

    @GetMapping("/{id}")
<#--    @SaCheckPermission("${table.entityPath}.list")-->
    public Result findOne(@PathVariable Integer id) {
        return Result.success(${table.entityPath}Service.getById(id));
    }

    @GetMapping("/page")
<#--    @SaCheckPermission("${table.entityPath}.list")-->
    public Result findPage(@RequestParam(defaultValue = "") String name,
                        @RequestParam Integer pageNum,
                        @RequestParam Integer pageSize) {
        QueryWrapper<${entity}> queryWrapper = new QueryWrapper<${entity}>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(${table.entityPath}Service.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
<#--    @SaCheckPermission("${table.entityPath}.export")-->
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<${entity}> list = ${table.entityPath}Service.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("${entity}信息表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }

    /**
    * excel 导入
    * @param file
    * @throws Exception
    */
    @PostMapping("/import")
<#--    @SaCheckPermission("${table.entityPath}.import")-->
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<${entity}> list = reader.readAll(${entity}.class);

        ${table.entityPath}Service.saveBatch(list);
        return Result.success();
    }

}
</#if>
