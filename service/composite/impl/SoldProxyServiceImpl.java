/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  SoldProxyServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.composite.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   Mar 7, 2019 10:38:10 AM   
 * @version V1.0 
 * @Copyright: 2019 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
package com.taolue.baoxiao.fund.service.composite.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.MqQueueConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BalanceSearchTypeEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyRoleType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.SystemNameEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.dto.AssignCouponDto;
import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.common.enums.Sequence;
import com.taolue.baoxiao.common.util.Exceptions;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.SequenceNumber;
import com.taolue.baoxiao.common.util.param.ParamCheckUtils;
import com.taolue.baoxiao.fund.api.dto.BalanceSearchParams;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.FundTradeFlowDto;
import com.taolue.baoxiao.fund.api.vo.OrderVo;
import com.taolue.baoxiao.fund.common.exception.FundServiceException;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.common.model.OrderApply;
import com.taolue.baoxiao.fund.entity.BusinessApplyBalance;
import com.taolue.baoxiao.fund.entity.BusinessApplyCharges;
import com.taolue.baoxiao.fund.entity.BusinessApplyParty;
import com.taolue.baoxiao.fund.entity.Order;
import com.taolue.baoxiao.fund.entity.TbFundAcct;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.service.ITbFundBalanceService;
import com.taolue.baoxiao.fund.service.ITbFundTradeFlowService;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceService;
import com.taolue.baoxiao.fund.service.composite.IBusinessApplyBusiService;
import com.taolue.baoxiao.fund.service.composite.ISoldProxyService;

import com.taolue.baoxiao.fund.service.composite.IWithdrawService;
import com.taolue.baoxiao.fund.service.composite.InviteFriendsService;
import com.taolue.baoxiao.fund.service.remote.IRefactorMemberServiceFactory;
import com.taolue.member.api.vo.BackCashVo;
import com.taolue.member.api.vo.MemberSubstituteSaleConfigVo;

import cn.hutool.core.bean.copier.BeanCopier;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Direction;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ClassName: SoldProxyServiceImpl </br>
 * <p>
 * Description:TODO(这里用一句话描述这个类的作用)</br>
 * <p>
 * Author: shilei</br>
 * <p>
 * date: Mar 7, 2019 10:38:10 AM </br>
 * 
 * @Copyright: 2019 www.jia-fu.cn Inc. All rights reserved.
 *             注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Slf4j
@Service
public class SoldProxyServiceImpl extends SelfProxyServiceImpl<SoldProxyServiceImpl> implements ISoldProxyService {

	@Autowired
	private IRefactorMemberServiceFactory refactorMemberServiceFactory;

	@Autowired
	private IAcctBalanceBusiService acctBalanceBusiService;

	@Autowired
	private IBusinessApplyBusiService businessApplyBusiService;

	@Autowired
	private ITbFundBalanceService fundBalanceService;

	@Autowired
	private ITbFundTradeFlowService fundTradeFlowService;

	@Autowired
	protected IAcctBalanceService acctBalanceService;
	
	@Autowired
	private IWithdrawService withdrawService;
	
	@Autowired
	private InviteFriendsService inviteFriendsService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Value("${application.model}")
	private String applicationModel = "";

	@Value("${application.withdraw.auto}")
	private boolean withdrawAuto = true;

	@Autowired
	private  RedissonClient redissonClient;
	
	public void doTask(AssignDto assignDto) {
		
		log.info("application model = " + this.applicationModel);

		String orderCode = ParamCheckUtils.getString(assignDto.getBusiOrderNo(), CommonConstant.STRING_BLANK);

		String mainType = ParamCheckUtils.getString(assignDto.getOrderType(), OrderType.TASK.getCateCode());

		String subType = ParamCheckUtils.getString(assignDto.getTradeCate(), OrderType.SOLDPROXY.getCateCode());

		String companyId = ParamCheckUtils.getString(assignDto.getCompanyId(), CommonConstant.DEFAULT_ACCT_MEMBER_ID);

		String sourceCode = ParamCheckUtils.getString(assignDto.getSource(), CommonConstant.STRING_BLANK);

		String memberId = ParamCheckUtils.getString(assignDto.getMemberId(), CommonConstant.STRING_BLANK);

		List<String> memberIds = assignDto.getMemberIds();

		if (CollUtil.isEmpty(memberIds)) {
			memberIds = Lists.newArrayList();
		}
		
		if (StrUtil.isNotBlank(memberId)) {
			memberIds.add(memberId);
			mainType = OrderType.MTASK.getCateCode();
		}
		
		RLock lock = redissonClient.getLock("newSoldProxy");
		boolean flag;
		Order applyMain = null;
		List<OrderVo> orderVo=null;
		try {
			log.info("线程{}开始尝试获取锁", Thread.currentThread().getName());
			flag = lock.tryLock(10, 5, TimeUnit.SECONDS);
			log.info("线程{}尝试获取锁结果{}", Thread.currentThread().getName(), flag);
			if (flag) {
				try {
					// 查询本月是否有未完成的自动代卖单据
					boolean hasProcessingOrder = this.businessApplyBusiService.getExsitApplyMain(mainType, subType, 
							memberId, BusinessApplyStatus.COMPLETED.getCateCode(), 
							DateUtil.beginOfMonth(new Date()), new Date());
					
					// 如果本月有未完成的自动发起的未完成的智能代卖单据并且当前是生产环境，则拒绝再次启动智能带你任务
//					if (hasProcessingOrder && StrUtil.isNotBlank(this.applicationModel)
//							&& StrUtil.equalsIgnoreCase(this.applicationModel, "release")) {
					if (hasProcessingOrder) {
						String message = "本月存在尚未完成的智能代卖任务，请核查后重新发起";
						if (StrUtil.isNotBlank(memberId)) {
							message = StrFormatter.format("会员{}当前存在未完成的手动代卖任务，本次手动代卖不成功", memberId);
						}
						FundServiceExceptionGenerator.FundServiceException(504, message);
					}
					orderVo=this.businessApplyBusiService.getOrderService().queryUnCompleteMember();
					log.info("查询需要过滤的用户数据：{}",JSON.toJSONString(orderVo));
					// 创建自动代卖任务主单据
					applyMain = this.businessApplyBusiService.createApplyMain(Sequence.SOLDPROXY_APPLY_MAIN.getPerfix(),
							orderCode, OrderType.getOrderType(mainType), OrderType.getOrderType(subType),
							BusinessApplyStatus.CREATED, companyId, sourceCode);
					if (applyMain != null) {
						applyMain.insertOrUpdate();
					}
				} finally {
					log.info("线程{}释放锁", Thread.currentThread().getName());
					lock.unlock();
				}
			}
		} catch (InterruptedException ei) {
			log.error(Exceptions.getStackTraceAsString(ei));
		}
		
		// 查询本月是否有未完成的自动代卖单据
//		boolean hasProcessingOrder = this.businessApplyBusiService.getExsitApplyMain(mainType, subType, 
//				memberId, BusinessApplyStatus.COMPLETED.getCateCode(), 
//				DateUtil.beginOfMonth(new Date()), new Date());
//		
//		// 如果本月有未完成的自动发起的未完成的智能代卖单据并且当前是生产环境，则拒绝再次启动智能带你任务
//		if (hasProcessingOrder && StrUtil.isNotBlank(this.applicationModel)
//				&& StrUtil.equalsIgnoreCase(this.applicationModel, "prod")) {
//			FundServiceExceptionGenerator.FundServiceException(504, "本月存在尚未完成的智能代卖任务，请核查后重新发起");
//		}
//
//		// 创建自动代卖任务主单据
//		Order applyMain = this.businessApplyBusiService.createApplyMain(Sequence.SOLDPROXY_APPLY_MAIN.getPerfix(),
//				orderCode, OrderType.getOrderType(mainType), OrderType.getOrderType(subType),
//				BusinessApplyStatus.CREATED, companyId, sourceCode);

		OrderApply orderApply = null;

		try {
			// 获取所有开启代卖的会员当前所需消费券的类型以及该类型消费券所需额度
			orderApply = getRequiredCouponAmounts(applyMain, memberIds,orderVo);

			if (null != orderApply) {
				// 触发智能代卖业务到总部
				log.info("触发总部智能代卖-接口参数{}",JSON.toJSONString(orderApply));
				fireMessage(orderApply);
				applyMain.setStatus(BusinessApplyStatus.PROCESS.getCateCode());
				applyMain.setRemark("待发送总部");
			}
		} catch (Exception e) {
			log.error(Exceptions.getStackTraceAsString(e));
			applyMain.setStatus(BusinessApplyStatus.FAULT.getCateCode());
			applyMain.setRemark(e.getMessage());
			FundServiceExceptionGenerator.FundServiceException(503, e.getMessage());
		} finally {
			applyMain.insertOrUpdate();
		}
	}

	public boolean rechargeRound(AssignDto assignDto) {

		
		BigDecimal amount = assignDto.getOrderAmount();
		if (ObjectUtil.isNull(amount)) {
			amount = CommonConstant.NO_AMOUNT;
		}
		if (amount.compareTo(CommonConstant.NO_AMOUNT)<=0) {
			log.info("本次凑整总额为0，不进行记录");
			return true;
		}
		
		String busiOrderNo = assignDto.getBusiOrderNo();
		BusinessApplyCharges soldCharge = this.businessApplyBusiService.getApplyChargeByCode(busiOrderNo);

		List<BusinessApplyParty> applyPartys = this.businessApplyBusiService.getApplyPartyByApplyCodeRole(
				soldCharge.getApplyCode(), BusinessApplyRoleType.SOLDPROXY_TARGET_COMPANY.getCateCode());

		BusinessApplyBalance roundBalance = this.businessApplyBusiService.createApplyBalance(
				Sequence.SOLDPROXY_APPLY_BALANDCE.getPerfix(), soldCharge.getApplyCode(), soldCharge.getCode(),
				applyPartys.get(0).getCode(), BillItemSubCate.BILL_ITEM_SOLD_PROXY_ROUND_COUPON,
				soldCharge.getRelateCode(), // 在生成charge时已经企将业入账balanceCode写入了charge的RelateCode
				BusinessApplyStatus.COMPLETED, amount, CommonConstant.STRING_BLANK, CommonConstant.NO_AMOUNT,
				CommonConstant.NO_AMOUNT);
		roundBalance.insertOrUpdate();
		return true;
	}

	public boolean soldPorxySuccess(AssignDto assignDto) {
		log.info("代卖成功回调接口-汇款单据处理，处理参数{}", JSON.toJSONString(assignDto));
		
		// 智能代卖单号
		String busiOrderNo = assignDto.getBusiOrderNo();
		// 本次代卖所用凑整总额
		BigDecimal soldAmount = assignDto.getCouponAmount();
		
		// 代卖所得明细
		List<AssignCouponDto> soldResultBalances = assignDto.getCoupons();

		// 本次代卖单据
		BusinessApplyCharges soldCharge = this.businessApplyBusiService.getApplyChargeByCode(busiOrderNo);
		
		// 代卖主单据
		Order soldOrder = this.businessApplyBusiService.getApplyMainByCode(soldCharge.getApplyCode());

		try {
			log.info("to complateSoldProxyCharge={}", JSON.toJSONString(soldCharge));
			//处理中代卖单据
			if (BusinessApplyStatus.PROCESS.getCateCode().equals(soldCharge.getStatus())) {
				log.info("in complateSoldProxyCharge={}", JSON.toJSONString(soldCharge));
				this.getSelfProxy().complateSoldProxyCharge(soldOrder, soldCharge, soldResultBalances, soldAmount);
				soldCharge.setStatus(BusinessApplyStatus.COMPLETED.getCateCode());
				soldCharge.setRemark("代卖成功回调接口-汇款单据处理完成");
			}
			log.info("out complateSoldProxyCharge={}", JSON.toJSONString(soldCharge));
		} catch (Exception e) {
			log.error(Exceptions.getStackTraceAsString(e));
			soldCharge.setStatus(BusinessApplyStatus.PROCESS.getCateCode());
			soldCharge.setRemark("代卖成功回调接口-汇款单据处理出错，需重新处理");
			FundServiceExceptionGenerator.FundServiceException(503, 
					"代卖单据" + soldCharge.getCode() + "回款接口处理出错，需重新处理",e);
		} finally {
			soldCharge.insertOrUpdate();
		}

		// 处理全部代卖单据
		complateSoldProxyOrder(soldOrder);

		return true;
	}

	/**
	 * 
	 * <p>
	 * 名称:类SoldProxyServiceImpl中的findCanSoldProxyMembers方法</br>
	 * <p>
	 * 描述: 查询当前设定的自动代卖额度</br>
	 * <p>
	 * 作者: shilei</br>
	 * <p>
	 * 日期: Mar 7, 2019 11:14:39 AM</br>
	 * 
	 * @param memberIds
	 *            {@link List}{@literal <}{@link String}{@literal >}
	 *            指定需要查询的会员，如果为空则查询所有
	 * @return {@link List}{@literal <}{@link AssignDto}{@literal >} 自动代卖额度数据列表对象；
	 *         {@link AssignDto} 对象属性：
	 *         <ul>
	 *         <li>{@link AssignDto#memberId} 类型：{@link String} 会员编号</li>
	 *         <li>{@link AssignDto#orderAmount} 类型：{@link BigDecimal}
	 *         会员当前设置的自动代卖额度</li>
	 *         </ul>
	 *         该返回对象不可以为空；
	 * @throws FundServiceException
	 *             <ul>
	 *             <li>异常代码{@link int}->503；异常信息->查询当前自动代卖额度出现错误</li>
	 *             <li>异常代码{@link int}->504；异常信息->没有获取到自动代卖额度</li>
	 *             </ul>
	 */
	public List<AssignDto> findSoldProxyAmounts(List<String> memberIds) throws FundServiceException {
		// 调用第三方服务接口查询该数据
		List<AssignDto> soldProxyAmountList = Lists.newArrayList();
		try {
			// todo-调用第三方服务查询
			R<List<MemberSubstituteSaleConfigVo>> r = refactorMemberServiceFactory
					.getRefactorMemberSubstituteSaleConfigService().findSubstituteSaleConfigByMIds(memberIds);

			if (r != null && r.getCode() == R.SUCCESS) {
				// 转换为 List<AssignDto>
				soldProxyAmountList = r.getData().stream().map(balance -> new AssignDto(balance.getMemberId(),
						balance.getGuid(), balance.getSaleAmountMonth().subtract(balance.getSaledAmountMonth()), 
							CommonConstant.NO_AMOUNT))
						.collect(Collectors.toList());
			}
		} catch (Exception e) {
			FundServiceExceptionGenerator.FundServiceException(503, "查询当前自动代卖额度出现错误", e);
		}
		if (CollUtil.isEmpty(soldProxyAmountList)) {
			FundServiceExceptionGenerator.FundServiceException(504, "没有获取到自动代卖额度");
		}
		return soldProxyAmountList;
	}

	/**
	 * 
	 * <p>
	 * 名称:类SoldProxyServiceImpl中的findSoldingAndSoledProxyAmounts方法</br>
	 * <p>
	 * 描述: 按会员ID查询当前系统中处于代卖中和已经代卖完成的额度</br>
	 * <p>
	 * 作者: shilei</br>
	 * <p>
	 * 日期: Mar 11, 2019 3:03:08 PM</br>
	 * 
	 * @param memberAssginDtos
	 *            {@link List}{@literal <}{@link String}{@literal >} 查询参数
	 *            <ul>
	 *            <li>{@link AssignDto#memberId} 类型：{@link String} 会员编号</li>
	 *            <li>{@link AssignDto#guid} 类型：{@link String} 会员第三方编号</li>
	 *            </ul>
	 * @return {@link Map}{@literal <}{@link String},{@link AssignDto}{@literal >}
	 *         自动代卖额度数据{@link Map}对象；
	 *         <ul>
	 *         <li>{@link Map}的key->{@link String}对象，为会员id</li>
	 *         <li>{@link Map}的value->{@link AssignDto}对象，为该会员当前已用和使用中的代卖额度数据对象：其属性为</li>
	 *         <ul>
	 *         <li>{@link AssignDto#memberId} 类型：{@link String} 会员编号</li>
	 *         <li>{@link AssignDto#guid} 类型：{@link String} 会员第三方编号</li>
	 *         <li>{@link AssignDto#orderAmount} 类型：{@link BigDecimal}
	 *         会员已经使用的自动代卖额度</li>
	 *         <li>{@link AssignDto#couponAmount} 类型：{@link BigDecimal}
	 *         会员使用中的自动代卖额度</li>
	 *         </ul>
	 *         </ul>
	 *         该返回对象可以为空；表示没有任何已使用和使用中的自动代卖额度
	 * @throws FundServiceException
	 *             <ul>
	 *             <li>异常代码{@link int}->503；异常信息->查询当前已使用和使用中的自动代卖额度出现错误</li>
	 *             </ul>
	 */
	public Map<String, AssignDto> findSoldingAndSoledProxyAmounts(List<AssignDto> memberAssginDtos)
			throws FundServiceException {
		try {

			List<String> memberIds = memberAssginDtos.stream().map(AssignDto::getMemberId).collect(Collectors.toList());

			// 查询当月内所有角色为BalanceSearchTypeEnum.SOLD_PROXY_COUPON_PAY的资金记录
			BalanceSearchParams params = new BalanceSearchParams();
			params.setMemberIds(memberIds);
			params.setValidTime(DateUtil.beginOfMonth(new Date()));
			params.setExpireTime(new Date());
			params.setBalanceType(BalanceSearchTypeEnum.SOLD_PROXY_COUPON_PAY.getCode());

			// BusinessApplyBalance.ApplyPartyCode
			// 为memberid，BusinessApplyBalance.ApplyChargeCode为guid，
			// BusinessApplyBalance.Amount为额度
			List<BusinessApplyBalance> results = this.businessApplyBusiService.getBusinessApplyBalanceService()
					.selectSoldingAndSoldedAmount(params);

			// 有查询结果表示存在已经使用了代卖额度的记录
			if (CollUtil.isNotEmpty(results)) {

				// 获取状态为完成的资金记录
				List<BusinessApplyBalance> complateBalances = results.stream()
						.filter(balance -> balance.getStatus().equals(BusinessApplyStatus.COMPLETED.getCateCode()))
						.collect(Collectors.toList());

				// key=关联的memberid，value=AssignDto，AssignDto.orderAmount为已用额度
				Map<String, AssignDto> complatesMap = complateBalances.stream()
						.collect(Collectors.toMap(BusinessApplyBalance::getApplyPartyCode,
								balance -> new AssignDto(balance.getApplyPartyCode(), balance.getApplyChargeCode(),
										balance.getAmount(), CommonConstant.NO_AMOUNT)));

				// 获取状态为未完成的资金记录
				List<BusinessApplyBalance> processBalances = results.stream()
						.filter(balance -> !balance.getStatus().equals(BusinessApplyStatus.COMPLETED.getCateCode()))
						.collect(Collectors.toList());

				// key=关联的memberid，value=AssignDto，AssignDto.couponAmouht为使用中额度
				Map<String, AssignDto> processMap = processBalances.stream()
						.collect(Collectors.toMap(BusinessApplyBalance::getApplyPartyCode,
								balance -> new AssignDto(balance.getApplyPartyCode(), balance.getApplyChargeCode(),
										CommonConstant.NO_AMOUNT, balance.getAmount())));

				complatesMap.forEach((key, value) -> processMap.merge(key, value, (v1, v2) -> {
							v1.setOrderAmount(v2.getOrderAmount());
							return v1;
						}));
				
//				Map<String, AssignDto> resultMap = Maps.newHashMap();
//				
//				if (CollUtil.isNotEmpty(complatesMap)) {
//					for (Map.Entry<String, AssignDto> centry : complatesMap.entrySet()) {
//						String memberId = centry.getKey();
//						if (CollUtil.isNotEmpty(processMap)) {
//							// 将已使用额度AssignDto对象的orderAmount属性设置到对应的
//							// 使用中AssignDto对象的orderAmount属性上
//							if (processMap.containsKey(memberId)) {
//								processMap.get(memberId).setOrderAmount(centry.getValue().getOrderAmount());
//
//							} else {
//								// 不在使用中的额度对象map中，表示只有已使用额度，则将已使用额度的AssignDto
//								// 设置到用中的额度对象map中
//								processMap.put(memberId, centry.getValue());
//								// resultMap.put(memberId, centry.getValue());
//							}
//						}
//					}
//				}
				return processMap;
			}
		} catch (Exception e) {
			FundServiceExceptionGenerator.FundServiceException(503, "查询当前已使用和使用中的自动代卖额度出现错误", e);
		}

		return Maps.newHashMap();
	}

	/**
	 * 
	 * <p>
	 * 名称:类SoldProxyServiceImpl中的findCouponConfigScope方法</br>
	 * <p>
	 * 描述: 查询可以用于自动代卖功能的券的id的列表，如果为空，则表示可以使用全部券</br>
	 * <p>
	 * 作者: shilei</br>
	 * <p>
	 * 日期: Mar 11, 2019 3:35:04 PM</br>
	 * 
	 * @param ignore
	 *            {@link boolean} 是否忽略异常,如果为true，则该方法出现异常时默认返回空
	 * @return {@link List}{@literal <}{@link String}{@literal >} 可用于自动代卖的券的id列表；
	 *         该返回值，可以为空，表示不限制券的范围；
	 * @throws FundServiceException
	 *             <ul>
	 *             <li>异常代码{@link int}->503；异常信息->查询可用于自动代卖的券的范围出现错误</li>
	 *             </ul>
	 */
	public List<String> findCouponConfigScope(boolean ignore) {
		List<String> couponIds = Lists.newArrayList();
		try {
			// todo-查询可用券配置

		} catch (Exception e) {
			if (!ignore) {
				FundServiceExceptionGenerator.FundServiceException(503, "查询可用于自动代卖的券的范围出现错误", e);
			} else {
				couponIds = Lists.newArrayList();
			}
		}
		return couponIds;
	}

	/**
	 * <p>
	 * 名称:类SoldProxyServiceImpl中的getLeftCouponAmounts方法</br>
	 * <p>
	 * 描述: 查询当前指定会员范围下可转让且非易得多购买的消费券账户资金信息 若指定了消费券的范围还会限制消费券id，查询结果按权重和更新时间正序排列</br>
	 * <p>
	 * 作者: shilei</br>
	 * <p>
	 * 日期: Mar 11, 2019 3:26:59 PM</br>
	 * 
	 * @param memberIds
	 *            {@link List}{@literal <}{@link String}{@literal >} 会员编码列表对象；
	 * @return {@link List}{@literal <}{@link FundBalanceDto}{@literal >}
	 *         账户资金列表对象；若返回空则不发起代卖
	 * @throws FundServiceException
	 *             <ul>
	 *             <li>异常代码{@link int}->503；异常信息->查询可用于自动代卖的消费券资金数据出现错误</li>
	 *             <li>异常代码{@link int}->504；异常信息->没有获取到可用于自动代卖的消费券资金数据</li>
	 *             </ul>
	 */
	public List<FundBalanceDto> getLeftCouponAmounts(List<String> memberIds) throws FundServiceException {
		List<FundBalanceDto> leftCouponAmounts = Lists.newArrayList();
		try {

			BalanceSearchParams params = new BalanceSearchParams();
			params.setMemberIds(memberIds);
			params.setCanTransfer(CommonConstant.STATUS_YES);
			params.setBalanceType(BalanceSearchTypeEnum.ESCAPYIDEDUO.getCode());
			params.setAcctCate(AcctCateEnums.ACCT_CATE_COUPON.getCateCode());
			params.addMemberCate(MemberCateEnums.MEMBER_CATE_ALO.getCateCode(),
					MemberCateEnums.MEMBER_CATE_EMP.getCateCode());
			params.addOrderFields(new cn.hutool.db.sql.Order[] { new cn.hutool.db.sql.Order("b.weight", Direction.ASC),
					new cn.hutool.db.sql.Order("b.updated_time", Direction.ASC) });

			// todo-查询可用券配置
			List<String> couponIds = findCouponConfigScope(true);
			if (CollUtil.isNotEmpty(couponIds)) {
				params.addBalanceItemCode(couponIds.toArray(new String[0]));
			}

			// 每个会员名下可用券的总和
			leftCouponAmounts = this.acctBalanceBusiService.findBalancesByParams(params);

			// 过滤券总额不足100的数据
			if (CollUtil.isNotEmpty(leftCouponAmounts)) {
				// key->memberid,value->sum(balanceAmount)
				Map<String, Long> groupByMemberIdAmount = leftCouponAmounts.stream().collect(Collectors.groupingBy(
						o -> o.getMemberId(), Collectors.summingLong(o -> o.getBalanceAmount().longValue())));

				for (Map.Entry<String, Long> amountEntry : groupByMemberIdAmount.entrySet()) {
					String memberId = amountEntry.getKey();
					Long amount = amountEntry.getValue();
					if (amount < CommonConstant.LIMIT_SOLDPROXY_AMOUNT.longValue()) {
						log.warn("会员{}的消费券额度总额为{},不足一百，该会员将不予代卖！", memberId, amount / 1000);
						leftCouponAmounts.removeIf(dto -> {
							return dto.getMemberId().equals(memberId);
						});
					}
				}
			}
		} catch (Exception e) {
			FundServiceExceptionGenerator.FundServiceException(503, "查询可用于自动代卖的消费券资金数据出现错误", e);
		}
		if (CollUtil.isEmpty(leftCouponAmounts)) {
			FundServiceExceptionGenerator.FundServiceException(504, "没有获取到可用于自动代卖的消费券资金数据");
		}
		return leftCouponAmounts;
	}

	public Order createApplyMain(String orderCode, String mainType, String subType, String sourceCode,
			String companyId) {
		Order applyMain = new Order();
		applyMain.setOrderNo(
				new SequenceNumber(IdWorker.getId()).getLiteBuinessCode(Sequence.SOLDPROXY_APPLY_MAIN.getPerfix()));
		applyMain.setOrderCode(orderCode);
		applyMain.setCompanyId(companyId);
		applyMain.setCompanyName(CommonConstant.STRING_BLANK);
		applyMain.setMainType(mainType);
		applyMain.setSubType(subType);
		applyMain.setMainTypeName(OrderType.getOrderType(mainType).getCateName());
		applyMain.setSubTypeName(OrderType.getOrderType(subType).getCateName());
		applyMain.setStatus(BusinessApplyStatus.CREATED.getCateCode());
		applyMain.setSourceCode(sourceCode);
		return applyMain;
	}

	public BusinessApplyParty createApplyParty(String applyMainCode, String partyCode, String roleType) {
		BusinessApplyParty partyEntry = new BusinessApplyParty();
		partyEntry.setApplyCode(applyMainCode);
		partyEntry.setCode(
				new SequenceNumber(IdWorker.getId()).getLiteBuinessCode(Sequence.SOLDPROXY_APPLY_PARTY.getPerfix()));
		partyEntry.setPartyCode(partyCode);
		partyEntry.setRoleCode(roleType);
		partyEntry.setStatus(BusinessApplyStatus.CREATED.getCateCode());
		return partyEntry;
	}

	public BusinessApplyCharges createApplyCharge(String applyMainCode, String chargeType, String chargeItemType,
			String chargeItemCode, BigDecimal amount) {
		BusinessApplyCharges applyCharge = new BusinessApplyCharges();
		applyCharge.setApplyCode(applyMainCode);
		applyCharge.setCode(
				new SequenceNumber(IdWorker.getId()).getLiteBuinessCode(Sequence.SOLDPROXY_APPLY_CHARGE.getPerfix()));
		applyCharge.setChargesType(chargeType);
		applyCharge.setChargesItemType(chargeItemType);
		applyCharge.setChargesItemCode(chargeItemCode);
		applyCharge.setAmount(amount);
		applyCharge.setStatus(BusinessApplyStatus.CREATED.getCateCode());
		return applyCharge;
	}

	public BusinessApplyBalance createApplyBalance(String applyMainCode, String applyChargeCode, String applyPartyCode,
			String roleType, String balanceCode, BigDecimal amount) {
		BusinessApplyBalance applyBalance = new BusinessApplyBalance();
		applyBalance.setApplyCode(applyMainCode);
		applyBalance.setCode(
				new SequenceNumber(IdWorker.getId()).getLiteBuinessCode(Sequence.SOLDPROXY_APPLY_BALANDCE.getPerfix()));
		applyBalance.setApplyChargeCode(applyChargeCode);
		applyBalance.setApplyPartyCode(applyPartyCode);
		applyBalance.setBalanceCode(balanceCode);
		applyBalance.setRoleType(roleType);
		applyBalance.setAmount(amount);
		applyBalance.setStatus(BusinessApplyStatus.CREATED.getCateCode());
		return applyBalance;
	}

	/**
	 * 
	 * <p>
	 * 名称:类SoldProxyServiceImpl中的determineCurrentSoldPorxyAmount方法</br>
	 * <p>
	 * 描述: 通过当月设置的自动代卖额度数据，已使用和使用中的自动代卖额度数据， 计算出当前可用的代卖额度，且过滤掉无剩余代卖额度的数据</br>
	 * <p>
	 * 作者: shilei</br>
	 * <p>
	 * 日期: Mar 13, 2019 2:26:50 AM</br>
	 * 
	 * @param applyMainCode
	 *            {@link String} 单据编号
	 * @param currentSoldProxyAmountList
	 *            {@link List}{@literal <}{@link AssignDto}{@literal >}
	 *            会员当月设置的自动代卖额度数据
	 *            <ul>
	 *            <li>{@link AssignDto#memberId} 类型：{@link String} 会员编号</li>
	 *            <li>{@link AssignDto#guid} 类型：{@link String} 会员第三方编号</li>
	 *            <li>{@link AssignDto#orderAmount} 类型：{@link BigDecimal}
	 *            会员当月设置的自动代卖额度</li>
	 *            </ul>
	 * @return {@link List}{@literal <}{@link ListBusinessApplyParty}{@literal >}
	 *         单据关联的需要代卖的会员信息
	 */
	public List<BusinessApplyParty> determineCurrentSoldPorxyAmount(String applyMainCode,
			List<AssignDto> currentSoldProxyAmountList) {

		List<BusinessApplyParty> applyPartyList = Lists.newArrayList();

		
		// 获取所有开启自动代卖的会员当前已完成代卖额度和代卖中代卖额度
		Map<String, AssignDto> soldingAndSoledProxyAmountMap = findSoldingAndSoledProxyAmounts(
				currentSoldProxyAmountList);
		
		log.info("soldingAndSoledProxyAmountMap="+JSON.toJSONString(soldingAndSoledProxyAmountMap));
		for (AssignDto assignDto : currentSoldProxyAmountList) {
			String memberId = assignDto.getMemberId();
			boolean createdEntity = true;
			if (CollUtil.isNotEmpty(soldingAndSoledProxyAmountMap)) {
				AssignDto usedAmout = soldingAndSoledProxyAmountMap.get(memberId);
				if (null != usedAmout) {
					// 已使用额度
					BigDecimal soldedAmount = usedAmout.getOrderAmount() == null ? CommonConstant.NO_AMOUNT
							: usedAmout.getOrderAmount();
					// 使用中额度
					BigDecimal soldingAmount = usedAmout.getCouponAmount() == null ? CommonConstant.NO_AMOUNT
							: usedAmout.getCouponAmount();

					// 已使用+使用中 额度
					assignDto.setCouponAmount(soldedAmount.add(soldingAmount));
					// 额度已经用完则将创建applyParty实体的标识置为false
					if (assignDto.getOrderAmount().subtract(assignDto.getCouponAmount())
							.compareTo(CommonConstant.NO_AMOUNT) <= 0) {
						createdEntity = false;
					}
				}
			}
			// 需要创建applyParty实体
			if (createdEntity) {
				// 生成单据关联会员信息
				BusinessApplyParty peopleParty = this.businessApplyBusiService.createApplyParty(
						Sequence.SOLDPROXY_APPLY_PARTY.getPerfix(), applyMainCode,
						BusinessApplyRoleType.SOLDPROXY_SOURCE_PEOPLE, memberId, BusinessApplyStatus.CREATED,
						assignDto.getGuid());
				applyPartyList.add(peopleParty);
			}
		}
		// 过滤无剩余自动代卖额度的记录
		currentSoldProxyAmountList.removeIf(assignDto -> {
			boolean flag = assignDto.getOrderAmount().subtract(assignDto.getCouponAmount())
					.compareTo(CommonConstant.NO_AMOUNT) <= 0;
			if (flag) {
				log.warn("会员{}当前设置自动代卖额度为{},已使用额度为{}, 已经超额,该会员此次将不予代卖！", assignDto.getMemberId(),
						assignDto.getOrderAmount().longValue() / 1000, assignDto.getCouponAmount().longValue() / 1000);
			}
			return flag;
		});
		return applyPartyList;
	}

	/**
	 * 
	 * <p>
	 * 名称:类SoldProxyServiceImpl中的determineCouponAmount方法</br>
	 * <p>
	 * 描述: 根据涉及的资金账户列表数据，当月剩余可用代卖额度数据，计算出代卖的消费券的账户资金记录数据</br>
	 * <p>
	 * 作者: shilei</br>
	 * <p>
	 * 日期: Mar 13, 2019 2:38:52 AM</br>
	 * 
	 * @param leftCouponAmountLists
	 *            {@link List}{@literal <}{@link FundBalanceDto}{@literal >}
	 *            资金账户列表数据
	 * 
	 * @param currentSoldProxyAmountList
	 *            {@link List}{@literal <}{@link AssignDto}{@literal >} 当月剩余可用代卖额度数据
	 * @return {@link List}{@literal <}{@link FundBalanceDto}{@literal >}
	 *         本次任务所需的所有账户资金记录
	 */
	public List<FundBalanceDto> determineCouponAmount(List<FundBalanceDto> leftCouponAmountLists,
			List<AssignDto> currentSoldProxyAmountList) {

		// 获取key->memberid,value->memberid下所有消费券总额的map
		Map<String, Long> groupByMemberIdAmount = leftCouponAmountLists.stream().collect(Collectors
				.groupingBy(o -> o.getMemberId(), Collectors.summingLong(o -> o.getBalanceAmount().longValue())));

		log.info("determineCouponAmount.groupByMemberIdAmount = " + JSON.toJSONString(groupByMemberIdAmount));
		log.info("determineCouponAmount.currentSoldProxyAmountList = " + JSON.toJSONString(currentSoldProxyAmountList));
		// 最终使用券资金列表
		List<FundBalanceDto> allUsedBalances = Lists.newArrayList();

		for (AssignDto assignDto : currentSoldProxyAmountList) {
			log.info("determineCouponAmount.for.assignDto = " + JSON.toJSONString(assignDto));

			if (groupByMemberIdAmount.containsKey(assignDto.getMemberId())) {
				// 当前用户消费券总额
				Long couponAmount = groupByMemberIdAmount.get(assignDto.getMemberId());
				// 当前用户需要代卖额度
				BigDecimal soldProxyAmount = assignDto.getOrderAmount().subtract(assignDto.getCouponAmount());

				log.info("determineCouponAmount.for.couponAmount = " + JSON.toJSONString(couponAmount));
				log.info("determineCouponAmount.for.soldProxyAmount = " + JSON.toJSONString(soldProxyAmount));

				// 默认券额度<=可用代卖额度，使用全部的账户资金
				List<FundBalanceDto> filterList = leftCouponAmountLists.stream()
						.filter(balance -> balance.getMemberId().equals(assignDto.getMemberId()))
						.collect(Collectors.toList());

				log.info("determineCouponAmount.for.filterList = " + JSON.toJSONString(soldProxyAmount));
				List<FundBalanceDto> willUsedBalance = null;

				// 如果券额度>可用代卖额度，顺序使用账户资金直到使用资金额度=可用代卖额度
				if (couponAmount > soldProxyAmount.longValue()) {
					log.info("determineCouponAmount.for.couponAmount > soldProxyAmount = "
							+ JSON.toJSONString(soldProxyAmount));
					willUsedBalance = determineUsedBalance(soldProxyAmount, filterList);
				}

				if (CollUtil.isNotEmpty(willUsedBalance)) {
					allUsedBalances.addAll(willUsedBalance);
				} else {
					if (CollUtil.isNotEmpty(filterList)) {
						for (FundBalanceDto balanceDto : filterList) {
							balanceDto.setTicketBalance(balanceDto.getBalanceAmount());
							allUsedBalances.add(balanceDto);
						}
					}
				}
				log.info("determineCouponAmount.for.couponAmount > soldProxyAmount = "
						+ JSON.toJSONString(allUsedBalances));
			}
		}
		if (CollUtil.isEmpty(allUsedBalances)) {
			if (log.isDebugEnabled()) {
				log.debug("无法确定代卖使用的消费券，代卖数据: {}, 可用消费券数据: {}", JSON.toJSONString(currentSoldProxyAmountList),
						JSON.toJSONString(leftCouponAmountLists));
			}
			FundServiceExceptionGenerator.FundServiceException(503, "智能代卖业务-没有可以用于代卖的消费券，请检查后重试！！");
		}

		return allUsedBalances;
	}

	private void freezingBalance(Order applyMain, BusinessApplyCharges applyCharge, FundBalanceDto balanceDto,
			String tradeFlowCode, int seq) {
		BigDecimal canUsedAmount = balanceDto.getBalanceAmount();
		BigDecimal freezedAmount = balanceDto.getFreezingBalance();
		BigDecimal tradeAmount = balanceDto.getTicketBalance();
		balanceDto.setBalanceAmount(canUsedAmount.subtract(tradeAmount));
		balanceDto.setFreezingBalance(freezedAmount.add(tradeAmount));

		FundTradeFlowDto tradeFlowDto = this.createTradeFlowDto("智能代卖业务-来源消费券冻结", seq, balanceDto.getBusiModel(),
				applyCharge.getCode(), OrderType.getOrderType(applyMain.getSubType()), applyCharge.getChargesType(),
				TradeCateEnums.TRADE_CATE_FREZZ, ActionType.ACTION_TYPE_OUT, tradeAmount, applyMain.getSourceCode(),
				tradeFlowCode, canUsedAmount, balanceDto.getBalanceAmount(), balanceDto.getBalanceCode());
		tradeFlowDto.setStatus(CommonConstant.STATUS_TRADE_FLOW_FREZZING);

		BeanCopier<TbFundTradeFlow> copierFlow = new BeanCopier<TbFundTradeFlow>(tradeFlowDto, new TbFundTradeFlow(),
				new CopyOptions());
		copierFlow.copy().insertOrUpdate();
		BeanCopier<TbFundBalance> copierBalance = new BeanCopier<TbFundBalance>(balanceDto, new TbFundBalance(),
				new CopyOptions());
		copierBalance.copy().insertOrUpdate();
	}

	/**
	 * 
	 * <p>
	 * 名称:类SoldProxyServiceImpl中的determineUsedBalance方法</br>
	 * <p>
	 * 描述:(这里用一句话描述这个方法的作用)</br>
	 * <p>
	 * 作者: shilei</br>
	 * <p>
	 * 日期: Mar 13, 2019 2:43:33 AM</br>
	 * 
	 * @param soldProxyAmount
	 *            {@link BigDecimal} 某会员本次需要代卖的额度
	 * @param filterList
	 *            {@link List}{@literal <}{@link FundBalanceDto}{@literal >}
	 *            某会员当前所有的券的资金记录
	 * @return {@link List}{@literal <}{@link FundBalanceDto}{@literal >}
	 *         某会员满足代卖额度所使用的账户资金记录
	 */
	public List<FundBalanceDto> determineUsedBalance(BigDecimal soldProxyAmount, List<FundBalanceDto> filterList) {

		List<FundBalanceDto> willUsedBalance = Lists.newArrayList();
		// 扣款总额
		BigDecimal leftAmount = new BigDecimal(soldProxyAmount.longValue());

		boolean breakFlag = false;
		for (FundBalanceDto balanceDto : filterList) {

			BigDecimal balanceAmount = balanceDto.getBalanceAmount();
			leftAmount = leftAmount.subtract(balanceAmount);

			// 如果剩余额度小于等于0，则说明已经足额
			if (leftAmount.compareTo(CommonConstant.NO_AMOUNT) <= 0) {
				balanceDto.setTicketBalance(balanceAmount.add(leftAmount));
				breakFlag = true;
			} else {
				balanceDto.setTicketBalance(balanceAmount);
			}

			willUsedBalance.add(balanceDto);
			if (breakFlag) {

				break;
			}
		}
		return willUsedBalance;
	}

	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public void creatApplyChargesAndApplyBalances(OrderApply orderApply, Map<String, Long> groupByCouponIdAmount,
			Map<String, List<FundBalanceDto>> groupByCouponId) {

		List<BusinessApplyCharges> applyCharges = Lists.newArrayList();
		List<BusinessApplyBalance> applyBalances = Lists.newArrayList();
		Order applyMain = orderApply.getApplyMain();
		String applyMainCode = applyMain.getOrderNo();

		TbFundAcct fundAcct = this.acctBalanceBusiService.findFundAcct(applyMain.getCompanyId(),
				AcctCateEnums.ACCT_CATE_COUPON.getCateCode());

		log.info("智能代卖的getApplyPartys:{}",JSON.toJSONString(orderApply.getApplyPartys()));
		Map<String, String> applyPartyCode = orderApply.getApplyPartys().stream()
				.collect(Collectors.toMap(BusinessApplyParty::getPartyCode, BusinessApplyParty::getCode));

		for (Map.Entry<String, Long> amountEntry : groupByCouponIdAmount.entrySet()) {

			String couponId = amountEntry.getKey();
			BigDecimal amount = new BigDecimal(amountEntry.getValue());

			BusinessApplyCharges applyCharge = this.businessApplyBusiService.createApplyCharge(
					Sequence.SOLDPROXY_APPLY_CHARGE.getPerfix(), applyMainCode,
					BillItemSubCate.BILL_ITEM_SOLD_PROXY_COUPON, AcctCateEnums.ACCT_CATE_COUPON, couponId, amount,
					BusinessApplyStatus.CREATED, CommonConstant.STRING_BLANK, CommonConstant.STRING_BLANK);

			List<FundBalanceDto> balances = groupByCouponId.get(couponId);
			int seq = 1;
			String tradeFlowCode = CommonConstant.KEY_PERFIX_TRADEFLOW_NO + IdWorker.getIdStr();
			for (FundBalanceDto balance : balances) {

				applyBalances.add(this.businessApplyBusiService.createApplyBalance(
						Sequence.SOLDPROXY_APPLY_BALANDCE.getPerfix(), applyMainCode, applyCharge.getCode(),
						applyPartyCode.get(balance.getMemberId()), BillItemSubCate.BILL_ITEM_SOLD_PROXY_COUPON,
						balance.getBalanceCode(), BusinessApplyStatus.PROCESS, balance.getTicketBalance(),
						CommonConstant.STRING_BLANK, CommonConstant.NO_AMOUNT, CommonConstant.NO_AMOUNT));

				this.freezingBalance(applyMain, applyCharge, balance, tradeFlowCode, seq);
				seq++;
			}

			// 企业入账-入冻结金额
			FundBalanceDto fundBalanceDto = new FundBalanceDto(fundAcct.getAcctInstNo(),
					CommonConstant.KEY_PERFIX_BALANCE_NO + IdWorker.getIdStr(), couponId,
					BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), CommonConstant.NO_AMOUNT, applyCharge.getAmount(),
					CommonConstant.NO_AMOUNT, CommonConstant.STATUS_YES, CommonConstant.STATUS_YES, null, null,
					applyMain.getCompanyId(), applyMain.getCompanyId(), "自动代卖-目标企业资金入账");
			fundBalanceDto.setExtendAttre(MemberCateEnums.MEMBER_CATE_CMP.getCateCode());
			fundBalanceDto.setExtendAttrd(applyCharge.getCode());

			FundTradeFlowDto tradeFlowDto = this.createTradeFlowDto("自动代卖-目标企业资金入账", seq,
					BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), applyCharge.getCode(),
					OrderType.getOrderType(applyMain.getSubType()), applyCharge.getChargesType(),
					TradeCateEnums.TRADE_CATE_ADDUCT, ActionType.ACTION_TYPE_IN, applyCharge.getAmount(),
					applyMain.getSourceCode(), tradeFlowCode, CommonConstant.NO_AMOUNT, applyCharge.getAmount(),
					fundBalanceDto.getBalanceCode());

			BeanCopier<TbFundTradeFlow> copierFlow = new BeanCopier<TbFundTradeFlow>(tradeFlowDto,
					new TbFundTradeFlow(), new CopyOptions());
			copierFlow.copy().insertOrUpdate();
			BeanCopier<TbFundBalance> copierBalance = new BeanCopier<TbFundBalance>(fundBalanceDto, new TbFundBalance(),
					new CopyOptions());
			copierBalance.copy().insertOrUpdate();

			// 将企业自己的balanceCode记录到relateCode，方便后续流程获取
			applyCharge.setRelateCode(fundBalanceDto.getBalanceCode());
			applyCharges.add(applyCharge);
		}

		orderApply.setApplyCharges(applyCharges);
		orderApply.setApplyBlances(applyBalances);

		this.createSoldProxyOrder(orderApply.getApplyMain(), orderApply.getApplyPartys(), orderApply.getApplyCharges(),
				orderApply.getApplyBlances());
	}

	public OrderApply getRequiredCouponAmounts(Order applyMain, List<String> members,List<OrderVo> orderVo) {

		OrderApply orderApply = new OrderApply(applyMain);
		// 当月可代卖额度,AssignDto.memberId-会员id，AssignDto.guid-会员第三方编码，AssignDto.orderAmount-会员可用代卖
		// 额度
		List<AssignDto> currentSoldProxyAmountList = this.findSoldProxyAmounts(members);
		log.info("currentSoldProxyAmountList="+JSON.toJSONString(currentSoldProxyAmountList));
		Map<String, OrderVo> unMemberMap = orderVo.stream()
				.collect(Collectors.toMap(OrderVo::getMemberId,o -> o));
		if(CollUtil.isNotEmpty(currentSoldProxyAmountList)) {
			
			currentSoldProxyAmountList.removeIf(dto -> {
				return unMemberMap.containsKey(dto.getMemberId());
			});
			
		}
		
		
		
		log.info("过滤之后的需要代卖的人：{}",JSON.toJSONString(currentSoldProxyAmountList));
		

		if(CollUtil.isEmpty(currentSoldProxyAmountList)) {
			FundServiceExceptionGenerator.FundServiceException(503, "当月可代卖额度人员数据为空");
		}
		
	
		log.info("智能代卖的currentSoldProxyAmountList:{}",JSON.toJSONString(currentSoldProxyAmountList));
		// 确定自动代卖额度信息，并生产单据关联会员信息(该操作会将currentSoldProxyAmountList中无代卖额度的人员过滤掉)
		List<BusinessApplyParty> applyPartyList = determineCurrentSoldPorxyAmount(applyMain.getOrderNo(),
				currentSoldProxyAmountList);
		if(CollUtil.isEmpty(applyPartyList)) {
			FundServiceExceptionGenerator.FundServiceException(503, "确定自动代卖额度信息数据为空");
		}

		
		
		log.info("AFT currentSoldProxyAmountList="+JSON.toJSONString(currentSoldProxyAmountList));
		// 获取当前可以进行代卖的会员的id列表
		List<String> memberIds = currentSoldProxyAmountList.stream().map(AssignDto::getMemberId)
				.collect(Collectors.toList());

		// 获取所有开启自动代卖的会员当前可用消费券额度
		List<FundBalanceDto> leftCouponAmountLists = getLeftCouponAmounts(memberIds);
		log.info("智能代卖的限制22万之前leftCouponAmountLists:{}",JSON.toJSONString(leftCouponAmountLists));
		BigDecimal sumAssAmount=new BigDecimal(0);
		List<FundBalanceDto> leftCouponAmountListsNew=Lists.newArrayList();
		for (FundBalanceDto assignDto : leftCouponAmountLists) {
			//当比20万小，继续累加，
			if(sumAssAmount.compareTo(new BigDecimal(220000000))==-1) {
				sumAssAmount=sumAssAmount.add(assignDto.getBalanceAmount());	
				leftCouponAmountListsNew.add(assignDto);
			}
		}
		log.info("智能代卖的限制22万之后leftCouponAmountListsNew:{}",JSON.toJSONString(leftCouponAmountListsNew));
		leftCouponAmountLists=leftCouponAmountListsNew;
		
		
		
		// 获取当前尚有消费券额度的会员列表
		List<String> leftMemberIds = leftCouponAmountLists.stream().map(FundBalanceDto::getMemberId)
				.collect(Collectors.toList());

		// 过滤自动代卖额度数据，将没有消费券额度或者消费券额度小于100的代卖数据去除
		currentSoldProxyAmountList.removeIf(dto -> {
			return !leftMemberIds.contains(dto.getMemberId());
		});
		log.info("智能代卖的applyPartyList.removeIf:{}",JSON.toJSONString(applyPartyList));
		
		// 过滤单据关联会员信息，将没有消费券额度或者消费券额度小于100的会员单据数据去除
		applyPartyList.removeIf(dto -> {
			return !leftMemberIds.contains(dto.getPartyCode());
		});
		log.info("智能代卖的applyPartyList之前:{}",JSON.toJSONString(applyPartyList));
		

		// 添加虚拟企业单据会员关联信息
		BusinessApplyParty companyParty = this.businessApplyBusiService.createApplyParty(
				Sequence.SOLDPROXY_APPLY_PARTY.getPerfix(), applyMain.getOrderNo(),
				BusinessApplyRoleType.SOLDPROXY_TARGET_COMPANY, CommonConstant.DEFAULT_ACCT_MEMBER_ID,
				BusinessApplyStatus.CREATED, CommonConstant.DEFAULT_ACCT_MEMBER_EID);
		log.info("智能代卖的applyPartyList之后companyParty:{}",JSON.toJSONString(companyParty));
		
		applyPartyList.add(companyParty);
		
		orderApply.setApplyPartys(applyPartyList);

		// 最终使用券资金列表,根据当前可用代卖额度和可用券的列表券顺序进行券额度扣款
		List<FundBalanceDto> allUsedBalances = determineCouponAmount(leftCouponAmountLists, currentSoldProxyAmountList);

		// 将可用的消费券资金记录以券id分组，获取资金总额
		Map<String, Long> groupByCouponIdAmount = allUsedBalances.stream()
				.collect(Collectors.groupingBy(FundBalanceDto::getBalanceItemCode,
						Collectors.summingLong(balance -> balance.getTicketBalance().longValue())));

		// 将可用的消费券资金记录以券id分组，获取资金记录
		Map<String, List<FundBalanceDto>> groupByCouponId = allUsedBalances.stream()
				.collect(Collectors.groupingBy(FundBalanceDto::getBalanceItemCode));

		// 创建单据关联费用信息，单据关联账户资金信息(带事务)
		log.info("智能代卖的orderApply:{}",JSON.toJSONString(orderApply));
		this.getSelfProxy().creatApplyChargesAndApplyBalances(orderApply, groupByCouponIdAmount, groupByCouponId);

		return orderApply;
	}

	public void fireMessage(OrderApply orderApply) {
		try {
			// 发送消息，在消息处理器总进行账户资金和资金流水处理并发送总部
			Message message = MessageBuilder.withBody(JSON.toJSONBytes(orderApply))
					.setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();

			message.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);

			rabbitTemplate.convertAndSend(MqQueueConstant.FUND_EXCHANGE, "SOLD_PROXY_TOPIC", message);
		} catch (Exception e) {
			FundServiceExceptionGenerator.FundServiceException(503, "发送自动代卖订单创建消息出现错误", e);
		}
	}

	private void createSoldProxySuccessBalance(List<TbFundBalance> balances, List<TbFundTradeFlow> tradeFlows, 
			Order soldOrder, BusinessApplyCharges soldCharge, BusinessApplyBalance moneyBalance, 
			int seq, String tradeFlowCode) {
		String remark = "代卖资金回款-企业凑整回款";
//		String acctCate = AcctCateEnums.ACCT_CATE_PTLZ.getCateCode();
//		String balanceItemCode = AcctCateEnums.PLANTFORM_ACCT_CATE_PTXJ.getCateCode();
//		String memberCate = MemberCateEnums.MEMBER_CATE_PT.getCateCode();
		String acctCate = AcctCateEnums.ACCT_CATE_SALARY.getCateCode();
		String balanceItemCode = AcctCateEnums.ACCT_CATE_SALARY.getCateCode();
		String memberCate = MemberCateEnums.MEMBER_CATE_CMP.getCateCode();
		if (BillItemSubCate.BILL_ITEM_SOLD_PROXY_MONEY_COUPON.getCateCode().equals(moneyBalance.getRoleType())) {
			remark = "代卖资金回款-券代卖回款";
			acctCate = AcctCateEnums.ACCT_CATE_SALARY.getCateCode();
			balanceItemCode = AcctCateEnums.ACCT_CATE_SALARY.getCateCode();
			memberCate = MemberCateEnums.MEMBER_CATE_ALO.getCateCode();
		}
		
//		TbFundAcct acct = this.acctBalanceBusiService.findFundAcct(moneyBalance.getBalanceCode(), 
//				acctCate);
		
		FundBalanceDto balanceDto = this.acctBalanceBusiService.findSignleBalanceByParams(CommonConstant.DEFAULT_ACCT_MEMBER_ID, 
						moneyBalance.getBalanceCode(), memberCate, acctCate, balanceItemCode, 
						BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), CommonConstant.STATUS_YES, CommonConstant.STATUS_YES, 
						moneyBalance.getBalanceCode(), null, null);
		
//		FundBalanceDto balanceDto = this.acctBalanceBusiService
//				.findSignleBalanceByAcctInstNo(acct.getAcctInstNo(), balanceItemCode);
		
		FundTradeFlowDto tradeflowDto = this.createTradeFlowDto(remark, seq, 
				BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), 
				soldCharge.getCode(), OrderType.getOrderType(soldOrder.getSubType()), moneyBalance.getRoleType(), 
				TradeCateEnums.TRADE_CATE_ADDUCT, ActionType.ACTION_TYPE_IN, moneyBalance.getAmount(), 
				soldOrder.getSourceCode(), tradeFlowCode, balanceDto.getBalanceAmount(), 
				balanceDto.getBalanceAmount().add(moneyBalance.getAmount()), 
				balanceDto.getBalanceCode());
		balanceDto.setBalanceAmount(tradeflowDto.getTradeLastAmount());
		
		BeanCopier<TbFundTradeFlow> copierFlow = new BeanCopier<TbFundTradeFlow>(tradeflowDto,
				new TbFundTradeFlow(), new CopyOptions());
		tradeFlows.add(copierFlow.copy());
		
		BeanCopier<TbFundBalance> copierBalance = new BeanCopier<TbFundBalance>(balanceDto, new TbFundBalance(),
				new CopyOptions());
		balances.add(copierBalance.copy());
	}
	
	@Transactional(rollbackFor = Exception.class)
	public void complateSoldProxyCharge(Order soldOrder, BusinessApplyCharges soldCharge,
			List<AssignCouponDto> soldResultBalances, BigDecimal soldAmount) {
		
		// key=会员id，value=单据关联会员关联编码
		Map<String, BusinessApplyParty> applyPartyCode = memberIdMapedApplyPartCode(soldOrder.getOrderNo());
		List<BusinessApplyBalance> applyBalances = Lists.newArrayList();
		
		List<TbFundBalance> balances = Lists.newArrayList();
		List<TbFundTradeFlow> tradeFlows = Lists.newArrayList();
		
		int seq = 1;
		String tradeFlowCode = CommonConstant.KEY_PERFIX_TRADEFLOW_NO + IdWorker.getIdStr();
		for (AssignCouponDto soldResultBalance : soldResultBalances) {
			BusinessApplyBalance moneyBalance = this.businessApplyBusiService
					.createApplyBalance(Sequence.SOLDPROXY_APPLY_BALANDCE.getPerfix(), 
					soldCharge.getApplyCode(), soldCharge.getCode(), 
					applyPartyCode.get(soldResultBalance.getMemberId()).getCode(),
					BillItemSubCate.BILL_ITEM_SOLD_PROXY_MONEY_COUPON, 
					soldResultBalance.getMemberId(), //此处用BALANCE_CODE记录会员id，提现时会用到
					BusinessApplyStatus.COMPLETED, soldResultBalance.getAmount(), 
					applyPartyCode.get(soldResultBalance.getMemberId()).getPartyGuid(), //此处用relate_code记录会员guid，提现时会用到
					CommonConstant.NO_AMOUNT, CommonConstant.NO_AMOUNT);
			applyBalances.add(moneyBalance);
			
			createSoldProxySuccessBalance(balances, tradeFlows, soldOrder, soldCharge, moneyBalance, seq, tradeFlowCode);
			seq++;
		}

		if (ObjectUtil.isNull(soldAmount)) {
			soldAmount = CommonConstant.NO_AMOUNT;
		}
		if (soldAmount.compareTo(CommonConstant.NO_AMOUNT)<=0) {
			log.info("本次凑整总额为0，不进行凑整回款记录");
		} else {
			BusinessApplyBalance roundBalance = this.businessApplyBusiService
					.createApplyBalance(Sequence.SOLDPROXY_APPLY_BALANDCE.getPerfix(), 
					soldCharge.getApplyCode(), soldCharge.getCode(), 
					applyPartyCode.get(soldOrder.getCompanyId()).getCode(),
					BillItemSubCate.BILL_ITEM_SOLD_PROXY_MONEY_ROUND, 
					applyPartyCode.get(soldOrder.getCompanyId()).getPartyCode(), //此处用BALANCE_CODE记录公司会员id
					BusinessApplyStatus.COMPLETED, soldAmount, 
					applyPartyCode.get(soldOrder.getCompanyId()).getPartyGuid(), //此处用relate_code记录公司会员guid，提现时会用到
					CommonConstant.NO_AMOUNT, CommonConstant.NO_AMOUNT);
			applyBalances.add(roundBalance);
			
			createSoldProxySuccessBalance(balances, tradeFlows, soldOrder, soldCharge, roundBalance, seq, tradeFlowCode);
		}
		
		List<BusinessApplyBalance> soldCopuonBalances = this.businessApplyBusiService.getApplyBalancesByChargeCode(soldCharge.getCode(), 
				BillItemSubCate.BILL_ITEM_SOLD_PROXY_COUPON.getCateCode()); 
		seq = 1;
		tradeFlowCode = CommonConstant.KEY_PERFIX_TRADEFLOW_NO + IdWorker.getIdStr();
		if (CollUtil.isNotEmpty(soldCopuonBalances)) {
			for (BusinessApplyBalance applyBalance : soldCopuonBalances) {
				
				FundBalanceDto fundBalanceDto = acctBalanceBusiService
						.findSignleBalanceByCode(applyBalance.getBalanceCode());
				
				FundTradeFlowDto tradeFlowDtoFrezz = this.createTradeFlowDto("自动代卖-源资金解冻", seq, 
						fundBalanceDto.getBusiModel(),soldCharge.getCode(), 
						OrderType.getOrderType(soldOrder.getSubType()), soldCharge.getChargesType(),
						TradeCateEnums.TRADE_CATE_UNFREZZ, ActionType.ACTION_TYPE_IN, 
						applyBalance.getAmount(),
						soldOrder.getSourceCode(), tradeFlowCode, 
						fundBalanceDto.getBalanceAmount(),
						fundBalanceDto.getBalanceAmount().add(applyBalance.getAmount()),
						fundBalanceDto.getBalanceCode());
				
				fundBalanceDto.setFreezingBalance(fundBalanceDto.getFreezingBalance().subtract(applyBalance.getAmount()));
				fundBalanceDto.setBalanceAmount(tradeFlowDtoFrezz.getTradeLastAmount());
				
				BeanCopier<TbFundTradeFlow> copierFlowFreez = new BeanCopier<TbFundTradeFlow>(tradeFlowDtoFrezz,
						new TbFundTradeFlow(), new CopyOptions());
				tradeFlows.add(copierFlowFreez.copy());
				
				//重置原冻结记录状态
				List<TbFundTradeFlow> relateTradeFlows = this.acctBalanceService
						.findTradeFlows(soldCharge.getCode(), soldCharge.getChargesType(), 
						 fundBalanceDto.getBalanceCode(), 
						applyBalance.getAmount(), TradeCateEnums.TRADE_CATE_FREZZ.getCateCode());
				if (CollUtil.isNotEmpty(relateTradeFlows)) {
					TbFundTradeFlow freezingFlow = relateTradeFlows.get(0);
					freezingFlow.setStatus(CommonConstant.STATUS_TRADE_FLOW_NORMAL);
					freezingFlow.insertOrUpdate();
				}
				seq++;
				//创建扣除记录
				FundTradeFlowDto tradeFlowDto = this.createTradeFlowDto("自动代卖-源资金出账", seq, fundBalanceDto.getBusiModel(),
						soldCharge.getCode(), OrderType.getOrderType(soldOrder.getSubType()), soldCharge.getChargesType(),
						TradeCateEnums.TRADE_CATE_DEDUCT, ActionType.ACTION_TYPE_OUT, applyBalance.getAmount(),
						soldOrder.getSourceCode(), tradeFlowCode, fundBalanceDto.getBalanceAmount(),
						fundBalanceDto.getBalanceAmount().subtract(applyBalance.getAmount()),
						fundBalanceDto.getBalanceCode());
				fundBalanceDto.setBalanceAmount(tradeFlowDto.getTradeLastAmount());
				
				BeanCopier<TbFundTradeFlow> copierFlowDecut = new BeanCopier<TbFundTradeFlow>(tradeFlowDto,
						new TbFundTradeFlow(), new CopyOptions());
				tradeFlows.add(copierFlowDecut.copy());
				
				BeanCopier<TbFundBalance> copierBalance = new BeanCopier<TbFundBalance>(fundBalanceDto, new TbFundBalance(),
						new CopyOptions());
				balances.add(copierBalance.copy());
				
				seq++;
				
				// 更新资金信息状态
				applyBalance.setStatus(BusinessApplyStatus.COMPLETED.getCateCode());
				applyBalance.setRemark("代卖完成");
			}
		}
		
		FundBalanceDto fundBalanceDto = acctBalanceBusiService
				.findSignleBalanceByCode(soldCharge.getRelateCode());
		fundBalanceDto.setFreezingBalance(fundBalanceDto.getFreezingBalance().subtract(soldCharge.getAmount()));
		BeanCopier<TbFundBalance> copierBalanceCompany = new BeanCopier<TbFundBalance>(fundBalanceDto, new TbFundBalance(),
				new CopyOptions());
		balances.add(copierBalanceCompany.copy());
		
		FundTradeFlowDto tradeFlowDto = this.createTradeFlowDto("自动代卖-目标企业券出账", seq,
				BusiModelEnums.BUSI_MODEL_NONE.getCateCode(), soldCharge.getCode(),
				OrderType.getOrderType(soldOrder.getSubType()), soldCharge.getChargesType(),
				TradeCateEnums.TRADE_CATE_DEDUCT, ActionType.ACTION_TYPE_OUT, soldCharge.getAmount(),
				soldOrder.getSourceCode(), tradeFlowCode, CommonConstant.NO_AMOUNT, soldCharge.getAmount(),
				fundBalanceDto.getBalanceCode());
		
		BeanCopier<TbFundTradeFlow> copierFlowCompyDecut = new BeanCopier<TbFundTradeFlow>(tradeFlowDto,
				new TbFundTradeFlow(), new CopyOptions());
		tradeFlows.add(copierFlowCompyDecut.copy());

		this.businessApplyBusiService.getBusinessApplyBalanceService().insertOrUpdateBatch(soldCopuonBalances);
		this.businessApplyBusiService.getBusinessApplyBalanceService().insertOrUpdateBatch(applyBalances);
		
		this.fundTradeFlowService.insertOrUpdateBatch(tradeFlows);
		this.fundBalanceService.insertOrUpdateBatch(balances);
	}

//	@Transactional(rollbackFor = Exception.class)
	public void complateSoldProxyOrder(Order soldOrder) {
		// 当前主单据下，是否存在没有完成的代卖申请
		boolean noData = this.businessApplyBusiService.getExsitApplyCharge(soldOrder.getOrderNo(),
				BusinessApplyStatus.COMPLETED.getCateCode());
		
		if (noData) {
			soldOrder.setStatus(BusinessApplyStatus.COMPLETED.getCateCode());
			soldOrder.setRemark("代卖单据" + soldOrder.getOrderNo() + "全部回款完成处理完成");
			
			try {
				log.info("开始进行代卖返现处理，处理参数{}", JSON.toJSONString(soldOrder));
				cashRetun(soldOrder);
			} catch (Exception e) {
				log.error(Exceptions.getStackTraceAsString(e));
			} 
			
			OrderApply withdrawOrderApply = null;
			try {
				withdrawOrderApply = this.getSelfProxy().createWithdrawOrder(soldOrder.getOrderNo(), soldOrder.getCompanyId());
				log.debug("代卖单据{}全部回款完成处理完成,回款结果:{}", soldOrder.getOrderNo(), JSON.toJSONString(withdrawOrderApply));
			} catch (Exception e) {
				log.error(Exceptions.getStackTraceAsString(e));
				soldOrder.setStatus(BusinessApplyStatus.PROCESS.getCateCode());
				soldOrder.setRemark("代卖单据" + soldOrder.getOrderNo() + "全部回款完成处理出错，需重新处理");
				FundServiceExceptionGenerator.FundServiceException(503,
						"代卖单据" + soldOrder.getOrderNo() + "全部回款完成处理出错，需重新处理", e);
			} finally {
				soldOrder.insertOrUpdate();
			}
			
			withdrawOrderApply.setWithdrawFlag(this.withdrawAuto);
			this.fireMessage(withdrawOrderApply);
//			withdrawService.createWithdrawBalances(false);
		}
	}

	private FundTradeFlowDto createTradeFlowDto(String remark, int seq, String busiModel, String tradeBusiCode,
			OrderType tradeBusiCate, String billItemCate, TradeCateEnums tradeCate, ActionType tradeActCate,
			BigDecimal tradeAmount, String source, String tradeFlowCode, BigDecimal perAmount, BigDecimal lastAmount,
			String balanceCode) {
		FundTradeFlowDto tradeFlowDto = new FundTradeFlowDto(remark, new BigDecimal(seq), tradeBusiCode, busiModel,
				tradeBusiCate.getCateCode(), billItemCate, tradeCate.getCateCode(), tradeActCate.getCateCode(),
				tradeAmount, source);

		tradeFlowDto.setTransBusiCateName(tradeBusiCate.getCateName());
		tradeFlowDto.setTransCateName(tradeCate.getCateName());
		tradeFlowDto.setTransActName(tradeActCate.getCateName());
		tradeFlowDto.setTradeFlowCode(tradeFlowCode);
		tradeFlowDto.setTradePreAmount(perAmount);
		tradeFlowDto.setTradeLastAmount(lastAmount);
		tradeFlowDto.setBalanceCode(balanceCode);
		return tradeFlowDto;
	}

	private void cashRetun(Order soldOrder) {
		OrderApply soldOrders = this.businessApplyBusiService.findOrderApplyByOrderNo(soldOrder.getOrderNo());
		List<BusinessApplyParty> allApplyParts = soldOrders.getApplyPartys();
		List<BusinessApplyBalance> allApplyBalances = soldOrders.getApplyBlances();
		
		List<BusinessApplyParty> peopleParts = allApplyParts.stream()
			.filter(party -> party.getRoleCode().equals(BusinessApplyRoleType.SOLDPROXY_SOURCE_PEOPLE.getCateCode()))
				.collect(Collectors.toList());
		Map<String,BusinessApplyParty> partyCodeParty = peopleParts.stream().collect(Collectors.toMap(BusinessApplyParty::getPartyCode, o -> o));
		Map<String, List<BusinessApplyBalance>> groupByCouponId = allApplyBalances.stream()
				.collect(Collectors.groupingBy(BusinessApplyBalance::getApplyPartyCode));
		List<String> memberIds = peopleParts.stream().map(BusinessApplyParty::getPartyCode)
				.collect(Collectors.toList());
		Date transactionTime = new Date();
		List<BackCashVo> backCashVos = memberIds.stream().map(memberId -> {
			TbFundTradeFlow firstOrderTradeFlow = this.inviteFriendsService.judgeTheFirstOrder(memberId);
			BackCashVo backCashVo = new BackCashVo();
		    backCashVo.setMemberId(memberId);
		    backCashVo.setTransTime(firstOrderTradeFlow.getBeginTime());
		    backCashVo.setFirstTransTime(firstOrderTradeFlow.getBeginTime());
		    
		    backCashVo.setIsFirst(CommonConstant.STATUS_YES);
		    if(firstOrderTradeFlow.getTradeAmount().compareTo(new BigDecimal("1")) > 0) {
		      backCashVo.setTransTime(transactionTime);
		      backCashVo.setIsFirst(CommonConstant.STATUS_NO);
		    }
			return backCashVo;
		}).collect(Collectors.toList());
		
		
//		TbFundTradeFlow firstOrderTradeFlow = this.inviteFriendsService.judgeTheFirstOrder(assignDto.getMemberId());
//	    List<BackCashVo> backCashVos = Lists.newArrayList();
//	    BackCashVo backCashVo = new BackCashVo();
//	    backCashVo.setMemberId(assignDto.getMemberId());
//	    backCashVo.setTransTime(firstOrderTradeFlow.getBeginTime());
//	    backCashVo.setFirstTransTime(firstOrderTradeFlow.getBeginTime());
//	    
//	    backCashVo.setIsFirst(CommonConstant.STATUS_YES);
//	    if(firstOrderTradeFlow.getTradeAmount().compareTo(new BigDecimal("1")) > 0) {
//	      backCashVo.setTransTime(transactionTime);
//	      backCashVo.setIsFirst(CommonConstant.STATUS_NO);
//	    }
//	    backCashVos.add(backCashVo);
	    
		R<List<String>> r = this.refactorMemberServiceFactory
			.getRefactorMemberRecommendInfoService().checkedSendFundData(backCashVos);
		if (null != r && R.SUCCESS == r.getCode()) {
			memberIds = r.getData();
		} else {
			memberIds = null;
		}
		
		if (CollUtil.isNotEmpty(memberIds)) {
			for (String memberId : memberIds) {
				List<BusinessApplyBalance> applyBalances = groupByCouponId.get(partyCodeParty.get(memberId).getCode());
				BigDecimal amounts = CommonConstant.NO_AMOUNT;
				for (BusinessApplyBalance abalance : applyBalances) {
					if (BusinessApplyRoleType.SOLD_PROXY_COUPON_PAY.getCateCode().equals(abalance.getRoleType())) {
						FundBalanceDto balanceDto = this.acctBalanceBusiService.findSignleBalanceByCode(abalance.getBalanceCode());
						if(AcctCateEnums.ACCT_CATE_PTQYX.getCateMgn().equals(balanceDto.getCompanyId()) && 
								memberId.equals(balanceDto.getOwnerId())) {
							amounts = amounts.add(abalance.getAmount());
						}
					}
				}
				if (amounts.compareTo(CommonConstant.NO_AMOUNT)>0) {
					log.info("发送返现计算消息队列--memberId:{}---amount:{}----tradeCate:{}", memberId, 
							amounts, OrderType.SOLDPROXY.getCateCode());
		          AssignDto returnAssignDto = new AssignDto();
		          returnAssignDto.setMemberId(memberId);
		          returnAssignDto.setOrderType(OrderType.SOLDPROXY.getCateCode());
		          returnAssignDto.setCouponAmount(amounts);
		          returnAssignDto.setTransTime(soldOrder.getUpdatedTime());
		          Message message=MessageBuilder
		                  .withBody(JSON.toJSONBytes(returnAssignDto))
		                  .setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
	              message.getMessageProperties().setContentType(org.springframework.amqp.core.MessageProperties.CONTENT_TYPE_JSON);
	              rabbitTemplate.convertAndSend(MqQueueConstant.FUND_EXCHANGE, MqQueueConstant.RETURN_INVITE_FRIEND_TOPIC, message); 
		         // rabbitTemplate.convertAndSend(MqQueueConstant.FUND_EXCHANGE, MqQueueConstant.RETURN_INVITE_FRIEND_TOPIC, returnAssignDto); 
		          log.info("发送返现计算消息队列结束");
				}
			}
		}
	}
	private Map<String, BusinessApplyParty> memberIdMapedApplyPartCode(String orderNo) {
		List<BusinessApplyParty> soldPartys = this.businessApplyBusiService.getApplyPartysByApplyCode(orderNo);
		return soldPartys.stream().collect(Collectors.toMap(BusinessApplyParty::getPartyCode, o -> o));
	}

	@Transactional(rollbackFor = Exception.class)
	public OrderApply createWithdrawOrder(String soldOrderNo, String companyId) {
		// 代卖单据全部信息
		OrderApply soldProxyOrderApply = this.businessApplyBusiService.findOrderApplyByOrderNo(soldOrderNo);
		log.info("createWithdrawOrder.findOrderApplyByOrderNo={}", JSON.toJSONString(soldProxyOrderApply));
		
		// 默认提现单据生成后暂停，不自动提现
		BusinessApplyStatus statusWithdraw = BusinessApplyStatus.PAUSE;
		// 如果自动提现设置为true，则进行自动提现
		if (this.withdrawAuto) {
			statusWithdraw = BusinessApplyStatus.START;
		}
				
		// 生成提现主单据
		Order withdrawOrder = this.businessApplyBusiService.createApplyMain("TX", soldOrderNo, OrderType.TASK, 
				OrderType.SOLDPROXY_WITHDRAW, statusWithdraw, companyId, 
				SystemNameEnum.System_platform.getSysCode());
		withdrawOrder.setRemark("生成提现订单出完成");
		
		OrderApply withdrawApply = new OrderApply(withdrawOrder);
		
		log.info("createWithdrawOrder.withdrawApply={}", JSON.toJSONString(withdrawApply));
		// key=applyPartCode,value=BusinessApplyParty
		Map<String, BusinessApplyParty> applyPartCodeGrouped = getApplyPartCodeGroupedParty(soldProxyOrderApply);
		log.info("createWithdrawOrder.applyPartCodeGrouped={}", JSON.toJSONString(applyPartCodeGrouped));
		// key=applyPartCode,value=需要提现的金额
		Map<String, Long> applyPartCodeGroupedAmount = getApplyPartCodeGroupedBalance(soldProxyOrderApply);
		log.info("applyPartCodeGroupedAmount.applyPartCodeGroupedAmount={}",
				JSON.toJSONString(applyPartCodeGroupedAmount));

		List<BusinessApplyParty> applyPartys = Lists.newArrayList();
		List<BusinessApplyCharges> applyCharges = Lists.newArrayList();

		try {
			for (Map.Entry<String, Long> entry : applyPartCodeGroupedAmount.entrySet()) {

				String applyPartyCode = entry.getKey();
				BusinessApplyParty applyParty = applyPartCodeGrouped.get(applyPartyCode);
				//跳过企业角色
				if (ObjectUtil.isNotNull(applyParty) && applyParty.getRoleCode()
						.equals(BusinessApplyRoleType.SOLDPROXY_TARGET_COMPANY.getCateCode())) {
					continue;
				}
				
				BigDecimal amount = new BigDecimal(entry.getValue());
				
				// 按会员创建提现单据
				BusinessApplyCharges applyCharge = this.businessApplyBusiService.createApplyCharge("TX", 
						withdrawOrder.getOrderNo(), BillItemSubCate.BILL_ITEM_SOLD_PROXY_WITHDRAW, 
						AcctCateEnums.ACCT_CATE_SALARY, applyParty.getPartyCode(), amount, statusWithdraw,
						applyParty.getPartyGuid(), CommonConstant.STRING_BLANK);
				applyCharges.add(applyCharge);

				// 创建提现主单据相关的会员单据
				applyParty.setId(null);
				applyParty.setApplyCode(withdrawOrder.getOrderNo());
				applyParty.setCode(new SequenceNumber(IdWorker.getId()).getLiteBuinessCode("TX"));
				applyParty.setStatus(statusWithdraw.getCateCode());
				applyPartys.add(applyParty);
			}

			// 添加提现相关公司会员信息
//			BusinessApplyParty companyParty = this.createApplyParty(withdrawOrder.getOrderNo(),
//					CommonConstant.PLANTFORM_ACCT_MEMBER_ID,
//					BusinessApplyRoleType.SOLDPROXY_TARGET_COMPANY.getCateCode());
//			companyParty.setPartyGuid(CommonConstant.PLANTFORM_ACCT_MEMBER_EID);
//			companyParty.setStatus(BusinessApplyStatus.CREATED.getCateCode());
//			companyParty.setCode(new SequenceNumber(IdWorker.getId()).getLiteBuinessCode("TX"));
//			applyPartys.add(companyParty);
//			this.businessApplyBusiService.getBusinessApplyChargesService().insertOrUpdateBatch(applyCharges);
//			this.businessApplyBusiService.getBusinessApplyPartyService().insertOrUpdateBatch(applyPartys);
			
			
//			withdrawOrder.setStatus(statusWithdraw);
//			withdrawOrder.setRemark("生成提现订单出完成");
			
			this.createSoldProxyOrder(withdrawOrder, applyPartys, applyCharges, null);
			withdrawApply.setApplyCharges(applyCharges);
			withdrawApply.setApplyPartys(applyPartys);

		} catch (Exception e) {
			log.error(Exceptions.getStackTraceAsString(e));
			withdrawApply.changeApplyMainStatus(BusinessApplyStatus.FAULT, "生成提现订单出现错误，需重新生成");
			FundServiceExceptionGenerator.FundServiceException(503, "生成提现订单出现错误，需重新生成", e);
		}
		return withdrawApply;
	}

	private Map<String, BusinessApplyParty> getApplyPartCodeGroupedParty(OrderApply orderApply) {
		List<BusinessApplyParty> soldProxyApplyPartys = orderApply.getApplyPartys();
		return soldProxyApplyPartys.stream().collect(Collectors.toMap(BusinessApplyParty::getCode, o -> o));
	}

	private Map<String, Long> getApplyPartCodeGroupedBalance(OrderApply orderApply) {

		List<BusinessApplyBalance> soldProxyApplyBalances = orderApply.getApplyBlances();

		List<BusinessApplyBalance> moneyBalances = soldProxyApplyBalances.stream().filter(
				balance -> balance.getRoleType().equals(BusinessApplyRoleType.SOLD_PROXY_MONEY_COUPON.getCateCode()))
				.collect(Collectors.toList());

		return moneyBalances.stream().collect(Collectors.groupingBy(BusinessApplyBalance::getApplyPartyCode,
				Collectors.summingLong(o -> o.getAmount().longValue())));
	}

	private void createSoldProxyOrder(Order order, List<BusinessApplyParty> applyParts,
			List<BusinessApplyCharges> applyCharges, List<BusinessApplyBalance> applyBalances) {
		if (order != null) {
			this.businessApplyBusiService.getOrderService().insertOrUpdate(order);
		}
		if (CollUtil.isNotEmpty(applyParts)) {
			this.businessApplyBusiService.getBusinessApplyPartyService().insertBatch(applyParts);
		}
		if (CollUtil.isNotEmpty(applyCharges)) {
			this.businessApplyBusiService.getBusinessApplyChargesService().insertBatch(applyCharges);
		}
		if (CollUtil.isNotEmpty(applyBalances)) {
			this.businessApplyBusiService.getBusinessApplyBalanceService().insertBatch(applyBalances);
		}
	}
}
