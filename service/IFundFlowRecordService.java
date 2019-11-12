package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.fund.api.vo.FundFlowRecordVo;
import com.taolue.baoxiao.fund.entity.FundFlowRecord;
import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 账单打款流程记录表 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-09-02
 */
public interface IFundFlowRecordService extends IService<FundFlowRecord> {

	/**
	 * 
	 * @Title: findCouponTaxCod   
	 * @Description: 查询打款记录  参数 companyId flowCode  memberId busiCode
	 * @author: fbh
	 * @date:   2019年8月9日 下午6:01:00  
	 * @param: @param vo
	 * @param: @return      
	 * @return: FundFlowRecordVo      
	 * @throws
	 */
	public FundFlowRecordVo findFundFlowRecord(FundFlowRecordVo fundVo);

	/**
	 * 
	 * @Title: addFundFlowRecord   
	 * @Description: 添加打款记录 
	 * @author: fbh
	 * @date:   2019年8月9日 下午6:01:00  
	 * @param: @param vo
	 * @param: @return      
	 * @return: FundFlowRecordVo      
	 * @throws
	 */
	public FundFlowRecordVo addFundFlowRecord(FundFlowRecordVo fundVo);

}
