package com.taolue.baoxiao.fund.service.composite;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.vo.BillComposeVo;

/**
 * 
 * @ClassName:  IBillComposeService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月24日 下午4:53:40   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IBillComposeService extends IService<BillComposeVo> {
	
	public Page<BillComposeVo> selectBillComposeVoPage(Query<BillComposeVo> page);
}
