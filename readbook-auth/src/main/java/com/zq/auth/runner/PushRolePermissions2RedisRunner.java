package com.zq.auth.runner;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zq.auth.constant.RedisKeyConstants;
import com.zq.auth.mapper.PermissionMapper;
import com.zq.auth.mapper.RoleMapper;
import com.zq.auth.mapper.RolePermissionMapper;
import com.zq.auth.pojo.Permission;
import com.zq.auth.pojo.Role;
import com.zq.auth.pojo.RolePermission;
import com.zq.framework.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class PushRolePermissions2RedisRunner implements ApplicationRunner {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;


    @Override
    public void run(ApplicationArguments args) {
        RLock lock = redissonClient.getLock("pushRolePermissions2RedisLock");
        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                log.info("==> 服务启动，开始同步角色权限数据到 Redis 中...");
                List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<Role>().eq(Role::getStatus, 0));
                if (!CollectionUtils.isEmpty(roles)) {
                    //ids
                    List<Long> list = roles.stream().map(Role::getId).toList();
                    //根据角色获取所有的权限
                    List<RolePermission> rolePermissions = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                            .in(RolePermission::getRoleId, list));

                    //根据roleId分不同的权限
                    Map<Long, List<Long>> map = rolePermissions.stream()
                            .collect(Collectors.groupingBy(RolePermission::getRoleId, Collectors.mapping(RolePermission::getPermissionId, Collectors.toList())));
                    List<Permission> permissions = permissionMapper.selectList(new LambdaQueryWrapper<Permission>().eq(Permission::getStatus, 0)
                            .eq(Permission::getType, 3));
                    Map<Long, Permission> permissionMap = permissions.stream().collect(Collectors.toMap(Permission::getId, permission -> permission));
                    // 组织 角色ID-权限 关系
                    Map<String, List<String>> roleIdPermissionMap = Maps.newHashMap();
                    // 循环所有角色
                    roles.forEach(roleDO -> {
                        // 当前角色 ID
                        String roleKey = roleDO.getRoleKey();
                        Long roleId = roleDO.getId();
                        // 当前角色 ID 对应的权限 ID 集合
                        List<Long> permissionIds = map.get(roleId);
                        if (CollUtil.isNotEmpty(permissionIds)) {
                            List<String> perDOS = Lists.newArrayList();
                            permissionIds.forEach(permissionId -> {
                                // 根据权限 ID 获取具体的权限 DO 对象
                                Permission permission = permissionMap.get(permissionId);
                                if (Objects.nonNull(permission)) {
                                    perDOS.add(permission.getPermissionKey());
                                }
                            });
                            roleIdPermissionMap.put(roleKey, perDOS);
                        }
                    });

                    // 同步至 Redis 中，方便后续网关查询鉴权使用
                    roleIdPermissionMap.forEach((roleId, permissionList) -> {
                        String key = RedisKeyConstants.buildRolePermissionsKey(roleId);
                        redisTemplate.opsForValue().set(key, JsonUtils.toJsonString(permissions));
                    });
                }

                log.info("==> 服务启动，成功同步角色权限数据到 Redis 中...");
            }else {
                log.info("同步角色权限已执行");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}