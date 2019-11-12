/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  IReimburseBalanceService.java   
 * @Package com.taolue.baoxiao.fund.service.composite   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 28, 2018 3:31:45 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite;

import java.math.BigDecimal;

import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.fund.common.exception.FundServiceException;

/**   
 * <p>ClassName:  IReimburseBalanceService </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: shilei</br>
 * <p>date:   Dec 28, 2018 3:31:45 PM </br>  
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IReimburseBalanceService {
	
	/**
	 * 
	 * <p>名称:类IReimburseBalanceService中的findReimburseAvailableAmount方法</br>    
	 * <p>描述: 查询指定会员在指定的公司下的剩余报销额度（券共享类型）</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Dec 28, 2018 4:52:54 PM</br>
	 * @throws Exception
	 * @param memberId {@link String} 会员编号
	 * @param companyId {@link String} 会员当前登陆公司编号
	 * @param memberCate {@link String} 会员当前类型编号
	 * @return {@link BigDecimal} 剩余可用于报销的消费券总额度 
	 */
	BigDecimal findReimburseAvailableAmount(String memberId, String companyId, String memberCate);
	
	/**
	 * 
	 * <p>名称:类IReimburseBalanceService中的determineEnoughAmount方法</br>    
	 * <p>描述:判断每条报销明细是否足额</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Dec 28, 2018 8:36:08 PM</br>
	 * @throws FundServiceException - code = 9069 如果有不足额的明细；则返回编码为9069的FundServiceException
	 * @param assignDto {@link AssignDto} 对象，需要判断是否足额的报销明细的参数对象
	 * 
	 * @return {@link AssignDto} 对象 返回判断结果
	 */
	AssignDto determineEnoughAmount(AssignDto assignDto);
	
}
