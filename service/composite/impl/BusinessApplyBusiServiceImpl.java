/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  BusinessApplyBusiServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Dec 9, 2018 7:25:23 PM   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyRoleType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.util.SequenceNumber;
import com.taolue.baoxiao.fund.api.dto.BusinessApplyBusiDto;
import com.taolue.baoxiao.fund.api.vo.OrderVo;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.common.model.OrderApply;
import com.taolue.baoxiao.fund.entity.BusinessApplyBalance;
import com.taolue.baoxiao.fund.entity.BusinessApplyCharges;
import com.taolue.baoxiao.fund.entity.BusinessApplyParty;
import com.taolue.baoxiao.fund.entity.Order;
import com.taolue.baoxiao.fund.mapper.BusinessApplyBusiMapper;
import com.taolue.baoxiao.fund.service.IBusinessApplyBalanceService;
import com.taolue.baoxiao.fund.service.IBusinessApplyChargesService;
import com.taolue.baoxiao.fund.service.IBusinessApplyPartyService;
import com.taolue.baoxiao.fund.service.IBusinessApplyProdService;
import com.taolue.baoxiao.fund.service.IOrderService;
import com.taolue.baoxiao.fund.service.composite.IBusinessApplyBusiService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * <p>ClassName:  BusinessApplyBusiServiceImpl </br>  
 * <p>Description:TODO(这里用一句话描述这个类的作用)</br>   
 * <p>Author: shilei</br>
 * <p>date:   Mar 8, 2019 10:33:55 PM </br>  
 *     
 * @Copyright: 2019 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class BusinessApplyBusiServiceImpl extends ServiceImpl<BusinessApplyBusiMapper, BusinessApplyBusiDto> 
			implements IBusinessApplyBusiService {

	private boolean isDebug = log.isDebugEnabled();
	
	@Autowired
	private IOrderService orderService;
	
	@Autowired
	private IBusinessApplyPartyService businessApplyPartyService;
	
	@Autowired
	private IBusinessApplyChargesService businessApplyChargesService;
	
	@Autowired
	private IBusinessApplyBalanceService businessApplyBalanceService;
	
	@Autowired
	private IBusinessApplyProdService businessApplyProdService;

	/**  
	 * <p>Title:getOrderService</p><BR>  
	 * <p>Description:获取属性orderService的值<BR>  
	 * @return IOrderService <BR>  
	 */
	@Override
	public IOrderService getOrderService() {
		return orderService;
	}
	
	/**  
	 * <p>Title:getBusinessApplyPartyService</p><BR>  
	 * <p>Description:获取属性businessApplyPartyService的值<BR>  
	 * @return IBusinessApplyPartyService <BR>  
	 */
	@Override
	public IBusinessApplyPartyService getBusinessApplyPartyService() {
		return businessApplyPartyService;
	}

	/**  
	 * <p>Title:getBusinessApplyChargesService</p><BR>  
	 * <p>Description:获取属性businessApplyChargesService的值<BR>  
	 * @return IBusinessApplyChargesService <BR>  
	 */
	@Override
	public IBusinessApplyChargesService getBusinessApplyChargesService() {
		return businessApplyChargesService;
	}

	/**  
	 * <p>Title:getBusinessApplyBalanceService</p><BR>  
	 * <p>Description:获取属性businessApplyBalanceService的值<BR>  
	 * @return IBusinessApplyBalanceService <BR>  
	 */
	@Override
	public IBusinessApplyBalanceService getBusinessApplyBalanceService() {
		return businessApplyBalanceService;
	}

	/**  
	 * <p>Title:getBusinessApplyProdService</p><BR>  
	 * <p>Description:获取属性businessApplyProdService的值<BR>  
	 * @return IBusinessApplyProdService <BR>  
	 */
	@Override
	public IBusinessApplyProdService getBusinessApplyProdService() {
		return businessApplyProdService;
	}
	
	public Order createApplyMain(String orderNoPefix, String orderCode, OrderType mainType, OrderType subType, 
			BusinessApplyStatus status, String companyId, String sourceCode) {
		Order applyMain = new Order();
		applyMain.setOrderNo(new SequenceNumber(IdWorker.getId()).getLiteBuinessCode(orderNoPefix));
		applyMain.setOrderCode(orderCode);
		applyMain.setMainType(mainType.getCateCode());
		applyMain.setSubType(subType.getCateCode());
		applyMain.setMainTypeName(mainType.getCateName());
		applyMain.setSubTypeName(subType.getCateName());
		
		if (ObjectUtil.isNull(status)) {
			status = BusinessApplyStatus.CREATED;
		}
		applyMain.setStatus(status.getCateCode());
		
		if (StrUtil.isBlank(companyId)) {
			companyId = CommonConstant.DEFAULT_ACCT_MEMBER_ID;
		}
		applyMain.setCompanyId(companyId);
		
		applyMain.setCompanyName(CommonConstant.STRING_BLANK);
		applyMain.setSourceCode(sourceCode);
		return applyMain;
	}
	
	public BusinessApplyParty createApplyParty(String applyPartyCodePerfix, String applyMainCode, 
			MemberCateEnums roleCode, String partyCode, BusinessApplyStatus status, String euid) {
		BusinessApplyParty partyEntry = new BusinessApplyParty();
		partyEntry.setApplyCode(applyMainCode);
		partyEntry.setCode(new SequenceNumber(IdWorker.getId()).getLiteBuinessCode(applyPartyCodePerfix));
		partyEntry.setPartyCode(partyCode);
		partyEntry.setRoleCode(roleCode.getCateCode());
		
		if (StrUtil.isBlank(euid)) {
			euid = CommonConstant.DEFAULT_ACCT_MEMBER_ID;
		}
		partyEntry.setPartyGuid(euid);
		
		if (ObjectUtil.isNull(status)) {
			status = BusinessApplyStatus.CREATED;
		}
		partyEntry.setStatus(status.getCateCode());
	
		return partyEntry;
	}
	
	public BusinessApplyParty createApplyParty(String applyPartyCodePerfix, String applyMainCode, 
			ActionType roleCode, String partyCode, BusinessApplyStatus status, String euid) {
		BusinessApplyParty partyEntry = new BusinessApplyParty();
		partyEntry.setApplyCode(applyMainCode);
		partyEntry.setCode(new SequenceNumber(IdWorker.getId()).getLiteBuinessCode(applyPartyCodePerfix));
		partyEntry.setPartyCode(partyCode);
		partyEntry.setRoleCode(roleCode.getCateCode());
		
		if (StrUtil.isBlank(euid)) {
			euid = CommonConstant.DEFAULT_ACCT_MEMBER_ID;
		}
		partyEntry.setPartyGuid(euid);
		
		if (ObjectUtil.isNull(status)) {
			status = BusinessApplyStatus.CREATED;
		}
		partyEntry.setStatus(status.getCateCode());
	
		return partyEntry;
	}
	
	public BusinessApplyParty createApplyParty(String applyPartyCodePerfix, String applyMainCode, 
			BusinessApplyRoleType roleCode, String partyCode, BusinessApplyStatus status, String euid) {
		BusinessApplyParty partyEntry = new BusinessApplyParty();
		partyEntry.setApplyCode(applyMainCode);
		partyEntry.setCode(new SequenceNumber(IdWorker.getId()).getLiteBuinessCode(applyPartyCodePerfix));
		partyEntry.setPartyCode(partyCode);
		partyEntry.setRoleCode(roleCode.getCateCode());
		
		if (StrUtil.isBlank(euid)) {
			euid = CommonConstant.DEFAULT_ACCT_MEMBER_ID;
		}
		partyEntry.setPartyGuid(euid);
		
		if (ObjectUtil.isNull(status)) {
			status = BusinessApplyStatus.CREATED;
		}
		partyEntry.setStatus(status.getCateCode());
	
		return partyEntry;
	}
	
	public BusinessApplyCharges createApplyCharge(String applyChargeCodePerfix, String applyMainCode, 
			BillItemSubCate chargeType, AcctCateEnums chargeItemType, String chargeItemCode, 
			BigDecimal amount, BusinessApplyStatus status, String relateCode, String chargeSubject) {
		BusinessApplyCharges applyCharge = new BusinessApplyCharges();
		applyCharge.setApplyCode(applyMainCode);
		applyCharge.setCode(new SequenceNumber(IdWorker.getId()).getLiteBuinessCode(applyChargeCodePerfix));
		applyCharge.setChargesType(chargeType.getCateCode());
		applyCharge.setChargesItemType(chargeItemType.getCateCode());
		applyCharge.setChargesItemCode(chargeItemCode);
		applyCharge.setRelateCode(relateCode);
		if (ObjectUtil.isNull(amount)) {
			amount = CommonConstant.NO_AMOUNT;
		}
		applyCharge.setAmount(amount);
		
		if (ObjectUtil.isNull(status)) {
			status = BusinessApplyStatus.CREATED;
		}
		applyCharge.setStatus(status.getCateCode());
		
		return applyCharge;
	}

	public BusinessApplyBalance createApplyBalance(String applyBalanceCodePerfix, String applyMainCode, 
			String applyChargeCode, String applyPartyCode, BusinessApplyRoleType roleCode, 
			String balanceCode, BusinessApplyStatus status, BigDecimal tradeAmount, String relateCode, 
			BigDecimal mustAmount, BigDecimal payAmout) {
		BusinessApplyBalance applyBalance = new BusinessApplyBalance();
		applyBalance.setApplyCode(applyMainCode);
		applyBalance.setCode(new SequenceNumber(IdWorker.getId()).getLiteBuinessCode(applyBalanceCodePerfix));
		applyBalance.setApplyChargeCode(applyChargeCode);
		applyBalance.setApplyPartyCode(applyPartyCode);
		applyBalance.setBalanceCode(balanceCode);
		if (ObjectUtil.isNotNull(roleCode)) {
			applyBalance.setRoleType(roleCode.getCateCode());
		}
		applyBalance.setRelateCode(relateCode);
		if (ObjectUtil.isNull(tradeAmount)) {
			tradeAmount = CommonConstant.NO_AMOUNT;
		}
		applyBalance.setAmount(tradeAmount);
		
		if (ObjectUtil.isNull(status)) {
			status = BusinessApplyStatus.CREATED;
		}
		applyBalance.setStatus(status.getCateCode());
		
		return applyBalance;
	}
	
	public BusinessApplyBalance createApplyBalance(String applyBalanceCodePerfix, String applyMainCode, 
			String applyChargeCode, String applyPartyCode, BillItemSubCate roleCode, 
			String balanceCode, BusinessApplyStatus status, BigDecimal tradeAmount, String relateCode, 
			BigDecimal mustAmount, BigDecimal payAmout) {
		BusinessApplyBalance applyBalance = new BusinessApplyBalance();
		applyBalance.setApplyCode(applyMainCode);
		applyBalance.setCode(new SequenceNumber(IdWorker.getId()).getLiteBuinessCode(applyBalanceCodePerfix));
		applyBalance.setApplyChargeCode(applyChargeCode);
		applyBalance.setApplyPartyCode(applyPartyCode);
		applyBalance.setBalanceCode(balanceCode);
		if (ObjectUtil.isNotNull(roleCode)) {
			applyBalance.setRoleType(roleCode.getCateCode());
		}
		applyBalance.setRelateCode(relateCode);
		if (ObjectUtil.isNull(tradeAmount)) {
			tradeAmount = CommonConstant.NO_AMOUNT;
		}
		applyBalance.setAmount(tradeAmount);
		
		if (ObjectUtil.isNull(status)) {
			status = BusinessApplyStatus.CREATED;
		}
		applyBalance.setStatus(status.getCateCode());
		
		return applyBalance;
	}
	
	@Transactional(rollbackFor=Exception.class)
	public void createSoldProxyOrder(Order order, 
			List<BusinessApplyParty> applyParts,
			List<BusinessApplyCharges> applyCharges,
			List<BusinessApplyBalance> applyBalances
			) {
		if (order != null) {
			this.getOrderService().insertOrUpdate(order);
		}
		if (CollUtil.isNotEmpty(applyParts)) {
			this.getBusinessApplyPartyService().insertBatch(applyParts);
		}
		if (CollUtil.isNotEmpty(applyCharges)) {
			this.getBusinessApplyChargesService().insertBatch(applyCharges);
		}
		if (CollUtil.isNotEmpty(applyBalances)) {
			this.getBusinessApplyBalanceService().insertBatch(applyBalances);
		}
	}
	
	public boolean getExsitApplyMain(String mainType, String subType, String memberId, String status, 
			Date beginTime, Date endTime) {
		
		if (StrUtil.isNotBlank(memberId)) {
			BusinessApplyBusiDto applyDto = this.baseMapper.findHadProcessSoldproxy(memberId);
			if (ObjectUtil.isNotNull(applyDto) 
					&& applyDto.getOrderAmount().compareTo(CommonConstant.NO_AMOUNT)>0) {
				return true;
			} else {
				return false;
			}
		} 
	

		int count = 0;//暂时不拦截有未完成代码批次，只过滤代卖中的人
		return count>0 ? true : false;
	}
	
	public boolean getExsitApplyCharge(String applyCode, String status) {
		BusinessApplyCharges charges = new BusinessApplyCharges();
		charges.setApplyCode(applyCode);
		EntityWrapper<BusinessApplyCharges> wrapperCharge = new EntityWrapper<>(charges);
		wrapperCharge.ne("status", status);
		int count = this.getBusinessApplyChargesService().selectCount(wrapperCharge);
		return count>0 ? false : true;
	}
	
	public Order getApplyMainByCode(String applyCode) {
		Order applyMain = new Order();
		applyMain.setOrderNo(applyCode);
		EntityWrapper<Order> wrapperOrder = new EntityWrapper<>(applyMain);
		applyMain = this.getOrderService().selectOne(wrapperOrder);
		if (ObjectUtil.isNull(applyMain)) {
			FundServiceExceptionGenerator.FundServiceException(503, "无法查询到编码为{"+applyCode+"}的单据信息");
		}
		return applyMain;
	}
	
	public BusinessApplyParty getApplyPartyByCode(String code) {
		BusinessApplyParty applyParty = new BusinessApplyParty();
		applyParty.setCode(code);
		EntityWrapper<BusinessApplyParty> wrapper = new EntityWrapper<>(applyParty);
		applyParty = this.getBusinessApplyPartyService().selectOne(wrapper);
		if (ObjectUtil.isNull(applyParty)) {
			FundServiceExceptionGenerator.FundServiceException(503, 
					"无法查询到编码为{"+code+"}会员信息");
		}
		return applyParty;
	}
	
	public List<BusinessApplyParty> getApplyPartyByApplyCodeRole(String applyCode, String roleCode) {
		BusinessApplyParty applyParty = new BusinessApplyParty();
		applyParty.setApplyCode(applyCode);
		if (StrUtil.isNotBlank(roleCode)) {
			applyParty.setRoleCode(roleCode);
		} else {
			roleCode="";
		}
		EntityWrapper<BusinessApplyParty> wrapper = new EntityWrapper<>(applyParty);
		List<BusinessApplyParty> applyPartys = this.getBusinessApplyPartyService().selectList(wrapper);
		if (CollUtil.isEmpty(applyPartys) || applyPartys.get(0) == null) {
			FundServiceExceptionGenerator.FundServiceException(503, 
					"无法查询到主单据编码为{"+applyCode+"}，会员角色为{"+roleCode+"}的会员信息");
		}
		return applyPartys;
	}
	
	public List<BusinessApplyParty> getApplyPartysByApplyCode(String applyCode) {
		BusinessApplyParty applyParty = new BusinessApplyParty();
		applyParty.setApplyCode(applyCode);
		EntityWrapper<BusinessApplyParty> wrapper = new EntityWrapper<>(applyParty);
		List<BusinessApplyParty> applyPartys = this.getBusinessApplyPartyService().selectList(wrapper);
		return CollUtil.isEmpty(applyPartys) ? Lists.newArrayList() : applyPartys;
	}
	
	public BusinessApplyCharges getApplyChargeByCode(String code) {
		BusinessApplyCharges applyCharge = new BusinessApplyCharges();
		applyCharge.setCode(code);
		EntityWrapper<BusinessApplyCharges> wrapper = new EntityWrapper<>(applyCharge);
		applyCharge = this.getBusinessApplyChargesService().selectOne(wrapper);
		if (ObjectUtil.isNull(applyCharge)) {
			FundServiceExceptionGenerator.FundServiceException(503, 
					"无法查询到编码为{"+code+"}费用信息");
		}
		return applyCharge;
	}
	
	public List<BusinessApplyCharges> getApplyChargesByApplyCode(String applyCode) {
		BusinessApplyCharges applyCharge = new BusinessApplyCharges();
		applyCharge.setApplyCode(applyCode);
		EntityWrapper<BusinessApplyCharges> wrapper = new EntityWrapper<>(applyCharge);
		List<BusinessApplyCharges> applyCharges = this.getBusinessApplyChargesService().selectList(wrapper);
		return CollUtil.isEmpty(applyCharges) ? Lists.newArrayList() : applyCharges;
	}
	
	public BusinessApplyBalance getApplyBalanceByCode(String code) {
		BusinessApplyBalance applyBalance = new BusinessApplyBalance();
		applyBalance.setCode(code);
		EntityWrapper<BusinessApplyBalance> wrapper = new EntityWrapper<>(applyBalance);
		applyBalance = this.getBusinessApplyBalanceService().selectOne(wrapper);
		if (ObjectUtil.isNull(applyBalance)) {
			FundServiceExceptionGenerator.FundServiceException(503, 
					"无法查询到编码为{"+code+"}账户资金信息");
		}
		return applyBalance;
	}
	
	public List<BusinessApplyBalance> getApplyBalancesByApplyCode(String applyCode) {
		BusinessApplyBalance applyBalance = new BusinessApplyBalance();
		applyBalance.setApplyCode(applyCode);
		EntityWrapper<BusinessApplyBalance> wrapper = new EntityWrapper<>(applyBalance);
		List<BusinessApplyBalance> applyBalances = this.getBusinessApplyBalanceService().selectList(wrapper);
		return CollUtil.isEmpty(applyBalances) ? Lists.newArrayList() : applyBalances;
	}
	
	public List<BusinessApplyBalance> getApplyBalancesByChargeCode(String chargeCode, String roleType) {
		BusinessApplyBalance applyBalance = new BusinessApplyBalance();
		applyBalance.setApplyChargeCode(chargeCode);
		if(StrUtil.isNotBlank(roleType)) {
			applyBalance.setRoleType(roleType);
		}
		EntityWrapper<BusinessApplyBalance> wrapper = new EntityWrapper<>(applyBalance);
		List<BusinessApplyBalance> applyBalances = this.getBusinessApplyBalanceService().selectList(wrapper);
		return CollUtil.isEmpty(applyBalances) ? Lists.newArrayList() : applyBalances;
	}
	
	public OrderApply findOrderApplyByOrderNo(String orderNo) {
		Order applyMain = this.getApplyMainByCode(orderNo);
		OrderApply orderApply = new OrderApply(applyMain);
		orderApply.setApplyPartys(this.getApplyPartysByApplyCode(orderNo));
		orderApply.setApplyCharges(this.getApplyChargesByApplyCode(orderNo));
		orderApply.setApplyBlances(this.getApplyBalancesByApplyCode(orderNo));
		return orderApply;
	}
	
	public List<BusinessApplyBusiDto>  findSoldProxyOrders(String memberId, String orderNo) {
		return this.baseMapper.findSoldProxyOrders(memberId, orderNo);
	}
	
	public List<BusinessApplyBalance> findWithdrawBalances(String status) {
		BusinessApplyBalance entity = new BusinessApplyBalance();
		entity.setStatus(status);
		entity.setRoleType(BusinessApplyRoleType.SOLD_PROXY_MONEY_WIHTDRAW.getCateCode());
		List<BusinessApplyBalance> balances = 
				this.businessApplyBalanceService.selectList(new EntityWrapper<BusinessApplyBalance>(entity));
		return balances;
	}
}


