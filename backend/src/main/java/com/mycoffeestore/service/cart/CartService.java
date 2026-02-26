package com.mycoffeestore.service.cart;

import com.mycoffeestore.dto.cart.CartAddDTO;
import com.mycoffeestore.dto.cart.CartUpdateDTO;
import com.mycoffeestore.vo.cart.CartListVO;

/**
 * 购物车服务接口
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
public interface CartService {

    /**
     * 添加到购物车
     *
     * @param userId 用户ID
     * @param dto    添加请求DTO
     */
    void add(Long userId, CartAddDTO dto);

    /**
     * 从购物车移除
     *
     * @param userId 用户ID
     * @param cartId 购物车项ID
     */
    void remove(Long userId, Long cartId);

    /**
     * 更新购物车数量
     *
     * @param userId 用户ID
     * @param dto    更新请求DTO
     */
    void update(Long userId, CartUpdateDTO dto);

    /**
     * 获取购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    CartListVO list(Long userId);

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     */
    void clear(Long userId);
}
