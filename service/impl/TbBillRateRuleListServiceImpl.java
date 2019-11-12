package com.taolue.baoxiao.fund.service.impl;

import com.taolue.baoxiao.fund.entity.TbBillRateRuleList;
import com.taolue.baoxiao.fund.mapper.TbBillRateRuleListMapper;
import com.taolue.baoxiao.fund.service.ITbBillRateRuleListService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * <p>
 * 账单服务费率规则信息表 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2018-11-09
 */
@Service
public class TbBillRateRuleListServiceImpl extends ServiceImpl<TbBillRateRuleListMapper, TbBillRateRuleList> implements ITbBillRateRuleListService {

	@Override
	public List<TbBillRateRuleList> queryRateRuleByDay(Map<String, Object> maps) {
		return this.baseMapper.queryRateRuleByDay(maps);
	}

}
