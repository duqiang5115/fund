package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.fund.api.dto.FundCouponTaxCodeDto;
import com.taolue.baoxiao.fund.api.vo.FundCouponTaxCodeVo;
import com.taolue.baoxiao.fund.entity.FundCouponTaxCode;

import java.util.List;

import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 消费券对应的发票编码 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-06-13
 */
public interface IFundCouponTaxCodeService extends IService<FundCouponTaxCode> {

	/**
	 * 
	 * @Title: findCouponTaxCode   
	 * @Description: 查询券对应的发票信息   
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: FundCouponTaxCodeVo      
	 * @throws
	 */
	List<FundCouponTaxCodeVo> findCouponTaxCode(FundCouponTaxCodeDto dto);
}
