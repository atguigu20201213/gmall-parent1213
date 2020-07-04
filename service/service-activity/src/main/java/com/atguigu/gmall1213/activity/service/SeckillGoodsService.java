package com.atguigu.gmall1213.activity.service;

import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.model.activity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {

    /**
     * 返回全部列表
     *
     * @return
     */
    List<SeckillGoods> findAll();


    /**
     * 根据ID获取实体
     *
     * @param skuId
     * @return
     */
    SeckillGoods getSeckillGoodsBySkuId(Long skuId);

    //预下单
    void seckillOrder(Long skuId, String userId);


    /***
     * 根据商品id与用户ID查看订单信息
     * @param skuId
     * @param userId
     * @return
     */
    Result checkOrder(Long skuId, String userId);

}


