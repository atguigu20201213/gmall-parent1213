package com.atguigu.gmall1213.product.Controller;

import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.model.product.BaseAttrInfo;
import com.atguigu.gmall1213.model.product.BaseCategory1;
import com.atguigu.gmall1213.model.product.BaseCategory2;
import com.atguigu.gmall1213.model.product.BaseCategory3;
import com.atguigu.gmall1213.product.service.ManageService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

        /**

        * 电商后台管理的控制

        */
@Api("后台接口测试")
@RestController // @ResponseBody + @Controller.
@RequestMapping("admin/product")
public class BaseManageController {
    @Autowired
    private ManageService manageService;
    // http://api.gmall.com/admin/product/getCategory1
    // 写谁 getCategory1 vue 项目页面需要获取的数据是Json 数据。
    // 封装过一个结果集的类 Result
    @GetMapping("getCategory1")
    public Result<List<BaseCategory1>> getCategory1(){
        // 最基本的返回方式！
        List<BaseCategory1> category1List = manageService.getCategory1();

        return Result.ok(category1List);
    }
    // http://api.gmall.com/admin/product/getCategory2/{category1Id}
    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id){
        // 根据一级分类Id 查询二级分类数据
        List<BaseCategory2> category2List = manageService.getCategory2(category1Id);
        return Result.ok(category2List);
    }

    // http://api.gmall.com/admin/product/getCategory3/{category2Id}
    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){
        // 根据二级分类Id 查询三级分类数据
        List<BaseCategory3> category3List = manageService.getCategory3(category2Id);
        return Result.ok(category3List);
    }

    // http://api.gmall.com/admin/product/attrInfoList/{category1Id}/{category2Id}/{category3Id}
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result getCategory3(@PathVariable Long category1Id,
                               @PathVariable Long category2Id,
                               @PathVariable Long category3Id){
        // 根据分类Id 查询平台属性数据
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(attrInfoList);
    }

    // http://api.gmall.com/admin/product/saveAttrInfo
    // 因为这个实体类中既有平台属性的数据，也有平台属性值的数据！
    // vue 项目在页面传递过来的是json 字符串， 能否直接映射成java 对象？
    // @RequestBody ： 将Json 数据转换为Java 对象。
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody  BaseAttrInfo baseAttrInfo){
        // 调用保存方法
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }
}