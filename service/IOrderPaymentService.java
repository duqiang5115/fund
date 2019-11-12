package com.taolue.baoxiao.fund.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.fund.api.dto.OrderPaymentDto;
import com.taolue.baoxiao.fund.api.vo.OrderPaymentVo;
import com.taolue.baoxiao.fund.api.vo.OrderVendorIndustryVo;
import com.taolue.baoxiao.fund.entity.OrderPayment;

/**
 * <p>
 * 订单的支付信息，一笔支付可以由多种支付方式可以有，现金支付，优惠券支付，消费券支付，优惠减免，福豆等方式，也可以是上述多种组合支付，因此单独建立一张子表记录支付情况 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2018-08-22
 */
public interface IOrderPaymentService extends IService<OrderPayment> {
	
	/**
	 * 
	 *
	 * @Title IOrderPaymentService.showOrderPaymentByParams
	 * @Description: 订单对应的订单支付信息
	 *
	 * @param params
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 * 修改历史: 
	 * 修改人: Michael.Zhang, 修改日期 : 2018年08月27日 下午7:06:32
	 * 修改内容 :
	 */
	List<OrderPaymentVo> showOrderPaymentByParams(Map<String,Object> params);

	/**
	 * 根据商户，企业查询已收票的账单状态
	 * @param params
	 * @return
	 */
	List<OrderPaymentVo>  selectOrderBillByParams(Map<String,Object> params);
	/**
	 * 根据订单号修改订单明细的账单编号
	 *
	 * @Title IOrderPaymentService.updateBillPaymentNo
	 * @Description: TODO
	 *
	 * @param params
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 */
	public boolean updateBillPaymentNo(Map<String,Object> params);
	
	public boolean updateBillNo(Map<String,Object> params);
	boolean updateInternalVendor(Map<String,Object> params);
	
	/**
	 * 
	 *
	 * @Title IOrderPaymentService.showGroupOrderPaymentByParams
	 * @Description: 根据账单查询订单明细，按模式分组
	 *
	 * @param params
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 */
	public List<OrderPaymentVo> showGroupOrderPaymentByParams(Map<String, Object> params) ;
		
	/**
	 * 一个账单对应模式下的订单明细
	 *
	 * @Title IOrderPaymentService.selectOrderPaymentByBusiModle
	 * @Description: TODO
	 *
	 * @param params
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 */
	public List<OrderPaymentVo> selectOrderPaymentByBusiModle(Map<String, Object> params) ;
	
	
	/**
	 * 
	 *
	 * @Title OrderPaymentMapper.selectOrderVendor
	 * @Description: 消费卷消费对应的商户
	 *
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 */
	List<OrderVendorIndustryVo> selectOrderVendor(Map<String, Object> params);
	
	/**
	 * 
	 *
	 * @Title OrderPaymentMapper.selectOrderIndustry
	 * @Description: 消费卷对应的行业
	 *
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 */
	List<OrderVendorIndustryVo> selectOrderIndustry(Map<String, Object> params);
	
	public List<OrderPaymentVo> selectOrderIndustryByVendor(Map<String, Object> params) ;
	List<OrderPayment> selectOrderGroup(Map<String,Object> params);
	
	/**
	 * 
	 * @Title: selectOrderPaymentByIds   
	 * @Description: 通过id查询订单明细  
	 * @param: @param dto
	 * @param: @return      
	 * @return: List<OrderPaymentVo>      
	 * @throws
	 */
	List<OrderPaymentVo>  selectOrderPaymentByIds(OrderPaymentDto dto);
}
