package com.taolue.baoxiao.fund.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.MqQueueConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderBusiStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.SequenceNumber;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.dto.OrderBusiDto;
import com.taolue.baoxiao.fund.api.dto.OrderBusiQueryDto;
import com.taolue.baoxiao.fund.api.dto.OrderBusiReturnDto;
import com.taolue.baoxiao.fund.api.dto.OrderPaymentDto;
import com.taolue.baoxiao.fund.api.vo.OrderBusiVo;
import com.taolue.baoxiao.fund.entity.HisOrderBusi;
import com.taolue.baoxiao.fund.entity.OrderBusi;
import com.taolue.baoxiao.fund.entity.OrderMember;
import com.taolue.baoxiao.fund.entity.OrderPayment;
import com.taolue.baoxiao.fund.mapper.OrderBusiMapper;
import com.taolue.baoxiao.fund.mapper.OrderBusiQueryMapper;
import com.taolue.baoxiao.fund.service.IOrderBusiService;
import com.taolue.baoxiao.fund.service.IOrderMemberService;
import com.taolue.baoxiao.fund.service.IOrderPaymentService;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService;
import com.taolue.baoxiao.fund.service.remote.IRefactorCouponServiceFactory;
import com.taolue.baoxiao.fund.service.remote.IRefactorInvoicServiceFactory;
import com.taolue.baoxiao.fund.service.remote.IRefactorMemberServiceFactory;
import com.taolue.coupon.api.dto.CouponBuyApplyDTO;
import com.taolue.coupon.api.vo.CouponVo;
import com.taolue.invoice.api.dto.ReimburseDTO;
import com.taolue.invoice.api.vo.ReimburseVO;
import com.taolue.member.api.dto.CompanyVendorDto;
import com.taolue.member.api.vo.CompanyDetailVo;
import com.taolue.member.api.vo.CompanyVendorVo;
import com.taolue.member.api.vo.MemberVendorVo;
import com.taolue.member.api.vo.MemberVo;
import com.taolue.member.api.vo.QueryDeptMemberVo;


/**
 * 
 * 
 * @ClassName:  OrderBusiServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:52:50   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Lazy
public class OrderBusiServiceImpl extends HisSupportServiceImpl<OrderBusiMapper, OrderBusi, HisOrderBusi> implements IOrderBusiService {
	private Log logger = LogFactory.getLog(OrderBusiServiceImpl.class);
	@Autowired
	private  IOrderMemberService iOrderMemberService;
	
	@Autowired
	private  IOrderPaymentService iOrderPaymentService;
	
	@Autowired
	private IRefactorCouponServiceFactory refactorCouponServiceFactory;
	
	@Autowired
	private IRefactorMemberServiceFactory refactorMemberServiceFactory;
	
	@Autowired
	private IRefactorInvoicServiceFactory refactorInvoicServiceFactory;
	
	@Autowired
	private OrderBusiQueryMapper orderBusiQueryMapper;
	
//	@Autowired
//	private IRefactorInvoiceReimburseService iRefactorInvoiceReimburseService;
	
	@Autowired
	private IAcctBalanceBusiService acctBalanceBusiService;
	
	
	@Autowired
    private RabbitTemplate rabbitTemplate;

	
	@Override
	public boolean updateBillNo(Map<String,Object> params) {
		this.baseMapper.updateBillNo(params);
		return true;
	}

	@Override
	public List<OrderBusiVo> showOrderBusiByParams(Map<String, Object> params) {
		List<OrderBusi> busiList=baseMapper.selectByMap(params);
	
		List<OrderBusiVo> dtlList = new ArrayList<OrderBusiVo>();
		if(busiList !=null && busiList.size() >0) {
			for (OrderBusi orderBusi : busiList) {
				OrderBusiVo vo=new OrderBusiVo();
				BeanUtils.copyProperties(orderBusi, vo);
				dtlList.add(vo);
			}
		}
		
		return dtlList;
	}

	
	
	@Override
	@Transactional(rollbackFor = {Exception.class })  
	public boolean addOrder(Map<String, Object> params) {
		/**
		 *     业务逻辑》》
		 *     父数据之前根据单号已经生成，可以得到这条信息，添加订单子数据（子数据最多四条。根据业务模式分组查询的）
		 * 然后在根据单号查询的账户资金信息，去添加订单支付信息，根据memberid添加订单member信息表
		 * 添加订单支付信息需要获取卷信息
		 */
		try {
			//接口信息，然后生成表信息。
			String tradeBusiCode=params.get("tradeBusiCode").toString();//支付单号	
			//String reimburseCode=params.get("reimburseCode").toString();//报销单号
			//按照人员，归属人，归属公司，业务模式，四个维度分组
			

			List<FundBalanceDto> balanceGroupBusiModelList=acctBalanceBusiService
					.findBalanceGroupWithFlowParams(tradeBusiCode);
			logger.info("生成订单资金汇总返回的参数>>"+JSON.toJSONString(balanceGroupBusiModelList));

			
			Map<String, Object> orderBusiMap=new HashMap<String, Object>();
			orderBusiMap.put("order_code", tradeBusiCode);
			orderBusiMap.put("parent_id", "0");
			List<OrderBusi> orderBusiList=baseMapper.selectByMap(orderBusiMap);
			if(orderBusiList.size()<=0) {
				logger.info("根据卷order_code="+tradeBusiCode+",没得到主订单信息");
   				throw new BaoxiaoException(1001, "没添加主订单信息");
			}

			String businessId=orderBusiList.get(0).getBusinessId();
			String orderType=orderBusiList.get(0).getMainType();//OT0014:消费券  OT0018:报销
			ReimburseDTO reimburseDto=new ReimburseDTO();
			reimburseDto.setReimburseCode(orderBusiList.get(0).getSourceCode());
			ReimburseVO reimburseVo=this.refactorInvoicServiceFactory.getRefactorInvoiceReimburseService().reimburseDetail(reimburseDto);
			logger.info("报销信息》》"+JSON.toJSONString(reimburseVo));
			logger.info("主订单信息》》》"+JSON.toJSONString(orderBusiList.get(0)));
			String memberIdd="";
			if(!StringUtils.isEmpty(orderBusiList.get(0).getMemberId())) {
				memberIdd=orderBusiList.get(0).getMemberId();
			}
			MemberVo memberP=refactorMemberServiceFactory.getRefactorMemberService().get(memberIdd);
			//String newOrderNo="XF"+IdWorker.getIdStr();
			if(memberP.getType().equals(MemberCateEnums.MEMBER_CATE_ALO.getCateCode())) {
				OrderMember orderMember=new OrderMember();
				orderMember.setOrderNo(orderBusiList.get(0).getOrderNo());//订单编号
				orderMember.setMemberOrderNo("OM"+IdWorker.getIdStr());//关联对象编号
				orderMember.setMemberId(memberP.getId());
				
				orderMember.setMemberName(memberP.getNickName());
		
				orderMember.setMemberNo(memberP.getId());
				orderMember.setMemberDepId("");
				orderMember.setMemberDepName("");
				orderMember.setMemberMobile(memberP.getMobile());
				orderMember.setMemberStatus("");// 是否在职，1：是，0：否
				iOrderMemberService.insert(orderMember);
			}else {
	
				//获取member信息
				List<QueryDeptMemberVo> memDeptList=refactorMemberServiceFactory.getRefactorMemberPlatformService()
						.queryDeptMemberByMemberId(memberIdd,
								orderBusiList.get(0).getCompanyId());
		
				//根据主订单交易人生成order——member
				if(null!=memDeptList && memDeptList.size()>0) {
					OrderMember orderMember=new OrderMember();
					orderMember.setOrderNo(orderBusiList.get(0).getOrderNo());//订单编号
					orderMember.setMemberOrderNo("OM"+IdWorker.getIdStr());//关联对象编号
					orderMember.setMemberId(memDeptList.get(0).getMemberId());
					orderMember.setMemberName(memDeptList.get(0).getRealName());
					orderMember.setMemberNo(memDeptList.get(0).getMemberId());
					orderMember.setMemberDepId(memDeptList.get(0).getDeptId());
					orderMember.setMemberDepName(memDeptList.get(0).getDeptName());
					orderMember.setMemberMobile(memDeptList.get(0).getMobile());
					orderMember.setMemberStatus(memDeptList.get(0).getIsOnjob());// 是否在职，1：是，0：否
					iOrderMemberService.insert(orderMember);
				}
			}
			
			
			 //然后再生成明细
			for (FundBalanceDto fundBalanceDto : balanceGroupBusiModelList) {
				String memberId=fundBalanceDto.getMemberId();//
				String busiModel=fundBalanceDto.getBusiModel();//业务模式
				String acctCate=fundBalanceDto.getAcctCate();//账户类型
				String memberCate=fundBalanceDto.getMemberCate();//账号类型
				BigDecimal composAmount=fundBalanceDto.getBalanceAmount();//支付金额
				String id=fundBalanceDto.getBalanceItemCode();//卷id
				String canticket=fundBalanceDto.getCanTicket();//是否可开票
				OrderPayment orderPayment=new OrderPayment();
				//String paymentOrderNo="OP"+IdWorker.getIdStr();
				String idWorker=IdWorker.getIdStr();
				SequenceNumber sn=new SequenceNumber(idWorker);
				String paymentOrderNo="";//订单明细编号
				String paymentCate="";//支付类型
				String originalMemberId="";//归属人
				String originalMemberName="";//归属人
				String originalCompanyId="";//归属公司
				String originalCompanyName="";//归属公司
				String paymentIndustryId="";//行业
				String paymentIndustryName="";//
			
				
				
				CompanyDetailVo companyVo=refactorMemberServiceFactory.getRefactorMemberCompanyService()
						.queryCompanyDetail(fundBalanceDto.getCompanyId());
				
				logger.info("根据企业查询公司信息"+JSON.toJSONString(companyVo));
				List<String> idList=new ArrayList<String>();
				idList.add(id);
				List<CouponVo> 	couponvoList=refactorCouponServiceFactory.getRefactorCouponService()
						.getListByIdList(idList);

				if(null==couponvoList || couponvoList.size()<=0) {
					logger.info("根据卷Id="+id+",没得到卷信息");
       				throw new BaoxiaoException(1001, "根据卷Id="+id+",没得到卷信息");
				}	
				
			
				orderPayment.setPaymentItemNo(couponvoList.get(0).getId());//支付项目编号
				orderPayment.setPaymentItemName(couponvoList.get(0).getName());//支付项目名称
		
				//报销
				if((OrderType.ORDER_TYPE_REIMBURSE.getCateCode()).equals(orderType)) {
					paymentOrderNo=sn.getLiteBuinessCode("BX", null);
					paymentCate=BillItemSubCate.BILL_ITEM_SUBCATE_XFBX.getCateCode();
					originalMemberId=reimburseVo.getMemberId();
					originalCompanyId=reimburseVo.getCompanyId();
					originalCompanyName=reimburseVo.getCompanyName();
					originalMemberName=reimburseVo.getMemberName();
					paymentIndustryId=reimburseVo.getCategoryId();
					paymentIndustryName=reimburseVo.getCategoryName();
					orderPayment.setStatus("3");//已收票
					
				}else if((OrderType.ORDER_TYPE_CASH.getCateCode()).equals(orderType)){
				//消费券
					paymentOrderNo=sn.getLiteBuinessCode("XF", null);
					paymentCate=BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode();
					originalMemberId=fundBalanceDto.getOwnerId();
					originalCompanyId=fundBalanceDto.getCompanyId();
					if(!ObjectUtils.isEmpty(companyVo)) {
						originalCompanyName=companyVo.getCompanyName();
					}
				
				}else {
					logger.info("订单类型有误：没有此类型>orderType="+orderType);
       				throw new BaoxiaoException(1001, "单类型有误");
				}
				
				orderPayment.setPaymentCate(paymentCate);//支付类型
				orderPayment.setOrderNo(orderBusiList.get(0).getOrderNo());
				orderPayment.setPaymentOrderNo(paymentOrderNo);
				orderPayment.setBusiModle(busiModel);//业务模式
				orderPayment.setPaymentAcctCate(acctCate);//支付账户类型 
				orderPayment.setPaymentMemberCate(memberCate);//支付member类型
			
				orderPayment.setPaymentAmount(composAmount);//支付数量  
				orderPayment.setPaymentMemberId(memberId);
				
				//归属人，公司
				orderPayment.setOriginalMemberId(originalMemberId);
				orderPayment.setOriginalCompanyId(originalCompanyId);
				
				orderPayment.setOriginalCompanyName(originalCompanyName);
	
				
				MemberVo member=refactorMemberServiceFactory.getRefactorMemberService().get(fundBalanceDto.getOwnerId());
				if(member.getType().equals(MemberCateEnums.MEMBER_CATE_ALO.getCateCode())) {
					OrderMember orderMember=new OrderMember();
					
					orderMember.setPaymentOrderNo(paymentOrderNo);
					orderMember.setMemberOrderNo("OM"+IdWorker.getIdStr());//关联对象编号
					orderMember.setMemberId(member.getId());
					orderMember.setMemberName(member.getNickName());
					orderMember.setMemberDepId("");
					orderMember.setMemberDepName("");
					orderMember.setMemberNo(member.getId());
					orderMember.setMemberMobile(member.getMobile());
					orderMember.setMemberStatus("");// 是否在职，1：是，0：否
					iOrderMemberService.insert(orderMember);
				}else {
					//获取member信息
					List<QueryDeptMemberVo> couponDeptList=refactorMemberServiceFactory
							.getRefactorMemberPlatformService()
							.queryDeptMemberByMemberId(fundBalanceDto.getOwnerId(),fundBalanceDto.getCompanyId());
					if(null!=couponDeptList && couponDeptList.size()>0) {
						originalMemberName=couponDeptList.get(0).getRealName();
					}	
					logger.info("开始添加卷归属人信息>>>>");
					//根据主订单交易人生成order——member
					if(null!=couponDeptList && couponDeptList.size()>0) {
						//orderPayment.setOriginalMemberName(couponDeptList.get(0).getRealName());
						OrderMember orderMember=new OrderMember();
						//orderMember.setOrderNo(orderBusiList.get(0).getOrderNo());//订单编号
						orderMember.setPaymentOrderNo(paymentOrderNo);
						orderMember.setMemberOrderNo("OM"+IdWorker.getIdStr());//关联对象编号
						orderMember.setMemberId(couponDeptList.get(0).getMemberId());
						orderMember.setMemberName(couponDeptList.get(0).getRealName());
						orderMember.setMemberNo(couponDeptList.get(0).getMemberId());
						orderMember.setMemberDepId(couponDeptList.get(0).getDeptId());
						orderMember.setMemberDepName(couponDeptList.get(0).getDeptName());
						orderMember.setMemberMobile(couponDeptList.get(0).getMobile());
						orderMember.setMemberStatus(couponDeptList.get(0).getIsOnjob());// 是否在职，1：是，0：否
						iOrderMemberService.insert(orderMember);
					}
				}
				
				orderPayment.setOriginalMemberName(originalMemberName);
				
				if(StringUtils.isEmpty(id)) {
					logger.info("错误提示》》》》》。卷Id is null");
       				throw new BaoxiaoException(1001, "卷Id is null");
				}

			                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
				
				// 设置商户
				//报销
				if((OrderType.ORDER_TYPE_REIMBURSE.getCateCode()).equals(orderType)) {
					orderPayment.setPaymentVendorName(DictionaryEnum.AcctCateEnums.ACCT_CATE_PTLZ.getCateName());//支付项目归属商户名称
					orderPayment.setPaymentVendorId(DictionaryEnum.AcctCateEnums.ACCT_CATE_PTLZ.getCateMgn());//支付项目归属商户id
					
				}else if((OrderType.ORDER_TYPE_CASH.getCateCode()).equals(orderType)){
				//消费券
					//commercialList商户
					/*List<CouponRelationVo> cialList=couponvoList.get(0).getCommercialList();
					if(null==cialList || cialList.size()<=0) {
						logger.info("根据卷idbalanceItemCode="+id+",没得到商户信息");
						throw new BaoxiaoException(1001, "根据卷Id="+id+",没得到商户信息");
					}*/
					
					orderPayment.setPaymentVendorId(businessId);//支付项目归属商户标识
					//根据商户id得到商户信息; old逻辑取得消费券的商户名字
					MemberVendorVo vendor=refactorMemberServiceFactory.getRefactorVendorService()
								.get(businessId);
					String vendorName=vendor.getVendorName();
					if(!StringUtils.isEmpty(vendor.getIndustryCode())) {
						paymentIndustryId=vendor.getIndustryCode();
						paymentIndustryName=vendor.getIndustryName();
					}

					
					orderPayment.setPaymentVendorName(vendorName);//支付项目归属商户名称
						
				
					List<String> strVendorIdList= Lists.newArrayList();
		        	strVendorIdList.add(businessId);
		        	//查询是否有内部商户
		        	/*List<MemberVendorVo> memberVendorVoList=refactorMemberServiceFactory
		        			.getRefactorVendorService().findVendorListByIdList(strVendorIdList);
		        	if(null!=memberVendorVoList && memberVendorVoList.size()>0) {
		        		orderPayment.setInternalVendorId(memberVendorVoList.get(0).getId());
		        		orderPayment.setInternalVendorName(memberVendorVoList.get(0).getVendorName());
		        	}*/
				}
				orderPayment.setPaymentIndustryId(paymentIndustryId);
				orderPayment.setPaymentIndustryName(paymentIndustryName);
				logger.info("订单的payment添加的参数>>>"+JSON.toJSONString(orderPayment));
				iOrderPaymentService.insert(orderPayment);
				
				//OrderDetail orderDetail=new OrderDetail();
				//BeanUtils.copyProperties(orderPayment, orderDetail);
				//orderDetail.setStatus(OrderDetailStatus.SUCCESS.getCateCode());
				//orderDetail.setOrderNo(newOrderNo);
				CouponBuyApplyDTO couponBuyApplyDto=new CouponBuyApplyDTO();
				couponBuyApplyDto.setCouponId(orderPayment.getPaymentItemNo());
				couponBuyApplyDto.setApplyor(memberId);
				//独立c用户去管理购买申请单号
				/*if(memberCate.equals(MemberCateEnums.MEMBER_CATE_ALO.getCateCode()) && canticket.equals("1")) {
					R<CouponBuyApplyVo> applyVo=refactorCouponBuyApplyService.findMaxAmountByCouponId(couponBuyApplyDto);
					logger.info("生成消费订单查询购买申请单号返回为"+JSON.toJSONString(applyVo));
					if(null!= applyVo && applyVo.getCode()==R.SUCCESS) {
						orderDetail.setBillNo(applyVo.getData().getApplyNo());
					}else {
						logger.info("生成消费订单---bug-查询购买申请单号错误");
					}
				}*/
				//orderDetail.setPaymentOrderNo("XF"+IdWorker.getIdStr());
				//iOrderDetailService.insert(orderDetail);
			}
			
			//Order order=new Order();
			//BeanUtils.copyProperties(orderBusiList.get(0), order);
			//order.setId("");
			//order.setStatus(OrderBusiStatus.COMPLETED.getCateCode());
			//order.setOrderNo(newOrderNo);
			//order.setMainType(TransType.CONSUME.getSysCode());
			//order.setMainTypeName(TransType.CONSUME.getSysName());
			//order.setSubType(TransType.CONSUME.getSysCode());
			//order.setSubTypeName(TransType.CONSUME.getSysName());
			//logger.info("插入新订单主表数据:{}"+JSON.toJSONString(order));
			//iOrderService.insert(order);
			CompanyVendorDto dto=new CompanyVendorDto();
			dto.setMemberId(orderBusiList.get(0).getMemberId());
			CompanyVendorVo companyVendorVo=refactorMemberServiceFactory.getRefactorMemberPlatformService().queryCompanyOrVendorByParam(dto);
			logger.info("获取企业信息》》》queryCompanyOrVendorByParam。"+JSON.toJSONString(companyVendorVo));
/*			List<CompanyVo> companyList=refactorMemberServiceFactory.getRefactorMemberCompanyRelationService()
					.queryCompanyList(orderBusiList.get(0).getMemberId(),orderBusiList.get(0).getCompanyId());*/
			if(ObjectUtils.isEmpty(companyVendorVo)) {
				logger.info("根据卷memberId="+orderBusiList.get(0).getMemberId()+",没得到公司信息");
   				//throw new BaoxiaoException(1001, "没添加主订单信息");
			}else {
				logger.info("find company from member for=====memberId="+orderBusiList.get(0).getMemberId()+",company info");
				//交易人公司信息回写
				OrderBusi orderBusi=new OrderBusi();
				orderBusi=orderBusiList.get(0);
				orderBusi.setCompanyId(companyVendorVo.getCompanyId());
				orderBusi.setCompanyName(companyVendorVo.getCompanyName());
				baseMapper.updateById(orderBusi);
				
				//Order upOrder=new Order();
				//upOrder=order;
				//upOrder.setCompanyId(companyVendorVo.getCompanyId());
				//upOrder.setCompanyName(companyVendorVo.getCompanyName());
				//iOrderService.updateById(upOrder);
			}
			
			
		} catch (Exception e) {
			logger.error("添加订单失败",e);
			throw new BaoxiaoException(1006,"添加订单失败");
		}
		return true;
	}

	@Override
	public boolean addOrderBusi() {
		OrderBusi orderBusi=new OrderBusi();
		orderBusi.setOrderCode("A001");
		orderBusi.setOrderAmount(new BigDecimal("1000"));
		orderBusi.setPayAmount(new BigDecimal("1000"));
		orderBusi.setCouponAmount(new BigDecimal("500"));
		orderBusi.setParentId("0");
		orderBusi.setCompanyId("111");
		orderBusi.setMainType("11");
		orderBusi.setSubType("111");
		orderBusi.setBusinessId("1027131946406907906");
		baseMapper.insert(orderBusi);
		Map<String, Object> contextJson = new HashMap();
        contextJson.put("tradeBusiCode", "A001");
        //contextJson.put("type", "addOrderBusi");     
        
        //contextJson.put("product", "Baoxiao");
        logger.info("发送新订单生成消息 -> 新订单编号:{A001}");
//        rabbitTemplate.convertAndSend(MqQueueConstant.ORDER_BUSI_CREATED,contextJson);
		return true;
	}

	@Override
	public Page<OrderBusiReturnDto> queryOrderBusi(Query query, OrderBusiQueryDto dto) {
	/*	List<OrderBusiReturnDto> newOrderBusiList=Lists.newArrayList();
		List<OrderBusiReturnDto> orderBusiList=orderBusiQueryMapper.selectOrderBusiByQuery(query, dto);
		if(orderBusiList.size()>0) {
			logger.info(">>>>PaymentStatus="+orderBusiList.get(0).getPaymentStatus());
		}*/
		/*for (OrderBusiReturnDto orderBusiReturnDto : orderBusiList) {
			OrderBusiReturnDto newOrderBusiReturnDto=new OrderBusiReturnDto();
			newOrderBusiReturnDto=orderBusiReturnDto;
        	List<String> strVendorIdList= Lists.newArrayList();
        	String strVendorId=orderBusiReturnDto.getVendorId();
        	strVendorIdList.add(strVendorId);
        	//查询是否有内部商户
        	List<MemberVendorVo> memberVendorVoList=iRefactorVendorService.findVendorListByIdList(strVendorIdList);
        	if(null!=memberVendorVoList && memberVendorVoList.size()>0) {
        		newOrderBusiReturnDto.setPaymentVendorName(memberVendorVoList.get(0).getVendorName());
        	}
        	newOrderBusiList.add(newOrderBusiReturnDto);
		}*/
		query.setRecords(orderBusiQueryMapper.selectOrderBusiByQuery(query, dto));
		return query;
	}
	@Override
	public Page<OrderBusiReturnDto> queryOrderBusiLZ(Query query, OrderBusiQueryDto dto) {
		
	/*	List<OrderBusiReturnDto> newOrderBusiList=Lists.newArrayList();
		List<OrderBusiReturnDto> orderBusiList=orderBusiQueryMapper.selectOrderBusiByQueryLZ(query, dto);
		for (OrderBusiReturnDto orderBusiReturnDto : orderBusiList) {
			OrderBusiReturnDto newOrderBusiReturnDto=new OrderBusiReturnDto();
			newOrderBusiReturnDto=orderBusiReturnDto;
        	List<String> strVendorIdList= Lists.newArrayList();
        	String strVendorId=orderBusiReturnDto.getVendorId();
        	strVendorIdList.add(strVendorId);
        	//查询是否有内部商户
        	List<MemberVendorVo> memberVendorVoList=iRefactorVendorService.findVendorListByIdList(strVendorIdList);
        	if(null!=memberVendorVoList && memberVendorVoList.size()>0) {
        		newOrderBusiReturnDto.setPaymentVendorName(memberVendorVoList.get(0).getVendorName());
        	}
        	newOrderBusiList.add(newOrderBusiReturnDto);
		}*/
		
		query.setRecords(orderBusiQueryMapper.selectOrderBusiByQueryLZ(query, dto));
		return query;
	}

	@Override
	public boolean updateOrderStatus(Map<String, Object> params) {
		return this.baseMapper.updateOrderStatus(params);
	}

	@Override
	public boolean  selectOrderBusiNoSon() {
		List<OrderBusiReturnDto> busiList=orderBusiQueryMapper.selectOrderBusiNoSon();
		logger.info("查询没有生成订单的信息》》》"+JSON.toJSONString(busiList));
		for (OrderBusiReturnDto orderBusiReturnDto : busiList) {
			Map<String,Object> params = Maps.newHashMap();
			params.put("tradeBusiCode", orderBusiReturnDto.getOrderCode());
			logger.info("开始从新生成订单mq>>>"+orderBusiReturnDto.getOrderCode());
			rabbitTemplate.convertAndSend(MqQueueConstant.FUND_EXCHANGE, 
					MqQueueConstant.ORDER_BUSI_TOPIC, params);
		}
		return true;
	}

	@Override
	public boolean addOrder(OrderBusiDto dto) {
		logger.info("添加交易订单入参参数>>"+JSON.toJSONString(dto));
		OrderBusi orderBusi = new OrderBusi();
		BeanUtils.copyProperties(dto, orderBusi);
		String orderNo=CodeUtils.genneratorShort("JY");
		orderBusi.setOrderCode(orderNo);
		orderBusi.setOrderNo(orderNo);
		orderBusi.setDiscountAmount(CommonConstant.NO_AMOUNT);
		orderBusi.setFreightAmount(CommonConstant.NO_AMOUNT);
		orderBusi.setServiceAmount(CommonConstant.NO_AMOUNT);
		orderBusi.setStatus(OrderBusiStatus.COMPLETED.getCateCode());
		orderBusi.setParentId("0");
		List<OrderPaymentDto> paymentDtoList=dto.getPaymentDtoList();
		if(null!=paymentDtoList && paymentDtoList.size()>0) {
			for (OrderPaymentDto orderPaymentDto : paymentDtoList) {
				OrderPayment payment=new OrderPayment();
				BeanUtils.copyProperties(orderPaymentDto, payment);
				payment.setOrderNo(orderNo);
				payment.setPaymentOrderNo(CodeUtils.genneratorShort("JY"));
				payment.setStatus("1");
				iOrderPaymentService.insert(payment);
			}
		}
		this.baseMapper.insert(orderBusi);
		return true;
	}
}
