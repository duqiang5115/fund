package com.taolue.baoxiao.fund.service.impl;

import com.taolue.baoxiao.fund.api.dto.FundCouponTaxCodeDto;
import com.taolue.baoxiao.fund.api.vo.FundCouponTaxCodeVo;
import com.taolue.baoxiao.fund.entity.FundCouponTaxCode;
import com.taolue.baoxiao.fund.mapper.FundCouponTaxCodeMapper;
import com.taolue.baoxiao.fund.service.IFundCouponTaxCodeService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * <p>
 * 消费券对应的发票编码 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-06-13
 */
@Service
public class FundCouponTaxCodeServiceImpl extends ServiceImpl<FundCouponTaxCodeMapper, FundCouponTaxCode> implements IFundCouponTaxCodeService {

	@Override
	public List<FundCouponTaxCodeVo> findCouponTaxCode(FundCouponTaxCodeDto dto) {
		return this.baseMapper.findCouponTaxCode(dto);
	}

}
