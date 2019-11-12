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

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.fund.api.dto.BalanceTradeFlowDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;

/**   
 * @ClassName:  IAccountService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月28日 上午10:39:33   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IAcctBlanceTradeFlowService {

	public void processTradeFlow(BalanceTradeFlowDto balanceTradeFlowDto);
	
	public List<BalanceTradeFlowDto> processBusiness(AssignDto assignDto) ;
	
	public void createTradeFlowsByBalanceTradeFlowDto(List<BalanceTradeFlowDto> fundTradeFlows);
	
		
	
	
	/**
	 * 根据账户资金编号查询账户的资金记录
	 * @Title: findAcctBalanceByCode   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   2018年9月5日 下午10:50:33  
	 * @param: balanceCode 账户资金编号 
	 * @return: TbFundBalance  账户资金记录；返回null表示查询不到记录  
	 * @throws 出现异常则会向上抛出
	 */
//	public FundBalanceDto findAcctBalanceByCode(String balanceCode);
	
//	public List<FundAcctBalanceDto> findAcctBalances(String memberId, String fundAcctCate, String balanceItemNo) ;
	
//	public List<FundAcctBalanceDto> findAcctBalances(String memberId, String fundAcctCate, List<String> balanceItemNos);
	
//	public List<FundBalanceDto> selectFundBalanceItemAmount(Map<String, Object> params);
	
	/**
	 * 
	 * @Title: findTradeFlows   
	 * @Description: 查找交易流水
	 * @Author: shilei
	 * @date:   Oct 24, 2018 4:49:19 PM  
	 * @param busiCode String 必传 业务订单号码
	 * @param billItemCate String 必传 资金科目 参考BillItemSubCate枚举
	 * @param tradeCateCodes String...  可选 交易流水类型 参考TradeCateEnums枚举
	 * 
	 * @return TbFundTradeFlow  返回交易流水实体，如果返回null则表示查询不到      
	 * @throws
	 *
	 */
	public List<TbFundTradeFlow> findTradeFlows(String busiCode, String billItemCate, String... tradeCateCodes);

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 
	 * @Title: findCompanyBlanceByCate   
	 * @Description: 查询保证金账户或者报销账户
	 * @Author: shilei
	 * @date:   Nov 13, 2018 11:15:18 AM  
	 * @param memberId 账号编码
	 * @param balanceCate 账户模式，常量值
	 * CommonConstant.BALANCE_ITEM_NO_SUFFX_BZJ 保证金账户
	 * CommonConstant.BALANCE_ITEM_NO_SUFFX_GBX 报销账户
	 * 
	 * @return FundComposDto  返回对应账户的结果，返回空则没有查询到结果
	 * @throws 某个资金查询不到，则抛9082异常
	 *
	 */
//	public FundComposDto findCompanyBlanceByCate(String memberId, String balanceCate);
	
//	/**
//	 * 
//	 * @Title: findBalanceFlowDetailsPaged   
//	 * @Description: 按SearchParamEnums类型分页查询账户资金流水
//	 * @Author: shilei
//	 * @date:   Nov 13, 2018 4:54:17 PM  
//	 * @param page Page<TbFundTradeFlow> 分页参数
//	  * @param memberId Sring 账户相关账号
//	 * @param paramCode String SearchParamEnums 的查询类型编码
//	 *                  具体请参见SearchParamEnums枚举
//	 *                  
//	 * @param beginTime Date 开始时间
//	 * @param endTime Date 结束时间
//	 * 
//	 * @return Page<TbFundTradeFlow>  分页返回 交易流水列表
//	 * @throws
//	 *
//	 */
//	public Page<TbFundTradeFlow> findBalanceFlowDetailsPaged(Page<TbFundTradeFlow> page,String memberId, String paramCode, 
//			Date beginTime, Date endTime);
	/**
	 * 
	 * @Title: findBalanceFlowDetails   
	 * @Description: 按SearchParamEnums类型查询账户资金流水
	 * @Author: shilei
	 * @date:   Nov 13, 2018 9:57:47 AM  
	 * @param memberId Sring 账户相关账号
	 * @param paramCode String SearchParamEnums 的查询类型编码
	 *                  具体请参见SearchParamEnums枚举
	 *                  
	 * @param beginTime Date 开始时间
	 * @param endTime Date 结束时间
	 * 
	 * @return List<TbFundTradeFlow> 交易流水列表
	 * @throws
	 * 
	 */
	public List<TbFundTradeFlow> findBalanceFlowDetails(String memberId, String paramCode, 
			Date beginTime, Date endTime);
	
	/**
	 * 
	 * @Title: checkingCompanyRechargeBalance   
	 * @Description: 从总部获取当前企业的保证金并更新系统内部记录
	 * @Author: shilei
	 * @date:   2018年9月2日 下午3:46:08  
	 * @param: memberId 公司编号
	 * @return: TbFundBalance  对应公司的账户保证金资金   
	 * @throws 出现异常则会向上抛出
	 */
//	public FundBalanceDto findPerOrAftPaymentBalance(String memberId, String... busiModel);
	
	/**
	 * 
	 * @Title: findTradeFlows   
	 * @Description: 按条件查询交易流水
	 * @Author: shilei
	 * @date:   Nov 13, 2018 10:32:18 AM  
	 * @param balanceCode String 资金编号
	 * @param busiModel String 业务模式
	 * @param billItemCate String 资金对象编码
	 * @param orderType String 订单类型
	 * @param tradeCate String 交易类型
	 * @param status String 交易状态
	 * @param busiCode String 业务编码
	 * @param tradeAmount BigDecimal 交易额
	 * @param beginTime Date 开始时间
	 * @param endTime Date 结束时间
	 * @return List<TbFundTradeFlow>        
	 * @throws
	 *
	 */
//	public List<TbFundTradeFlow> findTradeFlows(String balanceCode, String busiModel, String billItemCate, String orderType,
//			String tradeCate, String status, String busiCode, BigDecimal tradeAmount, Date beginTime, Date endTime);
//	
//	/**
//	 * 
//	 * @Title: selectFundBalanceItemAmounts   
//	 * @Description: 查询资金对象列表
//	 * @Author: shilei
//	 * @date:   Nov 8, 2018 3:15:30 PM  
//	 * @param balanceItemCodes List<String> 资金对应项目编码集合
//	 * @return List<FundBalanceDto> 对象     
//	 * @throws
//	 *
//	 */
//	public List<FundBalanceDto> selectFundBalanceItemAmounts(List<String> balanceItemCodes);

}
