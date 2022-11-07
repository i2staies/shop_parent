package com.atguigu.service.impl;

import com.atguigu.entity.PlatformPropertyKey;
import com.atguigu.entity.PlatformPropertyValue;
import com.atguigu.mapper.PlatformPropertyKeyMapper;
import com.atguigu.result.RetVal;
import com.atguigu.service.PlatformPropertyKeyService;
import com.atguigu.service.PlatformPropertyValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 属性表 服务实现类
 * </p>
 *
 * @author zhangqiang
 * @since 2022-10-30
 */
@Service
public class PlatformPropertyKeyServiceImpl extends ServiceImpl<PlatformPropertyKeyMapper, PlatformPropertyKey> implements PlatformPropertyKeyService {

    @Autowired
    private PlatformPropertyValueService platformPropertyValueService;

//    @Override
//    public List<PlatformPropertyKey> getPlatformPropertyByCategoryId(Long category1Id, Long category2Id, Long category3Id) {
//        //1. 根据商品分类id查询商品平台属性名称
//        List<PlatformPropertyKey> platformPropertyKeys = baseMapper.getPlatformPropertyByCategoryId(category1Id, category2Id, category3Id);
//        //2. 根据商品分类id查询所有平台属性值
//        if(!CollectionUtils.isEmpty(platformPropertyKeys)){
//            for (PlatformPropertyKey platformPropertyKey : platformPropertyKeys) {
//                Long id = platformPropertyKey.getId();
//                QueryWrapper<PlatformPropertyValue> queryWrapper = new QueryWrapper<>();
//                queryWrapper.eq("property_key_id", id);
//                List<PlatformPropertyValue> platformPropertyValues = platformPropertyValueService.list(queryWrapper);
//                platformPropertyKey.setPropertyValueList(platformPropertyValues);
//            }
//        }
//        return platformPropertyKeys;
//    }

    @Override
    public List<PlatformPropertyKey> getPlatformPropertyByCategoryId(Long category1Id, Long category2Id, Long category3Id) {
        //1. 根据商品分类id查询商品平台属性名称
        List<PlatformPropertyKey> platformPropertyKeys = baseMapper.getPlatformPropertyKeyByCategoryId(category1Id, category2Id, category3Id);
        return platformPropertyKeys;
    }

    @Override
    public RetVal getPropertyValueByPropertyKeyId(Long propertyKeyId) {
        QueryWrapper<PlatformPropertyValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("property_key_id", propertyKeyId);
        List<PlatformPropertyValue> propertyKeys = platformPropertyValueService.list(queryWrapper);
        return RetVal.ok(propertyKeys);
    }

    @Override
    public void savePlatformProperty(PlatformPropertyKey platformPropertyKey) {
        if(platformPropertyKey.getId()!=null){
            baseMapper.updateById(platformPropertyKey);
            QueryWrapper<PlatformPropertyValue> queryWrapper = new QueryWrapper<PlatformPropertyValue>().eq("property_key_id", platformPropertyKey.getId());
            platformPropertyValueService.remove(queryWrapper);
            List<PlatformPropertyValue> propertyValueList = platformPropertyKey.getPropertyValueList();
            if(propertyValueList!=null){
                for (PlatformPropertyValue platformPropertyValue : propertyValueList) {
                    platformPropertyValue.setPropertyKeyId(platformPropertyKey.getId());
                }
                platformPropertyValueService.saveBatch(propertyValueList);
            }
        }else {
            baseMapper.insert(platformPropertyKey);
        }
    }
}
