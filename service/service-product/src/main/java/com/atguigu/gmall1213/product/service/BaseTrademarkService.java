package com.atguigu.gmall1213.product.service;

import com.atguigu.gmall1213.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


public interface BaseTrademarkService {

    IPage<BaseTrademark> selectPage (Page<BaseTrademark> baseTrademarkPage);
}
