package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.InvoiceAmountConfigDto;
import com.taolue.baoxiao.fund.entity.FundInvoiceAmountConfig;
import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 开票公司额度配置表 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-07-02
 */
public interface IFundInvoiceAmountConfigService extends IService<FundInvoiceAmountConfig> {

	/**
	 * 
	 * @Title: invoiceAmountCheck   
	 * @Description: 开票公司额度校验
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<Boolean>      
	 * @throws
	 */
	public R<Boolean> invoiceAmountCheck(InvoiceAmountConfigDto dto) throws Exception;

	
	/**
	 * 
	 * @Title: loadInvoiceAmount   
	 * @Description: 初始化抬头额度  
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<Boolean>      
	 * @throws
	 */
	public R<Boolean> loadInvoiceAmount()throws Exception;
}
