/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  InviteFriendsService.java   
 * @Package com.taolue.baoxiao.fund.service.composite   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: DELL  
 * @date:   2019年4月1日 下午3:08:13   
 * @version V1.0 
 * @Copyright: 2019 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;

/**   
 * <p>ClassName:  InviteFriendsService </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: DELL</br>
 * <p>date:   2019年4月1日 下午3:08:13 </br>  
 *     
 * @Copyright: 2019 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */

public interface InviteFriendsService {
	/**
	 * 
	 * <p>名称:类InviteFriendsService中的returnCalculation方法</br>    
	 * <p>描述:邀请好友返现记录订单</br> 
	 * <p>作者: yangfan</br> 
	 * <p>日期: 2019年4月1日 下午9:01:46</br>
	 * @throws Exception
	 * @param r {@link R}{@literal <}{@link AssignDto}{@literal >}
	 */
	public void returnCalculation(R<AssignDto> r);
	
	/**
	 * 
	 * <p>名称:类InviteFriendsService中的recordFundBalance方法</br>    
	 * <p>描述:邀请好友返现记录资金流水</br> 
	 * <p>作者: yangfan </br> 
	 * <p>日期: 2019年4月1日 下午9:03:22</br>
	 * @throws Exception
	 * @param r
	 */
	public void recordFundBalance(AssignDto assignDto);
	
	/**
	 * 
	 * <p>名称:类InviteFriendsService中的recordFundBalance方法</br>    
	 * <p>描述:判断该用户是否是首单</br> 
	 * <p>作者: yangfan </br> 
	 * <p>日期: 2019年4月1日 下午9:03:22</br>
	 * @throws Exception
	 * @param r
	 */
	public TbFundTradeFlow judgeTheFirstOrder(String memberId);
}
