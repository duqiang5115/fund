package com.taolue.baoxiao.fund.service;

import java.util.List;

import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.fund.api.dto.BalanceSearchParams;
import com.taolue.baoxiao.fund.entity.BusinessApplyBalance;

/**
 * <p>
 * 业务单据相关账户信息 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-03-08
 */
public interface IBusinessApplyBalanceService extends IService<BusinessApplyBalance> {
	
	List<BusinessApplyBalance> selectSoldingAndSoldedAmount(BalanceSearchParams params);
}
