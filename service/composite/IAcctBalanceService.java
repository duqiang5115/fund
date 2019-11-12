package com.taolue.baoxiao.fund.service.composite;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.fund.api.dto.FundAcctBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.common.exception.FundServiceException;
import com.taolue.baoxiao.fund.entity.TbFundAcct;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;

/**
 * <p>
 * 账单明细表，每一条为账单中的没一个账单项 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2018-09-07
 */
public interface IAcctBalanceService extends IService<FundAcctBalanceDto> {
	
	/**
	 * 
	 * @Title: findFundAcctByMemberIdFundAcctCate   
	 * @Description: 通过账户账号和账户类型查询对应账户
	 * @Author: shilei
	 * @date:   2018年10月6日 下午9:52:15  
	 * @param memberId String 账号
	 * @param fundAcctCate String 账户类型
	 * @param companyIds String[] 可选参数 归属公司
	 * @return List<TbFundAcct> 返回账户对象集合，如果不存在则返回空
	 * 			 
	 * @throws FundServiceException 若指定了归属公司，结果查出多条账户信息，则抛出异常
	 *
	 */
//	public List<TbFundAcct> findFundAccts(String memberId, String fundAcctCate);
	
	/**
	 * 
	 * @Title: findFundAcctByMemberIdFundAcctCate   
	 * @Description: 通过账户账号和账户类型查询对应账户
	 * @Author: shilei
	 * @date:   2018年10月6日 下午9:52:15  
	 * @param memberId String 账号
	 * @param fundAcctCate String 账户类型
	 * @param companyIds String[] 可选参数 归属公司
	 * @return TbFundAcct 返回账户对象，如果不存在则返回空
	 * 			 
	 * @throws FundServiceException 若指定了归属公司，结果查出多条账户信息，则抛出异常
	 *
	 */
//	public TbFundAcct findFundAcct(String memberId, String fundAcctCate);
	
	/**
	 * 通过账户编码反查账户信息
	 * @Title: findAcctByAcctInstNo   
	 * @Description: 通过账户编码反查账户信息
	 * @Author: shilei
	 * @date:   2018年9月5日 下午10:50:40  
	 * @param: String instAcctNo    
	 * @return: TbFundAcct 参数instAcctNo对应的账户信息，返回null表示查询不到   
	 * 
	 * @throws 出现异常则会向上抛出
	 */
//	public TbFundAcct findAcctByAcctInstNo(String acctInstNo);
	
//	/**
//	 * 
//	 * @Title: hadAccount   
//	 * @Description: 判断指定账号是否存在账户
//	 * @Author: shilei
//	 * @date:   2018年10月6日 下午10:16:27  
//	 * @param memberId String 账号
//	 * @return int 如果存在则返回值大于0；否则为0 
//	 */
//	public int hadAccount(String memberId);

	/**
	 * 根据账户资金编号查询账户的资金记录
	 * @Title: findAcctBalanceByCode   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   2018年9月5日 下午10:50:33  
	 * @param: balanceCode 账户资金编号 
	 * @return: TbFundBalance  账户资金记录；返回null表示查询不到记录  
	 * @throws 出现异常则会向上抛出
	 */
//	public TbFundBalance findAcctBalanceByCode(String balanceCode);
	

//	public List<TbFundBalance> findBalanceByNcountId(String ncountId);

	/**
	 * 
	 * @Title: findSignleBalance   
	 * @Description: 获取指定对象某种账户下某种类型的项目的唯一条资金记录
	 * 
	 * @Author: shilei
	 * @date:   2018年9月12日 下午6:23:01  
	 * @param memberId String 必须，账户对象编码
	 * @param fundAcctCate String 必须，账户类型编码
	 * @param balanceItemNo String 必须，资金项目编码
	 * @param busiModel String 必须，业务类型
	 * @param canTicket String 可选，是否可开票，不传默认CommonConstant.STATUS_BALANCE_COMPOS_INUSE-表示可开票
	 * @param canTransfer String 可选，是否可转让，不传默认为CommonConstant.STATUS_BALANCE_COMPOS_INUSE-表示可转让
	 * @param expireTime Date 可选，balanceItemNo参数指定的对象的失效时间
	 * 			不传默认为CommonConstant.DEFALUT_EXPIRE_DATETIME
	 * @param validTime 可选，balanceItemNo参数指定的对象的生效时间
	 * 			不传则不会参与到查询中
	 * @return FundAcctBalanceDto 唯一的一条资金记录；如果查询结果多余一条，则会报异常，说明数据库数据有误
	 *  			如果查询无结果，返回null；
	 * @throws
	 *
	 */
//	public FundBalanceDto findSignleBalance(String memberId, String memeberCate, String acctCate, String acctBalanceItemNo, String balanceItemNo, 
//			String busiModel, String canTicket, String canTransfer, Date validTime, Date expireTime, String companyId, 
//			String ownerId);
	 
	public FundTradeFlowDto warpperTradeFlow(String balanceCode, String businessCode, String busiOrderNo, String billItemCate, String tradeCate,
			String tradeActCode, BigDecimal amount, String source, String remark, String busiModel, String tradeFlowCode, BigDecimal... tradeOrder);
	
	public FundTradeFlowDto warpperTradeFlow(String balanceCode, String businessCode, String busiOrderNo, String billItemCate, String tradeCate,
			String tradeActCode, BigDecimal amount, String source, String remark, String busiModel, BigDecimal... tradeOrder);
		
//	public List<FundBalanceDto> selectFundBalanceItemAmount(Map<String, Object> params);
	
	/**
	 * 
	 * @Title: findFundBalance   
	 * @Description: 非券账户的情况下，查询对应的账户的资金记录，
	 * 		券账户的情况下此方法不支持；
	 * 
	 * @Author: shilei
	 * @date:   2018年10月16日 下午8:55:32  
	 * @param acctInstNo 账户编码
	 * @param balanceItemNo 资金对象编码
	 * @return TbFundBalance 对象  
	 * @throws 如果为券账户则抛出不支持异常
	 *
	 */
//	public TbFundBalance findFundBalance(String acctInstNo, String balanceItemNo);
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 
	 * @Title: findBalances   
	 * @Description: 查询资金记录表和对应账户信息
	 * @Author: shilei
	 * @date:   Nov 22, 2018 12:19:25 AM  
	 * @param memberId 资金归属会员编号
	 * @param memeberCate 资金归属会员类型
	 * @param acctCate 资金账户类型
	 * @param acctBalanceItemNo 资金类型，非券情况下的资金项目的编号
	 * 			
	 * @param balanceItemNo 资金项目编号
	 * @param busiModel 业务模式
	 * @param canTicket 是否可以开票 默认为1，可开票
	 * @param canTransfer 是否可以转让 默认为1，可转让
	 * @param validTime 生效时间
	 * @param expireTime 失效时间
	 * @param companyId 归属公司会员编号
	 * @param ownerId 归属人会员编号
	 * @return FundAcctBalanceDto 对象；封装了资金对应账户和该账户下的所有资金记录对象
	 * @throws
	 *
	 */
//	public FundAcctBalanceDto findBalances(String memberId, String memeberCate, String acctCate, 
//			String acctBalanceItemNo, String balanceItemNo, String busiModel, String canTicket, String canTransfer, 
//			Date validTime, Date expireTime, String companyId, String ownerId);
	
	
	/**
	 * 
	 * @Title: findCompanyBlanceByCate   
	 * @Description: 查询保证金账户或者报销账户
	 * @Author: shilei
	 * @date:   Nov 13, 2018 11:15:18 AM  
	 * @param memberId 账号编码
	 * @param balanceCate 账户模式，常量值
	 * CommonConstant.BALANCE_ITEM_NO_SUFFX_BZJ 保证金账户
	 * CommonConstant.BALANCE_ITEM_NO_SUFFX_GBX 报销账户
	 * 
	 * @return FundComposDto  返回对应账户的结果，返回空则没有查询到结果
	 * @throws 某个资金查询不到，则抛9082异常
	 *
	 */
//	public FundComposDto findCompanyBlanceByCate(String memberId, String balanceCate);
	
	/**
	 * 
	 * @Title: findBalanceFlowDetailsPaged   
	 * @Description: 按SearchParamEnums类型分页查询账户资金流水
	 * @Author: shilei
	 * @date:   Nov 13, 2018 4:54:17 PM  
	 * @param page Page<TbFundTradeFlow> 分页参数
	  * @param memberId Sring 账户相关账号
	 * @param paramCode String SearchParamEnums 的查询类型编码
	 *                  具体请参见SearchParamEnums枚举
	 *                  
	 * @param beginTime Date 开始时间
	 * @param endTime Date 结束时间
	 * 
	 * @return Page<TbFundTradeFlow>  分页返回 交易流水列表
	 * @throws
	 *
	 */
//	public Page<TbFundTradeFlow> findBalanceFlowDetailsPaged(Page<TbFundTradeFlow> page,String memberId, String paramCode, 
//			Date beginTime, Date endTime);
	
	/**
	 * 
	 * @Title: findBalanceFlowDetails   
	 * @Description: 按SearchParamEnums类型查询账户资金流水
	 * @Author: shilei
	 * @date:   Nov 13, 2018 9:57:47 AM  
	 * @param memberId Sring 账户相关账号
	 * @param paramCode String SearchParamEnums 的查询类型编码
	 *                  具体请参见SearchParamEnums枚举
	 *                  
	 * @param beginTime Date 开始时间
	 * @param endTime Date 结束时间
	 * 
	 * @return List<TbFundTradeFlow> 交易流水列表
	 * @throws
	 * 
	 */
	public List<TbFundTradeFlow> findBalanceFlowDetails(String memberId, String paramCode, 
			Date beginTime, Date endTime);
	
	/**
	 * 
	 * @Title: findPerOrAftPaymentBalance   
	 * @Description: 查询企业现金或者垫资账户资金信息
	 * @Author: shilei
	 * @date:   2018年10月10日 下午3:52:44  
	 * @param memberId String 企业编码
	 * @param busiModel String 业务模式编码, 参见BusiModelEnums
	 * BusiModelEnums.BUSI_MODEL_YCCM 和 BusiModelEnums.BUSI_MODEL_YCPE 查询现金
	 * 
	 * BusiModelEnums.BUSI_MODEL_HFCM 和 BusiModelEnums.BUSI_MODEL_HFPE 查询垫资
	 * @return TbFundBalance 对象，账户资金信息
	 */
//	public TbFundBalance findPerOrAftPaymentBalance(String memberId, String busiModel);
	
	/**
	 * 
	 * @Title: findTradeFlows   
	 * @Description: 查找交易流水
	 * @Author: shilei
	 * @date:   Oct 24, 2018 4:49:19 PM  
	 * @param busiCode String 必传 业务订单号码
	 * @param billItemCate String 必传 资金科目 参考BillItemSubCate枚举
	 * @param balanceCode String  可选 资金编码，资金表记录的编码
	 * @param tradeAmount BigDecimal  可选 交易金额
	 * @param tradeCateCodes String...  可选 交易流水类型 参考TradeCateEnums枚举
	 * 
	 * @return TbFundTradeFlow  返回交易流水实体，如果返回null则表示查询不到      
	 * @throws
	 *
	 */
	public List<TbFundTradeFlow> findTradeFlows(String busiCode, String billItemCate, String balanceCode,
			BigDecimal tradeAmount, String... tradeCateCodes);
	
	/**
	 * 
	 * @Title: findTradeFlows   
	 * @Description: 按条件查询交易流水
	 * @Author: shilei
	 * @date:   Nov 13, 2018 10:32:18 AM  
	 * @param balanceCode String 资金编号
	 * @param busiModel String 业务模式
	 * @param billItemCate String 资金对象编码
	 * @param orderType String 订单类型
	 * @param tradeCate String 交易类型
	 * @param status String 交易状态
	 * @param busiCode String 业务编码
	 * @param tradeAmount BigDecimal 交易额
	 * @param beginTime Date 开始时间
	 * @param endTime Date 结束时间
	 * @return List<TbFundTradeFlow>        
	 * @throws
	 *
	 */
	public List<TbFundTradeFlow> findTradeFlows(String balanceCode, String busiModel, String billItemCate, String orderType,
			String tradeCate, String status, String busiCode, BigDecimal tradeAmount, Date beginTime, Date endTime);
	
	/**
	 * 使用DTO更新或者新建TbFundBalance
	 * @Title: updateFundBalance   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   Oct 24, 2018 2:30:07 PM  
	 * @param fundBalanceDto FundBalanceDto对象
	 * @return TbFundBalance 对象，插入或者更新的TbFundBalance实体对象    
	 *
	 */
	public TbFundBalance updateFundBalance(FundBalanceDto fundBalanceDto);
	
	/**
	 * 使用实体更新或者新建TbFundTradeFlow
	 * @Title: updateFundTradeFlow   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   Oct 24, 2018 2:30:07 PM  
	 * @param fundTradeFlow TbFundTradeFlow对象
	 * @return TbFundTradeFlow 对象，插入或者更新的TbFundTradeFlow实体对象    
	 *
	 */
	public TbFundTradeFlow updateFundTradeFlow(TbFundTradeFlow fundTradeFlow);
	
	/**
	 * 使用DTO更新或者新建TbFundTradeFlow
	 * @Title: updateFundTradeFlow   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   Oct 24, 2018 2:30:07 PM  
	 * @param fundTradeFlowDto FundTradeFlowDto对象
	 * @return TbFundTradeFlow 对象，插入或者更新的TbFundTradeFlow实体对象    
	 *
	 */
	public TbFundTradeFlow updateFundTradeFlow(FundTradeFlowDto fundTradeFlowDto);
}
