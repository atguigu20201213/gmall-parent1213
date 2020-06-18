package com.atguigu.gmall1213.item.Controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.item.service.ItemService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
//关于商品详情的接口   数据提供者
@RequestMapping("api/item")
public class ItemApiController {
    @Autowired
    private ItemService itemService;


    @GetMapping("{skuId}")
    public Result getItem(@PathVariable Long skuId) {
        Map<String, Object> result = itemService.getBySkuId(skuId);

        return Result.ok(result);
    }


}
