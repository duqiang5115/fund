/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  IAccountService.java   
 * @Package com.taolue.baoxiao.fund.service   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   2018年8月28日 上午10:39:33   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite;

import java.util.Date;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.vo.QueryFundBalanceVo;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;

/**   
 * @ClassName:  IAccountService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月28日 上午10:39:33   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IQueryFundBalanceService extends IService<QueryFundBalanceVo> {
	
	/**
	 * 
	 * @Title: queryCompanyAccountInfo   
	 * @Description: 查询企业保证金或者报销金账户流水 
	 * @Author: shilei
	 * @date:   Dec 6, 2018 1:54:13 AM  
	 * @param query Query<TbFundTradeFlow> 分页参数
	 * @param memberId String 必传，公司会员id
	 * @param beginTime Date 选填 数据开始时间 
	 * @param endTime Date 选填 数据结束时间
	 * @param balanceCate String 必传 常量
	 * 			CommonConstant.BALANCE_ITEM_NO_SUFFX_BZJ 
	 * 		或者
	 * 			CommonConstant.BALANCE_ITEM_NO_SUFFX_GBX
	 * 		之一
	 * @param balanceItemCate 选填 常量
	 * 			CommonConstant.BALANCE_ITEM_NO_FULL_YCBZJ 
	 * 		或者
	 * 			CommonConstant.BALANCE_ITEM_NO_FULL_YCGBX 
	 * 		或者
	 * 			CommonConstant.BALANCE_ITEM_NO_FULL_HFBZJ 
	 * 		或者
	 * 			CommonConstant.BALANCE_ITEM_NO_FULL_HFGBX
	 *      之一
	 * @param tradeCate 选填 枚举值
	 * 		   TradeCateEnums.TRADE_CATE_ADDUCT
	 *     或者
	 *         TradeCateEnums.TRADE_CATE_DEDUCT
	 *     之一 的 code 值
	 * @return Page<TbFundTradeFlow> 带分页信息的 TbFundTradeFlow 对象列表      
	 * @throws
	 *
	 */
	public Page<TbFundTradeFlow> queryCompanyAccountInfo(Query<TbFundTradeFlow> query, String memberId, 
			Date beginTime, Date endTime,String balanceCate, String balanceItemCate, String tradeCate);
}
