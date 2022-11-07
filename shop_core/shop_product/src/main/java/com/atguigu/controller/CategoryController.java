package com.atguigu.controller;


import com.atguigu.entity.BaseCategory1;
import com.atguigu.entity.BaseCategory2;
import com.atguigu.entity.BaseCategory3;
import com.atguigu.result.RetVal;
import com.atguigu.service.BaseCategory1Service;
import com.atguigu.service.BaseCategory2Service;
import com.atguigu.service.BaseCategory3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 一级分类表 前端控制器
 * </p>
 *
 * @author zhangqiang
 * @since 2022-10-30
 */
@RestController
@RequestMapping("product")
//@CrossOrigin
public class CategoryController {
    @Autowired
    private BaseCategory1Service baseCategory1Service;
    @Autowired
    private BaseCategory2Service baseCategory2Service;
    @Autowired
    private BaseCategory3Service baseCategory3Service;

    @GetMapping("getCategory1")
    public RetVal getCategory1(){
        //查找商品一级分类
        List<BaseCategory1> list = baseCategory1Service.list(null);
        return RetVal.ok(list);
    }

    @GetMapping("getCategory2/{category1Id}")
    public RetVal getCategory2(@PathVariable Long category1Id){
        //查找商品二级分类
        List<BaseCategory2> list = baseCategory2Service.getCategory2(category1Id);
        return RetVal.ok(list);
    }

    @GetMapping("/getCategory3/{category2Id}")
    public RetVal getCategory3(@PathVariable Long category2Id){
        //查找商品二级分类
        List<BaseCategory3> list = baseCategory3Service.getCategory3(category2Id);
        return RetVal.ok(list);
    }
}