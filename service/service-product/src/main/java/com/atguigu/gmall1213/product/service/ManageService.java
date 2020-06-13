package com.atguigu.gmall1213.product.service;


import com.atguigu.gmall1213.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;



import java.util.List;

public interface ManageService {


    List<BaseCategory1> getCategory1();
    List<BaseCategory2> getCategory2(Long category1Id);
    List<BaseCategory3> getCategory3(Long category2Id);
    List<BaseAttrInfo> getAttrInfoList(Long category1Id,Long category2Id,Long category3Id);

    //大保存  平台属性 平台属性值
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    // 根据平台属性 获取属性值
    BaseAttrInfo getAttrInfo(Long attrId);

    /**
     * 分页查询 多个spuInfo 必须指定，查询第几页，每页显示的数据条数，是否有抽出条件 {category3Id=?}。
     * http://api.gmall.com/admin/product/{page}/{limit}?category3Id=61
     * @param spuInfoPageParam
     * @param spuInfo 因为spuInfo 实体类的属性中有一个属性叫category3Id | spring mvc 封装对象传值
     * @return
     */
    //查询spu列表

    IPage<SpuInfo> selectPage(Page<SpuInfo> spuInfoPageParam , SpuInfo spuInfo);
    //查询sku 列表
    IPage<SkuInfo> selectPage(Page<SkuInfo> skuInfoPage);

    //获取所有的销售属性数据
    List<BaseSaleAttr> getBaseSaleAttrList();

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuImage> getSpuImageList( Long spuId);

    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    void saveSkuInfo(SkuInfo skuInfo);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

}

