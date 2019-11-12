package com.taolue.baoxiao.fund.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderBusiStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderDetailStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.SearchParamEnums;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.UserUtils;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.api.dto.OrderDTO;
import com.taolue.baoxiao.fund.api.dto.OrderDetailDTO;
import com.taolue.baoxiao.fund.api.vo.FundAcctVo;
import com.taolue.baoxiao.fund.api.vo.FundBalanceVo;
import com.taolue.baoxiao.fund.api.vo.FundTradeFlowVo;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.mapper.TbFundTradeFlowMapper;
import com.taolue.baoxiao.fund.service.IOrderService;
import com.taolue.baoxiao.fund.service.ITbFundAcctService;
import com.taolue.baoxiao.fund.service.ITbFundBalanceService;
import com.taolue.baoxiao.fund.service.ITbFundTradeFlowService;
import com.taolue.baoxiao.fund.service.composite.IBalanceBusiService;
import com.taolue.member.api.dto.QueryCompanyDto;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @ClassName:  TbFundTradeFlowServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:51:00   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class TbFundTradeFlowServiceImpl extends ServiceImpl<TbFundTradeFlowMapper, TbFundTradeFlow> implements ITbFundTradeFlowService {
	
	@Autowired
    private ITbFundAcctService fundAcctService;
	
	@Autowired
    private ITbFundBalanceService fundBalanceService;
	
	@Autowired
	private IBalanceBusiService balanceBusiService;
	
	@Autowired
	private IOrderService orderService;
	
	@Override
    public TbFundTradeFlow createTrandeFlow(String tradeFlowCode, String tradeBusiCode, BigDecimal tradeFlowOrder,
    		String balanceCode, String tradeCate, String transActCate, String transCateName, String transActName, 
    		BigDecimal tradeAmount, BigDecimal tradePreAmount, BigDecimal tradeLastAmount ) {
    	
    	TbFundTradeFlow tradeFlow = new TbFundTradeFlow();
    	tradeFlow.setTradeFlowCode(tradeFlowCode);
    	tradeFlow.setTradeBusiCode(tradeBusiCode);
    	tradeFlow.setTradeFlowOrder(tradeFlowOrder);
    	tradeFlow.setBalanceCode(balanceCode);
    	tradeFlow.setTradeCate(tradeCate);
    	tradeFlow.setTransActCate(transActCate);
    	tradeFlow.setTransCateName(transCateName);
    	tradeFlow.setTransActName(transActName);
    	tradeFlow.setTradeAmount(tradeAmount);
    	if (null != tradePreAmount) {
    		tradeFlow.setTradePreAmount(tradePreAmount);
    	}
    	if (null != tradeLastAmount) {
    		tradeFlow.setTradeLastAmount(tradeLastAmount);
    	}
    	tradeFlow.setStatus(CommonConstant.STATUS_NORMAL);
    	tradeFlow.setDelFlag(CommonConstant.STATUS_NORMAL);
    	if (this.insert(tradeFlow)) {
    		return tradeFlow;
    	}
    	return null;
    }
    
	@Override
    public  Page<TbFundTradeFlow> findFundTradeFlow(Query query, FundTradeFlowDto fundTradeFlowDto){
    	List<TbFundTradeFlow> newflowList=Lists.newArrayList();
    	List<TbFundTradeFlow> flowList=this.baseMapper.findFundTradeFlow( query,fundTradeFlowDto);
    	for (TbFundTradeFlow tbFundTradeFlow : flowList) {
    		TbFundTradeFlow newtbFundTradeFlow =new TbFundTradeFlow();
    		newtbFundTradeFlow=tbFundTradeFlow;
    		SearchParamEnums searchParam = SearchParamEnums.lookupbyParams(tbFundTradeFlow.getBusiModel(), 
    				tbFundTradeFlow.getStatus(),
    				tbFundTradeFlow.getBillItemCate(), 
    				tbFundTradeFlow.getTradeBusiCate(), 
    				tbFundTradeFlow.getTradeCate());
    		
    		if(!ObjectUtils.isEmpty(searchParam)) {
    			if(StringUtils.isNotBlank(searchParam.getParamName())) {
    		 		newtbFundTradeFlow.setRemark(searchParam.getParamName());
    			}
    		}
   
    		newflowList.add(newtbFundTradeFlow);
		}
    	return  query.setRecords(newflowList);
	  
    }

	@Override
	public R<Boolean> operationIntegral(FundTradeFlowVo flowVo) {
		R<Boolean> r = new R<Boolean>();
		String userName = "";
		log.info("从缓存中获取操作人信息："+JSON.toJSONString(UserUtils.getUserVo()));
		if(UserUtils.getUserVo()!=null && UserUtils.getUserVo().getRealName() != null){
			userName=UserUtils.getUserVo().getRealName();
	    }else {
	    	  userName= UserUtils.getUser();
	    }
		if (userName == null) {
			userName = "admin";
		}
		log.info("操作人姓名："+JSON.toJSONString(userName));
		//根据企业ID查询企业积分账户
		FundAcctVo vo = new FundAcctVo();
		vo.setMemberId(flowVo.getCompanyId());
		vo.setMemberName(flowVo.getCompanyName());
		vo.setAcctCate(DictionaryEnum.AcctCateEnums.ACCT_CATE_COMPANY_JF_ACCOUNT.getCateCode());
		FundAcctVo accdyq = new FundAcctVo();
		FundAcctVo accVojf = balanceBusiService.findCompanyIntegralAcc(vo);
		log.info("查询企业积分账号返回结果："+JSON.toJSONString(accVojf));
		//根据企业ID查询企业抵用券账户
		vo.setAcctCate(DictionaryEnum.AcctCateEnums.ACCT_CATE_COMPANY_DYQ_ACCOUNT.getCateCode());
		FundAcctVo accVodyq = balanceBusiService.findCompanyIntegralAcc(vo);
		log.info("查询企业抵用券账号返回结果："+JSON.toJSONString(accVodyq));
		
		if(!DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateCode().equals(flowVo.getTradeBusiCate())){
			if(ObjectUtils.isEmpty(accVojf)) {
				FundAcctVo vojf = new FundAcctVo();
				vojf.setMemberId(flowVo.getCompanyId());
				vojf.setMemberName(flowVo.getCompanyName());
				vojf.setMemberCate(MemberCateEnums.MEMBER_CATE_CMP.getCateCode());
				vojf.setAcctCate(DictionaryEnum.AcctCateEnums.ACCT_CATE_COMPANY_JF_ACCOUNT.getCateCode());
				log.info("创建企业积分账号传入参数："+JSON.toJSONString(vojf));
				R<FundAcctVo> rjf = fundAcctService.AddCompanyAcc(vojf);
				log.info("创建企业积分账号返回结果："+JSON.toJSONString(rjf));
				if(R.SUCCESS != rjf.getCode()) {
					r.setCode(R.FAIL);
					r.setData(false);
					r.setMsg("创建企业积分账户失败");
					return r;
				}
				BeanUtils.copyProperties(rjf.getData(), accdyq);
			}else {
				BeanUtils.copyProperties(accVojf, accdyq);
			}
			//如果没有 先创建企业积分账户和抵用券账户然后再插入tb_fund_trade_flow表操作记录
			if(ObjectUtils.isEmpty(accVodyq)) {
				FundAcctVo vodyq = new FundAcctVo();
				vodyq.setMemberId(flowVo.getCompanyId());
				vodyq.setMemberName(flowVo.getCompanyName());
				vodyq.setMemberCate(MemberCateEnums.MEMBER_CATE_CMP.getCateCode());
				vodyq.setAcctCate(DictionaryEnum.AcctCateEnums.ACCT_CATE_COMPANY_DYQ_ACCOUNT.getCateCode());
				log.info("创建企业抵用券账号传入参数："+JSON.toJSONString(vodyq));
				R<FundAcctVo> rdyq = fundAcctService.AddCompanyAcc(vodyq);
				log.info("创建企业抵用券账号传入参数："+JSON.toJSONString(rdyq));
				if(R.SUCCESS != rdyq.getCode()) {
					r.setCode(R.FAIL);
					r.setData(false);
					r.setMsg("创建企业抵用券账户失败");
					return r;
				}
			}
		}
		
		if(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateCode().equals(flowVo.getTradeBusiCate())){
			if(ObjectUtils.isEmpty(accVojf)) {
				r.setCode(R.FAIL);
				r.setData(false);
				r.setMsg("企业积分账户不存在");
				return r;
			}
			BeanUtils.copyProperties(accVojf, accdyq);
			if(ObjectUtils.isEmpty(accVodyq)) {
				r.setCode(R.FAIL);
				r.setData(false);
				r.setMsg("企业抵用券账户不存在");
				return r;
			}
		}
		log.info("<<<<<<<<判断企业账号逻辑结束>>>>>>>"+JSON.toJSONString(accdyq));
		//1.创建订单 2.创建balance 3.创建trade_flow
		flowVo.setName(accdyq.getName());
		flowVo.setAcctInstNo(accdyq.getAcctInstNo());
		//批次号：
		String batchNo = CodeUtils.genneratorShort("JF");
		Calendar date=Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY,-24);
        date.add(Calendar.YEAR, 1);
        date.set(Calendar.HOUR_OF_DAY, 23);
        date.set(Calendar.MINUTE, 59);
        date.set(Calendar.SECOND, 59);
        Date yearDate = date.getTime();
		String orderNo = "";
		if(DictionaryEnum.OrderType.COMPANY_VOUCHER_DEDUCT.getCateCode().equals(flowVo.getTradeBusiCate())) {
			orderNo = CodeUtils.genneratorShort("JFKJ");
		}
		QueryCompanyDto querydto = new QueryCompanyDto();
		querydto.setMemberId(flowVo.getCompanyId());
		querydto.setMemberName(flowVo.getCompanyName());
		querydto.setAcctCate(DictionaryEnum.AcctCateEnums.ACCT_CATE_COMPANY_JF_ACCOUNT.getCateCode());
		FundBalanceVo fundVo = new FundBalanceVo();
		fundVo.setMemberId(flowVo.getCompanyId());
		List<FundBalanceVo> fundList = balanceBusiService.findCompanyBalance(fundVo);
		log.info("查询企业积分余额返回结果>>>>>>>"+JSON.toJSONString(fundList));
		List<FundBalanceVo> fundBalanceList = balanceBusiService.findIntegralDcByMemberId(querydto);
		log.info("查询企业积分List返回结果>>>>>>>"+JSON.toJSONString(fundList));
		if(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateCode().equals(flowVo.getTradeBusiCate())) {
			if(null == fundList || fundList.size() <= 0) {
				r.setCode(R.FAIL);
				r.setData(false);
				r.setMsg("企业积分不足");
				return r;
			}else if(flowVo.getTradeAmount().compareTo(fundList.get(0).getBalanceAmount()) > 0){
				r.setCode(R.FAIL);
				r.setData(false);
				r.setMsg("企业积分不足");
				return r;
			}
		}
		if(DictionaryEnum.OrderType.COMPANY_VOUCHER_DEDUCT.getCateCode().equals(flowVo.getTradeBusiCate())) {
			if(null == fundBalanceList || fundBalanceList.size() <= 0) {
				log.info("开始进行企业积分扣减数据不存在时添加积分扣减数据记录>>>>>>>");
				TbFundBalance balanceVo = new TbFundBalance();
				balanceVo.setAcctInstNo(flowVo.getAcctInstNo());
				balanceVo.setBalanceCode(batchNo);
				balanceVo.setCompanyId(flowVo.getCompanyId());
				balanceVo.setCompanyName(flowVo.getCompanyName());
				balanceVo.setBusiModel(DictionaryEnum.SearchParamEnums.PARAM_CM10006_YSWC.getBusiModel());
				balanceVo.setBalanceItemCode(flowVo.getBillItemCate());
				balanceVo.setBalanceAmount(new BigDecimal(0).subtract(flowVo.getTradeAmount()));
				balanceVo.setValidTime(new Date());
				balanceVo.setExpireTime(yearDate);
				balanceVo.setCreator(userName);
				balanceVo.setAuthBalance(new BigDecimal(0).subtract(flowVo.getTradeAmount()));
				balanceVo.setExtendAttre(CodeUtils.genneratorShort("SJ"));
				balanceVo.setRemark(flowVo.getRemark());
				fundBalanceService.insert(balanceVo);
				TbFundTradeFlow tradeFlowvo = new TbFundTradeFlow();
				tradeFlowvo.setTradeBusiCate(DictionaryEnum.OrderType.COMPANY_VOUCHER_DEDUCT.getCateCode());
				tradeFlowvo.setTransBusiCateName(DictionaryEnum.OrderType.COMPANY_VOUCHER_DEDUCT.getCateName());
				tradeFlowvo.setTradeAmount(flowVo.getTradeAmount());
				tradeFlowvo.setTradePreAmount(new BigDecimal(0));
				tradeFlowvo.setTradeLastAmount(new BigDecimal(0).subtract(flowVo.getTradeAmount()));
				tradeFlowvo.setTradeFlowCode(orderNo);
				tradeFlowvo.setBusiModel(DictionaryEnum.SearchParamEnums.PARAM_CM10006_YSWC.getBusiModel());
				tradeFlowvo.setTradeBusiCode(CodeUtils.genneratorShort("JL"));
				tradeFlowvo.setBalanceCode(batchNo);
				tradeFlowvo.setCreator(userName);
				tradeFlowvo.setRemark(flowVo.getRemark());
				tradeFlowvo.setBillItemCate(BillItemSubCate.BILL_ITEM_SUBCATE_FPED_TRANSFORM.getCateCode());
				tradeFlowvo.setTradeCate(DictionaryEnum.TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode());
				tradeFlowvo.setTransActName(DictionaryEnum.ActionType.ACTION_TYPE_OUT.getCateName());
				tradeFlowvo.setTransCateName(DictionaryEnum.TradeCateEnums.TRADE_CATE_DEDUCT.getCateName());
				tradeFlowvo.setTransActCate(DictionaryEnum.ActionType.ACTION_TYPE_OUT.getCateCode());
				this.insert(tradeFlowvo);
				log.info("进行企业积分数据不存在是积分扣减数据记录结束>>>>>>>");
			}
		}
		
		log.info("<<<<<<<<开始进行积分数据操作>>>>>>>");
		//当发放积分时需要1.创建订单
		if(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateCode().equals(flowVo.getTradeBusiCate()) || DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateCode().equals(flowVo.getTradeBusiCate())) {
			log.info("<<<<<<<<开始进行积分发放和消费需对order表进行数据操作>>>>>>>");
			OrderDTO order = new OrderDTO();
			List<OrderDetailDTO> detailDtoList = Lists.newArrayList();
			OrderDetailDTO detail = new OrderDetailDTO();
			order.setCompanyId(flowVo.getCompanyId());
			order.setCompanyName(flowVo.getCompanyName());
			order.setOrderCode(batchNo);
			if(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateCode().equals(flowVo.getTradeBusiCate())) {
				orderNo = CodeUtils.genneratorShort("JFAD");
				order.setOrderNo(orderNo);
				detail.setRemark(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateMgn());
				order.setMainType(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateCode());
				order.setSubType(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateCode());
				order.setMainTypeName(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateName());
				order.setSubTypeName(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateName());
				detail.setPaymentIndustryId(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateCode()); // 消费场景编码，见枚举  SceneCodeStatus
				detail.setPaymentIndustryName(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateName()); // 消费场景编码，见枚举 SceneCodeStatus
			}
			if(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateCode().equals(flowVo.getTradeBusiCate())) {
				orderNo = CodeUtils.genneratorShort("JFXF");
				order.setOrderNo(orderNo);
				detail.setRemark(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateMgn());
				order.setMainType(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateCode());
				order.setSubType(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateCode());
				order.setMainTypeName(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateName());
				order.setSubTypeName(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateName());
				detail.setPaymentIndustryId(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateCode()); // 消费场景编码，见枚举  SceneCodeStatus
				detail.setPaymentIndustryName(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateName()); // 消费场景编码，见枚举 SceneCodeStatus
			}
			order.setOrderAmount(flowVo.getTradeAmount());
			order.setPayAmount(flowVo.getTradeAmount());
			order.setMemberId(flowVo.getCompanyId());
			order.setSourceType(CommonConstant.STATUS_DEL); //区分区分：0.智惠嘉  1.总部过来的订单
			order.setExpireTime(yearDate);
			order.setRemark(flowVo.getRemark());
			//订单状态为已完成状态
			order.setStatus(OrderBusiStatus.COMPLETED.getCateCode());
			detail.setOrderNo(orderNo);
			detail.setPaymentCate(flowVo.getBillItemCate());
			detail.setBusiModle(DictionaryEnum.SearchParamEnums.PARAM_CM10006_YSWC.getBusiModel());
			detail.setPaymentMemberCate(flowVo.getBillItemCate()); //支付对象类型
			detail.setPaymentAcctCate(AcctCateEnums.ACCT_CATE_COMPANY_JF_ACCOUNT.getCateCode()); //支付账户类型
			detail.setPaymentItemNo(orderNo);//总部支付订单号
			detail.setPaymentItemName(null);
			detail.setPaymentMemberId(flowVo.getCompanyId());
			detail.setStatus(OrderDetailStatus.SUCCESS.getCateCode()); //订单成功状态的
			detail.setPaymentAmount(flowVo.getTradeAmount());
			detailDtoList.add(detail);
			order.setDetailDtoList(detailDtoList);
			log.info("添加企业积分发放或消费订单的入参参数:{}",JSON.toJSONString(order));
			boolean isFlag = orderService.addOrder(order);
			log.info("添加企业积分发放或消费订单返回结果:{}",JSON.toJSONString(isFlag));
		}
		
		//2.创建balance 3.创建trade_flow
		if(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateCode().equals(flowVo.getTradeBusiCate())) {
			log.info("<<<<<<<<开始进行积分发放对balance表进行数据操作>>>>>>>");
			TbFundBalance balanceVo = new TbFundBalance();
			balanceVo.setAcctInstNo(flowVo.getAcctInstNo());
			balanceVo.setBalanceCode(batchNo);
			balanceVo.setCompanyId(flowVo.getCompanyId());
			balanceVo.setCompanyName(flowVo.getCompanyName());
			balanceVo.setBusiModel(DictionaryEnum.SearchParamEnums.PARAM_CM10006_YSWC.getBusiModel());
			balanceVo.setBalanceItemCode(flowVo.getBillItemCate());
			balanceVo.setBalanceAmount(flowVo.getTradeAmount());
			balanceVo.setValidTime(new Date());
			balanceVo.setExpireTime(yearDate);
			balanceVo.setCreator(userName);
			balanceVo.setAuthBalance(flowVo.getTradeAmount());
			balanceVo.setExtendAttre(CodeUtils.genneratorShort("SJ"));
			balanceVo.setRemark(flowVo.getRemark());
			fundBalanceService.insert(balanceVo);
			log.info("<<<<<<<<开始进行积分发放对TbFundTradeFlow表进行数据操作>>>>>>>");
			TbFundTradeFlow tradeFlowvo = new TbFundTradeFlow();
			tradeFlowvo.setTradeBusiCate(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateCode());
			tradeFlowvo.setTransBusiCateName(DictionaryEnum.OrderType.COMPANY_VOUCHER_ADD.getCateName());
			tradeFlowvo.setTradeAmount(flowVo.getTradeAmount());
			tradeFlowvo.setTradePreAmount(new BigDecimal(0));
			tradeFlowvo.setTradeLastAmount(flowVo.getTradeAmount());
			tradeFlowvo.setTradeFlowCode(orderNo);
			tradeFlowvo.setBusiModel(DictionaryEnum.SearchParamEnums.PARAM_CM10006_YSWC.getBusiModel());
			tradeFlowvo.setTradeBusiCode(CodeUtils.genneratorShort("JL"));
			tradeFlowvo.setBalanceCode(batchNo);
			tradeFlowvo.setCreator(userName);
			tradeFlowvo.setRemark(flowVo.getRemark());
			tradeFlowvo.setBillItemCate(BillItemSubCate.BILL_ITEM_SUBCATE_FPED_TRANSFORM.getCateCode());
			tradeFlowvo.setTradeCate(DictionaryEnum.TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode());
			tradeFlowvo.setTransActName(DictionaryEnum.ActionType.ACTION_TYPE_IN.getCateName());
			tradeFlowvo.setTransCateName(DictionaryEnum.TradeCateEnums.TRADE_CATE_ADDUCT.getCateName());
			tradeFlowvo.setTransActCate(DictionaryEnum.ActionType.ACTION_TYPE_IN.getCateCode());
			this.insert(tradeFlowvo);
		}else {
			//修改balance
			//消费和扣减
			log.info("<<<<<<<<开始进行积分消费和扣减对balanceVo和TbFundTradeFlow表进行数据操作>>>>>>>");
			String balanceVoid="";
			String tradeFlowvoid="";
			for(FundBalanceVo balanceVo : fundBalanceList) {
				if(flowVo.getTradeAmount().compareTo(new BigDecimal(0)) >0){
					balanceVoid= balanceVo.getId();
					TbFundTradeFlow tradeFlowvo = new TbFundTradeFlow();
					tradeFlowvo.setBalanceCode(balanceVo.getBalanceCode());
					tradeFlowvo.setCreator(userName);
					tradeFlowvo.setTradeFlowCode(orderNo);
					tradeFlowvo.setBusiModel(DictionaryEnum.SearchParamEnums.PARAM_CM10006_YSWC.getBusiModel());
					tradeFlowvo.setTradeBusiCode(CodeUtils.genneratorShort("JL"));
					tradeFlowvo.setBillItemCate(BillItemSubCate.BILL_ITEM_SUBCATE_FPED_TRANSFORM.getCateCode());
					tradeFlowvo.setTradeCate(DictionaryEnum.TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode());
					tradeFlowvo.setTransActName(DictionaryEnum.ActionType.ACTION_TYPE_OUT.getCateName());
					tradeFlowvo.setTransCateName(DictionaryEnum.TradeCateEnums.TRADE_CATE_DEDUCT.getCateName());
					tradeFlowvo.setTransActCate(DictionaryEnum.ActionType.ACTION_TYPE_OUT.getCateCode());
					if(DictionaryEnum.OrderType.COMPANY_VOUCHER_DEDUCT.getCateCode().equals(flowVo.getTradeBusiCate())) {
						tradeFlowvo.setRemark(flowVo.getRemark());
						tradeFlowvo.setTradeBusiCate(DictionaryEnum.OrderType.COMPANY_VOUCHER_DEDUCT.getCateCode());
						tradeFlowvo.setTransBusiCateName(DictionaryEnum.OrderType.COMPANY_VOUCHER_DEDUCT.getCateName());
					}else {
						tradeFlowvo.setRemark(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateMgn());
						tradeFlowvo.setTradeBusiCate(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateCode());
						tradeFlowvo.setTransBusiCateName(DictionaryEnum.OrderType.COMPANY_VOUCHER_CONSUME.getCateName());
					}
					if(flowVo.getTradeAmount().compareTo(balanceVo.getBalanceAmount()) >0) {
						tradeFlowvo.setTradeAmount(balanceVo.getBalanceAmount());
						tradeFlowvo.setTradePreAmount(balanceVo.getBalanceAmount());
						tradeFlowvo.setTradeLastAmount(flowVo.getTradeAmount().subtract(balanceVo.getBalanceAmount()));
						flowVo.setTradeAmount(flowVo.getTradeAmount().subtract(balanceVo.getBalanceAmount()));
						balanceVo.setBalanceAmount(new BigDecimal(0));
					}else {
						tradeFlowvo.setTradeAmount(flowVo.getTradeAmount());
						tradeFlowvo.setTradePreAmount(balanceVo.getBalanceAmount());
						tradeFlowvo.setTradeLastAmount(balanceVo.getBalanceAmount().subtract(flowVo.getTradeAmount()));
						balanceVo.setBalanceAmount(balanceVo.getBalanceAmount().subtract(flowVo.getTradeAmount()));
						flowVo.setTradeAmount(new BigDecimal(0));
					}
					TbFundBalance tbbalance = new TbFundBalance();
					BeanUtils.copyProperties(balanceVo, tbbalance);
					fundBalanceService.updateById(tbbalance);
					this.baseMapper.insert(tradeFlowvo);
					tradeFlowvoid= tradeFlowvo.getId();
					balanceVoid=balanceVo.getId();
					tradeFlowvoid=tradeFlowvo.getId();
				}
			}
			if(flowVo.getTradeAmount().compareTo(new BigDecimal(0)) >0){
				log.info("<<<<<<<<开始进行积分扣减比该企业总积分高情况对TbFundBalance和TbFundTradeFlow表进行数据操作>>>>>>>");
				log.info("<<<<<<<<tradeFlowvoid>>>>>>>"+JSON.toJSONString(tradeFlowvoid));
				log.info("<<<<<<<<balanceVoid>>>>>>>"+JSON.toJSONString(balanceVoid));
				if(StringUtils.isNotBlank(balanceVoid)){
					if(StringUtils.isNotBlank(tradeFlowvoid)) {
						TbFundTradeFlow tradeFlow = this.selectById(tradeFlowvoid);
					    tradeFlow.setTradeLastAmount(new BigDecimal(0).subtract(flowVo.getTradeAmount()));
					    tradeFlow.setTradeAmount(tradeFlow.getTradePreAmount().add(flowVo.getTradeAmount()));
					    this.updateById(tradeFlow);
					}
					TbFundBalance balance = fundBalanceService.selectById(balanceVoid);
					balance.setBalanceAmount(new BigDecimal(0).subtract(flowVo.getTradeAmount()));
					fundBalanceService.updateById(balance);
				}
			}
		}
		log.info("<<<<<<<<积分数据操作结束结果>>>>>>>"+JSON.toJSONString(r));
		return r;
	}
}
