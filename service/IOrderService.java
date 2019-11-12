package com.taolue.baoxiao.fund.service;

import java.util.List;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.BillSettleAccountsDto;
import com.taolue.baoxiao.fund.api.dto.OrderDTO;
import com.taolue.baoxiao.fund.api.dto.OrderListDto;
import com.taolue.baoxiao.fund.api.dto.QueryOrderListDto;
import com.taolue.baoxiao.fund.api.dto.UseOrderDto;
import com.taolue.baoxiao.fund.api.vo.OrderDetailVo;
import com.taolue.baoxiao.fund.api.vo.OrderListVo;
import com.taolue.baoxiao.fund.api.vo.OrderVo;
import com.taolue.baoxiao.fund.entity.Order;

/**
 * <p>
 * 订单信息交易主表； 服务类
 * </p>
 *
 * @author duqiang
 * @since 2019-03-08
 */
public interface IOrderService extends IService<Order> {
	
	/**
	 * 
	 * @Title: addOrder   
	 * @Description: 添加订单 
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: boolean      
	 * @throws
	 */
	public boolean addOrder(OrderDTO dto);


	/**
	 * 根据成员id，消费券id 获取订单列表
	 * @param orderDTO
	 * @return
	 */
	public R<List<OrderVo>> queryOrderInfo(OrderDTO orderDTO);


	/**
	 * 根据成员id，订单id集合查询订单
	 * @param orderDTO
	 * @return
	 */
	public R<List<OrderVo>> queryOrderList(OrderDTO orderDTO);



	/**
	 * 根据成员id，订单id集合查询订单明细
	 * @param orderDto
	 * @return
	 */
	public R<List<OrderDetailVo>>  queryOrderDetailList(OrderDTO orderDto);
	/**
	 * 根据成员id，订单id集合查询订单明细
	 * @param orderDto
	 * @return
	 */
	public R<List<OrderDetailVo>>  queryOrderDetailListByH5(OrderDTO orderDto);


	/**
	 * 
	 * @Title: uodateOrderStatus   
	 * @Description: 修改订单状态
	 * @param: @param orderDto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: boolean      
	 * @throws
	 */
	public boolean  uodateOrderStatus(OrderDTO orderDto);


	/**
	 * 
	 * @Title: queryOrderList   
	 * @Description: 查询订单列表
	 * @param: @param query
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: Page<OrderVo>      
	 * @throws
	 */
	public Page<OrderVo> queryOrderPageList(Query query,OrderDTO orderDto);
	
	/**
	 * 
	 * @Title: queryOrderDetailList   
	 * @Description: 查询订单明细 
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: List<OrderDetailVo>      
	 * @throws
	 */
	public List<OrderDetailVo> getOrderDetailList(OrderDTO orderDto);
	
	/**
     * 
     * @Title: queryUnCompleteMember   
     * @Description:查询代卖未完成去重的memberid
     * @param: @return    
     * @author: duqiang     
     * @return: List<OrderVo>      
     * @throws
     */
    public  List<OrderVo>  queryUnCompleteMember();
	 /**
     * 查询提现订单信息
     * @Title: getTxInfo   
     * @Description: TODO(这里用一句话描述这个方法的作用)   
     * @param: @param orderDTO
     * @param: @return    
     * @author: duqiang     
     * @return: OrderVo      
     * @throws
     */
    OrderVo getTxInfo(OrderDTO orderDTO);
    /**
     * 查询订单信息
     * @Title: getOrderInfo   
     * @Description: TODO(这里用一句话描述这个方法的作用)   
     * @param: @param orderDTO
     * @param: @return    
     * @author: duqiang     
     * @return: OrderVo      
     * @throws
     */
   public OrderVo getOrderInfo(OrderDTO OrderDTO);
    
   R<OrderVo> getInvoiceOrder(OrderDTO orderDTO);
	/**
	 * 
	 * 
	 * @Title IOrderService.queryOrderListByH5
	 * @Description: h5查询订单列表
	 *
	 * @param query
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年05月29日 上午11:28:29
	 * 修改内容 :
	 */
	public List<OrderListDto> queryOrderListByH5(OrderListDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderService.queryOrderByUseBuy
	 * @Description: 查询【购买】和【消费】类型的订单
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年05月29日 下午4:40:11
	 * 修改内容 :
	 */
	public List<OrderListDto> queryOrderByUseBuy(OrderListDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderService.addUseOrder
	 * @Description: 添加消费类型的订单
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年05月29日 下午5:01:40
	 * 修改内容 :
	 */
	public boolean addUseOrder(UseOrderDto dto)throws Exception;
    /**
     * 
     * @Title: editOrderStatus   
     * @Description: 批量修改订单状态
     * @author: zyj
     * 
     * @date:   2019年6月4日 下午6:17:04  
     * @param: @param orderDto
     * @param: @return      
     * @return: boolean      
     * @throws
     */
	public boolean  editOrderStatus(OrderDTO orderDto);
	
	
	/**
	 * 
	 * 
	 * @Title IOrderService.addUseOrderNew
	 * @Description: 添加消费类型的订单-新
	 *
	 * @param dto
	 * @return
	 * @throws Exception
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月20日 下午2:24:37
	 * 修改内容 :
	 */
	public boolean addUseOrderNew(UseOrderDto dto)throws Exception;
	
	
	public List<OrderVo> showOrderByNoDesc(OrderDTO dto);


	/**
	 * 根据订单编号查询订单信息
	 * @param orderDto
	 * @return
	 */
	public R<List<OrderListDto>> queryOrderListByOrderNo(OrderDTO orderDto);
	
	/**
	 * 
	 * 
	 * @Title IOrderService.findOrderListPage
	 * @Description: 后台用：查询订单列表（消费、购买、报销）订单
	 *
	 * @param query
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年09月23日 下午6:49:53
	 * 修改内容 :
	 */
	public Page<OrderListVo> findOrderListPage(Query query,QueryOrderListDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderService.findOrderDetailByOrderNo
	 * @Description: 查询订单详情
	 *
	 * @param orderNo
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年09月27日 下午4:11:14
	 * 修改内容 :
	 */
	public OrderListVo findOrderDetailByOrderNo(String orderNo);
	/**
	 * 
	 * 
	 * @Title IOrderService.findOrderListByExport
	 * @Description: 后台导出Excel-员工订单列表查询
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年10月08日 下午2:48:20
	 * 修改内容 :
	 */
	public List<OrderListVo> findOrderListByExport(QueryOrderListDto dto);
}
