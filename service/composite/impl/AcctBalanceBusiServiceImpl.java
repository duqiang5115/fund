/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  AcctBalanceBusiServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 10, 2018 4:20:17 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.google.common.collect.Lists;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.util.Exceptions;
import com.taolue.baoxiao.common.util.Group;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.dto.BalanceSearchParams;
import com.taolue.baoxiao.fund.api.dto.BusinessApplyBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundComposDto;
import com.taolue.baoxiao.fund.api.dto.QueryFundBalanceDto;
import com.taolue.baoxiao.fund.api.openplatform.IDockOpenPlatformService;
import com.taolue.baoxiao.fund.api.vo.FundBalanceVo;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.entity.BusinessApplyBalance;
import com.taolue.baoxiao.fund.entity.BusinessApplyCharges;
import com.taolue.baoxiao.fund.entity.TbFundAcct;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.service.IBusinessApplyBalanceService;
import com.taolue.baoxiao.fund.service.IBusinessApplyChargesService;
import com.taolue.baoxiao.fund.service.ITbFundAcctService;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.IBalanceBusiService;
import com.taolue.baoxiao.fund.service.remote.IOpenPlatformService;

import cn.hutool.core.bean.copier.BeanCopier;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**   
 * @ClassName:  AcctBalanceBusiServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   Dec 10, 2018 4:20:17 PM   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class AcctBalanceBusiServiceImpl implements IAcctBalanceBusiService {

	@Autowired
	private IBalanceBusiService balanceBusiService;
	@Autowired
	private IBusinessApplyBalanceService businessApplyBalanceService;
	@Autowired
	private IBusinessApplyChargesService businessApplyChargesService;
	
	@Autowired
    private ITbFundAcctService fundAcctService;
	
	@Autowired
    private  IOpenPlatformService openPlatformService;
	
    @Autowired
    private IDockOpenPlatformService dockOpenPlatformService;
    
    @Value("${application.release.skip:}")
	private boolean releaseSkip = false;

	public List<FundBalanceDto> findBalancesByParams(BalanceSearchParams params){
		return this.balanceBusiService.selectByConditions(params);
	}
	
	public List<FundBalanceDto> findBalancesByParams(String companyId, String memberId, String memberCate, String acctCate, 
    		String balanceItemNo, String busiModel, String canTicket, String canTransfer, String ownerId, 
    		Date validTime, Date expireTime) {
		
		BalanceSearchParams params = new BalanceSearchParams();
		
		//如果是砾州账户
		if (MemberCateEnums.MEMBER_CATE_PT.getCateCode().equals(memberCate)) {
			memberId = CommonConstant.PLANTFORM_ACCT_MEMBER_ID;
    		busiModel = CommonConstant.BALANCE_BUSI_MODEL_NONE;
    		acctCate = CommonConstant.PLANTFORM_ACCT_CATE;
    		ownerId = CommonConstant.PLANTFORM_ACCT_MEMBER_ID;
    		canTicket = CommonConstant.STATUS_YES;
    		canTransfer = CommonConstant.STATUS_YES;
    		memberCate = MemberCateEnums.MEMBER_CATE_PT.getCateCode();
    		companyId = CommonConstant.PLANTFORM_ACCT_MEMBER_ID;
    		
    		
		} else {
			//后付费类型
			if (!AcctCateEnums.ACCT_CATE_COUPON.getCateCode().equals(acctCate)) {
				if (CommonConstant.BALANCE_ITEM_NO_FULL_YCBZJ.equals(balanceItemNo)) {
					busiModel = BusiModelEnums.BUSI_MODEL_YCCM.getCateCode();
			    //预充值报销
				} else if (CommonConstant.BALANCE_ITEM_NO_FULL_YCGBX.equals(balanceItemNo)) {
					busiModel = BusiModelEnums.BUSI_MODEL_YCPE.getCateCode();
				//预充值保证金
				} else if (CommonConstant.BALANCE_ITEM_NO_FULL_HFBZJ.equals(balanceItemNo)) {
					busiModel = BusiModelEnums.BUSI_MODEL_HFCM.getCateCode();
				//预充值报销
				} else if (CommonConstant.BALANCE_ITEM_NO_FULL_HFGBX.equals(balanceItemNo)) {
					busiModel = BusiModelEnums.BUSI_MODEL_HFPE.getCateCode();
				//其他账户业务模式都为固定值
				} else {
					busiModel = CommonConstant.BALANCE_BUSI_MODEL_NONE;
				}
				canTicket = CommonConstant.STATUS_YES;
			}
		}
		
		if (StrUtil.isNotBlank(companyId)) {
    		params.setCompanyId(companyId);
    	}
    	if (StrUtil.isNotBlank(ownerId)) {
    		params.setOwnerId(ownerId);
    	}
		if (StrUtil.isNotBlank(canTransfer)) {
			params.setCanTransfer(canTransfer);
	    }
		if (StrUtil.isNotBlank(canTicket)) {
			params.setCanTicket(canTicket);
	    }
		if (!ObjectUtils.isEmpty(busiModel)) {
    		params.addbusiModel(busiModel);
    	}
		if (!ObjectUtils.isEmpty(validTime)) {
    		params.setValidTime(validTime);
    	}
		if (!ObjectUtils.isEmpty(expireTime)) {
    		params.setExpireTime(expireTime);
    	}
		params.addBalanceItemCode(balanceItemNo);
		params.addMemberId(memberId);
		
		if (!ObjectUtils.isEmpty(memberCate)) {
			params.addMemberCate(memberCate);
    	}

		params.setAcctCate(acctCate);
		params.setBalanceAmount(null);
		return this.findBalancesByParams(params);
	}
	
	public FundBalanceDto findSignleBalanceByParams(String companyId, String memberId, String memberCate, String acctCate, 
    		String balanceItemNo, String busiModel, String canTicket, String canTransfer, String ownerId, 
    		Date validTime, Date expireTime) {
		
		List<FundBalanceDto> results = this.findBalancesByParams(companyId, memberId, memberCate, acctCate, 
				balanceItemNo, busiModel, canTicket, canTransfer, ownerId, validTime, expireTime);
		if (CollUtil.isEmpty(results) || results.get(0) == null) {
			if (!AcctCateEnums.ACCT_CATE_COUPON.getCateCode().equals(acctCate)
					   && !AcctCateEnums.ACCT_CATE_DIRECT_COUPON.getCateCode().equals(acctCate)) {
					throw new BaoxiaoException(903,"["+memberId+"]的["+memberCate
					+"]类型账户的["+balanceItemNo+"]资金记录尚未创建！！请检查！");
					
				//，否则有可能券id相关的资金记录未创建，此时需要对其新建资金记录
				} else {
					TbFundAcct fundAcct = this.findFundAcct(memberId, acctCate);
					if (null == fundAcct) {
						FundServiceExceptionGenerator.FundServiceException("90001", "", memberId, acctCate);
					}
					FundBalanceDto fundBalanceDto = new FundBalanceDto(fundAcct.getAcctInstNo(), null, balanceItemNo, busiModel, 
							null, null, null, canTicket, canTransfer, validTime, expireTime, companyId, ownerId, null);
					fundBalanceDto.setExtendAttre(memberCate);
					return fundBalanceDto;
				}
			//
		}
		if (results.size()>1) {
			FundServiceExceptionGenerator.FundServiceException("90002", "");
		}
		return results.get(0);
	}
	
	public FundBalanceDto findSignleBalanceByCode(String balanceCode) {
		return this.balanceBusiService.selectOneByBalanceCode(balanceCode, CommonConstant.NO_LOCKE_SECOND);
	}
	
	public FundBalanceDto findSignleBalanceByBalanceItemCode(String balanceItemCode) {
		return this.balanceBusiService.selectOneByBalanceItemCode(balanceItemCode, CommonConstant.NO_LOCKE_SECOND);
	}
	
	@Override
	public FundBalanceDto findSignleBalanceByAcctInstNo(String acctInstNo, String balanceItemNo) {
		BalanceSearchParams params = new BalanceSearchParams();
		params.setAcctInstNo(acctInstNo);
		params.addBalanceItemCode(balanceItemNo);
		params.setBalanceAmount(null);
		List<FundBalanceDto> results = this.findBalancesByParams(params);
		
		if (CollUtil.isEmpty(results)) {
			FundServiceExceptionGenerator.FundServiceException("90001", "");
		}
		if (results.size()>1) {
			FundServiceExceptionGenerator.FundServiceException("90002", "");
		}
		return results.get(0);
    }
	
	public BigDecimal selectTotalNoCoupon(BalanceSearchParams params) {
		return this.balanceBusiService.selectTotalNoCoupon(params);
	}
	
	public Page<FundBalanceDto> selectNoCouponByConditionsPage(BalanceSearchParams params, Query<FundBalanceDto> query) {
		return this.balanceBusiService.selectNoCouponByConditionsPage(params, query);
	}
	
	/**   
	 * <p>Title: findGroupedBalances</p>   
	 * <p>Description: </p>   
	 * @param memberId
	 * @param busiModel
	 * @param companyId
	 * @param canTransfer
	 * @param balanceItemCodes
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService#findGroupedBalances(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List)   
	 */  
//	@Override
//	public List<FundBalanceDto> findGroupedCouponBalances(List<String> memberIds, List<String> busiModels, String companyId,
//			String acctCate, String canTransfer, List<Group> groups, List<String> balanceItemCodes) {
//		BalanceSearchParams params = new BalanceSearchParams();
//		if (CollUtil.isEmpty(groups)) {
//			params.setGroups(CommonConstant.DEFAULT_BALANCE_GROUP_TWOCOLUMNS);
//		} else {
//			params.setGroups(groups);
//		}
//		if (StrUtil.isEmpty(acctCate)) {
//			params.addMemberCate(AcctCateEnums.ACCT_CATE_COUPON.getCateCode());
//		} else {
//			params.addMemberCate(acctCate);
//		}
//		
//		params.setMemberIds(memberIds);
//		if (CollUtil.isEmpty(busiModels) || StrUtil.isEmpty(busiModels.get(0))) {
//			params.setBusiModels(null);
//		} else {
//			params.setBusiModels(busiModels);
//		}
//		params.setCompanyId(companyId);
//		params.setCanTransfer(canTransfer);
//		params.setBalanceItemCodes(balanceItemCodes);
//		return this.balanceBusiService.selectByConditions(params);
//	}
	
	/**
	 * 
	 * <p>Title: findGroupedCouponBalances</p>   
	 * <p>Description: </p>   
	 * @param params
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService#findGroupedCouponBalances(com.taolue.baoxiao.fund.api.dto.BalanceSearchParams)
	 */
	@Override
	public List<FundBalanceDto> findGroupedCouponBalances(BalanceSearchParams params) {
		
		if (null != params.getGroups()) {
			if (CollUtil.isEmpty(params.getGroups()) ||
					ObjectUtil.isNull(params.getGroups().get(0))) {
				params.setGroups(CommonConstant.DEFAULT_BALANCE_GROUP_TWOCOLUMNS);
			} 
		}
		if (StrUtil.isEmpty(params.getAcctCate())) {
			params.setAcctCate(AcctCateEnums.ACCT_CATE_COUPON.getCateCode());
		} 
		
		if (CollUtil.isEmpty(params.getBusiModels()) 
				|| StrUtil.isEmpty(params.getBusiModels().get(0))) {
			params.setBusiModels(null);
		}
		
		if (StrUtil.isEmpty(params.getCompanyId())) {
			params.setCompanyId(null);
		}
		
		if (StrUtil.isEmpty(params.getCanTransfer())) {
			params.setCanTransfer(null);
		}
		
		if (CollUtil.isEmpty(params.getBalanceItemCodes()) 
				|| StrUtil.isEmpty(params.getBalanceItemCodes().get(0))) {
			params.setBalanceItemCodes(null);
		}
		
		return this.balanceBusiService.selectByConditions(params);
	}

	/**   
	 * <p>Title: findNoCouponBalance</p>   
	 * <p>Description: </p>   
	 * @param memberId
	 * @param fundAcctCate
	 * @param balanceItemNo
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService#findNoCouponBalance(java.lang.String, java.lang.String, java.lang.String)   
	 */  
	@Override
	public FundBalanceDto findNoCouponBalance(String memberId, String balanceItemNo) {
		BalanceSearchParams params = new BalanceSearchParams();
		params.setGroups(null);
		params.addMemberId(memberId);
		//params.setAcctCate(fundAcctCate);
		params.setBalanceAmount(null);
		List<String> balanceItemCodes = Lists.newArrayList();
		balanceItemCodes.add(balanceItemNo);
		params.setBalanceItemCodes(balanceItemCodes);
		List<FundBalanceDto> result = this.balanceBusiService.selectByConditions(params);
		if (CollUtil.isNotEmpty(result)) {
			return result.get(0);
		}
		return null;
	}

	/**   
	 * <p>Title: findPerOrAftPaymentBalance</p>   
	 * <p>Description: </p>   
	 * @param memberId
	 * @param busiModel
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService#findPerOrAftPaymentBalance(java.lang.String, java.lang.String)   
	 */  
	@Override
	public FundBalanceDto findPerOrAftPaymentBalance(String memberId, String... busiModels) {
		
		String busiModel = ArrayUtil.firstNonNull(busiModels);
		if (StrUtil.isBlank(busiModel)) {
			busiModel = BusiModelEnums.BUSI_MODEL_YCCM.getCateCode();
		}
		
		//默认账户类型为后付费额度账户
		String fundAcctCate = 
				//MemberCateEnums.MEMBER_CATE_CMP.getCateCode()+
				AcctCateEnums.ACCT_CATE_AFTGM.getCateCode();
		
		BigDecimal currentAmount = null;
		
		//如果需要查询预充值企业或者预充值报销账户
		if (BusiModelEnums.BUSI_MODEL_YCCM.getCateCode().equals(busiModel)
				|| BusiModelEnums.BUSI_MODEL_YCPE.getCateCode().equals(busiModel)) {
			
			//设置账户类型为预充值现金账户
			fundAcctCate = 
					//MemberCateEnums.MEMBER_CATE_CMP.getCateCode()+
					AcctCateEnums.ACCT_CATE_PERGM.getCateCode();
			
			//查询当前总部现金账户
			if (this.releaseSkip) {
				currentAmount = new BigDecimal(1000000000);
			} else {
				try {
					currentAmount = this.openPlatformService.findCompanyRecharge(memberId);
				} catch (Exception e) {
					log.warn("调用总部获取现金账户余额出现错误，错误原因{},设置余额为NULL", Exceptions.getStackTraceAsString(e));
				}
			}
		}
		//指定账户编号和账户类型查询资金记录
		FundBalanceDto balanceDto = this.findNoCouponBalance(memberId, fundAcctCate);
		if (null == balanceDto) {
			FundServiceExceptionGenerator.FundServiceException("9082", "", memberId, busiModel, fundAcctCate);
		}
		
		if ((//MemberCateEnums.MEMBER_CATE_CMP.getCateCode()+
				AcctCateEnums.ACCT_CATE_PERGM.getCateCode()).equals(fundAcctCate)) {
			if (null != currentAmount) {
				balanceDto.setBalanceAmount(currentAmount);
				balanceDto.setAuthBalance(currentAmount);
				balanceDto.setDelFlag(CommonConstant.STATUS_NORMAL);
				balanceDto.setStatus(CommonConstant.STATUS_NORMAL);
				BeanCopier<TbFundBalance> copier = new BeanCopier<TbFundBalance>(balanceDto, 
						new TbFundBalance(), new CopyOptions());
				copier.copy().insertOrUpdate();
			}
		}
		return balanceDto;
	}
	
	public FundComposDto findCompanyBlanceByCate(String memberId, String balanceCate) {
		FundComposDto composDto = null;
		
		if (CommonConstant.BALANCE_ITEM_NO_SUFFX_BZJ.equals(balanceCate)) {
			
			FundBalanceDto balanceYczDto = 
					this.findNoCouponBalance(memberId, CommonConstant.BALANCE_ITEM_NO_FULL_YCBZJ);
			if (null == balanceYczDto || StrUtil.isBlank(balanceYczDto.getBalanceCode())) {
				FundServiceExceptionGenerator.FundServiceException("9082",memberId,
						BusiModelEnums.BUSI_MODEL_YCCM.getCateCode(),
						CommonConstant.BALANCE_ITEM_NO_FULL_YCBZJ);
			}
			
			
			FundBalanceDto balanceHffDto = 
					this.findNoCouponBalance(memberId, CommonConstant.BALANCE_ITEM_NO_FULL_HFBZJ);
			
			if (null == balanceHffDto || StrUtil.isBlank(balanceHffDto.getBalanceCode())) {
				FundServiceExceptionGenerator.FundServiceException("9082",memberId,
						BusiModelEnums.BUSI_MODEL_HFCM.getCateCode(),CommonConstant.BALANCE_ITEM_NO_FULL_HFBZJ);
			}
			
			composDto = new FundComposDto();
			composDto.setBalanceYczDto(balanceYczDto);
			composDto.setBalanceHffDto(balanceHffDto);
			composDto.setTotalAmount(balanceYczDto.getBalanceAmount().add(balanceHffDto.getBalanceAmount()));
		}
		
		if (CommonConstant.BALANCE_ITEM_NO_SUFFX_GBX.equals(balanceCate)) {
			FundBalanceDto balanceYczDto = 
					this.findNoCouponBalance(memberId, CommonConstant.BALANCE_ITEM_NO_FULL_YCGBX);
			if (null == balanceYczDto || StrUtil.isBlank(balanceYczDto.getBalanceCode())) {
				FundServiceExceptionGenerator.FundServiceException("9082",memberId,
						BusiModelEnums.BUSI_MODEL_YCPE.getCateCode(),CommonConstant.BALANCE_ITEM_NO_FULL_YCGBX);
			}
			
			FundBalanceDto balanceHffDto = 
					this.findNoCouponBalance(memberId, CommonConstant.BALANCE_ITEM_NO_FULL_HFGBX);
			
			if (null == balanceYczDto || StrUtil.isBlank(balanceYczDto.getBalanceCode())) {
				FundServiceExceptionGenerator.FundServiceException("9082",memberId,
						BusiModelEnums.BUSI_MODEL_HFPE.getCateCode(),CommonConstant.BALANCE_ITEM_NO_FULL_HFGBX);
			}
			
			composDto = new FundComposDto();
			composDto.setBalanceYczDto(balanceYczDto);
			composDto.setBalanceHffDto(balanceHffDto);
			composDto.setTotalAmount(balanceYczDto.getBalanceAmount().add(balanceHffDto.getBalanceAmount()));
		}
	
		return composDto;
	}

	/**   
	 * <p>Title: selectDepartmentCouponGroupMemberInfo</p>   
	 * <p>Description: </p>   
	 * @param page
	 * @param memberIds
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService#selectDepartmentCouponGroupMemberInfo(com.taolue.baoxiao.common.util.Query, java.util.List)   
	 */  
	@Override
	public Query<FundBalanceDto> selectDepartmentCouponGroupMemberInfo(Query<FundBalanceDto> page,
			List<String> memberIds) {
		List<Group> groups = Arrays.asList(new Group[] {new Group("a", "member_id"),new Group("b", "acct_inst_no")});
		BalanceSearchParams params = new BalanceSearchParams(CommonConstant.STRING_BLANK, 
				CommonConstant.NO_LOCKE_SECOND, groups);
		params.setAcctCate(AcctCateEnums.ACCT_CATE_COUPON.getCateCode());
		params.addMemberCate(MemberCateEnums.MEMBER_CATE_ORG.getCateCode());
		params.setMemberIds(memberIds);
		return this.balanceBusiService.selectByConditionsPage(params, page);
	}
	
	@Override
	public List<FundBalanceDto> findBalanceWithFlowParams(QueryFundBalanceDto query) {
		BalanceSearchParams params = new BalanceSearchParams();
		params.setJoinFlow(true);
		params.setTradeCate(query.getTradeCate());
		params.setFlowBeginTime(query.getBeginTime());
		params.setFlowEndTime(query.getEndTime());
		params.setRequireAmount(new BigDecimal("-1"));
		params.addMemberId(query.getMemberId());
		return this.balanceBusiService.selectByConditions(params);
	}
	
	public List<FundBalanceDto> findBalanceGroupWithFlowParams(String tradeBusiCode) {
		BalanceSearchParams params = new BalanceSearchParams();
		params.setJoinFlow(true);
		params.setTradeBusiCode(tradeBusiCode);
		params.addMemberCate(MemberCateEnums.MEMBER_CATE_ALO.getCateCode(),
				MemberCateEnums.MEMBER_CATE_EMP.getCateCode());
		params.setAcctCate(AcctCateEnums.ACCT_CATE_COUPON.getCateCode());
		params.setTradeCate(TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode());
		params.setGroups(CommonConstant.DEFAULT_BALANCE_GROUP_WITHFLOW);
		params.setBalanceAmount(null);
		return this.balanceBusiService.selectByConditions(params);
	}
	
	
//	@Override
//	public TbFundBalance findFundBalance(String acctInstNo, String balanceItemNo) {
//		
//    	EntityWrapper<TbFundBalance> acctWrapper = new EntityWrapper<>();
//    	acctWrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
//    	acctWrapper.eq("acct_inst_no", acctInstNo);
//    	acctWrapper.eq("balance_item_code", balanceItemNo);
//    	
//    	TbFundBalance fundBalance = this.fundBalanceService.selectOne(acctWrapper);
//    	
//    	if (null == fundBalance) {
//    		fundBalance = new TbFundBalance();
//    	}
//    	return fundBalance;
//    }
	
	@Override
	public List<TbFundAcct> findFundAccts(String memberId, String fundAcctCate) {
    	EntityWrapper<TbFundAcct> wrapper = new EntityWrapper<>();
    	
    	boolean isCompany = false;
    	
    	//wrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
    	wrapper.eq("member_id", memberId);
    	wrapper.eq("acct_cate", fundAcctCate);
    	
    	List<TbFundAcct> fundAccts = this.fundAcctService.selectList(wrapper);
    	if (isCompany && CollUtil.isNotEmpty(fundAccts) 
    			&& fundAccts.size()>1) {
    		FundServiceExceptionGenerator.FundServiceException("9001", 
    				new Object[] {"", memberId, fundAcctCate});
    	}
    	if (CollUtil.isNotEmpty(fundAccts)) {
    		return fundAccts;
    	}
    	return null;
    }
	
	@Override
	public TbFundAcct findFundAcct(String memberId, String fundAcctCate) {
		
		List<TbFundAcct> fundAccts = this.findFundAccts(memberId, fundAcctCate);
		if (CollUtil.isNotEmpty(fundAccts)) {
			return fundAccts.get(0);
		}
		return null;
    }
	
	@Override
	public TbFundAcct findAcctByAcctInstNo(String acctInstNo) {
    	EntityWrapper<TbFundAcct> acctWrapper = new EntityWrapper<>();
    	acctWrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
    	acctWrapper.eq("acct_inst_no", acctInstNo);
    	TbFundAcct fundAcct = this.fundAcctService.selectOne(acctWrapper);
    	if (null != fundAcct) {
    		return fundAcct;
    	}
    	return null;
    }
	
	@Override
	@Transactional
	public boolean accountAmount(String memberId, String amount) {

		FundBalanceDto fundBalance = this.findSignleBalanceByParams(memberId, memberId, 
							MemberCateEnums.MEMBER_CATE_CMP.getCateCode(), 
							AcctCateEnums.ACCT_CATE_AFTGM.getCateCode(), 
							AcctCateEnums.ACCT_CATE_AFTGM.getCateCode(), 
							BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
							CommonConstant.STATUS_YES, CommonConstant.STATUS_YES, memberId, null, null);
		
		BigDecimal currentAmount = CommonConstant.NO_AMOUNT;
		currentAmount = currentAmount.add(new BigDecimal(amount));
		
		BigDecimal oriAuthAmount = fundBalance.getAuthBalance();
		BigDecimal oriAvailAmount = fundBalance.getBalanceAmount();
		
		BigDecimal subAmount = oriAuthAmount.subtract(currentAmount);
		BigDecimal currAvailAmount = oriAvailAmount.subtract(subAmount);
		
		fundBalance.setAuthBalance(currentAmount);
		fundBalance.setBalanceAmount(currAvailAmount);
		
		BeanCopier<TbFundBalance> fundBalanceCopier = new BeanCopier<TbFundBalance>(fundBalance, 
				new TbFundBalance(), new CopyOptions());
		return fundBalanceCopier.copy().updateById();
//		return this..updateById(fundBalanceCopier.copy());
	}
	
	public FundBalanceDto findBalanceWithFlowSumAmount(String memberId, String tradeBusiCate, String tradeCate, boolean isCompany) {
		BalanceSearchParams params = new BalanceSearchParams();
		params.setJoinFlow(true);
		params.setSumAmount(true);
		if (isCompany) {
			params.setCompanyId(memberId);
			params.addMemberCate(MemberCateEnums.MEMBER_CATE_ALO.getCateCode(),
					MemberCateEnums.MEMBER_CATE_EMP.getCateCode(), 
					MemberCateEnums.MEMBER_CATE_ORG.getCateCode());
		} else {
			params.addMemberId(memberId);
			params.addMemberCate(MemberCateEnums.MEMBER_CATE_CMP.getCateCode());
//			params.setTradeCate(TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode());
		}
		params.setTradeBusiCate(tradeBusiCate);
		params.setTradeCate(tradeCate);
		params.setAcctCate(AcctCateEnums.ACCT_CATE_COUPON.getCateCode());
		params.setGroups(null);
		params.setBalanceAmount(null);
		List<FundBalanceDto> results = this.balanceBusiService.selectByConditions(params);
		if (CollUtil.isNotEmpty(results) && ObjectUtil.isNotNull(results.get(0))) {
			return results.get(0);
		}
		return null;
	}

//	@Override
//	public BigDecimal getAccountAmountByMemberId(String companyId, String memberId) {
//		R<UserVo> userVoR = this.dockOpenPlatformService.accountGetById(memberId, CommonConstant.STRING_BLANK);
//		log.info("查询现金账户可用额度返回蚕食:{}",JSON.toJSON(userVoR));
//		if (userVoR.getCode() != 0) {
//			throw new BaoxiaoException(904, userVoR.getMsg());
//		}
//		return userVoR.getData().getSalary_balance();
//	}
	/***
	 * 
	 * <p>Title: repairBalance</p>   
	 * <p>Description: 修复每日限额的问题</p>   
	 * @param dto
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService#repairBalance(com.taolue.baoxiao.fund.api.dto.BusinessApplyBalanceDto)
	 */
	@Override
	@Transactional
	 public R<Boolean> repairBalance( BusinessApplyBalanceDto dto){
		Boolean r=false;
		EntityWrapper<BusinessApplyBalance> balanceWrapper = new EntityWrapper<>();
		balanceWrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
		balanceWrapper.eq("code",dto.getCode() );
		balanceWrapper.eq("apply_code", dto.getApplyCode());
		log.info("查询失败订单信息入参参数{}"+JSON.toJSONString(balanceWrapper));
		BusinessApplyBalance businessApplyBalanceDto=businessApplyBalanceService.selectOne(balanceWrapper);
		log.info("查询失败订单信息结果{}"+JSON.toJSONString(businessApplyBalanceDto));
		BusinessApplyBalance balanceentity=new BusinessApplyBalance();
		balanceentity.setId(businessApplyBalanceDto.getId());
		balanceentity.setStatus(BusinessApplyStatus.PAUSE.getCateCode());
		balanceentity.setRemark(null);
		log.info("修改失败订单信息入参参数{}"+JSON.toJSONString(balanceentity));
		Boolean balanceBoolean=businessApplyBalanceService.updateById(balanceentity);
		EntityWrapper<BusinessApplyCharges> chargsWrapper = new EntityWrapper<>();
		chargsWrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
		chargsWrapper.eq("code",businessApplyBalanceDto.getApplyChargeCode() );
		log.info("查询信息入参参数{}"+JSON.toJSONString(chargsWrapper));
		BusinessApplyCharges businessApplyChargesDto=businessApplyChargesService.selectOne(chargsWrapper);
		log.info("查询信息结果{}"+JSON.toJSONString(businessApplyChargesDto));
		BusinessApplyCharges businessApplyCharges=new BusinessApplyCharges();
		businessApplyCharges.setStatus(BusinessApplyStatus.START.getCateCode());
		businessApplyCharges.setId(businessApplyChargesDto.getId());
		Boolean chargeBoolean=businessApplyChargesService.updateById(businessApplyCharges);
		if(chargeBoolean.equals(true)&& balanceBoolean.equals(true)) {
			log.info("修改结果"+chargeBoolean+balanceBoolean);
			r=true;
		}else {
			r=false;
		}
		 return new R<>(r);
	 }

	@Override
	public List<FundBalanceVo> findMemberCouponBalance(FundBalanceVo dto) {
		return this.balanceBusiService.findMemberCouponBalance(dto);
	};
	
	@Override
	public List<FundBalanceVo> findMemberCouponBalanceExport(FundBalanceVo dto) {
		return this.balanceBusiService.findMemberCouponBalanceExport(dto);
	};
	
	@Override
	public List<FundBalanceVo> findMemberCouponBalanceDetail(FundBalanceVo dto) {
		return this.balanceBusiService.findMemberCouponBalanceDetail(dto);
	};
	
}
