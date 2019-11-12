package com.taolue.baoxiao.fund.service.composite;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.TransDetailComposeDto;
import com.taolue.baoxiao.fund.api.vo.NCountCouponVo;
import com.taolue.baoxiao.fund.api.vo.NCountTransVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author baoxiao
 * @since 2018-11-21
 */
public interface ITransDetailComposeService extends IService<TransDetailComposeDto> {

	/**
	 * 
	 * @Title: findPagedCanTransferBalanceGroupAmount   
	 * @Description: 按券id分组查询memberId参数对应的可转让的券资金可用额度总和
	 * @Author: shilei
	 * @date:   Nov 20, 2018 10:35:51 AM  
	 * @param Page<NCountCouponVo> 分页参数 ，其中的参数对象包含
	 *  名称为 memberId 的资金对应的会员编号
	 * @return Page<NCountCouponVo> pagedParams 含有分页信息的数据     
	 * @throws
	 *
	 */
	Page<NCountCouponVo> findPagedCanTransferBalanceGroupAmount(Page<NCountCouponVo> pagedParams);
	
	/**
	 * 
	 * @Title: findBalanceByMemberIdAndCouponId   
	 * @Description: 通过会员，会员类型，券编号，查询该会员下指定券编号的券资金列表
	 * @Author: shilei
	 * @date:   Nov 22, 2018 12:53:46 AM  
	 * @param memberId 会员编号
	 * @param memeberCate 会员类型
	 * @param couponId 券编号
	 * @param companyId 会员当前交易归属公司
	 * @return List<FundBalanceDto> 会员 memberId 在 companyId 公司下的券账户和该账户下 couponId 类型券的资金列表    
	 * @throws
	 *
	 */
//	public List<FundBalanceDto> findBalanceByMemberIdAndCompanyIdAndCouponId(String memberId, String memeberCate, String couponId, 
//			String companyId);
	
	/**
	 * 
	 * @Title: createNcoupons   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   Nov 22, 2018 5:19:34 PM  
	 * @param ncountTransVo
	 * @return List<TransDetailComposeDto>        
	 * @throws
	 *
	 */
	public List<NCountCouponVo> createNcoupons(NCountTransVo ncountTransVo);
	
	/**
	 * 
	 * @Title: ncountBuy   
	 * @Description: 面额券购买接口
	 * @Author: shilei
	 * @date:   Nov 22, 2018 5:19:34 PM  
	 * @param ncountTransVo NCountTransVo 购买参数对象
	 * @return boolean 购买是否成功        
	 * @throws
	 *
	 */
	public boolean ncountBuy(NCountTransVo ncountTransVo);
	
	/**
	 * 
	 * @Title: ncountRecharge   
	 * @Description: 面额券充值接口
	 * @Author: shilei
	 * @date:   Nov 22, 2018 5:19:34 PM  
	 * @param ncountTransVo NCountTransVo 充值参数对象
	 * @return String 充值单编码       
	 * @throws
	 *
	 */
	public String ncountRecharge(NCountTransVo ncountTransVo);
	
	/**
	 * 
	 * @Title: returnNcount   
	 * @Description: 面额券下架接口
	 * @Author: shilei
	 * @date:   Nov 26, 2018 8:28:37 AM  
	 * @param ncountTransVo 下架接口参数
	 * @return boolean 是否下架成功
	 * @throws
	 *
	 */
	public boolean  returnNcount(NCountTransVo ncountTransVo);
	
	
	/**
	 * 
	 * @Title: findTransInfo   
	 * @Description: 分页查询充值列表
	 * @Author: shilei
	 * @date:   Nov 27, 2018 9:22:23 PM  
	 * @param pagedParams Page<TransDetailComposeDto> 分页和查询参数
	 * @return Page<TransDetailComposeDto> 结果列表    
	 * @throws
	 *
	 */
	public Page<TransDetailComposeDto> findTransInfo(Page<TransDetailComposeDto> pagedParams);
	
	/**
	 * 
	 * @Title: findLeftBalanceAmount   
	 * @Description: 查询券余额
	 * @Author: shilei
	 * @date:   Nov 27, 2018 10:09:41 PM  
	 * @param params NCountTransVo 查询参数
	 * @return List<FundBalanceDto> 余额明细     
	 * @throws
	 *
	 */
	public List<NCountCouponVo> findLeftBalanceAmount(@RequestParam(value="params") NCountTransVo params);
	
	/**
	 * 
	 * <p>名称:类ITransDetailComposeService中的selectNoCouponByConditionsPage方法</br>    
	 * <p>描述:(这里用一句话描述这个方法的作用)</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Jan 15, 2019 11:15:45 AM</br>
	 * @throws Exception
	 * @param query {@link Query}{@literal <}{@link FundBalanceDto}{@literal >} 带分页信息和查询参数的查询对象
	 * {@link key=} memberId {@link String} 会员编码 
	 * @return {@link NCountTransVo} 查询结果对象，该对象包含了待充值总额和分页的面额券信息
	 * <ul>
	 * <li>{@link Page}{@literal <}{@link FundBalanceDto}{@literal >} 带分页信息和面额券列表数据
	 * <ul>
	 * 
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
	 * </li>
	 * <li>{@link NCountTransVo#totalAmount} 待充值总额</li>
	 * </ul>
	 * 
	 */
	NCountTransVo selectNoCouponByConditionsPage(Query<FundBalanceDto> query);
}
