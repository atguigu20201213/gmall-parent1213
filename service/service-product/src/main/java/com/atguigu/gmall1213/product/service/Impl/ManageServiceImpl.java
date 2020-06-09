package com.atguigu.gmall1213.product.service.Impl;

import com.atguigu.gmall1213.model.product.*;
import com.atguigu.gmall1213.product.mapper.*;

import com.atguigu.gmall1213.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.annotation.Transient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class ManageServiceImpl implements ManageService {
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper<BaseCategory2> baseCategory2QueryWrapper = new QueryWrapper<>();
        baseCategory2QueryWrapper.eq("category1_Id", category1Id);
        return baseCategory2Mapper.selectList(baseCategory2QueryWrapper);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> baseCategory3QueryWrapper = new QueryWrapper<>();
        baseCategory3QueryWrapper.eq("category2_Id", category2Id);
        return baseCategory3Mapper.selectList(baseCategory3QueryWrapper);

    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {

        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id, category2Id, category3Id);
    }

    @Override
    @Transient
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (null != baseAttrInfo) {
            /**
             * baseAttrInfo  平台属性表
             *baseAttrValue  平台属性值
             */
            baseAttrInfoMapper.insert(baseAttrInfo);

            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            if (null != attrValueList && attrValueList.size() > 0) {

                for (BaseAttrValue baseAttrValue : attrValueList) {
                    //页面在提交数据的时候 没有给attrId赋值 所有在此时手动赋值
                    //attrID = baseAttrInfo.getId
                    baseAttrValue.setAttrId(baseAttrInfo.getId());
                    //循环将数据添加到数据表中
                    baseAttrValueMapper.insert(baseAttrValue);

                }
            }
        }

    }
}
