package com.zq.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.zq.auth.constant.RedisKeyConstants;
import com.zq.auth.constant.RoleConstants;
import com.zq.auth.enums.LoginTypeEnum;
import com.zq.auth.enums.ResponseCodeEnum;
import com.zq.auth.mapper.UserMapper;
import com.zq.auth.mapper.UserRoleMapper;
import com.zq.auth.pojo.User;
import com.zq.auth.pojo.UserRole;
import com.zq.auth.pojo.vo.UserLoginReqVO;
import com.zq.auth.service.UserService;
import com.zq.framework.common.response.Response;
import com.zq.framework.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.zq.auth.enums.LoginTypeEnum.VERIFICATION_CODE;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RedisTemplate<String,Object> redisTemplate;

    /**
     * 登陆或注册登陆
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {
        Integer type = userLoginReqVO.getType();
        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);
        Long userId;
        //验证码
        if (loginTypeEnum == VERIFICATION_CODE) {
            if (userLoginReqVO.getCode() == null) {
                return Response.fail(ResponseCodeEnum.PARAM_NOT_VALID.getErrorCode(), "验证码不能为空");
            }
            String key = RedisKeyConstants.buildVerificationCodeKey(userLoginReqVO.getPhone());
            String code = (String) redisTemplate.opsForValue().get(key);
            Preconditions.checkArgument(code != null,"无效的验证码");
            Preconditions.checkArgument(code.equals(userLoginReqVO.getCode()),"验证码不正确");

            //判断是否第一次登陆
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, userLoginReqVO.getPhone()));
            if (user == null) {
                // 若此用户还没有注册，系统自动注册该用户
                userId = registerUser(userLoginReqVO.getPhone());
            }else {
                userId = user.getId();
            }
        }else {
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, userLoginReqVO.getPhone()));
            if (user == null) {
                return Response.fail("未注册用户");
            }
            userId = user.getId();
        }
        StpUtil.login(userId);
        SaTokenInfo token = StpUtil.getTokenInfo();
        return Response.success(token.tokenValue);
    }


    public Long registerUser(String phone) {
        // 获取全局自增的readbook ID
        Long readBookId = redisTemplate.opsForValue().increment(RedisKeyConstants.READBOOK_ID_GENERATOR_KEY);

        User user = User.builder()
                .phone(phone)
                .readBookId(String.valueOf(readBookId)) // 自动生成小红书号 ID
                .nickname("红薯" + readBookId) // 自动生成昵称, 如：小红薯10000
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);

        //分配角色
        // 给该用户分配一个默认角色
        UserRole userRoleDO = UserRole.builder()
                .userId(user.getId())
                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userRoleMapper.insert(userRoleDO);

        // 将该用户的角色 ID 存入 Redis 中
        List<Long> roles = Lists.newArrayList();
        roles.add(RoleConstants.COMMON_USER_ROLE_ID);
        String userRolesKey = RedisKeyConstants.buildUserRoleKey(user.getId());
        redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

        return user.getId();
    }
}
