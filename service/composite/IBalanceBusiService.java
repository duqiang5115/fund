/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  IBalanceBusiService.java   
 * @Package com.taolue.baoxiao.fund.service.composite   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 9, 2018 7:24:16 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;

import com.baomidou.mybatisplus.plugins.Page;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.util.Group;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.BalanceSearchParams;
import com.taolue.baoxiao.fund.api.dto.FundAcctDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;

import com.taolue.baoxiao.fund.api.vo.FundBalanceVo;

import com.taolue.baoxiao.fund.api.vo.FundAcctVo;
import com.taolue.baoxiao.fund.api.vo.FundBalanceVo;
import com.taolue.baoxiao.fund.api.vo.FundTradeFlowVo;
import com.taolue.baoxiao.fund.api.vo.FundVoucherBalanceVo;
import com.taolue.member.api.dto.QueryCompanyDto;


/**   
 * @ClassName:  IBalanceBusiService   
 * @Description: 所有关于资金的业务逻辑
 * @Author: shilei
 * @date:   Dec 9, 2018 7:24:16 PM   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IBalanceBusiService {
	
	/**
	 * 
	 * @Title: selectByConditions   
	 * @Description: 通过资金查询对象BalanceSearchParams查询资金记录
	 * @Author: shilei
	 * @date:   Dec 9, 2018 11:56:14 PM  
	 * @param params BalanceSearchParams 资金查询对象
	 *      params.groups List<Group> - 分组字段数组，如果为空则不分组;默认为空不分组；若需要分组则此数组给定分组字段；
	 *      	指定分组字段时，需要同时指定表名称；在CommonConstant.DEFAULT_BALANCE_GROUP_TWOCOLUMNS常量中指定了一组默认
	 *          分组字段为acct_inst_no和balance_item_code；分组时如果requireAmount大于零，则会添加条件balanceAmount的和
	 *          是否大于等于requireAmount；否则添加条件balanceAmount的和是否大于零
	 *      params.lockSecond int - 是否需要锁定查询结果；默认为CommonConstant.NO_LOCKE_SECOND不锁定;
	 *      				若锁定，则该值指定锁的持续秒数
	 *      params.weight BigDecimal - 权重默认 CommonConstant.NO_AMOUNT无权重
	 *      params.requireAmount BigDecimal - 余额，默认CommonConstant.NO_AMOUNT表示不使用该参数
	 *      params.canTicket String String 资金是否可以开票，字符串“0”表示不可开票，字符串“1”表示可以开票 默认
	 *      		CommonConstant.STATUS_YES表示可开票
	 *      params.canTransfer String - 资金是否可以转让，字符串“0”表示不可转让，字符串“1”表示可以转让
	 *      params.status String - 记录状态，默认CommonConstant.STATUS_NORMAL表示正常状态;
	 *      params.memberId String - 账户归属对象编号（会员id，项目id，等）
	 *      params.memberCate String - 账户归属对象类型 （公司，雇员，会员，部门，项目等）
	 *      params.acctCate String - 账户类型，可选的类型已经在 AcctCateEnums 中定义
	 *      params.acctInstNo String - 账户实例编码，即在系统中的账户编号
	 *      params.balanceCode String - 资金的编码，系统分配给该资金记录的编号，全局唯一
	 *      params.busiModel String - 资金归属的业务模式，可选的模式已经在BusiModelEnums中定义
	 *      params.companyId String - 资金归属组织，如归属某个企业或者某个项目
	 *      params.ownerId String - 资金归属人，如资金归属某个雇员或者某个会员
	 *      params.validTime Date - 该笔资金的生效时间，默认1970-01-01 00:00:00
	 *      params.expireTime Date - 该笔资金的失效时间，默认为2099-12-31 23:59:59
	 *      params.composAttr String - 资金的默认扩展属性，目前只有面额券资金使用
	 *      params.balanceItemCodes - 资金项目编码列表，资金的具体分配对象，如券的编码
	 * 		
	 * @return List<FundBalanceDto> FundBalanceDto的列表，无结果返回null对象      
	 * @throws
	 *
	 */
	List<FundBalanceDto> selectByConditions(BalanceSearchParams params);
	
	/**
	 * 
	 * @Title: selectByConditions   
	 * @Description: 通过资金查询对象BalanceSearchParams查询资金记录
	 * @Author: shilei
	 * @date:   Dec 9, 2018 11:56:14 PM  
	 * @param params BalanceSearchParams 资金查询对象
	 *      params.groups List<Group> - 分组字段数组，如果为空则不分组;默认为空不分组；若需要分组则此数组给定分组字段；
	 *      	指定分组字段时，需要同时指定表名称；在CommonConstant.DEFAULT_BALANCE_GROUP_TWOCOLUMNS常量中指定了一组默认
	 *          分组字段为acct_inst_no和balance_item_code；分组时如果requireAmount大于零，则会添加条件balanceAmount的和
	 *          是否大于等于requireAmount；否则添加条件balanceAmount的和是否大于零
	 *      params.needLock int - 是否需要锁定查询结果；默认为CommonConstant.NO_LOCKE_SECOND不锁定;
	 *      				若锁定，则该值指定锁的持续秒数
	 *      params.weight BigDecimal - 权重默认 CommonConstant.NO_AMOUNT无权重
	 *      params.requireAmount BigDecimal - 余额，默认CommonConstant.NO_AMOUNT表示不使用该参数
	 *      params.canTicket String String 资金是否可以开票，字符串“0”表示不可开票，字符串“1”表示可以开票 默认
	 *      		CommonConstant.STATUS_YES表示可开票
	 *      params.canTransfer String - 资金是否可以转让，字符串“0”表示不可转让，字符串“1”表示可以转让
	 *      params.status String - 记录状态，默认CommonConstant.STATUS_NORMAL表示正常状态;
	 *      params.memberId String - 账户归属对象编号（会员id，项目id，等）
	 *      params.memberCate String - 账户归属对象类型 （公司，雇员，会员，部门，项目等）
	 *      params.acctCate String - 账户类型，可选的类型已经在 AcctCateEnums 中定义
	 *      params.acctInstNo String - 账户实例编码，即在系统中的账户编号
	 *      params.balanceCode String - 资金的编码，系统分配给该资金记录的编号，全局唯一
	 *      params.busiModel String - 资金归属的业务模式，可选的模式已经在BusiModelEnums中定义
	 *      params.companyId String - 资金归属组织，如归属某个企业或者某个项目
	 *      params.ownerId String - 资金归属人，如资金归属某个雇员或者某个会员
	 *      params.validTime Date - 该笔资金的生效时间，默认1970-01-01 00:00:00
	 *      params.expireTime Date - 该笔资金的失效时间，默认为2099-12-31 23:59:59
	 *      params.composAttr String - 资金的默认扩展属性，目前只有面额券资金使用
	 *      params.balanceItemCodes - 资金项目编码列表，资金的具体分配对象，如券的编码
	 * @param rowBounds 分页查询条件（可以为 RowBounds.DEFAULT）
	 * @return Query<FundBalanceDto> 带分页信息的FundBalanceDto的列表，无结果分页我对象的数据属性为空     
	 * @throws
	 *
	 */
	Query<FundBalanceDto> selectByConditionsPage(BalanceSearchParams params, Query<FundBalanceDto> query);
	
	/**
	 * 
	 * <p>名称:类IBalanceBusiService中的selectNoCouponByConditionsPage方法</br>    
	 * <p>描述:(这里用一句话描述这个方法的作用)</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Jan 15, 2019 11:05:07 AM</br>
	 	 * @throws Exception
	 * @param params {@link BalanceSearchParams} 资金查询对象
	 * <ul>
	 * <li>{@link BalanceSearchParams#memberId} 当前会员编码</li>
	 * </ul>
	 * @param rowBounds {@link RowBounds} 分页查询参数
	 * @return {@link Query}{@literal <}{@link FundBalanceDto}{@literal >} 带分页信息的面额券信息列表
	 * <ul>
	 * <li>{@link FundBalanceDto#balanceItemCode} 面额券编码</li>
	 * <li>{@link FundBalanceDto#balanceAmount} 面额券面额</li>
	 * <li>{@link FundBalanceDto#status} 面额券状态
	 * 	<ul>
	 * 		<li>{@link CommonConstant#FLAG_NO_SYNCH} 已充值</li>
	 * 		<li>{@link CommonConstant#STATUS_NO} 待充值</li>
	 * 		<li>{@link CommonConstant#STATUS_YES} 充值中</li>
	 *  </ul>
	 * </li>
	 * <li>{@link FundBalanceDto#createTime} 买入时间</li>
	 * </ul>
	 */
	Page<FundBalanceDto> selectNoCouponByConditionsPage(BalanceSearchParams params, Query<FundBalanceDto> query);
	
	/**
	 * 
	 * @Title: selectByConditions   
	 * @Description: 根据账户信息和资金属性查询资金记录
	 * @Author: shilei
	 * @date:   Dec 9, 2018 7:52:58 PM  
	 * @param acctInfoParams FundAcctDto对象， 资金归属账户;通常情况下会使用的此对象的如下属性中的一个或者多个
	 *     acctInfoParams.acctCate - String 账户类型，可选的类型已经在 AcctCateEnums 中定义
	 *     acctInfoParams.memberId - String 账户归属对象编号（会员id，项目id，等）
	 *     acctInfoParams.memberCate - String 账户归属对象类型 （公司，雇员，会员，部门，项目等）
	 *     		可选类型已经在MemberCateEnums中定义
	 *     acctInfoParams.acctInstNo String - 账户实例编码，即在系统中的账户编号
	 *     
	 * @param balanceInfoParams - FundBalanceDto 对象，资金属性查询参数，如果指定，则会查询符合指定资金属性的资金记录，通常情况下会使用该对象的如下属性中的一个或多个
	 * balanceInfoParams.balanceCode - String 资金的编码，系统分配给该资金记录的编号，全局唯一
	 * balanceInfoParams.balanceItemCode - String 资金项目编码，资金的具体分配对象，如券的编码
	 * balanceInfoParams.busiModel - String 资金归属的业务模式，可选的模式已经在BusiModelEnums中定义
	 * balanceInfoParams.companyId - String 资金归属组织，如归属某个企业或者某个项目
	 * balanceInfoParams.ownerId - String 资金归属人，如资金归属某个雇员或者某个会员
	 * balanceInfoParams.canTicket - String 资金是否可以开票，字符串“0”表示不可开票，字符串“1”表示可以开票
	 * balanceInfoParams.canTransfer - String 资金是否可以转让，字符串“0”表示不可转让，字符串“1”表示可以转让
	 * balanceInfoParams.validTime - Date 该笔资金的生效时间，默认为1970-01-01 00:00:00
	 * balanceInfoParams.expireTime - Date 该笔资金的失效时间，默认为2099-12-31 23:59:59
	 * balanceInfoParams.status - String 资金的状态，未启用默认为1
	 * balanceInfoParams.composAttr - String 资金的默认扩展属性，目前未启用，默认空白字符
	 * @param lockSecond int - 是否需要锁定查询结果；默认为CommonConstant.NO_LOCKE_SECOND不锁定;
	 *      				若锁定，则该值指定锁的持续秒数
	 * @param Groups List<Group> - 分组字段数组，如果为空则不分组;默认为空不分组；若需要分组则此数组给定分组字段；
	 *      	指定分组字段时，需要同时指定表名称；在CommonConstant.DEFAULT_BALANCE_GROUP_TWOCOLUMNS常量中指定了一组默认
	 *          分组字段为acct_inst_no和balance_item_code；分组时如果requireAmount大于零，则会添加条件balanceAmount的和
	 *          是否大于等于requireAmount；否则添加条件balanceAmount的和是否大于零
	 * @param balanceItemCodes 可变参数，可以用此参数指定一批 资金项目编码，以此来查询这些项目的资金记录
	 * 
	 * @return List<FundBalanceDto> FundBalanceDto的列表，无结果返回null对象
	 *     
	 * @throws
	 *
	 */
	List<FundBalanceDto> selectByConditions(FundAcctDto acctInfoParams, FundBalanceDto balanceInfoParams, 
			int lockSecond, List<Group> groups, String... balanceItemCodes);

	/**
	 * 
	 * @Title: selectByConditions   
	 * @Description: 根据账户信息和资金属性查询资金记录
	 * @Author: shilei
	 * @date:   Dec 9, 2018 7:52:58 PM  
	 *     
	 * @param balanceInfoParams - FundBalanceDto 对象，资金属性查询参数，如果指定，则会查询符合指定资金属性的资金记录，通常情况下会使用该对象的如下属性中的一个或多个
	 * <blockquote><pre>
	 * 	balanceInfoParams.balanceCode - String 资金的编码，系统分配给该资金记录的编号，全局唯一
	 * 	balanceInfoParams.balanceItemCode - String 资金项目编码，资金的具体分配对象，如券的编码
	 * 	balanceInfoParams.acctInstNo String - 账户实例编码，即在系统中的账户编号
	 * 	balanceInfoParams.busiModel - String 资金归属的业务模式，可选的模式已经在BusiModelEnums中定义
	 * 	balanceInfoParams.companyId - String 资金归属组织，如归属某个企业或者某个项目
	 * 	balanceInfoParams.ownerId - String 资金归属人，如资金归属某个雇员或者某个会员
	 * 	balanceInfoParams.canTicket - String 资金是否可以开票，字符串“0”表示不可开票，字符串“1”表示可以开票
	 * 	balanceInfoParams.canTransfer - String 资金是否可以转让，字符串“0”表示不可转让，字符串“1”表示可以转让
	 * 	balanceInfoParams.validTime - Date 该笔资金的生效时间，默认为1970-01-01 00:00:00
	 * 	balanceInfoParams.expireTime - Date 该笔资金的失效时间，默认为2099-12-31 23:59:59
	 * 	balanceInfoParams.status - String 资金的状态，未启用默认为1
	 * 	balanceInfoParams.composAttr - String 资金的默认扩展属性，目前未启用，默认空白字符
	 * </pre></blockquote>
	 * 
	 * @param lockSecond int - 是否需要锁定查询结果；默认为CommonConstant.NO_LOCKE_SECOND不锁定;
	 *      				若锁定，则该值指定锁的持续秒数 
	 * @param groups List<Group> - 分组字段数组，如果为空则不分组;默认为空不分组；若需要分组则此数组给定分组字段；
	 *      	指定分组字段时，需要同时指定表名称；在CommonConstant.DEFAULT_BALANCE_GROUP_TWOCOLUMNS常量中指定了一组默认
	 *          分组字段为acct_inst_no和balance_item_code；分组时如果requireAmount大于零，则会添加条件balanceAmount的和
	 *          是否大于等于requireAmount；否则添加条件balanceAmount的和是否大于零
	 * @return List<FundBalanceDto>   TbFundBalance的列表，无结果返回null对象     
	 * @throws
	 *
	 */
	List<FundBalanceDto> selectByConditions(FundBalanceDto balanceInfoParams, int lockSecond, List<Group> groups);
	
	/**
	 * 
	 * @Title: selectOneByBalanceItemCode   
	 * @Description: 根据资金的balanceItemCode查询唯一一条资金记录，目前只用在面额券的查询中
	 * 	因为某张面额券的资金记录balanceItemCode是全局唯一的，以此可以用该属性唯一确定一张面额券
	 *  消费券的balanceItemCode不唯一，同一balanceItemCode的消费券资金记录可以有多条
	 *  
	 *  
	 * @Author: shilei
	 * @date:   Dec 9, 2018 8:25:30 PM  
	 * @param balanceItemCode String 面额券的编码
	 * @param lockSecond int - 是否需要锁定查询结果；默认为CommonConstant.NO_LOCKE_SECOND不锁定;
	 *      				若锁定，则该值指定锁的持续秒数
	 * @return FundBalanceDto 对象       
	 * @throws
	 */
	FundBalanceDto selectOneByBalanceItemCode(String balanceItemCode, int lockSecond);
	
	/**
	 * 
	 * @Title: selectOneByBalanceCode   
	 * @Description: 通过资金的编码 查询唯一一条资金记录，因为资金的编码是全局唯一的，所以该方法适合所有资金
	 * @Author: shilei
	 * @date:   Dec 9, 2018 8:28:32 PM  
	 * @param balanceCode String 资金的编码
	 * @param lockSecond int - 是否需要锁定查询结果；默认为CommonConstant.NO_LOCKE_SECOND不锁定;
	 *      				若锁定，则该值指定锁的持续秒数
	 * @return FundBalanceDto 对象     
	 * @throws
	 *
	 */
	FundBalanceDto selectOneByBalanceCode(String balanceCode, int lockSecond);
	
	/**
	 * 
	 * <p>名称:类IBalanceBusiService中的selectTotalNoCoupon方法</br>    
	 * <p>描述:查询待充值面额券总额</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Jan 15, 2019 12:08:07 PM</br>
	 * @throws Exception
	 * @param params 查询参数
	 * @return BigDecimal 待充值面前券总额
	 */
	BigDecimal selectTotalNoCoupon(BalanceSearchParams params);
	
	/**
	 * 
	 * <p>名称:类IBalanceBusiService中的selectFundBalanceForChash方法</br>    
	 * <p>描述: 查询和校验券消费时的券的额度是否满足</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Jan 27, 2019 3:52:40 PM</br>
	 * @throws Exception
	 * @param params
	 * @return
	 */
	List<FundBalanceDto> selectFundBalanceForChash(Map<String, Object> params);

	/**
	 * 查询券账户资金信息
	 * @param dto
	 * @return
	 */
	public List<FundBalanceVo> findMemberCouponBalance(FundBalanceVo dto);
	
	/**
	 * 导出查询券账户资金信息
	 * @param dto
	 * @return
	 */
	public List<FundBalanceVo> findMemberCouponBalanceExport(FundBalanceVo dto);
	
	/**
	 * 查询券账户明细信息
	 * @param dto
	 * @return
	 */
	public List<FundBalanceVo> findMemberCouponBalanceDetail(FundBalanceVo dto);
	/**
	* @Title: findCompanyBalance  
	* @Description: 查询企业积分账户信息 
	* @param funddto
	* @return List<FundBalanceVo>
	* @author fbh
	* @date 2019年10月22日  下午2:54:55
	* @throws
	 */
	public List<FundBalanceVo> findCompanyBalance(FundBalanceVo funddto);

	/**
	* @Title: findCompanyIntegralAcc  
	* @Description: 查询企业积分、抵用券、员工抵用券账户
	* @param @param dto
	* @param @return
	* @return FundAcctVo
	* @author fbh
	* @date 2019年10月22日  下午5:49:24
	* @throws
	 */
	public FundAcctVo findCompanyIntegralAcc(FundAcctVo dto);

	/**
	* @Title: findVoucherNumber  
	* @Description: 查询员工未使用的抵用券数量  
	* @param @param memberId
	* @param @return
	* @return Integer
	* @author fbh
	* @date 2019年10月23日  下午3:42:33
	* @throws
	 */
	public Integer findVoucherNumber(FundVoucherBalanceVo dto);

	/**
	* @Title: findVoucherInfo  
	* @Description: 查询员工抵用券信息  
	* @param @param memberId
	* @param @param status
	* @param @return
	* @return List<FundVoucherBalanceVo>
	* @author fbh
	* @date 2019年10月23日  下午5:11:25
	* @throws
	 */
	public List<FundVoucherBalanceVo> findVoucherInfo(FundVoucherBalanceVo dto);

    public List<FundVoucherBalanceVo> findVoucherInfoByMemberId(FundVoucherBalanceVo dto);
	/**
	* @Title: findIntegralDetail  
	* @Description: 查询企业积分发放详情 
	* @param @param dto
	* @param @return
	* @return List<FundBalanceVo>
	* @author fbh
	* @date 2019年10月25日  上午11:25:11
	* @throws
	 */
	public List<FundBalanceVo> findIntegralDetail(QueryCompanyDto dto);
	
	/****
	* @Title: selectOperationDetail  
	* @Description: 查询企业积分操作记录  
	* @param @param dto
	* @param @return
	* @return List<FundTradeFlowVo>
	* @author fbh
	* @date 2019年10月25日  下午6:24:43
	* @throws
	 */
	public List<FundTradeFlowVo> selectOperationDetail(QueryCompanyDto dto);

	/**
	* @Title: findIntegralDcByMemberId  
	* @Description: 企业积分扣减消费查询列表
	* @param @param querydto
	* @param @return
	* @return List<FundBalanceVo>
	* @author fbh
	* @date 2019年10月29日  下午3:54:27
	* @throws
	 */
	public List<FundBalanceVo> findIntegralDcByMemberId(QueryCompanyDto querydto);
	
	/**
	 * 
	 * @Title: fundToActiveVoucherAccount   
	 * @Description: 查询需要激活的员工账户  
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: List<FundBalanceVo>      
	 * @throws
	 */
	public List<FundBalanceVo> fundToActiveVoucherAccount(FundBalanceDto dto);
	
	/**
	 * 
	 * @Title: toActiveVoucherAccount   
	 * @Description: 修改balance激活账户
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: boolean      
	 * @throws
	 */
	public boolean toActiveVoucherAccount(FundAcctDto dto);

	/**
	* @Title: insertIntegralByBatch  
	* @Description: 批量添加企业积分信息 
	* @param @param dto
	* @param @return
	* @return R<Boolean>
	* @author fbh
	* @date 2019年10月30日  下午8:28:31
	* @throws
	 */
	public R<Boolean> insertIntegralByBatch(FundTradeFlowVo dto);
	
	/**
	 * 
	 * @Title: companyVoucherAcctSub   
	 * @Description: 企业抵用券账户扣减
	 * @param: @param dto
	 * balanceCode（编号）
	 * extendAttrd（批次号）
	 * balanceAmount（剩余抵用券金额）
	 * tradeAmount(需要扣的抵用券)
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<Boolean>      
	 * @throws
	 */
	public R<Boolean> companyVoucherAcctSub(List<FundBalanceDto> dtoList);
	
	/**
	 * 
	 * @Title: findWaitLoseVoucher   
	 * @Description: 查询需要失效的数据
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: List<FundBalanceVo>      
	 * @throws
	 */
	public  List<FundBalanceVo> findWaitLoseVoucher(FundBalanceDto dto);
	
	
	/**
	 * 
	 * @Title: addAcctActiveAccount   
	 * @Description: 增加员工抵用券账户的激活数量 
	 * @param: @param dtoList (memberid ,activeCount)
	 * @param: @return    
	 * @author: duqiang     
	 * @return: R<Boolean>      
	 * @throws
	 */
	public R<Boolean> addAcctActiveAccount(List<FundBalanceDto> dtoList);
	
	/**
	 * 
	 * @Title: findThreeWaitLoseVoucher   
	 * @Description: 查询三天内要过期的数据
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: FundBalanceDto      
	 * @throws
	 */
	public  List<FundBalanceVo> findThreeWaitLoseVoucher(FundBalanceDto dto);
}
