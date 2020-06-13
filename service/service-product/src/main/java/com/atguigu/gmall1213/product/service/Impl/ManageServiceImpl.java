package com.atguigu.gmall1213.product.service.Impl;

import com.atguigu.gmall1213.model.product.*;
import com.atguigu.gmall1213.product.mapper.*;

import com.atguigu.gmall1213.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {


    // 通常会调用mapper 层。
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;



    @Override

    public List<BaseCategory1> getCategory1() {
        // select * from base_category1; 表与实体类与mapper 名称对应！
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        // select * from base_category2 where category1_id=category1Id;
        QueryWrapper<BaseCategory2> baseCategory2QueryWrapper = new QueryWrapper<>();
        // 第一个参数，是实体类的属性名，还是字段名？
        baseCategory2QueryWrapper.eq("category1_id", category1Id);
        return baseCategory2Mapper.selectList(baseCategory2QueryWrapper);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        // select * from base_category3 where category2_id=category2Id;
        QueryWrapper<BaseCategory3> baseCategory3QueryWrapper = new QueryWrapper<>();
        baseCategory3QueryWrapper.eq("category2_id", category2Id);
        return baseCategory3Mapper.selectList(baseCategory3QueryWrapper);
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        /*
         如果说只根据分类Id 查询平台属性!
         select * from base_attr_info where category_id=category1Id and category_level = 1 or
         select * from base_attr_info where category_id=category2Id and category_level = 2 or
         select * from base_attr_info where category_id=category3Id and category_level = 3 or
         category_level 表示层级关系
         category1Id category_level = 1
         category2Id category_level = 2
         category3Id category_level = 3
        -----------------------------------------------------------------
         扩展功能： 我们需要根据分类Id ，需要得到属性名，最好还能得到属性值名称。
            通过分类Id 能够得到属性名，
         base_attr_value 属性值数据在这张表中！

         */
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id, category2Id, category3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        /* if (null!=baseAttrInfo){
         *//*
            一个是平台属性表；baseAttrInfo
            一个是平台属性值表： baseAttrValue
             *//*
            baseAttrInfoMapper.insert(baseAttrInfo);
            // 平台属性值插入的时候，可能存在多个值的去情况，具体是多少个值，需要看传递过来的数据。
            // 页面在传递平台属性值数据的时候，数据会自动封装到 BaseAttrInfo 中 这个属性中 attrValueList
            //  前台页面给封装好的！ int i = 1/0;
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            if (null!=attrValueList && attrValueList.size()>0){
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    // 页面在提交数据的时候，并没有给attrId 赋值，所以在此处需要手动赋值
                    // attrId = baseAttrInfo.getId();
                    baseAttrValue.setAttrId(baseAttrInfo.getId());
                    // 循环将数据添加到数据表中
                    baseAttrValueMapper.insert(baseAttrValue);
                }
            }
        }*/
        //操作的平台属性表
        if (baseAttrInfo.getId() != null) {
            //修改功能
            baseAttrInfoMapper.updateById(baseAttrInfo);
        } else {
            //插入数据
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        //删除数据有条件
        QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
        baseAttrValueQueryWrapper.eq("attr_id", baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueQueryWrapper);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (null != attrValueList && attrValueList.size() > 0) {
            for (BaseAttrValue baseAttrValue : attrValueList) {
                // 页面在提交数据的时候，并没有给attrId 赋值，所以在此处需要手动赋值
                // attrId = baseAttrInfo.getId();
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                // 循环将数据添加到数据表中
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }

    }

    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        //不能直接返回   需要的是集合
        //平台属性值不是数据库  需要赋值
        if (null != baseAttrInfo) {
            QueryWrapper<BaseAttrValue> baseAttrInfoQueryWrapper = new QueryWrapper<>();
            baseAttrInfoQueryWrapper.eq("attr_id", attrId);
            List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.selectList(baseAttrInfoQueryWrapper);
            //将平台属性值结合放入 basseAttrInfo 中 此时才能返回
            baseAttrInfo.setAttrValueList(baseAttrValueList);
        }


        return baseAttrInfo;
    }

    @Override
    public IPage<SpuInfo> selectPage(com.baomidou.mybatisplus.extension.plugins.pagination.Page<SpuInfo> spuInfoPageParam, SpuInfo spuInfo) {
        //封装查询条件
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id", spuInfo.getCategory3Id());
        spuInfoQueryWrapper.orderByDesc("id");

        return spuInfoMapper.selectPage(spuInfoPageParam, spuInfoQueryWrapper);

    }

    @Override
    public IPage<SkuInfo> selectPage(Page<SkuInfo> skuInfoPage) {
       return skuInfoMapper.selectPage(skuInfoPage, new QueryWrapper<SkuInfo>().orderByDesc("id"));

    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        //调用mapper层
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        /*
            需要对应的mapper
            spuInfo 表中的数据
            spuImage 图片列表
            spuSaleAttr 销售属性
            spuSaleAttrValue 销售属性值
         */
        spuInfoMapper.insert(spuInfo);
        // 从获取到数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (null != spuImageList && spuImageList.size() > 0) {
            // 循环遍历添加
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            }
        }
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (null != spuSaleAttrList && spuSaleAttrList.size() > 0) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

                // 在销售属性中获取销售属性值集合
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();

                if (null != spuSaleAttrValueList && spuSaleAttrValueList.size() > 0) {
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());

                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        return spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id", spuId));

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        // 由于数据存在多张表中，所以需要自定义xml文件来实现
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
//        skuInfo 库存单元表
//        skuSaleAttrValue sku与销售属性值的中间表
//        skuAttrValue sku与平台属性中间表
//        skuImage 库存单元图片表
        skuInfoMapper.insert(skuInfo);
        // 获取销售属性的数据
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        // if (CollectionUtils.isEmpty())
        if (null!= skuSaleAttrValueList && skuSaleAttrValueList.size()>0){
            // 循环遍历
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                // ...... 可能有坑！填了！
                // 在已知的条件中获取spuId,skuId
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                // 从哪里获取 当数据从页面提交过来的时候，spuId 在skuInfo 中已经赋值了。
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                // 插入数据
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }

        // skuAttrValue 平台属性数据
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (null!= skuAttrValueList && skuAttrValueList.size()>0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                // ...... 可能有坑！
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }

        // skuImage 图片列表
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (null!=skuImageList && skuImageList.size()>0){
            for (SkuImage skuImage : skuImageList) {
                // ...... 可能有坑！
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }
    }

    @Override
    public void onSale(Long skuId) {
        //  is_sale = 1 表示可以上架，
        // update sku_info set is_sale = 1 where id=skuId
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(1);
        skuInfo.setId(skuId);
        skuInfoMapper.updateById(skuInfo);

    }

    @Override
    public void cancelSale(Long skuId) {
        // 0 那么则这商品不能买！ update sku_info set is_sale = 0 where id=skuId
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(0);
        skuInfo.setId(skuId);
        skuInfoMapper.updateById(skuInfo);
    }

}
