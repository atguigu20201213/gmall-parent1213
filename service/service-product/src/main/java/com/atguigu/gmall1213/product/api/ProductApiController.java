package com.atguigu.gmall1213.product.api;

import com.atguigu.gmall1213.model.product.BaseCategoryView;
import com.atguigu.gmall1213.model.product.SkuInfo;
import com.atguigu.gmall1213.model.product.SpuSaleAttr;
import com.atguigu.gmall1213.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * 整个类中的数据，都是为service-item 服务提供的
 * @date 2020/6/13 11:49
 */
@RestController
@RequestMapping("api/product")
public class ProductApiController {
    @Autowired
    private ManageService manageService;
    // 根据skuid 获取skuinfo   skuImage
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfoById(@PathVariable Long skuId) {
        return manageService.getSkuInfo(skuId);
    }
    // 根据三级分类Id 查询分类名称
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){
        return manageService.getBaseCategoryViewByCategory3Id(category3Id);

    }
    //根据skuId 获取商品价格
    // 根据skuId 获取商品价格
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        return manageService.getSkuPriceBySkuId(skuId);
    }
    // 回显销售属性-销售属性值
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId){
        return manageService.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    // 点击销售属性值进行切换
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){
        return manageService.getSkuValueIdsMap(spuId);
    }

}
