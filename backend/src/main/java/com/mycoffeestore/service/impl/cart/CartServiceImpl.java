package com.mycoffeestore.service.impl.cart;

import com.mycoffeestore.dto.cart.CartAddDTO;
import com.mycoffeestore.dto.cart.CartUpdateDTO;
import com.mycoffeestore.entity.Cart;
import com.mycoffeestore.entity.Coffee;
import com.mycoffeestore.exception.BusinessException;
import com.mycoffeestore.mapper.CartMapper;
import com.mycoffeestore.mapper.CoffeeMapper;
import com.mycoffeestore.service.cart.CartService;
import com.mycoffeestore.vo.cart.CartItemVO;
import com.mycoffeestore.vo.cart.CartListVO;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 *
 * @author Backend Developer
 * @since 2024-02-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final CoffeeMapper coffeeMapper;

    @Override
    @Transactional
    public void add(Long userId, CartAddDTO dto) {
        // 查询咖啡信息
        Coffee coffee = coffeeMapper.selectOneById(dto.getCoffeeId());
        if (coffee == null) {
            throw new BusinessException(2001, "咖啡不存在");
        }

        if (coffee.getStatus() != 1) {
            throw new BusinessException(2003, "咖啡已下架");
        }

        if (coffee.getStock() < dto.getQuantity()) {
            throw new BusinessException(2002, "咖啡库存不足");
        }

        // 查询购物车是否已存在
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getCoffeeId, dto.getCoffeeId());

        Cart existCart = cartMapper.selectOneByQuery(queryWrapper);

        if (existCart != null) {
            // 更新数量
            Integer newQuantity = existCart.getQuantity() + dto.getQuantity();
            if (coffee.getStock() < newQuantity) {
                throw new BusinessException(2002, "咖啡库存不足");
            }
            existCart.setQuantity(newQuantity);
            existCart.setUpdateTime(LocalDateTime.now());
            cartMapper.update(existCart);
        } else {
            // 添加新项
            Cart cart = Cart.builder()
                    .userId(userId)
                    .coffeeId(dto.getCoffeeId())
                    .quantity(dto.getQuantity())
                    .price(coffee.getPrice())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            cartMapper.insert(cart);
        }
    }

    @Override
    @Transactional
    public void remove(Long userId, Long cartId) {
        // 查询购物车项
        Cart cart = cartMapper.selectOneById(cartId);
        if (cart == null) {
            throw new BusinessException(3002, "购物车项不存在");
        }

        // 验证是否属于当前用户
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限操作");
        }

        cartMapper.deleteById(cartId);
    }

    @Override
    @Transactional
    public void update(Long userId, CartUpdateDTO dto) {
        // 查询购物车项
        Cart cart = cartMapper.selectOneById(dto.getCartId());
        if (cart == null) {
            throw new BusinessException(3002, "购物车项不存在");
        }

        // 验证是否属于当前用户
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限操作");
        }

        // 查询咖啡库存
        Coffee coffee = coffeeMapper.selectOneById(cart.getCoffeeId());
        if (coffee.getStock() < dto.getQuantity()) {
            throw new BusinessException(2002, "咖啡库存不足");
        }

        // 更新数量
        cart.setQuantity(dto.getQuantity());
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.update(cart);
    }

    @Override
    public CartListVO list(Long userId) {
        // 查询购物车列表
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Cart::getUserId, userId)
                .orderBy(Cart::getCreateTime, false);

        List<Cart> cartList = cartMapper.selectListByQuery(queryWrapper);

        // 转换为VO
        List<CartItemVO> voList = cartList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 计算总数量和总金额
        int totalQuantity = voList.stream()
                .mapToInt(CartItemVO::getQuantity)
                .sum();

        BigDecimal totalPrice = voList.stream()
                .map(CartItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartListVO.builder()
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice)
                .items(voList)
                .build();
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(Cart::getUserId, userId);
        cartMapper.deleteByQuery(queryWrapper);
    }

    /**
     * 转换为VO
     */
    private CartItemVO convertToVO(Cart cart) {
        // 查询咖啡信息
        Coffee coffee = coffeeMapper.selectOneById(cart.getCoffeeId());

        CartItemVO vo = new CartItemVO();
        vo.setCartId(cart.getId());
        vo.setCoffeeId(cart.getCoffeeId());
        vo.setPrice(cart.getPrice());
        vo.setQuantity(cart.getQuantity());
        vo.setSubtotal(cart.getPrice().multiply(new BigDecimal(cart.getQuantity())));

        if (coffee != null) {
            vo.setCoffeeName(coffee.getName());
            vo.setImageUrl(coffee.getImageUrl());
            vo.setStock(coffee.getStock());
            vo.setStatus(coffee.getStatus());
        }

        return vo;
    }
}
