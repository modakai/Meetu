package com.sakura.meetu.utils;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Collections;
import java.util.Properties;

/**
 * 代码生成器
 *
 * @author sakura
 * @create 2023/5/21 21:02
 */
public class CodeGenerator {
    public static final String MAPPER_XML_PATH = "/src/main/resources/mapper/";
    /**
     * 指定放在那个目录中
     */
    public static final String JAVA_CODE_PATH = "/src/main/java/";
    /**
     * 表名
     */
    private static final String TABLE = "dynamic";
    /**
     * 菜单名称
     */
    private static final String MODULE_NAME = "动态";
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

    public static void main(String[] args) {
        generateJava(TABLE);
    }

    private static void generateJava(String tableName) {
        ClassPathResource resource = new ClassPathResource("generate.yml");
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(resource);
        Properties dbProp = yamlPropertiesFactoryBean.getObject();
        FastAutoGenerator.create(dbProp.getProperty("spring.datasource.url"),
                        dbProp.getProperty("spring.datasource.username"),
                        dbProp.getProperty("spring.datasource.password"))
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
                            .mapperBuilder().enableFileOverride()
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
