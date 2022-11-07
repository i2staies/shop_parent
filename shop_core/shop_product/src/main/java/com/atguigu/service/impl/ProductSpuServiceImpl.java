package com.atguigu.service.impl;

import com.atguigu.entity.ProductImage;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.ProductSalePropertyValue;
import com.atguigu.entity.ProductSpu;
import com.atguigu.mapper.ProductSpuMapper;
import com.atguigu.service.ProductImageService;
import com.atguigu.service.ProductSalePropertyKeyService;
import com.atguigu.service.ProductSalePropertyValueService;
import com.atguigu.service.ProductSpuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author zhangqiang
 * @since 2022-11-01
 */
@Service
public class ProductSpuServiceImpl extends ServiceImpl<ProductSpuMapper, ProductSpu> implements ProductSpuService {

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ProductSalePropertyKeyService productSalePropertyKeyService;

    @Autowired
    private ProductSalePropertyValueService productSalePropertyValueService;

    @Override
    @Transactional
    public void saveProductSpu(ProductSpu productSpu) {
        baseMapper.insert(productSpu);
        List<ProductImage> productImageList = productSpu.getProductImageList();
        for (ProductImage productImage : productImageList) {
            productImage.setProductId(productSpu.getId());
        }
        productImageService.saveBatch(productImageList);
        List<ProductSalePropertyKey> salePropertyKeyList = productSpu.getSalePropertyKeyList();
        for (ProductSalePropertyKey productSalePropertyKey : salePropertyKeyList) {
            productSalePropertyKey.setProductId(productSpu.getId());
            List<ProductSalePropertyValue> salePropertyValueList = productSalePropertyKey.getSalePropertyValueList();
            for (ProductSalePropertyValue productSalePropertyValue : salePropertyValueList) {
                productSalePropertyValue.setProductId(productSpu.getId());
                productSalePropertyValue.setSalePropertyKeyName(productSalePropertyKey.getSalePropertyKeyName());
            }
            productSalePropertyValueService.saveBatch(productSalePropertyKey.getSalePropertyValueList());
        }
        productSalePropertyKeyService.saveBatch(salePropertyKeyList);
    }
}
