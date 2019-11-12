package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.ApplyInvoiceDetailDto;
import com.taolue.baoxiao.fund.api.dto.BillBaseNewDto;
import com.taolue.baoxiao.fund.api.dto.FundBillInvoiceSettingDto;
import com.taolue.baoxiao.fund.api.vo.ApplyInvoiceDetailVo;
import com.taolue.baoxiao.fund.api.vo.BillBaseNewVo;
import com.taolue.baoxiao.fund.api.vo.FundBillInvoiceSettingVo;
import com.taolue.baoxiao.fund.entity.BillBaseNew;
import com.taolue.member.api.vo.MemberInvoiceComRuleVo;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 账单主表，记录账单基础信息
 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-06-13
 */
public interface IBillBaseNewService extends IService<BillBaseNew> {

	/**
	 * 
	 * @Title: createBill   
	 * @Description: 生成账单   
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<Boolean>      
	 * @throws
	 */
	public R<Boolean> createBill(BillBaseNewDto dto)throws Exception;
	
	
	/**
	 * 
	 * @Title: queryBillBasePageByParams   
	 * @Description: 账单列表   
	 * @param: @param query
	 * @param: @param queryParams
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<Page<BillBaseVo>>      
	 * @throws
	 */
	public R<Page<BillBaseNewVo>> queryBillBasePageByParams(Query query, BillBaseNewDto queryParams);
	
	
	/**
	 * 
	 * @Title: queryBillBaseInfo   
	 * @Description: 查询账单详情   
	 * @param: @param queryParams
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<BillBaseNewVo>      
	 * @throws
	 */
	public R<BillBaseNewVo> queryBillBaseInfo(BillBaseNewDto queryParams);
	
	/**
	 * 
	 * @Title: getItemInfo   
	 * @Description: 查询科目信息  
	 * @param: @param set
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<List<MemberInvoiceComRuleVo>>      
	 * @throws
	 */
	public R<List<MemberInvoiceComRuleVo>> getItemInfo(FundBillInvoiceSettingVo set) throws Exception;
	
	/**
	 * 
	 * @Title: invoiceSetting   
	 * @Description:发票设置接口  
	 * @param: @param set
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<Boolean>      
	 * @throws
	 */
	public R<Boolean> invoiceSetting(FundBillInvoiceSettingDto dto);
	
	/**
	 * 
	 * @Title: toMakeMoney   
	 * @Description: 打款
	 * @param: @param queryParams
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<Boolean>      
	 * @throws
	 */
	public R<BillBaseNew> toMakeMoney(BillBaseNewDto queryParams)throws Exception;
	/**
	 * 
	 * @Title: invoicePhone   
	 * @Description: 发票图片查看
	 * @author: zyj
	 * @date:   2019年6月26日 下午1:47:48  
	 * @param: @param queryParams
	 * @param: @return      
	 * @return: R<List<ApplyInvoiceDetailVo>>      
	 * @throws
	 */
	public R<List<ApplyInvoiceDetailVo>> invoicePhone(ApplyInvoiceDetailDto queryParams) throws Exception;
	
	/**
	 * 
	 * @Title: toInvoice   
	 * @Description: 去开票  
	 * @param: @param baseNew
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<Boolean>      
	 * @throws
	 */
	public R<Boolean> toInvoice(BillBaseNew baseNew)throws Exception;
	
	/**
	 * 
	 * @Title: baseCheckInvoice   
	 * @Description: 账单开票
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<Boolean>      
	 * @throws
	 */
	public R<Boolean> baseCheckInvoice()throws Exception;
	
	/**
	   * 
	   * @Title: queryBaseSummary   
	   * @Description: 账单汇总
	   * @param: @return    
	   * @author: duqiang     
	   * @return: List<BillBaseNewVo>      
	   * @throws
	   */
	  public R<List<BillBaseNewVo>> queryBaseSummary();
	  
	  /**
	   * 
	   * @Title: queryInvoiceApplySummary   
	   * @Description: 发票申请汇总 
	   * @param: @return    
	   * @author: duqiang     
	   * @return: List<FundBillInvoiceSettingVo>      
	   * @throws
	   */
	  public R<List<FundBillInvoiceSettingVo>>   queryInvoiceApplySummary();


	  /**
	   * 根据账单号查询账单中嘉白条额度
	   * @param billNo
	   * @return
	   */
	public R<List<BillBaseNewVo>> queryJiaStripQuota(String billNo);
	  /**
	   * 
	   * @Title: queryBillBase   
	   * @Description:运营平台账单导出（禁止删除和修改） 
	   * @author: zyj
	   * @date:   2019年8月16日 下午2:20:07  
	   * @param: @param queryParams
	   * @param: @return      
	   * @return: List<BillBaseNewVo>      
	   * @throws
	   */
	  public R<List<BillBaseNewVo>> queryBillBase( BillBaseNewDto queryParams);
}
