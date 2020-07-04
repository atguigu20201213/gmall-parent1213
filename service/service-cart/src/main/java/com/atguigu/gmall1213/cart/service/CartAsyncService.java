package com.atguigu.gmall1213.cart.service;

import com.atguigu.gmall1213.model.cart.CartInfo;

public interface CartAsyncService {
    /**
     * 修改购物车
     * @param cartInfo
     */
    void updateCartInfo(CartInfo cartInfo);

    /**
     * 保存购物车
     * @param cartInfo
     */
    void saveCartInfo(CartInfo cartInfo);

    /**
     * 删除购物车
     */

    void deleteCartInfo(String userId);

    /**
     * 选中状态变更
     */
    void checkCart(String userId, Integer isChecked, Long skuId);

    /**
     * 删除购物车
     */
    void deleteCartInfo(String userId, Long skuId);
}
