package com.atguigu.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.entity.BaseCategoryView;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;
import com.atguigu.feign.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class WebDetailController {
    @Autowired
    private ProductFeignClient productFeignClient;

    @RequestMapping("/{skuId}.html")
    public String getSkuDetail(@PathVariable Long skuId, Model model){
        //    a.根据skuId查询商品的基本信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        model.addAttribute("skuInfo", skuInfo);
        //    b.根据三级分类id获取商品的分类信息
        Long category3Id = skuInfo.getCategory3Id();
        BaseCategoryView categoryView = productFeignClient.getCategoryView(category3Id);
        model.addAttribute("categoryView", categoryView);
        //    c.获取实时价格
        BigDecimal skuPrice1 = productFeignClient.getSkuPrice(skuInfo.getId());
        System.out.println(skuPrice1);
        model.addAttribute("price",skuPrice1);
        //    d.销售属性组合与skuId的对应关系
        Map<Object, Object> salePropertyIdAndSkuIdMapping = productFeignClient.getSalePropertyIdAndSkuIdMapping(skuInfo.getProductId());
        model.addAttribute("salePropertyValueIdJson", JSON.toJSONString(salePropertyIdAndSkuIdMapping));
        //    e.获取该sku对应的销售属性（一份）和所有的销售属性（全份）
        List<ProductSalePropertyKey> spuSalePropertyList = productFeignClient.getSpuPropertyAndSelected(skuInfo.getProductId(), skuInfo.getId());
        model.addAttribute("spuSalePropertyList",spuSalePropertyList);
        return "detail/index";
    }
}
