/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  BalanceBusiServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 9, 2018 7:25:23 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BalanceStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderBusiStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderDetailStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.Group;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.UserUtils;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.dto.BalanceSearchParams;
import com.taolue.baoxiao.fund.api.dto.FundAcctDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.vo.FundBalanceVo;
import com.taolue.baoxiao.fund.api.dto.OrderDTO;
import com.taolue.baoxiao.fund.api.dto.OrderDetailDTO;
import com.taolue.baoxiao.fund.api.vo.FundAcctVo;
import com.taolue.baoxiao.fund.api.vo.FundBalanceVo;
import com.taolue.baoxiao.fund.api.vo.FundTradeFlowVo;
import com.taolue.baoxiao.fund.api.vo.FundVoucherBalanceVo;
import com.taolue.baoxiao.fund.entity.TbFundAcct;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.mapper.BalanceBusiMapper;
import com.taolue.baoxiao.fund.service.IOrderService;
import com.taolue.baoxiao.fund.service.ITbFundAcctService;
import com.taolue.baoxiao.fund.service.ITbFundBalanceService;
import com.taolue.baoxiao.fund.service.ITbFundTradeFlowService;
import com.taolue.baoxiao.fund.service.composite.IBalanceBusiService;
import com.taolue.member.api.dto.QueryCompanyDto;

import ch.qos.logback.classic.Logger;
import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**   
 * @ClassName:  BalanceBusiServiceImpl   
 * @Description: 提供所有资金相关的服务
 * @Author: shilei
 * @date:   Dec 9, 2018 7:25:23 PM   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class BalanceBusiServiceImpl extends ServiceImpl<BalanceBusiMapper, FundBalanceDto> 
			implements IBalanceBusiService {

	@Autowired
    private ITbFundAcctService fundAcctService;
	
	@Autowired
	private IOrderService orderService;
	
	@Autowired
    private ITbFundBalanceService fundBalanceService;
	
	@Autowired
	private ITbFundTradeFlowService tradeFlowService;
	
	private boolean isDebug = log.isDebugEnabled();
	
	/**   
	 * <p>Title: selectByConditions</p>   
	 * <p>Description: </p>   
	 * @param params
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IBalanceBusiService#selectByConditions(com.taolue.baoxiao.fund.api.dto.BalanceSearchParams)   
	 */  
	@Override
	public List<FundBalanceDto> selectByConditions(BalanceSearchParams params) {
		return this.baseMapper.selectByParams(params);
	}
	
	/**   
	 * <p>Title: selectByConditions</p>   
	 * <p>Description: </p>   
	 * @param acctInfoParams
	 * @param balanceInfoParams
	 * @param lockFlag
	 * @param balanceItemCodes
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IBalanceBusiService#selectByConditions(com.taolue.baoxiao.fund.api.dto.FundAcctDto, com.taolue.baoxiao.fund.api.dto.FundBalanceDto, boolean, java.lang.String[])   
	 */  
	@Override
	public List<FundBalanceDto> selectByConditions(FundAcctDto acctInfoParams, FundBalanceDto balanceInfoParams,
			int lockSecond, List<Group> groups, String... balanceItemCodes) {
		BalanceSearchParams params = new BalanceSearchParams(acctInfoParams, balanceInfoParams, lockSecond, 
				groups, balanceItemCodes);
		if (isDebug) {
			log.info("selectByConditions(FundAcctDto acctInfoParams, FundBalanceDto balanceInfoParams," + 
					"boolean lockFlag, String... balanceItemCodes) 查询参数 为"+JSON.toJSONString(params));
		}
		return this.selectByConditions(params);
	}

	/**   
	 * <p>Title: selectByConditions</p>   
	 * <p>Description: </p>   
	 * @param balanceInfoParams
	 * @param lockFlag
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IBalanceBusiService#selectByConditions(com.taolue.baoxiao.fund.api.dto.FundBalanceDto, boolean)   
	 */  
	@Override
	public List<FundBalanceDto> selectByConditions(FundBalanceDto balanceInfoParams, int lockSecond, List<Group> groups) {
		BalanceSearchParams params = new BalanceSearchParams(null, balanceInfoParams, lockSecond, groups);
		if (isDebug) {
			log.info("selectByConditions(FundAcctDto acctInfoParams, FundBalanceDto balanceInfoParams," + 
					"boolean lockFlag, String... balanceItemCodes) 查询参数 为"+JSON.toJSONString(params));
		}
		return this.selectByConditions(params);
	}

	/**   
	 * <p>Title: selectOneByBalanceItemCode</p>   
	 * <p>Description: </p>   
	 * @param balanceItemCode
	 * @param lockFlag
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IBalanceBusiService#selectOneByBalanceItemCode(java.lang.String, boolean)   
	 */  
	@Override
	public FundBalanceDto selectOneByBalanceItemCode(String balanceItemCode, int lockSecond) {
		BalanceSearchParams params = new BalanceSearchParams(null, lockSecond, null, balanceItemCode);
		if (isDebug) {
			log.info("selectByConditions(FundAcctDto acctInfoParams, FundBalanceDto balanceInfoParams," + 
					"boolean lockFlag, String... balanceItemCodes) 查询参数 为"+JSON.toJSONString(params));
		}
		params.setBalanceAmount(null);
		List<FundBalanceDto> result = this.selectByConditions(params);
		if (CollUtil.isNotEmpty(result)) {
			return result.get(0);
		}
		return null;
	}

	/**   
	 * <p>Title: selectOneByBalanceCode</p>   
	 * <p>Description: </p>   
	 * @param balanceCode
	 * @param lockFlag
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IBalanceBusiService#selectOneByBalanceCode(java.lang.String, boolean)   
	 */  
	@Override
	public FundBalanceDto selectOneByBalanceCode(String balanceCode, int lockSecond) {
		BalanceSearchParams params = new BalanceSearchParams(balanceCode, lockSecond, null);
		if (isDebug) {
			log.info("selectByConditions(FundAcctDto acctInfoParams, FundBalanceDto balanceInfoParams," + 
					"boolean lockFlag, String... balanceItemCodes) 查询参数 为"+JSON.toJSONString(params));
		}
		params.setBalanceAmount(null);
		List<FundBalanceDto> result = this.selectByConditions(params);
		if (CollUtil.isNotEmpty(result)) {
			return result.get(0);
		}
		return null;
	}

	/**   
	 * <p>Title: selectByParamsPage</p>   
	 * <p>Description: </p>   
	 * @param params
	 * @param rowBounds
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IBalanceBusiService#selectByParamsPage(com.taolue.baoxiao.fund.api.dto.BalanceSearchParams, org.apache.ibatis.session.RowBounds)   
	 */  
	@Override
	public Query<FundBalanceDto> selectByConditionsPage(BalanceSearchParams params, Query<FundBalanceDto> query) {
		List<FundBalanceDto> records = this.baseMapper.selectByParams(params, query);
		query.setRecords(records);
		return query;
	}
	
	/**
	 * 
	 * <p>Title: selectNoCouponByConditionsPage</p>   
	 * <p>Description: </p>   
	 * @param params
	 * @param query
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IBalanceBusiService#selectNoCouponByConditionsPage(com.taolue.baoxiao.fund.api.dto.BalanceSearchParams, com.taolue.baoxiao.common.util.Query)
	 */
	@Override
	public Page<FundBalanceDto> selectNoCouponByConditionsPage(BalanceSearchParams params, 
			Query<FundBalanceDto> query){
		List<FundBalanceDto> records = this.baseMapper.selectListNoCoupon(params, query);
		query.setRecords(records);
		return query;
	}
	
	@Override
	public BigDecimal selectTotalNoCoupon(BalanceSearchParams params){
		List<FundBalanceDto> records = this.baseMapper.selectTotalNoCoupon(params);
		if (CollUtil.isNotEmpty(records) && null != records.get(0)) {
			return records.get(0).getBalanceAmount();
		}
		return CommonConstant.NO_AMOUNT;
	}
	
	@Override
	public List<FundBalanceDto> selectFundBalanceForChash(Map<String, Object> params) {
		return this.baseMapper.selectFundBalanceForChash(params);
	}

	@Override
	public List<FundBalanceVo> findMemberCouponBalance(FundBalanceVo dto) {
		return this.baseMapper.findMemberCouponBalance(dto);
	}
	
	
	@Override
	public List<FundBalanceVo> findMemberCouponBalanceExport(FundBalanceVo dto) {
		return this.baseMapper.findMemberCouponBalanceExport(dto);
	}
	
	@Override
	public List<FundBalanceVo> findMemberCouponBalanceDetail(FundBalanceVo dto) {
		return this.baseMapper.findMemberCouponBalanceDetail(dto);
	}
	public List<FundBalanceVo> findCompanyBalance(FundBalanceVo funddto) {
		return this.baseMapper.findCompanyBalance(funddto);
	}
	
	@Override
	public FundAcctVo findCompanyIntegralAcc(FundAcctVo dto) {
		return this.baseMapper.findCompanyIntegralAcc(dto);
	}

	@Override
	public Integer findVoucherNumber(FundVoucherBalanceVo dto) {
		return this.baseMapper.findVoucherNumber(dto);
	}

	@Override
	public List<FundVoucherBalanceVo> findVoucherInfo(FundVoucherBalanceVo dto) {
		return this.baseMapper.findVoucherInfo(dto);
	}
	@Override
	public List<FundVoucherBalanceVo> findVoucherInfoByMemberId(FundVoucherBalanceVo dto) {
		return this.baseMapper.findVoucherInfoByMemberId(dto);
	}

	@Override
	public List<FundBalanceVo> findIntegralDetail(QueryCompanyDto dto) {
		return this.baseMapper.findIntegralDetail(dto);
	}

	@Override
	public List<FundTradeFlowVo> selectOperationDetail(QueryCompanyDto dto) {
		return this.baseMapper.selectOperationDetail(dto);
	}

	@Override
	public List<FundBalanceVo> findIntegralDcByMemberId(QueryCompanyDto querydto) {
		return this.baseMapper.findIntegralDcByMemberId(querydto);
	}

	@Override
	public R<Boolean> insertIntegralByBatch(FundTradeFlowVo dto) {
		log.info("企业积分批量操作入参参数>>>>>"+JSON.toJSONString(dto));
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
		for(FundTradeFlowVo flowVo : dto.getDtoList()) {
			//根据企业ID查询企业积分账户
			FundAcctVo vo = new FundAcctVo();
			vo.setMemberId(flowVo.getCompanyId());
			vo.setMemberName(flowVo.getCompanyName());
			vo.setAcctCate(DictionaryEnum.AcctCateEnums.ACCT_CATE_COMPANY_JF_ACCOUNT.getCateCode());
			FundAcctVo accdyq = new FundAcctVo();
			FundAcctVo accVojf = this.findCompanyIntegralAcc(vo);
			log.info("查询企业积分账号返回结果："+JSON.toJSONString(accVojf));
			//根据企业ID查询企业抵用券账户
			vo.setAcctCate(DictionaryEnum.AcctCateEnums.ACCT_CATE_COMPANY_DYQ_ACCOUNT.getCateCode());
			FundAcctVo accVodyq = this.findCompanyIntegralAcc(vo);
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
			List<FundBalanceVo> fundList = this.findCompanyBalance(fundVo);
			log.info("查询企业积分余额返回结果>>>>>>>"+JSON.toJSONString(fundList));
			List<FundBalanceVo> fundBalanceList = this.findIntegralDcByMemberId(querydto);
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
					tradeFlowService.insert(tradeFlowvo);
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
				tradeFlowService.insert(tradeFlowvo);
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
						tradeFlowService.insert(tradeFlowvo);
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
							TbFundTradeFlow tradeFlow = tradeFlowService.selectById(tradeFlowvoid);
						    tradeFlow.setTradeLastAmount(new BigDecimal(0).subtract(flowVo.getTradeAmount()));
						    tradeFlow.setTradeAmount(tradeFlow.getTradePreAmount().add(flowVo.getTradeAmount()));
						    tradeFlowService.updateById(tradeFlow);
						}
						TbFundBalance balance = fundBalanceService.selectById(balanceVoid);
						balance.setBalanceAmount(new BigDecimal(0).subtract(flowVo.getTradeAmount()));
						fundBalanceService.updateById(balance);
					}
				}
			}
			log.info("<<<<<<<<单次积分数据操作结束结果>>>>>>>"+JSON.toJSONString(r));
		}
		
		log.info("企业积分批量操作返回结果>>>>>"+JSON.toJSONString(dto));
		return r;
	}
	public List<FundBalanceVo> fundToActiveVoucherAccount(FundBalanceDto dto) {
		return this.baseMapper.fundToActiveVoucherAccount(dto);
	}

	@Override
	public boolean toActiveVoucherAccount(FundAcctDto dto) {
		return this.baseMapper.toActiveVoucherAccount(dto);
	}

	@Override
	@Transactional
	public R<Boolean> companyVoucherAcctSub(List<FundBalanceDto> dtoList){
		log.info("企业抵用券账户扣减的入参:{}",JSON.toJSONString(dtoList));
		R<Boolean> r=new R<Boolean>();
		List<String> codeList = dtoList.stream().map(FundBalanceDto::getBalanceCode)
				.collect(Collectors.toList());
		Wrapper<TbFundBalance> wrapper=new EntityWrapper<>();
		wrapper.in("balance_code", codeList);
		List<TbFundBalance> balanceList=fundBalanceService.selectList(wrapper);
		Map<String, TbFundBalance> balanceMap = balanceList.stream()
				.collect(Collectors.toMap(TbFundBalance::getBalanceCode,o -> o));
		
		List<TbFundTradeFlow> tradeFlowList=Lists.newArrayList();
		List<TbFundBalance> entityList=Lists.newArrayList();
		for (FundBalanceDto fundBalanceDto : dtoList) {
			
			TbFundBalance balance=balanceMap.get(fundBalanceDto.getBalanceCode());
		
			
			fundBalanceDto.setBalanceAmount(balance.getBalanceAmount());
			TbFundTradeFlow flow=loadTradeFlow(fundBalanceDto);
			tradeFlowList.add(flow);
			
			BigDecimal balanceAmount=balance.getBalanceAmount().subtract(fundBalanceDto.getTradeAmount());
			balance.setBalanceAmount(balanceAmount);
			//当剩余额度大于0
			if(balanceAmount.compareTo(new BigDecimal(0))==0) {
				balance.setStatus(BalanceStatus.USED_2.getCateCode());//改为已使用
			}else if(balanceAmount.compareTo(new BigDecimal(0))<0) {
				log.info("扣减额度超过剩余额度，数据有误!");
				throw new BaoxiaoException("扣减额度超过剩余额度，数据有误");
			}
			entityList.add(balance);
		}
		if(tradeFlowList.size()>0) {
			tradeFlowService.insertBatch(tradeFlowList);
			fundBalanceService.updateBatchById(entityList);
		}
	
		return r;
	}
	
	public TbFundTradeFlow  loadTradeFlow(FundBalanceDto fundBalanceDto) {
		TbFundTradeFlow flow=new TbFundTradeFlow();
		String jhCode=CodeUtils.genneratorShort("");
		flow.setTradeFlowCode(jhCode);
		
		flow.setTradeBusiCode(fundBalanceDto.getExtendAttrd());
		flow.setBusiModel("BIM00001");
		flow.setTradeBusiCate(OrderType.ORDER_TYPE_CONPANY_DYQ_DC.getCateCode());
		flow.setTransBusiCateName(OrderType.ORDER_TYPE_CONPANY_DYQ_DC.getCateName());
		flow.setBalanceCode(fundBalanceDto.getBalanceCode());
		flow.setTradeCate(TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode());
		flow.setTransCateName(TradeCateEnums.TRADE_CATE_DEDUCT.getCateName());
		flow.setTransActCate(ActionType.ACTION_TYPE_OUT.getCateCode());
		flow.setTransActName(ActionType.ACTION_TYPE_OUT.getCateName());
		flow.setTradePreAmount(fundBalanceDto.getBalanceAmount());
		flow.setTradeAmount(fundBalanceDto.getTradeAmount());
		flow.setTradeLastAmount(fundBalanceDto.getBalanceAmount().subtract(fundBalanceDto.getTradeAmount()));
		flow.setSource("扣减企业抵用券");
		flow.setRemark("企业抵用券账户扣减");
		flow.setStatus("0");
		if(StringUtils.isNotBlank(UserUtils.getUser())) {
			flow.setCreator(UserUtils.getUser());
			flow.setUpdator(UserUtils.getUser());
		}else {
			flow.setCreator("admin");
			flow.setUpdator("admin");
		}
		return flow;
	}

	@Override
	public List<FundBalanceVo> findWaitLoseVoucher(FundBalanceDto dto) {
		return this.baseMapper.findWaitLoseVoucher(dto);
	}

	@Override
	@Transactional
	public R<Boolean> addAcctActiveAccount(List<FundBalanceDto> dtoList) {
		R<Boolean> r=new R<Boolean>();
		log.info("员工抵用券账户待激活数量增加入参："+JSON.toJSONString(dtoList));
		Map<String,Integer> map=Maps.newHashMap();
		//根据人吧数量累加
		for (FundBalanceDto fundBalanceDto : dtoList) {
			if(map.get(fundBalanceDto.getMemberId())!=null) {
				map.put(fundBalanceDto.getMemberId(), map.get(fundBalanceDto.getMemberId())+fundBalanceDto.getActiveCount());
			}else {
				map.put(fundBalanceDto.getMemberId(),fundBalanceDto.getActiveCount());
			}
		}
		log.info("员工抵用需要的增加的激活券数量累计之后值："+JSON.toJSONString(map));
		List<String> memberIds =Lists.newArrayList(map.keySet());
	
		//根据人查询acct抵用券账户数据
		Wrapper<TbFundAcct> wrapper=new EntityWrapper<>();
		wrapper.in("member_id", memberIds);
		wrapper.eq("acct_cate", "10027");
		List<TbFundAcct> acList=fundAcctService.selectList(wrapper);
		List<TbFundAcct> upAcctList=Lists.newArrayList();
		for (TbFundAcct tbFundAcct : acList) {
			TbFundAcct upAcct=new TbFundAcct();
			upAcct.setId(tbFundAcct.getId());
			String toActiveCount=(Integer.parseInt(tbFundAcct.getToActiveCount())+map.get(tbFundAcct.getMemberId()))+"";
			log.info("当前member计算之后的数量为toActiveCount"+toActiveCount+"memberId="+tbFundAcct.getMemberId());
			upAcct.setToActiveCount(toActiveCount);
			upAcctList.add(upAcct);
		}
		log.info("需要增加的数据集合为"+JSON.toJSONString(upAcctList));
		if(upAcctList.size()>0) {
			fundAcctService.updateBatchById(upAcctList);
		}
		return r;
	}

	@Override
	public List<FundBalanceVo> findThreeWaitLoseVoucher(FundBalanceDto dto) {
		return this.baseMapper.findThreeWaitLoseVoucher(dto);
	}
}

