/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  AcctBalanceBusiServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 10, 2018 4:20:17 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.BalanceFlowDetailDTO;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.api.dto.TradeFlowSearchParams;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.common.listener.BusiFlowConfigInitListener;
import com.taolue.baoxiao.fund.entity.OrderBusi;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.service.IOrderBusiService;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.IAcctTradeFlowBusiService;
import com.taolue.baoxiao.fund.service.composite.ITradeFlowBusiService;
import com.taolue.baoxiao.fund.service.remote.IRefactorCouponServiceFactory;
import com.taolue.baoxiao.fund.service.remote.IRefactorInvoicServiceFactory;
import com.taolue.invoice.api.dto.ReimburserQueryDTO;
import com.taolue.invoice.api.vo.ReimburseVO;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**   
 * @ClassName:  AcctBalanceBusiServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   Dec 10, 2018 4:20:17 PM   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class AcctTradeFlowBusiServiceImpl implements IAcctTradeFlowBusiService {

	@Autowired
	private ITradeFlowBusiService tradeFlowBusiService;
	
	@Autowired
	private IAcctBalanceBusiService acctBalanceBusiService;
	
	@Autowired
	private IOrderBusiService orderBusiService;
	
	@Autowired
	private IRefactorInvoicServiceFactory refactorInvoicServiceFactory;
	
	@Autowired
	private IRefactorCouponServiceFactory refactorCouponServiceFactory;
	
	/**   
	 * <p>Title: findTradeFlowsPaged</p>   
	 * <p>Description: </p>   
	 * @param queryParams
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IAcctTradeFlowBusiService#findTradeFlowsPaged(com.taolue.baoxiao.fund.api.dto.BalanceFlowDetailDTO)   
	 */  
	@Override
	public Query<FundTradeFlowDto> findTradeFlowsPaged(BalanceFlowDetailDTO queryParams) {
		
		Query<FundTradeFlowDto> query = new Query<>(Maps.newHashMap());
		
		query.setCurrent(queryParams.getCurrentPage());
		query.setSize(queryParams.getPageSize());
		
		String paramCode = queryParams.getParamCode();
		if (StrUtil.isEmpty(paramCode)) {
			FundServiceExceptionGenerator.FundServiceException("未指定查询编号");
		}
		if (log.isDebugEnabled()) {
			log.debug("IAcctTradeFlowBusiService.findTradeFlowsPaged查询参数BalanceFlowDetailDTO queryParams=",
					JSON.toJSONString(queryParams));
		}
		
		TradeFlowSearchParams searchParams = null;
		
		if ("YCZ-ALL".equals(paramCode)) {
			searchParams = new TradeFlowSearchParams();
			for (Map.Entry<String, TradeFlowSearchParams> enetry : getSearchConfigs("CM^YCZ").entrySet()) {
				TradeFlowSearchParams params = enetry.getValue();
				params.setMemberId(queryParams.getMemberId());
				params.setBeginTime(queryParams.getBeginTime());
				params.setEndTime(queryParams.getEndTime());
				searchParams.addMultisParams(params);
			}
		} else if ("HFF-ALL".equals(paramCode)) {
			searchParams = new TradeFlowSearchParams();
			for (Map.Entry<String, TradeFlowSearchParams> enetry : getSearchConfigs("CM^HFF").entrySet()) {
				TradeFlowSearchParams params = enetry.getValue();
				params.setMemberId(queryParams.getMemberId());
				params.setBeginTime(queryParams.getBeginTime());
				params.setEndTime(queryParams.getEndTime());
				searchParams.addMultisParams(params);
			}
		} else {
			searchParams = getSearchConfigs(CommonConstant.STRING_BLANK).get(paramCode);
			searchParams.setMemberId(queryParams.getMemberId());
			searchParams.setBeginTime(queryParams.getBeginTime());
			searchParams.setEndTime(queryParams.getEndTime());
		}
		query = this.tradeFlowBusiService.findTradeFlowsPaged(query, searchParams);
		return query;
	}
	
	@Override
	public Query<FundTradeFlowDto> findBxTradeFlowsPaged(BalanceFlowDetailDTO queryParams) {
		
		String paramCode = queryParams.getParamCode();
		String balanceCate = queryParams.getBalanceCate();
		String tradeCate = queryParams.getTradeCate();
		
		Map<String, TradeFlowSearchParams> configs = getBXConfigs(paramCode, balanceCate, tradeCate);
		if (CollUtil.isEmpty(configs)) {
			FundServiceExceptionGenerator.FundServiceException("无法定位到查询配置，请检查type={}, cate={}, op={}",
					paramCode, balanceCate, tradeCate);
		}
		
		Query<FundTradeFlowDto> query = new Query<>(Maps.newHashMap());
		query.setCurrent(queryParams.getCurrentPage());
		query.setSize(queryParams.getPageSize());
		
		TradeFlowSearchParams searchParams = new TradeFlowSearchParams();
		for (Map.Entry<String, TradeFlowSearchParams> enetry : configs.entrySet()) {
			TradeFlowSearchParams params = enetry.getValue();
			params.setMemberId(queryParams.getMemberId());
			params.setBeginTime(queryParams.getBeginTime());
			params.setEndTime(queryParams.getEndTime());
			searchParams.addMultisParams(params);
		}
		query = this.tradeFlowBusiService.findTradeFlowsPaged(query, searchParams);
		return query;
	}

	public List<FundTradeFlowDto> findTradeFlows(TradeFlowSearchParams params) {
		return this.tradeFlowBusiService.findTradeFlows(params);
	}
	public List<FundTradeFlowDto> findTradeFlowsRefund(TradeFlowSearchParams params) {
		return this.tradeFlowBusiService.findTradeFlowsRefund(params);
	}

	private Map<String, TradeFlowSearchParams> getBXConfigs(String typeKey, String cateKey, String opKey){
		Map<String, TradeFlowSearchParams> configs = null;
		if (MemberCateEnums.MEMBER_CATE_CMP.getCateCode().equals(typeKey)) {
			configs = BusiFlowConfigInitListener.findAllSearchConfigs("BX^CM");
		} else {
			configs = BusiFlowConfigInitListener.findAllSearchConfigs("BX^PE");
		}
		
		if (CollUtil.isNotEmpty(configs)) {
			if ("ALL".equals(cateKey) && "ALL".equals(opKey)) {
				return configs;
			} else if ("ALL".equals(cateKey) && !"ALL".equals(opKey)) {
				Map<String, TradeFlowSearchParams> subConfigs = Maps.newHashMap();
				for (Map.Entry<String, TradeFlowSearchParams> entry : configs.entrySet()) {
					String[] keys = entry.getKey().split("-");
					if (keys[1].equals(opKey)) {
						subConfigs.put(entry.getKey(), entry.getValue());
					}
				}
				return subConfigs;
			} else if (!"ALL".equals(cateKey) && "ALL".equals(opKey)) {
				Map<String, TradeFlowSearchParams> subConfigs = Maps.newHashMap();
				for (Map.Entry<String, TradeFlowSearchParams> entry : configs.entrySet()) {
					String[] keys = entry.getKey().split("-");
					if (keys[0].equals(cateKey)) {
						subConfigs.put(entry.getKey(), entry.getValue());
					}
				}
				return subConfigs;
			} else if (!"ALL".equals(cateKey) && !"ALL".equals(opKey)) {
				Map<String, TradeFlowSearchParams> subConfigs = Maps.newHashMap();
				String key = cateKey+"-"+opKey;
				subConfigs.put(key, configs.get(key));
				return subConfigs;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private Map<String, TradeFlowSearchParams> getSearchConfigs(String configType) {
		Map<String, TradeFlowSearchParams> result = null;
		if (StrUtil.isBlank(configType)) {
			result = BusiFlowConfigInitListener.findAllSearchConfigs("CM^YCZ","CM^HFF");
		} else {
			result = BusiFlowConfigInitListener.findCMSearchConfigs(configType);
		}
		if (CollUtil.isEmpty(result)) {
			FundServiceExceptionGenerator.FundServiceException("无法定位到查询配置，请检查");
		}
		return result;
	}
	
	public Query<FundTradeFlowDto> findTradeFlowsListPaged(Query<FundTradeFlowDto> query, TradeFlowSearchParams params) {
		Query<FundTradeFlowDto> results = this.tradeFlowBusiService.queryFlowsAndRefund(query, params);
		List<FundTradeFlowDto> retResults = Lists.newArrayList();
		if (null != results) {
			List<FundTradeFlowDto> fundradeFlowDtos = results.getRecords();
			Map<String, List<FundTradeFlowDto>> mapedBusiCodes = Maps.newHashMap();
			
			for (FundTradeFlowDto fundTradeFlowDto : fundradeFlowDtos) {
				String busiCate = fundTradeFlowDto.getTradeBusiCate();
				List<FundTradeFlowDto> busiCodes = mapedBusiCodes.get(busiCate);
				if (CollUtil.isEmpty(busiCodes)) {
					busiCodes = Lists.newArrayList();
				}
				busiCodes.add(fundTradeFlowDto);
				mapedBusiCodes.put(busiCate, busiCodes);
			}
			
			if (CollUtil.isNotEmpty(mapedBusiCodes)) {
				for (Map.Entry<String, List<FundTradeFlowDto>> entry : mapedBusiCodes.entrySet()) {
					String type = entry.getKey();
					List<FundTradeFlowDto> busiDtos = entry.getValue();
					Map<String, FundTradeFlowDto> mapedDto = Maps.newHashMap();
					List<String> busiCodes = Lists.newArrayList();
					if (CollUtil.isNotEmpty(busiDtos)) {
						for (FundTradeFlowDto fundTradeFlowDto : busiDtos) {
							mapedDto.put(fundTradeFlowDto.getTradeBusiCode(), fundTradeFlowDto);
							busiCodes.add(fundTradeFlowDto.getTradeBusiCode());
						}
					}
					
					if (OrderType.ORDER_TYPE_REIMBURSE.getCateCode().equals(type)) {
						ReimburserQueryDTO queryParams = new ReimburserQueryDTO();
						Map<String, Object> pageParams = Maps.newHashMap();
						queryParams.setReimburseCodes(busiCodes.toArray(new String[0]));
						pageParams.put("page", "1");
						pageParams.put("limit", busiCodes.size());
						Page<ReimburseVO> pagedRvo = this.refactorInvoicServiceFactory
							.getRefactorInvoiceReimburseService().selectReimburseVoPage(pageParams, queryParams);
						
						if (null != pagedRvo && CollUtil.isNotEmpty(pagedRvo.getRecords())) {
							Map<String, ReimburseVO> mapedReimburseVO = Maps.newHashMap();
							for (ReimburseVO reimburseVO : pagedRvo.getRecords()) {
								mapedReimburseVO.put(reimburseVO.getReimburseCode(), reimburseVO);
							}
							for (String busiCode : busiCodes) {
								ReimburseVO reimburseVO = mapedReimburseVO.get(busiCode);
								if (null != reimburseVO) {
									FundTradeFlowDto flowDto = mapedDto.get(busiCode);
									flowDto.setAuthorizationName(reimburseVO.getCompanyName());
									flowDto.setBeginTime(reimburseVO.getReimbDate());
									retResults.add(flowDto);
								}
							}
						}
					}
					
					if (OrderType.ORDER_TYPE_COUPON_ASSIGN.getCateCode().equals(type)) {
						R<List<Map<String, Object>>> retVo = refactorCouponServiceFactory
								.getRefactorCouponAllotService().findcompanyByAllotNo(busiCodes);
						
						if (null != retVo && CollUtil.isNotEmpty(retVo.getData())) {
							Map<String, Map<String, Object>> mapedAssignVO = Maps.newHashMap();
							for (Map<String, Object> row :  retVo.getData()) {
								String key = (String)row.get("allotNo");
								mapedAssignVO.put(key, row);
							}
							log.info("获取发放的单据信息："+JSON.toJSONString(retVo));
							log.info("单据集合："+JSON.toJSONString(busiCodes));
							for (String busiCode : busiCodes) {
								
								log.info("单据号码："+JSON.toJSONString(busiCodes));
								Map<String, Object> assignVo = mapedAssignVO.get(busiCode);
								log.info("获取单据信息："+JSON.toJSONString(assignVo));
								if (null != assignVo) {
									FundTradeFlowDto flowDto = mapedDto.get(busiCode);
									flowDto.setAuthorizationName(assignVo.getOrDefault("companyName", "").toString());
									String dateStr = assignVo.getOrDefault("date","").toString();
									if (StrUtil.isNotBlank(dateStr)) {
										flowDto.setBeginTime(DateUtil.parse(dateStr,DatePattern.NORM_DATETIME_PATTERN).toJdkDate());
									}
									retResults.add(flowDto);
								}
							}
						}
					}
					
					if (OrderType.ORDER_TYPE_CASH.getCateCode().equals(type)) {
						EntityWrapper<OrderBusi> wrapper = new EntityWrapper<>();
						wrapper.in("order_code", busiCodes);
						List<OrderBusi> listDatas = this.orderBusiService.selectList(wrapper);
						log.info("获取消费的单据信息："+JSON.toJSONString(listDatas));
						
						if (CollUtil.isNotEmpty(listDatas)) {
							Map<String, OrderBusi> mapedOrderVO = Maps.newHashMap();
							for (OrderBusi row :  listDatas) {
								mapedOrderVO.put(row.getOrderCode(), row);
							}
							for (String busiCode : busiCodes) {
								log.info("消费单据号码："+JSON.toJSONString(busiCodes));
								OrderBusi orderBusiVo = mapedOrderVO.get(busiCode);
								log.info("获取消费的单据信息："+JSON.toJSONString(orderBusiVo));
								if (orderBusiVo != null) {
									FundTradeFlowDto flowDto = mapedDto.get(busiCode);
//									flowDto.setAuthorizationName(assignVo.getOrDefault("companyName", "").toString());
//									Date date = (Date)assignVo.get("date");
									flowDto.setBeginTime(orderBusiVo.getCreateTime());
									retResults.add(flowDto);
								}
							}
						}
					}
					
					if (OrderType.ORDER_TYPE_REFUND.getCateCode().equals(type)) {
						EntityWrapper<OrderBusi> wrapper = new EntityWrapper<>();
						wrapper.in("order_code", busiCodes);
						List<OrderBusi> listDatas = this.orderBusiService.selectList(wrapper);
						log.info("获取消费券退款的单据信息："+JSON.toJSONString(listDatas));
						
						if (CollUtil.isNotEmpty(listDatas)) {
							Map<String, OrderBusi> mapedOrderVO = Maps.newHashMap();
							for (OrderBusi row :  listDatas) {
								mapedOrderVO.put(row.getOrderCode(), row);
							}
							for (String busiCode : busiCodes) {
								log.info("消费券退款单据号码："+JSON.toJSONString(busiCodes));
								OrderBusi orderBusiVo = mapedOrderVO.get(busiCode);
								log.info("获取消费券退款的单据信息："+JSON.toJSONString(orderBusiVo));
								if (orderBusiVo != null) {
									FundTradeFlowDto flowDto = mapedDto.get(busiCode);
									flowDto.setBeginTime(orderBusiVo.getUpdatedTime());
									retResults.add(flowDto);
								}
							}
						}
					}
				}
			}
		}
		if (CollUtil.isNotEmpty(retResults)) {
			retResults = CollUtil.sort(retResults, new Comparator<FundTradeFlowDto>() {

				@Override
				public int compare(FundTradeFlowDto o1, FundTradeFlowDto o2) {
					if (null == o1) {
						return -1;
					}
					if (o1.compareTo(o2)<0) {
						return 1;
					}
					if (o1.compareTo(o2)>0) {
						return -1;
					}
					if (o1.compareTo(o2)==0) {
						return 0;
					}
					return 0;
				}
			});
			
		}
		results.setRecords(retResults);
		return results;
	}
	
	public Multimap<String, Object> queryUnchargeFundTradeFlow(String tradeBusiCode) {
		TradeFlowSearchParams params = new TradeFlowSearchParams();
		params.setBusiOrderCode(tradeBusiCode);
		params.setOrderType(OrderType.ORDER_TYPE_CASH.getCateCode());
		params.addTradeCates(TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode());
		params.setTransActCate(ActionType.ACTION_TYPE_OUT.getCateCode());
		
		TbFundTradeFlow tradeFlow = new TbFundTradeFlow();
		tradeFlow.setTradeBusiCode(tradeBusiCode);
		tradeFlow.setTradeBusiCate(OrderType.ORDER_TYPE_CASH.getCateCode());
		tradeFlow.setTradeCate(TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode());
		tradeFlow.setTransActCate(ActionType.ACTION_TYPE_OUT.getCateCode());
		List<FundTradeFlowDto> tradeFlowDtos = this.tradeFlowBusiService.findTradeFlows(params);
		
//		List<TbFundTradeFlow> tradeFlows = this.fundTradeFlowService.selectList(new EntityWrapper<TbFundTradeFlow>(tradeFlow));
		if (CollUtil.isEmpty(tradeFlowDtos)) {
			FundServiceExceptionGenerator.FundServiceException("9013", new Object[] {tradeBusiCode});
		}
		
		ListMultimap<String, Object> mapedBalance =LinkedListMultimap.create() ;
		for (FundTradeFlowDto flowDto : tradeFlowDtos) {
			if (CollUtil.isEmpty(mapedBalance.get(flowDto.getBalanceCode()))) {
				FundBalanceDto fundBalanceDto = this.acctBalanceBusiService.findSignleBalanceByCode(flowDto.getBalanceCode());
//				BeanCopier<TbFundBalance> fundBalanceCopier = new BeanCopier<TbFundBalance>(fundBalanceDto, 
//						new TbFundBalance(), new CopyOptions());
				mapedBalance.put(flowDto.getBalanceCode(), fundBalanceDto);
				mapedBalance.put(flowDto.getBalanceCode(), flowDto);
				log.info("退款参数mapedBalance:{}",JSON.toJSONString(mapedBalance));
			}
		}
		return mapedBalance;
	}
}
