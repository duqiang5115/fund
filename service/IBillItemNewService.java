package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.fund.api.dto.BillItemNewDto;
import com.taolue.baoxiao.fund.api.vo.BillItemNewVo;
import com.taolue.baoxiao.fund.entity.BillItemNew;

import java.util.List;

import com.baomidou.mybatisplus.service.IService;

/**
 * <p>
 * 账单明细表，每一条为账单中的没一个账单项 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-06-13
 */
public interface IBillItemNewService extends IService<BillItemNew> {
	/**
	 * 
	 * @Title: queryBillItem   
	 * @Description: 账单明细查询   
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: List<BillItemNewVo>      
	 * @throws
	 */
	List<BillItemNewVo> queryBillItem(BillItemNewDto dto);
	/**
	 * 
	 * @Title: updateStatusByBillNo   
	 * @Description: 通过账单号修改状态  
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: boolean      
	 * @throws
	 */
	boolean updateStatusByBillNo(BillItemNewDto dto);
}
