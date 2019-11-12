/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  IRefactorInvoicServiceFactory.java   
 * @Package com.taolue.baoxiao.fund.service.remote   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 28, 2018 6:00:48 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.remote;

import java.util.List;

import com.taolue.baoxiao.fund.api.invoice.IRefactorInvoiceReimburseService;
import com.taolue.baoxiao.fund.api.invoice.IRefactorReimburseItemService;
import com.taolue.baoxiao.fund.api.invoice.IRefactorVoucherCategoryService;
import com.taolue.invoice.api.dto.IndustryDto;

/**   
 * <p>ClassName:  IRefactorInvoicServiceFactory </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: shilei</br>
 * <p>date:   Dec 28, 2018 6:00:48 PM </br>  
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IRefactorInvoicServiceFactory {
	
	/**
	 * 
	 * <p>名称:类IRefactorInvoicServiceFactory中的getRefactorVoucherCategoryService方法</br>    
	 * <p>描述: 获取发票类型和行业类别服务接口</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Dec 28, 2018 7:01:03 PM</br>
	 * @throws Exception
	 * @return
	 */
	IRefactorVoucherCategoryService getRefactorVoucherCategoryService();
	
	/**
	 * 
	 * <p>名称:类IRefactorInvoicServiceFactory中的getRefactorReimburseItemService方法</br>    
	 * <p>描述: 获取报销科目服务</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Jan 2, 2019 12:41:54 AM</br>
	 * @throws Exception
	 * @return
	 */
	IRefactorReimburseItemService getRefactorReimburseItemService();
	
	/**
	 * 
	 * <p>名称:类IRefactorInvoicServiceFactory中的getRefactorInvoiceReimburseService方法</br>    
	 * <p>描述:获取发票和报销科目服务类</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Jan 9, 2019 10:41:51 PM</br>
	 * @throws Exception
	 * @return
	 */
	IRefactorInvoiceReimburseService getRefactorInvoiceReimburseService();
	
	/**
	 * 
	 * <p>名称:类IRefactorInvoicServiceFactory中的findIndustrysByReimburseCode方法</br>    
	 * <p>描述:根据报销科目编码获取对应券行业分类列表</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Dec 28, 2018 7:09:37 PM</br>
	 * @throws Exception
	 * @param reimburseItemCode {@link String} 报销科目编码
	 * @return {@link List}{@literal <}@{@link IndustryDto}{@literal >} 发票类别对应券行业类别编码列表
	 */
	List<IndustryDto> findIndustrysByReimburseCode(String reimburseItemCode);

}
