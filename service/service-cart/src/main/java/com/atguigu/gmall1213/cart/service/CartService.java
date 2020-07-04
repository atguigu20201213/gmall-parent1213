package com.atguigu.gmall1213.cart.service;

import com.atguigu.gmall1213.model.cart.CartInfo;

import java.util.List;

public interface CartService {
    // 添加购物车 用户Id，商品Id，商品数量。
    void addToCart(Long skuId, String userId, Integer skuNum);

    /**
     * 通过用户Id 查询购物车列表
     * 查询登录 未登录
     */
    List<CartInfo> getCartList(String userId, String userTempId);

    //更新选中状态
    void checkCart(String userId, Integer isChecked, Long skuId);

    /**
     * 删除购物车
     */
    void deleteCart(Long skuId, String userId);

    //根据用户Id 查询用户的购物车
    List<CartInfo> getCartCheckedList(String userId);
    /**
     * 根据用户Id查询购物车最新数据并放入缓存
     * @param userId
     * @return
     */
    List<CartInfo> loadCartCache(String userId);

}
