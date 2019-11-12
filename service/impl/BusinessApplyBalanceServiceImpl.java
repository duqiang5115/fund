package com.taolue.baoxiao.fund.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.fund.api.dto.BalanceSearchParams;
import com.taolue.baoxiao.fund.entity.BusinessApplyBalance;
import com.taolue.baoxiao.fund.mapper.BusinessApplyBalanceMapper;
import com.taolue.baoxiao.fund.service.IBusinessApplyBalanceService;

/**
 * <p>
 * 业务单据相关账户信息 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-03-08
 */
@Service
public class BusinessApplyBalanceServiceImpl extends ServiceImpl<BusinessApplyBalanceMapper, BusinessApplyBalance> implements IBusinessApplyBalanceService {

	/**   
	 * <p>Title: selectSoldingAndSoldedAmount</p>   
	 * <p>Description: </p>   
	 * @param params
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.IBusinessApplyBalanceService#selectSoldingAndSoldedAmount(com.taolue.baoxiao.fund.api.dto.BalanceSearchParams)   
	 */  
	@Override
	public List<BusinessApplyBalance> selectSoldingAndSoldedAmount(BalanceSearchParams params) {
		return this.baseMapper.selectSoldingAndSoldedAmount(params);
	}

}
