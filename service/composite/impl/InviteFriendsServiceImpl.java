/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  InviteFriendsServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: DELL  
 * @date:   2019年4月1日 下午4:19:00   
 * @version V1.0 
 * @Copyright: 2019 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyRoleType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.common.util.Exceptions;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.FundAcctDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.entity.BusinessApplyParty;
import com.taolue.baoxiao.fund.entity.Order;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.mapper.TbFundTradeFlowMapper;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.IBusinessApplyBusiService;
import com.taolue.baoxiao.fund.service.composite.InviteFriendsService;

import cn.hutool.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;


/**   
 * <p>ClassName:  InviteFriendsServiceImpl </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: DELL</br>
 * <p>date:   2019年4月1日 下午4:19:00 </br>  
 *     
 * @Copyright: 2019 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Slf4j
@Service
public class InviteFriendsServiceImpl implements InviteFriendsService {

	@Autowired
	private IBusinessApplyBusiService businessApplyBusiService;
	
	@Autowired
	private IAcctBalanceBusiService acctBalanceBusiService;
	
	@Autowired
	private TbFundTradeFlowMapper fundTradeFlowMapper;
	/**   
	 * <p>Title: returnCalculation</p>   
	 * <p>Description: </p>   
	 * @param assignDto
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.InviteFriendsService#returnCalculation(com.taolue.baoxiao.common.dto.AssignDto)   
	 */
	@Transactional(rollbackFor=Exception.class)
	@Override
	public void returnCalculation(R<AssignDto> r) {
		AssignDto assignDto =  r.getData();
		String[] vendorNo = assignDto.getVendorNo().split("_");
		//订单前缀
		String orderNoPefix = "IF";
		String orderNo = assignDto.getBusiOrderNo() + vendorNo[0] + vendorNo[1];
		//创建单据
		try {
			Order order =  businessApplyBusiService.createApplyMain(orderNoPefix, CommonConstant.STRING_BLANK, OrderType.TRADE,
					OrderType.ORDER_TYPE_INVITE_FRIEND, BusinessApplyStatus.CREATED, AcctCateEnums.ACCT_CATE_PTQYX.getCateMgn(), assignDto.getSource());
			order.setOrderNo(assignDto.getBusiOrderNo());
			log.info("主订单开始----参数：{}",JSON.toJSONString(order.getOrderNo()));
			log.info("主订单开始----参数：{}",JSON.toJSONString(assignDto.getBusiOrderNo()));
			order.setOrderNo(orderNo);
			order.setOrderAmount(assignDto.getCouponAmount());
			log.info("主订单开始----参数：{}",JSON.toJSONString(order));
			order.insert();
			//参与人关联单据
			String applyPartyCodePerfix = "IFT";
			//交易人
			BusinessApplyParty applyPartyPay = businessApplyBusiService.createApplyParty(applyPartyCodePerfix, order.getOrderNo(), 
					BusinessApplyRoleType.INVITE_FRIEND_TRANSACTION, vendorNo[1], BusinessApplyStatus.CREATED, CommonConstant.STRING_BLANK);
			log.info("交易人订单开始----参数：{}",JSON.toJSONString(applyPartyPay));
			applyPartyPay.insert();
			//返现人
			BusinessApplyParty applyPartyReturn = businessApplyBusiService.createApplyParty(applyPartyCodePerfix, order.getOrderNo(), 
					BusinessApplyRoleType.INVITE_FRIEND_CASHBACK, assignDto.getMemberId(), BusinessApplyStatus.CREATED, assignDto.getGeid());
			log.info("返现人订单开始----参数：{}",JSON.toJSONString(applyPartyReturn));
			applyPartyReturn.insert();
			//返现企业
			String companyId = assignDto.getCompanyId();
			String cGeId = assignDto.getGuid();
			if(StringUtils.isBlank(companyId)) {
				companyId = AcctCateEnums.ACCT_CATE_PTQYX.getCateMgn();
				cGeId = CommonConstant.STRING_BLANK;
				assignDto.setCompanyId(companyId);
				assignDto.setGuid(cGeId);
			}
			BusinessApplyParty applyPartyCompany = businessApplyBusiService.createApplyParty(applyPartyCodePerfix, order.getOrderNo(), 
					BusinessApplyRoleType.INVITE_FRIEND_CASHBACK_BUSINESS, companyId , BusinessApplyStatus.CREATED, cGeId);
			log.info("返现企业订单开始----参数：{}",JSON.toJSONString(applyPartyCompany));
			applyPartyCompany.insert();
			assignDto.setAcceptBusiOrderNo(orderNo);
		} catch (Exception e) {
			r.setCode(HttpStatus.HTTP_INTERNAL_ERROR);
			r.setMsg("邀请好友返现订单生成失败！");
			FundServiceExceptionGenerator.FundServiceException(e);
		}
	}
	/**   
	 * <p>Title: recordFundBalance</p>   
	 * <p>Description: 邀请好友返现记账</p>   
	 * @param r   
	 * @see com.taolue.baoxiao.fund.service.composite.InviteFriendsService#recordFundBalance(com.taolue.baoxiao.common.util.R)   
	 */  
	@Transactional(rollbackFor=Exception.class)
	@Override
	public void recordFundBalance(AssignDto assignDto) {
		if(CommonConstant.STATUS_NO.equals(assignDto.getStatus())) {
			log.info("订单更改状态：{}",assignDto.getStatus());
			return;
		}
		/*
		 *为了去重先生成流水
		 *返现公司 	
		 */
		//返现公司ID
		String companyId = assignDto.getCompanyId();
		
		FundBalanceDto fundBalanceDtoCompany = acctBalanceBusiService.findSignleBalanceByParams(companyId, 
				companyId, MemberCateEnums.MEMBER_CATE_CMP.getCateCode(), 
				AcctCateEnums.ACCT_CATE_SALARY.getCateCode(), AcctCateEnums.ACCT_CATE_SALARY.getCateCode(), BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
				CommonConstant.STATUS_YES, CommonConstant.STATUS_YES, companyId, null, null);
		fundBalanceDtoCompany.setBalanceAmount(fundBalanceDtoCompany.getBalanceAmount().subtract(assignDto.getCouponAmount()));
		TbFundBalance fundBalanceCompany = new TbFundBalance();
		BeanUtils.copyProperties(fundBalanceDtoCompany, fundBalanceCompany);
		fundBalanceCompany.updateById();
		
		BigDecimal tradeFlowOrder = CommonConstant.TRADE_FLOW_ORDER_NOMAL;
		BigDecimal tradeFlowOrderAdd = new BigDecimal("1");
		String tradeFlowCode = CommonConstant.KEY_PERFIX_TRADEFLOW_NO + IdWorker.getIdStr();
		this.creatFundTradeFlowDto(tradeFlowCode, fundBalanceDtoCompany.getBalanceCode(), assignDto.getBusiOrderNo(), BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
				OrderType.ORDER_TYPE_INVITE_FRIEND.getCateCode(), OrderType.ORDER_TYPE_INVITE_FRIEND.getCateName(),
				BillItemSubCate.BILL_ITEM_INVITE_FRIEND.getCateCode(), TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode(), TradeCateEnums.TRADE_CATE_DEDUCT.getCateName(), 
				ActionType.ACTION_TYPE_OUT.getCateCode(), ActionType.ACTION_TYPE_OUT.getCateName(), 
				assignDto.getCouponAmount(), CommonConstant.STATUS_TRADE_FLOW_NORMAL, assignDto.getSource(), tradeFlowOrder.add(tradeFlowOrderAdd), "企业现金账户-邀请好友返现出账");
		tradeFlowOrder = tradeFlowOrder.add(tradeFlowOrderAdd);
		log.info("返现企业出账开始----balance参数：{} ------ tradeflowID：{}",JSON.toJSONString(fundBalanceDtoCompany),JSON.toJSONString(tradeFlowCode));
		//返现人入账
		String memberId = assignDto.getMemberId();
		FundBalanceDto fundBalanceDto = acctBalanceBusiService.findSignleBalanceByParams(companyId, 
				memberId, MemberCateEnums.MEMBER_CATE_ALO.getCateCode(), 
				AcctCateEnums.ACCT_CATE_SALARY.getCateCode(), AcctCateEnums.ACCT_CATE_SALARY.getCateCode(), BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
				CommonConstant.STATUS_YES, CommonConstant.STATUS_YES, memberId, null, null);
		fundBalanceDto.setBalanceAmount(fundBalanceDtoCompany.getBalanceAmount().add(assignDto.getCouponAmount()));
		TbFundBalance fundBalance = new TbFundBalance();
		BeanUtils.copyProperties(fundBalanceDto, fundBalance);
		fundBalanceCompany.updateById();
		
		this.creatFundTradeFlowDto(tradeFlowCode, fundBalanceDto.getBalanceCode(), assignDto.getBusiOrderNo(), BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
				OrderType.ORDER_TYPE_INVITE_FRIEND.getCateCode(), OrderType.ORDER_TYPE_INVITE_FRIEND.getCateName(),
				BillItemSubCate.BILL_ITEM_INVITE_FRIEND.getCateCode(), TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode(), TradeCateEnums.TRADE_CATE_ADDUCT.getCateName(), 
				ActionType.ACTION_TYPE_IN.getCateCode(), ActionType.ACTION_TYPE_IN.getCateName(), 
				assignDto.getCouponAmount(), CommonConstant.STATUS_TRADE_FLOW_NORMAL, assignDto.getSource(), tradeFlowOrder.add(tradeFlowOrderAdd), "个人现金账户-邀请好友返现进账");
		log.info("返现人入账开始----balance参数：{} ------ tradeflowID：{}",JSON.toJSONString(fundBalanceDto),JSON.toJSONString(tradeFlowCode));
	}
	
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
	 * <p>Title: judgeTheFirstOrder</p>   
	 * <p>Description: </p>   
	 * @param memberId
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.InviteFriendsService#judgeTheFirstOrder(java.lang.String)   
	 */  
	@Override
	public TbFundTradeFlow judgeTheFirstOrder(String memberId) {
		return fundTradeFlowMapper.judgeTheFirstOrder(memberId);
	}
	
}
