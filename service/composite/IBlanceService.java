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
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;

import com.baomidou.mybatisplus.plugins.Page;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.vo.CompanyBuyVoucherVo;
import com.taolue.baoxiao.fund.api.vo.FundVoucherBalanceVo;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
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
public interface IBlanceService {
	
	/*****
	 * @Title TbFundBalanceServiceImpl.queryBalaneByMemberId
	 * @Description:查询会员账户信息， 根据会员id,账户类型,资金对应实例  查询过滤
	 *
	 * @param memberId 会员id
	 * @param countType 账户类型
	 * @return
	 * 
	 * @version: 1.0 
	 * @author kwd 修改历史: 修改人: kwd, 修改日期 : 2018年08月27日 下午5:41:11 修改内容 :
	 */
	public List<TbFundBalance> queryBalaneByMIdAndCountType(String memberId, 
			String countType, String... companyIds);

	public Page<TbFundTradeFlow> findPagedTradeFlowByParams(Page<TbFundTradeFlow> page, String memberId, 
			String fundAcctCate, String businessCode, Date tradeBegin, Date tradeEnd);
	
	/**
	 * 
	 * @Title: accountAmount   
	 * @Description: 更新企业后付费账户授权额度
	 * @Author: shilei
	 * @date:   2018年10月10日 下午2:19:56  
	 * @param memberId String 企业编码
	 * @param amount String 最新授权额度
	 * @return boolean 是否更新成功     
	 * @throws
	 *
	 */
//	public boolean accountAmount(String memberId, String amount);
	
//	public List<TbFundBalance> queryAcctBalanceByMemeberId(String memberId, 
//			 String busiModel, List<String> balanceItemIds, String... companyIds);
//	
//	public List<TbFundBalance> queryAcctBalanceByMemeberIdAndCanTransfer(String memberId, String busiModel,
//			List<String> balanceItemIds,String canTransfer, String... companyIds);
//	
//	public BigDecimal queryAcctBalanceSumByMemeberId(String memberId, 
//			 String busiModel, List<String> balanceItemIds, String... companyIds);

//	public Multimap<String, Object> queryUnchargeFundTradeFlow(String tradeBusiCode);
	
	/**
	 * 
	 * 
	 * @Title IBlanceService.addBalanceByMemberId
	 * @Description: 企业抵用券账户额度的增加
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年10月29日 下午5:31:24
	 * 修改内容 :
	 */
	public boolean operateAcctBalance(List<FundVoucherBalanceVo> list);
	
	/**
	 * 
	 * 
	 * @Title IBlanceService.operateMemberVoucherBalance
	 * @Description: 员工抵用券账户额度增加
	 *
	 * @param list
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年10月30日 下午9:10:35
	 * 修改内容 :
	 */
	public boolean operateMemberVoucherBalance(List<FundVoucherBalanceVo> list);
	
	/***
	 * 
	 * 
	 * @Title IBlanceService.findCompanyBuyVoucherDeatil
	 * @Description: 查询公司购买抵用券详细信息
	 *
	 * @param companyId
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年11月01日 下午1:01:28
	 * 修改内容 :
	 */
	public Page<CompanyBuyVoucherVo>  findCompanyBuyVoucherDeatil(Query query,String companyId);
	
	/**
	 * 
	 * 
	 * @Title IBlanceService.findCompanyAllotVoucherDeatil
	 * @Description: 查询企业分配抵用券详细信息
	 *
	 * @param query
	 * @param companyId
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年11月01日 下午3:07:18
	 * 修改内容 :
	 */
	public List<CompanyBuyVoucherVo>  findCompanyAllotVoucherDeatil(String companyId);
}
