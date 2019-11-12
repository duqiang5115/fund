package com.taolue.baoxiao.fund.service.composite.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.SearchParamEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.util.Exceptions;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.dto.FundAcctBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.entity.TbFundAcct;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.mapper.FundAcctBalanceMapper;
import com.taolue.baoxiao.fund.service.ITbFundAcctService;
import com.taolue.baoxiao.fund.service.ITbFundBalanceService;
import com.taolue.baoxiao.fund.service.ITbFundTradeFlowService;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceService;
import com.taolue.baoxiao.fund.service.remote.IOpenPlatformService;

import cn.hutool.core.bean.copier.BeanCopier;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 
 * </p>
 *
 * @author baoxiao
 * @since 2018-09-07
 */
@Service
@Slf4j
public class AcctBalanceServiceImpl
		extends ServiceImpl<FundAcctBalanceMapper, FundAcctBalanceDto>
		implements IAcctBalanceService {
	
	@Autowired
	private ITbFundBalanceService fundBalanceService;
	
//    @Autowired
//    private ITbFundAcctService fundAcctService;
	
	@Autowired
	private IAcctBalanceBusiService acctBalanceBusiService;
	
	@Autowired
	private ITbFundTradeFlowService fundTradeFlowService;
	
//    @Autowired
//    private  IOpenPlatformService openPlatformService;
    
//	@Override
//	public List<FundBalanceDto> selectFundBalanceItemAmount(Map<String, Object> params){
//		return this.baseMapper.selectFundBalanceItemAmount(params);
//	}
	
//	@Override
//	public List<FundBalanceDto> selectFundBalanceItemAmounts(List<String> balanceItemCodes) {
//		return this.baseMapper.selectFundBalanceItemAmounts(balanceItemCodes);
//	}
	
//    private Map<String, Object> detemitParams(String memberId, String memeberCate, String acctCate, 
//    		String balanceItemNo, String busiModel, String canTicket, String canTransfer, String companyId, 
//    		String ownerId) {
//
//    	Map<String, Object> params = Maps.newHashMap();
//    	//如果是平台账户类型
//    	if (StrUtil.equals(MemberCateEnums.MEMBER_CATE_PT.getCateCode(), memeberCate)) {
//    		memberId = CommonConstant.PLANTFORM_ACCT_MEMBER_ID;
//    		busiModel = CommonConstant.BALANCE_BUSI_MODEL_NONE;
//    		acctCate = CommonConstant.PLANTFORM_ACCT_CATE;
//		//非平台账户
//		} else {
//			//后付费类型
//			if (CommonConstant.BALANCE_ITEM_NO_FULL_YCBZJ.equals(balanceItemNo)) {
//				busiModel = BusiModelEnums.BUSI_MODEL_YCCM.getCateCode();
//				
//		    //预充值报销
//			} else if (CommonConstant.BALANCE_ITEM_NO_FULL_YCGBX.equals(balanceItemNo)) {
//				busiModel = BusiModelEnums.BUSI_MODEL_YCPE.getCateCode();
//			
//			//预充值保证金
//			} else if (CommonConstant.BALANCE_ITEM_NO_FULL_HFBZJ.equals(balanceItemNo)) {
//				busiModel = BusiModelEnums.BUSI_MODEL_HFCM.getCateCode();
//				
//			//预充值报销
//			} else if (CommonConstant.BALANCE_ITEM_NO_FULL_HFGBX.equals(balanceItemNo)) {
//				busiModel = BusiModelEnums.BUSI_MODEL_HFPE.getCateCode();
//			//其他账户业务模式都为固定值
//			} else {
//				busiModel = CommonConstant.BALANCE_BUSI_MODEL_NONE;
//			}
//			//如果非券账户
//			if (!AcctCateEnums.ACCT_CATE_COUPON.getCateCode().equals(acctCate)) {
//				params.put("canTransfer",  "1");
//			}
//		}
//    	
//    	if (StrUtil.isNotBlank(companyId)) {
//    		params.put("companyId", companyId);
//    	}
//    	if (StrUtil.isNotBlank(ownerId)) {
//    		params.put("ownerId", ownerId);
//    	}
//    	
//		if (StrUtil.isNotBlank(canTransfer)) {
//			params.put("canTransfer",  canTransfer);
//	    } else {
//	    	params.put("canTransfer",  "1");
//	    }
////    	if (AcctCateEnums.ACCT_CATE_COUPON.getCateCode().equals(acctCate) 
////				|| AcctCateEnums.ACCT_CATE_DIRECT_COUPON.getCateCode().equals(acctCate) ) {
////    		if (StrUtil.isNotBlank(companyId)) {
////        		params.put("companyId", companyId);
////        	}
////        	if (StrUtil.isNotBlank(ownerId)) {
////        		params.put("ownerId", ownerId);
////        	}
////        	
////			if (StrUtil.isNotBlank(canTransfer)) {
////				params.put("canTransfer",  canTransfer);
////		    }
////		}
//    	
//    	if (!ObjectUtils.isEmpty(busiModel)) {
//    		params.put("busiModel", busiModel);
//    	}
//    	
//    	params.put("memberId", memberId);
//		params.put("fundAcctCate", acctCate);
//		List<String> balanceItemNos = Lists.newArrayList();
//		balanceItemNos.add(balanceItemNo);
//		params.put("balanceItemNos", balanceItemNos);
//		params.put("group", false);
//		params.put("memberCate", memeberCate);
//		params.put("canTicket", StrUtil.isEmpty(canTicket) ? CommonConstant.STATUS_YES : canTicket);
//		return params;
//    }

//    @Override
//	public FundBalanceDto findSignleBalance(String memberId, String memeberCate, String acctCate, 
//			String acctBalanceItemNo, String balanceItemNo, String busiModel, String canTicket, String canTransfer, 
//			Date validTime, Date expireTime, String companyId, String ownerId) {
//
//		FundAcctBalanceDto fundAcctBalanceDto = this.findBalances(memberId, memeberCate, acctCate, 
//				acctBalanceItemNo, balanceItemNo, busiModel, canTicket, canTransfer, validTime, expireTime, 
//				companyId, ownerId);
//
//		FundBalanceDto balanceDto = null;
//		
//		if (CollUtil.isNotEmpty(fundAcctBalanceDto.getFundBalances())) {
//			balanceDto = fundAcctBalanceDto.getFundBalances().get(0);
//		}
//		
//		if (null == balanceDto || StrUtil.isEmpty(balanceDto.getBalanceCode())) {
//			//非券账户非定向券账户的情况下
//			if (!AcctCateEnums.ACCT_CATE_COUPON.getCateCode().equals(acctCate)
//				   && !AcctCateEnums.ACCT_CATE_DIRECT_COUPON.getCateCode().equals(acctCate)) {
//				throw new BaoxiaoException(903,"["+memberId+"]的["+memeberCate+acctCate
//				+"]类型账户的["+balanceItemNo+"]资金记录尚未创建！！请检查！");
//				
//			//，否则有可能券id相关的资金记录未创建，此时需要对其新建资金记录
//			} else {
//				balanceDto = new FundBalanceDto(fundAcctBalanceDto.getAcctInstNo(), null, balanceItemNo, busiModel, 
//						null, null, null, canTicket, canTransfer, validTime, expireTime, companyId, ownerId, null);
//			}
//		}
//		return balanceDto;
//	}
	
//	@Override
//	@SuppressWarnings("unchecked")
//	public Map<String, Object> querySumAmountByAcctAndItemNo(String acctInstNo, String balanceItemCode) {
//		return this.fundBalanceService.selectMap(Condition.create()
//				.setSqlSelect("sum(balance_amount) AS "
//					+ CommonConstant.COLUMN_NAME_SUM_BALANCE_AMOUNT+", sum(freezing_balance) AS "
//					+ CommonConstant.COLUMN_NAME_SUM_FREEZING_BALANCE+",sum(ticket_balance) AS "
//					+ CommonConstant.COLUMN_NAME_SUM_TICKET_BALANCE)
//		.eq("acct_inst_no", acctInstNo)
//		.eq("balance_item_code", balanceItemCode)
//		.orderBy("weight", true));
//	}
	
	@Override
	public FundTradeFlowDto warpperTradeFlow(String balanceCode, String businessCode, String busiOrderNo, String billItemCate, String tradeCate,
			String tradeActCode, BigDecimal amount, String source, String remark, String busiModel, BigDecimal... tradeOrder) {
		
		FundTradeFlowDto fundTradeFlow = new FundTradeFlowDto();
		
		//资金记录编码
		fundTradeFlow.setBalanceCode(balanceCode);
		
		//生成全局唯一交易流水号
		fundTradeFlow.setTradeFlowCode(CommonConstant.KEY_PERFIX_TRADEFLOW_NO+IdWorker.getIdStr());
		
		//设置资金科目
		fundTradeFlow.setBillItemCate(billItemCate);
		
		//设置订单类型
		fundTradeFlow.setTradeBusiCate(businessCode);
		
		//设置订单类型名称
		fundTradeFlow.setTransBusiCateName(OrderType.getOrderType(businessCode).getCateName());
		
		//设置订单编号
		fundTradeFlow.setTradeBusiCode(busiOrderNo);
		
		//设置业务模式
		fundTradeFlow.setBusiModel(busiModel);
		
		//设置本次交易过程中，本条流水的顺序号
		BigDecimal order = ArrayUtil.firstNonNull(tradeOrder);
		
		if (null != order && order.compareTo(CommonConstant.NO_AMOUNT) > 0) {
			fundTradeFlow.setTradeFlowOrder(tradeOrder[0]);
		} else {
			fundTradeFlow.setTradeFlowOrder(CommonConstant.TRADE_FLOW_ORDER_NOMAL);
		}
		
		//设置交易类型
		fundTradeFlow.setTradeCate(tradeCate);
		
		//设置交易动作类型
		fundTradeFlow.setTransActCate(tradeActCode);
		
		//设置交易类型名称
		fundTradeFlow.setTransCateName(TradeCateEnums.lookupByCode(tradeCate).getCateName()); 
		
		//设置交易动作类型名称
		fundTradeFlow.setTransActName(ActionType.getActionType(tradeActCode).getCateName());
		
		//设置交易金额
		fundTradeFlow.setTradeAmount(amount);
		
		//设置交易请求方编码
		fundTradeFlow.setSource(source);
		
		//设置备注
		fundTradeFlow.setRemark(remark);
		
		return fundTradeFlow;
	}
	
	@Override
	public FundTradeFlowDto warpperTradeFlow(String balanceCode, String businessCode, String busiOrderNo, String billItemCate, String tradeCate,
			String tradeActCode, BigDecimal amount, String source, String remark, String busiModel, String tradeFlowCode, BigDecimal... tradeOrder) {
		
		FundTradeFlowDto fundTradeFlow = new FundTradeFlowDto();
		
		//资金记录编码
		fundTradeFlow.setBalanceCode(balanceCode);
		
		//生成全局唯一交易流水号
		if (StrUtil.isNotBlank(tradeFlowCode)) {
			fundTradeFlow.setTradeFlowCode(tradeFlowCode);
		} else {
			fundTradeFlow.setTradeFlowCode(CommonConstant.KEY_PERFIX_TRADEFLOW_NO+IdWorker.getIdStr());
		}
		
		//设置资金科目
		fundTradeFlow.setBillItemCate(billItemCate);
		
		//设置订单类型
		fundTradeFlow.setTradeBusiCate(businessCode);
		
		//设置订单类型名称
		fundTradeFlow.setTransBusiCateName(OrderType.getOrderType(businessCode).getCateName());
		
		//设置订单编号
		fundTradeFlow.setTradeBusiCode(busiOrderNo);
		
		//设置业务模式
		fundTradeFlow.setBusiModel(busiModel);
		
		//设置本次交易过程中，本条流水的顺序号
		BigDecimal order = ArrayUtil.firstNonNull(tradeOrder);
		
		if (null != order && order.compareTo(CommonConstant.NO_AMOUNT) > 0) {
			fundTradeFlow.setTradeFlowOrder(tradeOrder[0]);
		} else {
			fundTradeFlow.setTradeFlowOrder(CommonConstant.TRADE_FLOW_ORDER_NOMAL);
		}
		
		//设置交易类型
		fundTradeFlow.setTradeCate(tradeCate);
		
		//设置交易动作类型
		fundTradeFlow.setTransActCate(tradeActCode);
		
		//设置交易类型名称
		fundTradeFlow.setTransCateName(TradeCateEnums.lookupByCode(tradeCate).getCateName()); 
		
		//设置交易动作类型名称
		fundTradeFlow.setTransActName(ActionType.getActionType(tradeActCode).getCateName());
		
		//设置交易金额
		fundTradeFlow.setTradeAmount(amount);
		
		//设置交易请求方编码
		fundTradeFlow.setSource(source);
		
		//设置备注
		fundTradeFlow.setRemark(remark);
		
		return fundTradeFlow;
	}
	
	public TbFundTradeFlow findAdductTradeFlow(String balanceCode, String busiCode, String billItemCate, BigDecimal tradeAmount) {
		EntityWrapper<TbFundTradeFlow> wrapper = new EntityWrapper<>();
		//资金编号
		wrapper.eq("balance_code", balanceCode);
		//关联单据号
		wrapper.eq("trade_busi_code", busiCode);
		//交易类型是冻结
		wrapper.eq("trade_cate", TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode());
		//资金科目类型
		wrapper.eq("bill_item_cate", billItemCate);
		//状态冻结中
		wrapper.eq("status", CommonConstant.STATUS_TRADE_FLOW_NORMAL);
		
		if (null != tradeAmount && tradeAmount.longValue()>0) {
			//冻结金额
			wrapper.eq("trade_amount", tradeAmount);
		}
		
		TbFundTradeFlow frezzTradeFlow = this.fundTradeFlowService.selectOne(wrapper);
		if (null == frezzTradeFlow) {
			log.info("找不到资金{}订单{}的科目{}的入账记录，请检查！！",new Object[] {
					balanceCode,busiCode,billItemCate
			});
		}
		return frezzTradeFlow;
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
	
//	@Override
//	public List<TbFundAcct> findFundAccts(String memberId, String fundAcctCate) {
//    	EntityWrapper<TbFundAcct> wrapper = new EntityWrapper<>();
//    	
//    	boolean isCompany = false;
//    	
//    	wrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
//    	wrapper.eq("member_id", memberId);
//    	wrapper.eq("acct_cate", fundAcctCate);
//    	
//    	List<TbFundAcct> fundAccts = this.fundAcctService.selectList(wrapper);
//    	if (isCompany && CollUtil.isNotEmpty(fundAccts) 
//    			&& fundAccts.size()>1) {
//    		FundServiceExceptionGenerator.FundServiceException("9001", 
//    				new Object[] {"", memberId, fundAcctCate});
//    	}
//    	if (CollUtil.isNotEmpty(fundAccts)) {
//    		return fundAccts;
//    	}
//    	return null;
//    }
//	
//	@Override
//	public TbFundAcct findFundAcct(String memberId, String fundAcctCate) {
//		
//		List<TbFundAcct> fundAccts = this.findFundAccts(memberId, fundAcctCate);
//		if (CollUtil.isNotEmpty(fundAccts)) {
//			return fundAccts.get(0);
//		}
//		return null;
//    }
	
//	@Override
//	public int hadAccount(String memberId) {
//		EntityWrapper<TbFundAcct> wrapper = new EntityWrapper<TbFundAcct>();
//        wrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
//        wrapper.eq("member_id", memberId);
//
//		List<TbFundAcct> fundAccts = this.fundAcctService.selectList(wrapper);
//		if (CollUtil.isNotEmpty(fundAccts)) {
//			return fundAccts.size();
//		}
//		return 0; 
//	}
	
//	@Override
//	public TbFundAcct findAcctByAcctInstNo(String acctInstNo) {
//    	EntityWrapper<TbFundAcct> acctWrapper = new EntityWrapper<>();
//    	acctWrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
//    	acctWrapper.eq("acct_inst_no", acctInstNo);
//    	TbFundAcct fundAcct = this.fundAcctService.selectOne(acctWrapper);
//    	if (null != fundAcct) {
//    		return fundAcct;
//    	}
//    	return null;
//    }
	
//	public TbFundBalance checkingCompanyRechargeBalance(String memberId) {
//		
//		String funAcctCate = MemberCateEnums.MEMBER_CATE_CMP.getCateCode()
//				+AcctCateEnums.ACCT_CATE_PERGM.getCateCode();
//		
//		TbFundAcct acct = this.findFundAcct(memberId, funAcctCate);
//		
//		if (null == acct) {
//			throw new BaoxiaoException(904,"对象["+memberId+"]没有["+funAcctCate+"]类型的账户，请检查");
//		}
//		
//		String balanceItemCode = funAcctCate+CommonConstant.BALANCE_ITEM_NO_SUFFX_BZJ;
//		
//		TbFundBalance balance = this.findFundBalance(acct.getAcctInstNo(), balanceItemCode);
//		
//		if (null == balance || StrUtil.isBlank(balance.getId())) {
//			throw new BaoxiaoException(904,"对象["+memberId+"]没有["+balanceItemCode+"]类型的资金记录，请检查");
//		}
//		BigDecimal currentAmount = null;
//		try {
//			currentAmount = this.openPlatformService.findCompanyRecharge(memberId);
//		} catch (Exception e) {
//			log.warn("调用总部获取现金账户余额出现错误，错误原因{},设置余额为NULL", Exceptions.getStackTraceAsString(e));
//			currentAmount = null;
//		}
//		
//		if (null != currentAmount) {
//			balance.setBalanceAmount(currentAmount);
//			balance.setDelFlag(CommonConstant.STATUS_NORMAL);
//			balance.setStatus(CommonConstant.STATUS_NORMAL);
//			balance.insertOrUpdate();
//		}
//		return balance;
//	}
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	@Override
//	public TbFundBalance findAcctBalanceByCode(String balanceCode) {
//		EntityWrapper<TbFundBalance>  balance = new EntityWrapper<>();
//		balance.eq("del_flag", CommonConstant.STATUS_NORMAL);
//		balance.eq("balance_code", balanceCode);
//		return this.fundBalanceService.selectOne(balance);
//	}
//	
//	@Override
//	public List<TbFundBalance> findBalanceByNcountId(String ncountId) {
//		return this.baseMapper.findBalanceByNcountId(ncountId);
//	}

	@Override
	public List<TbFundTradeFlow> findBalanceFlowDetails(String memberId, String paramCode, 
			Date beginTime, Date endTime) {
		
		SearchParamEnums searchParam = SearchParamEnums.lookupbycode(paramCode);
		
		String memberCate = searchParam.getMemberCate();
		String acctCate = searchParam.getAcctCate();
		String balanceCate = searchParam.getBalanceCate();
		String busiModel = searchParam.getBusiModel();
		String billItemCate = searchParam.getBillItemCate();
		String orderType = searchParam.getOrderType();
		String tradeCate = searchParam.getTradeCate();
		String status = searchParam.getFlowStatus();
		FundBalanceDto fundBalanceDto = acctBalanceBusiService.findSignleBalanceByParams(CommonConstant.STRING_BLANK, memberId, memberCate, acctCate, balanceCate, busiModel, 
				null, null, CommonConstant.STRING_BLANK, null, null);
//		FundBalanceDto fundBalanceDto = this.findSignleBalance(memberId,memberCate, acctCate, balanceCate, balanceCate, busiModel, 
//				null, null, null, null, CommonConstant.STRING_BLANK, CommonConstant.STRING_BLANK);
		
		if (null != fundBalanceDto) {
			
			return this.findTradeFlows(fundBalanceDto.getBalanceCode(), 
					busiModel, billItemCate, orderType, tradeCate, status, CommonConstant.STRING_BLANK, 
					CommonConstant.NO_AMOUNT, beginTime, endTime);
		}
		
		return null;
	}
	
//	public Page<TbFundTradeFlow> findTradeFlowsPaged(Page<TbFundTradeFlow> page, 
//			String balanceCode, String busiModel, 
//			String billItemCate, String orderType,
//			String tradeCate, String status, 
//			String busiCode, BigDecimal tradeAmount, 
//			Date beginTime, Date endTime) {
//		
//		FundTradeFlowDto fundTradeFlowDto=new FundTradeFlowDto();	
//		fundTradeFlowDto.setBeginTime(beginTime);
//		fundTradeFlowDto.setEndTime(endTime);
//		fundTradeFlowDto.setTradeAmount(tradeAmount);
//		fundTradeFlowDto.setBusiCode(busiCode);
//		//状态
//		if (StrUtil.isNotBlank(status)) {
//			String[] statuss = StrUtil.split(status, ",");
//			fundTradeFlowDto.setStatuss(statuss);
//		} else {
//			fundTradeFlowDto.setStatus( CommonConstant.STATUS_TRADE_FLOW_NORMAL);
//		}
//		//资金编号
//		if (StrUtil.isNotBlank(balanceCode)) {
//			String[] balanceCodes = StrUtil.split(balanceCode, ",");
//			fundTradeFlowDto.setBalanceCodes(balanceCodes);
//		}
//		
//		//业务模式
//		if (StrUtil.isNotBlank(busiModel)) {
//			String[] busiModels = StrUtil.split(busiModel, ",");
//			fundTradeFlowDto.setBusiModels(busiModels);
//		}
//		
//		//交易类型
//		if (StrUtil.isNotBlank(orderType)) {
//			String[] orderTypes = StrUtil.split(orderType, ",");
//			fundTradeFlowDto.setOrderTypes(orderTypes);
//			
//		}
//		//资金科目类型
//		if (StrUtil.isNotBlank(billItemCate)) {
//			String[] billItemCates = StrUtil.split(billItemCate, ",");
//			fundTradeFlowDto.setBillItemCates(billItemCates);
//		}
//		
//		//交易流水类型
//		if (StrUtil.isNotBlank(tradeCate)) {
//			String[] tradeCates = StrUtil.split(tradeCate, ",");
//			fundTradeFlowDto.setTradeCates(tradeCates);
//		}
//		Map<String,Object> params=Maps.newHashMap();
//		params.put("page", page.getCurrent());
//		params.put("limit", page.getSize());
//		Page<TbFundTradeFlow>  tradeFlows=fundTradeFlowService.findFundTradeFlow(new Query<>(params), fundTradeFlowDto);
//		return tradeFlows;
//	}
	
	@Override
	public List<TbFundTradeFlow> findTradeFlows(String balanceCode, String busiModel, String billItemCate, String orderType,
			String tradeCate, String status, String busiCode, BigDecimal tradeAmount, Date beginTime, Date endTime) {
		
		EntityWrapper<TbFundTradeFlow> wrapper = new EntityWrapper<>();
		
		//资金编号
		if (StrUtil.isNotBlank(balanceCode)) {
			wrapper.eq("balance_code", balanceCode);
		}
		
		//业务模式
		if (StrUtil.isNotBlank(busiModel)) {
			wrapper.eq("busi_model", busiModel);
		}
		
		//交易类型
		if (StrUtil.isNotBlank(orderType)) {
			wrapper.eq("trade_busi_cate", orderType);
		}
		
		//关联单据号
		if (StrUtil.isNotBlank(busiCode)) {
			wrapper.eq("trade_busi_code", busiCode);
		}
		
		//资金科目类型
		if (StrUtil.isNotBlank(billItemCate)) {
			wrapper.eq("bill_item_cate", billItemCate);
		}
		
		//交易流水类型
		if (StrUtil.isNotBlank(tradeCate)) {
			wrapper.eq("trade_cate", tradeCate);
		}
		
		//状态
		if (StrUtil.isNotBlank(status)) {
			wrapper.eq("status", status);
		} else {
			wrapper.eq("status", CommonConstant.STATUS_TRADE_FLOW_NORMAL);
		}
				
		//金额
		if (null != tradeAmount && tradeAmount.compareTo(CommonConstant.NO_AMOUNT) > 0) {
			wrapper.eq("trade_amount", tradeAmount);
		}
		
		//开始时间
		if (null != beginTime) {
			wrapper.ge("create_time", beginTime);
		}
		
		//结束时间
		if (null != beginTime) {
			wrapper.ge("create_time", endTime);
		}
				
		List<TbFundTradeFlow> tradeFlows = this.fundTradeFlowService.selectList(wrapper);
		if (CollUtil.isEmpty(tradeFlows)) {
			log.warn("找不到资金{}的的资金流水记录，请检查参数wrapper={}",balanceCode,wrapper);
		}
		return tradeFlows;
	}
	
//	@Override
// 	public TbFundBalance findPerOrAftPaymentBalance(String memberId, String busiModel) {
//		//默认账户类型为后付费额度账户
//		String funAcctCate = MemberCateEnums.MEMBER_CATE_CMP.getCateCode()
//				+AcctCateEnums.ACCT_CATE_AFTGM.getCateCode();
//		
//		BigDecimal currentAmount = null;
//		//CommonConstant.NO_AMOUNT;
//		
//		//如果需要查询预充值企业或者预充值报销账户
//		if (BusiModelEnums.BUSI_MODEL_YCCM.getCateCode().equals(busiModel)
//				|| BusiModelEnums.BUSI_MODEL_YCPE.getCateCode().equals(busiModel)) {
//			
//			//设置账户类型为预充值现金账户
//			funAcctCate = MemberCateEnums.MEMBER_CATE_CMP.getCateCode()
//					+AcctCateEnums.ACCT_CATE_PERGM.getCateCode();
//			
//			//查询当前总部现金账户
//			try {
//				currentAmount = this.openPlatformService.findCompanyRecharge(memberId);
//			} catch (Exception e) {
//				log.warn("调用总部获取现金账户余额出现错误，错误原因{},设置余额为NULL", Exceptions.getStackTraceAsString(e));
//				currentAmount = null;
//			}
//		}
//		
//		//不指定公司查询当前账户
//		TbFundAcct acct = this.findFundAcct(memberId, funAcctCate);
//		
//		if (null == acct) {
//			FundServiceExceptionGenerator.FundServiceException("901", new Object[] {memberId,funAcctCate});
//		}
//		
//		//指定账户编号和账户类型查询资金记录
//		TbFundBalance balance = this.findFundBalance(acct.getAcctInstNo(), funAcctCate);
//		
//		if (null == balance || StrUtil.isBlank(balance.getId())) {
//			FundServiceExceptionGenerator.FundServiceException("903", new Object[] {memberId,funAcctCate,funAcctCate});
//			//throw new BaoxiaoException(904,"对象["+memberId+"]没有["+balanceItemCode+"]类型的资金记录，请检查");
//		}
//		if ((MemberCateEnums.MEMBER_CATE_CMP.getCateCode()
//				+AcctCateEnums.ACCT_CATE_PERGM.getCateCode()).equals(acct.getAcctCate())) {
//			if (null != currentAmount) {
//				balance.setBalanceAmount(currentAmount);
//				balance.setAuthBalance(currentAmount);
//				balance.setDelFlag(CommonConstant.STATUS_NORMAL);
//				balance.setStatus(CommonConstant.STATUS_NORMAL);
//				balance.insertOrUpdate();
//			}
//		}
//		return balance;
//	}
	
 	@Override
	public List<TbFundTradeFlow> findTradeFlows(String busiCode, String billItemCate, String balanceCode, 
			BigDecimal tradeAmount, String... tradeCateCodes) {
		EntityWrapper<TbFundTradeFlow> wrapper = new EntityWrapper<>();
		//资金编号
		wrapper.eq("balance_code", balanceCode);
		//关联单据号
		wrapper.eq("trade_busi_code", busiCode);
		//资金科目类型
		wrapper.eq("bill_item_cate", billItemCate);
		
		//交易类型
		String status = CommonConstant.STATUS_TRADE_FLOW_NORMAL;
		
		String tradeCateCode = ArrayUtil.firstNonNull(tradeCateCodes);
		if (StrUtil.isNotBlank(tradeCateCode)) {
			log.info("tradeCateCode={},TradeCateEnums.TRADE_CATE_FREZZ.getCateCode()={},tradeCateCode={},status={}",
					JSON.toJSONString(tradeCateCode),JSON.toJSONString(TradeCateEnums.TRADE_CATE_FREZZ.getCateCode()),JSON.toJSONString(tradeCateCode),JSON.toJSONString(status));
			if (TradeCateEnums.TRADE_CATE_FREZZ.getCateCode().equals(tradeCateCode)) {
				status = CommonConstant.STATUS_TRADE_FLOW_FREZZING;
				log.info("TradeCateEnums.TRADE_CATE_FREZZ.getCateCode().equals(tradeCateCode)={},status ===={}",JSON.toJSONString(true),JSON.toJSONString(status));
			}
			//交易流水类型
			wrapper.eq("trade_cate", tradeCateCode);
		}
		
		//状态
		wrapper.eq("status", status);
				
		if (null != tradeAmount && tradeAmount.compareTo(CommonConstant.NO_AMOUNT) > 0) {
			//冻结金额
			wrapper.eq("trade_amount", tradeAmount);
		}
		
		List<TbFundTradeFlow> tradeFlows = this.fundTradeFlowService.selectList(wrapper);
		log.info("List<TbFundTradeFlow> tradeFlows = this.fundTradeFlowService.selectList(wrapper)-->tradeFlows:{}",JSON.toJSONString(tradeFlows));
		if (CollUtil.isEmpty(tradeFlows)) {
			log.warn("找不到资金{}订单{}的科目{}的冻结或者入账记录，请检查！！",new Object[] {
					balanceCode,busiCode,billItemCate
			});
		}
		return tradeFlows;
	}
	
	@Override
	public TbFundBalance updateFundBalance(FundBalanceDto fundBalanceDto) {
		TbFundBalance fundBalance = new BeanCopier<>(fundBalanceDto, new TbFundBalance(), 
				new CopyOptions()).copy();
		return this.fundBalanceService.insertOrUpdate(fundBalance) ? fundBalance : null;  
	}
	
	@Override
	public TbFundTradeFlow updateFundTradeFlow(FundTradeFlowDto fundTradeFlowDto) {
		TbFundTradeFlow fundTradeFlow = new BeanCopier<>(fundTradeFlowDto, new TbFundTradeFlow(), 
				new CopyOptions()).copy();
		return this.updateFundTradeFlow(fundTradeFlow);
	}
	
	@Override
	public TbFundTradeFlow updateFundTradeFlow(TbFundTradeFlow fundTradeFlow) {
		return this.fundTradeFlowService.insertOrUpdate(fundTradeFlow) ? fundTradeFlow : null; 
	}

}
