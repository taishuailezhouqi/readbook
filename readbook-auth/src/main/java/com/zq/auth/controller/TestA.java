package com.zq.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RefreshScope
public class TestA {

   @Value("${test.type}")
   private String type;

    @GetMapping("/test1")
    public String test1(){
        System.out.println(type);
        return "s";
    }


}
