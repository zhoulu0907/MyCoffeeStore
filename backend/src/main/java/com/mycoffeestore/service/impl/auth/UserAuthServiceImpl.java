package com.mycoffeestore.service.impl.auth;

import com.mycoffeestore.dto.auth.UserLoginDTO;
import com.mycoffeestore.dto.auth.UserRegisterDTO;
import com.mycoffeestore.entity.User;
import com.mycoffeestore.exception.BusinessException;
import com.mycoffeestore.mapper.UserMapper;
import com.mycoffeestore.service.auth.UserAuthService;
import com.mycoffeestore.service.rbac.RbacService;
import com.mycoffeestore.util.JwtUtil;
import com.mycoffeestore.vo.auth.LoginVO;
import com.mycoffeestore.vo.auth.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 用户认证服务实现类
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RbacService rbacService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal("500.00");

    @Override
    @Transactional
    public LoginVO register(UserRegisterDTO dto) {
        // 检查用户名是否存在
        QueryWrapper checkUsername = QueryWrapper.create()
                .eq(User::getUsername, dto.getUsername());
        if (userMapper.selectOneByQuery(checkUsername) != null) {
            throw new BusinessException(1001, "用户名已存在");
        }

        // 检查邮箱是否存在
        QueryWrapper checkEmail = QueryWrapper.create()
                .eq(User::getEmail, dto.getEmail());
        if (userMapper.selectOneByQuery(checkEmail) != null) {
            throw new BusinessException(1002, "邮箱已注册");
        }

        // 创建用户
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .status(1)
                .balance(DEFAULT_BALANCE)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();

        userMapper.insert(user);

        // 注册时自动分配 user 角色
        rbacService.assignRole(user.getId(), "user");

        // 获取角色和权限
        String role = rbacService.getUserRoleCode(user.getId());
        List<String> permissions = rbacService.getUserPermissions(user.getId());

        // 生成Token（包含角色信息）
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);

        // 返回登录信息
        return LoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .token(token)
                .balance(user.getBalance())
                .role(role)
                .permissions(permissions)
                .build();
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        // 查询用户（支持用户名/邮箱/手机号登录）
        String account = dto.getAccount();
        // 先按用户名查
        QueryWrapper wrapper = QueryWrapper.create()
                .eq(User::getUsername, account)
                .eq(User::getIsDeleted, 0);
        User user = userMapper.selectOneByQuery(wrapper);
        // 再按邮箱查
        if (user == null) {
            wrapper = QueryWrapper.create()
                    .eq(User::getEmail, account)
                    .eq(User::getIsDeleted, 0);
            user = userMapper.selectOneByQuery(wrapper);
        }
        // 再按手机号查
        if (user == null) {
            wrapper = QueryWrapper.create()
                    .eq(User::getPhone, account)
                    .eq(User::getIsDeleted, 0);
            user = userMapper.selectOneByQuery(wrapper);
        }

        if (user == null) {
            throw new BusinessException(1003, "用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(1003, "用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(1004, "用户已被禁用");
        }

        // 更新最后登录信息
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.update(user);

        // 获取角色和权限
        String role = rbacService.getUserRoleCode(user.getId());
        List<String> permissions = rbacService.getUserPermissions(user.getId());

        // 生成Token（包含角色信息）
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);

        // 返回登录信息
        return LoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .token(token)
                .balance(user.getBalance())
                .role(role)
                .permissions(permissions)
                .build();
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 获取角色和权限
        String role = rbacService.getUserRoleCode(userId);
        List<String> permissions = rbacService.getUserPermissions(userId);

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setUserId(user.getId());
        vo.setRole(role);
        vo.setPermissions(permissions);
        return vo;
    }

    @Override
    public void logout(Long userId) {
        // JWT 无状态，客户端删除Token即可
        log.info("用户退出登录: userId={}", userId);
    }
}
