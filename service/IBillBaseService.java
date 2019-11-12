package com.taolue.baoxiao.fund.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.BillBaseDto;
import com.taolue.baoxiao.fund.api.vo.BillBaseVo;
import com.taolue.baoxiao.fund.entity.BillBase;

/**
 * 
 * @ClassName:  IBillBaseService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月24日 下午3:32:17   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IBillBaseService extends IService<BillBase> {
	/**
	 * 
	 * @Title: findBillBasePageByParams   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   2018年8月24日 下午3:32:07  
	 * @param: @param page
	 * @param: @param size
	 * @param: @param queryParams
	 * @param: @return      
	 * @return: Page<BillBase>      
	 * @throws
	 */
	public Page<BillBase> findBillBasePageByParams(int page, int size, BillBaseDto queryParams);
	
	public Page<BillBase> queryBillBasePageByParams(Query query, BillBaseDto queryParams);
	
	/**
	 * 判断是否有未结清订单
	 * @Title: hasUnfinishBill   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   2018年8月26日 下午8:40:26  
	 * @param: @param companyId
	 * @param: @return      
	 * @return: boolean  true 有，false 无
	 * @throws
	 */
	public boolean hasUnfinishBill(String companyId);
	
	/**
	 * 选择某公司下，一定时间范围内订单，生成对应账单，
	 * @Title: creaetBillBaseByOrders   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   2018年8月26日 下午9:32:37  
	 * @param: @param orderBegin 订单生成时间下限
	 * @param: @param orderEnd 订单生成时间下限
	 * @param: @param companyId 订单归属公司
	 * @param: @return      
	 * @return: boolean      
	 * @throws
	 */
	public String creaetBillBaseByOrders(Date orderBegin, Date orderEnd, String companyId,String busiModle) ;
	
	public void convertAndSendBillMessage(String billNo,String billNos);
	
	/**
	 * 
	 *
	 * @Title IBillBaseService.quer
	 * @Description: 查询平台没缴清的单子
	 *
	 * @param queryParams
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 */
	public List<BillBaseVo> queryUnclearedBill(BillBaseDto queryParams);
	
	
	/**
	 * 根据主张单编号，修改子账单的是否删除标志
	 *
	 * @Title IBillBaseService.updateBillBusiDelFlag
	 * @Description: TODO
	 *
	 * @param map
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 */
	boolean updateBillBusiDelFlag(Map<String,Object> map);
	
	/**
	 * 预生成账单
	 *
	 * @Title IBillBaseService.preBillBaseByOrders
	 * @Description: TODO
	 *
	 * @param orderBegin
	 * @param orderEnd
	 * @param companyId
	 * @param busiModle
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 */
	public BillBaseVo preBillBaseByOrders(Date orderBegin, Date orderEnd, String companyId,String busiModle) ;
	
	/**
	 * 刷新逾期费用
	 * @return
	 */
	public boolean  overdueAmountJobTask();
	
	/**
	 * 定时创建账单
	 * @param companyId
	 * @param busiModle
	 * @return
	 */
	public String  createBillJobTask(String companyId,String busiModle);
	
	

	 /**
	  * 
	  * @param dto
	  * @return
	  */
	public BillBase  queryBaseByModel (String companyId,String busiModle,Date beginTime) ;
}
