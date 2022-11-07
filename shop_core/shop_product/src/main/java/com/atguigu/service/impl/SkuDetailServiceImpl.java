package com.atguigu.service.impl;

import com.atguigu.cache.ShopCache;
import com.atguigu.mapper.SkuSalePropertyValueMapper;
import com.atguigu.service.SkuDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkuDetailServiceImpl implements SkuDetailService {
    @Autowired
    private SkuSalePropertyValueMapper skuSalePropertyValueMapper;

    @ShopCache(value = "salePropertyIdAndSkuIdMapping",enableBloom = false)
    @Override
    public Map<Object,Object> getSalePropertyIdAndSkuIdMapping(Long productId) {
        HashMap<Object, Object> hashMap = new HashMap<>();
        List<Map> lists = skuSalePropertyValueMapper.getSalePropertyIdAndSkuIdMapping(productId);
        for (Map list : lists) {
            hashMap.put(list.get("sale_property_value_id"),list.get("sku_id"));
        }
        return hashMap;
    }
}
