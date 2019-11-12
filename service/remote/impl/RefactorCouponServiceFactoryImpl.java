/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  RefactorCouponServiceFactoryImpl.java   
 * @Package com.taolue.baoxiao.fund.service.remote.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 26, 2018 7:34:02 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.remote.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.coupon.IRefactorCouponAllotService;
import com.taolue.baoxiao.fund.api.coupon.IRefactorCouponRelationService;
import com.taolue.baoxiao.fund.api.coupon.RefactorCouponService;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.service.remote.IRefactorCouponServiceFactory;
import com.taolue.coupon.api.utils.CouponConstant;
import com.taolue.coupon.api.vo.CouponRelationVo;
import com.xiaoleilu.hutool.collection.CollUtil;

import lombok.extern.slf4j.Slf4j;

/**   
 * <p>ClassName:  RefactorCouponServiceFactoryImpl </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: shilei</br>
 * <p>date:   Dec 26, 2018 7:34:02 PM </br>  
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class RefactorCouponServiceFactoryImpl implements IRefactorCouponServiceFactory {

	@Autowired
	private RefactorCouponService refactorCouponService;
     
	@Autowired
	private IRefactorCouponRelationService refactorCouponRelationService;
	
	@Autowired
	private IRefactorCouponAllotService refactorCouponAllotService;
	
	/**  
	 * <p>Title:getRefactorCouponService</p><BR>  
	 * <p>Description:获取属性refactorCouponService的值<BR>  
	 * @return RefactorCouponService <BR>  
	 */
	public RefactorCouponService getRefactorCouponService() {
		return refactorCouponService;
	}

	/**  
	 * <p>Title:getRefactorCouponRelationService</p><BR>  
	 * <p>Description:获取属性refactorCouponRelationService的值<BR>  
	 * @return IRefactorCouponRelationService <BR>  
	 */
	public IRefactorCouponRelationService getRefactorCouponRelationService() {
		return refactorCouponRelationService;
	}

	/**  
	 * <p>Title:getRefactorCouponAllotService</p><BR>  
	 * <p>Description:获取属性refactorCouponAllotService的值<BR>  
	 * @return IRefactorCouponAllotService <BR>  
	 */
	public IRefactorCouponAllotService getRefactorCouponAllotService() {
		return refactorCouponAllotService;
	}

	/**   
	 * <p>Title: findCouponsInfoByIndustryIdsAndCouponIds</p>   
	 * <p>Description: </p>   
	 * @param industryIds
	 * @param couponIds
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.remote.IRefactorCouponServiceFactory#findCouponsInfoByIndustryIdsAndCouponIds(java.util.List, java.util.List)   
	 */  
	@SuppressWarnings("unchecked")
	@Override
	public List<CouponRelationVo> findCouponsByIndustryAndCouponIds(List<String> industryIds,
			List<String> couponIds) {
		List<CouponRelationVo> records = Lists.newArrayList();
		Map<String, Object> params = Maps.newHashMap();
		try {
			params.put(CouponConstant.RELATION_COUPONIDLIST,couponIds);
			params.put(CouponConstant.RELATION_BUSINESSIDLIST,industryIds);
			R<List<CouponRelationVo>> results = refactorCouponRelationService.findCouponListByidList(params);
			log.info("results = "+JSON.toJSONString(results));
			if (CollUtil.isNotEmpty(results.getData())) {
				log.info("results.getData() = "+JSON.toJSONString(results.getData()));
				records = results.getData();
			}
		} catch (Exception e) {
			FundServiceExceptionGenerator.FundServiceException("9999",e);
		}
		return records;
	}
	
}
