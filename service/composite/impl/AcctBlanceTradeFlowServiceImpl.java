/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  AcctBlanceTradeFlowServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   2018年9月12日 下午7:02:39   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.dto.AssignCouponDto;
import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.dto.BalanceSearchParams;
import com.taolue.baoxiao.fund.api.dto.BalanceTradeFlowDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceService;
import com.taolue.baoxiao.fund.service.composite.IAcctBlanceTradeFlowService;
import com.taolue.baoxiao.fund.service.composite.IBalanceBusiService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**   
 * @ClassName:  AcctBlanceTradeFlowServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)    
 * @Author: shilei
 * @date:   2018年9月12日 下午7:02:39   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class AcctBlanceTradeFlowServiceImpl implements IAcctBlanceTradeFlowService {
//	private static final String BALANCE_ITEM_CODE_PER = "CM10006";
//	
//	private static final String BALANCE_ITEM_CODE_AFT = "CM10007";
	
	private static final String[] FUND_ACCT_CATE_PERGROUP = new String[]{CommonConstant.BALANCE_ITEM_NO_FULL_YCBZJ,CommonConstant.BALANCE_ITEM_NO_FULL_YCGBX
	};
	
	private static final String[] FUND_ACCT_CATE_AFTGROUP = new String[]{CommonConstant.BALANCE_ITEM_NO_FULL_HFBZJ,CommonConstant.BALANCE_ITEM_NO_FULL_HFGBX
	};
	
	@Autowired
	private IAcctBalanceService acctBalanceService;
	
	@Autowired
	private IBalanceBusiService balanceBusiService;
	
	@Autowired
	private IAcctBalanceBusiService acctBalanceBusiService;
    
	//冻结处理
	private void frezzOption(FundBalanceDto fundBalance, FundTradeFlowDto fundTradeFlow) {
		
		BigDecimal balanceAmount = fundBalance.getBalanceAmount();
		BigDecimal amount = fundTradeFlow.getTradeAmount();
		
		
		if (balanceAmount.compareTo(amount)<0) {
			throw new BaoxiaoException(905,"资金["+fundBalance.getBalanceCode()+"]余额["+balanceAmount.longValue()+"]不足["+amount.longValue()+"]请检查！！");
		}
		
		doCalculator(fundBalance, fundTradeFlow, TradeCateEnums.TRADE_CATE_FREZZ, false);
		
		fundTradeFlow.setStatus(CommonConstant.STATUS_TRADE_FLOW_FREZZING);
		this.acctBalanceService.updateFundBalance(fundBalance);
		this.acctBalanceService.updateFundTradeFlow(fundTradeFlow);	
	}
	
	//解冻处理
    private void unFrezzOption(FundBalanceDto fundBalance, FundTradeFlowDto fundTradeFlow) {
    			//1、查找到对应的冻结记录，指定balanceCode和业务订单号tradeCate为冻结的记录（只会有一条）
    			//2、将记录金额释放会余额，当前交易为额为amount，当前金额为当前blance的余额,最新金额为当前blance的余额+amount
    			//3、更新对应balance数据
    			TbFundTradeFlow frezzTradeFlow = this.findTradeFlow(fundBalance.getBalanceCode(), 
    					fundTradeFlow.getTradeBusiCode(), fundTradeFlow.getBillItemCate(), 
    					fundTradeFlow.getTradeAmount(), TradeCateEnums.TRADE_CATE_FREZZ.getCateCode());
    			
    			if (null == frezzTradeFlow) {
    				throw new BaoxiaoException(904,"找不到资金["+fundBalance.getBalanceCode()+"]订单["+fundTradeFlow.getTradeBusiCode()+"]的科目["+fundTradeFlow.getBillItemCate()+"]的冻结记录，请检查！！");
    			}
    			
    			BigDecimal oriFreezingBalance = fundBalance.getFreezingBalance();
    			
    			BigDecimal amount = frezzTradeFlow.getTradeAmount();
    			
    			if (amount.compareTo(CommonConstant.NO_AMOUNT)<=0) {
    				throw new BaoxiaoException(905,"资金["+fundBalance.getBalanceCode()+"]交易金额<=0请检查！！");
    			}
    			
    			if (oriFreezingBalance.compareTo(amount)<0) {
    				throw new BaoxiaoException(905,"资金["+fundBalance.getBalanceCode()+"]冻结余额["+oriFreezingBalance.longValue()+"]不足["+amount.longValue()+"]请检查！！");
    			}
    			
    			fundTradeFlow.setTradeAmount(amount);
    			
    			doCalculator(fundBalance, fundTradeFlow, TradeCateEnums.TRADE_CATE_UNFREZZ, false);
    			
    			fundTradeFlow.setStatus(CommonConstant.STATUS_TRADE_FLOW_NORMAL);
    			this.acctBalanceService.updateFundBalance(fundBalance);
    			this.acctBalanceService.updateFundTradeFlow(fundTradeFlow);
    			
    			frezzTradeFlow.setStatus(CommonConstant.STATUS_TRADE_FLOW_NORMAL);
    			this.acctBalanceService.updateFundTradeFlow(frezzTradeFlow);
	}
    
    //扣款处理
    private void deductOption(FundBalanceDto fundBalance, FundTradeFlowDto fundTradeFlow) {
    	//解冻过操作过程
    			//1、查找到对应的冻结记录，指定balanceCode和业务订单号tradeCate为冻结的记录（只会有一条）
    			//2、将记录金额释放会余额，当前交易为额为amount，当前金额为当前blance的余额,最新金额为当前blance的余额+amount
    			//3、更新对应balance数据
    			TbFundTradeFlow frezzTradeFlow = this.findTradeFlow(fundBalance.getBalanceCode(), 
    					fundTradeFlow.getTradeBusiCode(), fundTradeFlow.getBillItemCate(), 
    					fundTradeFlow.getTradeAmount(), TradeCateEnums.TRADE_CATE_FREZZ.getCateCode());
    			
    			BigDecimal balanceAmount = fundBalance.getBalanceAmount();
    			BigDecimal freezingAmount = fundBalance.getFreezingBalance();
    			
    			BigDecimal amount = fundTradeFlow.getTradeAmount();
    			
    			log.info("###################################deductOption(FundBalanceDto fundBalance, FundTradeFlowDto fundTradeFlow) fundBalance=" +
    			JSON.toJSONString(fundBalance)+",fundTradeFlow="+JSON.toJSONString(fundTradeFlow));
    			
    			//有冻结金额的情况下
    			if (null != frezzTradeFlow) {
    				if (null != amount && amount.longValue()>0 ) {
    					if (amount.compareTo(frezzTradeFlow.getTradeAmount()) != 0) {
    						throw new BaoxiaoException(904,"资金["+fundBalance.getBalanceCode()+"]订单["+fundTradeFlow.getTradeBusiCode()
    						+"]的科目["+fundTradeFlow.getBillItemCate()+"]的冻结记录的冻结金额为["+frezzTradeFlow.getTradeAmount().longValue()
    								+ "],与当前的交易金额["+amount.longValue()+"]不符合，请检查！！");
    					} 
    				}
    				
    				if (freezingAmount.compareTo(amount)<0) {
        				throw new BaoxiaoException(905,"资金["+fundBalance.getBalanceCode()+"]冻结余额["+freezingAmount.longValue()+"]不足["+amount.longValue()+"]请检查！！");
        			}
    				
    				fundTradeFlow.setTradeAmount(frezzTradeFlow.getTradeAmount());
    				doCalculator(fundBalance, fundTradeFlow, TradeCateEnums.TRADE_CATE_DEDUCT, true);
    				
    				
    				fundTradeFlow.setStatus(CommonConstant.STATUS_TRADE_FLOW_NORMAL);
        			this.acctBalanceService.updateFundBalance(fundBalance);
        			this.acctBalanceService.updateFundTradeFlow(fundTradeFlow);
        			
        			frezzTradeFlow.setStatus(CommonConstant.STATUS_TRADE_FLOW_NORMAL);
        			this.acctBalanceService.updateFundTradeFlow(frezzTradeFlow);
    				
    			//无冻结金额，直接扣除的情况下
    			} else {
    				TbFundTradeFlow adductTradeFlow = this.findTradeFlow(fundTradeFlow.getTradeBusiCode(), 
    						fundTradeFlow.getBillItemCate(), fundBalance.getBalanceCode(), 
    						fundTradeFlow.getTradeAmount(), TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode());
    				
    				if (null != adductTradeFlow) {
    					if (null != amount && amount.longValue()>0 ) {
    						
        					if (amount.compareTo(adductTradeFlow.getTradeAmount()) != 0) {
        						throw new BaoxiaoException(904,"资金["+fundBalance.getBalanceCode()+"]订单["+fundTradeFlow.getTradeBusiCode()
        						+"]的科目["+fundTradeFlow.getBillItemCate()+"]的冻结记录的冻结金额为["+adductTradeFlow.getTradeAmount().longValue()
        								+ "],与当前的交易金额["+amount.longValue()+"]不符合，请检查！！");
        					}
        				}
    					amount = adductTradeFlow.getTradeAmount();
    					fundTradeFlow.setTradeAmount(amount);
    				}
    				
    				if (null == amount || amount.longValue()<=0 ) {
    					throw new BaoxiaoException(904,"找不到资金["+fundBalance.getBalanceCode()+"]订单["+fundTradeFlow.getTradeBusiCode()
    					+"]的科目["+fundTradeFlow.getBillItemCate()+"]的入账记录，"
    							+ "且当前的交易额度不存在，请检查！！");
    				}
    				
    				if (balanceAmount.compareTo(amount)<0) {
        				throw new BaoxiaoException(905,"资金["+fundBalance.getBalanceCode()+"]余额["+balanceAmount.longValue()+"]不足["+amount.longValue()+"]请检查！！");
        			}
    				
    				fundTradeFlow.setTradeLastAmount(balanceAmount.subtract(amount));
    				doCalculator(fundBalance, fundTradeFlow, TradeCateEnums.TRADE_CATE_DEDUCT, false);
    				
    				fundTradeFlow.setStatus(CommonConstant.STATUS_TRADE_FLOW_NORMAL);
        			this.acctBalanceService.updateFundBalance(fundBalance);
        			this.acctBalanceService.updateFundTradeFlow(fundTradeFlow);
    			}
	}
    
    //入账处理
    private void adductOption(FundBalanceDto fundBalance, FundTradeFlowDto fundTradeFlow) {
    	//直接新增额度
		BigDecimal amount = fundTradeFlow.getTradeAmount();
		if (amount.compareTo(CommonConstant.NO_AMOUNT)<=0) {
			throw new BaoxiaoException(905,"资金["+fundBalance.getBalanceCode()+"]交易金额<=0请检查！！");
		}
		
		doCalculator(fundBalance, fundTradeFlow, TradeCateEnums.TRADE_CATE_ADDUCT, false);
		
		fundTradeFlow.setStatus(CommonConstant.STATUS_TRADE_FLOW_NORMAL);
		this.acctBalanceService.updateFundBalance(fundBalance);
		this.acctBalanceService.updateFundTradeFlow(fundTradeFlow);
		
	}
    
    private void frezzAcct(FundBalanceDto balance, FundTradeFlowDto fundTradeFlow, String balanceCate, 
    		BigDecimal changerAmount) {
    	
    	balance.setBalanceAmount(balance.getBalanceAmount().add(changerAmount));
    	balance.setFreezingBalance(balance.getFreezingBalance().add(changerAmount));

    }
    
    private void unFrezzAcct(FundBalanceDto balance, FundTradeFlowDto fundTradeFlow, String balanceCate, 
    		BigDecimal changerAmount) {
    	//DO NO OPERATOR
    }
    
    //金额计算
    protected void doCalculator(FundBalanceDto balance, FundTradeFlowDto fundTradeFlow, TradeCateEnums operation, boolean hasFreez) {
		BigDecimal currentAmount =  balance.getBalanceAmount();
		BigDecimal currentFreezing =  balance.getFreezingBalance();
		BigDecimal changerAmount = fundTradeFlow.getTradeAmount();
		String balanceCate = balance.getBalanceItemCode();
		fundTradeFlow.setTradePreAmount(currentAmount);
		
		if (TradeCateEnums.TRADE_CATE_FREZZ.equals(operation)) {
			if (ArrayUtil.indexOf(FUND_ACCT_CATE_PERGROUP, balanceCate) != ArrayUtil.INDEX_NOT_FOUND
					|| ArrayUtil.indexOf(FUND_ACCT_CATE_AFTGROUP, balanceCate) != ArrayUtil.INDEX_NOT_FOUND) {
				frezzAcct(balance,fundTradeFlow,balanceCate,changerAmount);
			} else {
				balance.setBalanceAmount(currentAmount.subtract(changerAmount));
				balance.setFreezingBalance(currentFreezing.add(changerAmount));
			}
		 } else if (TradeCateEnums.TRADE_CATE_UNFREZZ.equals(operation)) {
			 if (ArrayUtil.indexOf(FUND_ACCT_CATE_PERGROUP, balanceCate) != ArrayUtil.INDEX_NOT_FOUND
						|| ArrayUtil.indexOf(FUND_ACCT_CATE_AFTGROUP, balanceCate) != ArrayUtil.INDEX_NOT_FOUND) {
					unFrezzAcct(balance,fundTradeFlow,balanceCate,changerAmount);
				} else {
					 balance.setBalanceAmount(currentAmount.add(changerAmount));
					 balance.setFreezingBalance(currentFreezing.subtract(changerAmount));
				}
		 } else if (TradeCateEnums.TRADE_CATE_DEDUCT.equals(operation)) {
			 if (ArrayUtil.indexOf(FUND_ACCT_CATE_PERGROUP, balanceCate) != ArrayUtil.INDEX_NOT_FOUND
						|| ArrayUtil.indexOf(FUND_ACCT_CATE_AFTGROUP, balanceCate) != ArrayUtil.INDEX_NOT_FOUND) {
					balance.setBalanceAmount(currentAmount.subtract(changerAmount));
					balance.setFreezingBalance(currentFreezing.subtract(changerAmount));
			 } else {
				 if (hasFreez) {
					 balance.setFreezingBalance(currentFreezing.subtract(changerAmount));
				 } else {
					 balance.setBalanceAmount(currentAmount.subtract(changerAmount));
				 }
			 }
		 } else if (TradeCateEnums.TRADE_CATE_ADDUCT.equals(operation)) {
			 if (ArrayUtil.indexOf(FUND_ACCT_CATE_PERGROUP, balanceCate) != ArrayUtil.INDEX_NOT_FOUND
						|| ArrayUtil.indexOf(FUND_ACCT_CATE_AFTGROUP, balanceCate) != ArrayUtil.INDEX_NOT_FOUND) {
					frezzAcct(balance,fundTradeFlow,balanceCate,changerAmount);
			 } else {
				balance.setBalanceAmount(currentAmount.add(changerAmount));
			 }
		 }
		
		fundTradeFlow.setTradeLastAmount(balance.getBalanceAmount());
	}
    
    @Transactional(rollbackFor=Exception.class)
	public void processTradeFlow(String acctCate, String tradeCate,  String tradeActCode, String billItemCate, 
			String remark, AssignDto assignDto) {
		
		//1、找出此次交易对应的账户和账户资金记录
    	FundBalanceDto fundBalanceDto = acctBalanceBusiService.findSignleBalanceByParams(
    			assignDto.getMemberId(), 
    			assignDto.getMemberId(), 
    			assignDto.getMemberCate(), 
    			acctCate, assignDto.getCoupons().get(0).getCouponId(), 
    			assignDto.getCoupons().get(0).getBusiModel(), 
    			assignDto.getCoupons().get(0).getCanTicket(), 
    			assignDto.getCoupons().get(0).getCanTransfer(), 
    			CommonConstant.STRING_BLANK, 
    			assignDto.getCoupons().get(0).getValidDate(), 
    			assignDto.getCoupons().get(0).getExpireDate());
    	
//		FundBalanceDto fundBalanceDto = this.acctBalanceService.findSignleBalance(
//				assignDto.getMemberId(), 
//				assignDto.getMemberCate(), 
//				acctCate, 
//				"",
//				assignDto.getCoupons().get(0).getCouponId(), 
//				assignDto.getCoupons().get(0).getBusiModel(), 
//				assignDto.getCoupons().get(0).getCanTicket(), 
//				assignDto.getCoupons().get(0).getCanTransfer(), 
//				assignDto.getCoupons().get(0).getValidDate(), 
//				assignDto.getCoupons().get(0).getExpireDate(),
//				assignDto.getMemberId(),"");
		
		//2、构造默认交易流水
		FundTradeFlowDto fundTradeFlow = this.acctBalanceService.warpperTradeFlow(fundBalanceDto.getBalanceCode(),
				assignDto.getOrderType(), assignDto.getBusiOrderNo(), billItemCate, 
				tradeCate, tradeActCode, assignDto.getCoupons().get(0).getAmount(), 
				assignDto.getSource(), remark, assignDto.getBusiModel(), 
				assignDto.getCoupons().get(0).getAscNumber());
		
		//3、根据交易类型和账户资金记录完善交易流水
		if (TradeCateEnums.lookupByCode(tradeCate).equals(TradeCateEnums.TRADE_CATE_FREZZ)) {
			this.frezzOption(fundBalanceDto, fundTradeFlow);
		} else if (TradeCateEnums.lookupByCode(tradeCate).equals(TradeCateEnums.TRADE_CATE_UNFREZZ)) {
			this.unFrezzOption(fundBalanceDto, fundTradeFlow);
		} else if (TradeCateEnums.lookupByCode(tradeCate).equals(TradeCateEnums.TRADE_CATE_DEDUCT)) {
			this.deductOption(fundBalanceDto, fundTradeFlow);
		} else if (TradeCateEnums.lookupByCode(tradeCate).equals(TradeCateEnums.TRADE_CATE_ADDUCT)) {
			this.adductOption(fundBalanceDto, fundTradeFlow);
		}
	}
	
    @Override
	@Transactional
	public void processTradeFlow(BalanceTradeFlowDto balanceTradeFlowDto) {
		
    	FundBalanceDto fundBalanceDto = acctBalanceBusiService.findSignleBalanceByParams(
    			balanceTradeFlowDto.getCompanyId(), 
    			balanceTradeFlowDto.getMemberId(), 
    			balanceTradeFlowDto.getMemberCate(), 
    			balanceTradeFlowDto.getAcctCate(),
    			balanceTradeFlowDto.getBalanceItemCode(), 
    			balanceTradeFlowDto.getBusiModel(), 
    			balanceTradeFlowDto.getCanTicket(), 
    			balanceTradeFlowDto.getCanTransfer(), 
    			balanceTradeFlowDto.getOwnerId(),
    			balanceTradeFlowDto.getValidDate(), 
    			balanceTradeFlowDto.getExpireDate());
    	
		//1、找出此次交易对应的账户和账户资金记录
//		FundBalanceDto fundBalanceDto = this.acctBalanceService.findSignleBalance(
//				balanceTradeFlowDto.getMemberId(), 
//				balanceTradeFlowDto.getMemberCate(), 
//				balanceTradeFlowDto.getAcctCate(), 
////				balanceTradeFlowDto.getAcctBalanceIitemCode(),
//				balanceTradeFlowDto.getBalanceItemCode(), 
//				balanceTradeFlowDto.getBusiModel(), 
//				balanceTradeFlowDto.getCanTicket(), 
//				balanceTradeFlowDto.getCanTransfer(), 
//				balanceTradeFlowDto.getValidDate(), 
//				balanceTradeFlowDto.getExpireDate(),
//				balanceTradeFlowDto.getCompanyId(),
//				balanceTradeFlowDto.getOwnerId());
		
		log.info("0000000000processTradeFlow0000000000"+JSON.toJSONString(balanceTradeFlowDto));
		//2、构造默认交易流水
		FundTradeFlowDto fundTradeFlow = this.acctBalanceService.warpperTradeFlow(fundBalanceDto.getBalanceCode(),
				balanceTradeFlowDto.getTradeBusiCate(), balanceTradeFlowDto.getTradeBusiCode(), balanceTradeFlowDto.getBillItemCate(), 
				balanceTradeFlowDto.getTradeCate(), balanceTradeFlowDto.getTransActCate(), balanceTradeFlowDto.getTradeAmount(),
				balanceTradeFlowDto.getSource(), balanceTradeFlowDto.getRemark(), balanceTradeFlowDto.getBusiModel(), 
				balanceTradeFlowDto.getTradeFlowOrder());
		
		//3、根据交易类型和账户资金记录完善交易流水
		if (TradeCateEnums.lookupByCode(balanceTradeFlowDto.getTradeCate()).equals(TradeCateEnums.TRADE_CATE_FREZZ)) {
			this.frezzOption(fundBalanceDto, fundTradeFlow);
		} else if (TradeCateEnums.lookupByCode(balanceTradeFlowDto.getTradeCate()).equals(TradeCateEnums.TRADE_CATE_UNFREZZ)) {
			this.unFrezzOption(fundBalanceDto, fundTradeFlow);
		} else if (TradeCateEnums.lookupByCode(balanceTradeFlowDto.getTradeCate()).equals(TradeCateEnums.TRADE_CATE_DEDUCT)) {
			this.deductOption(fundBalanceDto, fundTradeFlow);
		} else if (TradeCateEnums.lookupByCode(balanceTradeFlowDto.getTradeCate()).equals(TradeCateEnums.TRADE_CATE_ADDUCT)) {
			this.adductOption(fundBalanceDto, fundTradeFlow);
		}
	}
	
    @Override
	@Transactional
	public void createTradeFlowsByBalanceTradeFlowDto(List<BalanceTradeFlowDto> fundTradeFlows) {
		for (BalanceTradeFlowDto balanceTradeFlowDto : fundTradeFlows) {
			this.processTradeFlow(balanceTradeFlowDto);
		}
	}
	
    @Override
	public List<TbFundTradeFlow> findTradeFlows(String busiCode, String billItemCate, String... tradeCates) {
		return this.acctBalanceService.findTradeFlows(busiCode, billItemCate, null, null, tradeCates);
	}

	/**   
	 * <p>Title: findAcctBalanceByCode</p>   
	 * <p>Description: </p>   
	 * @param balanceCode
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IAcctBlanceTradeFlowService#findAcctBalanceByCode(java.lang.String)   
	 */  
//	@Override
//	public FundBalanceDto findAcctBalanceByCode(String balanceCode) {
//		return acctBalanceBusiService.findSignleBalanceByCode(balanceCode);
////		return this.acctBalanceService.findAcctBalanceByCode(balanceCode);
//	}

//	@Override
//	public List<FundBalanceDto> selectFundBalanceItemAmount(Map<String, Object> params){
//		return this.balanceBusiService.selectFundBalanceForChash(params);
//	}
	
	@Override
	@Transactional
	public List<BalanceTradeFlowDto> processBusiness(AssignDto assignDto) {
		//定位账号，校验账号资金记录，每个券的资金记录都应该大于等于扣款时的额度；扣款券的列表是有顺序的，
		//因此除了最后一条券记录外，其余券都是全额扣除，这些券的额度需要进行足额判断，最后一条券的额度
		//应该高于所需扣除的总额度扣除其他券的所需额度后剩余的额度，这些都需要进行额度锁定，避免在
		//处理过程中出现变动，因此所有处理应该在同一事务中
		
		//准备查询参数】
		Map<String,Object> params = Maps.newHashMap();
		List<AssignCouponDto> validCoupons = assignDto.getCoupons();
		List<AssignCouponDto> newvalidCoupons=Lists.newArrayList();
		for (AssignCouponDto assignCouponDto : validCoupons) {
			assignCouponDto.setAcctInstNo(assignCouponDto.getAcctInstNo().trim());
			assignCouponDto.setCouponId(assignCouponDto.getCouponId().trim());
			newvalidCoupons.add(assignCouponDto);
		}
		params.put("coupons",newvalidCoupons);
		log.info("查询券额度数据入参参数:{}",JSON.toJSONString(params));
		//查询并锁定券列表中的额度，若返回的列表为空或者长度小于coupons的长度，则说明额度不足
		List<FundBalanceDto> fundBalances = this.balanceBusiService.selectFundBalanceForChash(params);
		
		log.info("^^^^^^^^^^^^^券交易/转让处理，锁定券列表："+JSON.toJSONString(fundBalances));
		if (CollUtil.isEmpty(fundBalances) 
				//|| fundBalances.size() != assignDto.getCoupons().size()
				) {
			throw new BaoxiaoException(901,"消费券金额不足");
		}
		
		List<BalanceTradeFlowDto> results = Lists.newArrayList();
		//循环券列表，确定每个券的额度的扣除组成
		for (AssignCouponDto coupon : validCoupons) {
			
			//拼装查询参数
			List<String> balanceItemNos = Lists.newArrayList();
			balanceItemNos.add(coupon.getCouponId());
			
			BalanceSearchParams balanceParams = new BalanceSearchParams();
			balanceParams.setAcctInstNo(coupon.getAcctInstNo());
			balanceParams.addBalanceItemCode(coupon.getCouponId());
			//balanceParams.addMemberCate(assignDto.getMemberCate());
			balanceParams.setAcctCate(assignDto.getAcctCate());
			balanceParams.setCanTicket(assignDto.getCanTicket());
			balanceParams.setLockSecond(3);
			//查询账户下当前券id的所有资金记录并按权重排序
			List<FundBalanceDto> couponBalances = balanceBusiService.selectByConditions(balanceParams);
			
			log.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&List<FundAcctBalanceDto> couponBalances = " + JSON.toJSONString(couponBalances));
			if (CollUtil.isEmpty(couponBalances)) {
				throw new BaoxiaoException(902,"无可用的消费券！！");
		    }
			//循环排序后的当前券的资金记录，扣除当前券的所需额度
			List<BalanceTradeFlowDto> couponsToUse = fetchCouponsAmounts(couponBalances, assignDto, coupon);
			if (CollUtil.isNotEmpty(couponsToUse)) {
				results.addAll(couponsToUse);
			}
			//循环排序后的当前券的资金记录，
			//扣除当前券的所需额度
//			this.decutAmount(fundAcctBalanceDtos, assignDto, coupon.getAmount());
		}
		return results;
	}
	
	private List<BalanceTradeFlowDto> fetchCouponsAmounts(List<FundBalanceDto> couponBalances, 
			AssignDto assignDto, AssignCouponDto coupon) {
		
		BigDecimal couponItemAmount = coupon.getAmount();
		
		List<BalanceTradeFlowDto> balanceTradeFlowDtos = Lists.newArrayList();
		BigDecimal tmpCouponItemAmount = new BigDecimal(couponItemAmount.longValue());
		
		int idx = 1;
		for (int i=0; i<couponBalances.size(); i++) {
			boolean breakFlag = false;
			FundBalanceDto fundBalanceDto = couponBalances.get(i);
			log.info("扣款流程"+i+"fundBalanceDto = " + JSON.toJSONString(fundBalanceDto));
			if (fundBalanceDto.getBalanceAmount().compareTo(CommonConstant.NO_AMOUNT)<=0) {
				continue;
			}
			BigDecimal currentItemAmount = fundBalanceDto.getBalanceAmount();
			tmpCouponItemAmount = tmpCouponItemAmount.subtract(currentItemAmount);
			
			//创建交易记录
			BalanceTradeFlowDto balanceTradeFlowDto = new BalanceTradeFlowDto(
					assignDto.getBusiOrderNo(), 
					assignDto.getOrderType(), 
					fundBalanceDto.getBusiModel(), 
					fundBalanceDto.getMemberId(), 
					fundBalanceDto.getCompanyId(),
					fundBalanceDto.getOwnerId(),
					assignDto.getMemberCate(), 
					assignDto.getAcctCate(), 
//					CommonConstant.STRING_BLANK,
					fundBalanceDto.getBalanceItemCode(), 
					assignDto.getBillItemCate(), 
					assignDto.getTradeCate(), 
					assignDto.getActionType(), 
					currentItemAmount, 
					assignDto.getSource(), 
					assignDto.getRemark(), 
					new BigDecimal(idx),fundBalanceDto.getCanTransfer());
			balanceTradeFlowDto.setBalanceCode(fundBalanceDto.getBalanceCode());
			idx = idx+1;
			//如果当前剩余需扣除金额小于零说明已经足额
			if (tmpCouponItemAmount.compareTo(CommonConstant.NO_AMOUNT)<=0) {
				balanceTradeFlowDto.setTradeAmount(currentItemAmount.add(tmpCouponItemAmount));
				breakFlag = true;
			}
			if (StrUtil.isNotBlank(coupon.getBusiItemCode())) {
				balanceTradeFlowDto.setTradeFlowCode(coupon.getBusiItemCode());
			}
			balanceTradeFlowDtos.add(balanceTradeFlowDto);
			if (breakFlag) {
				break;
			}
		}
		return balanceTradeFlowDtos;
	}
		
	private TbFundTradeFlow findTradeFlow(String busiCode, String billItemCate, String balanceCode,  
			BigDecimal tradeAmount, String... tradeCates) {
		List<TbFundTradeFlow> tradeflows = this.acctBalanceService.findTradeFlows(busiCode, billItemCate, 
				balanceCode, tradeAmount, tradeCates);
		if (CollUtil.isNotEmpty(tradeflows)) {
			return tradeflows.get(0);
		}
		return null;
	}
	
	@Override
	public List<TbFundTradeFlow> findBalanceFlowDetails(String memberId, String paramCode, 
			Date beginTime, Date endTime) {
		return this.acctBalanceService.findBalanceFlowDetails(memberId, paramCode, beginTime, endTime);
	}
	
//	@Override
//	public List<TbFundTradeFlow> findTradeFlows(String balanceCode, String busiModel, String billItemCate, String orderType,
//			String tradeCate, String status, String busiCode, BigDecimal tradeAmount, Date beginTime, Date endTime) {
//		return this.acctBalanceService
//				.findTradeFlows(balanceCode, busiModel, billItemCate, orderType, 
//						tradeCate, status, busiCode, tradeAmount, beginTime, endTime);
//	}
	
//	@Override
//	public FundBalanceDto findPerOrAftPaymentBalance(String memberId, String... busiModel) {
//		String param = ArrayUtil.firstNonNull(busiModel);
//		if (StrUtil.isBlank(param)) {
//			param = BusiModelEnums.BUSI_MODEL_YCCM.getCateCode();
//		}
//		return acctBalanceBusiService.findPerOrAftPaymentBalance(memberId, param);
//	}
}
