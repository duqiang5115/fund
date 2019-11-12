package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.fund.entity.FundReportTradeFlow;

import java.util.Date;

import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 会员交易记录宽表 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-09-24
 */
public interface IFundReportTradeFlowService extends IService<FundReportTradeFlow> {

	/****
	 * @Title IFundReportTradeFlowService.creatingFundReportTradeFlow
	 * @Description: 会员交易记录清洗
	 *
	 * @param startTime
	 * @param endTime
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-09-24 18:01:21  修改内容 :
	 */
	public boolean creatingFundReportTradeFlow(Date startTime, Date endTime,String adminLoginName);

	/****
	 * @Title FundReportTradeFlowServiceImpl.addCouponCostDetail
	 * @Description: 更新消费订单详情信息
	 *
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-10-16 18:27:54  修改内容 :
	 */
	public boolean addCouponCostDetail();

}
