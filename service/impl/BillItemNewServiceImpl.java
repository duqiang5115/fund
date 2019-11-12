package com.taolue.baoxiao.fund.service.impl;

import com.taolue.baoxiao.fund.api.dto.BillItemNewDto;
import com.taolue.baoxiao.fund.api.vo.BillItemNewVo;
import com.taolue.baoxiao.fund.entity.BillItemNew;
import com.taolue.baoxiao.fund.mapper.BillItemNewMapper;
import com.taolue.baoxiao.fund.service.IBillItemNewService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * <p>
 * 账单明细表，每一条为账单中的没一个账单项 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-06-13
 */
@Service
public class BillItemNewServiceImpl extends ServiceImpl<BillItemNewMapper, BillItemNew> implements IBillItemNewService {

	@Override
	public List<BillItemNewVo> queryBillItem(BillItemNewDto dto) {
		return this.baseMapper.queryBillItem(dto);
	}

	@Override
	public boolean updateStatusByBillNo(BillItemNewDto dto) {
		return this.baseMapper.updateStatusByBillNo(dto);
	}

}
