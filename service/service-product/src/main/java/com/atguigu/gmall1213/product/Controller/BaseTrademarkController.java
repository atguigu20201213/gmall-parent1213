package com.atguigu.gmall1213.product.Controller;

import com.atguigu.gmall1213.common.result.Result;
import com.atguigu.gmall1213.model.product.BaseTrademark;
import com.atguigu.gmall1213.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/product/baseTrademark")
public class BaseTrademarkController {
    @Autowired
    private BaseTrademarkService baseTrademarkService;

    @GetMapping("{page}/{limit}")
    public Result getPageList(@PathVariable Long page, @PathVariable Long limit) {
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page,limit);

        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkService.selectPage(baseTrademarkPage);
        return Result.ok(baseTrademarkIPage);
    }
}
