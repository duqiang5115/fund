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
import java.util.Date;
import java.util.List;

import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyRoleType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.fund.api.dto.BusinessApplyBusiDto;
import com.taolue.baoxiao.fund.common.model.OrderApply;
import com.taolue.baoxiao.fund.entity.BusinessApplyBalance;
import com.taolue.baoxiao.fund.entity.BusinessApplyCharges;
import com.taolue.baoxiao.fund.entity.BusinessApplyParty;
import com.taolue.baoxiao.fund.entity.Order;
import com.taolue.baoxiao.fund.service.IBusinessApplyBalanceService;
import com.taolue.baoxiao.fund.service.IBusinessApplyChargesService;
import com.taolue.baoxiao.fund.service.IBusinessApplyPartyService;
import com.taolue.baoxiao.fund.service.IBusinessApplyProdService;
import com.taolue.baoxiao.fund.service.IOrderService;

/**   
 * @ClassName:  IAccountService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月28日 上午10:39:33   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IBusinessApplyBusiService {
	
	/**  
	 * <p>Title:getOrderService</p><BR>  
	 * <p>Description:获取属性orderService的值<BR>  
	 * @return IOrderService <BR>  
	 */
	IOrderService getOrderService();
	
	/**  
	 * <p>Title:getBusinessApplyPartyService</p><BR>  
	 * <p>Description:获取属性businessApplyPartyService的值<BR>  
	 * @return IBusinessApplyPartyService <BR>  
	 */
	IBusinessApplyPartyService getBusinessApplyPartyService();

	/**  
	 * <p>Title:getBusinessApplyChargesService</p><BR>  
	 * <p>Description:获取属性businessApplyChargesService的值<BR>  
	 * @return IBusinessApplyChargesService <BR>  
	 */
	IBusinessApplyChargesService getBusinessApplyChargesService();

	/**  
	 * <p>Title:getBusinessApplyBalanceService</p><BR>  
	 * <p>Description:获取属性businessApplyBalanceService的值<BR>  
	 * @return IBusinessApplyBalanceService <BR>  
	 */
	IBusinessApplyBalanceService getBusinessApplyBalanceService();

	/**  
	 * <p>Title:getBusinessApplyProdService</p><BR>  
	 * <p>Description:获取属性businessApplyProdService的值<BR>  
	 * @return IBusinessApplyProdService <BR>  
	 */
	IBusinessApplyProdService getBusinessApplyProdService();
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的createApplyMain方法</br>    
	 * <p>描述: 创建业务主单据，不入库</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 23, 2019 12:53:26 PM</br>
	 * @throws Exception
	 * @param orderNoPefix {@link String} 单据编号前缀
	 * @param orderCode {@link String} 单据外部编码
	 * @param mainType {@link OrderType} 单据主类型枚举
	 * @param subType {@link OrderType} 单据子类型枚举
	 * @param status {@link BusinessApplyStatus} 单据状态枚举 可选，默认BusinessApplyStatus.CREATED
	 * @param companyId {@link String} 单据归属公司编码，可选，默认CommonConstant.DEFAULT_ACCT_MEMBER_ID
	 * @param sourceCode {@link String} 单据来源，可选
	 * @return {@link Order} 对象
	 */
	Order createApplyMain(String orderNoPefix, String orderCode, OrderType mainType, OrderType subType, 
			BusinessApplyStatus status, String companyId, String sourceCode);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的createApplyParty方法</br>    
	 * <p> 创建单据关联参与对象信息，不入库</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 23, 2019 1:07:11 PM</br>
	 * @throws Exception
	 * @param applyPartyCodePerfix {@link String} 关联编号前缀
	 * @param applyMainCode {@link String} 主单据编码
	 * @param roleCode {@link MemberCateEnums} 参与对象类型角色枚举
	 * @param partyCode {@link String} 参与对象编码
	 * @param status {@link BusinessApplyStatus} 单据状态枚举,可选，默认BusinessApplyStatus.CREATED
	 * @param euid {@link String} 参与对象第三方编码（guid/geid）可选
	 * @return {@link BusinessApplyParty} 对象
	 */
	BusinessApplyParty createApplyParty(String applyPartyCodePerfix, String applyMainCode, 
			MemberCateEnums roleCode, String partyCode, BusinessApplyStatus status, String euid);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的createApplyParty方法</br>    
	 * <p> 创建单据关联参与对象信息，不入库</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 23, 2019 1:07:11 PM</br>
	 * @throws Exception
	 * @param applyPartyCodePerfix {@link String} 关联编号前缀
	 * @param applyMainCode {@link String} 主单据编码
	 * @param roleCode {@link ActionType} 资金流角色枚举
	 * @param partyCode {@link String} 参与对象编码
	 * @param status {@link BusinessApplyStatus} 单据状态枚举,可选，默认BusinessApplyStatus.CREATED
	 * @param euid {@link String} 参与对象第三方编码（guid/geid）可选
	 * @return {@link BusinessApplyParty} 对象
	 */
	BusinessApplyParty createApplyParty(String applyPartyCodePerfix, String applyMainCode, 
			ActionType roleCode, String partyCode, BusinessApplyStatus status, String euid);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的createApplyParty方法</br>    
	 * <p> 创建关联参与对象信息，不入库</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 23, 2019 1:07:11 PM</br>
	 * @throws Exception
	 * @param applyPartyCodePerfix {@link String} 关联编号前缀
	 * @param applyMainCode {@link String} 主单据编码
	 * @param roleCode {@link BusinessApplyRoleType} 业务角色枚举
	 * @param partyCode {@link String} 参与对象编码
	 * @param status {@link BusinessApplyStatus} 单据状态枚举,可选，默认BusinessApplyStatus.CREATED
	 * @param euid {@link String} 参与对象第三方编码（guid/geid）可选
	 * @return {@link BusinessApplyParty} 对象
	 */
	BusinessApplyParty createApplyParty(String applyPartyCodePerfix, String applyMainCode, 
			BusinessApplyRoleType roleCode, String partyCode, BusinessApplyStatus status, String euid);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的createApplyCharge方法</br>    
	 * <p>描述: 创建关联的费用信息对象，不入库</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 23, 2019 1:27:28 PM</br>
	 * @throws Exception
	 * @param applyChargeCodePerfix {@link String} 关联编号前缀
	 * @param applyMainCode {@link String} 主单据编码
	 * @param chargeType {@link BillItemSubCate} 费用科目枚举
	 * @param chargeItemType {@link AcctCateEnums} 费用类型枚举
	 * @param chargeItemCode {@link String} 费用实例编码，如券id，memberid，可选，默认空
	 * @param amount {@link BigDecimal} 交易额度，可选，默认
	 * @param status {@link BusinessApplyStatus} 单据状态枚举,可选，默认BusinessApplyStatus.CREATED
	 * @param relateCode {@link String} 关联信息编码，如member的第三方编码，可选，默认空
	 * @param chargeSubject {@link String} 新费用科目，暂不启用，默认空
	 * @return {@link BusinessApplyCharges} 对象
	 */
	BusinessApplyCharges createApplyCharge(String applyChargeCodePerfix, String applyMainCode, 
			BillItemSubCate chargeType, AcctCateEnums chargeItemType, String chargeItemCode, 
			BigDecimal amount, BusinessApplyStatus status, String relateCode, String chargeSubject);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的createApplyBalance方法</br>    
	 * <p>描述:创建关联的资金信息（费用明细）对象，不入库</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 23, 2019 1:39:46 PM</br>
	 * @throws Exception
	 * @param applyBalanceCodePerfix {@link String} 关联编号前缀
	 * @param applyMainCode {@link String} 主单据编码
	 * @param applyChargeCode {@link String} 费用单据编码
	 * @param applyPartyCode {@link String} 人员单据编码
	 * @param roleCode {@link BusinessApplyRoleType} 角色枚举， 可选
	 * @param balanceCode {@link String}  资金编码，可选
	 * @param status {@link BusinessApplyStatus} 单据状态枚举,可选，默认BusinessApplyStatus.CREATED
	 * @param tradeAmount {@link BigDecimal} 本次交易额度，可选，默认CommonConstant.NO_AMOUNT;
	 * @param relateCode {@link String} 关联信息编码，可选，如银行卡号，默认空
	 * @param mustAmount {@link BigDecimal} 应发生总额，未启用
	 * @param payAmout {@link BigDecimal} 已发生付总额，未启用
	 * @return {@link BusinessApplyBalance} 对象
	 */
	BusinessApplyBalance createApplyBalance(String applyBalanceCodePerfix, String applyMainCode, 
			String applyChargeCode, String applyPartyCode, BusinessApplyRoleType roleCode, 
			String balanceCode, BusinessApplyStatus status, BigDecimal tradeAmount, String relateCode, 
			BigDecimal mustAmount, BigDecimal payAmout);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的createApplyBalance方法</br>    
	 * <p>描述:创建关联的资金信息（费用明细）对象，不入库</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 23, 2019 1:39:46 PM</br>
	 * @throws Exception
	 * @param applyBalanceCodePerfix {@link String} 关联编号前缀
	 * @param applyMainCode {@link String} 主单据编码
	 * @param applyChargeCode {@link String} 费用单据编码
	 * @param applyPartyCode {@link String} 人员单据编码
	 * @param roleCode {@link BillItemSubCate} 角色枚举， 可选
	 * @param balanceCode {@link String}  资金编码，可选
	 * @param status {@link BusinessApplyStatus} 单据状态枚举,可选，默认BusinessApplyStatus.CREATED
	 * @param tradeAmount {@link BigDecimal} 本次交易额度，可选，默认CommonConstant.NO_AMOUNT;
	 * @param relateCode {@link String} 关联信息编码，可选，如银行卡号，默认空
	 * @param mustAmount {@link BigDecimal} 应发生总额，未启用
	 * @param payAmout {@link BigDecimal} 已发生付总额，未启用
	 * @return {@link BusinessApplyBalance} 对象
	 */
	BusinessApplyBalance createApplyBalance(String applyBalanceCodePerfix, String applyMainCode, 
			String applyChargeCode, String applyPartyCode, BillItemSubCate roleCode, 
			String balanceCode, BusinessApplyStatus status, BigDecimal tradeAmount, String relateCode, 
			BigDecimal mustAmount, BigDecimal payAmout);
	
	void createSoldProxyOrder(Order order, List<BusinessApplyParty> applyParts,
			List<BusinessApplyCharges> applyCharges, List<BusinessApplyBalance> applyBalances);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的getApplyMainByStatus方法</br>    
	 * <p>描述: 查询是否有不是某种状态的主单据</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 18, 2019 6:08:06 AM</br>
	 * @throws Exception
	 * @param mainType {@link String} 主单据主类型
	 * @param subType {@link String} 主单据子类型
	 * @param memberId {@link String} 会员编号
	 * @param status {@link String} 主单据状态,该条件是查询不等与该状态的主单据
	 * @param beginTime {@link Date} 主单据创建时间下限
	 * @param endTime {@link Date} 主单据创建时间上限
	 * @return {@link boolean} 是否存在指定条件的主单据，true-存在；false-不存在
	 */
	boolean getExsitApplyMain(String mainType, String subType, String memberId, String status, 
			Date beginTime, Date endTime);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的getExsitApplyCharge方法</br>    
	 * <p>描述: 查询是否有指定条件的费用单据</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 18, 2019 6:08:06 AM</br>
	 * @throws Exception
	 * @param applyCode {@link String} 主单据编码
	 * @param status {@link String} 费用数据状态,该条件是查询不等与该状态的费用单据
	 * @return {@link boolean} 指定的主单据下是否存在不等与status的费用单据，true-不存在；false-存在
	 */
	boolean getExsitApplyCharge(String applyCode, String status);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的getApplyMainByCode方法</br>    
	 * <p>描述: 通过单据编码查询主单据信息</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 18, 2019 3:38:28 AM</br>
	 * @throws Exception 若没有查询到对应的主单据信息，则抛出503异常
	 * @param applyCode {@link String} 主单据编码
	 * @return {@link Order} 主单据对象
	 */
	Order getApplyMainByCode(String applyCode);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的getApplyPartyByCode方法</br>    
	 * <p>描述: 通与主单据关联的会员信息 关联编码查询会员关联信息</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 18, 2019 3:39:54 AM</br>
	 * @throws Exception 若查询不到对应数据，则抛出503异常
	 * @param code {@link String} 与某个主单据关联的会员信息关联关系编码
	 * @return {@link BusinessApplyParty} 与某个主单据关联的会员信息
	 */
	BusinessApplyParty getApplyPartyByCode(String code);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的getApplyPartyByByApplyCodeRole方法</br>    
	 * <p>描述: 通过主单据编码和会员角色编码查询单据关联会员信息</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 22, 2019 7:21:21 AM</br>
	 * @throws Exception
	 * @param applyCode {@link String} 主单据编码
	 * @param roleCode {@link String} 会员角色编码
	 * @return {@link List}{@literal <}{@link BusinessApplyParty}{@literal >} 对象列表
	 */
	List<BusinessApplyParty> getApplyPartyByApplyCodeRole(String applyCode, String roleCode);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的getApplyPartysByApplyCode方法</br>    
	 * <p>描述: 通过主单据编码，查询与该主单据关联的关联会员信息列表</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 18, 2019 3:42:53  AM</br>
	 * @throws Exception 
	 * @param applyCode {@link String} 主单据编码
	 * @return {@link List}{@literal <}{@link BusinessApplyParty}{@literal >} 与某个主单据关联的会员信息列表
	 * 不存在则返回空列表
	 */
	List<BusinessApplyParty> getApplyPartysByApplyCode(String applyCode);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的getApplyChargeByCode方法</br>    
	 * <p>描述: 通与主单据关联的会员信息 关联编码查询费用关联信息</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 18, 2019 3:47:57 AM</br>
	 * @throws Exception 若查询不到对应数据，则抛出503异常
	 * @param code {@link String} 与某个主单据关联的费用信息关联关系编码
	 * @return {@link BusinessApplyCharges} 与某个主单据关联的费用信息
	 *
	 */
	BusinessApplyCharges getApplyChargeByCode(String code);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的getApplyChargesByApplyCode方法</br>    
	 * <p>描述: 通过主单据编码，查询与该主单据关联的关联费用信息列表</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 18, 2019 3:49:29 AM</br>
	 * @throws Exception
	 * @param applyCode {@link String} 主单据编码
	 * @return {@link List}{@literal <}{@link BusinessApplyCharges}{@literal >} 与某个主单据关联的费用信息列表
	 * 不存在则返回空列表
	 */
	List<BusinessApplyCharges> getApplyChargesByApplyCode(String applyCode);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的getApplyBalanceByCode方法</br>    
	 * <p>描述: 通与主单据关联的会员信息 关联编码查询资金关联信息</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 18, 2019 3:50:41 AM</br>
	 * @throws Exception 若查询不到对应数据，则抛出503异常
	 * @param code{@link String} 与某个主单据关联的资金信息关联关系编码
	 * @return {@link BusinessApplyBalance} 与某个主单据关联的资金信息
	 */
	BusinessApplyBalance getApplyBalanceByCode(String code);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的getApplyBalancesByApplyCode方法</br>    
	 * <p>描述: 通过主单据编码，查询与该主单据关联的关联资金信息列表</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 18, 2019 3:52:00 AM</br>
	 * @throws Exception
	 * @param applyCode {@link String} 主单据编码
	 * @return {@link List}{@literal <}{@link BusinessApplyBalance}{@literal >} 与某个主单据关联的资金信息列表
	 * 不存在则返回空列表
	 */
	List<BusinessApplyBalance> getApplyBalancesByApplyCode(String applyCode);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的getApplyBalancesByChargeCode方法</br>    
	 * <p>描述: 按资金角色类型查询费用单据关联的资金单据，若不指定资金角色类型则查询该费用单据关联的所有资金单据</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 22, 2019 12:36:47 AM</br>
	 * @throws Exception
	 * @param chargeCode {@link String} 费用单据编码
	 * @param roleType {@link String} 资金角色编码-可以为空
	 * @return {@link List}{@literal <}{@link BusinessApplyBalance}{@literal >} 与某个费用单据关联的资金信息列表
	 * 如果指定了资金角色，则只会查询该资金角色的数据，不存在则返回空列表
	 */
	List<BusinessApplyBalance> getApplyBalancesByChargeCode(String chargeCode, String roleType);
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的findOrderApplyByOrderNo方法</br>    
	 * <p>描述: 通过主单据编码，查询主单据，与主单据关联的会员，费用，资金信息</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 18, 2019 3:53:03 AM</br>
	 * @throws Exception 若主单据不存在 则抛出503异常
	 * @param orderNo {@link String} 主单据编码
	 * @return {@link OrderApply} 包含主单据，与主单据关联的会员，费用，资金信息的组合对象
	 */
	OrderApply findOrderApplyByOrderNo(String orderNo);
	
	
	/**
	 * 
	 * <p>名称:类IBusinessApplyBusiService中的findSoldProxyOrders方法</br>    
	 * <p>描述: 按会员编号，智能代卖单据编号查询智能代卖单据，智能代卖单据编号为可选参数，若指定则单据查询指定单号单据</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 18, 2019 3:12:53 PM</br>
	 * @throws Exception
	 * @param memberId {@link String} 会员编号 必传
	 * @param orderNo {@link String} 智能代卖单据编号 可选 （若指定则查询单独一条订单）
	 * @return {@link List}{@literal <}{@link BusinessApplyBusiDto}{@literal >} 智能代卖单据列表
	 *  
	 */
	List<BusinessApplyBusiDto>  findSoldProxyOrders(String memberId, String orderNo);
	
	List<BusinessApplyBalance> findWithdrawBalances(String status) ;
}
