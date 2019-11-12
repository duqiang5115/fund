/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  QueryFundBalanceServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   2018年10月7日 下午5:24:35   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.api.vo.QueryFundBalanceVo;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.mapper.QueryFundBalanceMapper;
import com.taolue.baoxiao.fund.service.composite.IQueryFundBalanceService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

/**   
 * @ClassName:  QueryFundBalanceServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年10月7日 下午5:24:35   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
public class QueryFundBalanceServiceImpl extends ServiceImpl<QueryFundBalanceMapper, QueryFundBalanceVo> 
								implements IQueryFundBalanceService {
	
	public static Map<String, List<FundTradeFlowDto>> QUERY_CMBZJ_LISTPARAMS = Maps.newHashMap();
	public static Map<String, List<FundTradeFlowDto>> QUERY_CMBXJ_LISTPARAMS = Maps.newHashMap();
	
	static {
		//-------------------------------------------企业报销账户----------------------------------------
		FundTradeFlowDto bzj_perAd = new FundTradeFlowDto("CM10006BZJ", "BIM00001", "AD", "AT00002");
		FundTradeFlowDto bzj_perDc = new FundTradeFlowDto("CM10006BZJ", "BIM00001", "DC", "AT00001");
		FundTradeFlowDto bzj_aftAd = new FundTradeFlowDto("CM10007BZJ", "BIM00003", "AD", "AT00002");
		FundTradeFlowDto bzj_aftDc = new FundTradeFlowDto("CM10007BZJ", "BIM00003", "DC", "AT00001");
		
		List<FundTradeFlowDto> perAdd = Lists.newArrayList();
		perAdd.add(bzj_perAd);
		
		List<FundTradeFlowDto> perDc = Lists.newArrayList();
		perDc.add(bzj_perDc);
		
		List<FundTradeFlowDto> aftAdd = Lists.newArrayList();
		aftAdd.add(bzj_aftAd);
		
		List<FundTradeFlowDto> aftDc = Lists.newArrayList();
		aftDc.add(bzj_aftDc);
		
		
		List<FundTradeFlowDto> perAll = Lists.newArrayList();
		perAll.add(bzj_perAd);
		perAll.add(bzj_perDc);
		
		List<FundTradeFlowDto> aftAll = Lists.newArrayList();
		aftAll.add(bzj_aftAd);
		aftAll.add(bzj_aftDc);
		
		List<FundTradeFlowDto> addAll = Lists.newArrayList();
		addAll.add(bzj_perAd);
		addAll.add(bzj_aftAd);
		
		List<FundTradeFlowDto> decAll = Lists.newArrayList();
		decAll.add(bzj_perDc);
		decAll.add(bzj_aftDc);
		
		List<FundTradeFlowDto> bzjAll = Lists.newArrayList();
		bzjAll.add(bzj_perAd);
		bzjAll.add(bzj_perDc);
		bzjAll.add(bzj_aftAd);
		bzjAll.add(bzj_aftDc);
	
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_PERAD, perAdd);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_PERDC, perDc);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_AFTAD, aftAdd);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_AFTDC, aftDc);
		
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_PERALL, perAll);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_AFTALL, aftAll);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_ADDALL, addAll);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_DECALL, decAll);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJALL, bzjAll);
		
		//----------------------------------------个人报销账户----------------------------------------
		FundTradeFlowDto bxj_perAd = new FundTradeFlowDto("CM10006BXJ", "BIM00002", "AD", "AT00002");
		FundTradeFlowDto bxj_perDc = new FundTradeFlowDto("CM10006BXJ", "BIM00002", "DC", "AT00001");
		FundTradeFlowDto bxj_aftAd = new FundTradeFlowDto("CM10007BXJ", "BIM00004", "AD", "AT00002");
		FundTradeFlowDto bxj_aftDc = new FundTradeFlowDto("CM10007BXJ", "BIM00004", "DC", "AT00001");
		
		
		List<FundTradeFlowDto> bxj_perAdd = Lists.newArrayList();
		bxj_perAdd.add(bxj_perAd);
		
		List<FundTradeFlowDto> bxj_perDec = Lists.newArrayList();
		bxj_perDec.add(bxj_perDc);
		
		List<FundTradeFlowDto> bxj_aftAdd = Lists.newArrayList();
		bxj_aftAdd.add(bxj_aftAd);
		
		List<FundTradeFlowDto> bxj_aftDec = Lists.newArrayList();
		bxj_aftDec.add(bxj_aftDc);
		
		
		List<FundTradeFlowDto> bxj_perAll = Lists.newArrayList();
		bxj_perAll.add(bxj_perAd);
		bxj_perAll.add(bxj_perDc);
		
		List<FundTradeFlowDto> bxj_aftAll = Lists.newArrayList();
		bxj_aftAll.add(bxj_aftAd);
		bxj_aftAll.add(bxj_aftDc);
		
		List<FundTradeFlowDto> bxj_addAll = Lists.newArrayList();
		bxj_addAll.add(bxj_perAd);
		bxj_addAll.add(bxj_aftAd);
		
		List<FundTradeFlowDto> bxj_decAll = Lists.newArrayList();
		bxj_decAll.add(bxj_perDc);
		bxj_decAll.add(bxj_aftDc);
		
		List<FundTradeFlowDto> bxjAll = Lists.newArrayList();
		bxjAll.add(bxj_perAd);
		bxjAll.add(bxj_perDc);
		bxjAll.add(bxj_aftAd);
		bxjAll.add(bxj_aftDc);
		
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_PERAD, bxj_perAdd);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_PERDC, bxj_perDec);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_AFTAD, bxj_aftAdd);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_AFTDC, bxj_aftDec);
		
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_PERALL, bxj_perAll);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_AFTALL, bxj_aftAll);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_ADDALL, bxj_addAll);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_DECALL, bxj_decAll);
		QUERY_CMBZJ_LISTPARAMS.put(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJALL, bxjAll);
		
	}
	
	@Override
	public Page<TbFundTradeFlow> queryCompanyAccountInfo(Query<TbFundTradeFlow> query, String memberId, 
			Date beginTime, Date endTime,String balanceCate, String balanceItemCate, String tradeCate) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("memberId", memberId);
		if (null != beginTime) {
			params.put("beginTime", beginTime);
		}
		if (null != endTime) {
			params.put("endTime", endTime);
		}
		
		List<FundTradeFlowDto> paramsList = Lists.newArrayList();
		if (StrUtil.isBlank(balanceItemCate) && StrUtil.isBlank(tradeCate)) {
			if (CommonConstant.BALANCE_ITEM_NO_SUFFX_BZJ.equals(balanceCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJALL);
			}
			if (CommonConstant.BALANCE_ITEM_NO_SUFFX_GBX.equals(balanceCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJALL);
			}
		} else if (StrUtil.isBlank(balanceItemCate) && StrUtil.isNotBlank(tradeCate)) {
			if (TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode().equals(tradeCate)
					&& CommonConstant.BALANCE_ITEM_NO_SUFFX_BZJ.equals(balanceCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_ADDALL);
			}
			if (TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode().equals(tradeCate)
					&& CommonConstant.BALANCE_ITEM_NO_SUFFX_GBX.equals(balanceCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_ADDALL);
			}
			if (TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode().equals(tradeCate)
					&& CommonConstant.BALANCE_ITEM_NO_SUFFX_BZJ.equals(balanceCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_DECALL);
			}
			if (TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode().equals(tradeCate)
					&& CommonConstant.BALANCE_ITEM_NO_SUFFX_GBX.equals(balanceCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_DECALL);
			}
		} else if (StrUtil.isNotBlank(balanceItemCate) && StrUtil.isBlank(tradeCate)) {
			if (CommonConstant.BALANCE_ITEM_NO_FULL_YCBZJ.equals(balanceItemCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_PERALL);
			}
			if (CommonConstant.BALANCE_ITEM_NO_FULL_HFBZJ.equals(balanceItemCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_AFTALL);
			}
			if (CommonConstant.BALANCE_ITEM_NO_FULL_YCGBX.equals(balanceItemCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_PERALL);
			}
			if (CommonConstant.BALANCE_ITEM_NO_FULL_HFGBX.equals(balanceItemCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_AFTALL);
			}
		}else if (StrUtil.isNotBlank(balanceItemCate) && StrUtil.isNotBlank(tradeCate)) {
			if (CommonConstant.BALANCE_ITEM_NO_FULL_YCBZJ.equals(balanceItemCate)
					&& TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode().equals(tradeCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_PERAD);
			}
			if (CommonConstant.BALANCE_ITEM_NO_FULL_YCBZJ.equals(balanceItemCate)
					&& TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode().equals(tradeCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_PERDC);
			}
			if (CommonConstant.BALANCE_ITEM_NO_FULL_HFBZJ.equals(balanceItemCate)
					&& TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode().equals(tradeCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_AFTAD);
			}
			if (CommonConstant.BALANCE_ITEM_NO_FULL_HFBZJ.equals(balanceItemCate)
					&& TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode().equals(tradeCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BZJ_AFTDC);
			}
			
			if (CommonConstant.BALANCE_ITEM_NO_FULL_YCGBX.equals(balanceItemCate)
					&& TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode().equals(tradeCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_PERAD);
			}
			if (CommonConstant.BALANCE_ITEM_NO_FULL_YCGBX.equals(balanceItemCate)
					&& TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode().equals(tradeCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_PERDC);
			}
			if (CommonConstant.BALANCE_ITEM_NO_FULL_HFGBX.equals(balanceItemCate)
					&& TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode().equals(tradeCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_AFTAD);
			}
			if (CommonConstant.BALANCE_ITEM_NO_FULL_HFGBX.equals(balanceItemCate)
					&& TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode().equals(tradeCate)) {
				paramsList = QUERY_CMBZJ_LISTPARAMS.get(CommonConstant.SEARCH_PARAM_NAME_KEY_BXJ_AFTDC);
			}
		}
		if (CollUtil.isEmpty(paramsList)) {
			FundServiceExceptionGenerator.FundServiceException("参数有误，无法定位到数据！！");
		}
		params.put("paramsList", paramsList);
		
		List<TbFundTradeFlow> records = this.baseMapper.queryCompanyAccountInfo(query, params);
		query.setRecords(records);
		return query;
	}
	
}
