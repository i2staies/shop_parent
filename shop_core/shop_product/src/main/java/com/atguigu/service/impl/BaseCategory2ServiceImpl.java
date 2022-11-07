package com.atguigu.service.impl;

import com.atguigu.entity.BaseCategory2;
import com.atguigu.mapper.BaseCategory2Mapper;
import com.atguigu.service.BaseCategory2Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 二级分类表 服务实现类
 * </p>
 *
 * @author zhangqiang
 * @since 2022-10-30
 */
@Service
public class BaseCategory2ServiceImpl extends ServiceImpl<BaseCategory2Mapper, BaseCategory2> implements BaseCategory2Service {

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper<BaseCategory2> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category1_id", category1Id);
        return baseMapper.selectList(queryWrapper);
    }
}
