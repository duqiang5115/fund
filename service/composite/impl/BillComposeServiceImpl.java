package com.taolue.baoxiao.fund.service.composite.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.vo.BillComposeVo;
import com.taolue.baoxiao.fund.api.vo.BillItemComposeVo;
import com.taolue.baoxiao.fund.mapper.BillComposeMapper;
import com.taolue.baoxiao.fund.service.composite.IBillComposeService;

/**
 * 
 * @ClassName:  BillComposeServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月24日 下午4:51:34   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
public class BillComposeServiceImpl extends ServiceImpl<BillComposeMapper, BillComposeVo> implements IBillComposeService {

	@Override
	public Page<BillComposeVo> selectBillComposeVoPage(Query<BillComposeVo> page) { 
		List<BillComposeVo> billComposeVoList=baseMapper.selectBillComposes(page,page.getCondition());
		List<BillComposeVo> billComposeList =new ArrayList<BillComposeVo>();
		
		for (BillComposeVo billComposeVo : billComposeVoList) {
			List<BillItemComposeVo>  billItemComposeList=new ArrayList<BillItemComposeVo>(); 
			
			for (BillItemComposeVo billItemComposeVo : billComposeVo.getBillItems()) {
				if(billItemComposeVo.getBillItemCate().equals(DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_YQSV.getCateCode()) 
						|| billItemComposeVo.getBillItemCate().equals(DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_DZSV.getCateCode())) {
					billItemComposeVo.setItemType("2");//服务费
					
				}else if(billItemComposeVo.getBillItemCate().equals(DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode())
						|| billItemComposeVo.getBillItemCate().equals(DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_XFBX.getCateCode())) {
					billItemComposeVo.setItemType("1");//商品
				}
				billItemComposeList.add(billItemComposeVo);
			} 
			billComposeVo.setBillItems(billItemComposeList);
			billComposeList.add(billComposeVo);
		}
		page.setRecords(billComposeList);
        return page;
    }
}
