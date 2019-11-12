package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.fund.api.dto.FundWithDrawDTO;
import com.taolue.baoxiao.fund.api.vo.FundWithDrawVo;
import com.taolue.baoxiao.fund.entity.FundWithdraw;

import java.util.List;

import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 用户提现申请表 服务类
 * </p>
 *
 * @author duqiang
 * @since 2019-03-06
 */
public interface IFundWithdrawService extends IService<FundWithdraw> {

	
	/***
	 * @Title IFundWithdrawService.findFundWithdraw
	 * @Description: 查询提现信息
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-03-18 14:08:42  修改内容 :
	 */
	public List<FundWithDrawVo> findFundWithdraw(FundWithDrawDTO dto);

}
