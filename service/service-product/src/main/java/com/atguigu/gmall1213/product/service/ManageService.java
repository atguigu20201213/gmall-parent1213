package com.atguigu.gmall1213.product.service;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall1213.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ManageService {

    //查询所有的一级分类数据
    List<BaseCategory1> getCategory1();
    //根据一级分类Id 查询二级分类数据
    List<BaseCategory2> getCategory2(Long category1Id);

    /**根据二级分类Id 查询三级分类数据
     *
     * @param category2Id
     * @return
     */
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 根据分类Id 查询平台属性数据
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
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
    //查询skuInfo 列表
    IPage<SkuInfo> selectPage(Page<SkuInfo> skuInfoPage);

    //获取所有的销售属性数据
    List<BaseSaleAttr> getBaseSaleAttrList();
    /**
     * 保存spuInfo 数据。
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);
    /**
     * 根据spuId查询spuImage列表。
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageList( Long spuId);
    /**
     * 根据spuId查询销售属性列表
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);
    /**
     * 保存skuInfo 数据
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);
    /**
     * 根据skuId 实现商品上架
     * @param skuId
     */
    void onSale(Long skuId);
    /**
     * 根据skuId 实现商品下架
     * @param skuId
     */
    void cancelSale(Long skuId);
    /**
     * 根据skuId 查询数据
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 根据三级分类Id 来获取分类名称
     *
     * @param category3Id
     * @return
     */
    BaseCategoryView getBaseCategoryViewByCategory3Id(Long category3Id);

    /**
     * 通过skuId 查询价格
     * @param skuId
     * @return
     */
    BigDecimal getSkuPriceBySkuId(Long skuId);
    /**
     * 根据skuId spuId 查询销售属性集合数据
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    /**
     * 根据spuId 查询数据
     * map.put("value_ids","skuId")
     * @param spuId
     * @return
     */
    Map getSkuValueIdsMap(Long spuId);

    /**
     * 获取全部分类信息
     * @return
     */
    List<JSONObject> getBaseCategoryList();

}

