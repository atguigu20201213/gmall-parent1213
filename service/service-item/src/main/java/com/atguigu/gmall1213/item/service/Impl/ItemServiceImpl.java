package com.atguigu.gmall1213.item.service.Impl;

import com.atguigu.gmall1213.item.service.ItemService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class  ItemServiceImpl implements ItemService {

    @Override
    public Map<String, Object> getBySkuId(Long skuId) {

        Map<String, Object> result = new HashMap<>();
/*
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
