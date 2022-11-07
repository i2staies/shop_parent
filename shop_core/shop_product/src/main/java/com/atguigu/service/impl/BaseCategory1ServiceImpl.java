package com.atguigu.service.impl;

import com.atguigu.entity.BaseCategory1;
import com.atguigu.mapper.BaseCategory1Mapper;
import com.atguigu.service.BaseCategory1Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.ws.Action;
import java.util.List;

/**
 * <p>
 * 一级分类表 服务实现类
 * </p>
 *
 * @author zhangqiang
 * @since 2022-10-30
 */
@Service
public class BaseCategory1ServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategory1Service {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
}
