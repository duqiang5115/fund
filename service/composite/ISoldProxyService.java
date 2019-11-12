/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  IAccountService.java   
 * @Package com.taolue.baoxiao.fund.service   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   2018年8月28日 上午10:39:33   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.common.exception.FundServiceException;
import com.taolue.baoxiao.fund.entity.Order;

/**   
 * @ClassName:  ISoldProxyService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月28日 上午10:39:33   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface ISoldProxyService {
	
	boolean rechargeRound(AssignDto assignDto);
	
	boolean soldPorxySuccess(AssignDto assignDto);
	
	/**
	 * <p>名称:类SoldProxyServiceImpl中的findSoldingAndSoledProxyAmounts方法</br>    
	 * <p>描述: 按会员ID查询当前系统中处于代卖中和已经代卖完成的额度</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 11, 2019 3:03:08 PM</br>
	 * @param memberAssginDtos {@link List}{@literal <}{@link String}{@literal >} 查询参数
	 * <ul>
	 * 	  <li>{@link AssignDto#memberId} 类型：{@link String} 会员编号</li>
	 * 	  <li>{@link AssignDto#guid} 类型：{@link String} 会员第三方编号</li>
	 * </ul>
	 * @return {@link Map}{@literal <}{@link String},{@link AssignDto}{@literal >} 自动代卖额度数据{@link Map}对象；
	 * <ul>
	 * 	<li>{@link Map}的key->{@link String}对象，为会员id</li>
	 *  <li>{@link Map}的value->{@link AssignDto}对象，为该会员当前已用和使用中的代卖额度数据对象：其属性为</li>
	 *  <ul>
	 *    <li>{@link AssignDto#memberId} 类型：{@link String} 会员编号</li>
	 *    <li>{@link AssignDto#guid} 类型：{@link String} 会员第三方编号</li>
	 *    <li>{@link AssignDto#orderAmount} 类型：{@link BigDecimal} 会员已经使用的自动代卖额度</li>
	 *    <li>{@link AssignDto#couponAmount} 类型：{@link BigDecimal} 会员使用中的自动代卖额度</li>
	 *  </ul>
	 * </ul>
	 * 该返回对象可以为空；表示没有任何已使用和使用中的自动代卖额度
	 * @throws FundServiceException 
	 * <ul>
	 * 	<li>异常代码{@link int}->503；异常信息->查询当前已使用和使用中的自动代卖额度出现错误</li>
	 * </ul>
	 */
	Map<String, AssignDto> findSoldingAndSoledProxyAmounts(List<AssignDto> memberAssginDtos) throws FundServiceException;
	
	/**
	 * 
	 * <p>名称:类ISoldProxyService中的getLeftCouponAmounts方法</br>    
	 * <p>描述: 查询指定会员消费券可用总额</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 21, 2019 3:46:21 PM</br>
	 * @throws Exception
	 * @param memberIds {@link List}{@literal <}{@link String}{@literal >} 需要查询的会员编号
	 * @return {@link List}{@literal <}{@link FundBalanceDto}{@literal >} 结果对象
	 */
	List<FundBalanceDto> getLeftCouponAmounts(List<String> memberIds);
	
	void doTask(AssignDto assignDto);
	
//	void processTradeFlows(OrderApply orderApply);
	
	void complateSoldProxyOrder(Order soldOrder);
}
