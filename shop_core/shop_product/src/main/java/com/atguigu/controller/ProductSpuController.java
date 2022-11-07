package com.atguigu.controller;


import com.atguigu.entity.*;
import com.atguigu.mapper.ProductImageMapper;
import com.atguigu.mapper.ProductSalePropertyKeyMapper;
import com.atguigu.result.RetVal;
import com.atguigu.service.BaseSalePropertyService;
import com.atguigu.service.ProductSalePropertyKeyService;
import com.atguigu.service.ProductSalePropertyValueService;
import com.atguigu.service.ProductSpuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author zhangqiang
 * @since 2022-11-01
 */
@RestController
@RequestMapping("/product")
public class ProductSpuController {
    @Autowired
    private ProductSpuService productSpuService;

    @Autowired
    private BaseSalePropertyService baseSalePropertyService;

    @Autowired
    private ProductImageMapper productImageMapper;

    @Autowired
    private ProductSalePropertyKeyMapper productSalePropertyKeyMapper;

    @Autowired
    private ProductSalePropertyValueService productSalePropertyValueService;

    //查询priduct分页信息
    @GetMapping("/queryProductSpuByPage/{pageNum}/{pageSize}/{categoryId}")
    public RetVal queryProductSpuByPage(@PathVariable Integer pageNum,
                                        @PathVariable Integer pageSize,
                                        @PathVariable Integer categoryId){
        Page<ProductSpu> page = new Page<>(pageNum,pageSize);
        IPage<ProductSpu> iPage = productSpuService.page(page, new QueryWrapper<ProductSpu>().eq("category3_id", categoryId));
        return RetVal.ok(iPage);
    }

    //查询所有的销售信息
    @GetMapping("/queryAllSaleProperty")
    public RetVal queryAllSaleProperty(){
        List<BaseSaleProperty> list = baseSalePropertyService.list(null);
        return RetVal.ok(list);
    }

    @PostMapping("/saveProductSpu")
    public RetVal saveProductSpu(@RequestBody ProductSpu productSpu){
         productSpuService.saveProductSpu(productSpu);
         return RetVal.ok();
    }

    @GetMapping("/queryProductImageByProductId/{productId}")
    public RetVal queryProductImageByProductId(@PathVariable Integer productId){
        List<ProductImage> images = productImageMapper.selectList(new QueryWrapper<ProductImage>().eq("product_id", productId));
        return RetVal.ok(images);
    }

    @GetMapping("/querySalePropertyByProductId/{productId}")
    public RetVal querySalePropertyByProductId(@PathVariable Integer productId){
        //要构造一对多的关系至少需要两张表，并且返回resultMap
//        QueryWrapper<ProductSalePropertyValue> queryWrapper = new QueryWrapper<ProductSalePropertyValue>().eq("product_id", productId);
//        List<ProductSalePropertyValue> list = productSalePropertyValueService.list(queryWrapper);
        List<ProductSalePropertyKey> productSalePropertyKeys = productSalePropertyKeyMapper.querySalePropertyByProductId(productId);
        return RetVal.ok(productSalePropertyKeys);
    }
}

