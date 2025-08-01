package com.zq.auth.controller;

import com.zq.auth.pojo.User;
import com.zq.framework.common.response.Response;
import com.zq.framework.log.aspect.ApiOperationLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class TestController {

    @GetMapping("/test")
    @ApiOperationLog(description = "测试接口")
    public Response<String> test() {
        return Response.success("Hello, zq");
    }


    @GetMapping("/test2")
    @ApiOperationLog(description = "测试接口2")
    public Response<User> test2() {
        return Response.success(User.builder()
                .nickName("zq")
                .createTime(LocalDateTime.now())
                .build());
    }
}
