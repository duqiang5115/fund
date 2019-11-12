/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  TradeFlowBusiServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 10, 2018 9:04:46 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.api.dto.TradeFlowSearchParams;
import com.taolue.baoxiao.fund.mapper.TradeFlowBusiMapper;
import com.taolue.baoxiao.fund.service.composite.ITradeFlowBusiService;

import lombok.extern.slf4j.Slf4j;

/**   
 * @ClassName:  TradeFlowBusiServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   Dec 10, 2018 9:04:46 PM   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class TradeFlowBusiServiceImpl extends ServiceImpl<TradeFlowBusiMapper, FundTradeFlowDto> 
				implements ITradeFlowBusiService {

	@Override
	public List<FundTradeFlowDto> findTradeFlows(TradeFlowSearchParams params) {
		if (log.isDebugEnabled()) {
			log.debug("TradeFlowBusiMapper.queryFlows查询参数TradeFlowSearchParams params=[{}]",
					JSON.toJSONString(params));
		}
		return this.baseMapper.queryFlows(params);
	}
	@Override
	public List<FundTradeFlowDto> findTradeFlowsRefund(TradeFlowSearchParams params) {
		if (log.isDebugEnabled()) {
			log.debug("TradeFlowBusiMapper.queryFlows查询参数TradeFlowSearchParams params=[{}]",
					JSON.toJSONString(params));
		}
		return this.baseMapper.queryFlowsAndRefund(params);
	}
	
	@Override
	public Query<FundTradeFlowDto> findTradeFlowsPaged(Query<FundTradeFlowDto> query, 
			TradeFlowSearchParams params) {
		if (log.isDebugEnabled()) {
			log.debug("TradeFlowBusiMapper.queryFlows查询参数Query<FundTradeFlowDto> query=[],"
					+ "TradeFlowSearchParams params=[]",
					JSON.toJSONString(query),JSON.toJSONString(params));
		}
		List<FundTradeFlowDto> records = this.baseMapper.queryFlows(query,params);
		query.setRecords(records);
		return query;
	}
	@Override
	public Query<FundTradeFlowDto> queryFlowsAndRefund(Query<FundTradeFlowDto> query, 
			TradeFlowSearchParams params) {
		if (log.isDebugEnabled()) {
			log.debug("TradeFlowBusiMapper.queryFlowsAndRefund查询参数Query<FundTradeFlowDto> query=[],"
					+ "TradeFlowSearchParams params=[]",
					JSON.toJSONString(query),JSON.toJSONString(params));
		}
		List<FundTradeFlowDto> records = this.baseMapper.queryFlowsAndRefund(query,params);
		query.setRecords(records);
		return query;
	}
}
