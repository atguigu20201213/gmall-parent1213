package com.atguigu.gmall1213.product.mapper;


import com.atguigu.gmall1213.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    //细节    如果接口中传递多个参数，则需要指明参数与sql 条件中的那个参数

    //编写xml    写xml 文件
    List<BaseAttrInfo> selectBaseAttrInfoList(@Param("category1Id") Long category1Id,
                                              @Param("category2Id") Long category2Id,
                                              @Param("category3Id") Long category3Id);
}
