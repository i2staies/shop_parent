package com.atguigu.service.impl;

import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.mapper.ProductSalePropertyKeyMapper;
import com.atguigu.service.ProductSalePropertyKeyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * spu销售属性 服务实现类
 * </p>
 *
 * @author zhangqiang
 * @since 2022-11-01
 */
@Service
public class ProductSalePropertyKeyServiceImpl extends ServiceImpl<ProductSalePropertyKeyMapper, ProductSalePropertyKey> implements ProductSalePropertyKeyService {

    @Autowired
    private ProductSalePropertyKeyMapper productSalePropertyKeyMapper;
    @Override
    public List<ProductSalePropertyKey> getSpuPropertyAndSelected(Long productId,Long skuId) {
        return productSalePropertyKeyMapper.getSpuPropertyAndSelected(productId,skuId);
    }
}
