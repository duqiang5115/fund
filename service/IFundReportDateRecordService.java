package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.fund.api.vo.FundReportDateRecordVo;
import com.taolue.baoxiao.fund.entity.FundReportDateRecord;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 周期统计消费券账户记录表 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-09-24
 */
public interface IFundReportDateRecordService extends IService<FundReportDateRecord> {
    /****
     * @Title IFundReportDateRecordService.createReportDate
     * @Description: 生成周期性报表
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param type 报表类型
     * @param couponId 券id
     * @return
     * 
     * @version: 1.0 
     * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-09-25 13:57:51  修改内容 :
     */
	public boolean createReportDate(Date startTime, Date endTime, String type,String couponId);

	/***
	 * @Title IFundReportDateRecordService.findFundReportDateRecordList
	 * @Description: 查询已经统计好的报表
	 *
	 * @param vo
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-09-25 14:39:47  修改内容 :
	 */
	public List<FundReportDateRecordVo> findFundReportDateRecordList(FundReportDateRecordVo vo);

}
