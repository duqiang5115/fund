package com.taolue.baoxiao.fund.service;

import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.fund.api.vo.BillVoucherComposeVo;
import com.taolue.baoxiao.fund.entity.BillVoucher;

/**
 * <p>
 * 账单明细支付信息，针对某一账单项目，缴费的记录；目前此表只做记录，并不进行入账和对账 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2018-08-24
 */
public interface IBillVoucherService extends IService<BillVoucher> {
	/**
	 * 
	 * @Title: addelVouchers   
	 * @Description: 新建或者删除账单凭证
	 * @Author: shilei
	 * @date:   Nov 6, 2018 10:21:13 AM  
	 * @param vouchers List<BillVoucherComposeVo> 需要新建或者删除的凭证和凭证文件集合
	 * 如果凭证对账的id属性存在，则会执行删除
	 * 删除是置删除标识为删除状态并且将支付额度取反
	 * @return List<BillVoucher>  新建或者删除的BillVoucher对象集合    
	 * @throws
	 *
	 */
	List<BillVoucher> addelVouchers(List<BillVoucherComposeVo> vouchers);
	
	/**
	 * 
	 * @Title: calculateBill   
	 * @Description: 销账处理
	 * @Author: shilei
	 * @date:   Nov 6, 2018 10:24:05 AM  
	 * @param billItemNo String 账单项目编号
	 * @param payAmount BigDecimal 本次支付额度       
	 * @throws 
	 *
	 */
	 void calculateBill(String billItemNo, BigDecimal payAmount);
}
