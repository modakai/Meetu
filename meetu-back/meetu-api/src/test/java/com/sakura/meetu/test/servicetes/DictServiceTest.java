package com.sakura.meetu.test.servicetes;

import com.sakura.meetu.entity.Dict;
import com.sakura.meetu.service.IDictService;
import com.sakura.meetu.utils.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author sakura
 * @date 2023/8/13 17:17:26 周日
 */
@SpringBootTest
public class DictServiceTest {

    @Autowired
    private IDictService dictService;

    @Test
    @DisplayName("测试根据Type查询字典")
    void testListTypeAll() {
        List<Dict> dictList = dictService.list();
        String json = JsonUtil.toJson(dictList);
        Dict dict = new Dict();
        dict.setType("icon");
        dict.setId(1);
        dict.setContent("home");
        String json1 = JsonUtil.toJson(dict);
        System.out.println(JsonUtil.toBean(json1, Dict.class).getType());
    }
}
