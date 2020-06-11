package com.atguigu.gmall1213.product.service.Impl;

import com.atguigu.gmall1213.model.product.BaseTrademark;
import com.atguigu.gmall1213.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall1213.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import org.springframework.stereotype.Service;

@Service
public class BaseTrademarkServiceImpl implements BaseTrademarkService {
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;


    @Override
    public IPage<BaseTrademark> selectPage(Page<BaseTrademark> baseTrademarkPage) {
        QueryWrapper<BaseTrademark> baseTrademarkQueryWrapper = new QueryWrapper<>();
        baseTrademarkQueryWrapper.orderByAsc("id");
        return baseTrademarkMapper.selectPage(baseTrademarkPage,baseTrademarkQueryWrapper );
    }
}
