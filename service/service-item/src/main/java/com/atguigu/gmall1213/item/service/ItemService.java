package com.atguigu.gmall1213.item.service;

import java.util.Map;

public interface ItemService {
    //通过skuId 获取数据
    Map<String, Object> getBySkuId(Long skuId);
}
