package com.taolue.baoxiao.fund.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.fund.entity.FundBuyJbtRecord;
import com.taolue.baoxiao.fund.mapper.FundBuyJbtRecordMapper;
import com.taolue.baoxiao.fund.service.IFundBuyJbtRecordService;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 购买券嘉白条关联表 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-08-14
 */
@Slf4j
@Service
public class FundBuyJbtRecordServiceImpl extends ServiceImpl<FundBuyJbtRecordMapper, FundBuyJbtRecord> implements IFundBuyJbtRecordService {

	@Autowired
	private  FundBuyJbtRecordMapper  fundBuyJbtRecordMapper;
	
	@Override
	public boolean addJbtRecord(FundBuyJbtRecord vo) {
		log.info("添加购买券嘉白条关联表信息入参参数》》》》》"+JSON.toJSONString(vo));
		this.fundBuyJbtRecordMapper.insert(vo);
		return true;
	}

	@Override
	public void checkIntelligentReimburseCode(FundBuyJbtRecord vo) {
		log.info("校验智能报销单号是否存在入参》》》》》"+JSON.toJSONString(vo));
		FundBuyJbtRecord newParams=new FundBuyJbtRecord();
		newParams.setIntelligentReimburseCode(vo.getIntelligentReimburseCode());
		FundBuyJbtRecord record=this.fundBuyJbtRecordMapper.selectOne(vo);
		log.info("校验智能报销单号是否存在返回值》》》》》"+JSON.toJSONString(record));
		if(ObjectUtil.isNotNull(record)) {
			record.setBuyCode(vo.getBuyCode());
			this.fundBuyJbtRecordMapper.updateById(record);
		}
	}
	
	

}
