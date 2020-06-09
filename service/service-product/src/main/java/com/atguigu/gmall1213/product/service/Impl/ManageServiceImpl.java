package com.atguigu.gmall1213.product.service.Impl;

import com.atguigu.gmall1213.model.product.*;
import com.atguigu.gmall1213.product.mapper.*;

import com.atguigu.gmall1213.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        baseCategory2QueryWrapper.eq("category1_id",category1Id);
        return   baseCategory2Mapper.selectList(baseCategory2QueryWrapper);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        // select * from base_category3 where category2_id=category2Id;
        QueryWrapper<BaseCategory3> baseCategory3QueryWrapper = new QueryWrapper<>();
        baseCategory3QueryWrapper.eq("category2_id",category2Id);
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
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id,category2Id,category3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (null!=baseAttrInfo){
            /*
            一个是平台属性表；baseAttrInfo
            一个是平台属性值表： baseAttrValue
             */
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
        }
    }
}
