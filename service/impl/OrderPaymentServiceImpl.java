package com.taolue.baoxiao.fund.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.fund.api.dto.OrderPaymentDto;
import com.taolue.baoxiao.fund.api.vo.OrderPaymentVo;
import com.taolue.baoxiao.fund.api.vo.OrderVendorIndustryVo;
import com.taolue.baoxiao.fund.entity.OrderPayment;
import com.taolue.baoxiao.fund.mapper.OrderPaymentMapper;
import com.taolue.baoxiao.fund.service.IOrderPaymentService;

/**
 * <p>
 * 订单的支付信息，一笔支付可以由多种支付方式可以有，现金支付，优惠券支付，消费券支付，优惠减免，福豆等方式，也可以是上述多种组合支付，因此单独建立一张子表记录支付情况 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2018-08-22
 */
@Service
public class OrderPaymentServiceImpl extends ServiceImpl<OrderPaymentMapper, OrderPayment> implements IOrderPaymentService {

	@Override
	public List<OrderPaymentVo> showOrderPaymentByParams(Map<String, Object> params) {
		
		List<OrderPayment> paymentList=this.baseMapper.selectOrderPaymentByParams(params);
		List<OrderPaymentVo> dtlList = new ArrayList<OrderPaymentVo>();
		if(paymentList!=null && paymentList.size()>0) {
			for (OrderPayment orderPayment : paymentList) {
				OrderPaymentVo vo=new OrderPaymentVo();
				BeanUtils.copyProperties(orderPayment, vo);
				dtlList.add(vo);
			}
		}
		
		return dtlList;
	}
	@Override
	public List<OrderPaymentVo> showGroupOrderPaymentByParams(Map<String, Object> params) {
		
		List<OrderPayment> paymentList=this.baseMapper.selectGroupOrderPaymentByParams(params);
		List<OrderPaymentVo> dtlList = new ArrayList<OrderPaymentVo>();
		if(paymentList!=null && paymentList.size()>0) {
			for (OrderPayment orderPayment : paymentList) {
				OrderPaymentVo vo=new OrderPaymentVo();
				BeanUtils.copyProperties(orderPayment, vo);
				dtlList.add(vo);
			}
		}
		
		return dtlList;
	}
	
	@Override
	public List<OrderPaymentVo> selectOrderPaymentByBusiModle(Map<String, Object> params) {
		
		List<OrderPayment> paymentList=this.baseMapper.selectOrderPaymentByBusiModle(params);
		List<OrderPaymentVo> dtlList = new ArrayList<OrderPaymentVo>();
		if(paymentList!=null && paymentList.size()>0) {
			for (OrderPayment orderPayment : paymentList) {
				OrderPaymentVo vo=new OrderPaymentVo();
				BeanUtils.copyProperties(orderPayment, vo);
				dtlList.add(vo);
			}
		}
		
		return dtlList;
	}
	
	@Override
	public boolean updateBillPaymentNo(Map<String,Object> params) {
		return this.baseMapper.updateBillPaymentNo(params);
	}
	@Override
	public boolean updateBillNo(Map<String, Object> params) {
		return this.baseMapper.updateBillNo(params);
	}
	@Override
	public List<OrderVendorIndustryVo> selectOrderVendor(Map<String, Object> params) {
		return this.baseMapper.selectOrderVendor(params);
	}
	@Override
	public List<OrderVendorIndustryVo> selectOrderIndustry(Map<String, Object> params) {
		return this.baseMapper.selectOrderIndustry(params);
	}
	@Override
	public List<OrderPaymentVo> selectOrderIndustryByVendor(Map<String, Object> params) {
		List<OrderPayment> paymentList=this.baseMapper.selectOrderIndustryByVendor(params);
		List<OrderPaymentVo> dtlList = new ArrayList<OrderPaymentVo>();
		if(paymentList!=null && paymentList.size()>0) {
			for (OrderPayment orderPayment : paymentList) {
				OrderPaymentVo vo=new OrderPaymentVo();
				BeanUtils.copyProperties(orderPayment, vo);
				dtlList.add(vo);
			}
		}
		
		return dtlList;
	}
	@Override
	public List<OrderPayment> selectOrderGroup(Map<String, Object> params) {
		return this.baseMapper.selectOrderGroup(params);
	}
	@Override
	public boolean updateInternalVendor(Map<String, Object> params) {
		return this.baseMapper.updateInternalVendor(params);
	}
	@Override
	public List<OrderPaymentVo> selectOrderBillByParams(Map<String, Object> params) {
		return this.baseMapper.selectOrderBillByParams(params);
	}
	@Override
	public List<OrderPaymentVo> selectOrderPaymentByIds(OrderPaymentDto dto) {
		return this.baseMapper.selectOrderPaymentByIds(dto);
	}
}
