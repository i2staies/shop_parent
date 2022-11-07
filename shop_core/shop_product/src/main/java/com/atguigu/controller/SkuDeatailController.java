package com.atguigu.controller;

import com.atguigu.entity.BaseCategoryView;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;
import com.atguigu.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sku")
public class SkuDeatailController {

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private BaseCategoryViewService baseCategoryViewService;

    @Autowired
    private SkuDetailService skuDetailService;

    @Autowired
    private ProductSalePropertyKeyService productSalePropertyKeyService;

    @Value("${server.port}")
    private Integer port;
//    a.根据skuId查询商品的基本信息
    @GetMapping("/getSkuInfo/{skuid}")
    public SkuInfo getSkuInfo(@PathVariable Long skuid){
        System.out.println("端口号"+port+"被占用！");
        SkuInfo skuInfo = skuInfoService.getSkuInfo(skuid);
        return skuInfo;
    }

//    b.根据三级分类id获取商品的分类信息
    @GetMapping("/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){
        return baseCategoryViewService.getById(category3Id);
    }
//    c.获取实时价格
    @GetMapping("/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        SkuInfo skuInfo = skuInfoService.getById(skuId);
        return skuInfo.getPrice();
    }

//    d.销售属性组合与skuId的对应关系
    @GetMapping("/getSalePropertyIdAndSkuIdMapping/{productId}")
    public Map<Object,Object> getSalePropertyIdAndSkuIdMapping(@PathVariable Long productId){
        return skuDetailService.getSalePropertyIdAndSkuIdMapping(productId);
    }

//    e.获取该sku对应的销售属性（一份）和所有的销售属性（全份）
    @GetMapping("/getSalePropertyIdAndSkuIdMapping/{productId}/{skuId}")
    public List<ProductSalePropertyKey> getSpuPropertyAndSelected(@PathVariable Long productId,@PathVariable Long skuId){
        return productSalePropertyKeyService.getSpuPropertyAndSelected(productId,skuId);
    }

}
