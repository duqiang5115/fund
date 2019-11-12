/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  SoldProxyServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Mar 7, 2019 10:38:10 AM   
 * @version V1.0 
 * @Copyright: 2019 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
package com.taolue.baoxiao.fund.service.composite.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyRoleType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.entity.BusinessApplyParty;
import com.taolue.baoxiao.fund.entity.Order;
import com.taolue.baoxiao.fund.service.composite.IBusinessApplyBusiService;
import com.taolue.baoxiao.fund.service.composite.ITestService;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ClassName: SoldProxyServiceImpl </br>
 * <p>
 * Description:TODO(这里用一句话描述这个类的作用)</br>
 * <p>
 * Author: shilei</br>
 * <p>
 * date: Mar 7, 2019 10:38:10 AM </br>
 * 
 * @Copyright: 2019 www.jia-fu.cn Inc. All rights reserved.
 *             注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Slf4j
@Service
public class TestServiceImpl extends SelfProxyServiceImpl<TestServiceImpl> implements ITestService {


	@Autowired
	private IBusinessApplyBusiService businessApplyBusiService;
	
	@Transactional(rollbackFor=Exception.class)
	public void outMethod() {
		
//		Order main = this.businessApplyBusiService
//			.createApplyMain("TST", CommonConstant.STRING_BLANK, 
//					OrderType.AUDIT, OrderType.TRADE, BusinessApplyStatus.CREATED, CommonConstant.STRING_BLANK,
//					"TEST");
		
//		BusinessApplyParty applyParty = this.businessApplyBusiService.createApplyParty("TSTP", main.getOrderNo(), 
//				BusinessApplyRoleType.BUYER, "123456", BusinessApplyStatus.CREATED, "GU123456");
//		
//		try {
//			this.getSelfProxy().innerMethod(applyParty);
//			//innerMethod(applyParty);
//			main.setStatus(BusinessApplyStatus.PROCESS.getCateCode());
//		} catch (Exception e) {
//			log.error("外层方法错误，错误信息为"+e.getMessage());
//			//FundServiceExceptionGenerator.FundServiceException(503, "最上层方法抛出异常");
//		} finally {
//			main.insertOrUpdate();
//		}
		
	}
	
	@Transactional(rollbackFor=Exception.class,propagation=Propagation.REQUIRES_NEW)
	public void innerMethod(BusinessApplyParty applyParty) {
		
		try {
			applyParty.setStatus(BusinessApplyStatus.PAUSE.getCateCode());
			applyParty.insertOrUpdate();
			throw new NullPointerException();
		} catch (Exception e) {
			log.error("内层方法错误，错误信息为："+e.getMessage());
			FundServiceExceptionGenerator.FundServiceException(503, "中间层方法抛出异常");
		} finally {
		}
	}
	
	public void subInnerMethod() {
		
	}
}
