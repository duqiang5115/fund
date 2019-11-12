package com.taolue.baoxiao.fund.service;

import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.api.vo.FundTradeFlowVo;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;

/**
 * 
 * @ClassName:  ITbFundTradeFlowService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:50:07   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface ITbFundTradeFlowService extends IService<TbFundTradeFlow> {
	
	/**
	 * 
	 * @Title: createTrandeFlow   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   2018年8月20日 下午2:50:12  
	 * @param: @param tradeFlowCode
	 * @param: @param tradeBusiCode
	 * @param: @param tradeFlowOrder
	 * @param: @param balanceCode
	 * @param: @param tradeCate
	 * @param: @param transActCate
	 * @param: @param transCateName
	 * @param: @param transActName
	 * @param: @param tradeAmount
	 * @param: @param tradePreAmount
	 * @param: @param tradeLastAmount
	 * @param: @return      
	 * @return: TbFundTradeFlow      
	 * @throws
	 */
	public TbFundTradeFlow createTrandeFlow(String tradeFlowCode, String tradeBusiCode, BigDecimal tradeFlowOrder,
    		String balanceCode, String tradeCate, String transActCate, String transCateName, String transActName, 
    		BigDecimal tradeAmount, BigDecimal tradePreAmount, BigDecimal tradeLastAmount );
	
	public  Page<TbFundTradeFlow> findFundTradeFlow(Query query, FundTradeFlowDto fundTradeFlowDto);

	/**
	* @Title: operationIntegral  
	* @Description: 企业积分操作接口  
	* @param @param flowVo
	* @param @return
	* @return R<Boolean>
	* @author fbh
	* @date 2019年10月29日  下午5:24:02
	* @throws
	 */
	public R<Boolean> operationIntegral(FundTradeFlowVo flowVo);
}
