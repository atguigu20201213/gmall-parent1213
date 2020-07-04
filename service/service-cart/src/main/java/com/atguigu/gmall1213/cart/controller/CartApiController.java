package com.atguigu.gmall1213.cart.controller;

import com.atguigu.gmall1213.cart.service.CartService;
import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.common.util.AuthContextHolder;
import com.atguigu.gmall1213.model.cart.CartInfo;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartApiController {

  @Autowired private CartService cartService;

  /** 添加购物车 */
  // http://cart.gmall.com/addCart.html?skuId=30&skuNum=1 页面提交过来的数据，应该属于web-all项目的！
  @PostMapping("addToCart/{skuId}/{skuNum}")
  public Result addToCart(
      @PathVariable("skuId") Long skuId,
      @PathVariable("skuNum") Integer skuNum,
      HttpServletRequest request) {
    // 如何获取userId   需要用户id  common-util 中有工具类
    // 需要一个用户Id ，是从网关传递过来的！ 用户信息都放入了header 中！common-util 中有工具类AuthContextHolder
    // 获取登录的用户Id，添加购物车的时候，一定会有登录的用户Id么？不一定！
    String userId = AuthContextHolder.getUserId(request);
    if (StringUtils.isEmpty(userId)) {
      // 获取临时用户Id
      userId = AuthContextHolder.getUserTempId(request);
    }
    cartService.addToCart(skuId, userId, skuNum);
    return Result.ok();
  }

  /**
   * 以下控制器 不需要通过web-all 来访问 查询购物车 获取购物车列表
   *
   * @param request
   * @return
   */
  @GetMapping("cartList")
  public Result cartList(HttpServletRequest request) {
    // 获取登录用户Id
    String userId = AuthContextHolder.getUserId(request);
    // 获取临时用户Id
    String userTempId = AuthContextHolder.getUserTempId(request);
    List<CartInfo> cartList = cartService.getCartList(userId, userTempId);
    return Result.ok(cartList);
  }

  // 更改选中状态控制器
  @GetMapping("checkCart/{skuId}/{isChecked}")
  public Result checkCart(
      @PathVariable Long skuId, @PathVariable Integer isChecked, HttpServletRequest request) {
    // 选中状态的变更  登录 未登录都可以

    // 先获取用户ID
    String userId = AuthContextHolder.getUserId(request);

    if (StringUtils.isEmpty(userId)) {
      userId = AuthContextHolder.getUserTempId(request);
    }
    // 调用方法
    cartService.checkCart(userId, isChecked, skuId);

    return Result.ok();
  }
  /**
   * 删除购物车方法
   *
   * @param skuId
   * @param request
   * @return
   */
  @DeleteMapping("deleteCart/{skuId}")
  public Result deleteCart(@PathVariable("skuId") Long skuId, HttpServletRequest request) {
    // 如何获取userId
    String userId = AuthContextHolder.getUserId(request);
    if (StringUtils.isEmpty(userId)) {
      // 获取临时用户Id
      userId = AuthContextHolder.getUserTempId(request);
    }
    cartService.deleteCart(skuId, userId);
    return Result.ok();
  }

  /**
   * 根据用户Id 查询购物车列表 送货清单数据
   *
   * @param userId
   * @return
   */
  @GetMapping("getCartCheckedList/{userId}")
  public List<CartInfo> getCartCheckedList(@PathVariable(value = "userId") String userId) {
    return cartService.getCartCheckedList(userId);
  }
  /**
   * @param userId
   * @return
   */
  @GetMapping("loadCartCache/{userId}")
  public Result loadCartCache(@PathVariable("userId") String userId) {
    cartService.loadCartCache(userId);
    return Result.ok();
  }
}
