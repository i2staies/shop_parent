package com.atguigu.controller;


import com.atguigu.entity.SkuInfo;
import com.atguigu.result.RetVal;
import com.atguigu.service.ProductImageService;
import com.atguigu.service.SkuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author zhangqiang
 * @since 2022-11-01
 */
@RestController
@RequestMapping("/product/")
public class ProductSkuController {

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private ProductImageService productImageService;

    @PostMapping("saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo){
        skuInfoService.saveSkuInfo(skuInfo);
    }

    @GetMapping("/querySkuInfoByPage/{pageNum}/{pageSize}")
    public RetVal querySkuInfoByPage(@PathVariable Long pageNum,@PathVariable Long pageSize){
        IPage<SkuInfo> page = new Page<>(pageNum, pageSize);
        skuInfoService.page(page,null);
        return RetVal.ok(page);
    }

    @GetMapping("/onSale/{skuId}")
    public RetVal onSale(@PathVariable Long skuId){
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoService.updateById(skuInfo);
        return RetVal.ok();
    }

    @GetMapping("/offSale/{skuId}")
    public RetVal offSale(@PathVariable Long skuId){
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoService.updateById(skuInfo);
        return RetVal.ok();
    }
}

