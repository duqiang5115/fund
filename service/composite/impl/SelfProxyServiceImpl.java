/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  SelfProxyServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Mar 24, 2019 12:03:38 AM   
 * @version V1.0 
 * @Copyright: 2019 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import org.springframework.aop.framework.AopContext;
import org.springframework.aop.support.AopUtils;

/**   
 * <p>ClassName:  SelfProxyServiceImpl </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: shilei</br>
 * <p>date:   Mar 24, 2019 12:03:38 AM </br>  
 *     
 * @Copyright: 2019 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public class SelfProxyServiceImpl<T> {
	
	/**  
	 * <p>Title:getSelfProxy</p><BR>  
	 * <p>Description:获取属性selfProxy的值<BR>  
	 * @return T <BR>  
	 */
	@SuppressWarnings("unchecked")
	protected T getSelfProxy() {
		if (AopUtils.isAopProxy(AopContext.currentProxy())) {
			return (T)(AopContext.currentProxy());
		} else {
			return (T)this;
		}
	}
	
	
}
