package com.taolue.baoxiao.fund.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.BillBaseDto;
import com.taolue.baoxiao.fund.api.dto.BillItemDto;
import com.taolue.baoxiao.fund.api.vo.BillItemVo;
import com.taolue.baoxiao.fund.entity.BillItem;

/**
 * <p>
 * 账单明细表，每一条为账单中的没一个账单项 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2018-08-24
 */
public interface IBillItemService extends IService<BillItem> {
	public boolean addItem(String billno);
	Page<BillItemVo> selectBillItemByVendor(Query query, BillBaseDto dto);
	Page<BillItemVo> selectBillItemByExpenseon(Query query, BillBaseDto dto);
	
	public List<BillItem> selectBillItemByBillNo(Map<String,Object> params);
}
