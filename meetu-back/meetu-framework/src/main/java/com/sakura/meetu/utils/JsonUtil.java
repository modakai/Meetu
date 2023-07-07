package com.sakura.meetu.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.apache.poi.ss.formula.functions.T;

/**
 * @author sakura
 * @date 2023/7/6 20:20:45 周四
 */
public class JsonUtil {

    /**
     * 将 对象转换为 json
     *
     * @param o 对象
     * @return 对象转换的JSON字符串
     */

    public static String toJson(Object o) {
        return JsonUtil.toJson(o);
    }

    /**
     * 根据 Hutool 工具类库 将 JSON 字符串转化为 Bean
     *
     * @param json  bean 的json 字符串
     * @param clazz bean 的类型
     * @return 转换后 Bena 对象
     */
    public static T toBean(String json, Class<T> clazz) {
        JSONObject entries = JSONUtil.parseObj(json);
        try {
            return JSONUtil.toBean(entries, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
