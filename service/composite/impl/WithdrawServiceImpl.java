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
import java.util.stream.Collectors;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.MqQueueConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyRoleType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.util.Exceptions;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.CashQuerysDto;
import com.taolue.baoxiao.fund.api.dto.OrderDTO;
import com.taolue.baoxiao.fund.api.openplatform.IDockOpenSubstituteSaleService;
import com.taolue.baoxiao.fund.api.vo.CashQuerysVo;
import com.taolue.baoxiao.fund.api.vo.OrderDetailVo;
import com.taolue.baoxiao.fund.api.vo.OrderVo;
import com.taolue.baoxiao.fund.api.vo.RemoteResultVo;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.common.model.OrderApply;
import com.taolue.baoxiao.fund.entity.BusinessApplyBalance;
import com.taolue.baoxiao.fund.entity.BusinessApplyCharges;
import com.taolue.baoxiao.fund.entity.BusinessApplyParty;
import com.taolue.baoxiao.fund.entity.Order;
import com.taolue.baoxiao.fund.mapper.BusinessApplyBusiMapper;
import com.taolue.baoxiao.fund.service.IOrderService;
import com.taolue.baoxiao.fund.service.composite.IBusinessApplyBusiService;
import com.taolue.baoxiao.fund.service.composite.IWithdrawService;
import com.taolue.baoxiao.fund.service.remote.IOpenPlatformService;
import com.taolue.baoxiao.fund.service.remote.IRefactorMemberServiceFactory;

import com.taolue.dock.api.dto.CashOrderDto;
import com.taolue.dock.api.dto.DockCashOrderRecordDto;
import com.taolue.dock.api.vo.CashOrderVo;
import com.taolue.member.api.vo.BankCardInfoVo;

import cn.hutool.core.bean.copier.BeanCopier;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * ClassName: SoldProxyServiceImpl </br>
 * <p>
 * Description: 提现处理类</br>
 * 提现单据状态-
 * 1、START/PASUE，开始或者暂停，该提现请求初始化状态，START-自动启动状态/PASUE-暂停不自动启动，需认为确认是否自动启动
 * 2、PROCESS-该单据关联的提现请求在处理中-即提现中
 * 3、COMPLETED-该单据关联的提现请求全部完成-即提现完成
 * 提现请求状态：
 * 1、START/RESTART-创建提现请求完成，可以进行请求发送；START-首次发送提现请求/RESTART-重新发送提现请求
 * 2、PROCESS-提现请求发送成功，等待提现结果；
 * 3、COMPLETED-当前提现请求已发送且提现成功；
 * 4、REFUSE-因为某些参数原因当前提现请求发送没有成功未被接受，此种状态的提现请求可以维持不变继续重发；
 * 5、PAUSE-提现请求已经发送至对端，但返回6012状态码（业务编号重复），此状态的提现请求默认暂停，等待确认后可以选择更换业务编号后重发；
 * 6、WAITING-当前提现请求超出人纬度每日提现限额，需跨隔日重发；
 * 7、STOP-当前提现请求超出人纬度每月提现限额，需跨月后进行重发；
 * 8、FAULT-当前提现请求已经发送，但提现失败；
 * 
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
public class WithdrawServiceImpl implements IWithdrawService {

	private static final boolean isDebug = log.isDebugEnabled();
	@Autowired
	private IOpenPlatformService openPlatformService;
	@Autowired
	private IDockOpenSubstituteSaleService openSubstituteSaleService;
	@Autowired
	private IRefactorMemberServiceFactory refactorMemberServiceFactory;

	@Autowired
	private IBusinessApplyBusiService businessApplyBusiService;
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private IOrderService orderService;
	@Autowired
	private BusinessApplyBusiMapper businessApplyBusiMapper;

	private CopyOptions option = new CopyOptions(null,true, "status","remark","create_time","updated_time");
	
	public List<BusinessApplyBalance> findComplatedWithdrawBalanceByMemberId(String memberId, String code,
			String[] statusArray) {
		BusinessApplyBalance entity = new BusinessApplyBalance();
		entity.setBalanceCode(memberId);
		entity.setRoleType(BusinessApplyRoleType.SOLD_PROXY_MONEY_WIHTDRAW.getCateCode());
		
		EntityWrapper<BusinessApplyBalance> wrapper = new EntityWrapper<>(entity);
		
		if (StrUtil.isNotBlank(code)) {
			wrapper.eq("code", code);
		}
		
		if (ArrayUtil.isNotEmpty(statusArray)) {
			if (statusArray.length ==  1) {
				wrapper.eq("status", statusArray[0]);
			} else {
				wrapper.in("status", statusArray);
			}
		}
		wrapper.orderBy("updated_time", false);
		List<BusinessApplyBalance> applyBalanes = this.businessApplyBusiService
				.getBusinessApplyBalanceService().selectList(wrapper);
		return applyBalanes;
	}
	
	/**
	 * 
	 * <p>名称:类WithdrawServiceImpl中的createWithdrawBalances方法</br>    
	 * <p>描述: 为PASUE和START状态的提现单据创建提现资金明细；该方法若执行有异常需要恢复withdrawCharge单据状态为PAUSE
	 * 	下次启动时可以再次发起重新提现</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 22, 2019 7:44:55 AM</br>
	 * @throws Exception
	 * @param isContainsPasue {@link  boolean} 是否为暂停状态的提现单据创建资金明细 true-包含；false-不包含
	 */
	public synchronized void createWithdrawBalances(boolean isContainsPasue) {
		
		List<BusinessApplyCharges> withdrawCharges = findNoBalanceWithdraws(isContainsPasue);
		
		if (CollUtil.isNotEmpty(withdrawCharges)) {
			for (BusinessApplyCharges withdrawCharge : withdrawCharges) {
				//查关联用户
				//key->memberId,value=applyPartyCode
				Map<String, BusinessApplyParty> withdrawPartys = 
						getWithdrawPartysWithCache(withdrawCharge.getApplyCode());
				OrderApply orderApply = null;
				
				try  {
					BusinessApplyParty withdrawParty = withdrawPartys.get(withdrawCharge.getChargesItemCode());
					
					if (StrUtil.isEmpty(withdrawCharge.getRelateCode())) {
						withdrawCharge
							.setRelateCode(withdrawParty.getPartyGuid());
					}
					orderApply = new OrderApply(withdrawCharge, withdrawParty, OrderType.SOLDPROXY_WITHDRAW);
					this.createWithdrawBalance(orderApply);
					
					withdrawCharge.setStatus(BusinessApplyStatus.PROCESS.getCateCode());
					withdrawCharge.setRemark("已为提现单据"+withdrawCharge.getCode()+"生成提现请求");
					
				} catch (Exception e) {
					log.error(Exceptions.getStackTraceAsString(e));
					withdrawCharge.setStatus(BusinessApplyStatus.PAUSE.getCateCode());
					withdrawCharge.setRemark("为提现单据"
					+withdrawCharge.getCode()+"生成提现请求失败"
							+e.getMessage());
					orderApply = null;
				} finally {
					withdrawCharge.insertOrUpdate();
				}
				
				if (null != orderApply) {
					this.fireWithdrawRequest(orderApply);
				}
			}
		}
	}
	
	public synchronized void fireWithdrawWork() {
		queryPerMonthWithraws();
		queryProcessWithraws();
	}
	
	public void createWithdrawBalance(OrderApply orderApply) {
		
		BusinessApplyCharges withdrawCharge = orderApply.getApplyCharges().get(0);
		BusinessApplyParty withdrawParty = orderApply.getApplyPartys().get(0);
		
		/*是否有已经存在REFUSE和PASUE状态的withdrawBalance*/
		BusinessApplyBalance withdrawBalance = this.findRefuseAndPasueBalance(withdrawCharge.getCode());
		
		//若不存在则新建withdrawBalance
		if (null == withdrawBalance) {
			withdrawBalance = this.createNewitdrawBalance(withdrawParty.getCode(), withdrawCharge);
		}
		
		//取银行卡信息
		BankCardInfoVo bankCard =this.refactorMemberServiceFactory
				.findBankCardWithCache(withdrawCharge.getChargesItemCode(), 
						withdrawBalance.getRelateCode(), false);
		
		if (null == bankCard) {
			FundServiceExceptionGenerator.FundServiceException(HttpStatus.HTTP_UNAVAILABLE, 
					"会员{}无可用的银行卡", withdrawCharge.getChargesItemCode());
		} else {
			withdrawBalance.setRelateCode(bankCard.getBankNumber());
		}
		withdrawBalance.insertOrUpdate();
		orderApply.addApplyBalance(withdrawBalance);
		orderApply.addApplyBankCard(bankCard);
	}

	public void fireWithdrawRequest(OrderApply orderApply) {
		Message message=MessageBuilder
				.withBody(JSON.toJSONBytes(orderApply))
				.setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
		message.getMessageProperties().setContentType(org.springframework.amqp.core.MessageProperties.CONTENT_TYPE_JSON);
        rabbitTemplate.convertAndSend(MqQueueConstant.FUND_EXCHANGE,"WITHDRAW_REQUEST_TOPIC",message); 
	}
	
	public RemoteResultVo<CashOrderVo> withdrawRequest(CashOrderDto cashOrderDto) {
		return this.openPlatformService.cashOrder(cashOrderDto);
	}
	
	public RemoteResultVo<CashOrderVo> withdrawRequest(BusinessApplyCharges withdrawCharge, 
			BusinessApplyBalance witdrawBalance, BankCardInfoVo bankCard) {

		CashOrderDto  cashOrderDto = new CashOrderDto();
		cashOrderDto.setAmount(witdrawBalance.getAmount());
		cashOrderDto.setBankName(bankCard.getBankName());
		cashOrderDto.setBizId(witdrawBalance.getCode());
		cashOrderDto.setBranch(bankCard.getSubbranchBankName());
		cashOrderDto.setCardNo(bankCard.getBankNumber());
		cashOrderDto.setCity(bankCard.getCityName());
		cashOrderDto.setCompanyId(CommonConstant.DEFAULT_ACCT_MEMBER_ID);
		cashOrderDto.setGeid(CommonConstant.DEFAULT_ACCT_MEMBER_EID);
		cashOrderDto.setGuid(withdrawCharge.getRelateCode());
		cashOrderDto.setMemberId(withdrawCharge.getChargesItemCode());
		cashOrderDto.setName(bankCard.getName());
		cashOrderDto.setProvince(bankCard.getProvinceName());
		return this.withdrawRequest(cashOrderDto);
	}
	
	/*新建的状态为START*/
	private BusinessApplyBalance createNewitdrawBalance(String applyPartyCode, BusinessApplyCharges withdrawCharge) {
		//当前提现总额大于49500，则将交易金额设置为49500，
		BigDecimal tradeAmount = determineWithdrawAmount(withdrawCharge.getAmount());
		BusinessApplyBalance withdrawBalance = this.businessApplyBusiService.createApplyBalance("TX", 
				withdrawCharge.getApplyCode(), withdrawCharge.getCode(), applyPartyCode, 
				BusinessApplyRoleType.SOLD_PROXY_MONEY_WIHTDRAW, withdrawCharge.getChargesItemCode(), 
				BusinessApplyStatus.START, tradeAmount, CommonConstant.STRING_BLANK, 
				CommonConstant.NO_AMOUNT, CommonConstant.NO_AMOUNT);
		withdrawBalance.setRate(new BigDecimal(1));
		withdrawBalance.setCode(withdrawBalance.getCode()+"_"+withdrawBalance.getRate().intValue());
		return withdrawBalance;
	}
	
	private BusinessApplyBalance findRefuseAndPasueBalance(String applyChareCode) {
		
		BusinessApplyBalance withdrawBalance = null;
		List<BusinessApplyBalance> applyBalances = this.businessApplyBusiService
				.getApplyBalancesByChargeCode(applyChareCode, 
						BusinessApplyRoleType.SOLD_PROXY_MONEY_WIHTDRAW.getCateCode());
		if (isDebug) {
			log.debug("查询到的已经发送的提现请求为{}",JSON.toJSONString(applyBalances));
		}
		//REFUSE和PAUSE状态的请求针对同一个提现单同一时刻只会有一条
		if (CollUtil.isNotEmpty(applyBalances)) {
			for (BusinessApplyBalance applyBalance : applyBalances) {
				withdrawBalance = applyBalance;
				BigDecimal count = withdrawBalance.getRate() == null ? CommonConstant.NO_AMOUNT 
						: withdrawBalance.getRate();
				withdrawBalance.setRate(count.add(new BigDecimal(1)));
				//前次发起的提现请求为REFUSE-则可以直接重新发起
				if (BusinessApplyStatus.REFUSE.getCateCode().equals(applyBalance.getStatus())) {
					break;
					
				//前次发起的提现请求状态为PAUSE-则需要换请求编号后，再次发起
				} else if (BusinessApplyStatus.PAUSE.getCateCode().equals(applyBalance.getStatus())) {
					
					applyBalance.setCode(applyBalance.getCode().split("_")[0]+"_"
							+withdrawBalance.getRate().intValue());
					break;
				//其他状态均为结果查询任务返回状态，无需进行操作
				} else {
					withdrawBalance = null;
					continue;
				}
			}
		}
		if (ObjectUtil.isNotNull(withdrawBalance)) {
			withdrawBalance.setStatus(BusinessApplyStatus.START.getCateCode());
		}
		return withdrawBalance;
	}
	
	private void queryProcessWithraws () {
		//2、查询所有处理中状态的提现申请，进行处理
		List<BusinessApplyBalance> processBalances = this.businessApplyBusiService
				.findWithdrawBalances(BusinessApplyStatus.PROCESS.getCateCode());
		
		if (CollUtil.isNotEmpty(processBalances)) {
			for (BusinessApplyBalance applyBalance : processBalances) {
				BusinessApplyCharges applyCharges = this.businessApplyBusiService
						.getApplyChargeByCode(applyBalance.getApplyChargeCode());
				OrderApply orderApply = null;
				try {
					orderApply = queryWithdrawResults(applyCharges,applyBalance);
				} catch (Exception e) {
					log.error(Exceptions.getStackTraceAsString(e));
				}
				if (null != orderApply) {
					this.fireWithdrawRequest(orderApply);
				}
			}
		}
	}
	
	private void queryPerMonthWithraws() {
		//1、查询所有需重发状态的提现申请，若该申请已经跨月则更新为fault状态，并且按原额度重新重发发起请求
		List<BusinessApplyBalance> monthedBalances = this.businessApplyBusiService
				.findWithdrawBalances(BusinessApplyStatus.STOP.getCateCode());
		
		if (CollUtil.isNotEmpty(monthedBalances)) {
			for (BusinessApplyBalance applyBalance : monthedBalances) {
				//是上月单据
				if (DateUtil.thisMonth()>DateUtil.month(applyBalance.getUpdatedTime())) {
					
					BusinessApplyCharges applyCharge = this.businessApplyBusiService
							.getApplyChargeByCode(applyBalance.getApplyChargeCode());
					
					OrderApply orderApply = null;
					try {
						RemoteResultVo<BigDecimal> er = new RemoteResultVo<>();
						er.setData(applyBalance.getAmount());
						er.setCode(HttpStatus.HTTP_BAD_GATEWAY);
						er.setStatus(BusinessApplyStatus.FAULT);
						er.setMessage("失效上月请求单据，重新进行提现请求！！");
						orderApply = this.wrapperWithdrawBalanceByOldBalance(applyCharge, applyBalance, er);
						
					} catch (Exception e) {
						log.error(Exceptions.getStackTraceAsString(e));
					} 
					if (null != orderApply) {
						this.fireWithdrawRequest(orderApply);
					}
				}
			}
		}
	}
	
	@Transactional(rollbackFor=Exception.class)
	public OrderApply queryWithdrawResults(BusinessApplyCharges applyCharge, BusinessApplyBalance applyBalance) {
		String withdrawNo = applyBalance.getCode();
		RemoteResultVo<BigDecimal> er = this.openPlatformService.orderQuery(withdrawNo);
		return this.wrapperWithdrawBalanceByOldBalance(applyCharge, applyBalance, er);
	}
	
	@Transactional(rollbackFor=Exception.class)
	public OrderApply wrapperWithdrawBalanceByOldBalance(BusinessApplyCharges applyCharge,
			BusinessApplyBalance oriWitdrawBalance, RemoteResultVo<BigDecimal> er) {
		
		BusinessApplyBalance newWitdrawBalance = null;
		BankCardInfoVo bankCard = null;
		int code = er.getCode();
		oriWitdrawBalance.setStatus(er.getStatus().getCateCode());
		oriWitdrawBalance.setRemark(er.getMessage());
		
		//查询结果失败或者是处理中的，将不再提现
		//提现非卡类型超额,且已经没有余额则将当前请求置为STOP，需下月初再次启动，本次不再提现
		if (HttpStatus.HTTP_UNAVAILABLE == code || HttpStatus.HTTP_VERSION == code
				|| (HttpStatus.HTTP_BAD_GATEWAY == code 
						&& er.getData().compareTo(CommonConstant.NO_AMOUNT)<=0)) {
			newWitdrawBalance = null;
		}  else {
			BeanCopier<BusinessApplyBalance> copier = new BeanCopier<BusinessApplyBalance>(oriWitdrawBalance, 
					new BusinessApplyBalance(), option);
			newWitdrawBalance = copier.copy();
			newWitdrawBalance.setId(null);
			newWitdrawBalance.setRate(new BigDecimal(oriWitdrawBalance.getRate().intValue()+1));
			//设置新的请求编号
			newWitdrawBalance.setCode(oriWitdrawBalance.getCode().split("_")[0]
					+"_"+newWitdrawBalance.getRate().intValue());
			
			newWitdrawBalance.setStatus(BusinessApplyStatus.RESTART.getCateCode());
			
			bankCard =this.refactorMemberServiceFactory
					.findBankCardWithCache(applyCharge.getChargesItemCode(), 
							newWitdrawBalance.getRelateCode(), false);
			
			//成功提现
			if (HttpStatus.HTTP_OK == code) {
				//更新提现单据已提现金额
				if (ObjectUtil.isNull(applyCharge.getAmount())) {
					applyCharge.setAmount(CommonConstant.NO_AMOUNT);
				}
				applyCharge.setRateAmount(applyCharge.getRateAmount().add(oriWitdrawBalance.getAmount()));
				//剩余需提现金额
				BigDecimal leftAmount = applyCharge.getAmount().subtract(applyCharge.getRateAmount());
				//还需要继续提现
				if (leftAmount.compareTo(CommonConstant.NO_AMOUNT)>0) {
					//拆分金额
					BigDecimal withdrawAmount = determineWithdrawAmount(leftAmount);
					newWitdrawBalance.setAmount(withdrawAmount);
					newWitdrawBalance.setStatus(BusinessApplyStatus.START.getCateCode());
					
				//已经全部提现
				} else {
					applyCharge.setStatus(BusinessApplyStatus.COMPLETED.getCateCode());
					applyCharge.setRemark("当前提现单提现完成");
					newWitdrawBalance = null;
				}
				Order  order = new  Order();
				order.setOrderNo(oriWitdrawBalance.getApplyCode());
				order.setDelFlag(CommonConstant.STATUS_NORMAL);
				log.info("查询订单编号入参参数：{}",JSON.toJSONString(order));
				Order orderOne=orderService.selectOne(new EntityWrapper<>(order));
				log.info("跟剧查询订单编号结果：{}",JSON.toJSONString(orderOne));
				DockCashOrderRecordDto dto=new DockCashOrderRecordDto();
				dto.setBizId(oriWitdrawBalance.getCode());
				dto.setOrderNo(orderOne.getOrderCode());
				dto.setUpdatedTime(new Date());
				log.info("提现模板消息入参参数：{}",JSON.toJSONString(dto));
				 R<Boolean> r =	openSubstituteSaleService.cashOrderSuccessSendMsgTemplate(dto);
				log.info("提现模板消息结果：{}",JSON.toJSONString(r));
			//提现非卡类型超额,且尚有有余额，则重新设置提现金额，再次提现
			} else if (HttpStatus.HTTP_BAD_GATEWAY == code 
					&& er.getData().compareTo(CommonConstant.NO_AMOUNT)>0) {
				newWitdrawBalance.setAmount(er.getData());
				
			//提现卡类型超额
			} else if (HttpStatus.HTTP_NOT_IMPLEMENTED == code) {
				//若卡无提现额度，则换卡提现
				if (er.getData().compareTo(CommonConstant.NO_AMOUNT)<=0) {
					//取银行卡信息
					bankCard =this.refactorMemberServiceFactory
							.findBankCardWithCache(applyCharge.getChargesItemCode(), 
									newWitdrawBalance.getRelateCode(), true);
					if (bankCard == null) {
						newWitdrawBalance = null;
					} else {
						newWitdrawBalance.setRelateCode(bankCard.getBankNumber());
					}
				//若卡尚有余额，则更换提现金额，重新提现
				} else {
					newWitdrawBalance.setAmount(er.getData());
				}
			}
		}
		oriWitdrawBalance.insertOrUpdate();
		applyCharge.insertOrUpdate();
		if (newWitdrawBalance != null && bankCard != null) {
			newWitdrawBalance.insertOrUpdate();
			OrderApply orderApply = new OrderApply(applyCharge, null, OrderType.SOLDPROXY_WITHDRAW);
			orderApply.addApplyBalance(newWitdrawBalance);
			orderApply.addApplyBankCard(bankCard);
			return orderApply;
		}
		return null;
	}
	
	private BigDecimal determineWithdrawAmount(BigDecimal withdrawAmount) {
		return withdrawAmount.compareTo(CommonConstant.LIMIT_WIHTDRAW_AMOUNT)>0 ? CommonConstant.LIMIT_WIHTDRAW_AMOUNT
				: withdrawAmount;
	}
	
	private Map<String, BusinessApplyParty> getWithdrawPartysWithCache(String applyCode) {
		//查关联用户
		List<BusinessApplyParty> withdrawPartys = this.businessApplyBusiService
				.getApplyPartyByApplyCodeRole(applyCode, 
				BusinessApplyRoleType.SOLDPROXY_SOURCE_PEOPLE.getCateCode());
		
		if (CollUtil.isNotEmpty(withdrawPartys)) {
			return withdrawPartys.stream()
					.collect(Collectors.toMap(BusinessApplyParty::getPartyCode,
							applyParty -> applyParty));
	        
		}
		return null;
	}
	
	private List<BusinessApplyCharges> findNoBalanceWithdraws(boolean isContainsPasue){
		BusinessApplyCharges withdrawEntity = new BusinessApplyCharges();
		withdrawEntity.setChargesType(BillItemSubCate.BILL_ITEM_SOLD_PROXY_WITHDRAW.getCateCode());
		EntityWrapper<BusinessApplyCharges> wrapper = new EntityWrapper<>(withdrawEntity);
		if (isContainsPasue) {
			wrapper.in("status", new Object[] {
					BusinessApplyStatus.START.getCateCode(),
					BusinessApplyStatus.PAUSE.getCateCode()
			});
		} else {
			wrapper.eq("status", BusinessApplyStatus.START.getCateCode());
		}
		
		return this.businessApplyBusiService
				.getBusinessApplyChargesService().selectList(wrapper);
	}

	/**   
	 * <p>Title: cashQuerys</p>   
	 * <p>Description: </p>   
	 * @param page
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IWithdrawService#cashQuerys(com.baomidou.mybatisplus.plugins.Page)   
	 */  
	@Override
	public Page<CashQuerysVo> cashQuerys(CashQuerysDto page) {
		log.debug("查询参数：{}",JSON.toJSONString(page));
		Page<CashQuerysVo> cashQuerysVoPage = new Page<>(page.getCurrent(), page.getSize());
		List<CashQuerysVo> cashQuerysVos = businessApplyBusiMapper.cashQuerys(cashQuerysVoPage, page);
		cashQuerysVoPage.setRecords(cashQuerysVos);
		log.debug("返回列表：{}",JSON.toJSONString(cashQuerysVoPage));
		return cashQuerysVoPage;
	}

	/**   
	 * <p>Title: initiateCashApplication</p>   
	 * <p>Description: </p>   
	 * @param cashQuerysDto
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IWithdrawService#initiateCashApplication(com.taolue.baoxiao.fund.api.dto.CashQuerysDto)   
	 */  
	@Override
	public Boolean initiateCashApplication(CashQuerysDto cashQuerysDto) {
		log.info("参数值：{}",JSON.toJSONString(cashQuerysDto));
		//返回值
		Boolean flag = false;
		String status = cashQuerysDto.getStatus();
		String applyCode = cashQuerysDto.getApplyCode();
		String applyBalanceCode = cashQuerysDto.getCode();
		
		EntityWrapper<BusinessApplyBalance> wrapper = new EntityWrapper<>();
		wrapper.eq("code", applyBalanceCode);
		BusinessApplyBalance applyBalance = new BusinessApplyBalance();
		applyBalance = applyBalance.selectOne(wrapper);
		
		if(null == applyBalance) {
			log.debug("applyBalance为空");
			return false;
		}
		BusinessApplyCharges applyCharge = this.businessApplyBusiService
				.getApplyChargeByCode(applyBalance.getApplyChargeCode());
		if(null == applyCharge) {
			log.debug("applyCharge为空");
			return false;
		}
		
		log.info("applyBalance:{}",applyBalance);
		log.info("applyCharge:{}",applyCharge);
		OrderApply orderApply = null;
		if(BusinessApplyStatus.STOP.getCateCode().equals(status)) {
			try {
				RemoteResultVo<BigDecimal> er = new RemoteResultVo<>();
				er.setData(applyBalance.getAmount());
				er.setCode(HttpStatus.HTTP_BAD_GATEWAY);
				er.setStatus(BusinessApplyStatus.FAULT);
				er.setMessage("失效上月请求单据，重新进行提现请求！！");
				orderApply = this.wrapperWithdrawBalanceByOldBalance(applyCharge, applyBalance, er);
			} catch (Exception e) {
				log.error(Exceptions.getStackTraceAsString(e));
			} 
		}else {
			RemoteResultVo<BigDecimal> er = this.openPlatformService.orderQuery(applyCode);
			orderApply = this.wrapperWithdrawBalanceByOldBalance(applyCharge, applyBalance, er);
		}
		
		if (null != orderApply) {
			log.info("orderApply:{}",orderApply);
			this.fireWithdrawRequest(orderApply);
			flag = true;
		}
		return flag;
	}
	
}
