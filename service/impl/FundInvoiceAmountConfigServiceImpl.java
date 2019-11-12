package com.taolue.baoxiao.fund.service.impl;

import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.dto.InvoiceAmountConfigDto;
import com.taolue.baoxiao.fund.entity.FundCouponTaxCode;
import com.taolue.baoxiao.fund.entity.FundInvoiceAmountConfig;
import com.taolue.baoxiao.fund.entity.FundInvoiceAmountTrade;
import com.taolue.baoxiao.fund.mapper.FundInvoiceAmountConfigMapper;
import com.taolue.baoxiao.fund.mapper.FundInvoiceAmountTradeMapper;
import com.taolue.baoxiao.fund.service.IFundInvoiceAmountConfigService;
import com.taolue.dict.api.dto.CommonDictDto;
import com.taolue.dict.api.service.ICommonDictApi;
import com.taolue.dict.api.vo.CommonDictVo;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 * 开票公司额度配置表 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-07-02
 */
@Service
public class FundInvoiceAmountConfigServiceImpl extends ServiceImpl<FundInvoiceAmountConfigMapper, FundInvoiceAmountConfig> implements IFundInvoiceAmountConfigService {
    private Log logger = LogFactory.getLog(FundInvoiceAmountConfigServiceImpl.class);

    @Autowired
    private FundInvoiceAmountTradeMapper fundInvoiceAmountTradeMapper;
    @Autowired
    private ICommonDictApi commonDictApi;
	@Override
	@Transactional
	public R<Boolean> invoiceAmountCheck(InvoiceAmountConfigDto dto) throws Exception{
		R<Boolean> r=new R<Boolean>();
		String orgTaxNo=dto.getOrgTaxNo();//税号
		String companyId=dto.getCompanyId();//开票公司
		
		FundInvoiceAmountConfig selFIMC=new FundInvoiceAmountConfig();
		selFIMC.setOrgTaxNo(orgTaxNo);
		selFIMC.setCompanyId(companyId);
		selFIMC.setDelFlag("0");
		FundInvoiceAmountConfig invoiceAmountConfig=this.baseMapper.selectOne(selFIMC);
		logger.info("根据税号开票公司查询配置数据返回值:"+JSON.toJSONString(invoiceAmountConfig));
		//当为空的时候，直接新增新的配置
		String invoiceAmountCode=CodeUtils.genneratorShort("KPGS");
		CommonDictDto dictDto=new CommonDictDto();
		dictDto.setType("7");
		R<List<CommonDictVo>> dictR=commonDictApi.getDictList(dictDto);
		logger.info("查询发票抬头额度返回值:"+JSON.toJSONString(dictR));
		if(null==dictR || dictR.getCode()!=R.SUCCESS) {
			throw new BaoxiaoException("查询发票抬头额度失败");
		}
		BigDecimal ttAmount=new BigDecimal(dictR.getData().get(0).getValue());
		//开票信息不存在
		if(ObjectUtils.isEmpty(invoiceAmountConfig)) {
			//添加开票公司额度交易记录表
			editInvoiceAmountTrade(dto, invoiceAmountCode,ttAmount);
			//开票公司额度配置表
			addInvoiceAmountConfig(dto, invoiceAmountCode,ttAmount);
		}else {
			//当配置存在的时候,修改配置的金额，新增记录
			if(invoiceAmountConfig.getBalanceAmount().compareTo(dto.getTradeAmount())==-1) {
				logger.info("该发票抬头本月已超限额");
				r.setCode(R.FAIL);
				r.setData(false);
				r.setMsg("该发票抬头本月已超限额,如要继续开票，请更换抬头");
				return r;
			}
			//当开票单号为空，也就是开票之前准备额度校验，不需要添加数据
			if(StringUtils.isNotEmpty(dto.getInvoiceApplyCode())) {
				FundInvoiceAmountTrade tradeEntity=new FundInvoiceAmountTrade();
				tradeEntity.setInvoiceApplyCode(dto.getInvoiceApplyCode());
				tradeEntity.setStatus(CommonConstant.STATUS_NORMAL);//正常
				FundInvoiceAmountTrade tradeRtn=fundInvoiceAmountTradeMapper.selectOne(tradeEntity);
				if(!ObjectUtils.isEmpty(tradeRtn)) {//如果存在直接返回
					logger.info("改开票单号重复调用额度");
					return r;
				}
				
				
				boolean bTrade=editInvoiceAmountTrade(dto, invoiceAmountConfig.getInvoiceAmountCode(),invoiceAmountConfig.getBalanceAmount());
				if(bTrade) {//代表这个开票单已经冻结了
					if(invoiceAmountConfig.getFreezingBalance().compareTo(dto.getTradeAmount())==-1) {
						logger.info("冻结的额度不足applyCode:"+dto.getInvoiceApplyCode());
						r.setCode(R.FAIL);
						r.setData(false);
						r.setMsg("冻结的额度不足，数据有误！");
						return r;
					}
					invoiceAmountConfig.setAuthBalance(invoiceAmountConfig.getAuthBalance().subtract(dto.getTradeAmount()));
					invoiceAmountConfig.setFreezingBalance(invoiceAmountConfig.getFreezingBalance().subtract(dto.getTradeAmount()));//冻结
				}else {
					invoiceAmountConfig.setBalanceAmount(invoiceAmountConfig.getBalanceAmount().subtract(dto.getTradeAmount()));
					if(!"1".equals(dto.getBuyType())) {
						invoiceAmountConfig.setFreezingBalance(invoiceAmountConfig.getFreezingBalance().add(dto.getTradeAmount()));//冻结
					}else {//不是冻结的时候直接扣减
						invoiceAmountConfig.setAuthBalance(invoiceAmountConfig.getAuthBalance().subtract(dto.getTradeAmount()));
					}
				}
				this.baseMapper.updateById(invoiceAmountConfig);
			}
			
		}
		return r;
	}
	
	public void addInvoiceAmountConfig(InvoiceAmountConfigDto dto,String invoiceAmountCode,BigDecimal ttAmount) {
		FundInvoiceAmountConfig configEntity=new FundInvoiceAmountConfig();
		configEntity.setInvoiceAmountCode(invoiceAmountCode);
		configEntity.setCompanyId(dto.getCompanyId());
		configEntity.setCompanyName(dto.getCompanyName());
		configEntity.setOrgTaxNo(dto.getOrgTaxNo());
		
		//当开票单号为空，也就是开票之前准备额度校验，不需要添加数据
		if(StringUtils.isEmpty(dto.getInvoiceApplyCode())) {
			configEntity.setBalanceAmount(ttAmount);
			configEntity.setAuthBalance(ttAmount);
		}else {
			if(!"1".equals(dto.getBuyType())) {
				configEntity.setFreezingBalance(dto.getTradeAmount());//冻结
				configEntity.setAuthBalance(ttAmount);
			}else {//不是冻结的时候直接扣减
				configEntity.setAuthBalance(ttAmount.subtract(dto.getTradeAmount()));
			}
			configEntity.setBalanceAmount(ttAmount.subtract(dto.getTradeAmount()));
		}
		
		logger.info("添加开票公司额度配置的数据为:"+JSON.toJSONString(configEntity));
		this.baseMapper.insert(configEntity);
	}
	public boolean editInvoiceAmountTrade(InvoiceAmountConfigDto dto,String invoiceAmountCode,BigDecimal preAmount) {
		FundInvoiceAmountTrade tradeEntity=new FundInvoiceAmountTrade();
		tradeEntity.setInvoiceApplyCode(dto.getInvoiceApplyCode());
		tradeEntity.setStatus(CommonConstant.STATUS_DEL);//冻结
		FundInvoiceAmountTrade tradeRtn=fundInvoiceAmountTradeMapper.selectOne(tradeEntity);
		logger.info("根据开票code查询的记录返回为:"+JSON.toJSONString(tradeRtn));
		if(ObjectUtils.isEmpty(tradeRtn)) {
			tradeEntity.setCouponId(dto.getCouponId());
			tradeEntity.setCouponName(dto.getCouponName());
			tradeEntity.setMemberId(dto.getMemberId());

			tradeEntity.setInvoiceAmountCode(invoiceAmountCode);
			//当开票单号为空，也就是开票之前准备额度校验，不需要添加数据
			if(StringUtils.isEmpty(dto.getInvoiceApplyCode())) {
				tradeEntity.setStatus(CommonConstant.STATUS_NORMAL);//正常
				//tradeEntity.setTradeAmount(dto.getTradeAmount());
				//tradeEntity.setTradeLastAmount(preAmount.subtract(dto.getTradeAmount()));
			}else {
				if("1".equals(dto.getBuyType())) {//立即开票
					tradeEntity.setStatus(CommonConstant.STATUS_NORMAL);//正常
				}else {
					tradeEntity.setStatus(CommonConstant.STATUS_DEL);//冻结
				}
				tradeEntity.setTradeAmount(dto.getTradeAmount());
				tradeEntity.setTradeLastAmount(preAmount.subtract(dto.getTradeAmount()));
			}

			tradeEntity.setTradePreAmount(preAmount);
			logger.info("添加开票公司额度记录的数据为:"+JSON.toJSONString(tradeEntity));
			fundInvoiceAmountTradeMapper.insert(tradeEntity);
			return false;
		}else {
			tradeRtn.setStatus(CommonConstant.STATUS_NORMAL);
			fundInvoiceAmountTradeMapper.updateById(tradeRtn);
			return true;
		}
		
	}

	@Override
	@Transactional
	public R<Boolean> loadInvoiceAmount() throws Exception{
		CommonDictDto dictDto=new CommonDictDto();
		dictDto.setType("7");
		R<List<CommonDictVo>> dictR=commonDictApi.getDictList(dictDto);
		logger.info("查询发票抬头额度返回值:"+JSON.toJSONString(dictR));
		if(null==dictR || dictR.getCode()!=R.SUCCESS) {
			throw new BaoxiaoException("查询发票抬头额度失败");
		}
		BigDecimal ttAmount=new BigDecimal(dictR.getData().get(0).getValue());
		EntityWrapper<FundInvoiceAmountConfig> wrapper =  new EntityWrapper<FundInvoiceAmountConfig>();
		wrapper.eq("del_flag", "0");
		List<FundInvoiceAmountConfig> amountConfigList=this.baseMapper.selectList(wrapper);

		logger.info("查询需要初始化的抬头额度数据为:"+JSON.toJSONString(amountConfigList));
		
		for (FundInvoiceAmountConfig fundInvoiceAmountConfig : amountConfigList) {
			fundInvoiceAmountConfig.setAuthBalance(ttAmount);
			fundInvoiceAmountConfig.setBalanceAmount(ttAmount);
			this.baseMapper.updateById(fundInvoiceAmountConfig);
			
			FundInvoiceAmountTrade tradeEntity=new FundInvoiceAmountTrade();
			tradeEntity.setStatus(CommonConstant.STATUS_NORMAL);//冻结
			tradeEntity.setInvoiceAmountCode(fundInvoiceAmountConfig.getInvoiceAmountCode());
			tradeEntity.setTradePreAmount(ttAmount);
			logger.info("添加开票公司额度记录的数据为:"+JSON.toJSONString(tradeEntity));
			fundInvoiceAmountTradeMapper.insert(tradeEntity);
		}
		return new R<>(true);
	}

}
