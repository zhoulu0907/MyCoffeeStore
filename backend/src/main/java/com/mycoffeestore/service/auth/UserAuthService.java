package com.mycoffeestore.service.auth;

import com.mycoffeestore.dto.auth.UserLoginDTO;
import com.mycoffeestore.dto.auth.UserRegisterDTO;
import com.mycoffeestore.vo.auth.LoginVO;
import com.mycoffeestore.vo.auth.UserVO;

/**
 * 用户认证服务接口
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
public interface UserAuthService {

    /**
     * 用户注册
     *
     * @param dto 注册请求DTO
     * @return 登录信息（包含Token）
     */
    LoginVO register(UserRegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto 登录请求DTO
     * @return 登录信息（包含Token）
     */
    LoginVO login(UserLoginDTO dto);

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserInfo(Long userId);

    /**
     * 用户退出登录
     *
     * @param userId 用户ID
     */
    void logout(Long userId);
}
