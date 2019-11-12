package com.taolue.baoxiao.fund.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctRelateCateEnums;
import com.taolue.baoxiao.fund.entity.TbFundAcctRelate;
import com.taolue.baoxiao.fund.mapper.TbFundAcctRelateMapper;
import com.taolue.baoxiao.fund.service.ITbFundAcctRelateService;

/**
 * 
 * @ClassName:  TbFundAccttRelateServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:52:10   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
public class TbFundAccttRelateServiceImpl extends ServiceImpl<TbFundAcctRelateMapper, TbFundAcctRelate> implements ITbFundAcctRelateService {
	
	@Override
	public TbFundAcctRelate createRelate(String mainAcctNo, String relateAcctNo, AcctRelateCateEnums relateCate) {
		TbFundAcctRelate relate = new TbFundAcctRelate();
		relate.setAcctInstNo(mainAcctNo);
		relate.setRelateCate(relateCate.getCateCode());
		relate.setRelateInstNo(relateAcctNo);
		relate.setRemark(relateCate.getCateName());
		this.insert(relate);
		return relate;
	}
}
