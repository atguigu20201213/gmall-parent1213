package com.atguigu.gmall1213.all.controller;

import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.list.client.ListFeignClient;
import com.atguigu.gmall1213.model.list.SearchParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ListController {
    @Autowired
    private ListFeignClient listFeignClient;

    @GetMapping("list.html")
    public String search(SearchParam searchParam, Model model) {
        Result<Map> result = listFeignClient.list(searchParam);

        //制作检索条件拼接
        String urlParam = makeUrlParam(searchParam);

        //获取处理品牌数据
        String tradeMark = makeTradeMark(searchParam.getTrademark());
        //获取平台属性值数据
        List<Map<String, String>> list = makeProps(searchParam.getProps());
        //获取排序规则
        Map<String, Object> order = order(searchParam.getOrder());

        model.addAttribute("urlParam", urlParam);
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("trademarkParam", tradeMark);
        model.addAttribute("propsParamList",list);
        model.addAttribute("orderMap",order);

        //保存数据给页面使用
        model.addAllAttributes(result.getData());
        //检测列表
        return "list/index";
    }

    //处理排序 根据页面提供的数据
    private Map<String,Object> order(String order) {
        HashMap<String, Object> hashMap = new HashMap<>();
        if (!StringUtils.isEmpty(order)) {
            //order  将数据进行分割
            String[] split = order.split(":");
            if (null != split && split.length == 2) {
                //数据处理   type 安照什么字段排序
                hashMap.put("type", split[0]);
                //排序顺序  desc   asc
                hashMap.put("sort", split[1]);
            } else {
                //给一个默认排序规则
                hashMap.put("type", "1");
                //排序顺序  desc   asc
                hashMap.put("sort", "asc");
            }
        }else {
            //给一个默认排序规则
            hashMap.put("type", "1");
            //排序顺序  desc   asc
            hashMap.put("sort", "asc");
        }
        return hashMap;
    }

    // 拼接检索条件
    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder urlParam = new StringBuilder();

        if (null != searchParam.getKeyword()) {
            urlParam.append("keyword=").append(searchParam.getKeyword());
        }
        if (null != searchParam.getCategory3Id()) {
            urlParam.append("category3Id=").append(searchParam.getCategory3Id());
        }
        if (null != searchParam.getCategory2Id()) {
            urlParam.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if (null != searchParam.getCategory1Id()) {
            urlParam.append("category1Id=").append(searchParam.getCategory1Id());
        }
        //通过两个入口进来之后 ，通过品牌检索
        if (null != searchParam.getTrademark()) {
            if (urlParam.length() > 0) {
                urlParam.append("&trademark=").append(searchParam.getTrademark());
            }

        }
        //平台检索  品牌检索  属性值检索
        if (null != searchParam.getProps()) {

            for (String prop : searchParam.getProps()) {
                if (urlParam.length() > 0) {
                    urlParam.append("&props=").append(prop);
                }
            }

        }

        return "list.html?" + urlParam.toString();
    }

    //处理品牌   品牌 ：品牌名字
    //注意传入的参数应该与封装的品牌属性一致
    private String makeTradeMark(String trademark) {
        //用户点击的品牌
        if (!StringUtils.isEmpty(trademark)) {
            //trademark  2  :华为
            String[] split = trademark.split(":");
            //判断数据格式
            if (null != split && split.length == 2) {
                return "品牌：" + split[1];
            }
        }
        return null;
    }

    //处理平台属性   平台属性名称：平台属性值
    //分析一 下   数据存储的格式是list <map>
    private List<Map<String, String>> makeProps(String[] props) {
        List<Map<String, String>> list = new ArrayList<>();
        //数据格式  props : 4g :运行内存
        if (null != props && props.length > 0) {
            for (String prop : props) {
                //拆分数据
                String[] split = prop.split(":");
                //保证数据格式
                if (null != split && split.length == 3) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("attrId", split[0]);
                    map.put("attrValue", split[1]);
                    map.put("attrName", split[2]);
            // 添加到集合中
                    list.add(map);
                }
            }
        }

        return list;
    }
}
