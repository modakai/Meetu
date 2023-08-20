package com.sakura.meetu.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sakura
 * @date 2023/7/6 20:20:45 周四
 */
public class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper om = new ObjectMapper();

    /**
     * 将 对象转换为 json
     *
     * @param o 对象
     * @return 对象转换的JSON字符串
     */

    public static String toJson(Object o) {
        return JSONUtil.toJsonStr(o);
    }

    /**
     * 数组或者集合转json
     *
     * @param o
     * @return
     */
    public static String toJsonArr(Object o) {
        return JSONUtil.toJsonPrettyStr(o);
    }

    /**
     * 根据 Hutool 工具类库 将 JSON 字符串转化为 Bean
     *
     * @param json  bean 的json 字符串
     * @param clazz bean 的类型
     * @return 转换后 Bena 对象
     */
    public static <T> T toBean(String json, Class<T> clazz) {
        JSONObject entries = JSONUtil.parseObj(json);
        try {
            return JSONUtil.toBean(entries, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> toArrBean(String json, Class<T> clazz) {
        JSONArray array = JSONUtil.parseArray(json);
        if (array.isEmpty()) {
            return null;
        }
        return array.stream()
                .map(item -> {
                    log.info("{}", item);
                    JSONObject parse = JSONUtil.parseObj(item);
                    T bean = JSONUtil.toBean(parse, clazz);
                    return bean;
                })
                .collect(Collectors.toList());
    }
}
