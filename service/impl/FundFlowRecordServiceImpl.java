package com.taolue.baoxiao.fund.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.taolue.baoxiao.fund.api.vo.FundFlowRecordVo;
import com.taolue.baoxiao.fund.entity.FundFlowRecord;
import com.taolue.baoxiao.fund.entity.FundFlowRecordDetail;
import com.taolue.baoxiao.fund.mapper.FundFlowRecordDetailMapper;
import com.taolue.baoxiao.fund.mapper.FundFlowRecordMapper;
import com.taolue.baoxiao.fund.service.IFundFlowRecordService;

/**
 * <p>
 * 账单打款流程记录表 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-09-02
 */
@Service
public class FundFlowRecordServiceImpl extends ServiceImpl<FundFlowRecordMapper, FundFlowRecord> implements IFundFlowRecordService {
	org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private  FundFlowRecordDetailMapper fundFlowRecordDetailDetailMapper;

	@Override
	public FundFlowRecordVo findFundFlowRecord(FundFlowRecordVo fundVo) {
			logger.info("查询入参参数》》》》》"+JSON.toJSONString(fundVo));
			return this.baseMapper.findFundFlowRecord(fundVo);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public FundFlowRecordVo addFundFlowRecord(FundFlowRecordVo vo) {
		FundFlowRecord  entity=new FundFlowRecord();
		FundFlowRecordDetail entityDetail=new FundFlowRecordDetail();
		vo.setCreator(vo.getMemberId());
		vo.setUpdator(vo.getMemberId());
		vo.setDelFlag("0");
		try {
		logger.info("入参参数》》》》》"+JSON.toJSONString(vo));
		if(StringUtils.isBlank(vo.getFlowCode())) {
			vo.setId(IdWorker.getIdStr());
			vo.setFlowCode(IdWorker.getIdStr());
			BeanUtils.copyProperties(vo,entity);
			logger.info("添加主表入参参数》》》》》"+JSON.toJSONString(entity));
			this.baseMapper.insert(entity);
		}else {
			FundFlowRecord  fundEntity=new FundFlowRecord();
			fundEntity.setStatus(vo.getStatus());
			fundEntity.setId(vo.getId());
			fundEntity.setBusiParams(vo.getBusiParams());
			logger.info("修改状态主表入参参数》》》》》"+JSON.toJSONString(fundEntity));			
			this.baseMapper.updateById(fundEntity);
		}
		//判断明细是否存在 如果存在修改 否则新增
		if(StringUtils.isNotBlank(vo.getDetailId())) {
			FundFlowRecordDetail detail = fundFlowRecordDetailDetailMapper.selectById(vo.getDetailId());
			if(detail.getFlowNo().equals(vo.getFlowNo())) {
				FundFlowRecordDetail fundEntityDetail=new FundFlowRecordDetail();
				fundEntityDetail.setStatus(vo.getStatus());
				fundEntityDetail.setId(vo.getDetailId());
				fundEntityDetail.setBusiReturn(vo.getBusiReturn());
				logger.info("修改状态主表入参参数》》》》》"+JSON.toJSONString(fundEntityDetail));
				this.fundFlowRecordDetailDetailMapper.updateById(fundEntityDetail);
			}else {
				BeanUtils.copyProperties(vo,entityDetail);
				entityDetail.setId(IdWorker.getIdStr());
				entityDetail.setStatus(vo.getStatus());
				entityDetail.setBusiParams(vo.getBusiParams());
				entityDetail.setBusiReturn(vo.getBusiReturn());
				logger.info("添加子表入参参数》》》》》"+JSON.toJSONString(entityDetail));
				this.fundFlowRecordDetailDetailMapper.insert(entityDetail);
			}
		}else {
			BeanUtils.copyProperties(vo,entityDetail);
			entityDetail.setId(IdWorker.getIdStr());
			entityDetail.setStatus(vo.getStatus());
			entityDetail.setBusiParams(vo.getBusiParams());
			entityDetail.setBusiReturn(vo.getBusiReturn());
			logger.info("添加子表入参参数》》》》》"+JSON.toJSONString(entityDetail));
			this.fundFlowRecordDetailDetailMapper.insert(entityDetail);
		}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("添加记录表失败");
		}
		return vo;
	}

}
