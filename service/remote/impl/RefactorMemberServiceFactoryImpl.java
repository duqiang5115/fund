/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  RefactorMemberServiceFactoryImpl.java   
 * @Package com.taolue.baoxiao.fund.service.remote.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 26, 2018 7:53:41 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.remote.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taolue.baoxiao.common.util.Exceptions;
import com.taolue.baoxiao.common.util.R;
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
import com.taolue.baoxiao.fund.service.remote.IRefactorMemberServiceFactory;
import com.taolue.member.api.vo.BankCardInfoVo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

/**   
 * <p>ClassName:  RefactorMemberServiceFactoryImpl </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: shilei</br>
 * <p>date:   Dec 26, 2018 7:53:41 PM </br>  
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class RefactorMemberServiceFactoryImpl implements IRefactorMemberServiceFactory {
	
    private static final boolean isDebug = log.isDebugEnabled();
    
	@Autowired
	private IRefactorMemberService refactorMemberService;
	
	@Autowired
	private IRefactorMemberCompanyService refactorMemberCompanyService;
	
	@Autowired
	private IRefactorMemberCompanyRelationService refactorMemberCompanyRelationService;
	
	@Autowired
	private IRefactorMemberDeptService refactorMemberDeptService;
	
	@Autowired
	private IRefactorVendorService refactorVendorService;
	
	@Autowired
	private IRefactorMemberSubstituteSaleConfigService refactorMemberSubstituteSaleConfigService;
	
	@Autowired
	private RefactorMemberPlatformService refactorMemberPlatformService;
	
	@Autowired
	private RefactorMemberFundRateRuleListService refactorMemberFundRateRuleListService;
	
	@Autowired
	private RefactorMemberBillRateService refactorMemberBillRateService;
	
	@Autowired
	private IRefactorMemberBankCardService refactorMemberBankCardService;
	
	@Autowired
	private IRefactorMemberRecommendInfoService refactorMemberRecommendInfoService;
	
	@Value("${application.model}")
	private String applicationModel = "";

	/**  
	 * <p>Title:getRefactorMemberService</p><BR>  
	 * <p>Description:获取属性refactorMemberService的值<BR>  
	 * @return IRefactorMemberService <BR>  
	 */
	public IRefactorMemberService getRefactorMemberService() {
		return refactorMemberService;
	}

	/**  
	 * <p>Title:getRefactorMemberCompanyService</p><BR>  
	 * <p>Description:获取属性refactorMemberCompanyService的值<BR>  
	 * @return IRefactorMemberCompanyService <BR>  
	 */
	public IRefactorMemberCompanyService getRefactorMemberCompanyService() {
		return refactorMemberCompanyService;
	}

	/**  
	 * <p>Title:getRefactorMemberCompanyRelationService</p><BR>  
	 * <p>Description:获取属性refactorMemberCompanyRelationService的值<BR>  
	 * @return IRefactorMemberCompanyRelationService <BR>  
	 */
	public IRefactorMemberCompanyRelationService getRefactorMemberCompanyRelationService() {
		return refactorMemberCompanyRelationService;
	}

	/**  
	 * <p>Title:getRefactorMemberDeptService</p><BR>  
	 * <p>Description:获取属性refactorMemberDeptService的值<BR>  
	 * @return IRefactorMemberDeptService <BR>  
	 */
	public IRefactorMemberDeptService getRefactorMemberDeptService() {
		return refactorMemberDeptService;
	}

	/**  
	 * <p>Title:getRefactorVendorService</p><BR>  
	 * <p>Description:获取属性refactorVendorService的值<BR>  
	 * @return IRefactorVendorService <BR>  
	 */
	public IRefactorVendorService getRefactorVendorService() {
		return refactorVendorService;
	}

	/**  
	 * <p>Title:getRefactorMemberPlatformService</p><BR>  
	 * <p>Description:获取属性refactorMemberPlatformService的值<BR>  
	 * @return RefactorMemberPlatformService <BR>  
	 */
	public RefactorMemberPlatformService getRefactorMemberPlatformService() {
		return refactorMemberPlatformService;
	}

	/**  
	 * <p>Title:getRefactorMemberFundRateRuleListService</p><BR>  
	 * <p>Description:获取属性refactorMemberFundRateRuleListService的值<BR>  
	 * @return RefactorMemberFundRateRuleListService <BR>  
	 */
	public RefactorMemberFundRateRuleListService getRefactorMemberFundRateRuleListService() {
		return refactorMemberFundRateRuleListService;
	}

	/**  
	 * <p>Title:getRefactorMemberBillRateService</p><BR>  
	 * <p>Description:获取属性refactorMemberBillRateService的值<BR>  
	 * @return RefactorMemberBillRateService <BR>  
	 */
	public RefactorMemberBillRateService getRefactorMemberBillRateService() {
		return refactorMemberBillRateService;
	}

	/**  
	 * <p>Title:getRefactorMemberSubstituteSaleConfigService</p><BR>  
	 * <p>Description:获取属性refactorMemberSubstituteSaleConfigService的值<BR>  
	 * @return IRefactorMemberSubstituteSaleConfigService <BR>  
	 */
	public IRefactorMemberSubstituteSaleConfigService getRefactorMemberSubstituteSaleConfigService() {
		return refactorMemberSubstituteSaleConfigService;
	}

	/**  
	 * <p>Title:getRefactorMemberBankCardService</p><BR>  
	 * <p>Description:获取属性refactorMemberBankCardService的值<BR>  
	 * @return IRefactorMemberBankCardService <BR>  
	 */
	public IRefactorMemberBankCardService getRefactorMemberBankCardService() {
		return refactorMemberBankCardService;
	}
	
	
	/**  
	 * <p>Title:getRefactorMemberRecommendInfoService</p><BR>  
	 * <p>Description:获取属性refactorMemberRecommendInfoService的值<BR>  
	 * @return IRefactorMemberRecommendInfoService <BR>  
	 */
	public IRefactorMemberRecommendInfoService getRefactorMemberRecommendInfoService() {
		return refactorMemberRecommendInfoService;
	}

	public BankCardInfoVo findBankCardWithCache(String memberId, String oriBankNumber, boolean changeCard) throws FundServiceException {
		if (isDebug) {
			log.debug("查询银行卡的请求参数为memberId={},oribanknumber={},changecard={}", 
					memberId, oriBankNumber, changeCard);
			log.debug("THSI.APPMODEL={}",this.applicationModel);
		}
		
		List<BankCardInfoVo> bankCards = null;
		BankCardInfoVo bankCard = null;
		
		if ("test".equals(this.applicationModel)) {
			memberId = "1107827856228089857";
		} 
		
		R<List<BankCardInfoVo>> rank = null; 
		try {
			rank = getRefactorMemberBankCardService().findBankCardInfoVoList(memberId);
		} catch (Exception e) {
			log.error("调用接口查询会员{}绑定的银行卡信息，出现错误{}", 
					memberId,Exceptions.getStackTraceAsString(e));
			return null;
		}
		
		if (rank != null && (rank.getCode() == R.SUCCESS 
				|| rank.getCode() == HttpStatus.HTTP_OK)) {
			bankCards = rank.getData();
			if (isDebug) {
				log.debug("查询银行卡的返回={}", bankCards);
			}
		}

		if (CollUtil.isEmpty(bankCards)) {
			log.error("会员{}没有绑定银行卡，无法进行提现", memberId);
//			FundServiceExceptionGenerator.FundServiceException(HttpStatus.HTTP_UNAVAILABLE, 
//					"会员{}没有绑定银行卡，无法进行提现", memberId);
			return null;
		}
		
		//原银行卡号为空，说明第一次取卡，直接返回第一张卡
		if (StrUtil.isBlank(oriBankNumber)) {
			bankCard =  bankCards.get(0);
		
		//否则是再次取卡信息
		} else {
			boolean noMatchOriCard = true;
			//循环所有卡
			for (int i=0;i<bankCards.size();i++) {
				//如果原银行卡和当前的卡相同
				if (bankCards.get(i).getBankNumber().equals(oriBankNumber)){
					noMatchOriCard = false;
					//当前不需要换卡，说明是获取当前卡的详情，直接取当前卡信息返回
					if (!changeCard) {
						bankCard = bankCards.get(i);
						break;
						
					//否则,说明需要换当前卡，取当前卡的下一张卡的详情
					} else {
						//若当前已经是最后一张卡，则说明卡已经用完，无法换卡
						if (i==bankCards.size()-1) {
							bankCard = null;
							break;
							
						//否则取下一张卡的详情
						} else {
							bankCard = bankCards.get(i+1);
							break;
						}
					}
				}
			}
			//原银行卡没有在当前卡信息列表中，则说明用户有更改过绑定卡操作，如果需要换卡，则默认取列表的第一张卡
			if (noMatchOriCard && changeCard) {
				bankCard =  bankCards.get(0);
			}
		}
		if (null == bankCard) {
			log.error("会员{}已无可用银行卡", memberId);
			return null;
//			FundServiceExceptionGenerator.FundServiceException(HttpStatus.HTTP_UNAVAILABLE, 
//					"会员{}已无可用银行卡", memberId);
		}
		
		if (StrUtil.isBlank(bankCard.getProvinceName()) || 
				StrUtil.isBlank(bankCard.getCityName())) {
			log.error("会员{}绑定的银行卡{}的归属省或者市属性为空请检查", 
					memberId, bankCard.getBankNumber());
//			FundServiceExceptionGenerator.FundServiceException(HttpStatus.HTTP_UNAVAILABLE,
//					"会员{}绑定的银行卡{}的归属省或者市属性为空请检查", 
//					memberId, bankCard.getBankNumber());
			if ("test".equals(this.applicationModel)) {
				bankCard.setProvinceName("北京市");
				bankCard.setCityName("北京市");
			} else {
				return null;
			}
		}
		return bankCard;
	}
}
