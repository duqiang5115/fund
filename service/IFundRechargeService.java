package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.FundRechargeDTO;
import com.taolue.baoxiao.fund.api.dto.FundWithDrawDTO;
import com.taolue.baoxiao.fund.api.dto.OrderBusiQueryDto;
import com.taolue.baoxiao.fund.api.dto.OrderBusiReturnDto;
import com.taolue.baoxiao.fund.api.vo.FundRechargeVo;
import com.taolue.baoxiao.fund.api.vo.FundWithDrawVo;
import com.taolue.baoxiao.fund.entity.FundRecharge;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 充值表 服务类
 * </p>
 *
 * @author duqiang
 * @since 2019-03-06
 */
public interface IFundRechargeService extends IService<FundRecharge> {

	/**
	 * 
	 * @Title: queryRechargePage   
	 * @Description:充值列表查询  
	 * @param: @param query
	 * @param: @param dto
	 * @param: @return      
	 * @return: Page<FundRechargeVo>      
	 * @throws
	 */
	public Page<FundRechargeVo> queryRechargePage(Query query,FundRechargeDTO dto);
	
	
	/**
	 * 
	 * @Title: queryWithdrawPage   
	 * @Description: 提现列表查询
	 * @param: @param query
	 * @param: @param dto
	 * @param: @return      
	 * @return: Page<FundWithDrawVo>      
	 * @throws
	 */
	public Page<FundWithDrawVo> queryWithdrawPage(Query query,FundWithDrawDTO dto);
	
	/**
	 * 
	 * @Title: editRecharge   
	 * @Description: 编辑充值  
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: boolean      
	 * @throws
	 */
	public boolean editRecharge(FundRechargeDTO dto);
	
	/**
	 * 
	 * @Title: addRecharge   
	 * @Description: 添加充值记录 
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: FundRechargeVo      
	 * @throws
	 */
	public FundRechargeVo addRecharge(FundRechargeDTO dto);
	
	
}
