package com.taolue.baoxiao.fund.service.impl;

import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderBusiStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderDetailStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.PaymentStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TransType;
import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.dto.FundRechargeDTO;
import com.taolue.baoxiao.fund.api.dto.FundWithDrawDTO;
import com.taolue.baoxiao.fund.api.dto.OrderDTO;
import com.taolue.baoxiao.fund.api.dto.OrderDetailDTO;
import com.taolue.baoxiao.fund.api.vo.FundRechargeVo;
import com.taolue.baoxiao.fund.api.vo.FundWithDrawVo;
import com.taolue.baoxiao.fund.entity.FundRecharge;
import com.taolue.baoxiao.fund.mapper.FundRechargeMapper;
import com.taolue.baoxiao.fund.mapper.FundWithdrawMapper;
import com.taolue.baoxiao.fund.service.IFundRechargeService;
import com.taolue.baoxiao.fund.service.IOrderService;
import com.taolue.baoxiao.fund.service.composite.IPersonalUserService;

import lombok.extern.slf4j.Slf4j;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.StringUtils;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 * 充值表 服务实现类
 * </p>
 *
 * @author duqiang
 * @since 2019-03-06
 */
@Service
@Slf4j
public class FundRechargeServiceImpl extends ServiceImpl<FundRechargeMapper, FundRecharge> implements IFundRechargeService {
	
	@Autowired
	private FundWithdrawMapper fundWithdrawMapper;
	@Autowired
	private IOrderService iOrderService;
	@Autowired
	private IPersonalUserService iPersonalUserService;
	
	@Override
	public Page<FundRechargeVo> queryRechargePage(Query query, FundRechargeDTO dto) {
		query.setRecords(this.baseMapper.queryRechargePage(query, dto));
		return query;
	}

	@Override
	public Page<FundWithDrawVo> queryWithdrawPage(Query query, FundWithDrawDTO dto) {
		query.setRecords(fundWithdrawMapper.queryWithDrawPage(query, dto));
		return query;
	}

	@Override
	public boolean editRecharge(FundRechargeDTO dto) {
		FundRecharge recharge=baseMapper.selectById(dto.getId());
		if(!ObjectUtils.isEmpty(recharge)) {
			
			OrderDTO orderdto=new OrderDTO();
			orderdto.setOrderNo(recharge.getFromCode());
			String status="";
			if(dto.getState().equals(PaymentStatus.PAYMENT_SUCCESS.getCateCode())) {
				status=OrderBusiStatus.PAYMENT_SUCCESS.getCateCode();
				AssignDto assignDto=new AssignDto();
				assignDto.setMemberId(recharge.getMemberId());
				assignDto.setOrderAmount(recharge.getAmount());
				assignDto.setCompanyId(AcctCateEnums.ACCT_CATE_PTQYX.getCateMgn());
				assignDto.setBusiOrderNo(recharge.getCode());
				assignDto.setOrderType(TransType.RECHARGE.getSysCode());
				assignDto.setSource(recharge.getSource());
				assignDto.setBillItemCate(recharge.getRechargeType());
				log.info("充值调用资金入参参数：{}",JSON.toJSONString(assignDto));
				boolean reBool=iPersonalUserService.rechargeBusiness(assignDto);
				log.info("充值调用资金返回参数：{}",reBool);
			}else if(dto.getState().equals(PaymentStatus.PAYMENT_FAIL.getCateCode())){
				status=OrderBusiStatus.PAYMENT_FAIL.getCateCode();
			}else {
				log.info("修改充值状态不对");
				throw new BaoxiaoException("修改充值状态不对");
			}
			orderdto.setStatus(status);
			//修改订单状态
			iOrderService.uodateOrderStatus(orderdto);
			FundRecharge newrecharge=new FundRecharge();
			BeanUtils.copyProperties(dto, newrecharge);
			this.baseMapper.updateById(newrecharge);
		}
		return true;
	}

	@Override
	public FundRechargeVo addRecharge(FundRechargeDTO dto) {
	
		//dto.setAmount(dto.getAmount().multiply(new BigDecimal(1000)));
		dto.setCreator(dto.getMemberId());
		dto.setUpdator(dto.getMemberId());
		FundRecharge recharge=new FundRecharge();
		BeanUtils.copyProperties(dto, recharge);
		String code=CodeUtils.genneratorShort("CZ");
		dto.setCode(code);
		recharge.setCode(code);
		recharge.setDelFlag("0");
		addCZOrder(dto);
		//recharge.setFromCode(orderNo);
		this.baseMapper.insert(recharge);
		//RedisTemplate<String,Object> redis=new RedisTemplate<String,Object>();
		//redis.expire(recharge.getId(),30 , TimeUnit.MINUTES);//设置过期时间 
		FundRechargeVo vo=new FundRechargeVo();
		BeanUtils.copyProperties(recharge, vo);
		return vo;
	}
	public String addCZOrder(FundRechargeDTO dto) {
		OrderDTO order=new OrderDTO();
		String orderNo=CodeUtils.genneratorShort("CZ");
		order.setOrderCode(dto.getCode());
		order.setOrderNo(orderNo);
		order.setBusinessId("");
		order.setBusinessName("");
		order.setStatus(OrderBusiStatus.PENDING_PAYMENT.getCateCode());
		order.setCompanyId(AcctCateEnums.ACCT_CATE_PTQYX.getCateMgn());
		order.setCompanyName(AcctCateEnums.ACCT_CATE_PTQYX.getCateName());
		order.setMainType(TransType.RECHARGE.getSysCode());
		order.setMainTypeName(TransType.RECHARGE.getSysName());
		order.setSubType(TransType.RECHARGE.getSysCode());
		order.setSubTypeName(TransType.RECHARGE.getSysName());
		order.setOrderAmount(dto.getAmount());
		order.setPayAmount(dto.getAmount());
		order.setCouponAmount(new BigDecimal(0));
		order.setMemberId(dto.getMemberId());
		List<OrderDetailDTO> detailDtoList=Lists.newArrayList();
		OrderDetailDTO detail=new OrderDetailDTO();
		//现金类型
		detail.setPaymentCate(BillItemSubCate.BILL_ITEM_SUBCATE_XFXJ.getCateCode());
		detail.setBusiModle(BusiModelEnums.BUSI_MODEL_NONE.getCateCode());
		detail.setPaymentMemberCate(MemberCateEnums.MEMBER_CATE_ALO.getCateCode());
		detail.setPaymentAcctCate(MemberCateEnums.MEMBER_CATE_ALO.getCateCode()+AcctCateEnums.ACCT_CATE_MASTER.getCateCode());
		detail.setPaymentItemNo("");
		detail.setPaymentItemName("");
		detail.setStatus(OrderDetailStatus.PROCESS.getCateCode());
		detail.setPaymentMemberId(dto.getMemberId());
		detail.setPaymentAmount(dto.getAmount());
		detail.setPaymentVendorId("");
		detail.setPaymentVendorName("");
		detail.setRemark(TransType.RECHARGE.getSysName()+"-"+AcctCateEnums.PLANTFORM_ACCT_CATE_PTXJ.getCateName());
		detail.setPaymentIndustryId(AcctCateEnums.PLANTFORM_ACCT_CATE_PTXJ.getCateCode());//行业
		detail.setPaymentIndustryName(AcctCateEnums.PLANTFORM_ACCT_CATE_PTXJ.getCateName());
		detailDtoList.add(detail);
		order.setDetailDtoList(detailDtoList);
		log.info("添加购买订单的入参参数:{}",JSON.toJSONString(order));
		iOrderService.addOrder(order);	
		
		return orderNo;
	}

}
