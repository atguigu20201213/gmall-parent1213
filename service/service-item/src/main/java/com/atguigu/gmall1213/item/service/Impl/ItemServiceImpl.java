package com.atguigu.gmall1213.item.service.Impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1213.item.service.ItemService;
import com.atguigu.gmall1213.model.product.BaseCategoryView;
import com.atguigu.gmall1213.model.product.SkuInfo;
import com.atguigu.gmall1213.model.product.SpuInfo;
import com.atguigu.gmall1213.model.product.SpuSaleAttr;
import com.atguigu.gmall1213.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public Map<String, Object> getBySkuId(Long skuId) {
        Map<String, Object> result = new HashMap<>();
        // 通过skuId 获取skuInfo 对象数据
        SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
        // 通过skuId，spuId 获取销售属性集合数据
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
        // 通过category3Id 获取分类数据
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        // 通过skuId 获取价格数据
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
        // 根据spuId 获取 由销售属性值Id 和skuId 组成的map 集合数据。
        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
        // map.put("55|57","30")
        // 需要将skuValueIdsMap 转化为Json 字符串，给页面使用!  Map --->Json
        String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
        // 保存数据： 这个位置需要根据前端结合在一起使用！ key="" 是谁 {key 是由商品详情页面决定的，${skuInfo}}
        // 这个页面在哪，后续会给大家提供！ 先以课件为准。
        // 保存三级分类数据
        result.put("categoryView",categoryView);
        // 保存商品价格
        result.put("price",skuPrice);
        // valuesSkuJson 表示在页面中，需要一个json 字符串。这个字符串是谁？ {"55|57":"30","54|57":"31"....}
        // 保存销售属性值Id 和 skuId 组成的json 字符串
        result.put("valuesSkuJson",valuesSkuJson);
        // 保存销售属性-销售属性值
        result.put("spuSaleAttrList",spuSaleAttrListCheckBySku);
        // 保存skuInfo 数据 {包含了skuImage}
        result.put("skuInfo",skuInfo);
        /*categoryView
            1，Sku基本信息
                result.put("1","Sku基本信息")
            2，Sku图片信息
                result.put("2","Sku图片信息")
            3，Sku分类信息
                result.put("3","Sku分类信息")
            4，Sku销售属性相关信息
                result.put("4","Sku销售属性相关信息")
            5，Sku价格信息
                result.put("5","Sku价格信息")
         */

        return result;
    }
}
