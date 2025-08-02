package com.zq.auth.service;

import com.zq.auth.pojo.vo.SendVerificationCodeReqVO;
import com.zq.framework.common.response.Response;

public interface VerificationCodeService {
    Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO);

}
