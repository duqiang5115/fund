/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  IRefactorCouponServiceFactory.java   
 * @Package com.taolue.baoxiao.fund.service.remote   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 26, 2018 7:33:00 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.remote;

import java.util.List;

import com.taolue.baoxiao.fund.api.coupon.IRefactorCouponAllotService;
import com.taolue.baoxiao.fund.api.coupon.IRefactorCouponRelationService;
import com.taolue.baoxiao.fund.api.coupon.RefactorCouponService;
import com.taolue.coupon.api.vo.CouponRelationVo;

/**   
 * <p>ClassName:  IRefactorCouponServiceFactory </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: shilei</br>
 * <p>date:   Dec 26, 2018 7:33:00 PM </br>  
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IRefactorCouponServiceFactory {
	
	RefactorCouponService getRefactorCouponService();
	
	IRefactorCouponRelationService getRefactorCouponRelationService();
	
	IRefactorCouponAllotService getRefactorCouponAllotService();
	
	/**
	 * 
	 * <p>名称:类IRefactorCouponServiceFactory中的findCouponsByIndustryAndCouponIds方法</br>    
	 * <p>描述:获取在指定行业和指定券范围内的券的信息</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Dec 26, 2018 8:54:24 PM</br>
	 * @throws Exception 异常
	 * @param industryIds {@link List}{@literal <}{@link String}{@literal >}{@literal 行业编号集合，用来指定行业范围}
	 * {@code 必传}
	 * @param couponIds {@link List}{@literal <}{@link String}{@literal >}{@literal 券编号集合，用来指定券范围}
	 * {@code 必传}
	 * @return {@link List}{@literal <}{@link CouponRelationVo}{@literal >}{@literal 在couponIds中且归属在industryIds行业中的券的集合}
	 */
	List<CouponRelationVo> findCouponsByIndustryAndCouponIds(List<String> industryIds, 
			List<String> couponIds);
	
}
