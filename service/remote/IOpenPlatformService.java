package com.taolue.baoxiao.fund.service.remote;

import java.math.BigDecimal;

import com.taolue.baoxiao.fund.api.openplatform.IDockOpenPlatformService;
import com.taolue.baoxiao.fund.api.vo.RemoteResultVo;
import com.taolue.dock.api.dto.CashOrderDto;
import com.taolue.dock.api.vo.CashOrderVo;

/**
 * 
 * @ClassName:  IOpenPlatformService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:41:24   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IOpenPlatformService {
	
	IDockOpenPlatformService getDockOpenPlatformService();
	
	/**
	 * 
	 * @Title: findCompanyRecharge   
	 * @Description: 从总部获取当前可以用的保证金额度
	 * @Author: shilei
	 * @date:   2018年9月2日 下午3:40:37  
	 * @param: @param memberId
	 * @param: @return      
	 * @return: BigDecimal      
	 * @throws
	 */
	public BigDecimal findCompanyRecharge(String memberId);
	
	RemoteResultVo<CashOrderVo> cashOrder(CashOrderDto cashOrderDto);
	
	RemoteResultVo<BigDecimal> orderQuery(String withdrawNo);
	
	public void tradeRefundnr(BigDecimal amount, String flowNo, String orderNo);
}
