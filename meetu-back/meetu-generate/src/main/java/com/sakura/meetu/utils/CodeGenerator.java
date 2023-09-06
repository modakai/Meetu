package com.sakura.meetu.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.simple.SimpleDataSource;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.sql.SQLException;
import java.util.*;

/**
 * 代码生成器
 *
 * @author sakura
 * @create 2023/5/21 21:02
 */
@Slf4j
public class CodeGenerator {

    /**
     * Mapper 文件的目录
     */
    public static final String MAPPER_XML_PATH = "/src/main/resources/mapper/";
    /**
     * 指定放在那个目录中
     */
    public static final String JAVA_CODE_PATH = "/src/main/java/";
    /**
     * 表名
     */
    private static final String TABLE = "praise";
    /**
     * 菜单名称
     */
    private static final String MODULE_NAME = "点赞";
    /**
     * java代码的包名
     */
    private static final String PACKAGE_NAME = "com.sakura.meetu";
    /**
     * 作者
     */
    private static final String AUTHOR = "sakura";
    /**
     * 生成代码的路径 父级
     */
    private static final String PROJECT_PATH = System.getProperty("user.dir") + "/meetu-generate";
    /**
     * Vue 代码生成的路径
     */
    private static final String VUE_CODE_PATH = "D:\\JAVA\\project\\MeetU\\code\\meetu-manager\\src\\views\\";

    /******************************************************* 下面的属性值, 可以不用动  ***********************************************************/
    /**
     * Vue API 接口
     */
    private static final String VUE_API_PATH = "D:\\JAVA\\project\\MeetU\\code\\meetu-manager\\src\\api\\";
    private static final String SPACE8 = "        ";

    public static void main(String[] args) {
        // 生成Java代码
        generateJava(TABLE);

        // 生成 Vue 代码 加 AXIOS 封装
        generateVue(TABLE);
    }

    private static void generateVue(String table) {
        List<TableColumn> tableColumns = getTableColumns(table);

        Map<String, String> map = new HashMap<>();
        map.put("lowerEntity", getLowerEntity(table));  // 接口前缀

        String vueTableBody = getVueTableBody(tableColumns);
        map.put("tableBody", vueTableBody);

        String vueFormBody = getVueFormBody(tableColumns);
        map.put("dialog-form", vueFormBody);

        map.put("moduleName", MODULE_NAME);

        // 读取模板，生成代码
        String vueTemplate = ResourceUtil.readUtf8Str("templates/vue.templates");
        String vueApiTemplate = ResourceUtil.readUtf8Str("templates/vueApi.templates");

        // 生成页面代码 替换模板中 {} 括号对应的 map 集合的key值
        //  例如: 模板中有一个代码是: {moduleName} 那么调用format()方法后 会将 map 集合中 key 对应的 value 值替换掉整个 {moduleName}
        String vuePage = StrUtil.format(vueTemplate, map);  // vuePage是替换字符串模板后的内容
        String vueApiPage = StrUtil.format(vueApiTemplate, map);  // vuePage是替换字符串模板后的内容

        String entity = getEntity(table);

        FileUtil.writeUtf8String(vuePage, VUE_CODE_PATH + entity + ".vue");
        FileUtil.writeUtf8String(vueApiPage, VUE_API_PATH + getLowerEntity(table) + "Api.js");
        log.debug("==========================" + entity + ".vue文件生成完成！！！==========================");
    }

    /**
     * 生成Vue 中的表格列表
     *
     * @param tableColumnList 表数据结合
     */
    private static String getVueTableBody(List<TableColumn> tableColumnList) {
        StringBuilder builder = new StringBuilder();
        for (TableColumn tableColumn : tableColumnList) {
            if (tableColumn.getColumnName().equalsIgnoreCase("id") && StrUtil.isBlank(tableColumn.getColumnComment())) {
                tableColumn.setColumnComment("编号");
            }
            if (tableColumn.getColumnName().equalsIgnoreCase("deleted") || tableColumn.getColumnName().equalsIgnoreCase("create_time")
                    || tableColumn.getColumnName().equalsIgnoreCase("update_time")) {  // 排除deleted create_time  update_time 这个无需关注的字段
                continue;
            }
            // 拿到字段名的 驼峰命名法
            String camelCaseName = StrUtil.toCamelCase(tableColumn.getColumnName());
            if (tableColumn.getColumnName().endsWith("img")) {
                builder.append(SPACE8).append("<el-table-column label=\"图片\">\r\n<template #default=\"scope\">\r\n<el-image preview-teleported style=\"width: 100px; height: 100px\" :src=\"scope.row.").append(camelCaseName).append("\" :preview-src-list=\"[scope.row.img]\"></el-image>\r\n</template>\r\n</el-table-column>\r\n");
            } else if (tableColumn.getColumnName().endsWith("file")) {
                builder.append(SPACE8).append("<el-table-column label=\"文件\">\r\n<template #default=\"scope\"> \r\n<a :href=\"scope.row.").append(camelCaseName).append("\" target=\"_blank\" style=\"text-decoration: none; color: dodgerblue\">点击下载</a></template></el-table-column>\r\n");
            } else {
                builder.append(SPACE8).append("<el-table-column prop=\"").append(camelCaseName).append("\" label=\"").append(tableColumn.getColumnComment()).append("\"></el-table-column>\r\n");
            }

        }
        return builder.toString();
    }

    /**
     * 生成 表单数据
     *
     * @param tableColumnList 表数据集合
     */
    private static String getVueFormBody(List<TableColumn> tableColumnList) {
        StringBuilder builder = new StringBuilder();
        for (TableColumn tableColumn : tableColumnList) {
            if (tableColumn.getColumnName().equalsIgnoreCase("id")) {
                continue;
            }
            // 去掉一下不必要的字段
            if (tableColumn.getColumnName().equalsIgnoreCase("deleted") || tableColumn.getColumnName().equalsIgnoreCase("create_time")
                    || tableColumn.getColumnName().equalsIgnoreCase("update_time")) {  // 排除deleted create_time  update_time 这个无需关注的字段
                continue;
            }

            // 拼接对应的输入框
            String camelCaseName = StrUtil.toCamelCase(tableColumn.getColumnName());
            builder.append(SPACE8).append("<el-form-item prop=\"").append(camelCaseName).append("\" label=\"").append(tableColumn.getColumnComment()).append("\">\n");
            builder.append(SPACE8).append("  ").append("<el-input v-model=\"dialogData.formData.").append(camelCaseName).append("\" autocomplete=\"off\"></el-input>\n");
            builder.append(SPACE8).append("</el-form-item>\n");
        }
        return builder.toString();
    }


    /**
     * 读取 对应 表中的所有信息
     *
     * @param tableName 表名
     * @return 信息封装的集合
     */
    private static List<TableColumn> getTableColumns(String tableName) {
        DataProperties dbProp = createProperty();
        SimpleDataSource dataSource = new SimpleDataSource("jdbc:mysql://localhost:3306/information_schema", dbProp.username, dbProp.password);
        Db db = DbUtil.use(dataSource);
        List<TableColumn> tableColumnList = new ArrayList<>();
        try {
            List<Entity> columns = db.findAll(Entity.create("COLUMNS").set("TABLE_SCHEMA", dbProp.database).set("TABLE_NAME", tableName));
            //封装结构化的表数据信息
            for (Entity entity : columns) {
                String columnName = entity.getStr("COLUMN_NAME");  // 字段名称
                String dataType = entity.getStr("DATA_TYPE");  // 字段名称
                String columnComment = entity.getStr("COLUMN_COMMENT");  // 字段名称
                TableColumn tableColumn = TableColumn.builder().columnName(columnName).dataType(dataType).columnComment(columnComment).build();
                tableColumnList.add(tableColumn);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tableColumnList;
    }

    /**
     * 去除 实体的前缀
     *
     * @param tableName 表名
     * @return 去掉 t_ 或者 sys_ 下化线后的 小驼峰命名法的 名称
     */
    private static String getLowerEntity(String tableName) {
        tableName = tableName.replaceAll("t_", "").replaceAll("sys_", "");
        return StrUtil.toCamelCase(tableName);
    }

    /**
     * 将 表明 第一个字母转为大写
     *
     * @param tableName 表名
     * @return 表名的大驼峰命名法
     */
    private static String getEntity(String tableName) {
        String lowerEntity = getLowerEntity(tableName);
        return lowerEntity.substring(0, 1).toUpperCase() + lowerEntity.substring(1);
    }

    /**
     * 读取配置文件中的 数据源信息
     *
     * @return 数据源信息对象
     */
    private static DataProperties createProperty() {
        ClassPathResource resource = new ClassPathResource("generate.yml");
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(resource);
        Properties dbProp = yamlPropertiesFactoryBean.getObject();
        String url = dbProp.getProperty("spring.datasource.url");
        String database = url.substring(url.lastIndexOf("/") + 1, url.indexOf("?"));
        return new DataProperties(
                url,
                dbProp.getProperty("spring.datasource.username"),
                dbProp.getProperty("spring.datasource.password"),
                database
        );
    }

    /**
     * 生成 Java 的 MVC 模式的代码
     *
     * @param tableName 表名
     */
    private static void generateJava(String tableName) {
        DataProperties dbProp = createProperty();
        FastAutoGenerator.create(dbProp.url, dbProp.username, dbProp.password)
                .globalConfig(builder -> {
                    builder.author(AUTHOR) // 设置作者
                            .enableSwagger()
                            .disableOpenDir()
                            .outputDir(PROJECT_PATH + JAVA_CODE_PATH); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent(PACKAGE_NAME) // 设置父包名
                            .moduleName("") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, PROJECT_PATH + MAPPER_XML_PATH)); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.controllerBuilder().enableFileOverride().enableRestStyle().enableHyphenStyle()
                            .serviceBuilder().enableFileOverride()
                            .mapperBuilder().enableFileOverride().mapperAnnotation(Mapper.class)
                            .entityBuilder().enableFileOverride().enableLombok()
                            .logicDeleteColumnName("deleted")
                            .addTableFills(new Column("create_time", FieldFill.INSERT))
                            .addTableFills(new Column("update_time", FieldFill.INSERT_UPDATE));
                    builder.addInclude(tableName) // 设置需要生成的表名
                            .addTablePrefix("t_", "sys_"); // 设置过滤表前缀
                })
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
