package com.taolue.baoxiao.fund.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.fund.api.vo.FundReportDateRecordVo;
import com.taolue.baoxiao.fund.entity.FundReportDateRecord;
import com.taolue.baoxiao.fund.mapper.FundReportDateRecordMapper;
import com.taolue.baoxiao.fund.service.IFundReportDateRecordService;
import com.xiaoleilu.hutool.collection.CollUtil;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 周期统计消费券账户记录表 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-09-24
 */
@Service
@Slf4j
public class FundReportDateRecordServiceImpl extends ServiceImpl<FundReportDateRecordMapper, FundReportDateRecord> implements IFundReportDateRecordService {

	@Autowired
	private FundReportDateRecordMapper fundReportDateRecordMapper;
	@Override
	public boolean createReportDate(Date startTime, Date endTime,String type,String couponId) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		map.put("couponId", couponId);
		List<FundReportDateRecord>  resultList = this.fundReportDateRecordMapper.findInitFundReportDate(map);
		if(CollUtil.isNotEmpty(resultList)) {
			String code = getCode(startTime,endTime);
			Wrapper<FundReportDateRecord> wrapper = new EntityWrapper<>();
			wrapper.eq("coupon_id", couponId);
			wrapper.eq("code", code);
			int size = this.selectCount(wrapper);
			if(size>0) {
				log.error(code+"周期报表数据已经存在，不再生成");
				return false;
			}
			Date now = new Date(System.currentTimeMillis());
			for (FundReportDateRecord record : resultList) {
				record.setCode(code);
				record.setStartTime(startTime);
				record.setEndTime(endTime);
				record.setDelFlag(CommonConstant.STATUS_NORMAL);
				record.setType(type);
				record.setCreateTime(now);
				record.setUpdatedTime(now);
			}
			return this.insertBatch(resultList);
		}
		return false;
	}
	/***
	 * @Title FundReportDateRecordServiceImpl.getCode
	 * @Description: 生产code
	 *
	 * @param startTime
	 * @param endTime
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-09-25 13:59:07  修改内容 :
	 */
	private String getCode(Date startTime, Date endTime) {
		String start = DateUtil.format(startTime, DatePattern.PURE_DATETIME_FORMAT);
		String end = DateUtil.format(endTime, DatePattern.PURE_DATETIME_FORMAT);
		return start.concat("_").concat(end);
	}
	/***
	 * 
	 *
	 * @Title FundReportDateRecordServiceImpl.findFundReportDateRecordList
	 * @Description: 查询已经统计好的报表
	 *
	 * @param vo
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-09-25 14:39:14  修改内容 :
	 */
	@Override
	public List<FundReportDateRecordVo> findFundReportDateRecordList(FundReportDateRecordVo vo) {
		return this.fundReportDateRecordMapper.findFundReportDateRecordList(vo);
	}
}
