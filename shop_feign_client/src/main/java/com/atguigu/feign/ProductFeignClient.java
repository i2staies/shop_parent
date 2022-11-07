package com.atguigu.feign;

import com.atguigu.entity.BaseCategoryView;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(value = "shop-product")
public interface ProductFeignClient {
    //    a.根据skuId查询商品的基本信息
    @GetMapping("/sku/getSkuInfo/{skuid}")
    public SkuInfo getSkuInfo(@PathVariable Long skuid);

    //    b.根据三级分类id获取商品的分类信息
    @GetMapping("/sku/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id);
    //    c.获取实时价格
    @GetMapping("/sku/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId);

    //    d.销售属性组合与skuId的对应关系
    @GetMapping("/sku/getSalePropertyIdAndSkuIdMapping/{productId}")
    public Map<Object,Object> getSalePropertyIdAndSkuIdMapping(@PathVariable Long productId);

    //    e.获取该sku对应的销售属性（一份）和所有的销售属性（全份）
    @GetMapping("/sku/getSalePropertyIdAndSkuIdMapping/{productId}/{skuId}")
    public List<ProductSalePropertyKey> getSpuPropertyAndSelected(@PathVariable Long productId, @PathVariable Long skuId);

}
