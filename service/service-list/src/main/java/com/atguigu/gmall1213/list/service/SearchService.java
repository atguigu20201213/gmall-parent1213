package com.atguigu.gmall1213.list.service;

import com.atguigu.gmall1213.model.list.SearchParam;
import com.atguigu.gmall1213.model.list.SearchResponseVo;

public interface SearchService {
    /**
     * 上架商品列表
     *
     * @param skuId
     */
    void upperGoods(Long skuId);

    /**
     * 上传多个skuid
     */
    void upperGoods();

    /**
     * 下架商品列表
     * @param skuId
     */
    void lowerGoods(Long skuId);


    /**
     * 更新热点
     * @param skuId
     */
    void incrHotScore(Long skuId);

    /**
     * 检索数据
     * @param searchParam
     * @return
     * @throws Exception
     */
    SearchResponseVo search(SearchParam searchParam) throws Exception;

}
