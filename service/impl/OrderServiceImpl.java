package com.taolue.baoxiao.fund.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderBusiStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderDetailStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderTypeStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.SceneCodeStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TransType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.paymentType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.reimburseStatus;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.ListPageUtil;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.coupon.IRefactorCouponRelationService;
import com.taolue.baoxiao.fund.api.coupon.RefactorCouponService;
import com.taolue.baoxiao.fund.api.dto.OrderConsumeDetailDto;
import com.taolue.baoxiao.fund.api.dto.OrderCouponSceneDto;
import com.taolue.baoxiao.fund.api.dto.OrderDTO;
import com.taolue.baoxiao.fund.api.dto.OrderDetailDTO;
import com.taolue.baoxiao.fund.api.dto.OrderListDto;
import com.taolue.baoxiao.fund.api.dto.PayDetailDto;
import com.taolue.baoxiao.fund.api.dto.QueryOrderListDto;
import com.taolue.baoxiao.fund.api.dto.UseOrderDto;
import com.taolue.baoxiao.fund.api.invoice.IRefactorInvoiceReimburseService;
import com.taolue.baoxiao.fund.api.invoice.IRefactorReimburseAmountSetService;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberService;
import com.taolue.baoxiao.fund.api.openplatform.IDockOpenPlatformService;
import com.taolue.baoxiao.fund.api.vo.OrderConsumeDetailVo;
import com.taolue.baoxiao.fund.api.vo.OrderCouponSceneVo;
import com.taolue.baoxiao.fund.api.vo.OrderDetailVo;
import com.taolue.baoxiao.fund.api.vo.OrderListVo;
import com.taolue.baoxiao.fund.api.vo.OrderVo;
import com.taolue.baoxiao.fund.entity.FundCouponTaxCode;
import com.taolue.baoxiao.fund.entity.Order;
import com.taolue.baoxiao.fund.entity.OrderCouponScene;
import com.taolue.baoxiao.fund.entity.OrderDetail;
import com.taolue.baoxiao.fund.mapper.OrderConsumeDetailMapper;
import com.taolue.baoxiao.fund.mapper.OrderCouponSceneMapper;
import com.taolue.baoxiao.fund.mapper.OrderDetailMapper;
import com.taolue.baoxiao.fund.mapper.OrderMapper;
import com.taolue.baoxiao.fund.service.IFundCouponTaxCodeService;
import com.taolue.baoxiao.fund.service.IOrderBusiService;
import com.taolue.baoxiao.fund.service.IOrderConsumeDetailService;
import com.taolue.baoxiao.fund.service.IOrderCouponSceneService;
import com.taolue.baoxiao.fund.service.IOrderDetailService;
import com.taolue.baoxiao.fund.service.IOrderPaymentService;
import com.taolue.baoxiao.fund.service.IOrderService;
import com.taolue.coupon.api.vo.CouponVo;
import com.taolue.dock.api.dto.CouponTypeJfDto;
import com.taolue.invoice.api.dto.ReimburserQueryDTO;
import com.taolue.invoice.api.vo.ReimburseVO;
import com.taolue.member.api.dto.QueryMemberCerDto;
import com.taolue.member.api.vo.MemberCerVo;
import com.xiaoleilu.hutool.collection.CollUtil;
import com.xiaoleilu.hutool.util.ObjectUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 订单信息交易主表； 服务实现类
 * </p>
 *
 * @author duqiang
 * @since 2019-03-08
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

	@Autowired
	private IOrderDetailService orderDetailService;

	@Autowired
	private OrderMapper  orderMapper;
	
	@Autowired
	private OrderDetailMapper orderDetailMapper;
	
	@Autowired
	IOrderConsumeDetailService orderConsumeDetailService;

	@Autowired
	private IOrderCouponSceneService orderCouponSceneService;
	
	@Autowired
	IOrderBusiService orderBusiService;
	
	@Autowired
	IOrderPaymentService orderPaymentService;
	
	@Autowired
	IRefactorCouponRelationService refactorCouponRelationService;
	
	@Autowired
	IDockOpenPlatformService dockOpenPlatformService;

	@Autowired
	OrderConsumeDetailMapper orderConsumeDetailMapper;
	
	@Autowired
	OrderCouponSceneMapper orderCouponSceneMapper;
	
	@Autowired
	IFundCouponTaxCodeService fundCouponTaxCodeService;
	
	@Autowired
	IRefactorReimburseAmountSetService refactorReimburseAmountSetService;
	
	@Autowired
	IRefactorInvoiceReimburseService refactorInvoiceReimburseService;
	
	@Autowired
	IRefactorMemberService refactorMemberService;
	
	@Autowired
	RefactorCouponService refactorCouponService;
	
	@Override
	public boolean addOrder(OrderDTO dto) {
		Order order = new Order();
		BeanUtils.copyProperties(dto, order);
		order.setDiscountAmount(CommonConstant.NO_AMOUNT);
		if(null != dto.getFreightAmount()){
			order.setFreightAmount(dto.getFreightAmount());
		}else{
			order.setFreightAmount(CommonConstant.NO_AMOUNT);
		}
		order.setServiceAmount(CommonConstant.NO_AMOUNT);
		//order.setStatus(OrderBusiStatus.COMPLETED.getCateCode());
		order.setParentId("0");
		List<OrderDetailDTO> paymentDtoList=dto.getDetailDtoList();
		if(null!=paymentDtoList && paymentDtoList.size()>0) {
			for (OrderDetailDTO orderPaymentDto : paymentDtoList) {
				OrderDetail payment=new OrderDetail();
				BeanUtils.copyProperties(orderPaymentDto, payment);
				payment.setOrderNo(dto.getOrderNo());
				payment.setPaymentOrderNo(CodeUtils.genneratorShort("JY"));
				//payment.setStatus("1");
				orderDetailService.insert(payment);
			}
		}
		this.baseMapper.insert(order);
		return true;
	}

	/**
	 * 根据成员id，消费券id 获取订单列表
	 * @param orderDTO
	 * @return
	 */
	@Override
	public R<List<OrderVo>> queryOrderInfo(OrderDTO orderDTO) {
		R<List<OrderVo>> result = new R<List<OrderVo>>();
		List<OrderVo> orderVoList = orderMapper.queryOrderInfo(orderDTO);
		if(CollUtil.isNotEmpty(orderVoList)){
			result.setCode(R.SUCCESS);
			result.setData(orderVoList);
		}
		return result;
	}


	/**
	 * 根据成员id，订单id集合查询订单
	 * @param orderDTO
	 * @return
	 */
	@Override
	public R<List<OrderVo>> queryOrderList(OrderDTO orderDTO) {
		R<List<OrderVo>> result = new R<List<OrderVo>>();
		List<OrderVo> orderVoList = orderMapper.queryOrderList(orderDTO);
		if(CollUtil.isNotEmpty(orderVoList)){
			result.setCode(R.SUCCESS);
			result.setData(orderVoList);
		}
		return result;
	}

    @Override
    public R<List<OrderDetailVo>> queryOrderDetailList(OrderDTO orderDto) {
        R<List<OrderDetailVo>> result = new R<List<OrderDetailVo>>();
        List<OrderDetailVo> orderVoList = orderMapper.queryOrderDetailList(orderDto);


        if(CollUtil.isNotEmpty(orderVoList)){
            result.setCode(R.SUCCESS);
            result.setData(orderVoList);
        }
        return result;
    }
    @Override
    public R<List<OrderDetailVo>> queryOrderDetailListByH5(OrderDTO orderDto) {
        R<List<OrderDetailVo>> result = new R<List<OrderDetailVo>>();
        List<OrderDetailVo> orderVoList = orderMapper.queryOrderDetailListByH5(orderDto);
        if(CollUtil.isNotEmpty(orderVoList)){
            result.setCode(R.SUCCESS);
            result.setData(orderVoList);
        }
        return result;
    }

	@Override
	@Transactional
	public boolean uodateOrderStatus(OrderDTO orderDto) {
		String orderNo=orderDto.getOrderNo();
		Order order=new Order();
		order.setOrderNo(orderNo);
		Order seOrder=orderMapper.selectOne(order);
		if(!ObjectUtils.isEmpty(seOrder)) {
			order.setId(seOrder.getId());
			order.setStatus(orderDto.getStatus());
			orderMapper.updateById(order);
			OrderDetailDTO dtailDto=new OrderDetailDTO();
			dtailDto.setOrderNo(orderNo);
			List<OrderDetailVo> detailList=orderDetailMapper.selectOrderDetailByIds(dtailDto);
			log.info("uodateOrderStatus=====》》》selectOrderDetailByIds====》》》》"+ JSONObject.toJSONString(detailList));
			if(null!=detailList && detailList.size()>0) {
				for (OrderDetailVo orderDetailVo : detailList) {
					OrderDetail detail =new OrderDetail();
					detail.setId(orderDetailVo.getId());
					if(orderDto.getStatus().equals(OrderBusiStatus.PAYMENT_SUCCESS.getCateCode()) ||
							orderDto.getStatus().equals(OrderBusiStatus.COMPLETED.getCateCode())) {
						detail.setStatus(OrderDetailStatus.SUCCESS.getCateCode());
					}else if(orderDto.getStatus().equals(OrderBusiStatus.PAYMENT_FAIL.getCateCode())) {
						detail.setStatus(OrderDetailStatus.FAIL.getCateCode());
					}else if(orderDto.getStatus().equals(OrderBusiStatus.TICKET_OPENING_SUCCESS.getCateCode())){
						detail.setStatus(OrderDetailStatus.INVOICE.getCateCode());
					}else if(orderDto.getStatus().equals(OrderBusiStatus.TICKET_OPENING_FAIl.getCateCode())){
						detail.setStatus(OrderDetailStatus.PROCESS.getCateCode());
					}else{

					}
					log.info("uodateOrderStatus=====》》》detail====》》》》"+ JSONObject.toJSONString(detail));
					orderDetailMapper.updateById(detail);
				}
				
			}
		}else {
			log.info("该单号没查到数据");
		}
		
		return true;
	}





	@Override
	public Page<OrderVo> queryOrderPageList(Query query, OrderDTO orderDto) {
		query.setRecords(this.baseMapper.queryOrderPageList(query, orderDto));
		return query;
	}

	@Override
	public List<OrderDetailVo> getOrderDetailList(OrderDTO orderDto) {
		return this.baseMapper.getOrderDetailList(orderDto);
	}

	@Override
	public List<OrderVo> queryUnCompleteMember() {
		return this.baseMapper.queryUnCompleteMember();
	}
	
	 /**
     * 查询提现订单信息
     * @Title: getTxInfo   
     * @Description: TODO(这里用一句话描述这个方法的作用)   
     * @param: @param orderDTO
     * @param: @return    
     * @author: duqiang     
     * @return: OrderVo      
     * @throws
     */
	@Override
	public OrderVo getTxInfo(OrderDTO orderDTO) {
    	return this.baseMapper.getTxInfo(orderDTO);
    }

	@Override
	public R<OrderVo> getInvoiceOrder(OrderDTO orderDTO) {
		Map<String,Object> columnMap=Maps.newHashMap();
		columnMap.put("coupon_id", orderDTO.getCouponId());
		columnMap.put("del_flag", "0");
		OrderVo vo=null;
		List<OrderCouponScene> sceneList=orderCouponSceneService.selectByMap(columnMap);
		log.info("通过券查询对应的场景List:{}",JSON.toJSONString(sceneList));
		if(null!=sceneList && sceneList.size()>0) {
			List<String> scenes = sceneList.stream().map(OrderCouponScene::getSceneCode)
					.collect(Collectors.toList());
			orderDTO.setSubTypes(scenes);
		}
		vo=this.baseMapper.getInvoiceOrder(orderDTO);
		log.info("根据场景查询对应的消费订单的A类支付总额:{}",JSON.toJSONString(vo));
		return new R<>(vo);
	}
	
	
	@Override
	public List<OrderListDto> queryOrderListByH5(OrderListDto dto) {
		List<OrderListDto> result = orderMapper.queryOrderListByH5(dto);
		if(CollUtil.isNotEmpty(result)) {
			for (OrderListDto orderListDto : result) {
				if(StringUtils.isNotBlank(orderListDto.getOrderType())) {
					/**订单状态见枚举OrderTypeStatus**/
					if(orderListDto.getOrderType().equals(OrderTypeStatus.BUY.getCateCode())) {
						orderListDto.setOrderCode(OrderTypeStatus.BUY.getCateName());
					}else if(orderListDto.getOrderType().equals(OrderTypeStatus.CONSUME.getCateCode())) {
						orderListDto.setOrderCode(OrderTypeStatus.CONSUME.getCateName());
					}else if(orderListDto.getOrderType().equals(OrderTypeStatus.INVOICE.getCateCode())) {
						orderListDto.setOrderCode(OrderTypeStatus.INVOICE.getCateName());
					}else if(orderListDto.getOrderType().equals(OrderTypeStatus.REIMBURSE.getCateCode())) {
						orderListDto.setOrderCode(OrderTypeStatus.REIMBURSE.getCateName());
					}else if(orderListDto.getOrderType().equals(OrderTypeStatus.TRANSFER.getCateCode())) {
						orderListDto.setOrderCode(OrderTypeStatus.TRANSFER.getCateName());
					}else if(orderListDto.getOrderType().equals(OrderTypeStatus.RETURNMONEY.getCateCode())) {
						orderListDto.setOrderCode(OrderTypeStatus.RETURNMONEY.getCateName());
					}else if(orderListDto.getOrderType().equals(OrderTypeStatus.REVOKE_BUY.getCateCode())) {
						orderListDto.setOrderCode(OrderTypeStatus.REVOKE_BUY.getCateName());
					}
				}
			}
		}
		return result;
	}

	@Override
	public List<OrderListDto> queryOrderByUseBuy(OrderListDto dto) {
		/**我要结算查询购买、消费类型订单 1.先通过企业id查询对应的couponList，如果是空直接返回  2.couponList 当做条件去过滤购买类型订单 
		 *  3.消费类型：通过couponList查询场景code 4.场景codeList再当做条件去过滤消费类型的订单 5.两个查询结果合并到一起***/
		List<CouponTypeJfDto> couponList = Lists.newArrayList(); //券详细信息集合
		List<String> couponIdList = Lists.newArrayList(); // 券id集合
		List<String> sceneCodeList = Lists.newArrayList(); // 场景编码集合
		if(ObjectUtil.isNotNull(dto) && dto.getSourceFrom().equals(CommonConstant.STATUS_DEL) && StringUtils.isNotBlank(dto.getCompanyId())) {
			/**我要结算**/
			couponList = queryCouponType(dto.getCompanyId());
			
			log.info("通过公司id查询券信息返回结果，return result couponList:{}",JSON.toJSON(couponList));
			if(CollUtil.isEmpty(couponList)) {
				log.error("该企业id没有查询到可用的券信息，券信息为空");
				return null;
			}
			for (CouponTypeJfDto couponTypeJfDto : couponList) {
				if(StringUtils.isNotBlank(couponTypeJfDto.getCoupon_id())) {
					couponIdList.add(couponTypeJfDto.getCoupon_id());
				}
			}
		}else if(ObjectUtil.isNotNull(dto) && dto.getSourceFrom().equals(CommonConstant.BILL_ITEM_STATUS_PAYFULL) && StringUtils.isNotBlank(dto.getCouponId())){
			/***我要开票**/
			couponIdList.add(dto.getCouponId());
		}
		
		OrderCouponSceneDto orderCouponScene = new OrderCouponSceneDto();
		orderCouponScene.setCouponIdList(couponIdList);
		log.info("通过券id集合查询tb_order_coupon_scene表 场景Code集合请求参数，request param orderConsume:{}",JSON.toJSON(orderCouponScene));
		List<OrderCouponSceneVo> orderCouponSceneList = orderCouponSceneMapper.findOrderCouponSceneDetail(orderCouponScene);
		log.info("通过券id集合查询tb_order_coupon_scene表 场景Code集合返回结果，return result orderConsumeList:{}",JSON.toJSON(orderCouponSceneList));
		if(CollUtil.isEmpty(orderCouponSceneList)) {
			log.error("通过券id集合没有查询到可用的场景信息，场景信息为空");
			// return null;
		}else {
			/***场景集合**/
			for (OrderCouponSceneVo vo : orderCouponSceneList) {
				if(StringUtils.isNotBlank(vo.getSceneCode())) {
					sceneCodeList.add(vo.getSceneCode());
				}
			}
		}
		dto.setCouponIdList(couponIdList);
		dto.setPaymentIndustryIdList(sceneCodeList);
		log.info("查询购买/消费类型订单，请求参数 request param dto:{}",JSON.toJSON(dto));
		List<OrderListDto> result = orderMapper.queryOrderByUseBuy(dto);
		log.info("查询购买/消费类型订单，返回结果 return result list:{}",JSON.toJSON(result));
		/**订单号集合**/
		List<String> orderNoList = Lists.newArrayList();
		if(CollUtil.isEmpty(result)) {
			return null;
		}
		for (OrderListDto orderListDto : result) {
			/**订单类型枚举：OrderTypeStatus**/
			if(StringUtils.isNotBlank(orderListDto.getOrderType())) {
				if(orderListDto.getOrderType().equals(OrderTypeStatus.BUY.getCateCode())) {
					orderListDto.setOrderCode(OrderTypeStatus.BUY.getCateName());
				}else if(orderListDto.getOrderType().equals(OrderTypeStatus.CONSUME.getCateCode())) {
					orderListDto.setOrderCode(OrderTypeStatus.CONSUME.getCateName());
				}
			}
			PayDetailDto payDetail = new PayDetailDto();
			payDetail.setVoucherAmount(orderListDto.getVoucherAmount());
			payDetail.setAPayAmount(orderListDto.getAPayAmount());
			payDetail.setAPayName(orderListDto.getAPayName());
			payDetail.setAPayCode(orderListDto.getAPayCode());
			payDetail.setOrderAmount(orderListDto.getOrderAmount());
			payDetail.setCouponAmount(orderListDto.getCouponAmount());
			payDetail.setDiscountAmount(orderListDto.getDiscountAmount());
			payDetail.setFdAmount(orderListDto.getFdAmount());
			payDetail.setJfeAmount(orderListDto.getJfeAmount());
			payDetail.setPayAmount(orderListDto.getPayAmount());
			orderListDto.setPayDetail(payDetail);
			if(StringUtils.isNotBlank(orderListDto.getOrderNo())) {
				orderNoList.add(orderListDto.getOrderNo());
			}
		}
		/**查询消费订单对应的场景详细信息**/
		OrderConsumeDetailDto param = new OrderConsumeDetailDto();
		param.setPaymentOrderNoList(orderNoList);
		List<OrderConsumeDetailVo> returnList = orderConsumeDetailService.findOrderConsumeDetail(param);
		if(CollUtil.isEmpty(returnList)) {
			return result;
		}else {
			for (OrderConsumeDetailVo orderConsumeDetailVo : returnList) {
				for (OrderListDto orderListDto : result) {
					if (StringUtils.isNotBlank(orderConsumeDetailVo.getPaymentOrderNo())  
							&& StringUtils.isNotBlank(orderListDto.getOrderNo())
							&& orderConsumeDetailVo.getPaymentOrderNo().equals(orderListDto.getOrderNo())) {
						orderListDto.setOrderDetail(orderConsumeDetailVo);
					}
				}
			}
		}
		return result;
	}

	@Override
	@Transactional(rollbackFor=Exception.class)
	public boolean addUseOrder(UseOrderDto dto) throws Exception {
		/**通过消费场景code查询商户id**/
		Wrapper<OrderCouponScene> wrapper = new EntityWrapper<>();
		wrapper.eq("scene_code", dto.getService());
		wrapper.eq("del_flag", CommonConstant.STATUS_NORMAL);
		String businessId = null;
		String businessName = null;
		List<OrderCouponScene> returnList = orderCouponSceneService.selectList(wrapper);
		log.info("通过场景编码查询tb_order_coupon_scene信息 返回信息 returnList:{}",JSON.toJSON(returnList));
		if(CollUtil.isNotEmpty(returnList)) {
			OrderCouponScene orderCouponScene = returnList.get(0);
			if(StringUtils.isNotBlank(orderCouponScene.getCouponId())) {
				log.info("通过券id查询tb_fund_coupon_tax_code表对应的商户信息请求参数,request param couponId:{}", orderCouponScene.getCouponId());
				FundCouponTaxCode entFund = findVendorIdNameByCouponId(orderCouponScene.getCouponId());
				log.info("通过券id查询tb_fund_coupon_tax_code表对应的商户信息返回结果,return result r:{}",JSON.toJSON(entFund));
				if(ObjectUtil.isNull(entFund) || StringUtils.isEmpty(entFund.getVendorId()) || StringUtils.isEmpty(entFund.getVendorName())) {
					log.error("通过券id查询tb_fund_coupon_tax_code表对应的商户信息为空或者商户id、商户名称为空 return result entFund:{},"
							+ "vendorId:{},vendorName:{}",JSON.toJSON(entFund), entFund.getVendorId(), entFund.getVendorName());
					return false;
				}
				businessId = entFund.getVendorId();
				businessName = entFund.getVendorName();
			}
		}else {
			log.error("通过场景编码查询tb_order_coupon_scene信息，返回结果为空");
			return false;
		}
		if(StringUtils.isBlank(businessId) || StringUtils.isBlank(businessName)) {
			log.error("通过券id查询tb_fund_coupon_tax_code表商户信息  商户id/商户名称为空，直接返回");
			return false;
		}
		OrderDTO order = new OrderDTO();
		String orderNo = dto.getOrderNo();
	    order.setOrderCode(dto.getPaymentNo()); //外部订单号
	    order.setOrderNo(orderNo);
	    order.setStatus(OrderBusiStatus.COMPLETED.getCateCode());
	    order.setBusinessId(businessId); 
	    order.setBusinessName(businessName); 
	    order.setCompanyId(StringUtils.isEmpty(dto.getCompanyId()) ? AcctCateEnums.ACCT_CATE_PTQYX.getCateMgn() : dto.getCompanyId());
	    order.setCompanyName(StringUtils.isEmpty(dto.getCompanyName()) ? AcctCateEnums.ACCT_CATE_PTQYX.getCateName() : dto.getCompanyName()); 
	    order.setMainType(TransType.CONSUME.getSysCode());//消费类型订单
	    order.setMainTypeName(TransType.CONSUME.getSysName()); //消费类型订单
	    order.setSubType(TransType.CONSUME.getSysCode()); //消费类型订单
	    order.setSubTypeName(TransType.CONSUME.getSysName()); //消费类型订单
	    order.setOrderAmount(dto.getTotalAmount()); //订单总金额
	    order.setPayAmount(dto.getActualAmount()); // 订单支付金额
	    order.setCouponAmount(dto.getCouponAmount());//消费券支付金额
	    order.setVoucherAmount(dto.getVoucherAmount());// 抵用券支付金额
	    order.setMemberId(dto.getMemberId());
	    order.setAPayAmount(dto.getaPayAmount()); //A类支付金额
	    order.setAPayName(paymentType.existsAct(dto.getPaymentType())); // A类支付名称 见枚举paymentType
	    order.setAPayCode(dto.getPaymentType()); //A类支付code
	    order.setJfeAmount(dto.getJfEAmount()); //嘉福e卡支付金额
	    order.setFdAmount(dto.getFdAmount()); //福豆支付金额
	    order.setSourceType(CommonConstant.STATUS_DEL); //区分区分：0.智惠嘉  1.总部过来的订单
	    List<OrderDetailDTO> detailDtoList = Lists.newArrayList();
	    OrderDetailDTO detail = new OrderDetailDTO();
	    detail.setOrderNo(orderNo);
	    detail.setPaymentCate(BillItemSubCate.BILL_ITEM_SUBCATE_XFXJ.getCateCode());
	    detail.setBusiModle(BusiModelEnums.BUSI_MODEL_NONE.getCateCode());
	    detail.setPaymentMemberCate(MemberCateEnums.MEMBER_CATE_ALO.getCateCode()); //支付对象类型
	    detail.setPaymentAcctCate(AcctCateEnums.ACCT_CATE_MASTER.getCateCode()); //支付账户类型
	    detail.setPaymentItemNo(dto.getPaymentNo());
	    detail.setPaymentMemberId(dto.getMemberId());
	    detail.setStatus(OrderDetailStatus.SUCCESS.getCateCode()); //订单成功状态的
	    detail.setPaymentAmount(dto.getActualAmount());
	    detail.setPaymentVendorId(businessId);
	    detail.setPaymentVendorName(businessName); 
	    detail.setRemark(TransType.CONSUME.getSysName());
	    detail.setPaymentIndustryId(dto.getService()); // 消费场景编码，见枚举  SceneCodeStatus
	    detail.setPaymentIndustryName(SceneCodeStatus.existsAct(dto.getService())); // 消费场景编码，见枚举 SceneCodeStatus
	    detailDtoList.add(detail);
	    order.setDetailDtoList(detailDtoList);
    	log.info("添加消费订单的入参参数:{}",JSON.toJSONString(order));
		boolean isFlag = this.addOrder(order);
		log.info("添加消费订单的返回结果，return result isFlag:{}",JSON.toJSONString(isFlag));
		if(!isFlag) {
			throw new Exception("添加消费订单失败");
		}
	    return true;
	}

	/**   
	 * <p>Title: editOrderStatus</p>   
	 * <p>Description: 批量修改订单状态</p>   
	 * @param orderDto
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.IOrderService#editOrderStatus(com.taolue.baoxiao.fund.api.dto.OrderDTO)   
	 */  
	@Override
	@Transactional
	public boolean editOrderStatus(OrderDTO orderDto) {
		try {
			this.baseMapper.editOrderStatusByNos(orderDto);
			//OrderDetailDTO dto=new OrderDetailDTO();
			//dto.setDetailPaymentNos(orderDto.getOrderNoList());
			//dto.setStatus(OrderDetailStatus.SETTLED.getCateCode());
			//orderDetailMapper.editOrderDetailbyNos(dto);
		} catch (Exception e) {
			log.info("批量修改订单状态系统异常",e);
			throw new BaoxiaoException(e.getMessage());
		}
		
		return true;
	}
	/**
	 * 
	 * 
	 * @Title OrderServiceImpl.queryCouponType
	 * @Description: 调用dock服务查询券list
	 *
	 * @param companyId
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月19日 上午10:58:02
	 * 修改内容 :
	 */
	public List<CouponTypeJfDto> queryCouponType(String companyId) {
		String type="4";
		log.info("通过企业标识查询券集合信息，开始调用dock服务》》request param companyId:{},type:{}", companyId, type);
		R<List<CouponTypeJfDto>> r = dockOpenPlatformService.queryCouponType(companyId,type);
		log.info("通过企业标识查询券集合信息，调用dock服务结束》》return result r:{}",JSON.toJSON(r));
		if(r == null || R.SUCCESS != r.getCode()) {
			log.error("通过企业标识查询券集合信息，调用dock服务异常 e:{}",r.getMsg());
			if(r.getCode() == 6102) {
				return null;
			}
			throw new BaoxiaoException("通过企业标识查询券集合信息失败");
		}
		return r.getData();
	}
	
	

	@Override
	@Transactional(rollbackFor=Exception.class)
	public boolean addUseOrderNew(UseOrderDto dto) throws Exception {
	    String orderNo = CodeUtils.genneratorShort("XF");
	    dto.setOrderNo(orderNo);
		if(StringUtils.isNotBlank(dto.getRemark()) ) {
		    log.info("总部传入的json不为空，解析json保存到订单表和订单详细信息》》开始》》");
	    	log.info("解析总部传入的json串保存到场景详情表请求参数request params service:{},remark:{},orderNo:{},paymentTime:{}",dto.getService(), dto.getRemark(), orderNo, dto.getPaymentTime());
			boolean isOK = orderConsumeDetailService.addOrderConsumeDetailBySceneCode(dto.getService(), dto.getRemark(), orderNo, dto.getPaymentTime());
			log.info("解析总部传入的json串保存到场景详情表返回结果，return result isOK:{}", isOK);
			log.info("解析总部传入json串保存成功后，保存到订单表请求参数 request param dto:{}", JSON.toJSON(dto));
			boolean isTrue = this.addUseOrder(dto);
			log.info("解析总部传入json串保存成功后，保存到订单表返回结果 return result isTrue:{}", isTrue);
			if(!isTrue) {
				throw new Exception("添加消费类型订单异常");
			}
			return true;
	    }else {
	    	/**json传入为空直接保存订单信息***/
	    	log.info("总部传入的json串为空，直接保存订单信息》》开始》》请求参数，request param dto:{}", JSON.toJSON(dto));
	    	boolean isOK = this.addUseOrder(dto);
	    	log.info("总部传入的json串为空，直接保存订单信息》》结算》》返回结果，return result isOK:{}", isOK);
	    	if(!isOK) {
	    		throw new Exception("添加消费类型订单异常");
	    	}
	    	return true;
	    }
	}
	/**
	 * 
	 * 
	 * @Title OrderServiceImpl.findVendorIdNameByCouponId
	 * @Description: 通过券id查询商户信息
	 *
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月28日 下午4:04:17
	 * 修改内容 :
	 */
	public FundCouponTaxCode findVendorIdNameByCouponId(String couponId) {
		if(StringUtils.isNotBlank(couponId)) {
			Wrapper<FundCouponTaxCode> wrapper = new EntityWrapper<>();
			wrapper.eq("coupon_id", couponId);
			wrapper.eq("del_flag", CommonConstant.STATUS_NORMAL);
			List<FundCouponTaxCode> selectList = fundCouponTaxCodeService.selectList(wrapper);
			log.info("通过券id去tb_fund_coupon_tax_code 查询商户信息返回结果，return result list:{}",JSON.toJSON(selectList));
			if(CollUtil.isEmpty(selectList)) {
				log.info("通过券id去tb_fund_coupon_tax_code 查询商户信息返回结果，return result list is null");
				return null;
			}
			FundCouponTaxCode fundCouponTaxCode = selectList.get(0);
			return fundCouponTaxCode;
		}
		return null;
	}

	@Override
	public OrderVo getOrderInfo(OrderDTO orderDTO) {
		return this.baseMapper.getOrderInfo(orderDTO);
	}

	@Override
	public List<OrderVo> showOrderByNoDesc(OrderDTO dto) {
		
		return this.baseMapper.showOrderByNoDesc(dto);
	}


	@Override
	public R<List<OrderListDto>> queryOrderListByOrderNo(OrderDTO orderDto) {
		R<List<OrderListDto>> r = new R<List<OrderListDto>>();
		List<OrderListDto> result = baseMapper.queryOrderListByOrderNo(orderDto);
		if(null != result && result.size()>0) {
			r.setData(result);
			r.setCode(R.SUCCESS);
		}else {
			r.setCode(R.FAIL);
			r.setMsg("订单信息为空");
		}
		return r;
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public Page<OrderListVo> findOrderListPage(Query query, QueryOrderListDto dto) {
		List<OrderListVo> returnList = Lists.newArrayList();
 		List<String> paymentTypeList = dto.getPaymentTypeList();//传入的参数
		/**通过memberName或手机号去找memberId**/
		if(dto != null && (StringUtils.isNotBlank(dto.getMemberName()) || StringUtils.isNotBlank(dto.getMobile()) )) {
			QueryMemberCerDto memberDto = new QueryMemberCerDto();
			memberDto.setLoginName(dto.getMemberName());
			memberDto.setMobile(dto.getMobile());
			log.info("findOrderListPage 查询员工信息请求参数，request param memberDto:{}",JSON.toJSON(memberDto));
			List<MemberCerVo> memberList = queryMemberInfoByParam(memberDto);
			log.info("findOrderListPage 查询员工信息返回结果，return result memberList:{}",JSON.toJSON(memberList));
			if(CollUtil.isEmpty(memberList)) {
				dto.setFlag("1");
			}
			if(CollUtil.isNotEmpty(memberList)) {
				List<String> memberIdList = Lists.newArrayList();
				for (MemberCerVo memberCerVo : memberList) {
					if(StringUtils.isNotBlank(memberCerVo.getId())) {
						memberIdList.add(memberCerVo.getId());
					}
				}
				dto.setMemberIdList(memberIdList);
			}
		}
		/**购买、消费订单 和简易报销订单信息**/
		List<String> paymentType = Lists.newArrayList("underLine");
		List<String> orderType = Lists.newArrayList("3");
		/**不是线下支付、不是简易报销、不是简易报销状态才查询购买、消费订单**/
		if( (dto.getFlag().equals("0") && (CollUtil.isEmpty(dto.getOrderTypeList()) && CollUtil.isEmpty(paymentTypeList) && CollUtil.isEmpty(dto.getOrderStatusList()) )
				|| (CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("1") || dto.getOrderTypeList().contains("2")) && CollUtil.isEmpty(paymentTypeList) && CollUtil.isEmpty(dto.getOrderStatusList())) 
				|| (CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.size() == paymentType.size() && !(paymentTypeList.containsAll(paymentType)) && CollUtil.isEmpty(dto.getOrderTypeList()) && CollUtil.isEmpty(dto.getOrderStatusList())) 
				|| (CollUtil.isNotEmpty(dto.getOrderTypeList()) && dto.getOrderTypeList().size() == orderType.size() && !dto.getOrderTypeList().containsAll(orderType) && CollUtil.isEmpty(dto.getPaymentTypeList()) && CollUtil.isEmpty(dto.getOrderStatusList())) 
				|| (CollUtil.isNotEmpty(dto.getOrderStatusList()) && dto.getOrderStatusList().contains("0") && (CollUtil.isEmpty(dto.getPaymentTypeList()) || CollUtil.isEmpty(dto.getOrderStatusList())))  
				|| (CollUtil.isEmpty(paymentTypeList) && CollUtil.isNotEmpty(dto.getOrderStatusList()) && dto.getOrderStatusList().contains("0") && (CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("1") || dto.getOrderTypeList().contains("2"))) )
				|| ((CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("1") || dto.getOrderTypeList().contains("2"))) && (CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.size() == paymentType.size() && !(paymentTypeList.containsAll(paymentType)) &&(CollUtil.isEmpty(dto.getOrderStatusList())|| (CollUtil.isNotEmpty(dto.getOrderStatusList()) && dto.getOrderStatusList().contains("0")) ) ))
				|| (CollUtil.isNotEmpty(paymentTypeList) && !paymentTypeList.contains("underLine") && (CollUtil.isEmpty(dto.getOrderTypeList()) || (CollUtil.isNotEmpty(dto.getOrderTypeList()) && !dto.getOrderTypeList().contains("3"))) && (CollUtil.isEmpty(dto.getOrderStatusList()) || (CollUtil.isNotEmpty(dto.getOrderStatusList()) && dto.getOrderStatusList().contains("0"))))
				|| (CollUtil.isNotEmpty(paymentTypeList) && !(paymentTypeList.contains("underLine")) && CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("1") || dto.getOrderTypeList().contains("2")) && CollUtil.isEmpty(dto.getOrderStatusList()))
				|| (CollUtil.isNotEmpty(paymentTypeList) && ((paymentTypeList.contains("coupon") || paymentTypeList.contains("jbt") || paymentTypeList.contains("salary") || paymentTypeList.contains("wx") || paymentTypeList.contains("alipay"))) 
						&& CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("1") || dto.getOrderTypeList().contains("2")) && (CollUtil.isEmpty(dto.getOrderStatusList()) || (CollUtil.isNotEmpty(dto.getOrderStatusList()) && dto.getOrderStatusList().contains("0"))) )
				) ) {
			List<String> orderType1 = Lists.newArrayList("1");
			if(dto.getOrderTypeList() != null && dto.getOrderTypeList().size() == orderType1.size()  &&  dto.getOrderTypeList().containsAll(orderType1)) {
				dto.setOrderTypeList(orderType1);
			}
			List<String> orderType2 = Lists.newArrayList("2");
			if(dto.getOrderTypeList() != null && dto.getOrderTypeList().size() == orderType2.size()  &&  dto.getOrderTypeList().containsAll(orderType2)) {
				dto.setOrderTypeList(orderType2);
			}
			if(CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.contains("underLine")) {
				List<String> paymentType2 = Lists.newArrayList();
				paymentType2.addAll(paymentTypeList);
				paymentType2.remove("underLine");
				dto.setPaymentTypeList(paymentType2);
				dto.setPaymentTypeNewList(paymentType2);
			}else {
				dto.setPaymentTypeNewList(paymentTypeList);
			}
			if(CollUtil.isNotEmpty(dto.getPaymentTypeList())) {
				List<String> listNew = Lists.newArrayList("wx","coupon","salary");
				if(dto.getPaymentTypeList().containsAll(listNew)) {
					List<String> paymentTypeNew = Lists.newArrayList();
					paymentTypeNew.addAll(dto.getPaymentTypeList());
					paymentTypeNew.removeAll(listNew);
					dto.setPaymentTypeNewList(paymentTypeNew);
				}
			}
			if(dto.getPaymentTypeList() != null && (dto.getPaymentTypeList().contains("wx") ||
					dto.getPaymentTypeList().contains("coupon") || 
					dto.getPaymentTypeList().contains("salary"))) {
				dto.setIsAPayCode("1");
			}
			log.info("查询购买和消费类型订单信息，findOrderListPage 请求参数 request param dto:{}",JSON.toJSON(dto));
			List<OrderListVo> orderQueryList = orderMapper.findOrderListPage(dto);
			log.info("查询购买和消费类型订单信息，findOrderListPage 返回结果 return result orderQueryList:{}",JSON.toJSON(orderQueryList));
			if(CollUtil.isNotEmpty(orderQueryList)) {
				List<String> memberIds = Lists.newArrayList();
				for (OrderListVo orderListVo : orderQueryList) {
					StringBuffer sbf = new StringBuffer();
					if(orderListVo.getOrderType() != null && orderListVo.getOrderType().equals("2")) {
						if(orderListVo.getAPayCode().equals("salary") || orderListVo.getAPayCode().equals("s_cash")) {
							sbf.append(","+"工资额度");
						}else {
							sbf.append(","+DictionaryEnum.paymentType.existsAct(orderListVo.getAPayCode()));
						}
					}else if(orderListVo.getOrderType() != null && orderListVo.getOrderType().equals("1")) {
						orderListVo.setPaymentTypeName("消费券支付");
					}
					if(orderListVo.getCouponAmount()!=null && orderListVo.getCouponAmount().doubleValue() > 0) {
						sbf.append(","+"消费券支付");
					}
					if (sbf.toString().startsWith(",")) {
						orderListVo.setPaymentTypeName(sbf.toString().substring(1));
			        }
					orderListVo.setStatus("0");
					if(!memberIds.contains(orderListVo.getMemberId())) {
						memberIds.add(orderListVo.getMemberId());
					}
				}
				QueryMemberCerDto cerDto = new QueryMemberCerDto();
				cerDto.setMemberIdList(memberIds);
				log.info("批量查询员工信息请求参数，request param cerDto:{}",JSON.toJSON(cerDto));
				List<MemberCerVo> memberList = queryMemberInfoByParam(cerDto);
				List<MemberCerVo> memberNewList = Lists.newArrayList();
				log.info("批量查询员工信息返回结果，return result memberList:{}",JSON.toJSON(memberList));
				memberNewList = memberList !=null ? memberList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(MemberCerVo::getId))), ArrayList::new)):null;
				log.info("批量查询员工信息后再去通过memberId去重，return result memberNewList:{}",JSON.toJSON(memberNewList));
				Map<String, MemberCerVo> memberMap = memberNewList != null ? memberNewList.stream().collect(Collectors.toMap(MemberCerVo::getId, o -> o)):null;
				/**拼接员工姓名和手机号信息**/
				for (OrderListVo orderListVo : orderQueryList) {
					if(ObjectUtil.isNotNull(memberMap.get(orderListVo.getMemberId()))) {
						MemberCerVo memberCerVo = memberMap.get(orderListVo.getMemberId());
						orderListVo.setMemberName(memberCerVo.getRealName() == null ? memberCerVo.getNickName(): memberCerVo.getRealName());
						orderListVo.setMobile(memberCerVo.getMobile());
					}
				}
			}
			returnList.addAll(orderQueryList);
		}
		 /**orderTypeList订单类型集合：1：购买；2：消费；3：简易报销
	     	paymentTypeList付款方式集合：coupon:消费券支付；jbt:嘉白条；salary：工资额度；wx：微信支付(wx_wft/wx_wft_all)；alipay:支付宝;underLine:线下支付
	     	orderStatusList订单状态集合: 0:已付款（购买、消费订单）；报销单状态：1:待审批；3:审批通过 ；4：审批拒绝；5：撤销申请
	     */
		/**满足条件去查简易报销订单信息**/
		if(CollUtil.isEmpty(dto.getOrderTypeList()) && CollUtil.isEmpty(paymentTypeList) && CollUtil.isEmpty(dto.getOrderStatusList())
				|| (CollUtil.isEmpty(dto.getOrderTypeList()) && (CollUtil.isEmpty(paymentTypeList) || (CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.contains("underLine"))) &&  CollUtil.isNotEmpty(dto.getOrderStatusList()) && (!dto.getOrderStatusList().contains("0") && !dto.getOrderStatusList().contains("6") && !dto.getOrderStatusList().contains("7") ) )
				|| (CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("3") && CollUtil.isEmpty(paymentTypeList) )) 
				|| (CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("3") && CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.contains("underLine")) )
				|| (CollUtil.isEmpty(dto.getOrderTypeList()) && CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.contains("underLine")) 
				|| (CollUtil.isEmpty(dto.getOrderTypeList()) && CollUtil.isEmpty(dto.getPaymentTypeList()) && (!dto.getOrderStatusList().contains("0") || !dto.getOrderStatusList().contains("6") || !dto.getOrderStatusList().contains("7")))) {
			ReimburserQueryDTO reimburserQueryDTO = new ReimburserQueryDTO();
			if(CollUtil.isEmpty(dto.getOrderStatusList())) {
				String[] status = new String[] {"1","3","4","5"};
				reimburserQueryDTO.setStatus(status);
			}
			if(dto != null && StringUtils.isNotBlank(dto.getOrderNo())) {
				reimburserQueryDTO.setReimburseCode(dto.getOrderNo());
			}
			if(dto != null && StringUtils.isNotBlank(dto.getMemberName())) {
				reimburserQueryDTO.setRealName(dto.getMemberName());
			}
			if(dto != null && StringUtils.isNotBlank(dto.getMobile())) {
				reimburserQueryDTO.setPhone(dto.getMobile());
			}
			if(dto != null && dto.getStartDate() != null ) {
				reimburserQueryDTO.setStartDate(dto.getStartDate());
			}
			if(dto != null && dto.getEndDate() != null) {
				reimburserQueryDTO.setEndDate(dto.getEndDate());
			}
			if(dto != null && CollUtil.isNotEmpty(dto.getOrderStatusList())) {
				reimburserQueryDTO.setStatus(dto.getOrderStatusList().toArray(new String[dto.getOrderStatusList().size()]));
			}
			log.info("查询简易报销单信息，findOrderListPage 请求参数 request param reimburserQueryDTO:{}",JSON.toJSON(reimburserQueryDTO));
			List<ReimburseVO> reimburseList = refactorInvoiceReimburseService.reimburseList(reimburserQueryDTO);
			log.info("查询简易报销单信息，findOrderListPage 返回结果 return result reimburseList:{}",JSON.toJSON(reimburseList));
			if(CollUtil.isNotEmpty(reimburseList)) {
				List<OrderListVo> reimburseReturn = Lists.newArrayList();
				for (ReimburseVO reimburseVO : reimburseList) {
					OrderListVo orderListVO = new OrderListVo();
					orderListVO.setOrderNo(reimburseVO.getReimburseCode());
					orderListVO.setMemberId(reimburseVO.getMemberId());
					orderListVO.setMemberName(reimburseVO.getMemberName());
					orderListVO.setMobile(reimburseVO.getPhone());
					orderListVO.setOrderAmount(reimburseVO.getTotalAmt());
					orderListVO.setOrderType("3");
					orderListVO.setOrderTypeName("贴票报销");
					orderListVO.setPaymentTypeName(DictionaryEnum.paymentType.acct_type_under_line.getName());
					orderListVO.setCreateTime(reimburseVO.getReimbDate());
					orderListVO.setReimbId(reimburseVO.getId());
					orderListVO.setStatus(reimburseVO.getStatus());
					if(reimburseVO.getStatus().equals(reimburseStatus.reimburseStatus_3.getCode())) {
						orderListVO.setStatusName("审批通过");
					}else if(reimburseVO.getStatus().equals(reimburseStatus.reimburseStatus_1.getCode())) {
						orderListVO.setStatusName("待审批");
					}else if(reimburseVO.getStatus().equals(reimburseStatus.reimburseStatus_4.getCode())) {
						orderListVO.setStatusName("审批拒绝");
					}else if(reimburseVO.getStatus().equals(reimburseStatus.reimburseStatus_5.getCode())) {
						orderListVO.setStatusName("撤销申请");
					}
					reimburseReturn.add(orderListVO);
				}
				returnList.addAll(reimburseReturn);
			}
		}
		List<OrderListVo> sortList = returnList != null ? returnList.stream().sorted(Comparator.comparing(OrderListVo::getCreateTime).reversed()).collect(Collectors.toList()) : null;
		query.setTotal(sortList.size());
		log.info("未分页已排序的数据，返回结果 return result sortList:{}",JSON.toJSON(sortList));
		log.info("分页查询前传入总条数list.size:{},pageNum:{},pageSize:{}",sortList.size(),query.getCurrent(),query.getCondition().get("size") == null ? query.getSize() : Integer.parseInt(query.getCondition().get("size").toString()) );
		List<OrderListVo> resultPage = ListPageUtil.startPage(sortList, query.getCurrent(),query.getCondition().get("size") == null ? query.getSize() : Integer.parseInt(query.getCondition().get("size").toString()) );
		query.setRecords(resultPage);
		return query;
	}
	
	/***
	 * 
	 * 
	 * @Title OrderServiceImpl.queryMemberInfoByParam
	 * @Description: 批量查询用户信息
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年09月24日 下午6:52:47
	 * 修改内容 :
	 */
	public List<MemberCerVo> queryMemberInfoByParam(QueryMemberCerDto dto){
		return refactorMemberService.queryMemberInfoByParam(dto);
	}

	@Override
	public OrderListVo findOrderDetailByOrderNo(String orderNo) {
		log.info("查询订单详情信息，findOrderDetailByOrderNo 请求参数 request param orderNo:{}",JSON.toJSON(orderNo));
		OrderListVo orderDetail = orderMapper.findOrderDetailByOrderNo(orderNo);
		log.info("查询订单详情信息，findOrderDetailByOrderNo 返回结果 return result orderDetail:{}",JSON.toJSON(orderDetail));
		if(ObjectUtil.isNotNull(orderDetail) && CollUtil.isNotEmpty(orderDetail.getOrderDetail())) {
			for (OrderConsumeDetailVo orderConsumeDetailVo : orderDetail.getOrderDetail()) {
				orderConsumeDetailVo.setSceneCodeName(DictionaryEnum.SceneCodeStatus.existsAct(orderConsumeDetailVo.getSceneCode()));
			}
		}
		if(ObjectUtil.isNotNull(orderDetail) && CollUtil.isNotEmpty(orderDetail.getCouponPayDetail())) {
			List<String> idList = Lists.newArrayList();
			for (OrderListVo orderListVo : orderDetail.getCouponPayDetail()) {
				if(orderListVo != null && StringUtils.isNotBlank(orderListVo.getCouponId())) {
					idList.add(orderListVo.getCouponId());
				}
			}
			/**匹配券信息**/
			log.info("批量通过券id查询券信息，请求参数 request param idList:{}",JSON.toJSON(idList));
			List<CouponVo> couponInfoList = refactorCouponService.findCouponVoByIdList(idList);
			log.info("批量通过券id查询券信息，返回结果 return result couponInfoList:{}",JSON.toJSON(couponInfoList));
			if(CollUtil.isNotEmpty(couponInfoList)) {
				Map<String, CouponVo> couponMap = couponInfoList.stream().collect(Collectors.toMap(CouponVo::getId, o -> o));
				log.info("couponList通过券id转map，返回结果 return result couponMap:{}",JSON.toJSON(couponMap));
				for (OrderListVo orderListVo : orderDetail.getCouponPayDetail()) {
					if(ObjectUtil.isNotNull(couponMap.get(orderListVo.getCouponId()))) {
						orderListVo.setCouponName(couponMap.get(orderListVo.getCouponId()).getName());
						orderListVo.setCouponType(couponMap.get(orderListVo.getCouponId()).getDeliveryPlatform());
					}
				}
			}
		}
		return orderDetail;
	}

	@Override
	public List<OrderListVo> findOrderListByExport(QueryOrderListDto dto) {
		List<String> paymentTypeList = dto.getPaymentTypeList();//传入的参数
		List<OrderListVo> returnList = Lists.newArrayList();
		/**通过memberName或手机号去找memberId**/
		if(dto != null && (StringUtils.isNotBlank(dto.getMemberName()) || StringUtils.isNotBlank(dto.getMobile()) )) {
			QueryMemberCerDto memberDto = new QueryMemberCerDto();
			memberDto.setLoginName(dto.getMemberName());
			memberDto.setMobile(dto.getMobile());
			log.info("findOrderListByExport 查询员工信息请求参数，request param memberDto:{}",JSON.toJSON(memberDto));
			List<MemberCerVo> memberList = queryMemberInfoByParam(memberDto);
			log.info("findOrderListByExport 查询员工信息返回结果，return result memberList:{}",JSON.toJSON(memberList));
			if(CollUtil.isEmpty(memberList)) {
				dto.setFlag("1");
			}
			if(CollUtil.isNotEmpty(memberList)) {
				List<String> memberIdList = Lists.newArrayList();
				for (MemberCerVo memberCerVo : memberList) {
					if(StringUtils.isNotBlank(memberCerVo.getId())) {
						memberIdList.add(memberCerVo.getId());
					}
				}
				dto.setMemberIdList(memberIdList);
			}
		}
		/**购买、消费订单 和简易报销订单信息**/
		List<String> paymentType = Lists.newArrayList("underLine");
		List<String> orderType = Lists.newArrayList("3");
		/**不是线下支付、不是简易报销、不是简易报销状态才查询购买、消费订单**/
		if((dto.getFlag().equals("0") && (CollUtil.isEmpty(dto.getOrderTypeList()) && CollUtil.isEmpty(paymentTypeList) && CollUtil.isEmpty(dto.getOrderStatusList()) )
				|| (CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("1") || dto.getOrderTypeList().contains("2")) && CollUtil.isEmpty(paymentTypeList) && CollUtil.isEmpty(dto.getOrderStatusList())) 
				|| (CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.size() == paymentType.size() && !(paymentTypeList.containsAll(paymentType)) && CollUtil.isEmpty(dto.getOrderTypeList()) && CollUtil.isEmpty(dto.getOrderStatusList())) 
				|| (CollUtil.isNotEmpty(dto.getOrderTypeList()) && dto.getOrderTypeList().size() == orderType.size() && !dto.getOrderTypeList().containsAll(orderType) && CollUtil.isEmpty(dto.getPaymentTypeList()) && CollUtil.isEmpty(dto.getOrderStatusList())) 
				|| (CollUtil.isNotEmpty(dto.getOrderStatusList()) && dto.getOrderStatusList().contains("0") && (CollUtil.isEmpty(dto.getPaymentTypeList()) || CollUtil.isEmpty(dto.getOrderStatusList())))  
				|| (CollUtil.isEmpty(paymentTypeList) && CollUtil.isNotEmpty(dto.getOrderStatusList()) && dto.getOrderStatusList().contains("0") && (CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("1") || dto.getOrderTypeList().contains("2"))) )
				|| ((CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("1") || dto.getOrderTypeList().contains("2"))) && (CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.size() == paymentType.size() && !(paymentTypeList.containsAll(paymentType)) &&(CollUtil.isEmpty(dto.getOrderStatusList())|| (CollUtil.isNotEmpty(dto.getOrderStatusList()) && dto.getOrderStatusList().contains("0")) ) ))
				|| (CollUtil.isNotEmpty(paymentTypeList) && !paymentTypeList.contains("underLine") && (CollUtil.isEmpty(dto.getOrderTypeList()) || (CollUtil.isNotEmpty(dto.getOrderTypeList()) && !dto.getOrderTypeList().contains("3"))) && (CollUtil.isEmpty(dto.getOrderStatusList()) || (CollUtil.isNotEmpty(dto.getOrderStatusList()) && dto.getOrderStatusList().contains("0"))))
				|| (CollUtil.isNotEmpty(paymentTypeList) && !(paymentTypeList.contains("underLine")) && CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("1") || dto.getOrderTypeList().contains("2")) && CollUtil.isEmpty(dto.getOrderStatusList()))
				|| (CollUtil.isNotEmpty(paymentTypeList) && ((paymentTypeList.contains("coupon") || paymentTypeList.contains("jbt") || paymentTypeList.contains("salary") || paymentTypeList.contains("wx") || paymentTypeList.contains("alipay"))) 
						&& CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("1") || dto.getOrderTypeList().contains("2")) && (CollUtil.isEmpty(dto.getOrderStatusList()) || (CollUtil.isNotEmpty(dto.getOrderStatusList()) && dto.getOrderStatusList().contains("0"))) )
				) ) {
			List<String> orderType1 = Lists.newArrayList("1");
			if(dto.getOrderTypeList() != null && dto.getOrderTypeList().size() == orderType1.size()  &&  dto.getOrderTypeList().containsAll(orderType1)) {
				dto.setOrderTypeList(orderType1);
			}
			List<String> orderType2 = Lists.newArrayList("2");
			if(dto.getOrderTypeList() != null && dto.getOrderTypeList().size() == orderType2.size()  &&  dto.getOrderTypeList().containsAll(orderType2)) {
				dto.setOrderTypeList(orderType2);
			}
			if(CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.contains("underLine")) {
				List<String> paymentType2 = Lists.newArrayList();
				paymentType2.addAll(paymentTypeList);
				paymentType2.remove("underLine");
				dto.setPaymentTypeList(paymentType2);
				dto.setPaymentTypeNewList(paymentType2);
			}else {
				dto.setPaymentTypeNewList(paymentTypeList);
			}
			if(CollUtil.isNotEmpty(dto.getPaymentTypeList())) {
				List<String> listNew = Lists.newArrayList("wx","coupon","salary");
				if(dto.getPaymentTypeList().containsAll(listNew)) {
					List<String> paymentTypeNew = Lists.newArrayList();
					paymentTypeNew.addAll(dto.getPaymentTypeList());
					paymentTypeNew.removeAll(listNew);
					dto.setPaymentTypeNewList(paymentTypeNew);
				}
			}
			if(dto.getPaymentTypeList() != null && (dto.getPaymentTypeList().contains("wx") ||
					dto.getPaymentTypeList().contains("coupon") || 
					dto.getPaymentTypeList().contains("salary"))) {
				dto.setIsAPayCode("1");
			}
			log.info("查询购买和消费类型订单信息，findOrderListByExport 请求参数 request param dto:{}",JSON.toJSON(dto));
			List<OrderListVo> orderQueryList = orderMapper.findOrderListByExport(dto);
			log.info("查询购买和消费类型订单信息，findOrderListByExport 返回结果 return result orderQueryList:{}",JSON.toJSON(orderQueryList));
			if(CollUtil.isNotEmpty(orderQueryList)) {
				List<String> memberIds = Lists.newArrayList();
				for (OrderListVo orderListVo : orderQueryList) {
					StringBuffer sbf = new StringBuffer();
					if(orderListVo.getOrderType() != null && orderListVo.getOrderType().equals("2")) {
						orderListVo.setOrderTypeName("现金消费");
						if(orderListVo.getAPayCode().equals("salary") || orderListVo.getAPayCode().equals("s_cash")) {
							sbf.append(","+"工资额度");
						}else {
							sbf.append(","+DictionaryEnum.paymentType.existsAct(orderListVo.getAPayCode()));
						}
					}else if(orderListVo.getOrderType() != null && orderListVo.getOrderType().equals("1")) {
						orderListVo.setOrderTypeName("消费券购买");
						orderListVo.setPaymentTypeName("消费券支付");
					}
					if(orderListVo.getCouponAmount()!=null && orderListVo.getCouponAmount().doubleValue() > 0) {
						sbf.append(","+"消费券支付");
					}
					if (sbf.toString().startsWith(",")) {
						orderListVo.setPaymentTypeName(sbf.toString().substring(1));
			        }
					orderListVo.setStatus("0");
					if(!memberIds.contains(orderListVo.getMemberId())) {
						memberIds.add(orderListVo.getMemberId());
					}
				}
				QueryMemberCerDto cerDto = new QueryMemberCerDto();
				cerDto.setMemberIdList(memberIds);
				log.info("批量查询员工信息请求参数，request param cerDto:{}",JSON.toJSON(cerDto));
				List<MemberCerVo> memberList = queryMemberInfoByParam(cerDto);
				List<MemberCerVo> memberNewList = Lists.newArrayList();
				log.info("批量查询员工信息返回结果，return result memberList:{}",JSON.toJSON(memberList));
				memberNewList = memberList !=null ? memberList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(MemberCerVo::getId))), ArrayList::new)):null;
				log.info("批量查询员工信息后再去通过memberId去重，return result memberNewList:{}",JSON.toJSON(memberNewList));
				Map<String, MemberCerVo> memberMap = memberNewList != null ? memberNewList.stream().collect(Collectors.toMap(MemberCerVo::getId, o -> o)):null;
				/**拼接员工姓名和手机号信息**/
				for (OrderListVo orderListVo : orderQueryList) {
					if(ObjectUtil.isNotNull(memberMap.get(orderListVo.getMemberId()))) {
						MemberCerVo memberCerVo = memberMap.get(orderListVo.getMemberId());
						orderListVo.setMemberName(memberCerVo.getRealName() == null ? memberCerVo.getNickName(): memberCerVo.getRealName());
						orderListVo.setMobile(memberCerVo.getMobile());
					}
				}
			}
			returnList.addAll(orderQueryList);
		}
		 /**orderTypeList订单类型集合：1：购买；2：消费；3：简易报销
	     	paymentTypeList付款方式集合：coupon:消费券支付；jbt:嘉白条；salary：工资额度；wx：微信支付(wx_wft/wx_wft_all)；alipay:支付宝;underLine:线下支付
	     	orderStatusList订单状态集合: 0:已付款（购买、消费订单）；报销单状态：1:待审批；3:审批通过 ；4：审批拒绝；5：撤销申请
	     */
		/**满足条件去查简易报销订单信息**/
		if(CollUtil.isEmpty(dto.getOrderTypeList()) && CollUtil.isEmpty(paymentTypeList) && CollUtil.isEmpty(dto.getOrderStatusList())
				|| (CollUtil.isEmpty(dto.getOrderTypeList()) && (CollUtil.isEmpty(paymentTypeList) || (CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.contains("underLine"))) &&  CollUtil.isNotEmpty(dto.getOrderStatusList()) && (!dto.getOrderStatusList().contains("0") && !dto.getOrderStatusList().contains("6") && !dto.getOrderStatusList().contains("7") ) )
				|| (CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("3") && CollUtil.isEmpty(paymentTypeList) )) 
				|| (CollUtil.isNotEmpty(dto.getOrderTypeList()) && (dto.getOrderTypeList().contains("3") && CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.contains("underLine")) )
				|| (CollUtil.isEmpty(dto.getOrderTypeList()) && CollUtil.isNotEmpty(paymentTypeList) && paymentTypeList.contains("underLine")) 
				|| (CollUtil.isEmpty(dto.getOrderTypeList()) && CollUtil.isEmpty(dto.getPaymentTypeList()) && (!dto.getOrderStatusList().contains("0") || !dto.getOrderStatusList().contains("6") || !dto.getOrderStatusList().contains("7")))) {
			ReimburserQueryDTO reimburserQueryDTO = new ReimburserQueryDTO();
			if(CollUtil.isEmpty(dto.getOrderStatusList())) {
				String[] status = new String[] {"1","3","4","5"};
				reimburserQueryDTO.setStatus(status);
			}
			if(dto != null && StringUtils.isNotBlank(dto.getOrderNo())) {
				reimburserQueryDTO.setReimburseCode(dto.getOrderNo());
			}
			if(dto != null && StringUtils.isNotBlank(dto.getMemberName())) {
				reimburserQueryDTO.setRealName(dto.getMemberName());
			}
			if(dto != null && StringUtils.isNotBlank(dto.getMobile())) {
				reimburserQueryDTO.setPhone(dto.getMobile());
			}
			if(dto != null && dto.getStartDate() != null ) {
				reimburserQueryDTO.setStartDate(dto.getStartDate());
			}
			if(dto != null && dto.getEndDate() != null) {
				reimburserQueryDTO.setEndDate(dto.getEndDate());
			}
			if(dto != null && CollUtil.isNotEmpty(dto.getOrderStatusList())) {
				reimburserQueryDTO.setStatus(dto.getOrderStatusList().toArray(new String[dto.getOrderStatusList().size()]));
			}
			log.info("查询简易报销单信息，findOrderListPa	ge 请求参数 request param reimburserQueryDTO:{}",JSON.toJSON(reimburserQueryDTO));
			List<ReimburseVO> reimburseList = refactorInvoiceReimburseService.reimburseList(reimburserQueryDTO);
			log.info("查询简易报销单信息，findOrderListPage 返回结果 return result reimburseList:{}",JSON.toJSON(reimburseList));
			if(CollUtil.isNotEmpty(reimburseList)) {
				List<OrderListVo> reimburseReturn = Lists.newArrayList();
				for (ReimburseVO reimburseVO : reimburseList) {
					OrderListVo orderListVO = new OrderListVo();
					orderListVO.setOrderNo(reimburseVO.getReimburseCode());
					orderListVO.setMemberId(reimburseVO.getMemberId());
					orderListVO.setMemberName(reimburseVO.getMemberName());
					orderListVO.setMobile(reimburseVO.getPhone());
					orderListVO.setOrderAmount(reimburseVO.getTotalAmt());
					orderListVO.setOrderType("3");
					orderListVO.setOrderTypeName("贴票报销");
					orderListVO.setPaymentTypeName(DictionaryEnum.paymentType.acct_type_under_line.getName());
					orderListVO.setCreateTime(reimburseVO.getReimbDate());
					orderListVO.setReimbId(reimburseVO.getId());
					orderListVO.setStatus(reimburseVO.getStatus());
					if(reimburseVO.getStatus().equals(reimburseStatus.reimburseStatus_3.getCode())) {
						orderListVO.setStatusName("审批通过");
					}else if(reimburseVO.getStatus().equals(reimburseStatus.reimburseStatus_1.getCode())) {
						orderListVO.setStatusName("待审批");
					}else if(reimburseVO.getStatus().equals(reimburseStatus.reimburseStatus_4.getCode())) {
						orderListVO.setStatusName("审批拒绝");
					}else if(reimburseVO.getStatus().equals(reimburseStatus.reimburseStatus_5.getCode())) {
						orderListVO.setStatusName("撤销申请");
					}
					reimburseReturn.add(orderListVO);
				}
				returnList.addAll(reimburseReturn);
			}
		}
		List<OrderListVo> sortList = returnList != null ? returnList.stream().sorted(Comparator.comparing(OrderListVo::getCreateTime).reversed()).collect(Collectors.toList()) : null;
		return sortList;
	}


}
