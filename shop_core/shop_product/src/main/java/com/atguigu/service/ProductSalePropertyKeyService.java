package com.atguigu.service;

import com.atguigu.entity.ProductSalePropertyKey;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * spu销售属性 服务类
 * </p>
 *
 * @author zhangqiang
 * @since 2022-11-01
 */
public interface ProductSalePropertyKeyService extends IService<ProductSalePropertyKey> {

    List<ProductSalePropertyKey> getSpuPropertyAndSelected(Long productId,Long skuId);
}
