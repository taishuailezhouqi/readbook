package com.zq.auth.service;

import com.zq.auth.pojo.vo.UserLoginReqVO;
import com.zq.framework.common.response.Response;

public interface UserService {
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);
}
