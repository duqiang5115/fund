/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  IPersonalUserServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: DELL  
 * @date:   2019年3月19日 下午9:05:39   
 * @version V1.0 
 * @Copyright: 2019 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.CouponWeightEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.SourceFrom;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TransType;
import com.taolue.baoxiao.common.dto.AssignCouponDto;
import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.IPersonalUserService;
import com.xiaoleilu.hutool.collection.CollUtil;

import lombok.extern.slf4j.Slf4j;

/**   
 * <p>ClassName:  IPersonalUserServiceImpl </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: yangfan</br>
 * <p>date:   2019年3月19日 下午9:05:39 </br>  
 *     
 * @Copyright: 2019 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class IPersonalUserServiceImpl implements IPersonalUserService{

	@Autowired
    private IAcctBalanceBusiService acctBalanceBusiService;
	
	/**   
	 * <p>Title: rechargeBusiness</p>   
	 * <p>Description: </p>   
	 * @param assignDto
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IPersonalUserService#rechargeBusiness(com.taolue.baoxiao.common.dto.AssignDto)   
	 */  
	@Override
	public Boolean rechargeBusiness(AssignDto assignDto) {
		FundBalanceDto balanceDto = acctBalanceBusiService.findSignleBalanceByParams(AcctCateEnums.ACCT_CATE_PTQYX.getCateMgn(), 
				assignDto.getMemberId(),MemberCateEnums.MEMBER_CATE_ALO.getCateCode(), 
				AcctCateEnums.ACCT_CATE_SALARY.getCateCode(), 
				AcctCateEnums.ACCT_CATE_SALARY.getCateCode(), BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
				CommonConstant.STATUS_YES, CommonConstant.STATUS_YES, assignDto.getMemberId(), null, null);
		if(null == balanceDto) {
			log.debug("会员现金账户无资金流！");
		}
		/*FundTradeFlowDto tradeFlowDto = new FundTradeFlowDto();
		//流水编号
		tradeFlowDto.setTradeFlowCode(CommonConstant.KEY_PERFIX_TRADEFLOW_NO+IdWorker.getIdStr());
		//资金编号
		tradeFlowDto.setBalanceCode(balanceDto.getBalanceCode());
		//业务模式
		tradeFlowDto.setTradeBusiCode(assignDto.getBusiOrderNo());
		tradeFlowDto.setBusiModel(BusiModelEnums.BUSI_MODEL_NONE.getCateCode());
		tradeFlowDto.setTradeBusiCate(assignDto.getOrderType());
		tradeFlowDto.setTransBusiCateName(OrderType.getOrderType(assignDto.getOrderType()).getCateName());*/
		//交易归属账单科目
		String billItemCate = "";
		if(BillItemSubCate.BILL_ITEM_BANK_RECHARGE.getCateMgn().equals(assignDto.getBillItemCate())) {
			billItemCate = BillItemSubCate.BILL_ITEM_BANK_RECHARGE.getCateCode();
		}else if(BillItemSubCate.BILL_ITEM_WECHAT_RECHARGE.getCateMgn().equals(assignDto.getBillItemCate())) {
			billItemCate = BillItemSubCate.BILL_ITEM_WECHAT_RECHARGE.getCateCode();
		}else if(BillItemSubCate.BILL_ITEM_ALIPAY_RECHARGE.getCateMgn().equals(assignDto.getBillItemCate())) {
			billItemCate = BillItemSubCate.BILL_ITEM_ALIPAY_RECHARGE.getCateCode();
		}
		/*tradeFlowDto.setBillItemCate(billItemCate);
		//操作
		tradeFlowDto.setTradeCate(TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode());
		tradeFlowDto.setTransCateName(TradeCateEnums.TRADE_CATE_ADDUCT.getCateName());
		//交易流水动作
		tradeFlowDto.setTransActCate(ActionType.ACTION_TYPE_IN.getCateCode());
		tradeFlowDto.setTransActName(ActionType.ACTION_TYPE_IN.getCateName());
		//充值金额
		tradeFlowDto.setTradeAmount(assignDto.getOrderAmount());
		//状态
		tradeFlowDto.setStatus(CommonConstant.STATUS_TRADE_FLOW_NORMAL);
		//来源
		tradeFlowDto.setSource(SourceFrom.getSourceFrom(assignDto.getSource()).getCateName());
		//设置本次交易过程中，本条流水的顺序号
		tradeFlowDto.setTradeFlowOrder(new BigDecimal("1"));
		tradeFlowDto.setRemark("独立C现金账户 - 充值流水");
		TbFundTradeFlow entity = new TbFundTradeFlow();
		BeanUtils.copyProperties(tradeFlowDto, entity);
		entity.insert();*/
		String remark = "独立C现金账户 - 充值流水";
		return this.creatFundTradeFlowDto(CommonConstant.KEY_PERFIX_TRADEFLOW_NO+IdWorker.getIdStr(),
				balanceDto.getBalanceCode(),assignDto.getBusiOrderNo(),BusiModelEnums.BUSI_MODEL_NONE.getCateCode(),
				assignDto.getOrderType(),TransType.lookupByCode(assignDto.getOrderType()).getSysName(),
				billItemCate,TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode(),TradeCateEnums.TRADE_CATE_ADDUCT.getCateName(),
				ActionType.ACTION_TYPE_IN.getCateCode(),ActionType.ACTION_TYPE_IN.getCateName(),assignDto.getOrderAmount(),
				CommonConstant.STATUS_TRADE_FLOW_NORMAL,SourceFrom.getSourceFromMgn(assignDto.getSource()).getCateName(),
				new BigDecimal("1"),remark);
	}
	/**   
	 * <p>Title: buyingConsumerVouchers</p>   
	 * <p>Description: </p>   
	 * @param assignDto
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IPersonalUserService#buyingConsumerVouchers(com.taolue.baoxiao.common.dto.AssignDto)   
	 */  
	@Override
	public Boolean buyingConsumerVouchers(AssignDto assignDto) {
		log.info("购买券流程开始---------参数：{}",JSON.toJSONString(assignDto));
		//现金账户
		FundBalanceDto balanceDto = acctBalanceBusiService.findSignleBalanceByParams(AcctCateEnums.ACCT_CATE_PTQYX.getCateMgn(), 
				assignDto.getMemberId(),MemberCateEnums.MEMBER_CATE_ALO.getCateCode(), 
				AcctCateEnums.ACCT_CATE_SALARY.getCateCode(), 
				AcctCateEnums.ACCT_CATE_SALARY.getCateCode(), BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
				CommonConstant.STATUS_YES, CommonConstant.STATUS_YES, assignDto.getMemberId(), null, null);
		if(null == balanceDto) {
			log.debug("此会员现金账户无资金流！");
		}
		BigDecimal tradeFlowOrder = CommonConstant.TRADE_FLOW_ORDER_NOMAL;
		BigDecimal tradeFlowOrderAdd = new BigDecimal("1");
		//两条流水          购买券流水         运费流水
		String tradeFlowCode = CommonConstant.KEY_PERFIX_TRADEFLOW_NO+IdWorker.getIdStr();
		List<AssignCouponDto> assignCouponDtos = assignDto.getCoupons();
		if(CollUtil.isEmpty(assignCouponDtos)) {
			log.debug("无购买券记录");
		}
		//购买券流水
		this.creatFundTradeFlowDto(tradeFlowCode, balanceDto.getBalanceCode(), assignDto.getBusiOrderNo(), 
				BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), assignDto.getOrderType(), TransType.lookupByCode(assignDto.getOrderType()).getSysName(), 
				BillItemSubCate.BILL_ITEM_BUYING_COUPON.getCateCode(), TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode(),
				TradeCateEnums.TRADE_CATE_DEDUCT.getCateName(), ActionType.ACTION_TYPE_OUT.getCateCode(), ActionType.ACTION_TYPE_OUT.getCateName(), 
				assignDto.getOrderAmount(), CommonConstant.STATUS_TRADE_FLOW_NORMAL, SourceFrom.getSourceFromMgn(assignDto.getSource()).getCateName(), 
				tradeFlowOrder.add(tradeFlowOrderAdd), "独立C购买券 - 现金账户购买券流水");
		tradeFlowOrder = tradeFlowOrder.add(tradeFlowOrderAdd);
		for(AssignCouponDto assignCouponDto : assignCouponDtos) {
			if(BillItemSubCate.BILL_ITEM_COUPON_FREIGHT.getCateCode().equals(assignCouponDto.getBusiItemCode())) {
				log.info("购买券运费开始---------参数：{}",JSON.toJSONString(assignCouponDto));
				//购买券运费流水
				this.creatFundTradeFlowDto(tradeFlowCode, balanceDto.getBalanceCode(), assignDto.getBusiOrderNo(), 
						BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), assignDto.getOrderType(), TransType.lookupByCode(assignDto.getOrderType()).getSysName(), 
						BillItemSubCate.BILL_ITEM_COUPON_FREIGHT.getCateCode(), TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode(),
						TradeCateEnums.TRADE_CATE_DEDUCT.getCateName(), ActionType.ACTION_TYPE_OUT.getCateCode(), ActionType.ACTION_TYPE_OUT.getCateName(), 

						assignCouponDto.getAmount(), CommonConstant.STATUS_TRADE_FLOW_NORMAL, SourceFrom.getSourceFromMgn(assignDto.getSource()).getCateName(), 
						tradeFlowOrder.add(tradeFlowOrderAdd), "独立C购买券 - 现金账户运费流水");
				tradeFlowOrder = tradeFlowOrder.add(tradeFlowOrderAdd);
			}
			//券账户------------------
			//可转让
			if(BillItemSubCate.BILL_ITEM_SUBCATE_FPED_TRANSFORM.getCateCode().equals(assignCouponDto.getBusiItemCode())) {
				log.info("购买券可转让开始---------参数：{}",JSON.toJSONString(assignCouponDto));
				FundBalanceDto balanceDtoCoupon = acctBalanceBusiService.findSignleBalanceByParams(AcctCateEnums.ACCT_CATE_PTQYX.getCateMgn(), 
						assignDto.getMemberId(),MemberCateEnums.MEMBER_CATE_ALO.getCateCode(), 
						AcctCateEnums.ACCT_CATE_COUPON.getCateCode(), 
						assignCouponDto.getCouponId(), BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
						assignDto.getCanTicket(), CommonConstant.STATUS_YES, assignDto.getMemberId(), null, null);
				TbFundBalance fundBalance = new TbFundBalance();
				//当前可用资金/可用额度
				//balanceDtoCoupon.setBalanceAmount(amount);
				//券权重
				if(CouponWeightEnums.lookupByCode(assignCouponDto.getCouponId()) != null) {
					balanceDtoCoupon.setWeight(new BigDecimal(CouponWeightEnums.lookupByCode(assignCouponDto.getCouponId()).getCateMgn()));
				}
				balanceDtoCoupon.setBalanceAmount(balanceDtoCoupon.getBalanceAmount().add(assignCouponDto.getAmount()));
				BeanUtils.copyProperties(balanceDtoCoupon, fundBalance);
				fundBalance.insertOrUpdate();
				//券账户资金流水
				this.creatFundTradeFlowDto(tradeFlowCode, balanceDtoCoupon.getBalanceCode(), assignDto.getBusiOrderNo(), 
						BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), assignDto.getOrderType(), TransType.lookupByCode(assignDto.getOrderType()).getSysName(), 
						BillItemSubCate.BILL_ITEM_BUYING_COUPON.getCateCode(), TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode(),
						TradeCateEnums.TRADE_CATE_ADDUCT.getCateName(), ActionType.ACTION_TYPE_IN.getCateCode(), ActionType.ACTION_TYPE_IN.getCateName(), 
						assignCouponDto.getAmount(), CommonConstant.STATUS_TRADE_FLOW_NORMAL, SourceFrom.getSourceFromMgn(assignDto.getSource()).getCateName(), 
						tradeFlowOrder.add(tradeFlowOrderAdd), "独立C购买券-可转让券账户入账");
			}
			//不可转让
			if(BillItemSubCate.BILL_ITEM_SUBCATE_FPED_NOTRANSFORM.getCateCode().equals(assignCouponDto.getBusiItemCode())) {
				log.info("购买券不可转让开始---------参数：{}",JSON.toJSONString(assignCouponDto));
				FundBalanceDto balanceDtoCoupon = acctBalanceBusiService.findSignleBalanceByParams(AcctCateEnums.ACCT_CATE_PTQYX.getCateMgn(), 
						assignDto.getMemberId(),MemberCateEnums.MEMBER_CATE_ALO.getCateCode(), 
						AcctCateEnums.ACCT_CATE_COUPON.getCateCode(), 
						assignCouponDto.getCouponId(), BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
						assignDto.getCanTicket(), CommonConstant.STATUS_NO, assignDto.getMemberId(), null, null);
				TbFundBalance fundBalance = new TbFundBalance();
				//当前可用资金/可用额度
				//balanceDtoCoupon.setBalanceAmount(amount);
				//券权重
				if(CouponWeightEnums.lookupByCode(assignCouponDto.getCouponId()) != null) {
					balanceDtoCoupon.setWeight(new BigDecimal(CouponWeightEnums.lookupByCode(assignCouponDto.getCouponId()).getCateMgn()));
				}
				balanceDtoCoupon.setBalanceAmount(balanceDtoCoupon.getBalanceAmount().add(assignCouponDto.getAmount()));
				BeanUtils.copyProperties(balanceDtoCoupon, fundBalance);
				fundBalance.insertOrUpdate();
				//券账户资金流水
				this.creatFundTradeFlowDto(tradeFlowCode, balanceDtoCoupon.getBalanceCode(), assignDto.getBusiOrderNo(), 
						BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), assignDto.getOrderType(), TransType.lookupByCode(assignDto.getOrderType()).getSysName(), 
						BillItemSubCate.BILL_ITEM_BUYING_COUPON.getCateCode(), TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode(),
						TradeCateEnums.TRADE_CATE_ADDUCT.getCateName(), ActionType.ACTION_TYPE_IN.getCateCode(), ActionType.ACTION_TYPE_IN.getCateName(), 
						assignCouponDto.getAmount(), CommonConstant.STATUS_TRADE_FLOW_NORMAL, SourceFrom.getSourceFromMgn(assignDto.getSource()).getCateName(), 
						tradeFlowOrder.add(tradeFlowOrderAdd), "独立C购买券-不可转让券账户入账");
			}
		}
		log.info("购买券结束---------end");
		return true;
	}
	
	/**
	 * 
	 * <p>名称:类IPersonalUserServiceImpl中的creatFundTradeFlowDto方法</br>    
	 * <p>描述:创建资金流水</br> 
	 * <p>作者: yangfan</br> 
	 * <p>日期: 2019年3月25日 下午3:37:18</br>
	 * @throws Exception
	 * @param tradeFlowCode
	 * @param balanceCode
	 * @param tradeBusiCode
	 * @param busiModel
	 * @param tradeBusiCate
	 * @param transBusiCateName
	 * @param billItemCate
	 * @param tradeCate
	 * @param transCateName
	 * @param transActCate
	 * @param transActName
	 * @param tradeAmount
	 * @param status
	 * @param source
	 * @param tradeFlowOrder
	 * @param remark
	 * @return
	 */
	private Boolean creatFundTradeFlowDto(String tradeFlowCode,String balanceCode,String tradeBusiCode,
			String busiModel,String tradeBusiCate,String transBusiCateName,String billItemCate,String tradeCate,
			String transCateName,String transActCate,String transActName,BigDecimal tradeAmount,String status,
			String source,BigDecimal tradeFlowOrder,String remark) {
		FundTradeFlowDto tradeFlowDto = new FundTradeFlowDto();
		//流水编号
		tradeFlowDto.setTradeFlowCode(tradeFlowCode);
		//资金编号
		tradeFlowDto.setBalanceCode(balanceCode);
		//业务模式
		tradeFlowDto.setTradeBusiCode(tradeBusiCode);
		tradeFlowDto.setBusiModel(busiModel);
		tradeFlowDto.setTradeBusiCate(tradeBusiCate);
		tradeFlowDto.setTransBusiCateName(transBusiCateName);
		//交易归属账单科目
		tradeFlowDto.setBillItemCate(billItemCate);
		//操作
		tradeFlowDto.setTradeCate(tradeCate);
		tradeFlowDto.setTransCateName(transCateName);
		//交易流水动作
		tradeFlowDto.setTransActCate(transActCate);
		tradeFlowDto.setTransActName(transActName);
		//充值金额
		tradeFlowDto.setTradeAmount(tradeAmount);
		//状态
		tradeFlowDto.setStatus(status);
		//来源
		tradeFlowDto.setSource(source);
		//设置本次交易过程中，本条流水的顺序号
		tradeFlowDto.setTradeFlowOrder(tradeFlowOrder);
		tradeFlowDto.setRemark(remark);
		TbFundTradeFlow entity = new TbFundTradeFlow();
		BeanUtils.copyProperties(tradeFlowDto, entity);
		return entity.insert();
	}
	/**   
	 * <p>Title: buyingStaffVouchers</p>   
	 * <p>Description: </p>   
	 * @param assignDto
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IPersonalUserService#buyingStaffVouchers(com.taolue.baoxiao.common.dto.AssignDto)   
	 */  
	@Override
	public Boolean buyingStaffVouchers(AssignDto assignDto) {
		log.info("购买券流程开始---------参数：{}",JSON.toJSONString(assignDto));
		//现金账户
		FundBalanceDto balanceDto = acctBalanceBusiService.findSignleBalanceByParams(assignDto.getCompanyId(), 
				assignDto.getMemberId(),MemberCateEnums.MEMBER_CATE_EMP.getCateCode(), 
				AcctCateEnums.ACCT_CATE_SALARY.getCateCode(), 
				AcctCateEnums.ACCT_CATE_SALARY.getCateCode(), BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
				CommonConstant.STATUS_YES, CommonConstant.STATUS_YES, assignDto.getMemberId(), null, null);
		log.info("现金账户资金流查询结果---------：{}",JSON.toJSONString(balanceDto));
		if(null == balanceDto) {
			log.debug("此会员现金账户无资金流！");
		}
		BigDecimal tradeFlowOrder = CommonConstant.TRADE_FLOW_ORDER_NOMAL;
		BigDecimal tradeFlowOrderAdd = new BigDecimal("1");
		//两条流水          购买券流水         运费流水
		String tradeFlowCode = CommonConstant.KEY_PERFIX_TRADEFLOW_NO+IdWorker.getIdStr();
		List<AssignCouponDto> assignCouponDtos = assignDto.getCoupons();
		if(CollUtil.isEmpty(assignCouponDtos)) {
			log.debug("无购买券记录");
		}
		//购买券流水
		this.creatFundTradeFlowDto(tradeFlowCode, balanceDto.getBalanceCode(), assignDto.getBusiOrderNo(), 
				BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), assignDto.getOrderType(), TransType.lookupByCode(assignDto.getOrderType()).getSysName(), 
				BillItemSubCate.BILL_ITEM_BUYING_COUPON.getCateCode(), TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode(),
				TradeCateEnums.TRADE_CATE_DEDUCT.getCateName(), ActionType.ACTION_TYPE_OUT.getCateCode(), ActionType.ACTION_TYPE_OUT.getCateName(), 
				assignDto.getOrderAmount(), CommonConstant.STATUS_TRADE_FLOW_NORMAL, SourceFrom.getSourceFromMgn(assignDto.getSource()).getCateName(), 
				tradeFlowOrder.add(tradeFlowOrderAdd), "员工购买券 - 现金账户购买券流水");
		log.info("购买券流水查询结果---------：{}",JSON.toJSONString(balanceDto));
		tradeFlowOrder = tradeFlowOrder.add(tradeFlowOrderAdd);
		for(AssignCouponDto assignCouponDto : assignCouponDtos) {
			if(BillItemSubCate.BILL_ITEM_COUPON_FREIGHT.getCateCode().equals(assignCouponDto.getBusiItemCode())) {
				log.info("购买券运费开始---------参数：{}",JSON.toJSONString(assignCouponDto));
				//购买券运费流水
				this.creatFundTradeFlowDto(tradeFlowCode, balanceDto.getBalanceCode(), assignDto.getBusiOrderNo(), 
						BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), assignDto.getOrderType(), TransType.lookupByCode(assignDto.getOrderType()).getSysName(), 
						BillItemSubCate.BILL_ITEM_COUPON_FREIGHT.getCateCode(), TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode(),
						TradeCateEnums.TRADE_CATE_DEDUCT.getCateName(), ActionType.ACTION_TYPE_OUT.getCateCode(), ActionType.ACTION_TYPE_OUT.getCateName(), 

						assignCouponDto.getAmount(), CommonConstant.STATUS_TRADE_FLOW_NORMAL, SourceFrom.getSourceFromMgn(assignDto.getSource()).getCateName(), 
						tradeFlowOrder.add(tradeFlowOrderAdd), "员工购买券 - 现金账户运费流水");
				tradeFlowOrder = tradeFlowOrder.add(tradeFlowOrderAdd);
			}
			//券账户------------------
			//可转让
			if(BillItemSubCate.BILL_ITEM_SUBCATE_FPED_TRANSFORM.getCateCode().equals(assignCouponDto.getBusiItemCode())) {
				log.info("购买券可转让开始---------参数：{}",JSON.toJSONString(assignCouponDto));
				FundBalanceDto balanceDtoCoupon = acctBalanceBusiService.findSignleBalanceByParams(assignDto.getCompanyId(), 
						assignDto.getMemberId(),MemberCateEnums.MEMBER_CATE_EMP.getCateCode(), 
						AcctCateEnums.ACCT_CATE_COUPON.getCateCode(), 
						assignCouponDto.getCouponId(), BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
						assignDto.getCanTicket(), CommonConstant.STATUS_YES, assignDto.getMemberId(), null, null);
				TbFundBalance fundBalance = new TbFundBalance();
				//当前可用资金/可用额度
				//balanceDtoCoupon.setBalanceAmount(amount);
				//券权重
				if(CouponWeightEnums.lookupByCode(assignCouponDto.getCouponId()) != null) {
					balanceDtoCoupon.setWeight(new BigDecimal(CouponWeightEnums.lookupByCode(assignCouponDto.getCouponId()).getCateMgn()));
				}
				balanceDtoCoupon.setBalanceAmount(balanceDtoCoupon.getBalanceAmount().add(assignCouponDto.getAmount()));
				BeanUtils.copyProperties(balanceDtoCoupon, fundBalance);
				fundBalance.insertOrUpdate();
				//券账户资金流水
				this.creatFundTradeFlowDto(tradeFlowCode, balanceDtoCoupon.getBalanceCode(), assignDto.getBusiOrderNo(), 
						BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), assignDto.getOrderType(), TransType.lookupByCode(assignDto.getOrderType()).getSysName(), 
						BillItemSubCate.BILL_ITEM_BUYING_COUPON.getCateCode(), TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode(),
						TradeCateEnums.TRADE_CATE_ADDUCT.getCateName(), ActionType.ACTION_TYPE_IN.getCateCode(), ActionType.ACTION_TYPE_IN.getCateName(), 
						assignCouponDto.getAmount(), CommonConstant.STATUS_TRADE_FLOW_NORMAL, SourceFrom.getSourceFromMgn(assignDto.getSource()).getCateName(), 
						tradeFlowOrder.add(tradeFlowOrderAdd), "员工购买券-可转让券账户入账");
			}
			//不可转让
			if(BillItemSubCate.BILL_ITEM_SUBCATE_FPED_NOTRANSFORM.getCateCode().equals(assignCouponDto.getBusiItemCode())) {
				log.info("购买券不可转让开始---------参数：{}",JSON.toJSONString(assignCouponDto));
				FundBalanceDto balanceDtoCoupon = acctBalanceBusiService.findSignleBalanceByParams(assignDto.getCompanyId(), 
						assignDto.getMemberId(),MemberCateEnums.MEMBER_CATE_EMP.getCateCode(), 
						AcctCateEnums.ACCT_CATE_COUPON.getCateCode(), 
						assignCouponDto.getCouponId(), BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
						assignDto.getCanTicket(), CommonConstant.STATUS_NO, assignDto.getMemberId(), null, null);
				TbFundBalance fundBalance = new TbFundBalance();
				//当前可用资金/可用额度
				//balanceDtoCoupon.setBalanceAmount(amount);
				//券权重
				if(CouponWeightEnums.lookupByCode(assignCouponDto.getCouponId()) != null) {
					balanceDtoCoupon.setWeight(new BigDecimal(CouponWeightEnums.lookupByCode(assignCouponDto.getCouponId()).getCateMgn()));
				}
				balanceDtoCoupon.setBalanceAmount(balanceDtoCoupon.getBalanceAmount().add(assignCouponDto.getAmount()));
				BeanUtils.copyProperties(balanceDtoCoupon, fundBalance);
				fundBalance.insertOrUpdate();
				//券账户资金流水
				this.creatFundTradeFlowDto(tradeFlowCode, balanceDtoCoupon.getBalanceCode(), assignDto.getBusiOrderNo(), 
						BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), assignDto.getOrderType(), TransType.lookupByCode(assignDto.getOrderType()).getSysName(), 
						BillItemSubCate.BILL_ITEM_BUYING_COUPON.getCateCode(), TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode(),
						TradeCateEnums.TRADE_CATE_ADDUCT.getCateName(), ActionType.ACTION_TYPE_IN.getCateCode(), ActionType.ACTION_TYPE_IN.getCateName(), 
						assignCouponDto.getAmount(), CommonConstant.STATUS_TRADE_FLOW_NORMAL, SourceFrom.getSourceFromMgn(assignDto.getSource()).getCateName(), 
						tradeFlowOrder.add(tradeFlowOrderAdd), "员工购买券-不可转让券账户入账");
			}
		}
		log.info("购买券结束---------end");
		return true;
	}
	
}
