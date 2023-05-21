package com.sakura.meetu.service.impl;

import com.sakura.meetu.service.TestService;
import org.springframework.stereotype.Service;

/**
 * @author sakura
 * @create 2023/5/21 16:44
 */
@Service
public class TestServiceImpl implements TestService {
    @Override
    public String test(String name) {
        return name + " hello!";
    }
}
