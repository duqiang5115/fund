package com.taolue.baoxiao.fund.service.composite;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.OrderBusiComposeDto;
import com.taolue.baoxiao.fund.api.dto.OrderBusiQueryDto;

/**
 * 
 * @ClassName:  IOrderBusiComposeService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:16:53   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IOrderBusiComposeService extends IService<OrderBusiComposeDto> {
	
	/**
	 * 
	 * @Title: selectOrderBusiComposeDtoPage   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   2018年8月20日 下午2:16:43  
	 * @param: @param page
	 * @param: @return      
	 * @return: Page<OrderBusiComposeDto>      
	 * @throws
	 */
	public Page<OrderBusiComposeDto> selectOrderBusiComposeDtoPage(Query<OrderBusiComposeDto> page);
	
	public Page<OrderBusiComposeDto> selectOrderBusiComposeDtoByDto(Query query,OrderBusiQueryDto dto);
}
