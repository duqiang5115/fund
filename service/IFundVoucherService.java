package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.common.util.R;

/** 
* @author kwd
* @version 创建时间：2019年10月28日 下午5:59:35 
* @desc [类说明] 
*/
public interface IFundVoucherService {

	/****
	 * @Title IFundVoucherService.verification
	 * @Description: 用户抵用券核销
	 *
	 * @param memberId
	 * @param awardCouponId
	 * @param sourceFrom
	 * @param bizNo
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-10-29 15:36:19  修改内容 :
	 */
	public R<Boolean> verification(String memberId, String awardCouponId, String sourceFrom, String bizNo);

	/****
	 * @Title IFundVoucherService.rebuy
	 * @Description: 退券
	 *
	 * @param memberId
	 * @param awardCouponId
	 * @param sourceFrom
	 * @param OldBizNo
	 * @param bizNo
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-10-30 13:02:13  修改内容 :
	 */
	public R<Boolean> rebuy(String memberId, String awardCouponId, String sourceFrom, String OldBizNo, String bizNo);

}
