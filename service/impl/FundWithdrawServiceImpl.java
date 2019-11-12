package com.taolue.baoxiao.fund.service.impl;

import com.taolue.baoxiao.fund.api.dto.FundWithDrawDTO;
import com.taolue.baoxiao.fund.api.vo.FundWithDrawVo;
import com.taolue.baoxiao.fund.entity.FundWithdraw;
import com.taolue.baoxiao.fund.mapper.FundWithdrawMapper;
import com.taolue.baoxiao.fund.service.IFundWithdrawService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户提现申请表 服务实现类
 * </p>
 *
 * @author duqiang
 * @since 2019-03-06
 */
@Service
public class FundWithdrawServiceImpl extends ServiceImpl<FundWithdrawMapper, FundWithdraw> implements IFundWithdrawService {

	@Autowired
	private FundWithdrawMapper fundWithdrawMapper;
	@Override
	public List<FundWithDrawVo> findFundWithdraw(FundWithDrawDTO dto){
		return this.fundWithdrawMapper.queryWithDrawPage(dto);
	}
}
