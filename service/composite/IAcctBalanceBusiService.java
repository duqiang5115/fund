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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.plugins.Page;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.util.Group;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.BalanceSearchParams;
import com.taolue.baoxiao.fund.api.dto.BusinessApplyBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundComposDto;
import com.taolue.baoxiao.fund.api.dto.QueryFundBalanceDto;
import com.taolue.baoxiao.fund.api.vo.FundBalanceVo;
import com.taolue.baoxiao.fund.common.exception.FundServiceException;
import com.taolue.baoxiao.fund.entity.TbFundAcct;

/**   
 * @ClassName:  IAccountService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月28日 上午10:39:33   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IAcctBalanceBusiService {

	/**
	 * <p>名称: findGroupedCouponBalances方法</br>    
	 * <p>描述: 分组查询券类型（消费券/面额券/定向券）资金记录</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Jan 15, 2019 11:05:07 AM</br>
	 * @throws Exception
	 * @param memberIds {@link List}{@literal <}{@link String}{@literal >} 消费券归属会员编号列表
	 * @param busiModels {@link List}{@literal <}{@link String}{@literal >} 消费券资金业务模式编码列表，
	 * 		业务模式参见{@link BusiModelEnums}枚举
	 * @param companyId {@link String} 消费券归属公司编号，可选
	 * @param acctCate {@link String} 账户类型，可选；账户类型参见 {@link AcctCateEnums} 若不传默认为 {@link AcctCateEnums#ACCT_CATE_COUPON} 的编码值（券账户）
	 * @param canTransfer {@link String} 消费券是否可以转让，可选
	 * @param groups {@link List}{@literal <}{@link Group}{@literal >} 分组条件，可选；若不传默认为
	 * 			{@link CommonConstant#DEFAULT_BALANCE_GROUP_TWOCOLUMNS} 分组方式
	 * @param balanceItemCodes {@link List}{@literal <}{@link String}{@literal >} 消费券编号列表,可选；
	 * @return {@link List}{@literal <}{@link FundBalanceDto}{@literal >} 返回按groups指定的分组，分组后的资金记录集合
	 * 
	 *
	 */
//	List<FundBalanceDto> findGroupedCouponBalances(List<String> memberIds, List<String> busiModels, String companyId, 
//			String acctCate, String canTransfer, List<Group> groups, List<String> balanceItemCodes);
	
	/**
	 * 
	 * <p>名称:类IAcctBalanceBusiService中的findGroupedCouponBalances方法</br>    
	 * <p>描述: 根据查询对象查询券类型（消费券/面额券/定向券）资金</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Jan 28, 2019 12:50:59 AM</br>
	 * @throws Exception
	 * @param queryParams {@link BalanceSearchParams} 查询参数对象；查询参数说明：
	 * <ul>
	 * 	<li>{@link BalanceSearchParams#memberIds} {@link List}{@literal <}{@link String}{@literal >} 会员编号列表</li>
	 *  <li>{@link BalanceSearchParams#companyId} {@link String} 当前会员的券资金归属公司编号</li>
	 *  <li>{@link BalanceSearchParams#acctCate} {@link String} 当前会员的券资金归属账户类型，可选；
	 *  	账户类型参见 {@link AcctCateEnums} 可选值有：
	 *      <ul>
	 *      	<li>{@link AcctCateEnums#ACCT_CATE_COUPON} 消费券账户编码值</li>
	 *      	<li>{@link AcctCateEnums#ACCT_CATE_DIRECT_COUPON} 定向券账户编码值</li>
	 *      	<li>{@link AcctCateEnums#ACCT_CATE_NCOUNT_COUPON} 面额券账户编码值</li>
	 *      	<li>{@link CommonConstant#STRING_BLANK} 不指定该参数，此时默认
	 *      		{@link AcctCateEnums#ACCT_CATE_COUPON} 的编码值</li>
	 *      </ul>
	 *      若不传默认为 {@link AcctCateEnums#ACCT_CATE_COUPON} 的编码值
	 *  </li>
	 *  <li>{@link BalanceSearchParams#canTransfer} {@link String} 当前会员的券资金的转让属性，可选值为
	 *  	<ul>
	 *  		<li>{@link CommonConstant#STATUS_YES} 可转让</li>
	 *  		<li>{@link CommonConstant#STATUS_NO} 不可转让</li>
	 *  		<li>{@link CommonConstant#STRING_BLANK} 不指定该参数</li>
	 *  	</ul>
	 *  </li>
	 *  <li>{@link BalanceSearchParams#balanceItemCodes} {@link List}{@literal <}{@link String}{@literal >} 
	 *  	需要查询的券资金的券编码列表，可以不指定</li>
	 *  <li>{@link BalanceSearchParams#groups}{@link List}{@literal <}{@link Group}{@literal >} 分组字段
	 *  	在CommonConstant里定义了几个常用的分组方式，可以直接使用，分别为：
	 *  <ul>
	 *  	<li>{@link CommonConstant#DEFAULT_BALANCE_GROUP_TWOCOLUMNS}</li>
	 *  	<li>{@link CommonConstant#DEFAULT_BALANCE_GROUP_TWOCOLUMNS_V2}</li>
	 *      <li>{@link CommonConstant#DEFAULT_BALANCE_GROUP_THREECOLUMNS}</li>
	 *      <li>{@link CommonConstant#DEFAULT_BALANCE_GROUP_THREECOLUMNS_V2}</li>
	 *      <li>{@link CommonConstant#DEFAULT_BALANCE_GROUP_WITHFLOW}</li>
	 *  </ul>
	 *  也可以自定义自己的分组字段，{@link Group} 包含字段 
	 *  <ul>
	 *  	<li>{@link Group#tableName} 分组字段表名 此处必须为小写字母“b”</li>
	 *      <li>{@link Group#columName} 分组字段列名</li>
	 *  </ul>
	 *  若设置该属性为null值，则不进行分组；如果该属性不为null但为空则设置默认分组
	 *  {@link CommonConstant#DEFAULT_BALANCE_GROUP_TWOCOLUMNS}
	 * </li>
	 * <li>{@link BalanceSearchParams#balanceAmount} {@link BigDecimal} 资金是否限制最小可用金额，默认为0；
	 * 		该属性会添加资金记录的balance_amount值大于该指定金额查询条件；若不需要该条件，请将其设置为null；</li>
	 * <li>{@link BalanceSearchParams#requireAmount} {@link BigDecimal} 资金可用金额总和是否限制最小可用金额，默认为0；
	 * 		该属性会添加资金记录的sum(balance_amount)值大于该指定金额HAVING条件；</li>
	 * 
	 * @return
	 */
	List<FundBalanceDto> findGroupedCouponBalances(BalanceSearchParams params);
	
	/**
	 * 
	 * @Title: findNoCouponBalance   
	 * @Description: 查询非券账户资金
	 * @Author: shilei
	 * @date:   Dec 10, 2018 4:51:19 PM  
	 * @param memberId String 资金归属会员编号
	 * @param fundAcctCate String 资金账户全类型（会员类型+账户类型）
	 * 			参见MemberCateEnums枚举和AcctCateEnums枚举
	 * @param balanceItemNo String 资金类型
	 * @return FundBalanceDto 资金对象   
	 * @throws
	 *
	 */
	FundBalanceDto findNoCouponBalance(String memberId, String balanceItemNo);
	
	/**
	 * 
	 * @Title: findPerOrAftPaymentBalance   
	 * @Description: 查询企业预充值（CM10006）或者 企业后付费（CM10007）账户资金 
	 * @Author: shilei
	 * @date:   Dec 10, 2018 5:06:54 PM  
	 * @param memberId String - 企业会员标识
	 * @param busiModel String - 业务模式（BUSI_MODEL_YCCM/BUSI_MODEL_YCPE-查询预充值账户资金；
	 * 						BUSI_MODEL_HFCM/BUSI_MODEL_HFPE-查询后付费账户资金；）
	 * 				业务模式参见BusiModelEnums枚举 可选参数
	 * @return FundBalanceDto 对应的资金记录   
	 * @throws
	 *
	 */
	FundBalanceDto findPerOrAftPaymentBalance(String memberId, String... busiModel);
	
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
	FundComposDto findCompanyBlanceByCate(String memberId, String balanceCate);
	
	/**
	 * 
	 * @Title: selectOgCouponsGroupMemberInfo   
	 * @Description: 分页查询部门券账户资金，已部门id和对应的部门账户编号分组
	 * @Author: shilei
	 * @date:   Dec 11, 2018 9:53:18 PM  
	 * @param page Query<FundBalanceDto> 分页对象
	 * @param memberIds List<String> 部分编号列表
	 * @return List<FundBalanceDto> 结果列表      
	 * @throws
	 *
	 */
	Query<FundBalanceDto> selectDepartmentCouponGroupMemberInfo(Query<FundBalanceDto> page, List<String> memberIds);
	
	List<FundBalanceDto> findBalanceWithFlowParams(QueryFundBalanceDto query);
	
	List<FundBalanceDto> findBalanceGroupWithFlowParams(String tradeBusiCode);
	
	List<FundBalanceDto> findBalancesByParams(BalanceSearchParams params);
	
	List<FundBalanceDto> findBalancesByParams(String companyId, String memberId, String memberCate, String acctCate, 
    		String balanceItemNo, String busiModel, String canTicket, String canTransfer, String ownerId, 
    		Date validTime, Date expireTime);
	
    FundBalanceDto findSignleBalanceByParams(String companyId, String memberId, String memberCate, String acctCate, 
    		String balanceItemNo, String busiModel, String canTicket, String canTransfer, String ownerId, 
    		Date validTime, Date expireTime);
    
    FundBalanceDto findSignleBalanceByCode(String balanceCode);
	
	FundBalanceDto findSignleBalanceByBalanceItemCode(String balanceItemCode);
	
	FundBalanceDto findSignleBalanceByAcctInstNo(String acctInstNo, String balanceItemNo);
	
	BigDecimal selectTotalNoCoupon(BalanceSearchParams params);
	
	Page<FundBalanceDto> selectNoCouponByConditionsPage(BalanceSearchParams params, Query<FundBalanceDto> query);
	
	
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
	List<TbFundAcct> findFundAccts(String memberId, String fundAcctCate);
	
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
	TbFundAcct findFundAcct(String memberId, String fundAcctCate);
	
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
	TbFundAcct findAcctByAcctInstNo(String acctInstNo);
	
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
	boolean accountAmount(String memberId, String amount);
	
	/**
	 * 
	 * <p>名称:类IAcctBalanceBusiService中的findBalanceWithFlowSumAmount方法</br>    
	 * <p>描述: 查询指定的会员，订单类型和交易类型的交易总额</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Feb 14, 2019 7:45:44 PM</br>
	 * @throws Exception
	 * @param memberId String 会员编号
	 * @param tradeBusiCate String 订单类型
	 * @param tradeCate String 交易类型
	 * @param isCompany boolean 是否查询公司下的所有账户（人员或者部门）默认为false；
	 * 若为true则memberId参数为公司会员编号；会查询该会员编号下的所有员工和部门下的消费券总额；
	 * 若为false则会查询memberId指定的会员自己所有消费券的总和
	 * @return FundBalanceDto 查询结果，FundBalanceDto对想返回订单类型，交易类型，和总额三个属性
	 * 查询无结果时返回null
	 * 
	 */
	FundBalanceDto findBalanceWithFlowSumAmount(String memberId, String tradeBusiCate, 
			String tradeCate, boolean isCompany);
	
	
	/**
	 * 
	 * @Title: getAccountAmountByMemberId   
	 * @Description:  查询员工的现金账户可用余额
	 * @param: @param cpmpanyId
	 * @param: @param memberId
	 * @param: @return    
	 * @author: duqiang     
	 * @return: BigDecimal      
	 * @throws
	 */
	//public  BigDecimal getAccountAmountByMemberId(String cpmpanyId,String memberId);
	/**
	 * 
	 * @Title: repairBalance   
	 * @Description: 修复每日限额的数据问题
	 * @author: zyj
	 * @date:   2019年5月23日 下午12:56:40  
	 * @param: @param dto
	 * @param: @return      
	 * @return: R<Boolean>      
	 * @throws
	 */
	  public R<Boolean> repairBalance( BusinessApplyBalanceDto dto);

	  /**
	      * 查询员工券账户信息
	   * fbh
	   * @param dto
	   * @return
	   */
	public List<FundBalanceVo> findMemberCouponBalance(FundBalanceVo dto);
	
	/**
	 * 导出查询员工券账户信息
	 * fbh
	 * @param dto
	 * @return
	 */
	public List<FundBalanceVo> findMemberCouponBalanceExport(FundBalanceVo dto);
	
	/**
	 * 查询员工券账户明细信息
	 * fbh
	 * @param dto
	 * @return
	 */
	public List<FundBalanceVo> findMemberCouponBalanceDetail(FundBalanceVo dto);
}
