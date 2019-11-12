/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  IRefactorMemberServiceFactory.java   
 * @Package com.taolue.baoxiao.fund.service.remote   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 26, 2018 7:53:05 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.remote;

import com.taolue.baoxiao.fund.api.member.IRefactorMemberBankCardService;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberCompanyRelationService;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberCompanyService;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberDeptService;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberRecommendInfoService;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberService;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberSubstituteSaleConfigService;
import com.taolue.baoxiao.fund.api.member.IRefactorVendorService;
import com.taolue.baoxiao.fund.api.member.RefactorMemberBillRateService;
import com.taolue.baoxiao.fund.api.member.RefactorMemberFundRateRuleListService;
import com.taolue.baoxiao.fund.api.member.RefactorMemberPlatformService;
import com.taolue.baoxiao.fund.common.exception.FundServiceException;
import com.taolue.member.api.vo.BankCardInfoVo;

/**   
 * <p>ClassName:  IRefactorMemberServiceFactory </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: shilei</br>
 * <p>date:   Dec 26, 2018 7:53:05 PM </br>  
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IRefactorMemberServiceFactory {

	/**  
	 * <p>Title:getRefactorMemberService</p><BR>  
	 * <p>Description:获取属性refactorMemberService的值<BR>  
	 * @return IRefactorMemberService <BR>  
	 */
	public IRefactorMemberService getRefactorMemberService();

	/**  
	 * <p>Title:getRefactorMemberCompanyService</p><BR>  
	 * <p>Description:获取属性refactorMemberCompanyService的值<BR>  
	 * @return IRefactorMemberCompanyService <BR>  
	 */
	public IRefactorMemberCompanyService getRefactorMemberCompanyService();

	/**  
	 * <p>Title:getRefactorMemberCompanyRelationService</p><BR>  
	 * <p>Description:获取属性refactorMemberCompanyRelationService的值<BR>  
	 * @return IRefactorMemberCompanyRelationService <BR>  
	 */
	public IRefactorMemberCompanyRelationService getRefactorMemberCompanyRelationService();

	/**  
	 * <p>Title:getRefactorMemberDeptService</p><BR>  
	 * <p>Description:获取属性refactorMemberDeptService的值<BR>  
	 * @return IRefactorMemberDeptService <BR>  
	 */
	public IRefactorMemberDeptService getRefactorMemberDeptService();

	/**  
	 * <p>Title:getRefactorVendorService</p><BR>  
	 * <p>Description:获取属性refactorVendorService的值<BR>  
	 * @return IRefactorVendorService <BR>  
	 */
	public IRefactorVendorService getRefactorVendorService();

	/**  
	 * <p>Title:getRefactorMemberPlatformService</p><BR>  
	 * <p>Description:获取属性refactorMemberPlatformService的值<BR>  
	 * @return RefactorMemberPlatformService <BR>  
	 */
	public RefactorMemberPlatformService getRefactorMemberPlatformService();

	/**  
	 * <p>Title:getRefactorMemberFundRateRuleListService</p><BR>  
	 * <p>Description:获取属性refactorMemberFundRateRuleListService的值<BR>  
	 * @return RefactorMemberFundRateRuleListService <BR>  
	 */
	public RefactorMemberFundRateRuleListService getRefactorMemberFundRateRuleListService();

	/**  
	 * <p>Title:getRefactorMemberBillRateService</p><BR>  
	 * <p>Description:获取属性refactorMemberBillRateService的值<BR>  
	 * @return RefactorMemberBillRateService <BR>  
	 */
	public RefactorMemberBillRateService getRefactorMemberBillRateService();
	
	IRefactorMemberSubstituteSaleConfigService getRefactorMemberSubstituteSaleConfigService();
	
	IRefactorMemberBankCardService getRefactorMemberBankCardService();
	
	/**  
	 * <p>Title:getRefactorMemberRecommendInfoService</p><BR>  
	 * <p>Description:获取属性refactorMemberRecommendInfoService的值<BR>  
	 * @return IRefactorMemberRecommendInfoService <BR>  
	 */
	IRefactorMemberRecommendInfoService getRefactorMemberRecommendInfoService();
	
	/**
	 * 
	 * <p>名称:类IRefactorMemberServiceFactory中的findBankCardWithCache方法</br>    
	 * <p>描述: 获取银行卡信息</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 27, 2019 11:27:17 PM</br>
	 * @throws FundServiceException 503 
	 * @param memberId 会员编号
	 * @param oriBankNumber 上次使用卡号，如果需换卡，则取当前卡号为oriBankNumber所在位置的下一张卡
	 * 					若不需要换卡，则取当前卡
	 * @param changeCard 是否需要换卡 是否需要换卡
	 * @return BankCardInfoVo
	 */
	BankCardInfoVo findBankCardWithCache(String memberId, String oriBankNumber, boolean changeCard)  throws FundServiceException;
}
