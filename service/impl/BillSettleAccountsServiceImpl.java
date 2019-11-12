package com.taolue.baoxiao.fund.service.impl;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.MqQueueConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.IntelligentReimburseFlow;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderBusiStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderDetailStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TransType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.reimburseStatus;
import com.taolue.baoxiao.common.dto.AssignCouponDto;
import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.Result;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.coupon.RefactorCouponBuyApplyService;
import com.taolue.baoxiao.fund.api.dock.IOpenJiaBaiTiaoBuyCouponService;
import com.taolue.baoxiao.fund.api.dto.BillSettleAccountsDto;
import com.taolue.baoxiao.fund.api.dto.OrderDTO;
import com.taolue.baoxiao.fund.api.dto.OrderDetailDTO;
import com.taolue.baoxiao.fund.api.dto.OrderListDto;
import com.taolue.baoxiao.fund.api.invoice.IRefactorInvoiceReimburseService;
import com.taolue.baoxiao.fund.api.invoice.IRefactorInvoiceServiceRateConfigService;
import com.taolue.baoxiao.fund.api.invoice.IRefactorReimburseAmountSetService;
import com.taolue.baoxiao.fund.api.member.RefactorMemberPlatformService;
import com.taolue.baoxiao.fund.api.member.RefactorMemberRelationApi;
import com.taolue.baoxiao.fund.api.vo.BillBaseNewVo;
import com.taolue.baoxiao.fund.api.vo.BillSettleAccountsVo;
import com.taolue.baoxiao.fund.api.vo.FundIntelligentReimburseRecordVo;
import com.taolue.baoxiao.fund.api.vo.OrderDetailVo;
import com.taolue.baoxiao.fund.api.vo.OrderVo;
import com.taolue.baoxiao.fund.entity.BillSettleAccounts;
import com.taolue.baoxiao.fund.entity.FundBuyJbtRecord;
import com.taolue.baoxiao.fund.entity.FundCouponTaxCode;
import com.taolue.baoxiao.fund.entity.FundIntelligentReimburseRecordDetail;
import com.taolue.baoxiao.fund.entity.Order;
import com.taolue.baoxiao.fund.entity.OrderCouponScene;
import com.taolue.baoxiao.fund.entity.OrderSeparateBill;
import com.taolue.baoxiao.fund.mapper.BillSettleAccountsMapper;
import com.taolue.baoxiao.fund.mapper.FundCouponTaxCodeMapper;
import com.taolue.baoxiao.fund.mapper.OrderCouponSceneMapper;
import com.taolue.baoxiao.fund.service.IBillSettleAccountsService;
import com.taolue.baoxiao.fund.service.IFundBuyJbtRecordService;
import com.taolue.baoxiao.fund.service.IFundIntelligentReimburseRecordDetailService;
import com.taolue.baoxiao.fund.service.IFundIntelligentReimburseRecordService;
import com.taolue.baoxiao.fund.service.IOrderDetailService;
import com.taolue.baoxiao.fund.service.IOrderSeparateBillService;
import com.taolue.baoxiao.fund.service.IOrderService;
import com.taolue.coupon.api.dto.CouponBuyApplyDTO;
import com.taolue.dock.api.common.DockR;
import com.taolue.dock.api.vo.JiaBaiTiaoReturnVo;
import com.taolue.invoice.api.dto.InvoiceServiceRateConfigDto;
import com.taolue.invoice.api.dto.ReimburseAmountSetDto;
import com.taolue.invoice.api.dto.ReimburserQueryDTO;
import com.taolue.invoice.api.vo.InvoiceServiceRateConfigVo;
import com.taolue.invoice.api.vo.ReimburseAmountSetVo;
import com.taolue.invoice.api.vo.ReimburseVO;
import com.taolue.member.api.common.constant.enums.ResultEnum;
import com.taolue.member.api.vo.PlatformVo;
import com.taolue.member.api.vo.RelationVo;
import com.xiaoleilu.hutool.bean.BeanUtil;
import com.xiaoleilu.hutool.collection.CollUtil;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 结算订单表 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-05-27
 */
@Slf4j
@Service
public class BillSettleAccountsServiceImpl extends ServiceImpl<BillSettleAccountsMapper, BillSettleAccounts> implements IBillSettleAccountsService {

	@Autowired
	BillSettleAccountsMapper billSettleAccountsMapper;
	@Autowired
	RefactorMemberPlatformService refactorMemberPlatformService;
	@Autowired
	IOrderDetailService orderDetailService;
	@Autowired
	RefactorMemberRelationApi refactorMemberRelationApi;
	@Autowired
	IRefactorReimburseAmountSetService refactorReimburseAmountSetService;
	@Autowired
	private OrderCouponSceneMapper orderCouponSceneMapper;
	@Autowired
	private FundCouponTaxCodeMapper fundCouponTaxCodeMapper;
	@Autowired
	IRefactorReimburseAmountSetService refactorReimburseAmountSetServiceApi;
	@Autowired 
	private IOrderService orderService;
	@Autowired
	public  RabbitTemplate rabbitTemplate;
	@Autowired
	private IRefactorInvoiceServiceRateConfigService invoiceServiceRateConfigService;
	@Autowired
	private IFundIntelligentReimburseRecordService fundIntelligentReimburseRecordService;
	@Autowired
	private RefactorCouponBuyApplyService refactorCouponBuyApplyService;
	@Autowired
	private IOpenJiaBaiTiaoBuyCouponService openJiaBaiTiaoBuyCouponService;
	@Autowired
	private IFundBuyJbtRecordService fundBuyJbtRecordService;
	@Autowired
	private IFundIntelligentReimburseRecordDetailService fundIntelligentReimburseRecordDetailService;
	
	@Autowired
	private IOrderSeparateBillService orderSeparateBillService;
	
	@Autowired
	private IRefactorInvoiceReimburseService refactorInvoiceReimburseService;
	
	
	@Override
	@Transactional(rollbackFor = {Exception.class }) 
	public boolean addBillSettleAccounts(BillSettleAccountsDto dto) throws Exception {
		log.info("添加结算订单请求参数，request param dto:{}",JSON.toJSON(dto));
		String settleNo = null;
		if(dto.getSourceType().equals(CommonConstant.STATUS_DEL)) {
			//拆单逻辑
			BillSettleAccountsDto rtnDto=separateBill(dto, dto.getBillItemList().get(0).getSettleNo());
			dto=rtnDto;
			/**结算订单类型**/
			List<BillSettleAccounts> entityList = Lists.newArrayList();
			for (BillSettleAccountsDto item : dto.getBillItemList()) {
				settleNo = item.getSettleNo();
				BillSettleAccounts entity = new BillSettleAccounts();
				BeanUtil.copyProperties(item, entity);
				entity.setName(dto.getName());
				entity.setCompanyId(dto.getCompanyId());
				entity.setCompanyName(dto.getCompanyName());
				entity.setMemberId(dto.getMemberId());
				BigDecimal cashBackAmount = getCashBackAmount(entity);
				if(cashBackAmount != null && cashBackAmount.doubleValue() > 0 ) {
					entity.setCashBackAmount(cashBackAmount);
					// addReturnMoneyOrder(entity); // 添加返现订单
				}else {
					entity.setCashBackAmount(new BigDecimal(0));
				}
				entityList.add(entity);
			}
			log.info("添加结算订单请求参数，request param dto:{}",JSON.toJSONString(entityList));
			boolean isOK = this.insertBatch(entityList);
			if(!isOK) {
				log.error("消费/购买类型订单生成结算订单失败，错误原因isOK:{}",isOK);
				throw new Exception("消费/购买类型订单生成结算订单失败");
			}
			/**扣减额度***/
			ReimburseAmountSetDto amountSetDto = new ReimburseAmountSetDto();
			amountSetDto.setOperationType(CommonConstant.STATUS_BALANCE_COMPOS_READY);
			amountSetDto.setType(CommonConstant.STATUS_DEL);
			amountSetDto.setOperationAmount(dto.getSettleAmountSum());
			amountSetDto.setCompanyId(dto.getCompanyId());
			amountSetDto.setCompanyName(dto.getCompanyName());
			amountSetDto.setMemberId(dto.getMemberId());
			amountSetDto.setMemberName(dto.getMemberName());
			amountSetDto.setPhone(dto.getMoblie());
			log.info("消费/购买类型订单生成结算订单后》》调用invoice服务》》扣减员工报销额度请求参数，request param amountSetDto:{}",JSON.toJSON(amountSetDto));
			reduceMemberAmount(amountSetDto); //扣减员工报销额度
			if(CollectionUtil.isNotEmpty(dto.getBillNoList())) {
				log.info("开始修改订单状态变成已结算》》》》开始》》》》》");
				/**修改订单状态变成已结算***/
				OrderDTO orderDto = new OrderDTO();
				orderDto.setOrderNoList(dto.getBillNoList()); 
				orderDto.setStatus(OrderBusiStatus.SETTLED.getCateCode()); //已结算状态
				orderDto.setSourceCode(settleNo); // 结算单号
				this.editOrderStatus(orderDto); // 修改订单状态
			}

			return true;
		}else {
			/**简易报销类型***/
			BillSettleAccounts entity = new BillSettleAccounts();
			BeanUtil.copyProperties(dto, entity);
			boolean isOK = this.insert(entity);
			if(!isOK) {
				log.error("简易报销类型订单生成结算订单失败，错误原因isOK：{}",isOK);
				throw new Exception("简易报销类型订单生成结算单失败");
			}
			return isOK;
		}
	}

	public BigDecimal getCashBackAmount(BillSettleAccounts billSettleAccounts) throws Exception{
		log.info("开始计算返现金额>>》》》》》》》》》》》》》》》》");
		BigDecimal invoiceServiceAmount = new BigDecimal(0);//发票服务费
		String couponId="";
		if("2".equals(billSettleAccounts.getSettleType())){//消费
			OrderCouponScene scene=new OrderCouponScene();
			scene.setSceneCode(billSettleAccounts.getBillService());
			scene.setDelFlag("0");
			OrderCouponScene couponScene=orderCouponSceneMapper.selectOne(scene);
			log.info("通过结算单的场景code获取消费券id返回值：couponScene:{}"+JSON.toJSONString(couponScene));
			if(ObjectUtils.isEmpty(couponScene)) {
				throw new BaoxiaoException("通过场景code没有查询到券id");
			}
			couponId=couponScene.getCouponId();
		}else if("3".equals(billSettleAccounts.getSettleType())){//购买
			couponId=billSettleAccounts.getBillService();
		}
		Map<String,Object> taxCodeMap=Maps.newHashMap();
		taxCodeMap.put("coupon_id", couponId);
		List<FundCouponTaxCode> taxCodeList=fundCouponTaxCodeMapper.selectByMap(taxCodeMap);
		log.info("得到消费券费率等信息:"+JSON.toJSONString(taxCodeList));
		if(null==taxCodeList || taxCodeList.size()<=0) {
			throw new BaoxiaoException("没有查询到消费券的费率信息");
		}
		BigDecimal invoiceAmount=new BigDecimal(0);
		BigDecimal serviceRate=new BigDecimal(0);
		FundCouponTaxCode fundCouponTaxCode=taxCodeList.get(0);
		//专票
		if("1".equals(fundCouponTaxCode.getInvoiceType()) || "2".equals(fundCouponTaxCode.getInvoiceType())) {
			invoiceAmount =billSettleAccounts.getSettleAmount();
			serviceRate = getServiceRate(billSettleAccounts.getCompanyId());
			log.info("专票总金额:"+invoiceAmount+",税点:"+serviceRate);
			invoiceServiceAmount=invoiceAmount.multiply(serviceRate).divide(new BigDecimal(10000));
		}
		log.info("发票服务费用为:"+invoiceServiceAmount);
		return invoiceServiceAmount;
	}
	@Override
	public List<BillSettleAccountsVo> findBillSettleAccounts(BillSettleAccountsDto dto) {
//		status 结算单状态 0：待审核， 1：已生成账单（待打款），2：已打款，3：撤销，4：拒绝
//		报销单状态  1:待审核， 3：待打款，4：拒绝  5：撤销，6：打款处理中，7：打款失败， 8:部分打款，10:系统打款，11:手动打款
		log.info("查询所有的结算单信息，请求参数 request param dto:{}",JSON.toJSON(dto));
		ReimburserQueryDTO reimburseDto = new ReimburserQueryDTO();
		reimburseDto.setDelFlag(CommonConstant.STATUS_NORMAL);
		if(dto != null && org.apache.commons.lang.StringUtils.isNotBlank(dto.getMemberId())) {
			reimburseDto.setMemberId(dto.getMemberId());
		}
		if(dto != null && org.apache.commons.lang.StringUtils.isNotBlank(dto.getCompanyId())) {
			reimburseDto.setCompanyId(dto.getCompanyId());
		}
		log.info("查询购买、消费结算单信息，请求参数 request param dto:{}",JSON.toJSON(dto));
		List<BillSettleAccountsVo> resultList = billSettleAccountsMapper.findBillSettleAccounts(dto);
		log.info("查询购买、消费结算单信息，返回结果 return result list:{}",JSON.toJSON(resultList));
		List<ReimburseVO> reimburseList = Lists.newArrayList();
		if(dto.getStatusList() == null || dto.getStatusList().size() == 0) {
			String[] status = new String[]{"1","3","4","5","10","11"};
			reimburseDto.setStatus(status);
			log.info("关联查询报销单请求参数 request param reimburseDto:{}",JSON.toJSON(reimburseDto));
			reimburseList = refactorInvoiceReimburseService.reimburseList(reimburseDto);
			log.info("全部状态下》》关联报销单信息返回结果，return result reimburseList:{}", JSON.toJSON(reimburseList));
			if(CollUtil.isNotEmpty(reimburseList)) {
				List<BillSettleAccountsVo> addList = Lists.newArrayList();
				for (ReimburseVO reimburseVO : reimburseList) {
					BillSettleAccountsVo vo = new BillSettleAccountsVo();
					BeanUtil.copyProperties(reimburseVO, vo);
					vo.setName(DictionaryEnum.BillSettleType.settle_type_1.getName()+"-"+reimburseVO.getCategoryName());
					if(reimburseVO.getStatus().equals("1")) {
						vo.setStatus(DictionaryEnum.billSettleAccountsStatus.INIT_STATUS.getCode());
					}else if(reimburseVO.getStatus().equals("3")) {
						vo.setStatus(DictionaryEnum.billSettleAccountsStatus.ALREADY_ORDER.getCode());
					}else if(reimburseVO.getStatus().equals("4")) {
						vo.setStatus(DictionaryEnum.billSettleAccountsStatus.STATUS_4.getCode());
					}else if(reimburseVO.getStatus().equals("5")) {
						vo.setStatus(DictionaryEnum.billSettleAccountsStatus.STATUS_3.getCode());
					}else if(reimburseVO.getStatus().equals("10")) {
						vo.setStatus(DictionaryEnum.billSettleAccountsStatus.STATUS_2.getCode());
					}
					vo.setStatusName(reimburseStatus.existsAct(reimburseVO.getStatus()));
					vo.setCreateTime(reimburseVO.getReimbDate());
					vo.setSettleAmount(reimburseVO.getTotalAmt());
					vo.setRemark("报销单信息");
					vo.setFlag("1");
					addList.add(vo);
				}
				resultList.addAll(addList);
			}
		} else if(dto.getStatusList().contains("0")){
			reimburseDto.setReimburseStatus("1");
			log.info("待审核状态下》》关联查询报销单请求参数 request param reimburseDto:{}",JSON.toJSON(reimburseDto));
			reimburseList = refactorInvoiceReimburseService.reimburseList(reimburseDto);
			log.info("待审核状态下》》关联报销单信息返回结果，return result reimburseList:{}", JSON.toJSON(reimburseList));
			List<BillSettleAccountsVo> addList = Lists.newArrayList();
			if(CollUtil.isNotEmpty(reimburseList)) {
				for (ReimburseVO reimburseVO : reimburseList) {
					BillSettleAccountsVo vo = new BillSettleAccountsVo();
					BeanUtil.copyProperties(reimburseVO, vo);
					vo.setName(DictionaryEnum.BillSettleType.settle_type_1.getName()+"-"+reimburseVO.getCategoryName());
					if(reimburseVO.getStatus().equals("1")) {
						vo.setStatus(DictionaryEnum.billSettleAccountsStatus.INIT_STATUS.getCode());
					}
					vo.setStatusName(DictionaryEnum.reimburseStatus.existsAct(reimburseVO.getStatus()));
					vo.setCreateTime(reimburseVO.getReimbDate());
					vo.setSettleAmount(reimburseVO.getTotalAmt());
					vo.setRemark("报销单信息");
					vo.setFlag("1");
					addList.add(vo);
				}
				resultList.addAll(addList);
			}
		} else if(dto.getStatusList().contains("1")){
			reimburseDto.setReimburseStatus("3");
			log.info("待打款状态下》》关联查询报销单请求参数 request param reimburseDto:{}",JSON.toJSON(reimburseDto));
			reimburseList = refactorInvoiceReimburseService.reimburseList(reimburseDto);
			log.info("待打款状态下》》关联报销单信息返回结果，return result reimburseList:{}", JSON.toJSON(reimburseList));
			List<BillSettleAccountsVo> addList = Lists.newArrayList();
			if(CollUtil.isNotEmpty(reimburseList)) {
				for (ReimburseVO reimburseVO : reimburseList) {
					BillSettleAccountsVo vo = new BillSettleAccountsVo();
					BeanUtil.copyProperties(reimburseVO, vo);
					vo.setName(DictionaryEnum.BillSettleType.settle_type_1.getName()+"-"+reimburseVO.getCategoryName());
					if(reimburseVO.getStatus().equals("3")) {
						vo.setStatus(DictionaryEnum.billSettleAccountsStatus.ALREADY_ORDER.getCode());
					}
					vo.setStatusName(reimburseStatus.existsAct(reimburseVO.getStatus()));
					vo.setCreateTime(reimburseVO.getReimbDate());
					vo.setSettleAmount(reimburseVO.getTotalAmt());
					vo.setRemark("报销单信息");
					vo.setFlag("1");
					addList.add(vo);
				}
				resultList.addAll(addList);
			}
		} else if(dto.getStatusList().contains("2")){
			String[] status = new String[]{"10","11"};
			reimburseDto.setStatus(status);
			log.info("已打款状态下》》关联查询报销单请求参数 request param reimburseDto:{}",JSON.toJSON(reimburseDto));
			reimburseList = refactorInvoiceReimburseService.reimburseList(reimburseDto);
			log.info("已打款状态下》》关联报销单信息返回结果，return result reimburseList:{}", JSON.toJSON(reimburseList));
			List<BillSettleAccountsVo> addList = Lists.newArrayList();
			if(CollUtil.isNotEmpty(reimburseList)) {
				for (ReimburseVO reimburseVO : reimburseList) {
					BillSettleAccountsVo vo = new BillSettleAccountsVo();
					BeanUtil.copyProperties(reimburseVO, vo);
					vo.setName(DictionaryEnum.BillSettleType.settle_type_1.getName()+"-"+reimburseVO.getCategoryName());
					if(reimburseVO.getStatus().equals("10")) {
						vo.setStatus(DictionaryEnum.billSettleAccountsStatus.STATUS_2.getCode());
					}
					vo.setStatusName(reimburseStatus.existsAct(reimburseVO.getStatus()));
					vo.setCreateTime(reimburseVO.getReimbDate());
					vo.setSettleAmount(reimburseVO.getTotalAmt());
					vo.setRemark("报销单信息");
					vo.setFlag("1");
					addList.add(vo);
				}
				resultList.addAll(addList);
			}
		} else if(dto.getStatusList().contains("3") && dto.getStatusList().contains("4")){
			String[] status = new String[]{"4","5"};
			reimburseDto.setStatus(status);
			log.info("撤销、拒绝状态下》》关联查询报销单请求参数 request param reimburseDto:{}",JSON.toJSON(reimburseDto));
			reimburseList = refactorInvoiceReimburseService.reimburseList(reimburseDto);
			log.info("撤销、拒绝状态下》》关联报销单信息返回结果，return result reimburseList:{}", JSON.toJSON(reimburseList));
			List<BillSettleAccountsVo> addList = Lists.newArrayList();
			if(CollUtil.isNotEmpty(reimburseList)) {
				for (ReimburseVO reimburseVO : reimburseList) {
					BillSettleAccountsVo vo = new BillSettleAccountsVo();
					BeanUtil.copyProperties(reimburseVO, vo);
					vo.setName(DictionaryEnum.BillSettleType.settle_type_1.getName()+"-"+reimburseVO.getCategoryName());
					if(reimburseVO.getStatus().equals("4")) {
						vo.setStatus(DictionaryEnum.billSettleAccountsStatus.STATUS_4.getCode());
					}else if(reimburseVO.getStatus().equals("5")) {
						vo.setStatus(DictionaryEnum.billSettleAccountsStatus.STATUS_3.getCode());
					}
					vo.setStatusName(reimburseStatus.existsAct(reimburseVO.getStatus()));
					vo.setCreateTime(reimburseVO.getReimbDate());
					vo.setSettleAmount(reimburseVO.getTotalAmt());
					vo.setRemark("报销单信息");
					vo.setFlag("1");
					addList.add(vo);
				}
				resultList.addAll(addList);
			}
		}
		// list 排序 按照时间倒序
		List<BillSettleAccountsVo> resultListSort =  resultList != null ? resultList.stream().sorted(Comparator.comparing(BillSettleAccountsVo::getCreateTime).reversed()).collect(Collectors.toList()) : null;
		return resultListSort;
	}
	
	@Override
	public List<BillSettleAccountsVo> findBillSettleInfoList(BillSettleAccountsDto dto) {
		List<BillSettleAccountsVo> result = billSettleAccountsMapper.findBillSettleAccounts(dto);
		return result;
	}
	
	@Override
	public List<BillSettleAccountsDto> findBillSettleAccountDetail(BillSettleAccountsDto dto) {
		List<BillSettleAccountsDto> result = billSettleAccountsMapper.findBillSettleAccountDetail(dto);
		return result;
	}
	/**
	 * 
	 * 
	 * @Title BillSettleAccountsServiceImpl.reduceMemberAmount
	 * @Description: 额度扣减
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月04日 下午4:51:59
	 * 修改内容 :
	 * @throws Exception 
	 */
	@Override
	@Transactional(rollbackFor = {Exception.class }) 
	public boolean reduceMemberAmount(ReimburseAmountSetDto dto) throws Exception {
		log.info("扣减/增加  员工报销额度，调用invoice服务》》开始》》请求参数，request param dto:{}",JSON.toJSON(dto));
		R<Boolean> r = refactorReimburseAmountSetService.operationMemberAmount(dto);
		log.info("扣减/增加 员工报销额度，调用invoice服务》》结束》》返回结果，return result r:{}",JSON.toJSON(r));
		if(r == null || R.SUCCESS != r.getCode()) {
			log.error("扣减员工报销额度，调用invoice服务》》返回异常，return result r:{}",JSON.toJSON(r));
			throw new BaoxiaoException(ResultEnum.SYSTEM_ERROR.getCode(), "扣减员工报销额度异常");
		}
		return true;
	}

	@Override
	public Page<BillSettleAccountsDto> findPageBillSettleAccounts(Query query, BillSettleAccountsDto queryParams) {
		query.setRecords(billSettleAccountsMapper.findPageBillSettleAccounts(query, queryParams));
		return query;
	}
	
	@Override
	public List<PlatformVo> findMemberPlatform(PlatformVo vo) {
		List<PlatformVo> voList = refactorMemberPlatformService.findMemberPlatform(vo);
		return voList;
	}

	@Override
	public Page<BillSettleAccountsDto> findSettlementDetailList(Query query, BillSettleAccountsDto queryParams) {
		query.setRecords(billSettleAccountsMapper.findSettlementDetailList(query, queryParams));
		return query;
	}

	@Override
	public RelationVo queryDistinctPostByMemberId(String memberId,String companyId) {
		R<RelationVo> voList = refactorMemberRelationApi.queryDistinctPostByMemberIdNew(memberId,companyId);
		return voList.getData();
	}

	@Override
	public RelationVo queryDistinctRankByMemberId(String memberId,String companyId) {
		R<RelationVo> voList = refactorMemberRelationApi.queryDistinctRankByMemberIdNew(memberId,companyId);
		return voList.getData();
	}

	@Override
	public BillSettleAccountsDto findSettlementDetail(BillSettleAccountsDto queryParams) {
		List<BillSettleAccountsDto> dtoList = billSettleAccountsMapper.findSettlementDetail(queryParams);
		BillSettleAccountsDto dto = new BillSettleAccountsDto();
		BigDecimal totalAmount = new BigDecimal(0);
		for(BillSettleAccountsDto billDto: dtoList) {
			totalAmount = totalAmount.add(billDto.getSettleAmount());
			if("1".equals(billDto.getSettleType())) {
				dto.setReimbursementAmount(billDto.getSettleAmount());
			}else if("2".equals(billDto.getSettleType())) {
				dto.setCashAmount(billDto.getSettleAmount());
			}else if("3".equals(billDto.getSettleType())) {
				dto.setCouponAmount(billDto.getSettleAmount());
			}
			dto.setMemberId(billDto.getMemberId());
			dto.setCompanyId(billDto.getCompanyId());
			dto.setCreateTime(billDto.getCreateTime());
		}
		dto.setTotalAmount(totalAmount);
		return dto;
	}

	@Override
	public List<BillSettleAccountsDto> findSettlementAmountByNo(BillSettleAccountsDto queryParams) {
		return this.baseMapper.findSettlementAmountByNo(queryParams);
	}

	@Override
	public boolean editStatusByNos(BillSettleAccountsDto dto) {
		return this.baseMapper.editStatusByNos(dto);
	}

	/**   
	 * <p>Title: findPageBillBillDetail</p>   
	 * <p>Description: 员工报销汇总</p>   
	 * @param query
	 * @param queryParams
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.IBillSettleAccountsService#findPageBillBillDetail(com.taolue.baoxiao.common.util.Query, com.taolue.baoxiao.fund.api.dto.BillSettleAccountsDto)   
	 */  
	@Override
	public Page<BillSettleAccountsDto> findPageBillDetail(Query query, BillSettleAccountsDto queryParams) {
		query.setRecords(billSettleAccountsMapper.findPageBillDetail(query, queryParams));
		return query;
	}

	/**   
	 * <p>Title: findPageBillDetailBybillNumber</p>   
	 * <p>Description:报销明细 </p>   
	 * @param query
	 * @param queryParams
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.IBillSettleAccountsService#findPageBillDetailBybillNumber(com.taolue.baoxiao.common.util.Query, com.taolue.baoxiao.fund.api.dto.BillSettleAccountsDto)   
	 */  
	@Override
	public Page<BillSettleAccountsDto> findPageBillDetailBybillNumber(Query query, BillSettleAccountsDto queryParams) {
		query.setRecords(billSettleAccountsMapper.findPageBillDetailBybillNumber(query, queryParams));
		return query;
	}
	/**
	 * 
	 * @Title: findAmountByMember   
	 * @Description: 通过账单号查询每个人的报销金额
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: List<BillSettleAccountsVo>      
	 * @throws
	 */
	public List<BillSettleAccountsDto> findAmountByMember(BillSettleAccountsDto dto){
		return this.baseMapper.findAmountByMember(dto);
	}
	
	
	
	/**
	 * 
	 * 
	 * @Title BillSettleAccountsServiceImpl.editOrderStatus
	 * @Description: 批量编辑订单信息
	 *
	 * @param orderDto
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月25日 下午1:45:21
	 * 修改内容 :
	 * @throws Exception 
	 */
	@Transactional(rollbackFor = {Exception.class }) 
	public boolean editOrderStatus(OrderDTO orderDto) throws Exception {
		log.info("批量编辑订单信息请求参数 request param dto:{}"+JSON.toJSON(orderDto));
		if(ObjectUtils.isEmpty(orderDto) || CollUtil.isEmpty(orderDto.getOrderNoList())) {
			throw new BaoxiaoException("参数为空或订单号为空");
		}
		boolean isOK = orderService.editOrderStatus(orderDto);
		log.info("批量编辑订单信息返回结果，return result isOK:{}", isOK);
		if(!isOK) {
			throw new BaoxiaoException("批量修改订单状态信息失败");
		}
		return isOK;
	}
	
	
	
	
	/**
	 * 
	 * 
	 * @Title BillSettleAccountsServiceImpl.getServiceRate
	 * @Description: 获取服务费率
	 *
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月26日 下午12:04:24
	 * 修改内容 :
	 */
	public BigDecimal getServiceRate(String companyId) {
		BigDecimal serviceRate = new BigDecimal(0);
		/*CommonDictDto dictDto = new CommonDictDto();
	    dictDto.setType("6");
	    R<List<CommonDictVo>> dictR = refactorCommonDictService.getDictList(dictDto);*/
		InvoiceServiceRateConfigDto rateConfigDto=new InvoiceServiceRateConfigDto();
		rateConfigDto.setCompanyId(companyId);
		rateConfigDto.setIsPlatform("1");
		rateConfigDto.setIsSet("1");
		R<InvoiceServiceRateConfigVo> rateConfig=invoiceServiceRateConfigService.findInvoiceServiceRateConfigByParam(rateConfigDto);
		
	
	    log.info("查询返还员工服务费率返回值 return result rateConfig:{}", JSON.toJSONString(rateConfig));
	    if(null == rateConfig || rateConfig.getCode() != R.SUCCESS) {
	    	throw new BaoxiaoException("查询返还员工服务费率失败");
	    }
	    if(ObjectUtils.isEmpty(rateConfig.getData())) {
	    	log.error("查询返还员工服务费率返回值为空 return result is null");
	    }else {
	        serviceRate =rateConfig.getData().getReturnRate();
	    }
	    return serviceRate;
	}
	
	
	
	
	/**
	 * 
	 * 
	 * @Title BillSettleAccountsServiceImpl.addReturnMoneyOrder
	 * @Description: 添加返现类型订单
	 *
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月26日 下午12:13:37
	 * 修改内容 :
	 */
	@Transactional(rollbackFor = {Exception.class }) 
	public boolean addReturnMoneyOrder(BillSettleAccounts dto) throws Exception{
		log.info("开始添加返现类型订单开始》》》》");
		//返现订单
	    OrderDTO order = new OrderDTO();
	    String orderNo = CodeUtils.genneratorShort("FX");
	    order.setOrderCode(orderNo);
	    order.setOrderNo(orderNo);
	    //order.setBusinessId(queryParams.getVendorId());
	    //order.setBusinessName(applyInvoiceDto.getVendorName());
	    order.setCompanyId(dto.getCompanyId());
	    order.setCompanyName(dto.getCompanyName());
	    order.setMainType(TransType.CASHBACK.getSysCode());
	    order.setMainTypeName(TransType.CASHBACK.getSysName());
	    order.setSubType(TransType.CASHBACK.getSysCode());
	    order.setSubTypeName(TransType.CASHBACK.getSysName());
	    order.setOrderAmount(dto.getCashBackAmount());
	    //order.setPayAmount(applyInvoiceDto.getTaxIncludedPrice());
	    order.setCouponAmount(new BigDecimal(0));
	    order.setMemberId(dto.getMemberId());
	    order.setSourceType("1");
	    order.setStatus(OrderBusiStatus.PENDING_PAYMENT.getCateCode());
	    order.setSourceCode(dto.getSettleNo()); // 结算单号
	    log.info("添加返现订单的入参参数 order:{}", JSON.toJSONString(order));
	    OrderDetailDTO detail = new OrderDetailDTO();
		  detail.setPaymentCate(BillItemSubCate.BILL_ITEM_SUBCATE_XFXJ.getCateCode());
		  detail.setPaymentAmount(dto.getCashBackAmount());
		  detail.setBusiModle(BusiModelEnums.BUSI_MODEL_NONE.getCateCode());
		  detail.setPaymentMemberCate(MemberCateEnums.MEMBER_CATE_EMP.getCateCode());
		  detail.setPaymentAcctCate(MemberCateEnums.MEMBER_CATE_EMP.getCateCode()+AcctCateEnums.ACCT_CATE_SALARY.getCateCode());
		  detail.setPaymentItemNo("");
		  detail.setPaymentItemName("");
		  detail.setStatus(OrderDetailStatus.SUCCESS.getCateCode());
		  detail.setPaymentMemberId(dto.getMemberId());
		  //detail.setPaymentVendorId(applyInvoiceDto.getVendorId());
		  //detail.setPaymentVendorName(applyInvoiceDto.getVendorName());
		  //detail.setBillNo(dto.getBillNo());//申请id
		  detail.setPaymentIndustryId("");//行业
		  detail.setPaymentIndustryName("");
		  log.info("添加返现订单明细的入参参数:{}"+JSON.toJSONString(detail));
		  List<OrderDetailDTO> detailDtoList = Lists.newArrayList();
	      detailDtoList.add(detail);
	      order.setDetailDtoList(detailDtoList);
	      log.info("添加返现所有订单明细入参参数为:{}"+JSON.toJSONString(order));
	      boolean isOK = orderService.addOrder(order);
	      if(!isOK) {
	    	  throw new Exception("添加返现订单失败");
	      }
	      return true;
	}

	@Override
	public Result<Boolean> intelligentReimburse(BillSettleAccountsDto dto) {
		Result<Boolean> r = new Result<Boolean>();
		//查询是否首次智能报销
		FundIntelligentReimburseRecordVo fundVo = new FundIntelligentReimburseRecordVo();
		fundVo.setMemberId(dto.getMemberId());
		fundVo.setCompanyId(dto.getCompanyId());
		fundVo.setStatus("2");
		log.info("开始查询是否首次智能报销传入参数》》》》{}"+JSON.toJSONString(dto));
		FundIntelligentReimburseRecordVo findCouponTaxCod = fundIntelligentReimburseRecordService.findCouponTaxCod(fundVo);
		log.info("查询是否首次智能报销返回结果》》》》{}"+JSON.toJSONString(findCouponTaxCod));
		//查询是否首次智能报销 如果不是说明是再次发起
		if(ObjectUtil.isNotNull(findCouponTaxCod) && "2".equals(findCouponTaxCod.getStatus())) {
			boolean isSplete=false;
			if(findCouponTaxCod.getReimburseAmount().compareTo(findCouponTaxCod.getOrderAmount())<0) {
				isSplete=true;//拆单
			}
			r = reLaunchReimburse(findCouponTaxCod,isSplete);
		}else{
			//查询是否首次智能报销 如果是说明是首次发起
			r = firstLaunchReimburse(dto);
		}
		return r;
	}

	private Result<Boolean> firstLaunchReimburse(BillSettleAccountsDto dto) {
		Result<Boolean> r = new Result<Boolean>();
		log.info("开始智能报销》》》》{}"+JSON.toJSONString(dto));
		FundIntelligentReimburseRecordVo fvo = new FundIntelligentReimburseRecordVo();
		String orderCode = CodeUtils.genneratorShort("GM");
		dto.setOrderApplyNo(orderCode);
		BigDecimal operationAmount=new BigDecimal(0);
		boolean isSplete=false;
		if(dto.getReimburseAmount().compareTo(dto.getOrderAmount())<0) {
			operationAmount=dto.getReimburseAmount();
			isSplete=true;
		}else {
			operationAmount = dto.getCouponAmount().add(dto.getOrderAmount());
		}
	
		//1.冻结额度并将可报销订单待提交状态
		ReimburseAmountSetDto reimburseAmountSetDto = new ReimburseAmountSetDto();
		reimburseAmountSetDto.setCompanyId(dto.getCompanyId());
		reimburseAmountSetDto.setMemberId(dto.getMemberId());
		reimburseAmountSetDto.setOperationType("0");
		reimburseAmountSetDto.setType("2");
		reimburseAmountSetDto.setOperationAmount(operationAmount);
		log.info("开始冻结额度传入参数》》》》{}"+JSON.toJSONString(dto));
		R<Boolean> roperation = refactorReimburseAmountSetService.operationMemberAmount(reimburseAmountSetDto);
		log.info("冻结额度返回结果》》》》{}"+JSON.toJSONString(roperation));
		
		dto.setFlowName(IntelligentReimburseFlow.FREE_REIMBURSE_AMOUNT.getCateName());
		dto.setFlowNo(IntelligentReimburseFlow.FREE_REIMBURSE_AMOUNT.getCateCode()+"");
		dto.setStatus(roperation.getData() ? "1" : "2");
		dto.setSort(IntelligentReimburseFlow.FREE_REIMBURSE_AMOUNT.getCateMgn()+"");
		dto.setBusiReturn(JSON.toJSONString(roperation));
		log.info("智能报销流程记录扣减报销额度传入参数》》》》"+JSON.toJSONString(dto));
		fvo = this.addCouponTaxCod(dto);
		dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
		log.info("智能报销流程记录扣减报销额度返回结果》》》》"+JSON.toJSONString(fvo));
		
		if(null == roperation || R.SUCCESS != roperation.getCode()) {
			r.setCode(R.FAIL);
			r.setMsg("扣减报销额度失败！");
			r.setData(false);
			return r;
		}
		
		log.info("开始修改可报销订单为待提交状态传入参数》》》》"+JSON.toJSONString(dto));
		OrderListDto orderdto = new OrderListDto();
		orderdto.setMemberId(dto.getMemberId());
		orderdto.setCompanyId(dto.getCompanyId());
		orderdto.setSourceFrom("1");
		log.info("查询可报销订单传入参数》》》》"+JSON.toJSONString(orderdto));
		List<OrderListDto> dtoList = Lists.newArrayList();
		List<OrderListDto> dtoLists = orderService.queryOrderByUseBuy(orderdto);
		if(CollUtil.isNotEmpty(dtoLists)) {
			dtoList.addAll(dtoLists);
		}
		log.info("查询可报销订单返回结果》》》》"+JSON.toJSONString(dtoList));
		List<Order> orderList = Lists.newArrayList();
		if(!CollUtil.isEmpty(dtoList) && !isSplete) {
			for(OrderListDto ordto : dtoList) {
				Order ordervo = new Order();
				//BeanUtils.copyProperties(ordto, ordervo);
				ordervo.setId(ordto.getId());
				ordervo.setStatus(OrderBusiStatus.SETTLED_PROCESS.getCateCode());
				orderList.add(ordervo);
			}
			boolean result = orderService.updateBatchById(orderList);
			
			dto.setFlowName(IntelligentReimburseFlow.UPDATE_ORDER_STATUS.getCateName());
			dto.setFlowNo(IntelligentReimburseFlow.UPDATE_ORDER_STATUS.getCateCode()+"");
			dto.setStatus(result ? "1" : "2");
			dto.setSort(IntelligentReimburseFlow.UPDATE_ORDER_STATUS.getCateMgn()+"");
			dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
			dto.setMainId(fvo.getId());
			dto.setDetailId(fvo.getDetailId());
			dto.setBusiReturn(JSON.toJSONString(result));
			log.info("智能报销流程记录修改可报销订单为待提交状态传入参数》》》》"+JSON.toJSONString(dto));
			fvo = this.addCouponTaxCod(dto);
			log.info("智能报销流程记录修改可报销订单为待提交状态返回结果》》》》"+JSON.toJSONString(fvo));
			
			log.info("修改可报销订单为待提交状态返回结果》》》》"+JSON.toJSONString(result));
			if(!result) {
				r.setCode(R.FAIL);
				r.setMsg("可报销订单待提交状态修改失败！");
				r.setData(false);
				return r;
			}
		}
		
		// 2嘉白条记录买券扣减金额
		if(dto.getCouponAmount().compareTo(new BigDecimal("0")) > 0) {
			log.info("嘉白条记录买券扣减金额--通知嘉白条开始》》》》"+JSON.toJSONString(dto));
			DockR<JiaBaiTiaoReturnVo> jbtResult = openJiaBaiTiaoBuyCouponService.jiaBaiTiaoBuyCoupon(dto.getCouponAmount(),orderCode,dto.getMemberId(),dto.getCompanyId());
			log.info("嘉白条记录买券扣减金额--通知嘉白条返回结果》》》》"+JSON.toJSONString(jbtResult));
			dto.setBillNo(orderCode);
			log.info("开始保存购买券嘉白条关联表记录信息》》》》"+JSON.toJSONString(jbtResult));
			boolean result = this.addJbtRecord(jbtResult,dto);
			log.info("记录购买券嘉白条关联表信息返回结果》》》》"+JSON.toJSONString(result));
			
			dto.setFlowName(IntelligentReimburseFlow.JBT_DECREASE_AMOUNT.getCateName());
			dto.setFlowNo(IntelligentReimburseFlow.JBT_DECREASE_AMOUNT.getCateCode()+"");
			dto.setStatus(jbtResult.getCode().equals(DockR.SUCCESS) ? "1" : "2");
			dto.setSort(IntelligentReimburseFlow.JBT_DECREASE_AMOUNT.getCateMgn()+"");
			dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
			dto.setMainId(fvo.getId());
			dto.setDetailId(fvo.getDetailId());
			dto.setBusiReturn(JSON.toJSONString(jbtResult));
			log.info("智能报销流程记录嘉白条记录买券扣减金额传入参数》》》》"+JSON.toJSONString(dto));
			fvo = this.addCouponTaxCod(dto);
			log.info("智能报销流程记录嘉白条记录买券扣减金额返回结果》》》》"+JSON.toJSONString(fvo));
			
			if(null == jbtResult || !(DockR.SUCCESS.equals(jbtResult.getCode()))) {
				r.setCode(R.FAIL);
				r.setData(false);
				r.setMsg("嘉白条记录买券扣减金额结果失败");
				return r;
			}
					
			// 3.生成买券订单
			FundBuyJbtRecord record = new FundBuyJbtRecord();
			record.setIntelligentReimburseCode(dto.getIntelligentReimburseCode());
			record.setBuyCode(orderCode);
			fundBuyJbtRecordService.checkIntelligentReimburseCode(record);
			log.info("开始买券生成购买订单传入参数》》》》"+JSON.toJSONString(dto));
			R<CouponBuyApplyDTO> resultCoupon = this.buyCoupon(dto);
			log.info("买券生成购买订单返回结果》》》》"+JSON.toJSONString(resultCoupon));
			
			dto.setFlowName(IntelligentReimburseFlow.CREATE_BUY_ORDER.getCateName());
			dto.setFlowNo(IntelligentReimburseFlow.CREATE_BUY_ORDER.getCateCode()+"");
			dto.setStatus(resultCoupon.getCode() == R.SUCCESS ? "1" : "2");
			dto.setSort(IntelligentReimburseFlow.CREATE_BUY_ORDER.getCateMgn()+"");
			dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
			dto.setMainId(fvo.getId());
			dto.setDetailId(fvo.getDetailId());
			dto.setBusiReturn(JSON.toJSONString(resultCoupon));
			if(null == resultCoupon || R.SUCCESS != resultCoupon.getCode()) {
				dto.setBusiCode(orderCode);
			}
			dto.setBusiReturn(JSON.toJSONString(dto));
			log.info("智能报销流程记录买券生成购买订单传入参数》》》》"+JSON.toJSONString(dto));
			fvo = this.addCouponTaxCod(dto);
			log.info("智能报销流程记录买券生成购买订单返回结果》》》》"+JSON.toJSONString(fvo));
			
			if(null == resultCoupon || R.SUCCESS != resultCoupon.getCode()) {
				r.setCode(R.FAIL);
				r.setMsg("买券生成购买订单失败！");
				r.setData(false);
				return r;
			}
			OrderDTO vo = new OrderDTO();
			orderCode = resultCoupon.getData().getApplyNo();
			vo.setOrderCode(orderCode);
			OrderVo orderVo = orderService.getOrderInfo(vo);
			dto.setBillNo(orderVo.getOrderNo());
			dto.setBusiCode(orderCode);
			OrderListDto orderListDto = new OrderListDto();
			BeanUtils.copyProperties(orderVo, orderListDto);
			orderListDto.setCouponId(resultCoupon.getData().getCouponId());
			orderListDto.setCouponName(resultCoupon.getData().getCouponName());
			dtoList.add(orderListDto);
			log.info("嘉白条记录买券扣减金额--通知总部开始》》》》"+JSON.toJSONString(dto));
			
			//3.消费券入账并冻结
			log.info("消费券入账并冻结--发送MQ信息开始传入参数》》》》"+JSON.toJSONString(dto));
			this.sendBuyCouponMql(dto);
			log.info("消费券入账并冻结--发送MQ信息结束");
			dto.setFlowName(IntelligentReimburseFlow.FREE_COUPON_AMOUNT.getCateName());
			dto.setFlowNo(IntelligentReimburseFlow.FREE_COUPON_AMOUNT.getCateCode()+"");
			dto.setStatus("1");
			dto.setSort(IntelligentReimburseFlow.FREE_COUPON_AMOUNT.getCateMgn()+"");
			dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
			dto.setMainId(fvo.getId());
			dto.setDetailId(fvo.getDetailId());
			dto.setBusiReturn(JSON.toJSONString(dto));
			log.info("智能报销流程记录消费券额度冻结传入参数》》》》"+JSON.toJSONString(dto));
			fvo = this.addCouponTaxCod(dto);
			log.info("智能报销流程记录消费券额度冻结返回结果》》》》"+JSON.toJSONString(fvo));
		}
		
		
		//解冻智能报销额度
		log.info("开始解冻智能报销额度传入参数》》》》"+JSON.toJSONString(reimburseAmountSetDto));
		reimburseAmountSetDto.setOperationType("0");
		reimburseAmountSetDto.setType("0");
		R<Boolean> roperation2 = refactorReimburseAmountSetService.operationMemberAmount(reimburseAmountSetDto);
		log.info("解冻智能报销额度返回结果》》》》"+JSON.toJSONString(roperation2));
		
		dto.setFlowName(IntelligentReimburseFlow.UNFREE_REIMBURSE_AMOUNT.getCateName());
		dto.setFlowNo(IntelligentReimburseFlow.UNFREE_REIMBURSE_AMOUNT.getCateCode()+"");
		dto.setStatus(roperation2.getCode() == R.SUCCESS ? "1" : "2");
		dto.setSort(IntelligentReimburseFlow.UNFREE_REIMBURSE_AMOUNT.getCateMgn()+"");
		dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
		dto.setMainId(fvo.getId());
		dto.setDetailId(fvo.getDetailId());
		dto.setBusiReturn(JSON.toJSONString(roperation2));
		log.info("智能报销流程记录解冻智能报销额度传入参数》》》》"+JSON.toJSONString(dto));
		fvo = this.addCouponTaxCod(dto);
		log.info("智能报销流程记录解冻智能报销额度返回结果》》》》"+JSON.toJSONString(fvo));
		
		if(null == roperation2 || R.SUCCESS != roperation2.getCode()) {
			r.setCode(R.FAIL);
			r.setData(false);
			r.setMsg("解冻智能报销额度失败");
			return r;
		}
		
		//买券订单与待提交的可报销订单一起提交报销结算
		log.info("生成结算订单传入参数》》》》"+JSON.toJSONString(dto),JSON.toJSONString(dtoList));
		boolean bflag = this.addAutoBillSettleAccounts(dtoList,dto);
		log.info("生成结算订单返回结果》》》》"+JSON.toJSONString(bflag));
		
		dto.setFlowName(IntelligentReimburseFlow.CREATE_SETTLE_ORDER.getCateName());
		dto.setFlowNo(IntelligentReimburseFlow.CREATE_SETTLE_ORDER.getCateCode());
		dto.setStatus(bflag ? "1" : "2");
		dto.setSort(IntelligentReimburseFlow.CREATE_SETTLE_ORDER.getCateMgn()+"");
		dto.setBusiReturn(JSON.toJSONString(bflag));
		log.info("智能报销流程记录生成结算订单传入参数》》》》"+JSON.toJSONString(dto));
		fvo = this.addCouponTaxCod(dto);
		log.info("智能报销流程记录生成结算订单返回结果》》》》"+JSON.toJSONString(fvo));
		
		if(!bflag) {
			r.setCode(R.FAIL);
			r.setData(false);
			r.setMsg("生成结算订单失败");
			return r;
		}
		
		return r;
	}

	private boolean addJbtRecord(DockR<JiaBaiTiaoReturnVo> jbtResult,BillSettleAccountsDto bdto) {
		JiaBaiTiaoReturnVo vo = jbtResult.getData();
		FundBuyJbtRecord record = new FundBuyJbtRecord();
		BeanUtils.copyProperties(vo, record);
		record.setExtRefOrderId(CodeUtils.genneratorShort("HKDH"));
		record.setBuyCode(bdto.getBillNo());
		record.setCreator(bdto.getMemberId());
		record.setUpdator(bdto.getMemberId());
		record.setIntelligentReimburseCode(bdto.getIntelligentReimburseCode());
		boolean result = fundBuyJbtRecordService.addJbtRecord(record);
		return result;
	}

	private FundIntelligentReimburseRecordVo addCouponTaxCod(BillSettleAccountsDto dto) {
		FundIntelligentReimburseRecordVo fundVo = new FundIntelligentReimburseRecordVo();
		fundVo.setFlowName(dto.getFlowName());
		fundVo.setFlowNo(dto.getFlowNo());
		fundVo.setStatus(dto.getStatus());
		fundVo.setSort(dto.getSort());
		if(StringUtils.isNotBlank(dto.getBusiCode())) {
			fundVo.setBusiCode(dto.getBusiCode());
		}
		if(StringUtils.isNotBlank(dto.getIntelligentReimburseCode())) {
			fundVo.setIntelligentReimburseCode(dto.getIntelligentReimburseCode());
		}
		if(StringUtils.isNotBlank(dto.getDetailId())) {
			fundVo.setDetailId(dto.getDetailId());
		}
		if(StringUtils.isNotBlank(dto.getMainId())) {
			fundVo.setId(dto.getMainId());
		}
		fundVo.setBusiReturn(dto.getBusiReturn());
		fundVo.setCompanyId(dto.getCompanyId());
		fundVo.setMemberId(dto.getMemberId());
		fundVo.setCouponId(dto.getCoupondId());
		fundVo.setReimburseAmount(dto.getReimburseAmount());
		fundVo.setOrderAmount(dto.getOrderAmount());
		fundVo.setBuyAmount(dto.getCouponAmount());
		fundVo.setBusiCode(dto.getBillNumber());
		fundVo.setBusiParams(JSON.toJSONString(dto));
		FundIntelligentReimburseRecordVo isture = fundIntelligentReimburseRecordService.addCouponTaxCod(fundVo);
		return isture;
	}

	private Result<Boolean> reLaunchReimburse(FundIntelligentReimburseRecordVo findCouponTaxCod,boolean isSplete) {
		FundIntelligentReimburseRecordVo fvo = new FundIntelligentReimburseRecordVo();
		// 解析josn字符串
		String orderCode = "";
		String couponName = "";
		String couponId = "";
		Result<Boolean> r = new Result<Boolean>();
		String js = findCouponTaxCod.getFlowbusiParams();
		JSONObject jsonObj = new JSONObject(js);
		BillSettleAccountsDto dto = new BillSettleAccountsDto();
		if(null == findCouponTaxCod.getBuyAmount()) {
			r.setCode(R.FAIL);
			r.setMsg("智能报销金额为空！");
			r.setData(false);
			return r;
		}
		if(null == jsonObj.get("companyId").toString()) {
			r.setCode(R.FAIL);
			r.setMsg("公司ID为空！");
			r.setData(false);
			return r;
		}
		if(null == jsonObj.get("coupondId").toString()) {
			r.setCode(R.FAIL);
			r.setMsg("券ID为空！");
			r.setData(false);
			return r;
		}
		if(null == jsonObj.get("memberId").toString()) {
			r.setCode(R.FAIL);
			r.setMsg("memberId为空！");
			r.setData(false);
			return r;
		}
		if(null != jsonObj.get("companyName")) {
			dto.setCompanyName(jsonObj.get("companyName").toString());
		}
		if(null != jsonObj.get("name")) {
			dto.setName(jsonObj.get("name").toString());
		}
		if(null != jsonObj.get("operationType")) {
			dto.setOperationType(jsonObj.get("operationType").toString());
		}
		if(null != jsonObj.get("sourceType")) {
			dto.setSourceType(jsonObj.get("sourceType").toString());
		}
		dto.setFlowNo(findCouponTaxCod.getFlowNo());
		String step = findCouponTaxCod.getFlowNo();
		if(null != jsonObj.get("busiCode")) {
			dto.setBusiCode(jsonObj.get("busiCode").toString());
			orderCode = dto.getBusiCode();
		}
		if("FREE_REIMBURSE_AMOUNT".equals(step) || "UPDATE_ORDER_STATUS".equals(step) || "JBT_DECREASE_AMOUNT".equals(step)) {
			orderCode = CodeUtils.genneratorShort("GM");
			dto.setOrderApplyNo(orderCode);
		}
		
		if("CREATE_BUY_ORDER".equals(step) && "2".equals(findCouponTaxCod.getStatus()) && !isSplete) {
			Map<String, Object> map = Maps.newHashMap();
			map.put("intelligent_reimburse_code", findCouponTaxCod.getIntelligentReimburseCode());
			map.put("status", "1");
			map.put("flow_no", "JBT_DECREASE_AMOUNT");
			List<FundIntelligentReimburseRecordDetail> selectByMap = fundIntelligentReimburseRecordDetailService.selectByMap(map);
			log.info("智能报销生成购买订单失败嘉白条额度扣减成功查询记录信息》》》》"+JSON.toJSONString(selectByMap));
			if(null != selectByMap && selectByMap.size() > 0) {
				String josn = selectByMap.get(0).getBusiReturn();
				JSONObject jsObj = new JSONObject(josn);
				if(null != jsObj.get("data").toString()) {
					JSONObject jsObjs = new JSONObject(jsObj.get("data").toString());
					if(null != jsObjs.get("extOrderId").toString()) {
						orderCode = jsObjs.get("extOrderId").toString();
						dto.setOrderApplyNo(orderCode);
					}
				}else {
					orderCode = CodeUtils.genneratorShort("GM");
					dto.setOrderApplyNo(orderCode);
				}
			}else {
				orderCode = CodeUtils.genneratorShort("GM");
				dto.setOrderApplyNo(orderCode);
			}
		}
		dto.setCompanyId(jsonObj.get("companyId").toString());
		dto.setCouponAmount(findCouponTaxCod.getBuyAmount());
		dto.setSettleAmountSum(dto.getCouponAmount());
		dto.setCoupondId(jsonObj.get("coupondId").toString());
		dto.setMemberId(jsonObj.get("memberId").toString());
		dto.setIntelligentReimburseCode(findCouponTaxCod.getIntelligentReimburseCode());
		fvo.setIntelligentReimburseCode(findCouponTaxCod.getIntelligentReimburseCode());
		fvo.setId(findCouponTaxCod.getId());
		fvo.setDetailId(findCouponTaxCod.getDetailId());
		log.info("开始智能报销》》》》"+JSON.toJSONString(dto));
		if(StringUtils.isBlank(findCouponTaxCod.getFlowNo())) {
			r.setCode(R.FAIL);
			r.setMsg("自动报销流程号为空！");
			r.setData(false);
			return r;
		}
		
		//1.冻结额度并将可报销订单待提交状态
		List<OrderListDto> dtoList=Lists.newArrayList();
		BigDecimal operationAmount =new BigDecimal(0);
		if(findCouponTaxCod.getReimburseAmount().compareTo(findCouponTaxCod.getOrderAmount())<0) {
			operationAmount=findCouponTaxCod.getReimburseAmount();
		}else {
			operationAmount = findCouponTaxCod.getBuyAmount().add(findCouponTaxCod.getOrderAmount());
		}
		ReimburseAmountSetDto reimburseAmountSetDto = new ReimburseAmountSetDto();
		reimburseAmountSetDto.setCompanyId(dto.getCompanyId());
		reimburseAmountSetDto.setMemberId(dto.getMemberId());
		reimburseAmountSetDto.setOperationAmount(operationAmount);
		if("FREE_REIMBURSE_AMOUNT".equals(step)) {
			reimburseAmountSetDto.setOperationType("0");
			reimburseAmountSetDto.setType("2");
			log.info("开始冻结额度传入参数》》》》"+JSON.toJSONString(dto));
			R<Boolean> roperation = refactorReimburseAmountSetService.operationMemberAmount(reimburseAmountSetDto);
			log.info("冻结额度返回结果》》》》"+JSON.toJSONString(roperation));
			
			dto.setFlowName(IntelligentReimburseFlow.FREE_REIMBURSE_AMOUNT.getCateName());
			dto.setFlowNo(IntelligentReimburseFlow.FREE_REIMBURSE_AMOUNT.getCateCode()+"");
			dto.setStatus(roperation.getData() ? "1" : "2");
			dto.setSort(IntelligentReimburseFlow.FREE_REIMBURSE_AMOUNT.getCateMgn()+"");
			dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
			dto.setMainId(fvo.getId());
			dto.setDetailId(fvo.getDetailId());
			dto.setBusiReturn(JSON.toJSONString(roperation));
			log.info("智能报销流程记录冻结额度传入参数》》》》"+JSON.toJSONString(dto));
			fvo = this.addCouponTaxCod(dto);
			log.info("智能报销流程记录冻结额度返回结果》》》》"+JSON.toJSONString(fvo));
			
			if(null == roperation || R.SUCCESS != roperation.getCode()) {
				r.setCode(R.FAIL);
				r.setMsg("扣减报销额度失败！");
				r.setData(false);
				return r;
			}
			
		}
		
		OrderListDto orderdto = new OrderListDto();
		orderdto.setMemberId(dto.getMemberId());
		orderdto.setCompanyId(dto.getCompanyId());
		orderdto.setSourceFrom("1");
		log.info("查询可报销订单传入参数》》》》"+JSON.toJSONString(orderdto));
		List<OrderListDto> dtoLists = orderService.queryOrderByUseBuy(orderdto);
		log.info("查询可报销订单返回结果》》》》"+JSON.toJSONString(dtoLists));
		if(CollUtil.isNotEmpty(dtoLists)) {
			dtoList.addAll(dtoLists);
		}
		if(("FREE_REIMBURSE_AMOUNT".equals(step) || "UPDATE_ORDER_STATUS".equals(step)) && !isSplete) {
			log.info("开始修改可报销订单为待提交状态传入参数》》》》"+JSON.toJSONString(dto));
			List<Order> orderList = Lists.newArrayList();
			if(!CollUtil.isEmpty(dtoList)) {
				for(OrderListDto ordto : dtoList) {
					Order ordervo = new Order();
					ordervo.setId(ordto.getId());
					ordervo.setStatus(OrderBusiStatus.SETTLED_PROCESS.getCateCode());
					orderList.add(ordervo);
				}
				boolean result = orderService.updateBatchById(orderList);
				
				dto.setFlowName(IntelligentReimburseFlow.UPDATE_ORDER_STATUS.getCateName());
				dto.setFlowNo(IntelligentReimburseFlow.UPDATE_ORDER_STATUS.getCateCode()+"");
				dto.setStatus(result ? "1" : "2");
				dto.setSort(IntelligentReimburseFlow.UPDATE_ORDER_STATUS.getCateMgn()+"");
				dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
				dto.setMainId(fvo.getId());
				dto.setDetailId(fvo.getDetailId());
				dto.setBusiReturn(JSON.toJSONString(result));
				log.info("智能报销流程记录修改可报销订单为待提交状态传入参数》》》》"+JSON.toJSONString(dto));
				fvo = this.addCouponTaxCod(dto);
				log.info("智能报销流程记录修改可报销订单为待提交状态返回结果》》》》"+JSON.toJSONString(fvo));
				
				log.info("修改可报销订单为待提交状态返回结果》》》》"+JSON.toJSONString(result));
				if(!result) {
					r.setCode(R.FAIL);
					r.setMsg("可报销订单待提交状态修改失败！");
					r.setData(false);
					return r;
				}
			}
		}
		
		// 2 嘉白条记录买券扣减金额
		if(("FREE_REIMBURSE_AMOUNT".equals(step) || "UPDATE_ORDER_STATUS".equals(step) || "JBT_DECREASE_AMOUNT".equals(step)) && !isSplete ) {
			if(dto.getCouponAmount().compareTo(new BigDecimal("0")) > 0) {
				log.info("嘉白条记录买券扣减金额--通知嘉白条开始》》》》"+JSON.toJSONString(dto));
				DockR<JiaBaiTiaoReturnVo> jbtResult = openJiaBaiTiaoBuyCouponService.jiaBaiTiaoBuyCoupon(dto.getCouponAmount(),orderCode,dto.getMemberId(),dto.getCompanyId());
				log.info("嘉白条记录买券扣减金额--通知嘉白条返回结果》》》》"+JSON.toJSONString(jbtResult));
				dto.setBillNo(orderCode);
				log.info("开始保存购买券嘉白条关联表记录信息》》》》"+JSON.toJSONString(jbtResult));
				boolean result = this.addJbtRecord(jbtResult,dto);
				log.info("记录购买券嘉白条关联表信息返回结果》》》》"+JSON.toJSONString(result));
				
				dto.setFlowName(IntelligentReimburseFlow.JBT_DECREASE_AMOUNT.getCateName());
				dto.setFlowNo(IntelligentReimburseFlow.JBT_DECREASE_AMOUNT.getCateCode()+"");
				dto.setStatus(jbtResult.getCode().equals(DockR.SUCCESS) ? "1" : "2");
				dto.setSort(IntelligentReimburseFlow.JBT_DECREASE_AMOUNT.getCateMgn()+"");
				dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
				dto.setMainId(fvo.getId());
				dto.setDetailId(fvo.getDetailId());
				dto.setBusiReturn(JSON.toJSONString(jbtResult));
				log.info("智能报销流程记录嘉白条记录买券扣减金额--通知嘉白条传入参数》》》》"+JSON.toJSONString(dto));
				fvo = this.addCouponTaxCod(dto);
				log.info("智能报销流程记录嘉白条记录买券扣减金额--通知嘉白条返回结果》》》》"+JSON.toJSONString(fvo));
				
				if(null == jbtResult || !(DockR.SUCCESS.equals(jbtResult.getCode()))) {
					r.setCode(R.FAIL);
					r.setData(false);
					r.setMsg("嘉白条记录买券扣减金额结果失败");
					return r;
				}
			}
		}
		
		// 3.生成买券订单
		if(("FREE_REIMBURSE_AMOUNT".equals(step) || "UPDATE_ORDER_STATUS".equals(step) || "CREATE_BUY_ORDER".equals(step) || "JBT_DECREASE_AMOUNT".equals(step)) && !isSplete) {
			if(dto.getCouponAmount().compareTo(new BigDecimal("0")) > 0) {
				FundBuyJbtRecord record = new FundBuyJbtRecord();
				record.setIntelligentReimburseCode(dto.getIntelligentReimburseCode());
				record.setBuyCode(orderCode);
				fundBuyJbtRecordService.checkIntelligentReimburseCode(record);
				log.info("开始买券生成购买订单传入参数》》》》"+JSON.toJSONString(dto));
				R<CouponBuyApplyDTO> resultCoupon = this.buyCoupon(dto);
				log.info("买券生成购买订单返回结果》》》》"+JSON.toJSONString(resultCoupon));
				
				dto.setFlowName(IntelligentReimburseFlow.CREATE_BUY_ORDER.getCateName());
				dto.setFlowNo(IntelligentReimburseFlow.CREATE_BUY_ORDER.getCateCode()+"");
				dto.setStatus(resultCoupon.getCode() == R.SUCCESS ? "1" : "2");
				dto.setSort(IntelligentReimburseFlow.CREATE_BUY_ORDER.getCateMgn()+"");
				dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
				dto.setMainId(fvo.getId());
				dto.setDetailId(fvo.getDetailId());
				dto.setBusiReturn(JSON.toJSONString(resultCoupon));
				if(null == resultCoupon || R.SUCCESS != resultCoupon.getCode()) {
					dto.setBusiCode(resultCoupon.getData().getApplyNo());
				}
				dto.setBusiReturn(JSON.toJSONString(dto));
				log.info("智能报销流程记录买券生成购买订单传入参数》》》》"+JSON.toJSONString(dto));
				fvo = this.addCouponTaxCod(dto);
				log.info("智能报销流程记录买券生成购买订单返回结果》》》》"+JSON.toJSONString(fvo));
				
				if(null == resultCoupon || R.SUCCESS != resultCoupon.getCode()) {
					r.setCode(R.FAIL);
					r.setMsg("买券生成购买订单失败！");
					r.setData(false);
					return r;
				}
				orderCode = resultCoupon.getData().getApplyNo();
				couponId = resultCoupon.getData().getCouponId();
				couponName = resultCoupon.getData().getCouponName();
			}
		}
		if(StringUtils.isNotBlank(orderCode)) {
			OrderDTO vo = new OrderDTO();
			vo.setOrderCode(orderCode);
			dto.setBusiCode(orderCode);
			OrderVo orderVo = orderService.getOrderInfo(vo);
			dto.setBillNo(orderVo.getOrderNo());
			OrderListDto orderListDto = new OrderListDto();
			BeanUtils.copyProperties(orderVo, orderListDto);
			orderListDto.setCouponId(couponId);
			orderListDto.setCouponName(couponName);
			dtoList.add(orderListDto);
		}
		
		// 通知总部购买券记录
		log.info("嘉白条记录买券扣减金额--通知总部开始》》》》"+JSON.toJSONString(dto));
		
		//3.消费券入账并冻结
		if(("FREE_REIMBURSE_AMOUNT".equals(step) || "UPDATE_ORDER_STATUS".equals(step) || "CREATE_BUY_ORDER".equals(step) || "JBT_DECREASE_AMOUNT".equals(step) || "FREE_COUPON_AMOUNT".equals(step)) && !isSplete) {
			if(dto.getCouponAmount().compareTo(new BigDecimal("0")) > 0) {
				log.info("消费券入账并冻结--发送MQ信息开始传入参数》》》》"+JSON.toJSONString(dto));
				this.sendBuyCouponMql(dto);
				log.info("消费券入账并冻结--发送MQ信息结束");
				dto.setFlowName(IntelligentReimburseFlow.FREE_COUPON_AMOUNT.getCateName());
				dto.setFlowNo(IntelligentReimburseFlow.FREE_COUPON_AMOUNT.getCateCode()+"");
				dto.setStatus("1");
				dto.setSort(IntelligentReimburseFlow.FREE_COUPON_AMOUNT.getCateMgn()+"");
				dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
				dto.setMainId(fvo.getId());
				dto.setDetailId(fvo.getDetailId());
				dto.setBusiReturn(JSON.toJSONString(dto));
				log.info("智能报销流程记录消费券额度冻结传入参数》》》》"+JSON.toJSONString(dto));
				fvo = this.addCouponTaxCod(dto);
				log.info("智能报销流程记录消费券额度冻结返回结果》》》》"+JSON.toJSONString(fvo));
			}
		}
		
		//解冻智能报销额度
		if(!"CREATE_SETTLE_ORDER".equals(step)) {
			log.info("开始解冻智能报销额度传入参数》》》》"+JSON.toJSONString(reimburseAmountSetDto));
			reimburseAmountSetDto.setOperationType("0");
			reimburseAmountSetDto.setType("0");
			R<Boolean> roperation2 = refactorReimburseAmountSetService.operationMemberAmount(reimburseAmountSetDto);
			log.info("解冻智能报销额度返回结果》》》》"+JSON.toJSONString(roperation2));
			
			dto.setFlowName(IntelligentReimburseFlow.UNFREE_REIMBURSE_AMOUNT.getCateName());
			dto.setFlowNo(IntelligentReimburseFlow.UNFREE_REIMBURSE_AMOUNT.getCateCode()+"");
			dto.setStatus(roperation2.getCode() == R.SUCCESS ? "1" : "2");
			dto.setSort(IntelligentReimburseFlow.UNFREE_REIMBURSE_AMOUNT.getCateMgn()+"");
			dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
			dto.setMainId(fvo.getId());
			dto.setDetailId(fvo.getDetailId());
			dto.setBusiReturn(JSON.toJSONString(roperation2));
			log.info("智能报销流程记录解冻智能报销额度传入参数》》》》"+JSON.toJSONString(dto));
			fvo = this.addCouponTaxCod(dto);
			log.info("智能报销流程记录解冻智能报销额度返回结果》》》》"+JSON.toJSONString(fvo));
			
			if(null == roperation2 || R.SUCCESS != roperation2.getCode()) {
				r.setCode(R.FAIL);
				r.setData(false);
				r.setMsg("解冻智能报销额度失败");
				return r;
			}
		}
				
		//4.买券订单与待提交的可报销订单一起提交报销结算
		log.info("生成结算订单传入参数》》》》"+JSON.toJSONString(dto),JSON.toJSONString(dtoList));
		boolean bflag = this.addAutoBillSettleAccounts(dtoList,dto);
		log.info("生成结算订单返回结果》》》》"+JSON.toJSONString(bflag));
		
		dto.setFlowName(IntelligentReimburseFlow.CREATE_SETTLE_ORDER.getCateName());
		dto.setFlowNo(IntelligentReimburseFlow.CREATE_SETTLE_ORDER.getCateCode());
		dto.setStatus(bflag ? "1" : "2");
		dto.setSort(IntelligentReimburseFlow.CREATE_SETTLE_ORDER.getCateMgn()+"");
		dto.setIntelligentReimburseCode(fvo.getIntelligentReimburseCode());
		dto.setMainId(fvo.getId());
		dto.setDetailId(fvo.getDetailId());
		dto.setBusiReturn(JSON.toJSONString(bflag));
		log.info("智能报销流程记录生成结算订单传入参数》》》》"+JSON.toJSONString(dto));
		fvo = this.addCouponTaxCod(dto);
		log.info("智能报销流程记录生成结算订单返回结果》》》》"+JSON.toJSONString(fvo));
		
		if(!bflag) {
			r.setCode(R.FAIL);
			r.setData(false);
			r.setMsg("生成结算订单失败");
			return r;
		}
		
		return r;
	}

	private R<CouponBuyApplyDTO> buyCoupon(BillSettleAccountsDto dto) {
		CouponBuyApplyDTO cto = new CouponBuyApplyDTO();
		BeanUtils.copyProperties(dto, cto);
		cto.setLetterheadId(dto.getCompanyId());
		cto.setLetterheadName(dto.getCompanyName());
		cto.setApplyor(dto.getMemberId());
		cto.setBuyAmount(dto.getCouponAmount());
		cto.setCouponId(dto.getCoupondId());
		log.info("开始买券生成购买订单传入参数》》》》"+JSON.toJSONString(cto));
		R<CouponBuyApplyDTO> resultCoupon = refactorCouponBuyApplyService.buyCoupon(cto);
		log.info("买券生成购买订单返回结果》》》》"+JSON.toJSONString(resultCoupon));
		return resultCoupon;
	}

	private boolean addAutoBillSettleAccounts(List<OrderListDto> dto,BillSettleAccountsDto bdto) {
		boolean flag = false;
		try {
			log.info("添加结算订单入参参数 request param dto:{},bdto{}",JSON.toJSONString(dto),JSON.toJSONString(bdto));
			ReimburseAmountSetDto amountSetDto = new ReimburseAmountSetDto();
    		amountSetDto.setMemberId(bdto.getMemberId());
    		amountSetDto.setCompanyId(bdto.getCompanyId());
			log.info("查询员工剩下可用报销额度，请求参数 request param amountSetDto:{}"+JSON.toJSON(amountSetDto));
    		R<ReimburseAmountSetVo> ravailableBalance = refactorReimburseAmountSetServiceApi.findReimburseAmount(amountSetDto);
    		if(null == ravailableBalance || R.SUCCESS != ravailableBalance.getCode()) {
    			flag = false;
    			return flag;
    		}
    		ReimburseAmountSetVo availableBalance = new ReimburseAmountSetVo();
    		BeanUtils.copyProperties(ravailableBalance.getData(), availableBalance);
    		log.info("查询员工剩下可用报销额度，返回结果 return result availableBalance:{}"+JSON.toJSON(availableBalance));
    		/**校验是否有可用报销额度***/
    		if(ObjectUtil.isNull(availableBalance) || availableBalance.getAvailableAmount() == null 
    				|| availableBalance.getAvailableAmount().doubleValue() <= 0
    				|| (availableBalance.getAvailableAmount().compareTo(bdto.getCouponAmount()) == -1)) {
    			log.error("校验员工结算前剩下可用额度不足，》》invoice返回》》剩下可用报销额度availableBalance:{},"
    					+ "结算总金额settleAmountSum:{}",availableBalance.getAvailableAmount(), bdto.getCouponAmount());
    			flag = false;
    			return flag;
    		}
    		bdto.setMemberName(availableBalance.getMemberName());
    		bdto.setMoblie(availableBalance.getPhone());
    		List<BillSettleAccountsDto> blist = Lists.newArrayList();
    		BillSettleAccountsDto billDto = new BillSettleAccountsDto();
    		List<String> billNoList = Lists.newArrayList();
    		BigDecimal settleAmountSum = new BigDecimal("0");
            String settleNo = IdWorker.getIdStr();
			for(OrderListDto vo : dto) {
                OrderDetailDTO detail = new OrderDetailDTO();
                detail.setOrderNo(vo.getOrderNo());
                List<OrderDetailVo> odlists = orderDetailService.selectOrderDetailByIds(detail);
				settleAmountSum = settleAmountSum.add(vo.getPayAmount());
			    BillSettleAccountsDto item = new BillSettleAccountsDto();
				item.setBillNo(vo.getOrderNo());
				item.setSettleNo(settleNo);
				item.setBillTime(vo.getCreateTime());
				item.setSettleAmount(vo.getPayAmount());
				item.setBillAmount(vo.getOrderAmount());
				item.setServiceVendor(vo.getBusinessId());
				item.setServiceVendorName(vo.getBusinessName());
				item.setStatus("0");
				if(null != vo.getSubType() && "1".equals(vo.getSubType())) {
					item.setSettleType("3");
					item.setBillService(vo.getCouponId());
					item.setBillServiceName(vo.getCouponName());
				}
				if(null != vo.getSubType() && "2".equals(vo.getSubType())) {
					item.setSettleType("2");
					item.setBillService(odlists.get(0).getPaymentIndustryId());
					item.setBillServiceName(odlists.get(0).getPaymentIndustryName());
					item.setPaymentType(vo.getAPayCode());
					item.setPaymentTypeName(vo.getAPayName());
				}
				billNoList.add(vo.getOrderNo());
				blist.add(item);
			}
			billDto.setBillNoList(billNoList);
			billDto.setBillItemList(blist);
			billDto.setName(bdto.getName());
			billDto.setCompanyId(bdto.getCompanyId());
			billDto.setCompanyName(bdto.getCompanyName());
			billDto.setMemberId(bdto.getMemberId());
			billDto.setSourceType(CommonConstant.STATUS_YES);
			billDto.setStatus("0");
			billDto.setSettleAmountSum(settleAmountSum);
			log.info("添加结算订单的入参参数:{}"+JSON.toJSONString(billDto));
			billDto.setReimburseType("1");//智能报销
			flag = this.addBillSettleAccounts(billDto);
			log.info("添加结算订单的返回结果:{}"+JSON.toJSONString(flag));
		}catch(Exception e) {
			log.error("生成结算订单失败，错误原因 e:{}",JSON.toJSON(e));
		}
		return flag;
	}

	private void sendBuyCouponMql(BillSettleAccountsDto dto) {
		List<AssignCouponDto> coupons = Lists.newArrayList();
		AssignCouponDto coudto = new AssignCouponDto();
		coudto.setCouponId(dto.getCoupondId());
		coudto.setAmount(dto.getCouponAmount());
		coupons.add(coudto);
		AssignDto mqDto = new AssignDto();
		mqDto.setSource("智能购买报销冻结消费券");
		mqDto.setBusiModel("*");
		mqDto.setFlowNo(CommonConstant.BUSI_FLOW_NO_PAY);
		mqDto.setOrderType(DictionaryEnum.OrderType.ORDER_TYPE_REIMBURSE.getCateCode());
		mqDto.setMemberId(dto.getMemberId());
		mqDto.setCompanyId(dto.getCompanyId());
		mqDto.setOrderAmount(dto.getCouponAmount());
		mqDto.setCouponAmount(dto.getCouponAmount());
		mqDto.setBusiOrderNo(dto.getBillNo());//券转让时，购买单据号
		mqDto.setCanTicket("1");//是否可开票 1-可开票；0-不可开票；
		//mqDto.setMemberCate("CP");//交易人类型
		mqDto.setCoupons(coupons);
		rabbitTemplate.convertAndSend(MqQueueConstant.FUND_EXCHANGE, MqQueueConstant.REIMBURSE_COUPON_TRANDE_TOPIC, mqDto);
	}
	
	@Override
	@Transactional(rollbackFor = {Exception.class }) 
	public BillSettleAccountsDto separateBill(BillSettleAccountsDto dto,String settleNo) throws Exception {
		List<BillSettleAccountsDto> rtnList=dto.getBillItemList();
		
		log.info("开始拆单的入参:{}",JSON.toJSON(rtnList)+","+settleNo);
		BigDecimal sumOrderAmount=dto.getSettleAmountSum();//整个结算金额
		BigDecimal settleAmountSum=new BigDecimal(0);//最终需要结算的金额
		List<String> billNoList=dto.getBillNoList();
		List<String> rtnBillNoList=Lists.newArrayList();
		BigDecimal reimburseAmount=new BigDecimal(0);//可报销额度
		ReimburseAmountSetDto setDto=new ReimburseAmountSetDto();
		setDto.setCompanyId(dto.getCompanyId());
		setDto.setMemberId(dto.getMemberId());
		R<ReimburseAmountSetVo> setR=refactorReimburseAmountSetService.findReimburseAmount(setDto);
		log.info("结算查询这个人所在企业的报销额度返回结果，return setR r:{}",JSON.toJSON(setR));
		if(null == setR || R.SUCCESS != setR.getCode()) {
			throw new Exception("查询员工报销额度失败");
		}
		reimburseAmount=setR.getData().getSurplusAmount().subtract(setR.getData().getFreezeAmount());
		if(reimburseAmount.compareTo(new BigDecimal(0))<=0 || sumOrderAmount.compareTo(new BigDecimal(0))<=0) {
			log.info("报销金额或者结算单总额小于等于0："+sumOrderAmount+","+reimburseAmount);
			throw new Exception("报销金额或者结算单总额小于等于0");
		}

		//当结算金额 比报销额度大，才开始拆单操作
		OrderDTO deOrderDto=new OrderDTO();
		deOrderDto.setOrderNoList(billNoList);
		if(!StringUtils.isEmpty(dto.getReimburseType())) {
			deOrderDto.setOperationType(dto.getReimburseType());
		}

		List<BillSettleAccountsDto> newRtnList=Lists.newArrayList();
		//按时间 正序查询订单数据
		List<OrderVo> orderList=orderService.showOrderByNoDesc(deOrderDto);
	
		log.info("根据订单orderNo查询的订单返回值:{}",JSON.toJSONString(orderList));
		if(CollectionUtil.isEmpty(orderList)) {
			throw new Exception("根据订单号没有查询到任何订单信息");
		}
		
		if(reimburseAmount.compareTo(sumOrderAmount) >=0 ) {
			log.info("报销金额大于订单总额不做拆单处理》》》开始保存订单信息到拆单表");
			for(OrderVo ordto : orderList) {
				OrderSeparateBill separate=new OrderSeparateBill();
				BeanUtil.copyProperties(ordto, separate);
				separate.setId("");
				separate.setOrderNo(CodeUtils.genneratorShort("QB"));
				separate.setOrderCode(ordto.getOrderNo());
				separate.setStatus(OrderBusiStatus.SETTLED.getCateCode());
				separate.setSourceCode(settleNo);
				separate.setPayAmount(ordto.getOrderAmount());
				separate.setCreateTime(new Date());
				separate.setUpdatedTime(new Date());
				log.info("拆单插入新表参数为{}",JSON.toJSONString(separate));
				orderSeparateBillService.insert(separate);
			}
			return dto;
		}

		Map<String,BigDecimal> orderMap=Maps.newHashMap();//拆分的订单跟拆分之后的金额
		Map<String,String> inNoMap=Maps.newHashMap();//要用的订单
	
		//当报销额度小于最小订单金额
		if(reimburseAmount.compareTo(orderList.get(0).getOrderAmount())<0) {
			log.info("报销额度小于最小订单金额，给最小订单额度拆单");
			OrderSeparateBill separate=new OrderSeparateBill();
			BeanUtil.copyProperties(orderList.get(0), separate);
			separate.setId("");
			separate.setOrderNo(CodeUtils.genneratorShort("CD"));
			separate.setOrderCode(orderList.get(0).getOrderNo());
			separate.setStatus(OrderBusiStatus.SETTLED.getCateCode());
			separate.setSourceCode(settleNo);
			separate.setOrderAmount(reimburseAmount);
			separate.setPayAmount(reimburseAmount);
			separate.setCreateTime(new Date());
			separate.setUpdatedTime(new Date());
			log.info("拆单插入新表参数为{}",JSON.toJSONString(separate));
			orderSeparateBillService.insert(separate);
			orderMap.put(orderList.get(0).getOrderNo(), reimburseAmount);
			for (BillSettleAccountsDto billSettleAccountsDto : rtnList) {
				//当这个单号对应的金额存在，开始设置为这个map的金额为结算金额
				if(!ObjectUtils.isEmpty(orderMap.get(billSettleAccountsDto.getBillNo()))) {
					billSettleAccountsDto.setSettleAmount(orderMap.get(billSettleAccountsDto.getBillNo()));
					settleAmountSum=orderMap.get(billSettleAccountsDto.getBillNo());
					newRtnList.add(billSettleAccountsDto);
				}
			}

			log.info("最终拆单结果单号list为:{}",JSON.toJSONString(rtnBillNoList));
			log.info("最终拆单结果结算单数据list为:{}",JSON.toJSONString(newRtnList));
			log.info("最终拆单结果结算总金额为:{}",settleAmountSum);
			dto.setBillItemList(newRtnList);
			dto.setBillNoList(rtnBillNoList);
			dto.setSettleAmountSum(settleAmountSum);
			return dto;
		}
		BigDecimal reimburseAmountSub=reimburseAmount;//需要拆解的报销额度
		for (OrderVo orderVo : orderList) {
			log.info("拆单for逻辑ordervo:{}",JSON.toJSONString(orderVo));
			//当差额大于等于订单金额，删除这个单号
			if(reimburseAmountSub.compareTo(orderVo.getOrderAmount()) >= 0 && reimburseAmountSub.compareTo(new BigDecimal(0)) > 0) {
				OrderSeparateBill separate=new OrderSeparateBill();
				BeanUtil.copyProperties(orderVo, separate);
				separate.setId("");
				separate.setOrderNo(CodeUtils.genneratorShort("QB"));
				separate.setOrderCode(orderVo.getOrderNo());
				separate.setStatus(OrderBusiStatus.SETTLED.getCateCode());
				separate.setSourceCode(settleNo);
				separate.setPayAmount(orderVo.getOrderAmount());
				separate.setCreateTime(new Date());
				separate.setUpdatedTime(new Date());
				log.info("拆单插入新表参数为{}",JSON.toJSONString(separate));
				orderSeparateBillService.insert(separate);
				inNoMap.put(orderVo.getOrderNo(), orderVo.getOrderNo());
				reimburseAmountSub=reimburseAmountSub.subtract(orderVo.getOrderAmount());
			}else if(reimburseAmountSub.compareTo(orderVo.getOrderAmount()) < 0 && reimburseAmountSub.compareTo(new BigDecimal(0)) > 0){
				//吧这个金额插入新的拆单表中，
				OrderSeparateBill separate=new OrderSeparateBill();
				BeanUtil.copyProperties(orderVo, separate);
				separate.setId("");
				separate.setOrderNo(CodeUtils.genneratorShort("CD"));
				separate.setOrderCode(orderVo.getOrderNo());
				separate.setStatus(OrderBusiStatus.SETTLED.getCateCode());
				separate.setSourceCode(settleNo);
				separate.setOrderAmount(reimburseAmountSub);
				separate.setPayAmount(reimburseAmountSub);
				separate.setCreateTime(new Date());
				separate.setUpdatedTime(new Date());
				log.info("拆单插入新表参数为{}",JSON.toJSONString(separate));
				orderSeparateBillService.insert(separate);
				orderMap.put(orderVo.getOrderNo(), reimburseAmountSub);
				reimburseAmountSub=new BigDecimal(0);

			}
		}
		log.info("拆单逻辑之后需要直接使用的inNoMap:{}",JSON.toJSONString(inNoMap));
		log.info("拆单逻辑之后要改变额度orderMap:{}",JSON.toJSONString(orderMap));
	
		for (BillSettleAccountsDto billSettleAccountsDto : rtnList) {
			log.info("拆单开始给需要扣减额度单子的billSettleAccountsDto:{}",JSON.toJSONString(billSettleAccountsDto));
			BigDecimal dAmount=new BigDecimal(0);
			//当使用的在这个item中，直接存放
			if(StringUtils.isNotEmpty(inNoMap.get(billSettleAccountsDto.getBillNo()))){
				newRtnList.add(billSettleAccountsDto);
				dAmount=billSettleAccountsDto.getSettleAmount();
				rtnBillNoList.add(billSettleAccountsDto.getBillNo());
			}
			//当这个单号对应的金额存在，开始设置为这个map的金额为结算金额
			if(!ObjectUtils.isEmpty(orderMap.get(billSettleAccountsDto.getBillNo()))) {
				billSettleAccountsDto.setSettleAmount(orderMap.get(billSettleAccountsDto.getBillNo()));
				dAmount=orderMap.get(billSettleAccountsDto.getBillNo());
				newRtnList.add(billSettleAccountsDto);
			}

			settleAmountSum=settleAmountSum.add(dAmount);
		}
		
		log.info("最终拆单结果单号list为:{}",JSON.toJSONString(rtnBillNoList));
		log.info("最终拆单结果结算单数据list为:{}",JSON.toJSONString(newRtnList));
		log.info("最终拆单结果结算总金额为:{}",settleAmountSum);
		dto.setBillItemList(newRtnList);
		dto.setBillNoList(rtnBillNoList);
		dto.setSettleAmountSum(settleAmountSum);
		return dto;
		
	}

	@Override
	@Transactional(rollbackFor = {Exception.class }) 
	public boolean revokeBillSettleAccount(BillSettleAccountsDto dto) throws Exception {
		log.info("撤销、拒绝前通过结算单号、操作类型（手动还是自动生成的结算单）查询结算单关联订单详细信息 ，请求参数 request param dto:{}",JSON.toJSON(dto));
		List<BillBaseNewVo> autoList = Lists.newArrayList();
		List<BillBaseNewVo> handList = Lists.newArrayList();
		/** operationType（区分是自动还是手动） 、 结算单修改状态（传入的状态）、恢复嘉白条额度（手动的不需要恢复额度）、增加报销额度、修改订单状态为初始化 **/
		if(dto.getOperationType().equals("1")) {
			// 智能报销
			log.info("》》智能报销》》撤销、拒绝前通过结算单号、操作类型是自动生成的结算单，查询结算单关联订单详细信息 ，请求参数 request param dto:{}",JSON.toJSON(dto));
			autoList = billSettleAccountsMapper.findSettleAccountsBySettleNo(dto);
			log.info("》》智能报销》》撤销、拒绝前通过结算单号、操作类型是自动生成的结算单，查询结算单关联订单详细信息 ，返回结果 return result autoList:{}",JSON.toJSON(autoList));
			dto.setOperationType("2");
			log.info("》》智能报销》》撤销、拒绝前通过结算单号、操作类型是自动的并且包含手动生成的结算单，查询结算单关联订单详细信息 ，请求参数 request param dto:{}",JSON.toJSON(dto));
			handList = billSettleAccountsMapper.findSettleAccountsBySettleNo(dto);
			log.info("》》智能报销》》撤销、拒绝前通过结算单号、操作类型是自动的并且包含手动生成的结算单，查询结算单关联订单详细信息 ，返回结果 return result handList:{}",JSON.toJSON(handList));
			List<String> orderNoHandList = Lists.newArrayList();
			if(CollUtil.isNotEmpty(handList)) {
				for (BillBaseNewVo billBaseNewVo : handList) {
					orderNoHandList.add(billBaseNewVo.getBillNo());
				}
			}
			List<String> orderNoList = Lists.newArrayList();
			if(CollUtil.isNotEmpty(autoList) ) {
				BillBaseNewVo billBaseNewVo = autoList.get(0);
				orderNoList.add(billBaseNewVo.getBillNo());
				log.info("》》智能报销订单-撤销》》嘉白条恢复额度请求参数 request params amount:{},order_id:{},repay_type:{},ext_ref_order_id:{},jf_repay_no:{},memberId:{},companyId:{}", billBaseNewVo.getBillAmount(), billBaseNewVo.getJbtId(), "02", billBaseNewVo.getExtRefOrderId(), null, billBaseNewVo.getMemberId(), billBaseNewVo.getCompanyId());
				DockR<JiaBaiTiaoReturnVo> r = openJiaBaiTiaoBuyCouponService.jiaBaiTiaoRepayment(billBaseNewVo.getBillAmount(), billBaseNewVo.getJbtId(), "02", billBaseNewVo.getExtRefOrderId(), null, billBaseNewVo.getMemberId(), billBaseNewVo.getCompanyId());
				log.info("》》智能报销订单-撤销》》嘉白条恢复额度返回值 r:{}",JSON.toJSONString(r));
				if(null == r || !DockR.SUCCESS.equals(r.getCode())) {
					throw new BaoxiaoException("给员工恢复嘉白条额度失败!");
				}
				PlatformVo platform = findMemberPlatInfo(billBaseNewVo.getMemberId(), billBaseNewVo.getCompanyId());
				log.info("查询企业用户信息返回结果，return result platform:{}",JSON.toJSON(platform));
				if(platform == null || StringUtils.isBlank(platform.getId())) {
					throw new BaoxiaoException("该员工信息不存在此企业!");
				}
				/**智能购券结算订单 恢复报销额度 **/
				ReimburseAmountSetDto amountSetDto = new ReimburseAmountSetDto();
				amountSetDto.setOperationType("2");
				amountSetDto.setType("0");
				amountSetDto.setOperationAmount(billBaseNewVo.getBillAmount());
				amountSetDto.setCompanyId(billBaseNewVo.getCompanyId());
				amountSetDto.setCompanyName(billBaseNewVo.getCompanyName());
				amountSetDto.setMemberId(billBaseNewVo.getMemberId());
				amountSetDto.setMemberName(platform.getRealName());
				amountSetDto.setPhone(platform.getMobile());
				log.info("恢复员工报销额度接口请求参数，request param amountSetDto:{}",JSON.toJSON(amountSetDto));
				reduceMemberAmount(amountSetDto); //恢复报销额度
				/**修改订单状态**/
				OrderDTO orderDTO = new OrderDTO();
				orderDTO.setOrderNoList(orderNoList);
				orderDTO.setStatus(OrderBusiStatus.REVOKE_BUY.getCateCode()); //撤销购买
				orderDTO.setSourceCode(dto.getSettleNo());
				editOrderStatus(orderDTO); // 自动购券订单修改状态为【撤销购买】
			}
			
			/**自动购买订单中包含手动购买的订单 也需要处理***/
			if(orderNoHandList != null && orderNoHandList.size() > 0 ) {
				PlatformVo platform = findMemberPlatInfo(handList.get(0).getMemberId(), handList.get(0).getCompanyId());
				log.info("查询企业用户信息返回结果，return result platform:{}",JSON.toJSON(platform));
				if(platform == null || StringUtils.isBlank(platform.getId())) {
					throw new BaoxiaoException("该员工信息不存在此企业!");
				}
				BigDecimal settleAmountSum = new BigDecimal(0);
				ReimburseAmountSetDto amountSetDto1 = new ReimburseAmountSetDto();
				for (BillBaseNewVo billBaseNewVo2 : handList) {
					settleAmountSum = settleAmountSum.add(billBaseNewVo2.getBiilSettleServiceAmount());
					amountSetDto1.setCompanyId(billBaseNewVo2.getCompanyId());
					amountSetDto1.setCompanyName(billBaseNewVo2.getCompanyName());
					amountSetDto1.setMemberId(billBaseNewVo2.getMemberId());
				}
				amountSetDto1.setOperationType("2");
				amountSetDto1.setType("0");
				amountSetDto1.setOperationAmount(settleAmountSum);
				amountSetDto1.setMemberName(platform.getRealName());
				amountSetDto1.setPhone(platform.getMobile());
				log.info("恢复员工报销额度接口请求参数，request param amountSetDto1:{}",JSON.toJSON(amountSetDto1));
				reduceMemberAmount(amountSetDto1); //恢复报销额度
				
				OrderDTO orderDTO2 = new OrderDTO();
				orderDTO2.setOrderNoList(orderNoHandList);
				orderDTO2.setStatus(OrderBusiStatus.TICKET_OPENING.getCateCode()); //订单初始化状态
				orderDTO2.setSourceCode(dto.getSettleNo());
				log.info("开始修改订单状态变成初始化，请求参数 request param orderDTO2:{}",JSON.toJSON(orderDTO2));
				editOrderStatus(orderDTO2);
			}
		}else if(dto.getOperationType().equals("2")) {
			// 手动报销 
			log.info("》》》手动报销 start》》》》撤销、拒绝前通过结算单号、操作类型是手动生成的结算单查询结算单关联订单详细信息 ，请求参数 request param dto:{}",JSON.toJSON(dto));
			handList = billSettleAccountsMapper.findSettleAccountsBySettleNo(dto);
			log.info("》》》手动报销 start》》》》撤销、拒绝前通过结算单号、操作类型是手动生成的结算单查询结算单关联订单详细信息 ，返回结果 return result handList:{}",JSON.toJSON(handList));
			if(CollUtil.isEmpty(handList)) {
				throw new BaoxiaoException("通过结算单号没关联到结算单信息!");
			}
			List<String> orderNoList = Lists.newArrayList();
			BigDecimal settleAmountSum = new BigDecimal(0);//累加结算总金额
			ReimburseAmountSetDto amountSetDto = new ReimburseAmountSetDto();
			for (BillBaseNewVo billBaseNewVo2 : handList) {
				orderNoList.add(billBaseNewVo2.getBillNo());
				settleAmountSum = settleAmountSum.add(billBaseNewVo2.getBiilSettleServiceAmount());
				amountSetDto.setCompanyId(billBaseNewVo2.getCompanyId());
				amountSetDto.setCompanyName(billBaseNewVo2.getCompanyName());
				amountSetDto.setMemberId(billBaseNewVo2.getMemberId());
			}
			PlatformVo platform = findMemberPlatInfo(amountSetDto.getMemberId(), amountSetDto.getCompanyId());
			log.info("");
			if(platform == null || StringUtils.isBlank(platform.getId())) {
				throw new BaoxiaoException("该员工信息不存在此企业!");
			}
			amountSetDto.setOperationType("2");
			amountSetDto.setType("0");
			amountSetDto.setOperationAmount(settleAmountSum);
			amountSetDto.setMemberName(platform.getRealName());
			amountSetDto.setPhone(platform.getMobile());
			log.info("恢复员工报销额度接口请求参数，request param amountSetDto:{}",JSON.toJSON(amountSetDto));
			reduceMemberAmount(amountSetDto); //恢复报销额度
			
			OrderDTO orderDTO2 = new OrderDTO();
			orderDTO2.setOrderNoList(orderNoList);
			orderDTO2.setStatus(OrderBusiStatus.TICKET_OPENING.getCateCode()); //订单初始化状态
			orderDTO2.setSourceCode(dto.getSettleNo());
			log.info("开始修改订单状态变成初始化，请求参数 request param orderDTO2:{}",JSON.toJSON(orderDTO2));
			editOrderStatus(orderDTO2);
		}
		
		/**修改结算单状态 （撤销/拒绝） **/
		BillSettleAccountsVo billSettleAccountsVo = new BillSettleAccountsVo();
		List<String> settleNoList = Lists.newArrayList();
		settleNoList.add(dto.getSettleNo());
		billSettleAccountsVo.setSettleNoList(settleNoList);
		billSettleAccountsVo.setStatus(dto.getStatus());
		log.info("开始修改结算单状态，请求参数 request param billSettleAccountsVo:{}",JSON.toJSON(billSettleAccountsVo));
		updateBatchBillSettleAccounts(billSettleAccountsVo);
		updateBatchSeparateBill(dto); // 修改拆单表信息
		return true;
	}


	
	
	@Override
	public boolean updateBatchBillSettleAccounts(BillSettleAccountsVo dto) throws Exception {
		Wrapper<BillSettleAccounts> wrapper = new EntityWrapper<>();
		wrapper.in("settle_no", dto.getSettleNoList());
		wrapper.eq("del_flag", CommonConstant.STATUS_NORMAL);
		List<BillSettleAccounts> selectList = this.selectList(wrapper);
		log.info("根据结算单号查询结算单信息返回结果，return result selectList:{}",JSON.toJSON(selectList));
		if(CollUtil.isEmpty(selectList)) {
			throw new BaoxiaoException("关联结算单信息不存在");
		}
		List<BillSettleAccounts> entityList = Lists.newArrayList();
		for (BillSettleAccounts billSettleAccounts : selectList) {
			for (String settleNo : dto.getSettleNoList()) {
				if(billSettleAccounts.getSettleNo().equals(settleNo)) {
					dto.setId(billSettleAccounts.getId());
					BillSettleAccounts entity = new BillSettleAccounts();
					BeanUtils.copyProperties(dto, entity);
					entityList.add(entity);
				}
			}
		}
		log.info("批量修改结算单信息》》开始》》请求参数，request param entityList:{}",JSON.toJSON(entityList));
		boolean isOK = this.updateBatchById(entityList);
		log.info("批量修改结算单信息》》结束》》返回结果，return result isOK:{}",JSON.toJSON(isOK));
		if(!isOK) {
			throw new BaoxiaoException("批量修改结算单信息失败");
		}
		return isOK;
	}
	
	/**
	 * 
	 * 
	 * @Title BillSettleAccountsServiceImpl.findMemberPlatInfo
	 * @Description: 通过公司id和员工标识查询企业员工信息
	 *
	 * @param memberId
	 * @param companyId
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年09月17日 下午5:44:48
	 * 修改内容 :
	 */
	public PlatformVo findMemberPlatInfo(String memberId,String companyId) {
		PlatformVo queryByMemberId = refactorMemberPlatformService.queryByMemberId(memberId, companyId);
		return queryByMemberId;
	}
	
	/**
	 * 
	 * 
	 * @Title BillSettleAccountsServiceImpl.updateBatchSeparateBill
	 * @Description: 通过订单号和结算单号去关联拆单表信息并批量修改拆单信息
	 *
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年10月11日 下午6:17:54
	 * 修改内容 :
	 */
	public void updateBatchSeparateBill(BillSettleAccountsDto dto) {
		Wrapper<BillSettleAccounts> wrapper = new EntityWrapper<>();
		wrapper.eq("settle_no", dto.getSettleNo());
		wrapper.eq("del_flag", "0");
		List<BillSettleAccounts> billSettleList = this.selectList(wrapper);
		List<String> orderNoList = Lists.newArrayList();
		for (BillSettleAccounts billSettleAccounts : billSettleList) {
			orderNoList.add(billSettleAccounts.getBillNo());
		}
		Wrapper<OrderSeparateBill> orderSeparate = new EntityWrapper<>();
		orderSeparate.in("order_code", orderNoList);	
		orderSeparate.eq("source_code",dto.getSettleNo());
		List<OrderSeparateBill> orderSeparateList = orderSeparateBillService.selectList(orderSeparate);
		if(CollUtil.isNotEmpty(orderSeparateList)) {
			List<OrderSeparateBill> entityList = Lists.newArrayList();
			for (OrderSeparateBill orderSeparateBill : orderSeparateList) {
				OrderSeparateBill entity = new OrderSeparateBill();
				entity.setId(orderSeparateBill.getId());
				entity.setDelFlag("1");
				entityList.add(entity);
			}
			orderSeparateBillService.updateBatchById(entityList);
		}
	}
}