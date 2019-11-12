package com.taolue.baoxiao.fund.service.composite.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.OrderBusiComposeDto;
import com.taolue.baoxiao.fund.api.dto.OrderBusiQueryDto;
import com.taolue.baoxiao.fund.mapper.OrderBusiComposeMapper;
import com.taolue.baoxiao.fund.service.composite.IOrderBusiComposeService;

/**
 * 
 * @ClassName:  OrderBusiComposeServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:15:51   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
public class OrderBusiComposeServiceImpl extends ServiceImpl<OrderBusiComposeMapper, OrderBusiComposeDto> implements IOrderBusiComposeService {

	@Override
	public Page<OrderBusiComposeDto> selectOrderBusiComposeDtoPage(Query<OrderBusiComposeDto> page) { 
        page.setRecords(baseMapper.selectOrderBusiComposes(page,page.getCondition()));
        return page;
    }

	@Override
	public Page<OrderBusiComposeDto> selectOrderBusiComposeDtoByDto(Query query, OrderBusiQueryDto dto) {
		query.setRecords(baseMapper.selectOrderBusiComposeDtoByDto(query, dto));
		 return query;
	}
}
