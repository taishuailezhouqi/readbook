package com.zq.auth.constant;

public class RedisKeyConstants {

    /**
     * 验证码 KEY 前缀
     */
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification_code:";
    public static final String READBOOK_ID_GENERATOR_KEY = "readbook.id.generator";
    /**
     * 角色对应的权限集合 KEY 前缀
     */
    private static final String ROLE_PERMISSIONS_KEY_PREFIX = "role:permissions:";

    /**
     * 用户角色数据 KEY 前缀
     */
    private static final String USER_ROLES_KEY_PREFIX = "user:roles:";
    /**
     * 构建验证码 KEY
     */
    public static String buildVerificationCodeKey(String phone) {
        return VERIFICATION_CODE_KEY_PREFIX + phone;
    }

    /**
     * 构建用户-角色 Key
     */
    public static String buildUserRoleKey(Long userId) {
        return USER_ROLES_KEY_PREFIX + userId;
    }
    /**
     * 构建角色对应的权限集合 KEY
     */
    public static String buildRolePermissionsKey(String  roleKey) {
        return ROLE_PERMISSIONS_KEY_PREFIX + roleKey;
    }
}
