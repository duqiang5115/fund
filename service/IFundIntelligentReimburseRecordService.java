package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.fund.api.vo.FundIntelligentReimburseRecordVo;
import com.taolue.baoxiao.fund.entity.FundIntelligentReimburseRecord;
import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 智能报销记录表 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-08-08
 */
public interface IFundIntelligentReimburseRecordService extends IService<FundIntelligentReimburseRecord> {
	/**
	 * 
	 * @Title: findCouponTaxCod   
	 * @Description: 查询记录  参数 companyId  couponId  intelligentReimburseCode  memberId
	 * @author: zyj
	 * @date:   2019年8月9日 下午6:01:00  
	 * @param: @param vo
	 * @param: @return      
	 * @return: FundIntelligentReimburseRecordVo      
	 * @throws
	 */
	public	FundIntelligentReimburseRecordVo findCouponTaxCod(FundIntelligentReimburseRecordVo vo);
	/**
	 * 
	 * @Title: addCouponTaxCod   
	 * @Description: 添加智能买券记录
	 * @author: zyj
	 * @date:   2019年8月12日 上午11:09:16  
	 * @param: @param vo
	 * @param: @return      
	 * @return: Boolean      
	 * @throws
	 */
	public	FundIntelligentReimburseRecordVo addCouponTaxCod(FundIntelligentReimburseRecordVo vo);
}
