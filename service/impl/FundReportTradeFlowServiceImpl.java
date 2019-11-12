package com.taolue.baoxiao.fund.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.coupon.RefactorCouponService;
import com.taolue.baoxiao.fund.api.dto.CouponCostDetailDto;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberCompanyService;
import com.taolue.baoxiao.fund.api.member.RefactorMemberPlatformService;
import com.taolue.baoxiao.fund.entity.FundReportTradeFlow;
import com.taolue.baoxiao.fund.mapper.FundReportTradeFlowMapper;
import com.taolue.baoxiao.fund.service.IFundReportTradeFlowService;
import com.taolue.coupon.api.vo.CouponVo;
import com.taolue.member.api.vo.MemberCerVo;
import com.xiaoleilu.hutool.collection.CollUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 会员交易记录宽表 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-09-24
 */
@Service
@Slf4j
@Transactional(rollbackFor=Exception.class)
public class FundReportTradeFlowServiceImpl extends ServiceImpl<FundReportTradeFlowMapper, FundReportTradeFlow> implements IFundReportTradeFlowService {

	@Autowired
	private RefactorMemberPlatformService refactorMemberPlatformService;
	@Autowired
	private  RefactorCouponService refactorCouponService;
	@Autowired
	private FundReportTradeFlowMapper fundReportTradeFlowMapper;
	@Autowired
	private IRefactorMemberCompanyService refactorMemberCompanyService;
	@Override
	public boolean creatingFundReportTradeFlow(Date startTime, Date endTime, String adminLoginName) {
		List<String> companyIds = this.refactorMemberCompanyService.queryCompanyIdsByAdminLoginName(adminLoginName);
		if (CollUtil.isEmpty(companyIds)) {
			log.error("会员交易记录清洗》没有查询到公司数据信息");
			return false;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		map.put("companyIds", companyIds);
		List<FundReportTradeFlow> resultList = this.fundReportTradeFlowMapper.findCreatingFundReportTradeFlow(map);
		if (CollUtil.isEmpty(resultList)) {
			log.info("会员交易记录清洗》没有查询到数据");
			return false;
		}

		R<List<MemberCerVo>> r = this.refactorMemberPlatformService.queryMemberCerByAdminLoginName(adminLoginName);
		if (null == r || r.getCode() != R.SUCCESS) {
			log.info("会员交易记录清洗》没有查询到公司员工信息");
			return false;
		}

		List<FundReportTradeFlow> list = removeNotEmployeeList(resultList, r.getData());
		for (FundReportTradeFlow flow : list) {
			r.getData().forEach(obj -> {
				if (StringUtils.isBlank(flow.getMemberName())
						&& flow.getMemberId().trim().equals(obj.getMemberId().trim())) {
					flow.setMemberName(obj.getRealName());
				}
				if (StringUtils.isBlank(flow.getCompanyName())
						&& flow.getCompanyId().trim().equals(obj.getCompanyId().trim())) {
					flow.setCompanyName(obj.getCompanyName());
				}
			});
		}

		List<String> idList = new ArrayList<String>();
		List<String> ids = new ArrayList<String>();
		list.forEach(flow -> {
			if (StringUtils.isNotBlank(flow.getCouponId())) {
				idList.add(flow.getCouponId().trim());
			}
			ids.add(flow.getId());
		});
		List<FundReportTradeFlow> addList = removeList(ids, list);
		if (CollUtil.isEmpty(addList)) {
			log.info("会员交易记录清洗》没有查询到合法数据数据");
			return false;
		}

		List<CouponVo> couponList = this.refactorCouponService.findCouponVoByIdList(idList);
		if (CollUtil.isNotEmpty(couponList)) {
			for (FundReportTradeFlow flow : addList) {
				couponList.forEach(obj -> {
					if (flow.getCouponId().trim().equals(obj.getId().trim())) {
						flow.setCouponName(obj.getName());
					}
				});
			}
		}

		return this.insertBatch(addList);
	}
	/****
	 * @Title FundReportTradeFlowServiceImpl.removeNotEmployeeList
	 * @Description: 删除非员工数据
	 *
	 * @param resultList
	 * @param memberList
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-10-12 17:54:54  修改内容 :
	 */
	private List<FundReportTradeFlow> removeNotEmployeeList(List<FundReportTradeFlow>  resultList,List<MemberCerVo> memberList){
	   if(CollUtil.isEmpty(memberList)) {
		   return resultList;
	   }	
	   List<FundReportTradeFlow>  deleteList = new ArrayList<FundReportTradeFlow>();
	   resultList.forEach(obj -> {
		   boolean istrue = false;
		   for (MemberCerVo member : memberList) {
			   if(StringUtils.isNotBlank(obj.getMemberId()) && StringUtils.isNotBlank(member.getMemberId()) && member.getMemberId().trim().equals(obj.getMemberId().trim())) {
				   istrue = true;
			   }
		   }
		   if(!istrue) {
			   deleteList.add(obj);
		   }
	   });
	   if(CollUtil.isNotEmpty(memberList)) {
		   resultList.removeAll(deleteList);
	   }
	  return resultList;
	}
	
	/***
	 * @Title FundReportTradeFlowServiceImpl.removeList
	 * @Description: 清除已存在数据
	 *
	 * @param ids
	 * @param pList
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-09-27 14:00:23  修改内容 :
	 */
	private List<FundReportTradeFlow> removeList(List<String> ids,List<FundReportTradeFlow>  pList) {
		    Wrapper<FundReportTradeFlow> wrapper = new EntityWrapper<>();
		    wrapper.in("id", ids);
		    wrapper.eq("del_flag", CommonConstant.STATUS_NORMAL);
		    List<FundReportTradeFlow> rList = this.selectList(wrapper);
		    if(CollUtil.isNotEmpty(rList)) {
		    	Iterator<FundReportTradeFlow> it = pList.iterator();
		    	while(it.hasNext()) {
		    		FundReportTradeFlow flow = it.next();
		    		for (FundReportTradeFlow obj : rList) {
		    			if(obj.getId().trim().equals(flow.getId().trim())) {
		    				it.remove();
		    				break;
		    			}
					}
		    	}
		    }
		    return pList;
	}
	/****
	 * @Title FundReportTradeFlowServiceImpl.addCouponCostDetail
	 * @Description: 更新消费订单详情信息
	 *
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-10-16 18:27:54  修改内容 :
	 */
	@Override
	public boolean addCouponCostDetail() {
		Wrapper<FundReportTradeFlow> wrapper  = new EntityWrapper<FundReportTradeFlow>();
		wrapper.isNull("payment_industry_id");
		Date now = new Date(System.currentTimeMillis());
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -45);
		wrapper.between("trade_time", calendar.getTime(), now);
		List<FundReportTradeFlow> resultList = this.selectList(wrapper);
		if(CollUtil.isEmpty(resultList)) {
			log.info("更新消费订单详情信息");
			return false;
		}
		log.info("更新消费订单详情信息》addCouponCostDetail》start----");
		addCouponCostDetailListPage(resultList);
		log.info("更新消费订单详情信息》addCouponCostDetail》end ----");
		return true;
	}
	/****
	 * @Title FundReportTradeFlowServiceImpl.addCouponCostDetailListPage
	 * @Description: 分页处理处理数据订单详情数据
	 *
	 * @param resultList
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2019-10-21 15:37:07  修改内容 :
	 */
	private void addCouponCostDetailListPage(List<FundReportTradeFlow> resultList) {
		Integer totalSize = resultList.size();
	    /**每页N条***/ 
	    int pageSize = 500;
	    /****共N页***/ 
	    int totalPage = totalSize / pageSize;
	    if (totalSize % pageSize != 0) {
	        totalPage += 1;
	        if (totalSize < pageSize) {
	            pageSize = totalSize;
	        }
	    }
	    for (int i = 1; i < totalPage+1 ; i++) {
	    	int startIndex = (i - 1) * pageSize;
	        int endIndex = i * pageSize > totalSize ? (totalSize) : i * pageSize;
	        log.info("分页处理处理数据订单详情数据》totalSize="+totalSize+"，totalPage="+totalPage+" ，startIndex="+startIndex+" ， endIndex="+endIndex);
			List<FundReportTradeFlow> sonList = resultList.subList(startIndex, endIndex);
			if(CollUtil.isNotEmpty(sonList)) {
				addCouponCostDetailList(sonList);
			}
		}
	}
	@Transactional(rollbackFor=Exception.class)
	private void addCouponCostDetailList(List<FundReportTradeFlow> resultList) {
		List<String> itemIds = new ArrayList<String>();
		resultList.forEach(obj ->{
			itemIds.add(obj.getId());
		});
		List<CouponCostDetailDto>  detailDtoList = this.fundReportTradeFlowMapper.findCouponCostDetail(itemIds);
		if(CollUtil.isEmpty(detailDtoList)) {
			return;
		}
		List<FundReportTradeFlow> entityList = new ArrayList<FundReportTradeFlow>();
		detailDtoList.forEach(obj ->{
			FundReportTradeFlow flow = new FundReportTradeFlow();
			flow.setId(obj.getId());
			flow.setPaymentIndustryId(obj.getPaymentIndustryId());
			flow.setPaymentIndustryName(obj.getPaymentIndustryName());
			flow.setOperationParams(obj.getOperationParams());
			flow.setOrderCode(obj.getOrderCode());
			entityList.add(flow);
		});
		this.updateBatchById(entityList);
	}
}
