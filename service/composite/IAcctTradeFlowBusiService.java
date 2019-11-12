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

import java.util.List;

import com.google.common.collect.Multimap;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.BalanceFlowDetailDTO;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.api.dto.TradeFlowSearchParams;


/**   
 * @ClassName:  IAccountService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月28日 上午10:39:33   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IAcctTradeFlowBusiService {

	/**
	 * 
	 * <p>名称:类IAcctTradeFlowBusiService中的findTradeFlowsPaged方法</br>    
	 * <p>描述:分页查询企业现金账户/企业后付费额度账户的资金流水</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Dec 19, 2018 9:49:26 AM</br>
	 * @throws Exception
	 * @param queryParams BalanceFlowDetailDTO 调用方查询参数
	 * <ul>
	 * <li>BalanceFlowDetailDTO.currentPage Integer 当前页码</li>
	 * <li>BalanceFlowDetailDTO.pageSize Integer 每页记录数</li>
	 * <li>BalanceFlowDetailDTO.memberId String 资金归属的会员编号 必传</li>
	 * <li>BalanceFlowDetailDTO.paramCode String 查询编号（对应查询配置表中的编号）必传<br>
	 * 如果 为 “YCZ-ALL” 或者 “HFF-ALL” 则表示查询预充值或者后付费账户全部交易类型资金
	 * </li>
	 * <li>BalanceFlowDetailDTO.beginTime Date 预查询的结果的时间范围-开始时间 可选</li>
	 * <li>BalanceFlowDetailDTO.endTime Date 预查询的结果的时间范围-结束时间 可选</li>
	 * </ul>
	 * @return Query&lt;FundTradeFlowDto&gt; 带有返回结果数据和分页信息的分页对象
	 */
	Query<FundTradeFlowDto> findTradeFlowsPaged(BalanceFlowDetailDTO queryParams);
    
	/**
	 * 
	 * <p>名称:类IAcctTradeFlowBusiService中的findBxTradeFlowsPaged方法</br>    
	 * <p>描述: </br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Jan 29, 2019 8:39:07 PM</br>
	 * @throws Exception
	 * @param queryParams
	 * @return
	 */
	Query<FundTradeFlowDto> findBxTradeFlowsPaged(BalanceFlowDetailDTO queryParams);
	
	List<FundTradeFlowDto> findTradeFlows(TradeFlowSearchParams params);
	
	List<FundTradeFlowDto> findTradeFlowsRefund(TradeFlowSearchParams params);
	
	Query<FundTradeFlowDto> findTradeFlowsListPaged(Query<FundTradeFlowDto> query, TradeFlowSearchParams params);

	Multimap<String, Object> queryUnchargeFundTradeFlow(String tradeBusiCode);
}
