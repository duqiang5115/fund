package com.taolue.baoxiao.fund.service.composite.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BalanceStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.vo.FundVoucherBalanceVo;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.service.IFundVoucherService;
import com.taolue.baoxiao.fund.service.ITbFundBalanceService;
import com.taolue.baoxiao.fund.service.ITbFundTradeFlowService;
import com.taolue.baoxiao.fund.service.composite.IBalanceBusiService;
import com.xiaoleilu.hutool.collection.CollUtil;

import lombok.extern.slf4j.Slf4j;

/** 
* @author kwd
* @version 创建时间：2019年10月28日 下午6:06:07 
* @desc [类说明] 
*/
@Service
@Slf4j
public class FundVoucherServiceImpl implements IFundVoucherService {

	@Autowired
    private ITbFundBalanceService fundBalanceService;
	@Autowired
	private IBalanceBusiService balanceBusiService;
	@Autowired
	private ITbFundTradeFlowService tbFundTradeFlowService;
	@Override
	@Transactional(rollbackFor=Exception.class)
	public R<Boolean> verification(String memberId,String awardCouponId,String sourceFrom,String bizNo){
		R<Boolean> r = new R<Boolean>();
		FundVoucherBalanceVo dto = new FundVoucherBalanceVo();
		dto.setId(awardCouponId);
		dto.setMemberId(memberId);
		List<FundVoucherBalanceVo> fundVoucherBalanceList = balanceBusiService.findVoucherInfoByMemberId(dto);
		if(CollUtil.isEmpty(fundVoucherBalanceList)) {
			r.setCode(R.FAIL);
			r.setMsg("用户抵用券信息不正确");
			r.setData(false);
			return r;
		}
		FundVoucherBalanceVo vo = fundVoucherBalanceList.get(0);
		if(!vo.getStatus().equals(BalanceStatus.IS_SHOP_0.getCateCode())) {
			r.setCode(R.FAIL);
			r.setMsg("用户抵用券信息已使用");
			return r;
		}
		if(!vo.getExpireTime().after(new Date())) {
			log.error("用户抵用券已经过期");
			r.setCode(R.FAIL);
			r.setMsg("用户抵用券已经过期");
			return r;
		}
		Wrapper<TbFundBalance> wrapper = new EntityWrapper<TbFundBalance>();
		wrapper.eq("del_flag", CommonConstant.STATUS_NORMAL);
		wrapper.eq("biz_no", bizNo);
		List<TbFundBalance> resultList = this.fundBalanceService.selectList(wrapper);
		if(CollUtil.isNotEmpty(resultList)) {
			log.error("用户抵用券业务编号已经存在："+bizNo);
			r.setCode(R.FAIL);
			r.setMsg("用户抵用券核销时业务编号"+bizNo+"已经存在");
			return r;
		}
		
		TbFundBalance entity = new TbFundBalance();
		entity.setId(awardCouponId);
		entity.setStatus(BalanceStatus.USED_2.getCateCode());
		entity.setBizNo(bizNo);
		entity.setBizRemark("用户抵用券消费，消费业务编号："+bizNo);
		entity.setUpdator(memberId);
		boolean istrue = this.fundBalanceService.updateById(entity);
		if(!istrue) {
			r.setCode(R.FAIL);
			r.setMsg("用户抵用券信息已使用");
			r.setData(false);
			return r;
		}
		String tradeFlowCode = CommonConstant.KEY_PERFIX_TRADEFLOW_NO.concat(IdWorker.getIdStr());
		TbFundTradeFlow flow = new TbFundTradeFlow();
		flow.setTradeFlowCode(tradeFlowCode);
		/***核销业务编号***/
		flow.setTradeBusiCode(bizNo);
		flow.setTradeFlowOrder(new BigDecimal("0"));
		flow.setBalanceCode(vo.getBalanceCode());
		flow.setTradeCate(TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode());
		flow.setTransActCate(ActionType.ACTION_TYPE_OUT.getCateCode());
		flow.setTransCateName(TradeCateEnums.TRADE_CATE_DEDUCT.getCateName());
		flow.setTransActName(ActionType.ACTION_TYPE_OUT.getCateName());
		flow.setTradeBusiCate(OrderType.ORDER_TYPE_MEMBER_DYQ_CONSUME.getCateCode());
		flow.setTransBusiCateName(OrderType.ORDER_TYPE_MEMBER_DYQ_CONSUME.getCateName());
		/***抵用券面额id***/
		flow.setComposOprNo(awardCouponId);
		flow.setTradeAmount(vo.getBalanceAmount());
		flow.setTradePreAmount(vo.getBalanceAmount());
		flow.setTradeLastAmount(new BigDecimal("0"));
		flow.setStatus(CommonConstant.STATUS_NORMAL);
		flow.setDelFlag(CommonConstant.STATUS_NORMAL);
		flow.setSource(sourceFrom);
		flow.setCreator(memberId);
		flow.setUpdator(memberId);
		flow.setRemark("用户抵用券信息消费");
		boolean isok = this.tbFundTradeFlowService.insert(flow);
		if(!isok) {
			r.setCode(R.FAIL);
			r.setMsg("用户抵用券退券失败");
			r.setData(false);
			return r;
		}
		r.setData(true);
		return r;
	}
	@Override
	@Transactional(rollbackFor=Exception.class)
	public R<Boolean> rebuy(String memberId,String awardCouponId,String sourceFrom,String OldBizNo,String bizNo){
		R<Boolean> r = new R<Boolean>();
		FundVoucherBalanceVo dto = new FundVoucherBalanceVo();
		dto.setId(awardCouponId);
		dto.setMemberId(memberId);
		dto.setExtendAttra(OldBizNo);
		List<FundVoucherBalanceVo> fundVoucherBalanceList = balanceBusiService.findVoucherInfoByMemberId(dto);
		if(CollUtil.isEmpty(fundVoucherBalanceList)) {
			r.setCode(R.FAIL);
			r.setMsg("用户抵用券信息不正确");
			r.setData(false);
			return r;
		}
		FundVoucherBalanceVo vo = fundVoucherBalanceList.get(0);
		if(!vo.getStatus().equals(BalanceStatus.USED_2.getCateCode())) {
			r.setCode(R.FAIL);
			r.setMsg("未发现用户已使用抵用券信息");
			return r;
		}
		if(!vo.getExpireTime().after(new Date())) {
			log.error("用户抵用券已经过期");
			r.setCode(R.SUCCESS);
			r.setMsg("用户抵用券已经过期");
			rebuyTradeFlow(bizNo, OldBizNo, sourceFrom, vo);
			return r;
		}
		Wrapper<TbFundBalance> wrapper = new EntityWrapper<TbFundBalance>();
		wrapper.eq("del_flag", CommonConstant.STATUS_NORMAL);
		wrapper.eq("biz_no", bizNo);
		List<TbFundBalance> resultList = this.fundBalanceService.selectList(wrapper);
		if(CollUtil.isNotEmpty(resultList)) {
			log.error("用户抵用券业务编号已经存在："+bizNo);
			r.setCode(R.FAIL);
			r.setMsg("用户抵用券退券时业务编号"+bizNo+"已经存在");
			return r;
		}
		TbFundBalance entity = new TbFundBalance();
		entity.setId(awardCouponId);
		entity.setStatus(BalanceStatus.IS_SHOP_0.getCateCode());
		entity.setBizNo(bizNo);
		entity.setBizRemark("用户抵用券退券-退券编号："+bizNo);
		entity.setUpdator(memberId);
		boolean istrue = this.fundBalanceService.updateById(entity);
		if(!istrue) {
			log.error("退抵用券》用户抵用券退券失败》更新券状态失败");
			r.setCode(R.FAIL);
			r.setMsg("用户抵用券退券失败");
			r.setData(false);
			return r;
		}
		String tradeFlowCode = CommonConstant.KEY_PERFIX_TRADEFLOW_NO.concat(IdWorker.getIdStr());
		TbFundTradeFlow flow = new TbFundTradeFlow();
		flow.setTradeFlowCode(tradeFlowCode);
		/***退券业务编号***/
		flow.setTradeBusiCode(bizNo);
		flow.setTradeFlowOrder(new BigDecimal("0"));
		flow.setBalanceCode(vo.getBalanceCode());
		flow.setTradeCate(TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode());
		flow.setTransActCate(ActionType.ACTION_TYPE_IN.getCateCode());
		flow.setTransCateName(TradeCateEnums.TRADE_CATE_ADDUCT.getCateName());
		flow.setTransActName(ActionType.ACTION_TYPE_IN.getCateName());
		flow.setTradeBusiCate(OrderType.ORDER_TYPE_MEMBER_DYQ_REBUY.getCateCode());
		flow.setTransBusiCateName(OrderType.ORDER_TYPE_MEMBER_DYQ_REBUY.getCateName());
		/***抵用券面额id***/
		flow.setComposOprNo(awardCouponId);
		flow.setTradeAmount(vo.getBalanceAmount());
		flow.setTradePreAmount(new BigDecimal("0"));
		flow.setTradeLastAmount(vo.getBalanceAmount());
		flow.setStatus(CommonConstant.STATUS_NORMAL);
		flow.setDelFlag(CommonConstant.STATUS_NORMAL);
		flow.setRemark("用户抵用券信息消费退券，核销业务编号-"+OldBizNo);
		flow.setCreator(memberId);
		flow.setUpdator(memberId);
		boolean isok = this.tbFundTradeFlowService.insert(flow);
		if(!isok) {
			r.setCode(R.FAIL);
			r.setMsg("用户抵用券退券失败");
			r.setData(false);
			return r;
		}
		r.setData(true);
		return r;
	}
	/****
	 * @Title FundVoucherServiceImpl.rebuyTradeFlow
	 * @Description: 用户退抵用券-券过期交易记录
	 *
	 * @param bizNo
	 * @param OldBizNo
	 * @param sourceFrom
	 * @param vo
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-10-30 13:53:12  修改内容 :
	 */
	private void rebuyTradeFlow(String bizNo,String OldBizNo,String sourceFrom,FundVoucherBalanceVo vo) {
		new Thread(()->{
			String tradeFlowCode = CommonConstant.KEY_PERFIX_TRADEFLOW_NO.concat(IdWorker.getIdStr());
			TbFundTradeFlow flow = new TbFundTradeFlow();
			flow.setTradeFlowCode(tradeFlowCode);
			flow.setTradeBusiCode(bizNo);
			flow.setTradeFlowOrder(new BigDecimal("0"));
			flow.setBalanceCode(vo.getBalanceCode());
			flow.setTradeCate(TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode());
			flow.setTransActCate(ActionType.ACTION_TYPE_IN.getCateCode());
			flow.setTransCateName(TradeCateEnums.TRADE_CATE_ADDUCT.getCateName());
			flow.setTransActName(ActionType.ACTION_TYPE_IN.getCateName());
			flow.setTradeBusiCate(OrderType.ORDER_TYPE_MEMBER_DYQ_REBUY.getCateCode());
			flow.setTransBusiCateName(OrderType.ORDER_TYPE_MEMBER_DYQ_REBUY.getCateName());
			/***抵用券面额id***/
			flow.setComposOprNo(vo.getId());
			flow.setTradeAmount(vo.getBalanceAmount());
			flow.setTradePreAmount(new BigDecimal("0"));
			flow.setTradeLastAmount(new BigDecimal("0"));
			flow.setStatus(CommonConstant.STATUS_NORMAL);
			flow.setDelFlag(CommonConstant.STATUS_NORMAL);
			//flow.setComposOprNo(OldBizNo);//原始抵用券消费总部业务编码
			flow.setSource(sourceFrom);
			flow.setRemark("用户抵用券信息消费退券-抵用券已过期，核销业务单号："+OldBizNo);
			flow.setCreator(vo.getMemberId() == null? "":vo.getMemberId());
			flow.setUpdator(vo.getMemberId() == null? "":vo.getMemberId());
			this.tbFundTradeFlowService.insert(flow);
		});
	}
}
