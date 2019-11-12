/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  IAccountService.java   
 * @Package com.taolue.baoxiao.fund.service   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   2018年8月28日 上午10:39:33   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite;

import java.util.List;

import com.baomidou.mybatisplus.plugins.Page;
import com.taolue.baoxiao.fund.api.dto.CashQuerysDto;
import com.taolue.baoxiao.fund.api.vo.CashQuerysVo;
import com.taolue.baoxiao.fund.api.vo.RemoteResultVo;
import com.taolue.baoxiao.fund.entity.BusinessApplyBalance;
import com.taolue.baoxiao.fund.entity.BusinessApplyCharges;
import com.taolue.dock.api.dto.CashOrderDto;
import com.taolue.dock.api.vo.CashOrderVo;
import com.taolue.member.api.vo.BankCardInfoVo;

/**   
 * @ClassName:  ISoldProxyService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月28日 上午10:39:33   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IWithdrawService {
	
	void createWithdrawBalances(boolean isContainsPasue);
	
	void fireWithdrawWork() ;
	
	List<BusinessApplyBalance> findComplatedWithdrawBalanceByMemberId(String memberId, String code,
			String[] statusArray);
	
	RemoteResultVo<CashOrderVo> withdrawRequest(CashOrderDto cashOrderDto);
	
	RemoteResultVo<CashOrderVo> withdrawRequest(BusinessApplyCharges withdrawCharge, 
			BusinessApplyBalance witdrawBalance, BankCardInfoVo bankCard);
	Page<CashQuerysVo> cashQuerys(CashQuerysDto page);
	Boolean initiateCashApplication(CashQuerysDto cashQuerysDto);
}
