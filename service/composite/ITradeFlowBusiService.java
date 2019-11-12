/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  ITradeFlowBusiService.java   
 * @Package com.taolue.baoxiao.fund.service.composite   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 10, 2018 9:04:02 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite;

import java.util.List;

import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.api.dto.TradeFlowSearchParams;

/**   
 * @ClassName:  ITradeFlowBusiService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   Dec 10, 2018 9:04:02 PM   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface ITradeFlowBusiService {

	/**
	 * 
	 * <p>名称:类ITradeFlowBusiService中的findTradeFlows方法</br>    
	 * <p>描述:根据资金流水查询条件TradeFlowSearchParams类的对象查询资金流水</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Dec 19, 2018 2:55:04 AM</br>
	 * @throws Exception
	 * @param params TradeFlowSearchParams 资金流水查询参数对象
	 * <ul>
	 * 	<li>
	 * 		TradeFlowSearchParams.memberId String 流水归属会员编码 必传
	 *  </li>
	 *  <li>
	 * 		TradeFlowSearchParams.memberCate String 流水归属会员类型编码 可选
	 *  </li>
	 * </ul>
	 * @return List<FundTradeFlowDto> 查询结果
	 */
	List<FundTradeFlowDto> findTradeFlows(TradeFlowSearchParams params);
	/**
	 * 
	 * <p>名称:类ITradeFlowBusiService中的findTradeFlows方法</br>    
	 * <p>描述:根据资金流水查询条件TradeFlowSearchParams类的对象查询资金流水</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Dec 19, 2018 2:55:04 AM</br>
	 * @throws Exception
	 * @param params TradeFlowSearchParams 资金流水查询参数对象
	 * <ul>
	 * 	<li>
	 * 		TradeFlowSearchParams.memberId String 流水归属会员编码 必传
	 *  </li>
	 *  <li>
	 * 		TradeFlowSearchParams.memberCate String 流水归属会员类型编码 可选
	 *  </li>
	 * </ul>
	 * @return List<FundTradeFlowDto> 查询结果
	 */
	List<FundTradeFlowDto> findTradeFlowsRefund(TradeFlowSearchParams params);
	
	/**
	 * 
	 * <p>名称:类ITradeFlowBusiService中的findTradeFlowsPaged方法</br>    
	 * <p>描述:根据资金流水查询条件TradeFlowSearchParams类的对象分页查询资金流水</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Dec 19, 2018 2:55:04 AM</br>
	 * @throws Exception
	 * @param query Query<FundTradeFlowDto> 针对FundTradeFlowDto类型，含有分页信息的分页对象
	 * @param params TradeFlowSearchParams 资金流水查询参数对象
	 * <ul>
	 * 	<li>
	 * 		TradeFlowSearchParams.memberId String 流水归属会员编码 必传
	 *  </li>
	 *  <li>
	 * 		TradeFlowSearchParams.memberCate String 流水归属会员类型编码 可选
	 *  </li>
	 * </ul>
	 * @return Query<FundTradeFlowDto> 含有查询结果和分页信息的分页对象
	 */
	Query<FundTradeFlowDto> findTradeFlowsPaged(Query<FundTradeFlowDto> query, TradeFlowSearchParams params);
	/**
	 * 
	 * <p>名称:类ITradeFlowBusiService中的queryFlowsAndRefund方法</br>    
	 * <p>描述:根据资金流水查询条件TradeFlowSearchParams类的对象分页查询资金流水</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Dec 19, 2018 2:55:04 AM</br>
	 * @throws Exception
	 * @param query Query<FundTradeFlowDto> 针对FundTradeFlowDto类型，含有分页信息的分页对象
	 * @param params TradeFlowSearchParams 资金流水查询参数对象
	 * <ul>
	 * 	<li>
	 * 		TradeFlowSearchParams.memberId String 流水归属会员编码 必传
	 *  </li>
	 *  <li>
	 * 		TradeFlowSearchParams.memberCate String 流水归属会员类型编码 可选
	 *  </li>
	 * </ul>
	 * @return Query<FundTradeFlowDto> 含有查询结果和分页信息的分页对象包含退款记录
	 */
	Query<FundTradeFlowDto> queryFlowsAndRefund(Query<FundTradeFlowDto> query, TradeFlowSearchParams params);
}
