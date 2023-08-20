package com.sakura.meetu.test.servicetes;

import com.sakura.meetu.service.IPermissionService;
import com.sakura.meetu.utils.Result;
import com.sakura.meetu.vo.PermissionVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author sakura
 * @date 2023/8/3 23:23:26 周四
 */
@SpringBootTest
public class PermissionServiceTest {

    @Autowired
    private IPermissionService permissionService;

    @Test
    @DisplayName("测试树化")
    void testTreeList() {
        Result tree = permissionService.tree();
        Object data = tree.getData();
        List<PermissionVo> list = (List<PermissionVo>) data;
        list.forEach(item -> {
            System.out.println(item.toString());
        });
    }
}
