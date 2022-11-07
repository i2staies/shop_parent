package com.atguigu.service;

import com.atguigu.entity.BaseCategory2;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 二级分类表 服务类
 * </p>
 *
 * @author zhangqiang
 * @since 2022-10-30
 */
public interface BaseCategory2Service extends IService<BaseCategory2> {
    List<BaseCategory2> getCategory2(Long category1Id);
}
