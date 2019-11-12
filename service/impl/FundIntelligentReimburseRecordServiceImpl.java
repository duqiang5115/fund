package com.taolue.baoxiao.fund.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.taolue.baoxiao.fund.api.vo.FundIntelligentReimburseRecordVo;
import com.taolue.baoxiao.fund.entity.FundIntelligentReimburseRecord;
import com.taolue.baoxiao.fund.entity.FundIntelligentReimburseRecordDetail;
import com.taolue.baoxiao.fund.mapper.FundIntelligentReimburseRecordDetailMapper;
import com.taolue.baoxiao.fund.mapper.FundIntelligentReimburseRecordMapper;
import com.taolue.baoxiao.fund.service.IFundIntelligentReimburseRecordService;

import org.apache.commons.lang.StringUtils;
/**
 * <p>
 * 智能报销记录表 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-08-08
 */
@Service
public class FundIntelligentReimburseRecordServiceImpl extends ServiceImpl<FundIntelligentReimburseRecordMapper, FundIntelligentReimburseRecord> implements IFundIntelligentReimburseRecordService {
	org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());
	@Autowired
	 private  FundIntelligentReimburseRecordDetailMapper  fundIntelligentReimburseRecordDetailMapper;
	/**   
	 * <p>Title: findCouponTaxCod</p>   
	 * <p>Description:查询记录  </p>   
	 * @param vo
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.IFundIntelligentReimburseRecordService#findCouponTaxCod(com.taolue.baoxiao.fund.api.vo.FundIntelligentReimburseRecordVo)   
	 */  
	@Override
	public FundIntelligentReimburseRecordVo findCouponTaxCod(FundIntelligentReimburseRecordVo vo) {
		logger.info("查询入参参数》》》》》"+JSON.toJSONString(vo));
		return this.baseMapper.findCouponTaxCode(vo);
	}

	/**   
	 * <p>Title: addCouponTaxCod</p>   
	 * <p>Description: </p>   
	 * @param vo
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.IFundIntelligentReimburseRecordService#addCouponTaxCod(com.taolue.baoxiao.fund.api.vo.FundIntelligentReimburseRecordVo)   
	 */  
	@Override
	@Transactional(rollbackFor = Exception.class)
	public FundIntelligentReimburseRecordVo addCouponTaxCod(FundIntelligentReimburseRecordVo vo) {
		FundIntelligentReimburseRecord  entity=new FundIntelligentReimburseRecord();
		FundIntelligentReimburseRecordDetail entityDetail=new FundIntelligentReimburseRecordDetail();
		vo.setCreator(vo.getMemberId());
		vo.setUpdator(vo.getMemberId());
		vo.setDelFlag("0");
		try {
		logger.info("入参参数》》》》》"+JSON.toJSONString(vo));
		if(StringUtils.isBlank(vo.getIntelligentReimburseCode())) {
			vo.setId(IdWorker.getIdStr());
			vo.setIntelligentReimburseCode(IdWorker.getIdStr());
			BeanUtils.copyProperties(vo,entity);
			logger.info("添加主表入参参数》》》》》"+JSON.toJSONString(entity));
			this.baseMapper.insert(entity);
		}else {
			FundIntelligentReimburseRecord  fundEntity=new FundIntelligentReimburseRecord();
			fundEntity.setStatus(vo.getStatus());
			fundEntity.setId(vo.getId());
			logger.info("修改状态主表入参参数》》》》》"+JSON.toJSONString(fundEntity));			
			this.baseMapper.updateById(fundEntity);
		}
		//判断明细是否存在 如果存在修改 否则新增
		if(StringUtils.isNotBlank(vo.getDetailId())) {
			FundIntelligentReimburseRecordDetail detail = fundIntelligentReimburseRecordDetailMapper.selectById(vo.getDetailId());
			if(detail.getFlowNo().equals(vo.getFlowNo())) {
				FundIntelligentReimburseRecordDetail fundEntityDetail=new FundIntelligentReimburseRecordDetail();
				fundEntityDetail.setStatus(vo.getStatus());
				fundEntityDetail.setId(vo.getDetailId());
				logger.info("修改状态主表入参参数》》》》》"+JSON.toJSONString(fundEntityDetail));
				this.fundIntelligentReimburseRecordDetailMapper.updateById(fundEntityDetail);
			}else {
				BeanUtils.copyProperties(vo,entityDetail);
				entityDetail.setId(IdWorker.getIdStr());
				entityDetail.setStatus(vo.getStatus());
				entityDetail.setBusiParams(vo.getBusiParams());
				logger.info("添加子表入参参数》》》》》"+JSON.toJSONString(entityDetail));
				this.fundIntelligentReimburseRecordDetailMapper.insert(entityDetail);
			}
		}else {
			BeanUtils.copyProperties(vo,entityDetail);
			entityDetail.setId(IdWorker.getIdStr());
			entityDetail.setStatus(vo.getStatus());
			entityDetail.setBusiParams(vo.getBusiParams());
			logger.info("添加子表入参参数》》》》》"+JSON.toJSONString(entityDetail));
			this.fundIntelligentReimburseRecordDetailMapper.insert(entityDetail);
		}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("添加记录表失败");
		}
		return vo;
	}

}
