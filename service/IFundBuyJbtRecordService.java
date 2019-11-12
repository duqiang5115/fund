package com.taolue.baoxiao.fund.service;

import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.entity.FundBuyJbtRecord;

/**
 * <p>
 * 购买券嘉白条关联表 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-08-14
 */
public interface IFundBuyJbtRecordService extends IService<FundBuyJbtRecord> {
	
	/**
	 * 
	 * @Title: addCouponTaxCod   
	 * @Description: 添加购买券嘉白条关联表信息
	 * @author: zyj
	 * @date:   2019年8月14日 上午15:09:16  
	 * @param: @param vo
	 * @param: @return      
	 * @return: Boolean      
	 * @throws
	 */
	public boolean addJbtRecord(FundBuyJbtRecord vo);

	public void checkIntelligentReimburseCode(FundBuyJbtRecord vo);
}
