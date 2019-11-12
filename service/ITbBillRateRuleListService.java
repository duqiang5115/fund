package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.fund.entity.TbBillRateRuleList;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 账单服务费率规则信息表 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2018-11-09
 */
public interface ITbBillRateRuleListService extends IService<TbBillRateRuleList> {
	List<TbBillRateRuleList> queryRateRuleByDay(Map<String,Object> maps);
}
