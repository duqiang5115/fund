package com.taolue.baoxiao.fund.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.OrderBusiComposeDto;
import com.taolue.baoxiao.fund.api.dto.OrderBusiDto;
import com.taolue.baoxiao.fund.api.dto.OrderBusiQueryDto;
import com.taolue.baoxiao.fund.api.dto.OrderBusiReturnDto;
import com.taolue.baoxiao.fund.api.vo.OrderBusiVo;
import com.taolue.baoxiao.fund.entity.OrderBusi;

/**
 * <p>
 * 订单信息主表，记录订单的基本信息； 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2018-08-22
 */
public interface IOrderBusiService extends IService<OrderBusi> {
	/**
	 * billNo
	 * id
	 * @Title: updateBillNo   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   2018年8月26日 下午9:27:09  
	 * @param: @param params
	 * @param: @return      
	 * @return: boolean      
	 * @throws
	 */
	boolean updateBillNo(Map<String,Object> params);
	
	/**
	 * 
	 *
	 * @Title IOrderBusiService.showOrderBusiByParams
	 * @Description: TODO
	 *
	 * @param params
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 * 修改历史: 
	 * 修改人: Michael.Zhang, 修改日期 : 2018年08月27日 下午6:22:17
	 * 修改内容 :
	 */
	List<OrderBusiVo> showOrderBusiByParams(Map<String,Object> params);
	
	/**
	 * 
	 *
	 * @Title IOrderBusiService.addOrder
	 * @Description: 添加订单信息，订单支付信息，订单客户信息
	 *
	 * @param params
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 */
	boolean addOrder(Map<String,Object> params);
	
	public boolean addOrderBusi();
	
	
	public Page<OrderBusiReturnDto> queryOrderBusi(Query query,OrderBusiQueryDto dto);
	
	public Page<OrderBusiReturnDto> queryOrderBusiLZ(Query query, OrderBusiQueryDto dto);
	
	boolean updateOrderStatus(Map<String,Object> params);
	 /**
	    * 查询没有生成子订单的信息
	    * @return
	    */
	boolean selectOrderBusiNoSon();

	public boolean addOrder(OrderBusiDto dto);


}
