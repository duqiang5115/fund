package com.taolue.baoxiao.fund.service.composite.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.SequenceNumber;
import com.taolue.baoxiao.common.util.UserUtils;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.dto.BalanceSearchParams;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.api.dto.TransDetailComposeDto;
import com.taolue.baoxiao.fund.api.dto.TransDetailDto;
import com.taolue.baoxiao.fund.api.vo.NCountCouponVo;
import com.taolue.baoxiao.fund.api.vo.NCountTransVo;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.entity.TbFundAcct;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.entity.Trans;
import com.taolue.baoxiao.fund.entity.TransDetail;
import com.taolue.baoxiao.fund.factory.INcountSplitAlgorithmContext;
import com.taolue.baoxiao.fund.mapper.TransDetailComposeMapper;
import com.taolue.baoxiao.fund.service.ITransDetailService;
import com.taolue.baoxiao.fund.service.ITransService;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.ITransDetailComposeService;
import com.taolue.baoxiao.fund.strategy.ncount.NcountSplitAlgorithmVo;
import com.taolue.baoxiao.fund.strategy.ncount.NcountSplitAlgorithmVo.NcountItem;
import com.taolue.baoxiao.fund.strategy.ncount.SplitCouponVo;

import cn.hutool.core.bean.copier.BeanCopier;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2018-11-21
 */
@Service
@Slf4j
public class TransDetailComposeServiceImpl extends ServiceImpl<TransDetailComposeMapper, TransDetailComposeDto> implements ITransDetailComposeService {
	
	@Autowired
	private ITransService transService;
	
	@Autowired
	private ITransDetailService transDetailService;
	
//	@Autowired
//	private IAcctBalanceService acctBalanceService;
	
	@Autowired
	private IAcctBalanceBusiService acctBalanceBusiService;
	
	@Autowired
	private INcountSplitAlgorithmContext ncountSplitAlgorithmContext;
	
	private CopyOptions options = null;
	
	@Override
	public Page<NCountCouponVo> findPagedCanTransferBalanceGroupAmount(Page<NCountCouponVo> pagedParams) {
		log.info("findPagedCanTransferBalanceGroupAmount params ="+JSON.toJSONString(pagedParams));
		
		String memberId = pagedParams.getCondition().getOrDefault("memberId", "").toString();
		List<NCountCouponVo> records = this.baseMapper.findPagedCanTransferBalanceGroupAmount(pagedParams, memberId);
		if (CollUtil.isNotEmpty(records)) {
			pagedParams.setRecords(records);
		} 
		return pagedParams;
	}
	
//	@Override
	private List<FundBalanceDto> findBalanceByMemberIdAndCompanyIdAndCouponId(String memberId, String memberCate, 
			String couponId, String companyId) {
		return this.acctBalanceBusiService
				.findBalancesByParams(companyId, memberId, null, 
						AcctCateEnums.ACCT_CATE_COUPON.getCateCode(), couponId, null, 
						CommonConstant.STRING_BLANK, 
						CommonConstant.STATUS_YES, null, null, null);
	}

	/**
	 * 
	 * @Title: createNcoupons   
	 * @Description: 拆分逻辑
	 * @Author: shilei
	 * @date:   Nov 22, 2018 3:01:55 PM  
	 * @param ncountTransVo NCountTransVo 请求参数
	 * @return List<TransDetailComposeDto>  拆分结果后的交易对象集合      
	 * @throws
	 *
	 */
	@Override
	@Transactional
	public List<NCountCouponVo> createNcoupons(NCountTransVo ncountTransVo) {
		
		SequenceNumber seq = new SequenceNumber(IdWorker.getId());
		String transBatchCode = CommonConstant.COUPON_NCOUPON_SN_PREFIX
				+seq.getTimestampAsString()
				+seq.getSeqNum();
		seq = null;
		
		//
		Map<String, List<NCountCouponVo>> couponIdGroupVoMap = 
				this.couponIdGroupVoMap(ncountTransVo.getCouponList());
		
		List<TransDetailComposeDto> transList = Lists.newArrayList();
		
		if (CollUtil.isNotEmpty(couponIdGroupVoMap)) {
			
			for (Map.Entry<String, List<NCountCouponVo>> entity : couponIdGroupVoMap.entrySet()) {
				List<NCountCouponVo> couponList = entity.getValue();
				String couponId = entity.getKey();
				log.info("面额券上架-查询可用消费券，查询参数：memberId:{},memberCate:{},couponId:{}",
						ncountTransVo.getMemberId(),ncountTransVo.getMemberCate(),couponId);
				List<FundBalanceDto> fundBalanceDtos = this.
						findBalanceByMemberIdAndCompanyIdAndCouponId(ncountTransVo.getMemberId(), 
						ncountTransVo.getMemberCate(), couponId, null);
				log.info("面额券上架-查询可用消费券，结果:{}",JSON.toJSONString(fundBalanceDtos));
				
				//过滤小于等于0的资金
				fundBalanceDtos = CollUtil.filter(fundBalanceDtos, new Filter<FundBalanceDto>() {

					@Override
					public boolean accept(FundBalanceDto t) {
					    if (t.getBalanceAmount().compareTo(CommonConstant.NO_AMOUNT)>0) {
					    	return true;
					    }
						return false;
					}
					
				});
				log.info("面额券上架-最终可用券列表，结果:{}",JSON.toJSONString(fundBalanceDtos));
				
				List<SplitCouponVo> splitCouponVos = this.splitCouponVos(fundBalanceDtos);
				log.info("面额券上架-拆分消费券-开始-需拆分券：{}",JSON.toJSONString(splitCouponVos));
				for (NCountCouponVo ncountCouponVo : couponList) {
					
					BigDecimal transAmount = CommonConstant.NO_AMOUNT;
					
					seq = new SequenceNumber(IdWorker.getId());
					String  transCode = seq.getBuinessCode(CommonConstant.COUPON_NCOUPON_DETAIL_SN_PREFIX, 
							CommonConstant.STRING_BLANK);
					seq = null;
					
					TransDetailComposeDto transDetailComposeDto 
					= new TransDetailComposeDto(transBatchCode, 
							OrderType.ORDER_TYPE_NCOUNT_TRANSFER.getCateCode(),
							transCode,
							ncountTransVo.getSource(),
							transAmount);
						transDetailComposeDto.setFundBalanceDtos(fundBalanceDtos);
						
					//面额
					BigDecimal ncountAmount = ncountCouponVo.getAmount();
					//数量
					int count = ncountCouponVo.getCount();
					
					//拆分金额
					NcountSplitAlgorithmVo ncountSplitAlgorithmVo = 
							ncountSplitAlgorithmContext.splitCouponToNcount("DNSA", splitCouponVos, 
									ncountAmount, count);
					log.info("面额券上架-拆分消费券-开始-拆分后结果：{}",
							JSON.toJSONString(ncountSplitAlgorithmVo));
					
					//组合返回
					if (null != ncountSplitAlgorithmVo 
							&& CollUtil.isNotEmpty(ncountSplitAlgorithmVo.getSplitCouponVos())) {
						splitCouponVos = ncountSplitAlgorithmVo.getSplitCouponVos();
						List<NcountItem> ncountItems = ncountSplitAlgorithmVo.getNcountItems();
						transDetailComposeDto =  createTransDetail(transDetailComposeDto, ncountItems, couponId);
						transList.add(transDetailComposeDto);
					}
				}
			}
		}
		log.info("面额券上架-需记录流水交易列表：{}",
				JSON.toJSONString(transList));
		createNcountTransAndTradeFlow(ncountTransVo.getCompanyId(),ncountTransVo.getMemberId(), 
				ncountTransVo.getMemberCate(), transList);
		List<NCountCouponVo> result = Lists.newArrayList();
		
		if (CollUtil.isNotEmpty(transList)) {
			for (TransDetailComposeDto transDetailComposeDto : transList) {
				List<NCountCouponVo> ncountCoupons = transDetailComposeDto.getNCountCouponVos();
				result.addAll(ncountCoupons);
			}
		}
		
		return result;
	}
	
	@Override
	@Transactional
	public boolean ncountBuy(NCountTransVo ncountTransVo) {
		this.buyCouponInfo(ncountTransVo.getMemberId(), ncountTransVo.getMemberCate(), 
				ncountTransVo.getCompanyId(), ncountTransVo.getCouponList());
		return true;
	}
	
	@Override
	@Transactional
	public String ncountRecharge(NCountTransVo ncountTransVo) {
		//1、创建充值主单据
		String[] busiCodes = createBatchAndTradeCode();
		TransDetailComposeDto transDetailComposeDto = new TransDetailComposeDto(busiCodes[0], 
				OrderType.ORDER_TYPE_NCOUNT_RECHARGE.getCateCode(),
				busiCodes[1],
				ncountTransVo.getSource(),
				CommonConstant.NO_AMOUNT);
		
		//定义总交易额，初始为零
		BigDecimal transAmount = transDetailComposeDto.getTransAmount();
		
		//2、创建明细
		//初始化明细为空列表
		List<TransDetailDto> transDetails = transDetailComposeDto.getTransDetails();
		if (CollUtil.isEmpty(transDetails)) {
			transDetails = Lists.newArrayList();
		}
		
		//获取充值面额券列表
		List<NCountCouponVo>  couponList = ncountTransVo.getCouponList();
		
		//查询面额券账户
//		TbFundAcct accountNcount = this.acctBalanceBusiService.findFundAcct(ncountTransVo.getMemberId(), 
//				ncountTransVo.getMemberCate()+AcctCateEnums.ACCT_CATE_NCOUNT_COUPON.getCateCode());
		TbFundAcct accountNcount = this.acctBalanceBusiService.findFundAcct(ncountTransVo.getMemberId(), 
				AcctCateEnums.ACCT_CATE_NCOUNT_COUPON.getCateCode());
		
		
		//查询消费券账户
//		TbFundAcct accountCoupon = this.acctBalanceBusiService.findFundAcct(ncountTransVo.getMemberId(), 
//				ncountTransVo.getMemberCate()+AcctCateEnums.ACCT_CATE_COUPON.getCateCode());
		TbFundAcct accountCoupon = this.acctBalanceBusiService.findFundAcct(ncountTransVo.getMemberId(), 
				AcctCateEnums.ACCT_CATE_COUPON.getCateCode());
		//处理面额券充值
		for (NCountCouponVo ncountCouponVo : couponList) {
			
			//查询当前会员，当前面额券资金，需要清零
			FundBalanceDto ncountBalanceDto = this.acctBalanceBusiService
					.findSignleBalanceByAcctInstNo(accountNcount.getAcctInstNo(), 
					ncountCouponVo.getId());
			
			if (null == ncountBalanceDto) {
				FundServiceExceptionGenerator.FundServiceException("无法查询到会员["+ncountTransVo.getMemberId()
					+"]面额券["+ncountCouponVo.getId()+"]的资金记录！！");
			}
			if (ncountBalanceDto.getBalanceAmount().compareTo(CommonConstant.NO_AMOUNT)<=0) {
				FundServiceExceptionGenerator.FundServiceException("会员["+ncountTransVo.getMemberId()
					+"]面额券["+ncountCouponVo.getId()+"]的额度低于或等于零，该面额券可能已经充值，无法再次充值！！");
			}
			
			BeanCopier<TbFundBalance> ncountBalanceDtoCopier = new BeanCopier<TbFundBalance>(ncountBalanceDto, 
					new TbFundBalance(), createCopyOptions());
			TbFundBalance ncountBalance = ncountBalanceDtoCopier.copy();
			
			//查询当前面额券原始上架记录，如果面额券是组合生成的，则有肯能会查出多条记录
			List<TransDetail> oriNcountDetails = this.findTransDetailByNcountId(ncountCouponVo.getId());
			if (CollUtil.isEmpty(oriNcountDetails)) {
				FundServiceExceptionGenerator.FundServiceException("无法查询到面额券["
													+ncountCouponVo.getId()+"]的上架记录！！");
			}
			
			for (TransDetail oriNcountDetail : oriNcountDetails) {
				
				//复制detail，作为充值detail
				BeanCopier<TransDetail> oriNcountDetailCopier = 
						new BeanCopier<TransDetail>(oriNcountDetail, new TransDetail(), createCopyOptions());
				TransDetail ncountDetail = oriNcountDetailCopier.copy();
				//源消费券资金编号
			    String oriBalanceCode = ncountDetail.getTransItemSourceCode();
			    //源消费券资金使用额度
			    BigDecimal payAmount = ncountDetail.getPayAmount();
			    //累加总交易额度
				transAmount = transAmount.add(payAmount);
				
			    //重置充值明细相关属性
				ncountDetail.setId(null);
				ncountDetail.setTransCode(transDetailComposeDto.getTransCode());
				ncountDetail.setTransItemTargetCode(oriBalanceCode);
				ncountDetail.setTransItemSourceCode(ncountCouponVo.getId());
				ncountDetail.setCreator(UserUtils.getUser());
				ncountDetail.setUpdator(UserUtils.getUser());
				
				//获取源消费券资金记录
//				TbFundBalance oriCouponBalance = 
//						this.acctBalanceService.findAcctBalanceByCode(oriBalanceCode);
				FundBalanceDto oriCouponBalance = 
						this.acctBalanceBusiService.findSignleBalanceByCode(oriBalanceCode);
				
				//复制源消费券资金记录，作为当前充值的消费券资金记录，
				//若果当前充值会员已经有了相同的消费券资金记录，则更新现有消费券资金记录，否则新建相应消费券资金
				BeanCopier<FundBalanceDto> copyerOriCouponBalance = new BeanCopier<FundBalanceDto>(oriCouponBalance, 
						new FundBalanceDto(), createCopyOptions());
				FundBalanceDto couponBalanceDto =  copyerOriCouponBalance.copy();

				//判断是否已经存在对应消费券资金
				FundBalanceDto hadbalanceDto = this.acctBalanceBusiService.findSignleBalanceByParams(couponBalanceDto.getCompanyId(), 
						accountCoupon.getMemberId(), 
						MemberCateEnums.MEMBER_CATE_ALO.getCateCode(), 
						accountCoupon.getAcctCate(), 
						couponBalanceDto.getBalanceItemCode(), 
						couponBalanceDto.getBusiModel(), 
						couponBalanceDto.getCanTicket(), 
						couponBalanceDto.getCanTransfer(), 
						couponBalanceDto.getOwnerId(), null, null);
		
				//重置相关属性
				couponBalanceDto.setAcctInstNo(accountCoupon.getAcctInstNo());
				couponBalanceDto.setBalanceAmount(CommonConstant.NO_AMOUNT);
				couponBalanceDto.setFreezingBalance(CommonConstant.NO_AMOUNT);
				couponBalanceDto.setId(null);
				
				//如果已经存在，则复用
				if (null != hadbalanceDto && StrUtil.isNotBlank(hadbalanceDto.getId())) {
					couponBalanceDto = hadbalanceDto;
				//否则新建资金记录
				} else {
					couponBalanceDto.setBalanceCode(CommonConstant.KEY_PERFIX_BALANCE_NO+IdWorker.getIdStr());
				}
				
				String source = ncountTransVo.getSource();
				if (StrUtil.isBlank(source)) {
					source =  CommonConstant.TRANS_CHANNEL_ZHJ;
				}
				//创建面额券扣除资金流水
				FundTradeFlowDto  ncountCouponDecutFlow = this.createDecutTradeFlow(ncountBalance, new BigDecimal(1), 
						transDetailComposeDto.getTransCode(), payAmount, source);
				
				//创建消费券资金入账资金流水
				FundTradeFlowDto  couponAdductFlow = this.createAdductTradeFlow(couponBalanceDto, new BigDecimal(2), 
						transDetailComposeDto.getTransCode(), payAmount, source);
				
				
				//资金流水入库
				BeanCopier<TbFundTradeFlow> ncountCouponDecutFlowEntity = new BeanCopier<TbFundTradeFlow>(ncountCouponDecutFlow, 
						new TbFundTradeFlow(), createCopyOptions());
				
				BeanCopier<TbFundTradeFlow> couponAdductFlowEntity = new BeanCopier<TbFundTradeFlow>(couponAdductFlow, 
						new TbFundTradeFlow(), createCopyOptions());
				ncountCouponDecutFlowEntity.copy().insertOrUpdate();
				couponAdductFlowEntity.copy().insertOrUpdate();
				
				couponBalanceDto.setComposAttr(ncountBalance.getBalanceItemCode());
				//圈存的券固定归属个人
				couponBalanceDto.setExtendAttre(MemberCateEnums.MEMBER_CATE_ALO.getCateCode());
				BeanCopier<TbFundBalance> nowCouponBalance = new BeanCopier<TbFundBalance>(couponBalanceDto, 
						new TbFundBalance(), createCopyOptions());
				
				//更新面额券资金，新建或者更新消费券资金 入库
				ncountBalance.insertOrUpdate();
				nowCouponBalance.copy().insertOrUpdate();
				
				//新建充值单据明细
				ncountDetail.insertOrUpdate();
			}
			
		}
		//创建充值单和充值单明细
		BeanCopier<Trans> trans = new BeanCopier<Trans>(transDetailComposeDto, 
				new Trans(), createCopyOptions());
		Trans transEntuty = trans.copy();
		transEntuty.insertOrUpdate();
		return transEntuty.getTransCode();
	}
	
	@Override
	@Transactional
	public boolean  returnNcount(NCountTransVo ncountTransVo) {
		//查询到转换单据和对应的转换明细，
		//通过明细查询到消费券交易流水记录和对应券资金
		//通过明细查询到面额券交易流水记录和对应券资金
		//对其进行回退
		//查询面额券账户
		List<NCountCouponVo> ncountVos = ncountTransVo.getCouponList();
		if (CollUtil.isNotEmpty(ncountVos)) {
			for (NCountCouponVo ncountVo : ncountVos) {
				
				//通常情况，每一个detail对应一个源消费券流水，即该查询结果为一条记录
				//但是，如果是一个面额券是有多条券资金记录组合生成的，则会存在多条detail
				List<TransDetail> details = this.findTransDetailByNcountId(ncountVo.getId());
				
				if (CollUtil.isEmpty(details)) {
					FundServiceExceptionGenerator.FundServiceException("无法查询到面额券"
							+ncountVo.getId()+"的上架明细,可能该面额券已经下架");
				}
				
				for (TransDetail detail : details) {
					//从detail中获取当前面额券id
					String ncountId = detail.getTransItemTargetCode();
					//从detail中获取当前detail关联的源消费券资金code
					String couponBalanceCode = detail.getTransItemSourceCode();
					
					//查询面额券的的资金记录，即使该面额券是多个消费券组合构成，单针对同一个面额券id，只有有一条对应的资金记录
					//即该查询始终返回一条数据
//					List<TbFundBalance> ncountBalances = this.acctBalanceService.findBalanceByNcountId(ncountId);
//					if (CollUtil.isEmpty(ncountBalances)) {
//						FundServiceExceptionGenerator.FundServiceException("找不到固定券"+ncountId+"的资金记录");
//					}
					
					FundBalanceDto ncountBalanceDto = this.acctBalanceBusiService.findSignleBalanceByBalanceItemCode(ncountId);
					if (ObjectUtil.isNull(ncountBalanceDto)) {
						FundServiceExceptionGenerator.FundServiceException("找不到固定券"+ncountId+"的资金记录");
					}
					BeanCopier<TbFundBalance> ncountBalanceDtoCopier = new BeanCopier<TbFundBalance>(ncountBalanceDto, 
							new TbFundBalance(), createCopyOptions());
					
					//查询detail对应的trans
					Trans trans = findTransByTransCode(detail.getTransCode());
					
					//获取面额券资金记录
					TbFundBalance ncountBalance = ncountBalanceDtoCopier.copy();
					
					//获取源消费券资金记录
//					TbFundBalance couponBalance = this.acctBalanceService.findAcctBalanceByCode(couponBalanceCode);
					FundBalanceDto couponBalanceDto = this.acctBalanceBusiService.findSignleBalanceByCode(couponBalanceCode);
					BeanCopier<TbFundBalance> couponBalanceDtoCopier = new BeanCopier<TbFundBalance>(couponBalanceDto, 
							new TbFundBalance(), createCopyOptions());
					TbFundBalance couponBalance = couponBalanceDtoCopier.copy();
					
					//获取当前detail的交易额，即需要退回的额度
					BigDecimal tradeAmount = detail.getPayAmount();
					
					//构造消费券退款流水
					FundTradeFlowDto flowCoupon = new FundTradeFlowDto(CommonConstant.STRING_BLANK, 
							new BigDecimal(1), trans.getTransBatchCode(),
							CommonConstant.BALANCE_BUSI_MODEL_NONE, 
							OrderType.ORDER_TYPE_NCOUNT_UNTRANSFER.getCateCode(), 
							null, TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode(), ActionType.ACTION_TYPE_IN.getCateCode(), 
							tradeAmount, CommonConstant.STRING_BLANK);
					
					//当前消费券余额应该加上退回的额度
					BigDecimal currentAmount = couponBalance.getBalanceAmount();
					BigDecimal tradeLastAmount = currentAmount.add(tradeAmount);
					couponBalance.setBalanceAmount(tradeLastAmount);
					
					flowCoupon.setTradePreAmount(currentAmount);
					flowCoupon.setTradeLastAmount(tradeLastAmount);
					flowCoupon.setTradeFlowCode(detail.getTransCode()+"-"+detail.getTransItemTargetCode());
					flowCoupon.setBalanceCode(couponBalance.getBalanceCode());
					
					//构造面额券退款流水
					FundTradeFlowDto flowNcount = new FundTradeFlowDto(CommonConstant.STRING_BLANK, 
							new BigDecimal(2), trans.getTransBatchCode(),
							CommonConstant.BALANCE_BUSI_MODEL_NONE, 
							OrderType.ORDER_TYPE_NCOUNT_UNTRANSFER.getCateCode(), 
							null, TradeCateEnums.TRADE_CATE_UNFREZZ.getCateCode(), 
							ActionType.ACTION_TYPE_OUT.getCateCode(), 
							tradeAmount, CommonConstant.STRING_BLANK);
	                
					flowNcount.setTradeFlowCode(detail.getTransCode()+"-"+detail.getTransItemTargetCode());
					flowNcount.setBalanceCode(ncountBalance.getBalanceCode());
					
					//面额券退款
					BigDecimal currentFrezzingAmount = ncountBalance.getFreezingBalance();
					BigDecimal tradeLastFrezzingAmount = currentFrezzingAmount.subtract(tradeAmount);
					ncountBalance.setFreezingBalance(tradeLastFrezzingAmount);
					flowNcount.setTradePreAmount(currentFrezzingAmount);
					flowNcount.setTradeLastAmount(tradeLastFrezzingAmount);
					
					
					//消费券资金流水入库
					couponBalance.insertOrUpdate();
					BeanCopier<TbFundTradeFlow> flowCouponCopier = new BeanCopier<TbFundTradeFlow>(flowCoupon, 
							new TbFundTradeFlow(), createCopyOptions());
					flowCouponCopier.copy().insertOrUpdate();
					
					//面额券资金流水入库
					ncountBalance.insertOrUpdate();
					BeanCopier<TbFundTradeFlow> flowNcountCopier = new BeanCopier<TbFundTradeFlow>(flowNcount, 
							new TbFundTradeFlow(), createCopyOptions());
					flowNcountCopier.copy().insertOrUpdate();
					
					//更新当前detail为已删除
					detail.setDelFlag(CommonConstant.STATUS_DEL);
					detail.updateById();
				}
			}
		}
		return true;
	}
	
	@Override
	public Page<TransDetailComposeDto> findTransInfo(Page<TransDetailComposeDto> pagedParams) {
		String transBatchCode = pagedParams.getCondition().getOrDefault("transBatchCode", "").toString();
		List<TransDetailComposeDto> result = this.baseMapper.findTransComponse(pagedParams, transBatchCode);
		pagedParams.setRecords(result);
		return pagedParams;
	}
	
	@Override
	public List<NCountCouponVo> findLeftBalanceAmount(NCountTransVo params){
		return this.baseMapper.findLeftBalanceAmount(params);
	}

	private String[] createBatchAndTradeCode() {
		SequenceNumber seq = new SequenceNumber(IdWorker.getId());
		String transBatchCode = CommonConstant.NCOUNT_COUPON_SN_PREFIX
				+seq.getTimestampAsString()
				+seq.getSeqNum();
		
		seq = new SequenceNumber(IdWorker.getId());
		String  transCode = seq.getBuinessCode(CommonConstant.NCOUNT_COUPON_DETAIL_SN_PREFIX, 
				CommonConstant.STRING_BLANK);
		seq = null;
		
		return new String[] {transBatchCode, transCode};
	}
	
	private FundBalanceDto findExsiseCouponBalance(TbFundAcct accountCoupon, FundBalanceDto couponBalance) {
		FundBalanceDto hadbalanceDto = null;
		
		try {
			
			return this.acctBalanceBusiService.findSignleBalanceByParams(couponBalance.getCompanyId(), 
					accountCoupon.getMemberId(), 
					accountCoupon.getMemberCate(), 
					accountCoupon.getAcctCate(), 
					couponBalance.getBalanceItemCode(), 
					couponBalance.getBusiModel(), 
					couponBalance.getCanTicket(), 
					couponBalance.getCanTransfer(), 
					couponBalance.getOwnerId(), null, null);

		}catch (Exception e) {
			if (e instanceof BaoxiaoException) {
				BaoxiaoException bex = (BaoxiaoException)e;
				if(bex.getCode()==904) {
					hadbalanceDto = null;
				}
			} else {
				throw e;
			}
		}
		return hadbalanceDto;
	}
	
	private Trans findTransByTransCode(String transCode) {
		EntityWrapper<Trans> wrapper = new EntityWrapper<>();
		wrapper.eq("trans_code", transCode);
		Trans trans = transService.selectOne(wrapper);
		if (null == trans) {
			FundServiceExceptionGenerator.FundServiceException("无法查询到面额券上架单据，交易单据编码"+transCode);
		}
		return trans;
	}
	
	private List<TransDetail> findTransDetailByNcountId(String ncountCouponId) {
		EntityWrapper<TransDetail> wrapper = new EntityWrapper<>();
		wrapper.eq("del_flag", "0");
		wrapper.eq("trans_item_target_code", ncountCouponId);
		List<TransDetail> detail = transDetailService.selectList(wrapper);
		if (null == detail) {
			FundServiceExceptionGenerator.FundServiceException("无法查询到源消费券记录，面额券"+ncountCouponId);
		}
		return detail;
	}
	
	private FundTradeFlowDto createAdductTradeFlow(FundBalanceDto buyerBalance, BigDecimal tradeFlowOrder, String tradeBusiCode, 
			BigDecimal tradeAmount, String source) {
		String balanceCode = buyerBalance.getBalanceCode();
		
		FundTradeFlowDto flowDto = new FundTradeFlowDto("面额券充值-消费券资金入账", 
				tradeFlowOrder, 
				tradeBusiCode, 
				CommonConstant.BALANCE_BUSI_MODEL_NONE,
				OrderType.ORDER_TYPE_NCOUNT_RECHARGE.getCateCode(), 
				null, 
				TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode(), 
				ActionType.ACTION_TYPE_IN.getCateCode(), 
				tradeAmount, source);
		BigDecimal currentAmount = buyerBalance.getBalanceAmount();
		BigDecimal lastAmount = currentAmount.add(tradeAmount);
		
		flowDto.setBalanceCode(balanceCode);
		flowDto.setTradePreAmount(currentAmount);
		flowDto.setTradeLastAmount(currentAmount.add(tradeAmount));
		flowDto.setTradeFlowCode(CommonConstant.KEY_PERFIX_BALANCE_NO+IdWorker.getIdStr());
		buyerBalance.setBalanceAmount(lastAmount);
		return flowDto;
	}
	
	private FundTradeFlowDto createDecutTradeFlow(TbFundBalance salerBalance, BigDecimal tradeFlowOrder, String tradeBusiCode, 
			BigDecimal tradeAmount, String source) {
		
		String balanceCode = salerBalance.getBalanceCode();
		FundTradeFlowDto flowDto = null;
		
		flowDto = new FundTradeFlowDto("面额券充值-面额券资金扣除", 
				tradeFlowOrder, 
				tradeBusiCode, 
				CommonConstant.BALANCE_BUSI_MODEL_NONE,
				OrderType.ORDER_TYPE_NCOUNT_RECHARGE.getCateCode(), 
				null, 
				TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode(), 
				ActionType.ACTION_TYPE_OUT.getCateCode(), 
				tradeAmount, source);
		
		BigDecimal currentAmount = salerBalance.getBalanceAmount();
		BigDecimal lastAmount = currentAmount.subtract(tradeAmount);
		
		flowDto.setBalanceCode(balanceCode);
		flowDto.setTradePreAmount(currentAmount);
		flowDto.setTradeLastAmount(currentAmount.subtract(tradeAmount));
		flowDto.setTradeFlowCode(CommonConstant.KEY_PERFIX_BALANCE_NO+IdWorker.getIdStr());
		salerBalance.setBalanceAmount(lastAmount);
		return flowDto;
	}
	
	private Map<String, List<NCountCouponVo>> couponIdGroupVoMap(List<NCountCouponVo> couponList){
		Map<String, List<NCountCouponVo>> couponIdGroupVoMap = Maps.newHashMap();
		if (CollUtil.isNotEmpty(couponList)) {
			for (NCountCouponVo ncountCouponVo : couponList) {
				if (ncountCouponVo.getCount()<=0) {
					continue;
				}
				if (!couponIdGroupVoMap.containsKey(ncountCouponVo.getCouponId())) {
					List<NCountCouponVo> couponIdGroupVo = Lists.newArrayList();
					couponIdGroupVoMap.put(ncountCouponVo.getCouponId(), couponIdGroupVo);
				}
				couponIdGroupVoMap.get(ncountCouponVo.getCouponId()).add(ncountCouponVo);
			}
		}
		return couponIdGroupVoMap;
	}
	
	private List<SplitCouponVo> splitCouponVos(List<FundBalanceDto> fundBalanceDtos){
		List<SplitCouponVo> splitCouponVos = Lists.newArrayList();
		if (CollUtil.isNotEmpty(fundBalanceDtos)) {
			for (FundBalanceDto fundBalanceDto : fundBalanceDtos) {
				SplitCouponVo splitCouponVo = new SplitCouponVo(fundBalanceDto.getBalanceCode(), 
						fundBalanceDto.getBalanceAmount());
				splitCouponVos.add(splitCouponVo);
			}
		}
		return splitCouponVos;
	}
	
	private TransDetailComposeDto createTransDetail(TransDetailComposeDto transDetailComposeDto, 
			List<NcountItem> ncountItems, String couponId) {
		
		List<TransDetailDto> transDetails = Lists.newArrayList();
		Map<String, NCountCouponVo> nCountCouponVoMap = Maps.newHashMap();
		List<NCountCouponVo> nCountCouponVos = Lists.newArrayList();
		
		String transCode = transDetailComposeDto.getTransCode();
		BigDecimal transAmount = transDetailComposeDto.getTransAmount();
		
		for (NcountItem ncountItem : ncountItems) {
			TransDetailDto td = new TransDetailDto();
			td.setTransCode(transCode);
			td.setTransItemSourceCode(ncountItem.getBalanceFlag());
			td.setTransItemTargetCode(ncountItem.getNcountId());
			
			//面额券面额
			td.setTransAmount(ncountItem.getNcountAmount());
			//消费券使用额度，当某张面额券是多条消费券资金组合生成时，此字段记录当前消费券资金记录使用额度
			BigDecimal amount = ncountItem.getBalanceAmount();
			td.setPayAmount(amount);
			td.setCreator(UserUtils.getUser());
			td.setUpdator(UserUtils.getUser());
			transDetails.add(td);
			transAmount = transAmount.add(amount);
			if (!nCountCouponVoMap.containsKey(ncountItem.getNcountId())) {
				NCountCouponVo ncountVo = new NCountCouponVo();
				ncountVo.setCouponId(couponId);
				ncountVo.setId(ncountItem.getNcountId());
				ncountVo.setAmount(ncountItem.getNcountAmount());
				nCountCouponVos.add(ncountVo);
				nCountCouponVoMap.put(ncountItem.getNcountId(), ncountVo);
			}
		}
		transDetailComposeDto.setTransAmount(transAmount);
		transDetailComposeDto.setTransDetails(transDetails);
		transDetailComposeDto.setNCountCouponVos(nCountCouponVos);
		return transDetailComposeDto;
	}
	
	private void createNcountTransAndTradeFlow(String companyId, String memberId, String memberCate,List<TransDetailComposeDto> transDetailComposeDtos) {
		//1、记录交易明细
		//2、从对应资金扣除交易额，到面额券账户资金记录，冻结该记录
		if (CollUtil.isNotEmpty(transDetailComposeDtos)) {
			log.info("面额券上架-需记录流水交易列表：{}",
					JSON.toJSONString(transDetailComposeDtos));
			for (TransDetailComposeDto transDetailComposeDto : transDetailComposeDtos) {
				List<FundBalanceDto> fundBalanceDtos = transDetailComposeDto.getFundBalanceDtos();
				
				Map<String, FundBalanceDto> mapedFundBalanceDto = Maps.newHashMap();
				if (CollUtil.isNotEmpty(fundBalanceDtos)) {
					for (FundBalanceDto fundBalanceDto : fundBalanceDtos) {
						String balanceCode = fundBalanceDto.getBalanceCode();
						if (!mapedFundBalanceDto.containsKey(balanceCode)) {
							mapedFundBalanceDto.put(balanceCode, fundBalanceDto);
						}
					}
				}
				
				log.info("面额券上架-mapedFundBalanceDto：{}",
						JSON.toJSONString(mapedFundBalanceDto));
				
				List<TransDetailDto> transDetails = transDetailComposeDto.getTransDetails();
				if (CollUtil.isNotEmpty(transDetails)) {
					int i=1;
//					for (Map.Entry<String, List<TransDetailDto>> transDetailDto : 
//						groupByItemTargetCode.entrySet()) {
//						//需要生成的面额券
//						String ncountNumber = transDetailDto.getKey();
//						
//						//生成该面额券所需的消费券列表
//						List<TransDetailDto> transDetailDtos = transDetailDto.getValue();
//						
//						if (CollUtil.isNotEmpty(transDetailDtos)) {
//							
//						}
//						
//					}
					for (TransDetailDto transDetailDto : transDetails) {
						
						TbFundTradeFlow flowCoupon = new TbFundTradeFlow();
						
//						TbFundBalance fundBalanceCoupon = 
//								acctBalanceService.findAcctBalanceByCode(transDetailDto.getTransItemSourceCode());
						
						FundBalanceDto fundBalanceCouponDto = this.acctBalanceBusiService
								.findSignleBalanceByCode(transDetailDto.getTransItemSourceCode());
						
						BeanCopier<TbFundBalance> fundBalanceCouponDtoCopier = new BeanCopier<TbFundBalance>(fundBalanceCouponDto, 
								new TbFundBalance(), createCopyOptions());
						TbFundBalance fundBalanceCoupon = fundBalanceCouponDtoCopier.copy();
						
						flowCoupon.setBalanceCode(transDetailDto.getTransItemSourceCode());
						flowCoupon.setTradeBusiCode(transDetailComposeDto.getTransBatchCode());
						flowCoupon.setTradeFlowCode(transDetailDto.getTransCode());
						flowCoupon.setTradeFlowOrder(new BigDecimal(i));
						
						flowCoupon.setTradeBusiCate(OrderType.ORDER_TYPE_NCOUNT_TRANSFER.getCateCode());
						flowCoupon.setTransBusiCateName(OrderType.ORDER_TYPE_NCOUNT_TRANSFER.getCateName());
						flowCoupon.setTradeCate(TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode());
						flowCoupon.setTransCateName(TradeCateEnums.TRADE_CATE_DEDUCT.getCateName());
						flowCoupon.setTransActCate(ActionType.ACTION_TYPE_OUT.getCateCode());
						flowCoupon.setTransActName(ActionType.ACTION_TYPE_OUT.getCateName());
						
						flowCoupon.setTradeAmount(transDetailDto.getPayAmount());
						
						BigDecimal currentAmount = (fundBalanceCoupon.getBalanceAmount())
								.subtract(transDetailDto.getPayAmount());
						
						flowCoupon.setTradePreAmount(fundBalanceCoupon.getBalanceAmount());
						flowCoupon.setTradeLastAmount(currentAmount);
						fundBalanceCoupon.setBalanceAmount(currentAmount);
						
						flowCoupon.insert();
						fundBalanceCoupon.insertOrUpdate();
						
						i++;
						
//						TbFundAcct acctount = this.acctBalanceBusiService.findFundAcct(memberId, 
//								memberCate+AcctCateEnums.ACCT_CATE_NCOUNT_COUPON.getCateCode());
						TbFundAcct acctount = this.acctBalanceBusiService.findFundAcct(memberId, 
								AcctCateEnums.ACCT_CATE_NCOUNT_COUPON.getCateCode());
						
						String ncountId = transDetailDto.getTransItemTargetCode();
						
//						List<TbFundBalance> ncountBalances = this.acctBalanceService.findBalanceByNcountId(ncountId);
						
						FundBalanceDto ncountBalanceDto = this.acctBalanceBusiService
								.findSignleBalanceByBalanceItemCode(ncountId);

						log.info("面额券上架-ncountBalanceDto：{}",
								JSON.toJSONString(ncountBalanceDto));

						
						TbFundBalance ncountBalance = null;
						FundBalanceDto fundBalanceDto = null;
						
						if (ObjectUtil.isNotNull(ncountBalanceDto)) {
							BeanCopier<TbFundBalance> ncountBalanceDtoCopier = new BeanCopier<TbFundBalance>(ncountBalanceDto, 
									new TbFundBalance(), createCopyOptions());
							ncountBalance = ncountBalanceDtoCopier.copy();
						}
						
						if (null != ncountBalance) {
							BeanCopier<FundBalanceDto> copierb = new BeanCopier<FundBalanceDto>(ncountBalance, 
									new FundBalanceDto(), createCopyOptions());
							fundBalanceDto = copierb.copy();
							
							fundBalanceDto.setFreezingBalance(fundBalanceDto.getFreezingBalance()
									.add(transDetailDto.getPayAmount()));
							
							if (StrUtil.isNotBlank(fundBalanceDto.getComposAttr())) {
								fundBalanceDto.setComposAttr(fundBalanceDto.getComposAttr()+","
											+transDetailDto.getTransItemSourceCode());
							}
						} else {
							fundBalanceDto = new FundBalanceDto(acctount.getAcctInstNo(), 
									CommonConstant.STRING_BLANK, 
									ncountId, 
									CommonConstant.BALANCE_BUSI_MODEL_NONE, 
									CommonConstant.NO_AMOUNT, 
									transDetailDto.getPayAmount(), 
									CommonConstant.NO_AMOUNT, 
									CommonConstant.STRING_BLANK, 
									CommonConstant.STRING_BLANK, 
									null,
									null, 
									companyId, memberId, CommonConstant.STRING_BLANK);
							fundBalanceDto.setComposAttr(transDetailDto.getTransItemSourceCode());
							fundBalanceDto.setExtendAttre(memberCate);
						}
						
						TbFundTradeFlow flowNcount = new TbFundTradeFlow();
						flowNcount.setBalanceCode(fundBalanceDto.getBalanceCode());
						flowNcount.setTradeBusiCode(transDetailComposeDto.getTransBatchCode());
						flowNcount.setTradeFlowCode(transDetailDto.getTransCode());
						flowNcount.setTradeFlowOrder(new BigDecimal(i));
						
						flowNcount.setTradeBusiCate(OrderType.ORDER_TYPE_NCOUNT_TRANSFER.getCateCode());
						flowNcount.setTransBusiCateName(OrderType.ORDER_TYPE_NCOUNT_TRANSFER.getCateName());
						flowNcount.setTradeCate(TradeCateEnums.TRADE_CATE_FREZZ.getCateCode());
						flowNcount.setTransCateName(TradeCateEnums.TRADE_CATE_FREZZ.getCateName());
						flowNcount.setTransActCate(ActionType.ACTION_TYPE_OUT.getCateCode());
						flowNcount.setTransActName(ActionType.ACTION_TYPE_OUT.getCateName());
						
						flowNcount.setTradePreAmount(fundBalanceDto.getBalanceAmount());
						flowNcount.setTradeAmount(transDetailDto.getPayAmount());
						flowNcount.setTradeLastAmount(fundBalanceDto.getBalanceAmount()
								.add(transDetailDto.getPayAmount()));
						i++;
						
						flowNcount.insert();
						
						BeanCopier<TbFundBalance> copierb = new BeanCopier<TbFundBalance>(fundBalanceDto, 
								new TbFundBalance(), createCopyOptions());
						copierb.copy().insertOrUpdate();
						
						BeanCopier<TransDetail> copierd = new BeanCopier<TransDetail>(transDetailDto, 
								new TransDetail(), createCopyOptions());
						copierd.copy().insert();
						
					}
				}
				BeanCopier<Trans> copiert = new BeanCopier<Trans>(transDetailComposeDto, 
						new Trans(), createCopyOptions());
				copiert.copy().insert();
			}
		}
	}
	
	/*
	 * 面额权交易主处理
	 */
	private void buyCouponInfo(String memberId, String memberCate, String companyId,
			List<NCountCouponVo> ncountCouponVos) {
		//查询买方面额券账户
		TbFundAcct account = this.acctBalanceBusiService.findFundAcct(memberId, 
				//memberCate+
				AcctCateEnums.ACCT_CATE_NCOUNT_COUPON.getCateCode());
		
		for (NCountCouponVo ncountCouponVo : ncountCouponVos) {

			FundBalanceDto salerFundBalanceDto = this.acctBalanceBusiService
					.findSignleBalanceByBalanceItemCode(ncountCouponVo.getId());
			
			if (ObjectUtil.isNull(salerFundBalanceDto)) {
				FundServiceExceptionGenerator.FundServiceException("无法查询到面额券["+ncountCouponVo.getId()+"]的资金记录");
			}
			BeanCopier<TbFundBalance> salerFundBalanceDtoCopier = new BeanCopier<TbFundBalance>(salerFundBalanceDto, 
					new TbFundBalance(), createCopyOptions());
			//卖方面额券资金记录
			TbFundBalance salerFundBalance = salerFundBalanceDtoCopier.copy();
			if (salerFundBalance.getFreezingBalance().compareTo(CommonConstant.NO_AMOUNT)<=0) {
				FundServiceExceptionGenerator.FundServiceException("面额券["+ncountCouponVo.getId()+"]的额度低于或等于零，无法进行购买");
			}
			
			//拷贝卖方面额券的金记录，作为买方的面额券购买资金记录
			BeanCopier<TbFundBalance> copyerNcountBalance = new BeanCopier<TbFundBalance>(salerFundBalance, 
					new TbFundBalance(), createCopyOptions());
			TbFundBalance buyerNcountBalance = copyerNcountBalance.copy();
			
			//更改买方面额券归属账户，生成新的资金编码，置空资金记录主键
			buyerNcountBalance.setAcctInstNo(account.getAcctInstNo());
			buyerNcountBalance.setBalanceCode(CommonConstant.KEY_PERFIX_BALANCE_NO+IdWorker.getIdStr());
			buyerNcountBalance.setId(null);
			
			//生成交易流水业务编号
			String tradeBusiCode = CommonConstant.COUPON_NCOUNT_ORDER_SN_PERFIX+IdWorker.getIdStr();
			//获取交易额
			BigDecimal tradeAmount = salerFundBalance.getFreezingBalance();
			
			//构建卖方解冻交易流水
			FundTradeFlowDto  unFrezzSalerFlowDto = this.createUnFrezzDecutTradeFlow(salerFundBalance, new BigDecimal(1), 
					tradeBusiCode, tradeAmount, CommonConstant.TRANS_CHANNEL_ZHJ, false);
			//构建卖方扣除交易流水
			FundTradeFlowDto decutSalerFlowDto = this.createUnFrezzDecutTradeFlow(salerFundBalance, new BigDecimal(2), 
					tradeBusiCode, tradeAmount, CommonConstant.TRANS_CHANNEL_ZHJ, true);
			
			//构建买方入账流水
			FundTradeFlowDto addcutBuyerFlowDto = this.createAddTradeFlow(buyerNcountBalance, new BigDecimal(3), tradeBusiCode, tradeAmount, 
					CommonConstant.TRANS_CHANNEL_ZHJ);
	
			//资金流水和资金记录入库
			BeanCopier<TbFundTradeFlow> copyerUnFrezzSalerFlowDto = new BeanCopier<TbFundTradeFlow>(unFrezzSalerFlowDto, 
					new TbFundTradeFlow(), createCopyOptions());
			BeanCopier<TbFundTradeFlow> copyerDecutSalerFlowDto = new BeanCopier<TbFundTradeFlow>(decutSalerFlowDto, 
					new TbFundTradeFlow(), createCopyOptions());
			BeanCopier<TbFundTradeFlow> copyerAddcutBuyerFlowDto = new BeanCopier<TbFundTradeFlow>(addcutBuyerFlowDto, 
					new TbFundTradeFlow(), createCopyOptions());
			
			copyerUnFrezzSalerFlowDto.copy().insertOrUpdate();
			copyerDecutSalerFlowDto.copy().insertOrUpdate();
			copyerAddcutBuyerFlowDto.copy().insertOrUpdate();
			
			salerFundBalance.insertOrUpdate();
			buyerNcountBalance.insertOrUpdate();
			
		}
	}
	
	/*
	 * 创建卖方解冻和扣除资金流水，
	 * FundBalanceDto salerBalance 卖方资金编码
	 * BigDecimal tradeFlowOrder 流水序号
	 * String tradeBusiCode 业务交易编号
	 * BigDecimal tradeAmount 交易金额
	 * String source 来源
	 * boolean decut 扣除还是解冻
	 */
	private FundTradeFlowDto createUnFrezzDecutTradeFlow(TbFundBalance salerBalance, BigDecimal tradeFlowOrder, String tradeBusiCode, 
			BigDecimal tradeAmount, String source, boolean decut) {
		
		String tradeFlowCode = CommonConstant.KEY_PERFIX_TRADEFLOW_NO+IdWorker.getIdStr()+"-"+salerBalance.getBalanceItemCode();
		String balanceCode = salerBalance.getBalanceCode();
		FundTradeFlowDto flowDto = null;
		
		if (decut) {
			flowDto = new FundTradeFlowDto("面额券购买-卖方扣除", 
					tradeFlowOrder, 
					tradeBusiCode, 
					CommonConstant.BALANCE_BUSI_MODEL_NONE,
					OrderType.ORDER_TYPE_NCOUNT_BUY.getCateCode(), 
					null, 
					TradeCateEnums.TRADE_CATE_DEDUCT.getCateCode(), 
					ActionType.ACTION_TYPE_OUT.getCateCode(), 
					tradeAmount, source);
			
			flowDto.setBalanceCode(balanceCode);
			flowDto.setTradePreAmount(tradeAmount);
			flowDto.setTradeLastAmount(CommonConstant.NO_AMOUNT);
			salerBalance.setBalanceAmount(CommonConstant.NO_AMOUNT);
			
		} else {
			flowDto = new FundTradeFlowDto("面额券购买-卖方解冻", 
					tradeFlowOrder, 
					tradeBusiCode, 
					CommonConstant.BALANCE_BUSI_MODEL_NONE,
					OrderType.ORDER_TYPE_NCOUNT_BUY.getCateCode(), 
					null, 
					TradeCateEnums.TRADE_CATE_UNFREZZ.getCateCode(), 
					ActionType.ACTION_TYPE_IN.getCateCode(), 
					tradeAmount, source);
			
			flowDto.setBalanceCode(balanceCode);
			flowDto.setTradePreAmount(CommonConstant.NO_AMOUNT);
			flowDto.setTradeLastAmount(tradeAmount);
			salerBalance.setBalanceAmount(tradeAmount);
			salerBalance.setFreezingBalance(CommonConstant.NO_AMOUNT);
		}
		
		flowDto.setTradeFlowCode(tradeFlowCode);
		return flowDto;
	}
	
	/*
	 * 创建买方资金流水
	 * String balanceCode 买方资金编码
	 * BigDecimal tradeFlowOrder 流水序号
	 * String tradeBusiCode 业务交易编号
	 * BigDecimal tradeAmount 交易金额
	 * String source 来源
	 * 
	 */
	private FundTradeFlowDto createAddTradeFlow(TbFundBalance buyerBalance, BigDecimal tradeFlowOrder, String tradeBusiCode, 
			BigDecimal tradeAmount, String source) {
		String balanceCode = buyerBalance.getBalanceCode();
		
		FundTradeFlowDto flowDto = new FundTradeFlowDto("面额券购买-买方入账", 
				tradeFlowOrder, 
				tradeBusiCode, 
				CommonConstant.BALANCE_BUSI_MODEL_NONE,
				OrderType.ORDER_TYPE_NCOUNT_BUY.getCateCode(), 
				null, 
				TradeCateEnums.TRADE_CATE_ADDUCT.getCateCode(), 
				ActionType.ACTION_TYPE_IN.getCateCode(), 
				tradeAmount, source);
		
		flowDto.setBalanceCode(balanceCode);
		flowDto.setTradePreAmount(CommonConstant.NO_AMOUNT);
		flowDto.setTradeLastAmount(tradeAmount);
		flowDto.setTradeFlowCode(CommonConstant.KEY_PERFIX_TRADEFLOW_NO+IdWorker.getIdStr()+"-"+buyerBalance.getBalanceItemCode());
		buyerBalance.setBalanceAmount(tradeAmount);
		buyerBalance.setFreezingBalance(CommonConstant.NO_AMOUNT);
		return flowDto;
	}
	
	private CopyOptions createCopyOptions() {
		if (null == this.options) {
			this.options = new CopyOptions();
			this.options.setIgnoreProperties("create_time","updated_time");
		}
		return this.options;
	}
	
	public NCountTransVo selectNoCouponByConditionsPage(Query<FundBalanceDto> query) {
		String memberId = query.getCondition()
				.getOrDefault("memberId", CommonConstant.STRING_BLANK).toString();
		BalanceSearchParams params = new BalanceSearchParams();
		params.addMemberId(memberId);
		BigDecimal total = this.acctBalanceBusiService.selectTotalNoCoupon(params);
		Page<FundBalanceDto> records = this.acctBalanceBusiService.selectNoCouponByConditionsPage(params, query);
		NCountTransVo result = new NCountTransVo();
		result.setTotalAmount(total);
		result.setPagedNoCouponBalance(records);
		return result;
	}
}
