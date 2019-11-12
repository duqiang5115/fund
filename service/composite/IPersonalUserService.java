/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  ITradeFlowBusiService.java   
 * @Package com.taolue.baoxiao.fund.service.composite   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 10, 2018 9:04:02 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;

import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.api.dto.TradeFlowSearchParams;

/**
 * 
 * <p>ClassName:  IPersonalUserService </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: yangfan</br>
 * <p>date:   2019年3月19日 下午8:40:56 </br>  
 *     
 * @Copyright: 2019 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IPersonalUserService {
	/**
	 * 
	 * <p>名称:类IPersonalUserService中的rechargeBusiness方法</br>    
	 * <p>描述:独立C充值生成流水</br> 
	 * <p>作者: yangfan</br> 
	 * <p>日期: 2019年3月19日 下午8:41:55</br>
	 * @throws Exception
	 * @param assignDto
	 * @return
	 */
	public Boolean rechargeBusiness(AssignDto assignDto);
	
	/**
	 * 
	 * <p>名称:类IPersonalUserService中的rechargeBusiness方法</br>    
	 * <p>描述:独立C购买券生成资金流水</br> 
	 * <p>作者: yangfan</br> 
	 * <p>日期: 2019年3月19日 下午8:41:55</br>
	 * @throws Exception
	 * @param assignDto
	 * @return
	 */
	public Boolean buyingConsumerVouchers(AssignDto assignDto);
	/**
	 * 
	 * @Title: buyingStaffVouchers   
	 * @Description: 企业员工购买券生成资金流水
	 * @author: zyj
	 * @date:   2019年6月3日 下午3:06:53  
	 * @param: @param assignDto
	 * @param: @return      
	 * @return: Boolean      
	 * @throws
	 */
	public Boolean buyingStaffVouchers(AssignDto assignDto);
	
}
