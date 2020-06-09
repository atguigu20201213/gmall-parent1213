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
@Api("接口测试")
@RestController  // @responseBody  + @controller
@RequestMapping("admin/product")
public class BaseManageController {
    @Autowired
    private ManageService manageService;

    //书写一级分类
    @GetMapping("getCategory1")
    public Result<List<BaseCategory1>> getCategory1() {
        //最基本的返回方式
        List<BaseCategory1> category1List = manageService.getCategory1();
        return Result.ok(category1List);
    }


    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id) {
        List<BaseCategory2> category2List = manageService.getCategory2(category1Id);
        return Result.ok(category2List);
    }

    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id) {
        List<BaseCategory3> category3List = manageService.getCategory3(category2Id);
        return Result.ok(category3List);
    }

    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result getCategory3(@PathVariable Long category1Id, @PathVariable Long category2Id, @PathVariable Long category3Id) {
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(attrInfoList);

    }
    //@RequestBody   将json 转换为java对象
    @PostMapping("saveAttrInfo")
    public Result  saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

}
