/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  RefactorInvoicServiceFactoryImpl.java   
 * @Package com.taolue.baoxiao.fund.service.remote.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 28, 2018 6:56:45 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.remote.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.invoice.IRefactorInvoiceReimburseService;
import com.taolue.baoxiao.fund.api.invoice.IRefactorReimburseItemService;
import com.taolue.baoxiao.fund.api.invoice.IRefactorVoucherCategoryService;
import com.taolue.baoxiao.fund.service.remote.IRefactorInvoicServiceFactory;
import com.taolue.invoice.api.dto.IndustryDto;

import cn.hutool.core.collection.CollUtil;

/**   
 * <p>ClassName:  RefactorInvoicServiceFactoryImpl </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: shilei</br>
 * <p>date:   Dec 28, 2018 6:56:45 PM </br>  
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
public class RefactorInvoicServiceFactoryImpl implements IRefactorInvoicServiceFactory {

	@Autowired
	private IRefactorVoucherCategoryService  refactorVoucherCategoryService;
	
	@Autowired
	private IRefactorReimburseItemService refactorReimburseItemService;
	
	@Autowired
	private IRefactorInvoiceReimburseService refactorInvoiceReimburseService;

	/**  
	 * <p>Title:getRefactorVoucherCategoryService</p><BR>  
	 * <p>Description:获取属性refactorVoucherCategoryService的值<BR>  
	 * @return IRefactorVoucherCategoryService <BR>  
	 */
	public IRefactorVoucherCategoryService getRefactorVoucherCategoryService() {
		return refactorVoucherCategoryService;
	}
	
	/**  
	 * <p>Title:getRefactorReimburseItemService</p><BR>  
	 * <p>Description:获取属性refactorReimburseItemService的值<BR>  
	 * @return IRefactorReimburseItemService <BR>  
	 */
	public IRefactorReimburseItemService getRefactorReimburseItemService() {
		return refactorReimburseItemService;
	}

	/**  
	 * <p>Title:getRefactorInvoiceReimburseService</p><BR>  
	 * <p>Description:获取属性refactorInvoiceReimburseService的值<BR>  
	 * @return IRefactorInvoiceReimburseService <BR>  
	 */
	public IRefactorInvoiceReimburseService getRefactorInvoiceReimburseService() {
		return refactorInvoiceReimburseService;
	}

	public List<IndustryDto> findIndustrysByReimburseCode(String reimburseItemCode) {
		R<List<IndustryDto>> results = 
				this.refactorReimburseItemService.findIndustrysByReimburseItemCode(reimburseItemCode);
		if (null != results && CollUtil.isNotEmpty(results.getData())) {
			return results.getData();
		}
		return null;
	}
	
}
