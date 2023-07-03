package com.sakura.meetu.utils;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean 的转换工具
 *
 * @author sakura
 * @create 2023-07-03 21:00:30
 */
public class BeanUtil {

    /**
     * 单个bean 转换
     *
     * @param source 转换类
     * @param clazz  要转换的类型
     * @return 转换成功后的 类对象
     */
    public static <V> V copyBean(Object source, Class<V> clazz) {
        //创建目标对象
        V result = null;
        try {
            result = clazz.getDeclaredConstructor().newInstance();
            //实现属性copy
            BeanUtils.copyProperties(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回结果
        return result;
    }


    public static <O, V> List<V> copyBeanList(List<O> list, Class<V> clazz) {
        return list.stream()
                .map(o -> copyBean(o, clazz))
                .collect(Collectors.toList());
    }
}