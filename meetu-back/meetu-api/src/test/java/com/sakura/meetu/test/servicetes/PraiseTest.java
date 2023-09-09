package com.sakura.meetu.test.servicetes;

import com.sakura.meetu.service.IPraiseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author sakura
 * @date 2023/9/9 17:17:29 周六
 */
@SpringBootTest
public class PraiseTest {

    @Autowired
    private IPraiseService praiseService;

    @Test
    void test1() {
        System.out.println(praiseService.listPage(null, null, 1, 10));
    }

}
