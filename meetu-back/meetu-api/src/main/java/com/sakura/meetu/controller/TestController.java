package com.sakura.meetu.controller;

import com.sakura.meetu.service.TestService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sakura
 * @create 2023/5/21 15:34
 */
@RestController
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class TestController {

    private final TestService testService;

    @GetMapping("/test")
    public String test(){
        return testService.test("meetu");
    }
}
