/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  ReimburseBalanceServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 28, 2018 3:33:13 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.dto.AssignCouponDto;
import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.common.util.Exceptions;
import com.taolue.baoxiao.common.util.Group;
import com.taolue.baoxiao.fund.api.dto.BalanceSearchParams;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.common.model.Cell;
import com.taolue.baoxiao.fund.common.model.Node;
import com.taolue.baoxiao.fund.common.model.ReimburseBalanceTradeProcess;
import com.taolue.baoxiao.fund.common.model.TradeProcessContext;
import com.taolue.baoxiao.fund.factory.IBusinessFlowHandle;
import com.taolue.baoxiao.fund.factory.IBusinessFlowHandleFactory;
import com.taolue.baoxiao.fund.service.composite.IBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.IReimburseBalanceService;
import com.taolue.baoxiao.fund.service.remote.IRefactorCouponServiceFactory;
import com.taolue.baoxiao.fund.service.remote.IRefactorInvoicServiceFactory;
import com.taolue.coupon.api.vo.CouponRelationVo;
import com.taolue.invoice.api.dto.IndustryDto;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**   
 * <p>ClassName:  ReimburseBalanceServiceImpl </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: shilei</br>
 * <p>date:   Dec 28, 2018 3:33:13 PM </br>  
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class ReimburseBalanceServiceImpl implements IReimburseBalanceService {

	@Autowired
	private IBalanceBusiService balanceBusiService;
	
	@Autowired
	private IRefactorInvoicServiceFactory refactorInvoicServiceFactory;
	
	@Autowired
	private IRefactorCouponServiceFactory refactorCouponServiceFactory;
	
	@Autowired
	private IBusinessFlowHandleFactory businessFlowHandleFactory;

	@Autowired
    private RabbitTemplate rabbitTemplate;
	
	public BigDecimal findReimburseAvailableAmount(String memberId, String companyId, String memberCate) {
		BigDecimal total = CommonConstant.NO_AMOUNT;
		BalanceSearchParams params = new BalanceSearchParams();
		params.addComposAttrs(CommonConstant.BALANCE_COMPOS_ATTR_REIMBURSE_COUPONSHARE);
        params.addMemberCate(memberCate);		
		params.setAcctCate(AcctCateEnums.ACCT_CATE_REIMBURSE.getCateCode());
		params.addMemberId(memberId);
		params.setCanTicket(null);
		
		List<FundBalanceDto> balances = this.balanceBusiService.selectByConditions(params);
		if (CollUtil.isNotEmpty(balances)) {
			for (FundBalanceDto fundBalanceDto : balances) {
				total = total.add(fundBalanceDto.getBalanceAmount());
			}
		} else {
			params = new BalanceSearchParams();
			params.addMemberCate(memberCate);
			params.setAcctCate(AcctCateEnums.ACCT_CATE_COUPON.getCateCode());
			params.setCompanyId(companyId);
			params.addMemberId(memberId);
			params.setCanTicket(null);
			balances = this.balanceBusiService.selectByConditions(params);
			if (CollUtil.isNotEmpty(balances)) {
				for (FundBalanceDto fundBalanceDto : balances) {
					total = total.add(fundBalanceDto.getBalanceAmount());
				}
			}
		}
		return total;
	}
	
	public AssignDto determineEnoughAmount(AssignDto assignDto) {
		boolean nopass = true;
		log.info("校验报销申请-参数{}",JSON.toJSONString(assignDto));
		String memberId = assignDto.getMemberId();
		String companyId = assignDto.getCompanyId();
		String memberCate = assignDto.getMemberCate();
		BalanceSearchParams params = new BalanceSearchParams();
		params.addMemberCate(memberCate);
		params.setAcctCate(AcctCateEnums.ACCT_CATE_COUPON.getCateCode());
		params.addMemberId(memberId);
		params.setCompanyId(companyId);
		if ("2".equals(assignDto.getCanTicket())) {
			params.setCanTicket("1");
			nopass = false;
			assignDto.setCanTicket("1");
		} else {
			params.setCanTicket(assignDto.getCanTicket());
		}
		params.addGroup(new Group("b", "balance_item_code"));
		
		//获取当前账户在当前公司下所有可用消费券id和其对应的可用额度
		List<Node> toUsedNodes = Lists.newArrayList();
		List<Node> targetNodes = Lists.newArrayList();
		
		Map<String, Set<String>>  canUsedCouponIds = Maps.newHashMap();
		List<FundBalanceDto> result = this.balanceBusiService.selectByConditions(params);
		
		List<String> couponIds = Lists.newArrayList();
		if (CollUtil.isNotEmpty(result)) {
			for (FundBalanceDto balance : result) {
				String couponId = balance.getBalanceItemCode();
				couponIds.add(couponId);
				toUsedNodes.add(new Node(couponId,balance.getBalanceAmount()));
			}
		} else {
			FundServiceExceptionGenerator.FundServiceException("9998", "无可用消费券！！");
		}
		
		//获取当前报销明细；couponId是报销科目code；amount是报销金额
		List<AssignCouponDto> coupons = assignDto.getCoupons();
		if (CollUtil.isNotEmpty(coupons)) {
			//循环每个报销明细
			for (AssignCouponDto assignCouponDto : coupons) {
				
				String reimburseItemCode = assignCouponDto.getCouponId();
				//通过报销科目编码获取归属行业类别
				Set<String> industryIds = getIndustryIds(reimburseItemCode);
				List<String> lists = Lists.newArrayList();
				lists.addAll(industryIds);
				
				if (CollUtil.isEmpty(lists)) {
					FundServiceExceptionGenerator.FundServiceException("9998", "无法查询到行业类型");
				}
				//获取在当前账户下，且在上述行业类别中的券
				List<CouponRelationVo> couponVos = this.refactorCouponServiceFactory
						.findCouponsByIndustryAndCouponIds(lists, couponIds);
				
				if (CollUtil.isEmpty(lists)) {
					FundServiceExceptionGenerator.FundServiceException("9998", "无可用消费券！！");
				}
				
				//按券id去重,获取到该报销明细中可用的消费券id
				Set<String> availableCouponIds = removeDuplication(couponVos, "couponId");
				canUsedCouponIds.put(reimburseItemCode, availableCouponIds);
				targetNodes.add(new Node(reimburseItemCode, assignCouponDto.getAmount()));
			}
		}
		
		TradeProcessContext context = new TradeProcessContext(toUsedNodes.toArray(new Node[0]),
				targetNodes.toArray(new Node[0]));
		
		ReimburseBalanceTradeProcess process = new ReimburseBalanceTradeProcess(canUsedCouponIds);
		process.process(context);
		Map<String, Map<String, Cell>> records = context.getProcessRecords();
		log.info("处理后的结果为："+JSON.toJSONString(records));
		
		List<AssignCouponDto> couponsResult = context.getNoEnoughTargetNodes();
		
		if (CollUtil.isNotEmpty(couponsResult) && nopass) {
			assignDto.setCoupons(couponsResult);
			assignDto.setRemark("9069");
		} else {
			
//			this.rabbitTemplate.convertAndSend(MqQueueConstant.FUND_EXCHANGE,
//					MqQueueConstant.REIMBURSE_COUPON_TRANDE_TOPIC, assignDto);
			List<AssignCouponDto> assignCouponDto = context.getSuccessUsedNodes();
			try {
				if (CollUtil.isNotEmpty(assignCouponDto)) {
					BigDecimal total = CommonConstant.NO_AMOUNT;
					for (AssignCouponDto dto : assignCouponDto) {
						dto.setCanTicket(assignDto.getCanTicket());
						total = total.add(dto.getAmount());
					}
					assignDto.setCoupons(assignCouponDto);
					assignDto.setOrderType(OrderType.ORDER_TYPE_REIMBURSE.getCateCode());
					assignDto.setRemark(CommonConstant.STRING_BLANK);
					assignDto.setCouponAmount(total);
					log.info("assignDto为："+JSON.toJSONString(assignDto));
		    		IBusinessFlowHandle handle = businessFlowHandleFactory.getBusinessFlowHandle(assignDto.getOrderType());
		    		Map<String, Object> paramsMap = Maps.newHashMap();
		    		paramsMap.put(CommonConstant.PARAM_NAME_BUSINESS_HANDLE_PARAMS, assignDto);
		    		handle.handleBusiFlow(paramsMap);
				}
	    	} catch (Exception e) {
	    		log.info(Exceptions.getStackTraceAsString(e));
	    		FundServiceExceptionGenerator.FundServiceException("9999", "系统繁忙请稍后重试！！");
	    	}
			if (CollUtil.isNotEmpty(couponsResult)) {
				List<AssignCouponDto> rets = Lists.newArrayList();
				rets.addAll(couponsResult);
				rets.addAll(assignCouponDto);
				assignDto.setCoupons(rets);
				assignDto.setRemark("9069");
			}
		}
		log.info("校验报销申请-返回参数{}",JSON.toJSONString(assignDto));
		
		return assignDto;
	}
	
	private Set<String> getIndustryIds(String reimburseCode){
		List<IndustryDto> industrys = refactorInvoicServiceFactory
				.findIndustrysByReimburseCode(reimburseCode);
		Set<String> industryIds = removeDuplication(industrys, "industryId");
		return industryIds;
	}

	private <S,T> Set<S> removeDuplication(List<T> oriDatas, String attrName) {
		Set<S> sets = Sets.newHashSet();
//		List<S> lists = Lists.newArrayList();
		try {
			if (CollUtil.isNotEmpty(oriDatas)) {
				for (T item : oriDatas) {
					S ret = (S) ClassUtil.getClass(item).getMethod(StrUtil.genGetter(attrName)).invoke(item);
					sets.add(ret);
				}
			}
			//lists.addAll(sets);
		} catch (Exception e) {
			log.error(Exceptions.getStackTraceAsString(e));
			FundServiceExceptionGenerator.FundServiceException(e);
		}
		return sets;
	}
	
}
