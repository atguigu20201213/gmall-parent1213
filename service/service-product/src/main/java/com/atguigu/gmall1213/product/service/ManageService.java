package com.atguigu.gmall1213.product.service;

import com.atguigu.gmall1213.model.product.BaseAttrInfo;
import com.atguigu.gmall1213.model.product.BaseCategory1;
import com.atguigu.gmall1213.model.product.BaseCategory2;
import com.atguigu.gmall1213.model.product.BaseCategory3;

import java.util.List;

public interface ManageService {


    List<BaseCategory1> getCategory1();
    List<BaseCategory2> getCategory2(Long category1Id);
    List<BaseCategory3> getCategory3(Long category2Id);
    List<BaseAttrInfo> getAttrInfoList(Long category1Id,Long category2Id,Long category3Id);

    //大保存  平台属性 平台属性值
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);
}
