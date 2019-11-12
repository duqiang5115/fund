/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  BlanceServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   2018年8月28日 上午10:48:55   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.enums.SqlLike;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BalanceStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.voucherGrantWay;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.DateToolUtils;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.coupon.RefactorCouponMemberTmpService;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.vo.CompanyBuyVoucherVo;
import com.taolue.baoxiao.fund.api.vo.FundVoucherBalanceVo;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.entity.TbFundAcct;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.mapper.BalanceBusiMapper;
import com.taolue.baoxiao.fund.service.ITbFundAcctService;
import com.taolue.baoxiao.fund.service.ITbFundBalanceService;
import com.taolue.baoxiao.fund.service.ITbFundTradeFlowService;
import com.taolue.baoxiao.fund.service.composite.IAccountService;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.IBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.IBlanceService;
import com.taolue.coupon.api.dto.CouponMemberTmpDto;
import com.taolue.coupon.api.vo.CouponMemberTmpVo;
import com.xiaoleilu.hutool.bean.BeanUtil;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**   
 * @ClassName:  BlanceServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月28日 上午10:48:55   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class BlanceServiceImpl implements IBlanceService {
    
    @Autowired
    private ITbFundBalanceService fundBalanceService;
    
    @Autowired
    private ITbFundAcctService fundAcctService;
    
	@Autowired
	private IAcctBalanceBusiService  acctBalanceBusiService;
	
    @Autowired
    private ITbFundTradeFlowService fundTradeFlowService;
    
	@Autowired
	private IBalanceBusiService balanceBusiService;
	
	
	
	@Autowired
	private BalanceBusiMapper balanceBusiMapper;
	
	@Autowired
	private RefactorCouponMemberTmpService refactorCouponMemberTmpService;
	
	@Autowired
	private IAccountService accountService;
	

	private List<TbFundBalance> findFundBalances(String acctInstNo, String balanceItemNo) {
    	EntityWrapper<TbFundBalance> acctWrapper = new EntityWrapper<>();
    	acctWrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
    	acctWrapper.eq("acct_inst_no", acctInstNo);
    
    	if (StrUtil.isNotBlank(balanceItemNo)) {
    		acctWrapper.eq("balance_item_code", balanceItemNo);
    	}
    	return this.fundBalanceService.selectList(acctWrapper);
    }
	
	public TbFundBalance createFundBalance(String acctInstNo, String balanceItemCode, BigDecimal balanceAmount, 
    		BigDecimal freezingAmount, BigDecimal ticketAmount) {
    	TbFundBalance balance = new TbFundBalance();
		balance.setAcctInstNo(acctInstNo);
		balance.setBalanceItemCode(balanceItemCode);
		if (CommonConstant.BALANCE_ITEM_NO_FULL_YCBZJ.equals(balanceItemCode)) {
			balance.setBusiModel(BusiModelEnums.BUSI_MODEL_YCCM.getCateCode());
		} else if (CommonConstant.BALANCE_ITEM_NO_FULL_YCGBX.equals(balanceItemCode)) {
			balance.setBusiModel(BusiModelEnums.BUSI_MODEL_YCPE.getCateCode());
		} else if (CommonConstant.BALANCE_ITEM_NO_FULL_HFBZJ.equals(balanceItemCode)) {
			balance.setBusiModel(BusiModelEnums.BUSI_MODEL_HFCM.getCateCode());
		} else if (CommonConstant.BALANCE_ITEM_NO_FULL_HFGBX.equals(balanceItemCode)) {
			balance.setBusiModel(BusiModelEnums.BUSI_MODEL_HFPE.getCateCode());
		} else if (balanceItemCode.indexOf(AcctCateEnums.ACCT_CATE_COUPON.getCateCode())<0
				|| balanceItemCode.indexOf(AcctCateEnums.ACCT_CATE_MASTER.getCateCode())<0) {
			balance.setBusiModel(CommonConstant.BALANCE_BUSI_MODEL_NONE);
		}
		
		balance.setBalanceCode(CommonConstant.KEY_PERFIX_BALANCE_NO+IdWorker.getIdStr());
		balance.setBalanceAmount(balanceAmount);
		if (null != freezingAmount) {
			balance.setFreezingBalance(freezingAmount);
		} else {
			balance.setFreezingBalance(CommonConstant.NO_AMOUNT);
		}
		if (null != ticketAmount) {
			balance.setTicketBalance(ticketAmount);
		} else {
			balance.setTicketBalance(CommonConstant.NO_AMOUNT);
		}
		
		balance.setValidTime(CommonConstant.DEFALUT_VALIDE_DATETIME);
		balance.setExpireTime(CommonConstant.DEFALUT_EXPIRE_DATETIME);
		balance.setStatus(CommonConstant.STATUS_NORMAL);
		balance.setDelFlag(CommonConstant.STATUS_NORMAL);
		boolean success = this.fundBalanceService.insert(balance);
		if (success) {
			return balance;
		}
		return null;
    }
	
	/**
	 * @Title TbFundBalanceServiceImpl.queryBalaneByMemberId
	 * @Description:查询会员账户信息， 根据会员id,账户类型,资金对应实例  查询过滤
	 *
	 * @param memberId 会员id
	 * @param countType 账户类型
	 * @param balanceItemIds 资金对应实例 集合
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2018年08月27日 下午5:41:11 修改内容 :
	 */
	@Override
	public List<TbFundBalance> queryBalaneByMIdAndCountType(String memberId, 
			String countType, String... companyIds) {
		EntityWrapper<TbFundAcct> wrapper = new EntityWrapper<>();
		wrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
		wrapper.eq("member_id", memberId);
		String companyId = ArrayUtil.firstNonNull(companyIds);
		if (StrUtil.isNotBlank(companyId)) {
			wrapper.eq("company_id", companyId);
		}
		if(org.apache.commons.lang.StringUtils.isBlank(countType) 
				|| countType.trim().equals(AcctCateEnums.ACCT_CATE_COUPON.getCateCode())) {
			//默认券账户
			wrapper.like("acct_cate",AcctCateEnums.ACCT_CATE_COUPON.getCateCode(),SqlLike.LEFT);
		}else if(countType.trim().equals(AcctCateEnums.ACCT_CATE_MASTER.getCateCode())){
			//账号总账户
			wrapper.like("acct_cate",AcctCateEnums.ACCT_CATE_MASTER.getCateCode(),SqlLike.LEFT);
		}else if(countType.trim().equals(AcctCateEnums.ACCT_CATE_SALARY.getCateCode())){
			//资总账户
			wrapper.like("acct_cate",AcctCateEnums.ACCT_CATE_SALARY.getCateCode(),SqlLike.LEFT);
		}else if(countType.trim().equals(AcctCateEnums.ACCT_CATE_WEAL.getCateCode())){
			//福利总账户
			wrapper.like("acct_cate",AcctCateEnums.ACCT_CATE_WEAL.getCateCode(),SqlLike.LEFT);
		}else if(countType.trim().equals(AcctCateEnums.ACCT_CATE_FBEAN.getCateCode())){
			//福豆总账户
			wrapper.like("acct_cate",AcctCateEnums.ACCT_CATE_FBEAN.getCateCode(),SqlLike.LEFT);
		}else{
			wrapper.like("acct_cate",AcctCateEnums.ACCT_CATE_COUPON.getCateCode(),SqlLike.LEFT);
			log.info("查询会员账户信息》queryBalaneByMIdAndCountType 账户类型参数有误");
		}
		
		TbFundAcct fundAcct = this.fundAcctService.selectOne(wrapper);
		String acctInstNo = fundAcct.getAcctInstNo();
		EntityWrapper<TbFundBalance> wrapperFundBalance = new EntityWrapper<>();
		wrapperFundBalance.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
		
		wrapperFundBalance.eq("acct_inst_no", acctInstNo);
		wrapperFundBalance.ne("balance_item_code", fundAcct.getAcctCate());
		List<TbFundBalance> balances = this.fundBalanceService.selectList(wrapperFundBalance);
		return balances;
	}
	
	@Override
	public Page<TbFundTradeFlow> findPagedTradeFlowByParams(Page<TbFundTradeFlow> page, String memberId, 
			String fundAcctCate, String businessCode, Date tradeBegin, Date tradeEnd) {
		
		TbFundAcct fundAcct = this.acctBalanceBusiService.findFundAcct(memberId, fundAcctCate);
		
		if (null == fundAcct) {
			FundServiceExceptionGenerator.FundServiceException("9011", new Object[] {memberId, "", 
					fundAcctCate});
		}
		
		List<TbFundBalance> balances = this.findFundBalances(fundAcct.getAcctInstNo(),null);
		if (CollUtil.isEmpty(balances)) {
			FundServiceExceptionGenerator.FundServiceException("9012", new Object[] {memberId, "", 
					fundAcctCate});
		}
		
		EntityWrapper<TbFundTradeFlow> wrapper = new EntityWrapper<>();
		
		List<String> balanceCodes = Lists.newArrayList();
		for (TbFundBalance balance : balances) {
			balanceCodes.add(balance.getBalanceCode());
		}
		
		wrapper.in("balance_code", balanceCodes);
		
		if (StringUtils.hasLength(businessCode)) {
			wrapper.eq("trade_cate", businessCode);
		}
		if (null != tradeBegin) {
			wrapper.ge("create_time", tradeBegin);
		}
		if (null != tradeBegin) {
			wrapper.le("create_time", tradeEnd);
		}
		return this.fundTradeFlowService.selectPage(page, wrapper);
	}

	@Override
	public boolean operateAcctBalance(List<FundVoucherBalanceVo> list) {
		List<String> activeMemberIdList = Lists.newArrayList();
		log.info("企业券账户-分配抵用券开始分配 请求参数 list：{}>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>",JSON.toJSON(list));
//		[{"memberId":"memberID","":"","companyId":"公司id","balanceCode":"分配批次号","billItemCate":"业务来源","transBusiCateName":"发放原因",
		//"balanceItemCode":"抵用券id","authBalance":"抵用券分配额度","balanceAmount":"抵用券分配额度","useAmount":"券面额"}]
		/***企业发放抵用券 balance 里面有几条存几条数据 tradeFlow里面按照 公司+券id+来源 ****/
		List<String> companyIdList = Lists.newArrayList();
		List<FundVoucherBalanceVo> onlyFiledList = Lists.newArrayList();
		for (FundVoucherBalanceVo fundVoucherBalanceVo : list) {
			activeMemberIdList.add(fundVoucherBalanceVo.getMemberId());//激活人员
			if(!companyIdList.contains(fundVoucherBalanceVo.getCompanyId())) {
				companyIdList.add(fundVoucherBalanceVo.getCompanyId());
			}
			FundVoucherBalanceVo vo = new FundVoucherBalanceVo();
			BeanUtil.copyProperties(fundVoucherBalanceVo, vo);
			vo.setOnlyField(vo.getBalanceCode()+"-"+vo.getCompanyId()+"-"+vo.getBalanceItemCode()+"-"+vo.getBillItemCate()+"-"+fundVoucherBalanceVo.getTransBusiCateName()); 
			//给企业发抵用券唯一字段  批次号+公司id+券id+业务来源+发放原因
			onlyFiledList.add(vo);
		}
		log.info("组装list返回结果 return result list:{}",JSON.toJSON(onlyFiledList));
		Map<String, List<FundVoucherBalanceVo>> tradeFlowMap = onlyFiledList.stream().collect(Collectors.toMap(FundVoucherBalanceVo::getOnlyField, part ->
		  Lists.newArrayList(part),(List<FundVoucherBalanceVo> newValueList,List<FundVoucherBalanceVo> oldValueList)-> {	
	       		oldValueList.addAll(newValueList);
	       		return oldValueList;
	      }));
		log.info("组装list转map返回结果返回结果 return result tradeFlowMap:{}",JSON.toJSON(tradeFlowMap));
		Wrapper<TbFundAcct> wrapper = new EntityWrapper<>();
		wrapper.in("member_id", companyIdList);
		wrapper.eq("acct_cate", AcctCateEnums.ACCT_CATE_COMPANY_DYQ_ACCOUNT.getCateCode());//企业抵用券账户
		log.info("批量查询账户企业账户，请求参数 request params memberIds:{},memberCate:{}",JSON.toJSON(companyIdList), AcctCateEnums.ACCT_CATE_COMPANY_DYQ_ACCOUNT.getCateCode());
		List<TbFundAcct> fundAcctList = this.fundAcctService.selectList(wrapper);
		log.info("批量查询账户企业账户，返回结果 return result fundAcctList:{}",JSON.toJSON(fundAcctList));
		if(CollUtil.isEmpty(fundAcctList)) {
			log.error("企业抵用券账户不存在，请注意！"); 
			return false;
		}
		List<String> accInstNoList = Lists.newArrayList();
		for (TbFundAcct tbFundAcct : fundAcctList) {
			accInstNoList.add(tbFundAcct.getAcctInstNo());
		}
		/**查询账户余额***/
		Wrapper<TbFundBalance> wrapper1 = new EntityWrapper<TbFundBalance>();
		wrapper1.in("acct_inst_no", accInstNoList);
		List<TbFundBalance> balanceList = fundBalanceService.selectList(wrapper1);
		Map<String, List<TbFundBalance>> balanceMap = balanceList.stream().collect(Collectors.toMap(TbFundBalance::getAcctInstNo, part ->
		  Lists.newArrayList(part),(List<TbFundBalance> newValueList,List<TbFundBalance> oldValueList)-> {	
	       		oldValueList.addAll(newValueList);
	       		return oldValueList;
	      }));
		Set<Entry<String, List<FundVoucherBalanceVo>>> tradeFlowEntrySet = tradeFlowMap.entrySet();// 企业发抵用券券 key为：分配批次号+公司id+券id+业务来源
		log.info("通过唯一key循环得到结果>>>>>>>>>tradeFlowEntrySet ， tradeFlowEntrySet:{}",JSON.toJSON(tradeFlowEntrySet));
		Map<String, TbFundAcct> fundAcctMap = fundAcctList.stream().collect(Collectors.toMap(TbFundAcct::getMemberId, o -> o)); // 账户信息 key为账户id
		List<TbFundBalance> bathAddBalanceList = Lists.newArrayList();
		List<TbFundTradeFlow> tradeFlowList = Lists.newArrayList();
		for (Entry<String, List<FundVoucherBalanceVo>> tradeFlow : tradeFlowEntrySet) {
			if(ObjectUtil.isNull(fundAcctMap.get(tradeFlow.getKey().split("-")[1]))) {
				log.error(tradeFlow.getKey().split("-")[1] +"-企业抵用券账户不存在，请检查！");
				continue;
			}else {
				List<FundVoucherBalanceVo> fundBalanceList = tradeFlow.getValue();
				BigDecimal balanceAmount = new BigDecimal(0);
				BigDecimal balanceAmountTotal = new BigDecimal(0);
				if(CollUtil.isNotEmpty(fundBalanceList)) {
					List<TbFundBalance> banlanceList = balanceMap.get(fundAcctMap.get(tradeFlow.getKey().split("-")[1]).getAcctInstNo()); //查询账户的余额
					if(CollUtil.isEmpty(banlanceList)) {
						balanceAmount = new BigDecimal(0);
					}else {
						for (TbFundBalance tbFundBalance : banlanceList) {
							balanceAmount = balanceAmount.add(tbFundBalance.getBalanceAmount());//累加发券金额
						}
					}
					String balanceCode = CodeUtils.genneratorShort("GMDYQ");
					TbFundBalance tbFundBalance = new TbFundBalance();
					tbFundBalance.setBalanceCode(balanceCode);
					tbFundBalance.setBusiModel("NONE");
					tbFundBalance.setExtendAttrd(IdWorker.getIdStr());
					tbFundBalance.setCanTicket(CommonConstant.STATUS_DEL);
					for (FundVoucherBalanceVo fundVoucherBalanceVo : fundBalanceList) {
						tbFundBalance.setBalanceItemCode(fundVoucherBalanceVo.getBalanceItemCode());
						tbFundBalance.setComposAttr(fundVoucherBalanceVo.getComposAttr());
						tbFundBalance.setAcctInstNo(fundAcctMap.get(tradeFlow.getKey().split("-")[1]).getAcctInstNo());
						tbFundBalance.setCompanyId(fundVoucherBalanceVo.getCompanyId());
						tbFundBalance.setCompanyName(fundVoucherBalanceVo.getCompanyName());
						tbFundBalance.setExtendAttre(fundVoucherBalanceVo.getUseAmount().toString()); //抵用券单张面额
						tbFundBalance.setExtendAttrb(fundVoucherBalanceVo.getBillItemCate());
						tbFundBalance.setExtendAttrd(fundVoucherBalanceVo.getBalanceCode());
						tbFundBalance.setOwnerId(IdWorker.getIdStr());
						balanceAmountTotal = balanceAmountTotal.add(fundVoucherBalanceVo.getBalanceAmount());
						tbFundBalance.setBalanceAmount(balanceAmountTotal);
						tbFundBalance.setAuthBalance(balanceAmountTotal);
						tbFundBalance.setCompanyId(tradeFlow.getKey().split("-")[1]);
						tbFundBalance.setStatus(BalanceStatus.IS_SHOP_0.getCateCode());
						tbFundBalance.setRemark(fundVoucherBalanceVo.getTransBusiCateName()+"-企业购买抵用券(公司id-券id-业务来源-发放原因)："+tradeFlow.getKey());
					}
					bathAddBalanceList.add(tbFundBalance);
					BigDecimal totalAmount = new BigDecimal(0);
					String tradeFlowCode = CodeUtils.genneratorShort("GMDYQ");
					int index = 0;
					for (FundVoucherBalanceVo fundVoucherBalanceVo : tradeFlow.getValue()) {
						index++;
						TbFundTradeFlow tbFundTradeFlow = new TbFundTradeFlow();
						tbFundTradeFlow.setBalanceCode(balanceCode);
						tbFundTradeFlow.setTradeFlowCode(tradeFlowCode);
						tbFundTradeFlow.setTradeFlowOrder(new BigDecimal(index));
						tbFundTradeFlow.setTradeBusiCode(fundVoucherBalanceVo.getBalanceCode());//分配批次号
						tbFundTradeFlow.setBusiModel("NONE");
						tbFundTradeFlow.setTradeBusiCate(OrderType.COMPANY_VOUCHER_CONSUME.getCateCode()); //购买抵用券
						tbFundTradeFlow.setTransBusiCateName(fundVoucherBalanceVo.getTransBusiCateName());
						tbFundTradeFlow.setTradeCate(TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode());
						tbFundTradeFlow.setTransCateName(TradeCateEnums.TRADE_CATE_ADDUCT.getCateName());
						tbFundTradeFlow.setTransActCate(ActionType.ACTION_TYPE_IN.getCateCode());
						tbFundTradeFlow.setTransActName(ActionType.ACTION_TYPE_IN.getCateName());
						tbFundTradeFlow.setTradeAmount(fundVoucherBalanceVo.getBalanceAmount());//交易金额
						tbFundTradeFlow.setTradePreAmount(totalAmount);// 交易前 
						totalAmount = totalAmount.add(fundVoucherBalanceVo.getBalanceAmount());
						tbFundTradeFlow.setTradeLastAmount(tbFundTradeFlow.getTradeAmount().add(tbFundTradeFlow.getTradePreAmount()));// 交易后
						tbFundTradeFlow.setSource(fundVoucherBalanceVo.getBillItemCate());
						tbFundTradeFlow.setRemark(fundVoucherBalanceVo.getTransBusiCateName()+"-企业购买抵用券(公司id-券id-业务来源-发放原因)："+tradeFlow.getKey());
						tradeFlowList.add(tbFundTradeFlow);
					}
				}
			}
		}
		log.info("批量操作企业券账户-分配抵用券，tb_fund_balance 分配抵用券 请求参数 request param list:{}",JSON.toJSON(bathAddBalanceList));
		boolean isOK = fundBalanceService.insertBatch(bathAddBalanceList);
		log.info("批量操作企业券账户-分配抵用券，tb_fund_balance 分配抵用券 返回结果 return result isOK:{}",JSON.toJSON(isOK));
		if(!isOK) {
			log.error("批量操作企业券账户-分配抵用券，tb_fund_balance 分配抵用券失败");
			return false;
		}
		log.info("批量操作企业券账户-分配抵用券，tb_fund_trade_flow 分配抵用券 请求参数 request param list:{}",JSON.toJSON(tradeFlowList));
		boolean isTrue = fundTradeFlowService.insertBatch(tradeFlowList);
		log.info("批量操作企业券账户-分配抵用券，tb_fund_trade_flow 分配抵用券 返回结果 return result isTrue:{}",JSON.toJSON(isTrue));
		if(!isTrue) {
			log.error("批量操作企业券账户-分配抵用券，tb_fund_trade_flow 分配抵用券失败");
			return false;
		}
		companyVoucherAcctSub(bathAddBalanceList); //抵用券账户扣减额度
		log.info("企业券账户-分配抵用券分配完毕结束>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		return true;
	}

	@Override
	public boolean operateMemberVoucherBalance(List<FundVoucherBalanceVo> list) {
		log.info("员工抵用券账户分配抵用券开始>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 请求参数 list:{}",JSON.toJSON(list));
/*	[{"memberId":"memberId","":"","companyId":"公司id","extendAttrd":"分配批次号","billItemCate":"业务来源","transBusiCateName":"发放原因",
		"balanceItemCode":"抵用券id","authBalance":"抵用券单张额度","balanceAmount":"抵用券分配单张额度","useAmount":"券面额","extendAttrc":"使用有效天数",
		"validTime":"抵用券使用开始时间","expireTime":"抵用券使用结束时间"}]  */
		List<FundVoucherBalanceVo> newList = Lists.newArrayList();
		List<String> memberIdList = Lists.newArrayList();
		List<String> companyIdList = Lists.newArrayList();
		for (FundVoucherBalanceVo fundVoucherBalanceVo : list) {
			if(!memberIdList.contains(fundVoucherBalanceVo.getMemberId())) {
				memberIdList.add(fundVoucherBalanceVo.getMemberId());
			}
			if(!companyIdList.contains(fundVoucherBalanceVo.getCompanyId())) {
				companyIdList.add(fundVoucherBalanceVo.getCompanyId());
			}
			FundVoucherBalanceVo newVo = new FundVoucherBalanceVo();
			BeanUtil.copyProperties(fundVoucherBalanceVo,newVo);
			newVo.setOnlyField(fundVoucherBalanceVo.getExtendAttrd()+"-"+fundVoucherBalanceVo.getMemberId()+"-"+fundVoucherBalanceVo.getBalanceItemCode()+"-"+
			fundVoucherBalanceVo.getBillItemCate()+"-"+fundVoucherBalanceVo.getTransBusiCateName());//批次号+memberId+voucherId+来源+发放原因
			newVo.setRemark(fundVoucherBalanceVo.getExtendAttrd()+"-"+fundVoucherBalanceVo.getCompanyId()+"-"+fundVoucherBalanceVo.getBalanceItemCode()+"-"+
			fundVoucherBalanceVo.getBillItemCate()+"-"+fundVoucherBalanceVo.getTransBusiCateName()); //批次号+公司id+voucherId+来源+发放原因
			newList.add(newVo);
		}
		log.info("newList返回结果 return result newList:{}",JSON.toJSON(newList));
		Map<String, List<FundVoucherBalanceVo>> balanceTradeFlowMap = newList.stream().collect(Collectors.toMap(FundVoucherBalanceVo::getOnlyField, part ->
		  Lists.newArrayList(part),(List<FundVoucherBalanceVo> newValueList,List<FundVoucherBalanceVo> oldValueList)-> {	
	       		oldValueList.addAll(newValueList);
	       		return oldValueList;
	      }));
		
		log.info("newList转map返回结果 return result balanceTradeFlowMap:{}",JSON.toJSON(balanceTradeFlowMap));
		Wrapper<TbFundAcct> wrapper = new EntityWrapper<>();
		wrapper.in("member_id", memberIdList);
		wrapper.eq("acct_cate", AcctCateEnums.ACCT_CATE_MEMBER_DYQ_ACCOUNT.getCateCode());//员工抵用券账户
		log.info("批量查询员工抵用券账户，请求参数 request params memberIds:{},memberCate:{}",JSON.toJSON(memberIdList), AcctCateEnums.ACCT_CATE_MEMBER_DYQ_ACCOUNT.getCateCode());
		List<TbFundAcct> fundAcctList = this.fundAcctService.selectList(wrapper);
		log.info("批量查询员工抵用券账户，返回结果 return result fundAcctList:{}",JSON.toJSON(fundAcctList));
		if(CollUtil.isEmpty(fundAcctList)) {
			log.error("员工抵用券账户不存在，请注意！"); 
			return false;
		}
		CompanyBuyVoucherVo vo = new CompanyBuyVoucherVo();
		vo.setCompanyIdList(companyIdList);
		log.info("批量通过公司id查询账户balance 信息 请求参数 request param dto:{}",JSON.toJSON(vo));
		List<CompanyBuyVoucherVo> companyAccount = balanceBusiMapper.findAcctBalanceByCompanyId(vo);
		log.info("批量通过公司id查询账户balance 信息 返回结果 return result :{}",JSON.toJSON(companyAccount));
		List<CompanyBuyVoucherVo> companyAccountNewList = Lists.newArrayList();
		for (CompanyBuyVoucherVo companyBuyVoucherVo : companyAccount) {
			CompanyBuyVoucherVo newVo = new CompanyBuyVoucherVo();
			BeanUtil.copyProperties(companyBuyVoucherVo, newVo); // onlyFile 字段拼接   批次号+公司id+券id+业务来源+发放类型）
			newVo.setOnlyFiled(companyBuyVoucherVo.getBatchNo()+"-"+companyBuyVoucherVo.getOnlyFiled()+"-"+companyBuyVoucherVo.getRemark().split("-")[0]); 
			companyAccountNewList.add(newVo);
		}
		log.info("查询balance里面的数据信息转list companyAccountNewList:{}",JSON.toJSON(companyAccountNewList));
		Map<String, CompanyBuyVoucherVo> balanceMap = companyAccountNewList.stream().collect(Collectors.toMap(CompanyBuyVoucherVo::getOnlyFiled, o -> o));//账户信息
		log.info("查询balance里面的数据信息转map 返回结果 return result balanceMap:{}",JSON.toJSON(balanceMap));
		
		Map<String, TbFundAcct> acctMap = fundAcctList.stream().collect(Collectors.toMap(TbFundAcct::getMemberId, o -> o)); //员工抵用券账户信息
		log.info("批量查询账户 acct 里面的数据信息转map 返回结果 return result acctMap:{}",JSON.toJSON(acctMap));
		
		/***循环传入的信息 一个一个去匹配账户信息 增加 balance和TradeFlow  发几张记录几张券***/
		Set<Entry<String, List<FundVoucherBalanceVo>>> balanceTradeFlowMapEntrySet = balanceTradeFlowMap.entrySet();
		List<TbFundBalance> balanceList = Lists.newArrayList(); 
		List<TbFundTradeFlow> tradeFlowList = Lists.newArrayList();
		/*	[{"memberId":"memberId","companyId":"公司id","companyName":"公司名称","billItemCate":"业务来源","transBusiCateName":"发放原因",
		"balanceItemCode":"抵用券id","authBalance":"抵用券单张额度","balanceAmount":"抵用券分配单张额度","useAmount":"券面额",
		"extendAttra":"一次性全部到账激活。。。",
		"extendAttrc":"使用有效天数","extendAttrd":"分配批次号",
		"validTime":"抵用券使用开始时间","expireTime":"抵用券使用结束时间"}]  */
		for (Entry<String, List<FundVoucherBalanceVo>> balanceTradeFlow : balanceTradeFlowMapEntrySet) {
			if(ObjectUtil.isNull(acctMap.get(balanceTradeFlow.getKey().split("-")[1])) ) {
				log.error("员工抵用券账户不存在，memberId:{}", acctMap.get(balanceTradeFlow.getKey().split("-")[1]).getMemberId());
				continue;
			}else {
				String acctInstNo = acctMap.get(balanceTradeFlow.getKey().split("-")[1]).getAcctInstNo();
				List<FundVoucherBalanceVo> balanceTradeFlowValueList = balanceTradeFlow.getValue();
				if(CollUtil.isNotEmpty(balanceTradeFlowValueList)) {
					int index = 0;
					for (FundVoucherBalanceVo fundVoucherBalanceVo : balanceTradeFlowValueList) {
						String balanceCode = CodeUtils.genneratorShort("FPDYQ");
						String tradeFlowCode = CodeUtils.genneratorShort("GMDYQ");
						TbFundBalance balance = new TbFundBalance();
						TbFundTradeFlow tradeFlow = new TbFundTradeFlow();
						index++;
						balance.setBalanceCode(balanceCode);
						balance.setAcctInstNo(acctInstNo);
						balance.setBalanceItemCode(fundVoucherBalanceVo.getBalanceItemCode());
						balance.setAuthBalance(fundVoucherBalanceVo.getAuthBalance());
						balance.setBalanceAmount(fundVoucherBalanceVo.getBalanceAmount());
						balance.setBusiModel("NONE");
						balance.setCompanyId(fundVoucherBalanceVo.getCompanyId());
						balance.setCompanyName(fundVoucherBalanceVo.getCompanyName());
						balance.setOwnerId(IdWorker.getIdStr());
						balance.setCanTicket(CommonConstant.STATUS_DEL);
						balance.setCanTransfer(CommonConstant.STATUS_DEL);
						balance.setExtendAttra(fundVoucherBalanceVo.getExtendAttra());
						balance.setExtendAttrb(fundVoucherBalanceVo.getBillItemCate());
						balance.setExtendAttrc(fundVoucherBalanceVo.getExtendAttrc());
						balance.setExtendAttrd(fundVoucherBalanceVo.getExtendAttrd());
						balance.setExtendAttre(fundVoucherBalanceVo.getBalanceAmount()+"");
						balance.setComposAttr(fundVoucherBalanceVo.getComposAttr());
						if(fundVoucherBalanceVo.getExtendAttra().equals(voucherGrantWay.voucher_grant_way_1.getCode())) {
							/***一次性全部到账激活 balance状态为已激活***/
							balance.setStatus(BalanceStatus.IS_SHOP_0.getCateCode());
							/**券使用天数不为空，去计算有效开始和结束时间***/
							if(org.apache.commons.lang.StringUtils.isNotBlank(fundVoucherBalanceVo.getExtendAttrc()) && Integer.parseInt(fundVoucherBalanceVo.getExtendAttrc()) > 0) {
								balance.setValidTime(new Date());
								balance.setExpireTime(DateToolUtils.toNewDateAddDays(new Date(), Integer.parseInt(fundVoucherBalanceVo.getExtendAttrc())));
							}else if( (org.apache.commons.lang.StringUtils.isBlank(fundVoucherBalanceVo.getExtendAttrc()) ||
									(org.apache.commons.lang.StringUtils.isNotBlank(fundVoucherBalanceVo.getExtendAttrc()) && Integer.parseInt(fundVoucherBalanceVo.getExtendAttrc()) == 0) )
									&&  (fundVoucherBalanceVo.getValidTime() == null && fundVoucherBalanceVo.getExpireTime() == null ) ) {
								log.error(balanceTradeFlow.getKey().split("-")[1]+"-一次性全部到账激活发券券形式，券使用天数为空，日期时间段也为空，数据有误>>>>>>>>>>>>>>请注意！！！");
								balance.setValidTime(fundVoucherBalanceVo.getValidTime());
								balance.setExpireTime(fundVoucherBalanceVo.getExpireTime());
							}else {
								balance.setValidTime(fundVoucherBalanceVo.getValidTime());
								balance.setExpireTime(fundVoucherBalanceVo.getExpireTime());
							}
						}else if(fundVoucherBalanceVo.getExtendAttra().equals(voucherGrantWay.voucher_grant_way_2.getCode())) {
							/**按工作日逐张到账激活  更新账户抵用券账户张数**/
							balance.setStatus(BalanceStatus.NOT_SHOP_1.getCateCode());
							balance.setValidTime(fundVoucherBalanceVo.getValidTime());
							balance.setExpireTime(fundVoucherBalanceVo.getExpireTime());
							FundBalanceDto dto = new FundBalanceDto();
							dto.setMemberId(fundVoucherBalanceVo.getMemberId());
							//paramList.add(dto);
						}
						balance.setRemark(OrderType.ORDER_TYPE_MEMBER_DYQ_ADD.getCateMgn()+"-抵用券入账(memberId-券id-业务来源-发放原因)："+ balanceTradeFlow.getKey());
						balanceList.add(balance);
						
						tradeFlow.setTradeFlowCode(tradeFlowCode);
						tradeFlow.setTradeFlowOrder(new BigDecimal(index));
						if(ObjectUtil.isNotNull(balanceMap.get(fundVoucherBalanceVo.getRemark()))) {
							tradeFlow.setTradeBusiCode(balanceMap.get(fundVoucherBalanceVo.getRemark()).getBalanceCode()); // 企业购买时候balance_code
						}
						tradeFlow.setBusiModel("NONE");
						tradeFlow.setTradeBusiCate(OrderType.ORDER_TYPE_MEMBER_DYQ_ADD.getCateCode());
						tradeFlow.setTransBusiCateName(fundVoucherBalanceVo.getTransBusiCateName());
						tradeFlow.setBalanceCode(balanceCode);
						tradeFlow.setTradeCate(TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode());
						tradeFlow.setTransCateName(TradeCateEnums.TRADE_CATE_ADDUCT.getCateName());
						tradeFlow.setTransActCate(ActionType.ACTION_TYPE_IN.getCateCode());
						tradeFlow.setTransActName(ActionType.ACTION_TYPE_IN.getCateName());
						tradeFlow.setTradeAmount(fundVoucherBalanceVo.getBalanceAmount());
						tradeFlow.setTradePreAmount(new BigDecimal(0));
						tradeFlow.setTradeLastAmount(tradeFlow.getTradeAmount().add(tradeFlow.getTradePreAmount()));
						tradeFlow.setSource(fundVoucherBalanceVo.getBillItemCate());
						tradeFlow.setRemark(OrderType.ORDER_TYPE_MEMBER_DYQ_ADD.getCateMgn()+"-抵用券入账(memberId-券id-业务来源-发放原因)："+ balanceTradeFlow.getKey());
						tradeFlowList.add(tradeFlow);
					}
				}
			}
		}
		
		log.info("员工抵用券账户分配抵用券，tb_fund_balance 保存数据 请求参数 balanceList:{}",JSON.toJSON(balanceList));
		boolean isOK = fundBalanceService.insertBatch(balanceList);
		log.info("员工抵用券账户分配抵用券，tb_fund_balance 保存数据 返回结果  result:{}",JSON.toJSON(isOK));
		if(!isOK) {
			log.error("员工抵用券账户分配抵用券额度失败");
			return false;
		}
		log.info("员工抵用券账户分配抵用券，tb_fund_trade_flow 保存数据 请求参数 tradeFlowList:{}",JSON.toJSON(tradeFlowList));
		boolean isTrue = fundTradeFlowService.insertBatch(tradeFlowList);
		log.info("员工抵用券账户分配抵用券，tb_fund_trade_flow 返回结果 isTrue:{}",JSON.toJSON(isTrue));
		if(!isTrue) {
			log.error("员工抵用券账户分配抵用券额度详细信息失败");
			return false;
		}
		
		//balanceBusiService.addAcctActiveAccount(paramList); // 激活抵用券张数（按工作日到账激活才有）
		
		log.info("员工抵用券账户分配抵用券结束>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		return true;
	}
	
	/***
	 * 
	 * 
	 * @Title BlanceServiceImpl.companyVoucherAcctSub
	 * @Description: 企业抵用券账户扣减抵用券额度
	 *
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年10月31日 下午5:02:54
	 * 修改内容 :
	 */
	public boolean companyVoucherAcctSub(List<TbFundBalance> bathAddBalanceList) {
		List<FundBalanceDto> paramList = Lists.newArrayList();
		for (TbFundBalance tbFundBalance : bathAddBalanceList) {
			FundBalanceDto dto = new FundBalanceDto();
			dto.setBalanceCode(tbFundBalance.getBalanceCode());
			dto.setExtendAttrd(tbFundBalance.getExtendAttrd());
			dto.setBalanceAmount(tbFundBalance.getBalanceAmount());
			dto.setTradeAmount(tbFundBalance.getBalanceAmount());
			paramList.add(dto);
		}
		log.info("企业抵用券账户扣减抵用券额度请求参数，request param list:{}",JSON.toJSON(paramList));
		R<Boolean> r = balanceBusiService.companyVoucherAcctSub(paramList);
		log.info("企业抵用券账户扣减抵用券额度返回结果，return result r:{}",JSON.toJSON(r));
		if(r == null || R.SUCCESS != r.getCode()) {
			log.error("企业抵用券账户扣减抵用券额度返回结果异常");
			return false;
		}
		return true;
	}
	

	@Override
	public Page<CompanyBuyVoucherVo> findCompanyBuyVoucherDeatil(Query query,String companyId) {
		log.info("通过公司id查询公司购买抵用券信息请求参数，request params companyId:{}",companyId);
		List<CompanyBuyVoucherVo> returnList = balanceBusiMapper.findCompanyBuyVoucherDeatil(query,companyId);
		log.info("通过公司id查询公司购买抵用券信息返回结果，return result list:{}",JSON.toJSON(returnList));
		if(CollUtil.isEmpty(returnList)) {
			query.setTotal(returnList.size());
			return query.setRecords(returnList);
		}
		CouponMemberTmpDto dto = new CouponMemberTmpDto();
		dto.setCompanyId(companyId);
		List<String> bathNoList = Lists.newArrayList();
		List<String> voucherIdList = Lists.newArrayList();
		for (CompanyBuyVoucherVo companyBuyVoucherVo : returnList) {
			if(!bathNoList.contains(companyBuyVoucherVo.getTmpBatchNo())) {
				bathNoList.add(companyBuyVoucherVo.getTmpBatchNo());
			}
			if(!voucherIdList.contains(companyBuyVoucherVo.getVoucherId())) {
				voucherIdList.add(companyBuyVoucherVo.getVoucherId());
			}
		}
		dto.setBatchNoList(bathNoList);
		dto.setVoucherIdList(voucherIdList);
		log.info("通过公司id查询公司购买抵用券信息通过批次号和券id去查询抵用券属性 请求参数 dto:{}",JSON.toJSON(dto));
		R<List<CouponMemberTmpVo>> r = refactorCouponMemberTmpService.findAllotVoucherAttr(dto);
		log.info("通过公司id查询公司购买抵用券信息通过批次号和券id去查询抵用券属性 返回结果 return result r:{}",JSON.toJSON(r));
		if(r == null || R.SUCCESS != r.getCode()) {
			log.error("公司购买抵用券详细信息，去匹配券属性异常");
			return query.setRecords(returnList);
		}
		List<CouponMemberTmpVo> voucherAttrList = r.getData();
		List<CouponMemberTmpVo> sortList = Lists.newArrayList();
		for (CouponMemberTmpVo couponMemberTmpVo : voucherAttrList) {
			CouponMemberTmpVo vo = new CouponMemberTmpVo();
			BeanUtil.copyProperties(couponMemberTmpVo, vo);
			vo.setOnlyField(couponMemberTmpVo.getCompanyId()+"-"+couponMemberTmpVo.getVoucherId()+"-"+couponMemberTmpVo.getBatchNo());
			sortList.add(vo);
		}
		Map<String, List<CouponMemberTmpVo>> voucherMap = sortList.stream().collect(Collectors.toMap(CouponMemberTmpVo::getOnlyField, part ->
		  Lists.newArrayList(part),(List<CouponMemberTmpVo> newValueList,List<CouponMemberTmpVo> oldValueList)-> {	
	       		oldValueList.addAll(newValueList);
	       		return oldValueList;
	      }));
		/***匹配券属性**/
		for (CompanyBuyVoucherVo companyBuyVoucherVo : returnList) {
			companyBuyVoucherVo.setSourceName(DictionaryEnum.sourceCode.existsCode(companyBuyVoucherVo.getSource()));
			if(ObjectUtil.isNotNull(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()))) {
				companyBuyVoucherVo.setVoucherTypeName(DictionaryEnum.voucherType.existsCode(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getVoucherType()));
				companyBuyVoucherVo.setVoucherType(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getVoucherType());
				companyBuyVoucherVo.setVoucherGrantWay(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getVoucherGrantWay());
				companyBuyVoucherVo.setVoucherGrantWayName(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getVoucherGrantWayName());
				if(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getUseType().equals("1")) {
					companyBuyVoucherVo.setUseDay(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getUseDay()+"天");
				}else {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					companyBuyVoucherVo.setUseDay(sdf.format(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getValidStartTime())+"至"+sdf.format(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getValidEndTime()));
				}
				if(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getGrantType().equals("1")) {
					companyBuyVoucherVo.setGrantDay(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getGrantDay()+"天");
				}else {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					companyBuyVoucherVo.setGrantDay(sdf.format(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getGrantStartTime())+"至"+sdf.format(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getGrantEndTime()));
				}
				companyBuyVoucherVo.setCreator(voucherMap.get(companyBuyVoucherVo.getOnlyFiled()).get(0).getCreator());
			}
		}
		query.setTotal(returnList.size());
		return query.setRecords(returnList);
	}

	@Override
	public List<CompanyBuyVoucherVo> findCompanyAllotVoucherDeatil(String companyId) {
		log.info("查询员工抵用券员工分配明细>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>请求参数 companyId:{}",companyId);
		List<CompanyBuyVoucherVo> fundBalanceList = balanceBusiMapper.findCompanyBuyVoucherDeatilNoPage(companyId);
		log.info("通过公司id先去查询对应企业对应的balance 里面的 balanceCode 返回结果 return result balanceList:{}",JSON.toJSON(fundBalanceList));
		if(CollUtil.isEmpty(fundBalanceList)) {
			return null;
		}
		List<String> balanceCodeList = Lists.newArrayList();
		for (CompanyBuyVoucherVo companyBuyVoucherVo : fundBalanceList) {
			balanceCodeList.add(companyBuyVoucherVo.getBalanceCode());
		}
		CompanyBuyVoucherVo companyVoucherDto = new CompanyBuyVoucherVo();
		companyVoucherDto.setBalanceCodeList(balanceCodeList);
		log.info("查询员工分配抵用券详细信息>>>>前先去查询归属企业，请求参数 request param  dto:{}",JSON.toJSON(companyVoucherDto));
		List<CompanyBuyVoucherVo> allotDetail = balanceBusiMapper.findMemberVoucherAllotCount(companyVoucherDto);
		log.info("查询员工分配抵用券详细信息>>>>前先去查询归属企业，返回结果 return result allotDetail:{}",JSON.toJSON(allotDetail));
		CouponMemberTmpDto dto = new CouponMemberTmpDto();
		List<String> batchNoList = Lists.newArrayList();
		List<String> voucherIdList = Lists.newArrayList();
		for (CompanyBuyVoucherVo companyBuyVoucherVo : allotDetail) {
			if(!batchNoList.contains(companyBuyVoucherVo.getTmpBatchNo())) {
				batchNoList.add(companyBuyVoucherVo.getTmpBatchNo());
			}
			if(!voucherIdList.contains(companyBuyVoucherVo.getVoucherId())) {
				voucherIdList.add(companyBuyVoucherVo.getVoucherId());
			}
		}
		dto.setBatchNoList(batchNoList);
		dto.setVoucherIdList(voucherIdList);
		log.info("查询员工分配抵用券详细信息>>>>批量查询匹配券属性，请求参数 request param dto:{}",JSON.toJSON(dto));
		R<List<CouponMemberTmpVo>> r = refactorCouponMemberTmpService.findAllotVoucherAttr(dto);
		log.info("查询员工分配抵用券详细信息>>>>批量查询匹配券属性，返回结果 return result r:{}",JSON.toJSON(r));
		if(r == null || R.SUCCESS != r.getCode()) {
			log.error("公司购买抵用券详细信息，去匹配券属性异常");
			return fundBalanceList;
		}
		List<CouponMemberTmpVo> voucherAttrList = Lists.newArrayList();
		for (CouponMemberTmpVo couponMemberTmpVo : r.getData()) {
			CouponMemberTmpVo vo = new CouponMemberTmpVo();
			BeanUtil.copyProperties(couponMemberTmpVo, vo);
			vo.setOnlyField(couponMemberTmpVo.getBatchNo()+"-"+couponMemberTmpVo.getMemberId()+"-"+couponMemberTmpVo.getVoucherId()+"-"
			+couponMemberTmpVo.getSourceCode()+"-"+couponMemberTmpVo.getAllotReasonName());
			voucherAttrList.add(vo);
		}
		Map<String, List<CouponMemberTmpVo>> voucherAttrMap = voucherAttrList.stream().collect(Collectors.toMap(CouponMemberTmpVo::getOnlyField, part ->
		  Lists.newArrayList(part),(List<CouponMemberTmpVo> newValueList,List<CouponMemberTmpVo> oldValueList)-> {	
	       		oldValueList.addAll(newValueList);
	       		return oldValueList;
	      }));
		
		/***按照批次号、员工id、券id、来源、发放原因 统计**/
		Map<String, List<CompanyBuyVoucherVo>> voucherMap = allotDetail.stream().collect(Collectors.toMap(CompanyBuyVoucherVo::getOnlyFiled, part ->
		  Lists.newArrayList(part),(List<CompanyBuyVoucherVo> newValueList,List<CompanyBuyVoucherVo> oldValueList)-> {	
	       		oldValueList.addAll(newValueList);
	       		return oldValueList;
	      }));
		log.info("查询资金分配明细返回结果 ，return result map:{}",JSON.toJSON(voucherMap));
		List<CompanyBuyVoucherVo> returnList = Lists.newArrayList();
		Set<Entry<String, List<CompanyBuyVoucherVo>>> entrySet = voucherMap.entrySet();
		for (Entry<String, List<CompanyBuyVoucherVo>> entry : entrySet) {
			CompanyBuyVoucherVo returnVo = new CompanyBuyVoucherVo();
			if(ObjectUtil.isNotNull(voucherAttrMap.get(entry.getKey()))) {
				returnVo.setMobile(voucherAttrMap.get(entry.getKey()).get(0).getMobile());
				returnVo.setRealName(voucherAttrMap.get(entry.getKey()).get(0).getRealName());
				returnVo.setVoucherId(voucherAttrMap.get(entry.getKey()).get(0).getVoucherId());
				returnVo.setVoucherName(voucherAttrMap.get(entry.getKey()).get(0).getVoucherName());
				returnVo.setVoucherType(voucherAttrMap.get(entry.getKey()).get(0).getVoucherType());
				returnVo.setVoucherTypeName(DictionaryEnum.voucherType.existsCode(voucherAttrMap.get(entry.getKey()).get(0).getVoucherType()));
				returnVo.setVoucherGrantWayName(voucherAttrMap.get(entry.getKey()).get(0).getVoucherGrantWayName());
				if(voucherAttrMap.get(entry.getKey()).get(0).getUseType().equals("1")) {
					returnVo.setUseDay(voucherAttrMap.get(entry.getKey()).get(0).getUseDay()+"天");
				}else {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					returnVo.setUseDay(sdf.format(voucherAttrMap.get(entry.getKey()).get(0).getValidStartTime())+"至"+sdf.format(voucherAttrMap.get(entry.getKey()).get(0).getValidEndTime()));
				}
				returnVo.setSource(voucherAttrMap.get(entry.getKey()).get(0).getSourceName());
				returnVo.setAllotReasonName(voucherAttrMap.get(entry.getKey()).get(0).getAllotReasonName());
				returnVo.setRemark(voucherAttrMap.get(entry.getKey()).get(0).getExcelRemark());
				returnVo.setCreator(voucherAttrMap.get(entry.getKey()).get(0).getCreator());
			}
			returnVo.setCouponAmount(entry.getValue().get(0).getCouponAmount());
			returnVo.setBatchNo(entry.getValue().get(0).getBalanceCode());
			returnVo.setAllotNum(entry.getValue().size()+"");
			returnVo.setCreateTime(entry.getValue().get(0).getCreateTime());
			returnVo.setCouponAmountShink(entry.getValue().get(0).getCouponAmountShink());
			returnList.add(returnVo);
		}
		return returnList;
	}
	
}

