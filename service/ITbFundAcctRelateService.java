package com.taolue.baoxiao.fund.service;

import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctRelateCateEnums;
import com.taolue.baoxiao.fund.entity.TbFundAcctRelate;

/**
 * 
 * @ClassName:  ITbFundAccttRelateService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:48:29   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface ITbFundAcctRelateService extends IService<TbFundAcctRelate> {
	/**
	 * 
	 * @Title: createRelate   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   2018年8月20日 下午2:48:36  
	 * @param: @param mainAcctNo
	 * @param: @param relateAcctNo
	 * @param: @param relateCate
	 * @param: @return      
	 * @return: TbFundAccttRelate      
	 * @throws
	 */
	public TbFundAcctRelate createRelate(String mainAcctNo, String relateAcctNo, AcctRelateCateEnums relateCate);
}
