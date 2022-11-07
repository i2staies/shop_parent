package com.atguigu.controller;


import com.atguigu.entity.PlatformPropertyKey;
import com.atguigu.result.RetVal;
import com.atguigu.service.PlatformPropertyKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 属性表 前端控制器
 * </p>
 *
 * @author zhangqiang
 * @since 2022-10-30
 */
@RestController
@RequestMapping("/product")
//@CrossOrigin
public class PlatformPropertyController {
    @Autowired
    private PlatformPropertyKeyService platformPropertyKeyService;

    @GetMapping("/getPlatformPropertyByCategoryId/{category1Id}/{category2Id}/{category3Id}")
    public RetVal getPlatformPropertyByCategoryId(@PathVariable Long category1Id,
                                                  @PathVariable Long category2Id,
                                                  @PathVariable Long category3Id){
        List<PlatformPropertyKey> list = platformPropertyKeyService.getPlatformPropertyByCategoryId(category1Id,category2Id,category3Id);
        return RetVal.ok(list);
    }

    @GetMapping("/getPropertyValueByPropertyKeyId/{propertyKeyId}")
    public RetVal getPropertyValueByPropertyKeyId(@PathVariable Long propertyKeyId){
        return platformPropertyKeyService.getPropertyValueByPropertyKeyId(propertyKeyId);
    }

    @PostMapping("/savePlatformProperty")
    public void savePlatformProperty(@RequestBody PlatformPropertyKey platformPropertyKey){
        platformPropertyKeyService.savePlatformProperty(platformPropertyKey);
    }

}

