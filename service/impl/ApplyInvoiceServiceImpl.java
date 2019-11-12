package com.taolue.baoxiao.fund.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.InvoiceApplyStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.InvoiceType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.InvoiceTypeNew;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderBusiStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderDetailStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.PaymentStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TransType;
import com.taolue.baoxiao.common.enums.Sequence;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.MoneyUtils;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.coupon.RefactorCouponDiscountConfigService;
import com.taolue.baoxiao.fund.api.dto.ApplyInvoiceDto;
import com.taolue.baoxiao.fund.api.dto.InvoiceAmountConfigDto;
import com.taolue.baoxiao.fund.api.dto.OrderDTO;
import com.taolue.baoxiao.fund.api.dto.OrderDetailDTO;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberComInvoiceApi;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberService;
import com.taolue.baoxiao.fund.api.member.IRefactorVendorService;
import com.taolue.baoxiao.fund.api.member.RefactorMemberPlatformService;
import com.taolue.baoxiao.fund.api.openplatform.IDockOpenPlatformService;
import com.taolue.baoxiao.fund.api.openplatform.IRefactorOpenBaiWangService;
import com.taolue.baoxiao.fund.api.vo.*;
import com.taolue.baoxiao.fund.entity.FundCouponTaxCode;
import com.taolue.baoxiao.fund.entity.InvoiceApplyDetail;
import com.taolue.baoxiao.fund.entity.Order;
import com.taolue.baoxiao.fund.entity.TbInvoiceApply;
import com.taolue.baoxiao.fund.entity.TbOrderInvoiceApply;
import com.taolue.baoxiao.fund.mapper.InvoiceApplyDetailMapper;
import com.taolue.baoxiao.fund.mapper.OrderDetailMapper;
import com.taolue.baoxiao.fund.mapper.TbInvoiceApplyMapper;
import com.taolue.baoxiao.fund.mapper.TbOrderInvoiceApplyMapper;
import com.taolue.baoxiao.fund.service.IApplyInvoiceService;
import com.taolue.baoxiao.fund.service.IFundCouponTaxCodeService;
import com.taolue.baoxiao.fund.service.IFundInvoiceAmountConfigService;
import com.taolue.baoxiao.fund.service.IInvoiceApplyDetailService;
import com.taolue.baoxiao.fund.service.IOrderService;
import com.taolue.coupon.api.dto.CouponBuyApplyDTO;
import com.taolue.coupon.api.vo.AddPersonalDiscountConfigVo;
import com.taolue.dock.api.common.BaiWangEnum;
import com.taolue.dock.api.dto.SmsDto;
import com.taolue.dock.api.vo.baiwang.BaiWangEbillDto;
import com.taolue.dock.api.vo.baiwang.BaiWangEbillResponseContent;
import com.taolue.dock.api.vo.baiwang.BaiWangXMXXItemVo;
import com.taolue.dock.api.vo.baiwang.BaiWangXMXXVo;
import com.taolue.member.api.dto.MemberInvoiceComRuleDto;
import com.taolue.member.api.vo.*;
import com.xiaoleilu.hutool.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ApplyInvoiceServiceImpl extends ServiceImpl<TbInvoiceApplyMapper,TbInvoiceApply> implements IApplyInvoiceService {

    @Autowired
    private TbInvoiceApplyMapper tbInvoiceApplyMapper;

    @Autowired
    private InvoiceApplyDetailMapper invoiceApplyDetailMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private TbOrderInvoiceApplyMapper tbOrderInvoiceApplyMapper;

    @Autowired
    private IRefactorOpenBaiWangService refactorOpenBaiWangService;
    
    @Autowired
    private IOrderService iOrderService;
    
    @Autowired
    private IInvoiceApplyDetailService iInvoiceApplyDetailService;



    @Autowired
    private RefactorMemberPlatformService iRefactorMemberPlatService;

 

    @Autowired
    private IRefactorMemberComInvoiceApi iRefactorMemberComInvoiceApi;

    @Autowired
    private IRefactorVendorService iRefactorVendorService;
    
    @Autowired
    private  RefactorCouponDiscountConfigService refactorCouponDiscountConfigService;

    @Autowired
    private IFundCouponTaxCodeService fundCouponTaxCodeService;
    @Autowired
    private IFundInvoiceAmountConfigService fundInvoiceAmountConfigService;
    
    @Value("${baiwang.switch}")
    private String baiWangSwitch;


    /**
     * 根据人员id,消费券id获取开票历史记录
     *
     * @return
     * @Param applyInvoiceDto
     */
    @Override
    public R<List<ApplyInvoiceVo>> findOpenInvoiceHistory(ApplyInvoiceDto applyInvoiceDto) {
        log.info("findOpenInvoiceHistory ===>>>" + JSONObject.toJSONString(applyInvoiceDto));
        R<List<ApplyInvoiceVo>> result = new R<List<ApplyInvoiceVo>>();
        List<ApplyInvoiceVo> applyInvoiceVos = Lists.newArrayList();
//        EntityWrapper<TbInvoiceApply> entityWrapper = new EntityWrapper<TbInvoiceApply>();
//        if (StrUtil.isNotBlank(applyInvoiceDto.getApplyId())) {
//            entityWrapper.eq("apply_id", applyInvoiceDto.getApplyId());
//        }
//        if (StrUtil.isNotBlank(applyInvoiceDto.getCouponId())) {
//            entityWrapper.eq("coupon_id", applyInvoiceDto.getCouponId());
//        }
//        if(StrUtil.isNotBlank(applyInvoiceDto.getDelFlag())){
//            entityWrapper.eq("del_flag",applyInvoiceDto.getDelFlag());
//        }
//        if(StrUtil.isNotBlank(applyInvoiceDto.getInvoiceStatus())){
//            entityWrapper.eq("invoice_status",applyInvoiceDto.getInvoiceStatus());
//        }
//        entityWrapper.orderBy("created_time",false);
        List<TbInvoiceApply> tbInvoiceApplies = tbInvoiceApplyMapper.selectInvoiceHistoryList(applyInvoiceDto);
        log.info("findOpenInvoiceHistory==end========》》》》"+JSONObject.toJSONString(tbInvoiceApplies));
        if (CollUtil.isNotEmpty(tbInvoiceApplies)) {
            tbInvoiceApplies.forEach(invoiceApply -> {
                ApplyInvoiceVo applyInvoiceVo = new ApplyInvoiceVo();
                BeanUtil.copyProperties(invoiceApply, applyInvoiceVo);
                applyInvoiceVos.add(applyInvoiceVo);
            });
        }
        if (CollUtil.isNotEmpty(applyInvoiceVos)) {
            result.setCode(R.SUCCESS);
            result.setData(applyInvoiceVos);
        }
        return result;
    }

    /**
     * 根据开票申请获取详情
     *
     * @param applyInvoiceDto 传id或者reimburse_code
     * @return
     */
    @Override
    public R<ApplyInvoiceVo> findInvoiceDetailById(ApplyInvoiceDto applyInvoiceDto) {
        log.info("根据开票申请获取详情===>>>" + JSONObject.toJSONString(applyInvoiceDto));
        R<ApplyInvoiceVo> result = new R<ApplyInvoiceVo>();
        ApplyInvoiceVo applyInvoiceVo = tbInvoiceApplyMapper.findInvoiceDetailById(applyInvoiceDto);
        if (null != applyInvoiceVo) {
            result.setCode(R.SUCCESS);
            result.setData(applyInvoiceVo);
        } else {
            result.setCode(R.FAIL);
            result.setMsg("根据开票申请未能查询到详情");
        }
        log.info("根据开票申请获取详情 end ===>>>" + JSONObject.toJSONString(applyInvoiceVo));
        return result;
    }

    /**
     * 根据成员id查询订单交易集合
     *
     * @return
     * @Param applyInvoiceDto
     */
    @Override
    public R<List<OrderDetailVo>> findOrderListByMemberId(ApplyInvoiceDto applyInvoiceDto) {
        R<List<OrderDetailVo>> result = new R<List<OrderDetailVo>>();
        ApplyInvoiceVo applyInvoiceVo = tbInvoiceApplyMapper.findInvoiceDetailById(applyInvoiceDto);
        List<OrderApplyInvoiceVo> orderApplyInvoiceVos = Lists.newArrayList();
        List<OrderDetailVo> orderDetailVoList = Lists.newArrayList();
        List<String> orderPaymentNos = Lists.newArrayList();
        if (null != applyInvoiceVo) {
            orderApplyInvoiceVos = applyInvoiceVo.getOrderApplyInvoices();
        }
        if (CollUtil.isNotEmpty(orderApplyInvoiceVos)) {
            orderApplyInvoiceVos.forEach(item -> {
                if (null != item && StrUtil.isNotBlank(item.getOrderPaymentNo())) {
                    orderPaymentNos.add(item.getOrderPaymentNo());
                }
            });
        }
        if (CollUtil.isNotEmpty(orderPaymentNos)) {
            OrderDetailDTO orderDetailDto = new OrderDetailDTO();
            orderDetailDto.setDetailPaymentNos(orderPaymentNos);
            orderDetailVoList = orderDetailMapper.selectOrderDetailByIds(orderDetailDto);
        }
        if (CollUtil.isNotEmpty(orderDetailVoList)) {
            result.setCode(R.SUCCESS);
            result.setData(orderDetailVoList);
        } else {
            result.setCode(R.FAIL);
            result.setMsg("根据成员未能查询到订单信息");
            log.info("根据成员未能查询到订单信息====>" + JSONObject.toJSONString(applyInvoiceDto));
        }
        return result;
    }


    @Override
    public R<List<OrderBusiVo>> findOrderDetailByOrderNo(ApplyInvoiceDto applyInvoiceDto) {
        return null;
    }

    @Override
    public BigDecimal getAmountByParams(ApplyInvoiceDto applyInvoiceDto) {
        TbInvoiceApply apply = this.baseMapper.getAmountByParams(applyInvoiceDto);
        log.info("查询子公司开票总金额:{}", JSON.toJSON(apply));
        return apply.getInvoiceAmt();
    }

    /**
     * 新增开票申请
     * @param applyInvoiceDto
     * @return
     */
    @Override
    @Transactional
    public R<ApplyInvoiceVo> addInvoiceApply(ApplyInvoiceDto applyInvoiceDto) {
        log.info("addInvoiceApply==applyInvoiceDto====>>>>"+JSONObject.toJSONString(applyInvoiceDto));
		R<ApplyInvoiceVo> result = new R<ApplyInvoiceVo>();
		applyInvoiceDto.setApplyDate(new Date());
		applyInvoiceDto.setInvoiceStatus(CommonConstant.STATUS_YES);
		applyInvoiceDto.setCreator(applyInvoiceDto.getApplyId());
		applyInvoiceDto.setUpdator(applyInvoiceDto.getApplyId());
        PlatformVo memberVo=iRefactorMemberPlatService.queryByMemberId(applyInvoiceDto.getApplyId(),null);
		if(!ObjectUtils.isEmpty(memberVo)) {
			applyInvoiceDto.setApplyName(memberVo.getRealName());
		}
		// 设置销售方
        MemberInvoiceComRuleDto invoiceComRuleDto = new MemberInvoiceComRuleDto();
        MemberInvoiceComRuleVo invoiceComRuleVo = null;
        invoiceComRuleDto.setInvoiceIndustryCode(applyInvoiceDto.getItemCode());
        invoiceComRuleDto.setInvoiceCategoryCode(applyInvoiceDto.getSubItemCode());
        invoiceComRuleDto.setTaxRate(applyInvoiceDto.getRate());
        invoiceComRuleDto.setInvoiceTypeCode(applyInvoiceDto.getTypeId());
        R<MemberInvoiceComRuleVo> invoiceComRe = iRefactorMemberComInvoiceApi.findOpenCompanyBySubjectAndRate(invoiceComRuleDto);
        if(null != invoiceComRe && R.SUCCESS == invoiceComRe.getCode()){
            invoiceComRuleVo = invoiceComRe.getData();
        }
        if(null != invoiceComRuleVo){
            VendorVo vendorVo = iRefactorVendorService.queryVendorDetail(invoiceComRuleVo.getVendorId());
            applyInvoiceDto.setSalesId(invoiceComRuleVo.getVendorId());
            applyInvoiceDto.setSalesName(vendorVo.getVendorName());
            applyInvoiceDto.setSalesAddr(vendorVo.getContactAddress());
            applyInvoiceDto.setSalesNumber(vendorVo.getIdentificationNumber());
            if(null != vendorVo && CollUtil.isNotEmpty(vendorVo.getBankCardList())){
                MemberBankCardVo bankCardVo = vendorVo.getBankCardList().get(0);
                if(null != bankCardVo &&  StrUtil.isNotBlank(bankCardVo.getSubbranchBankName())){
                    applyInvoiceDto.setSalesBankaccount(bankCardVo.getSubbranchBankName());
                }
            }
        }
        //设置购买方 暂由前端传值
		TbInvoiceApply tbInvoiceApply = new TbInvoiceApply();
		BeanUtil.copyProperties(applyInvoiceDto, tbInvoiceApply);
        if(null != applyInvoiceDto &&  StrUtil.isNotBlank(applyInvoiceDto.getTypeId()) ){
            if(DictionaryEnum.InvoiceTypeNew.VAT_INVOICE.getCateCode().equals(applyInvoiceDto.getTypeId())){
                tbInvoiceApply.setTypeName(DictionaryEnum.InvoiceTypeNew.VAT_INVOICE.getCateName());
            }else if(DictionaryEnum.InvoiceTypeNew.VAT_SPECIAL_INVOICE.getCateCode().equals(applyInvoiceDto.getTypeId())){
                tbInvoiceApply.setTypeName(DictionaryEnum.InvoiceTypeNew.VAT_SPECIAL_INVOICE.getCateName());
            }else{
                tbInvoiceApply.setTypeName(DictionaryEnum.InvoiceTypeNew.E_INVOICE.getCateName());
            }
        }
		String code = CodeUtils.genneratorShort(Sequence.KP.getPerfix());
		tbInvoiceApply.setInvoiceApplyCode(code);

		// 金额拆分
		List<BigDecimal> bigList = amountSplit(applyInvoiceDto.getTaxIncludedPrice());
		log.info("金额拆分====》》》》》"+JSONObject.toJSONString(bigList));
		if (null != bigList && bigList.size() > 0) {
			// 添加订单主表参数组装
			OrderDTO orderDto = addOrder(applyInvoiceDto);
			tbInvoiceApply.setOrderId(orderDto.getOrderNo());
            this.baseMapper.insert(tbInvoiceApply);
            log.info("tbInvoiceApply===insert=》》》》》"+JSONObject.toJSONString(tbInvoiceApply));
			List<OrderDetailDTO> detailDtoList = Lists.newArrayList();
			for (BigDecimal bigDecimal : bigList) {
				InvoiceApplyDetail applyDetail = new InvoiceApplyDetail();
				BeanUtil.copyProperties(tbInvoiceApply, applyDetail,"id");
				applyDetail.setTaxIncludedPrice(bigDecimal);
				// 金额拆分之后添加开票申请明细
				invoiceApplyDetailMapper.insert(applyDetail);
				// 订单明细组装
			}
			
			OrderDetailDTO detailDto = addOrderDetail(tbInvoiceApply);
			detailDtoList.add(detailDto);
			/*//有运费
			if(!ObjectUtils.isEmpty(tbInvoiceApply.getExpressFee())) {
				InvoiceApplyDetail applyDetail = new InvoiceApplyDetail();
				BeanUtil.copyProperties(tbInvoiceApply, applyDetail);
				OrderDetailDTO detailDtoE = addOrderDetail(applyDetail,"2");//type 1:开票  2：运费
				detailDtoList.add(detailDtoE);
			}*/
			orderDto.setDetailDtoList(detailDtoList);
			log.info("添加所有订单明细入参参数为:{}", JSON.toJSONString(orderDto));
			iOrderService.addOrder(orderDto);
		} else {
			throw new BaoxiaoException("开票总金额有误");
		}
		if (null != applyInvoiceDto && CollUtil.isNotEmpty(applyInvoiceDto.getOrderPayNos())) {
			applyInvoiceDto.getOrderPayNos().forEach(item -> {
				TbOrderInvoiceApply orderInvoiceApply = new TbOrderInvoiceApply();
				orderInvoiceApply.setOrderPaymentNo(item);
				orderInvoiceApply.setInvoiceApplyCode(code);
                orderInvoiceApply.setCreator(applyInvoiceDto.getApplyId());
                orderInvoiceApply.setCreatedTime(new Date());
                orderInvoiceApply.setUpdator(applyInvoiceDto.getApplyId());
                orderInvoiceApply.setUpdatedTime(new Date());
				tbOrderInvoiceApplyMapper.insert(orderInvoiceApply);
			});
		}
	
		result.setCode(R.SUCCESS);
		ApplyInvoiceVo applyInvoiceVo = new ApplyInvoiceVo();
		log.info("addInvoiceApply=====>>>>>>"+JSONObject.toJSONString(tbInvoiceApply));
		BeanUtil.copyProperties(tbInvoiceApply, applyInvoiceVo);
		result.setData(applyInvoiceVo);
		return result;
    }
    /**
     * 
     * @Title: amountSplit   
     * @Description: 金额按照规则拆分  
     * 规则是：单张最大金额如果是2万，最小开票金额为200。
     * 如果开票总金额是40100， 拆分为200,20000,19900
     * 如果开票总金额是40300，拆分为300,20000,20000
     * @param: @param invoiceSumAmount
     * @param: @return    
     * @author: duqiang     
     * @return: List<BigDecimal>      
     * @throws
     */
    public  List<BigDecimal> amountSplit(BigDecimal invoiceSumAmount){
    	log.info("金额拆分入参值为：amountSplit{}",invoiceSumAmount);
    	R<AddPersonalDiscountConfigVo> disConfigR=refactorCouponDiscountConfigService.findDiscountConfigCurrentDetails();
    	log.info("查询配置的开票额度限制返回：{}",disConfigR);
    	if(null==disConfigR || disConfigR.getCode()!=R.SUCCESS) {
    		log.info("查询配置的开票额度有误");
    		throw new BaoxiaoException("查询配置的开票额度出错");
    	}
    	BigDecimal ruleMaxAmount=disConfigR.getData().getCouponDiscountConfigVo().getInvoiceAmountMax();//规则最大面额
    	BigDecimal ruleMinAmount=disConfigR.getData().getCouponDiscountConfigVo().getInvoiceAmountMin();//规则最小面额
    	//BigDecimal ruleMaxAmount=new BigDecimal(20000000);
    	//BigDecimal ruleMinAmount=new BigDecimal(2000000);
    	List<BigDecimal> splitAmountList=Lists.newArrayList();
    	//开票总金额大于0，
    	if(invoiceSumAmount.compareTo(new BigDecimal(0))>0) {
    		//开票总金额大于最大规则金额时，去拆分处理，否则直接为一笔
    		if(invoiceSumAmount.compareTo(ruleMaxAmount)>0) {
    			BigDecimal[] bigs=invoiceSumAmount.divideAndRemainder(ruleMaxAmount);
    			//余数跟最小金额对比 
    			if(bigs[1].compareTo(new BigDecimal(0))==0){
    				int bInt=bigs[0].intValue();
					for (int i = 0; i < bInt; i++) {
						splitAmountList.add(ruleMaxAmount);
					}
    			}else  if(bigs[1].compareTo(ruleMinAmount)<0) {
    				//当余数小于最小金额，那么整数减1做处理
    				int bInt=bigs[0].intValue()-1;
    				BigDecimal bAmount=bigs[1];
					//最小金额的一笔
					splitAmountList.add(ruleMinAmount);
					//最大金额减去最小金额的一笔
					splitAmountList.add(ruleMaxAmount.subtract(ruleMinAmount).add(bAmount));
    				//整数要大于0才能去循环
    				if(bInt>0) {
    					for (int i = 0; i < bInt; i++) {
    						splitAmountList.add(ruleMaxAmount);
    					}
    				}
    			}else {
    				int bInt=bigs[0].intValue();
    				BigDecimal bAmount=bigs[1];
    				//余数的一笔
					splitAmountList.add(bAmount);
					for (int i = 0; i < bInt; i++) {
						splitAmountList.add(ruleMaxAmount);
					}
    			}
    		}else {
    			splitAmountList.add(invoiceSumAmount);
    		}
    	}else {
    		log.info("金额拆分金额必须大于0：amountSplit{}",invoiceSumAmount);
    	}
    
    	return splitAmountList;
    }
    
    public OrderDTO addOrder(ApplyInvoiceDto applyInvoiceDto) {
    	 //添加订单
        OrderDTO order=new OrderDTO();
		String orderNo=CodeUtils.genneratorShort("KP");
		order.setOrderCode(orderNo);
		order.setOrderNo(orderNo);
		order.setBusinessId(applyInvoiceDto.getVendorId());
		order.setBusinessName(applyInvoiceDto.getVendorName());
		order.setCompanyId(AcctCateEnums.ACCT_CATE_PTQYX.getCateMgn());
		order.setCompanyName(AcctCateEnums.ACCT_CATE_PTQYX.getCateName());
		order.setMainType(TransType.INVOICE.getSysCode());
		order.setMainTypeName(TransType.INVOICE.getSysName());
		order.setSubType(TransType.INVOICE.getSysCode());
		order.setSubTypeName(TransType.INVOICE.getSysName());
		order.setOrderAmount(applyInvoiceDto.getTaxIncludedPrice());
		//字纸 并且不是立即开票才给运费
		if(!"3".equals(applyInvoiceDto.getTypeId()) && !"1".equals(applyInvoiceDto.getInvoiceSelect())){
            order.setFreightAmount(applyInvoiceDto.getExpressFee());
        }
		order.setPayAmount(applyInvoiceDto.getTaxIncludedPrice());
		order.setCouponAmount(new BigDecimal(0));
		order.setMemberId(applyInvoiceDto.getApplyId());
		order.setSourceType(applyInvoiceDto.getBizSource());
		//电子发票直接为开票成功  
		if(applyInvoiceDto.getTypeId().equals(InvoiceTypeNew.E_INVOICE.getCateCode())) {
			order.setStatus(OrderBusiStatus.PAYMENT_SUCCESS.getCateCode());
		}else {
			order.setStatus(OrderBusiStatus.PENDING_PAYMENT.getCateCode());
		}
	
		log.info("添加购买订单的入参参数:{}",JSON.toJSONString(order));
		return order;
    }

    /**
     * 编辑开票申请
     * @param applyInvoiceDto
     * @return
     */
    @Override
    @Transactional
    public R<ApplyInvoiceVo> editInvoiceApply(ApplyInvoiceDto applyInvoiceDto) {
        R<ApplyInvoiceVo> result = new R<ApplyInvoiceVo>();
        TbInvoiceApply applyInvoice = new TbInvoiceApply();
        applyInvoice.setId(applyInvoiceDto.getId());
        TbInvoiceApply invoiceApply = this.baseMapper.selectOne(applyInvoice);

    	//一期只做电子发票修改状态
        if(invoiceApply.getTypeId().equals(InvoiceType.E_INVOICE.getCateCode())) {
          	Map<String,Object> detailMap=Maps.newHashMap();
          	detailMap.put("invoice_apply_code", invoiceApply.getInvoiceApplyCode());
          	List<InvoiceApplyDetail> detailList=iInvoiceApplyDetailService.selectByMap(detailMap);
          	log.info("查询开票申请明细值为:{}",JSON.toJSON(detailList));
        	//开票成功否者失败
        	if(PaymentStatus.PAYMENT_SUCCESS.getCateCode().equals(applyInvoiceDto.getInvoiceStatus())) {
        		//invoiceApply.setInvoiceStatus(PaymentStatus.PAYMENT_SUCCESS.getCateCode());
        		for (InvoiceApplyDetail invoiceApplyDetail : detailList) {
        			ApplyInvoiceVo applyInvoiceVo = new ApplyInvoiceVo();
                    BeanUtil.copyProperties(invoiceApplyDetail, applyInvoiceVo);
                    
              	
                    
                    
                    /*BaiWangEbillDto ebillDto = this.assemInvoice(applyInvoiceVo);
                    log.info("组装百望接口参数为:{}",JSON.toJSON(ebillDto));
                    R<BaiWangEbillResponseContent> baiwRe = refactorOpenBaiWangService.createEbill(ebillDto);
                    log.info("调用百望接口返回为:{}",JSON.toJSON(baiwRe));
                    if (null != baiwRe && R.SUCCESS == baiwRe.getCode() && null != baiwRe.getData()) {
                    BaiWangEbillResponseContent responseContent = baiwRe.getData();
		                 if ("0000".equals(responseContent.getReturnCode())) {
		                	  invoiceApplyDetail.setInvoiceNo(responseContent.getFP_HM());
		                	  invoiceApplyDetail.setInvoiceCode(responseContent.getFP_DM());
		                	  invoiceApplyDetail.setCheckCode(responseContent.getJYM());
		                	  invoiceApplyDetail.setInvoiceDate(DateUtil.parseDate(responseContent.getKPRQ()));
		                	  invoiceApplyDetail.setInvoiceStatus(PaymentStatus.PAYMENT_SUCCESS.getCateCode());
		                	  invoiceApplyDetail.setPicUrl(responseContent.getPDF_URL());
		                	  OrderDTO orderDto=new OrderDTO();
		                	  orderDto.setOrderNo(invoiceApply.getOrderId());
		                	  orderDto.setStatus(OrderBusiStatus.PAYMENT_SUCCESS.getCateCode());
		                	  iOrderService.uodateOrderStatus(orderDto);
		                	  Boolean flag = false;
		                      SmsDto smsDto = new SmsDto();
		                      smsDto.setSmsCode("SMS0020");
		                      smsDto.setChannel(DictionaryEnum.SmsChannelType.SMS_CHANNEL_TYPE_JF.getCateCode());
		                      smsDto.setType(DictionaryEnum.SmsType.SMS_TYPE_YY.getCateCode());
		                      Map<String,Object> map = Maps.newHashMap();
		                      if(null != applyInvoiceVo){
		                          smsDto.setPhone(applyInvoiceVo.getReceivingPhone());
		                          map.put("url",responseContent.getPDF_URL());
		                      }
		                      smsDto.setMsg(map);
		                      R<Boolean> rBool = refactorOpenCompanyInServiceApi.smsVerification(smsDto);
		                      log.info("开票发送短信返回结果:{}",JSON.toJSONString(rBool));
		                 } else {
		                	  invoiceApplyDetail.setInvoiceStatus(PaymentStatus.PAYMENT_FAIL.getCateCode());
		                	  throw new BaoxiaoException("开票失败");
		                 }
                    } else {
                    	invoiceApplyDetail.setInvoiceStatus(PaymentStatus.PAYMENT_FAIL.getCateCode());
                    	throw new BaoxiaoException("开票失败");
                    }*/
                    //iInvoiceApplyDetailService.updateById(invoiceApplyDetail);
    			}
        	}else {
        		invoiceApply.setInvoiceStatus(PaymentStatus.PAYMENT_FAIL.getCateCode());
        		for (InvoiceApplyDetail invoiceApplyDetail : detailList) {
        			invoiceApplyDetail.setInvoiceStatus(PaymentStatus.PAYMENT_FAIL.getCateCode());
        			iInvoiceApplyDetailService.updateById(invoiceApplyDetail);
    			}
        		OrderDTO orderDto=new OrderDTO();
           	    orderDto.setOrderNo(invoiceApply.getOrderId());
           	    orderDto.setStatus(OrderBusiStatus.PAYMENT_FAIL.getCateCode());
           	    iOrderService.uodateOrderStatus(orderDto);
        	}

        	  this.baseMapper.updateById(invoiceApply);
        	  ApplyInvoiceVo applyInvoiceVo = new ApplyInvoiceVo();
              BeanUtil.copyProperties(invoiceApply, applyInvoiceVo);
	          result.setCode(R.SUCCESS);
	          result.setData(applyInvoiceVo);
    	}else {
    		log.info("不是电子发票类型直接修改为已付款 2 因为王钦那边查询的纸质2为已申请，所以这边存放2");
    		if(PaymentStatus.PAYMENT_SUCCESS.getCateCode().equals(applyInvoiceDto.getInvoiceStatus())) {
        		invoiceApply.setInvoiceStatus("2");
    		}else {
        		invoiceApply.setInvoiceStatus(PaymentStatus.PAYMENT_FAIL.getCateCode());
        	}
    		this.baseMapper.updateById(invoiceApply);
    	}
       /* InvoiceAmountConfigDto configDto=new InvoiceAmountConfigDto();
        configDto.setOrgTaxNo(invoiceApply.getBuyerId());
        configDto.setCompanyId(invoiceApply.getVendorId());
        configDto.setCompanyName(invoiceApply.getVendorName());
        configDto.setInvoiceApplyCode(invoiceApply.getInvoiceApplyCode());
        configDto.setCouponId(invoiceApply.getCouponId());
        configDto.setCouponName(invoiceApply.getCouponName());
        configDto.setMemberId(invoiceApply.getApplyId());
        configDto.setTradeAmount(invoiceApply.getTaxIncludedPrice());
        configDto.setBuyType("1");//立即开票
        R<Boolean> configR=fundInvoiceAmountConfigService.invoiceAmountCheck(configDto);
        log.info("校验发票抬头开票额度返回值:{}",JSON.toJSONString(configR));
        if(null == configR || configR.getCode() != R.SUCCESS) {
        	 log.info("发票抬头额度不足");
			throw new BaoxiaoException(1,"开票额度不足");
		}*/
        return result;
    }
    /**
     *
     * @Title: assemInvoice
     * @Description: 开票数据组装
     * @param: @param applyInvoiceVo
     * @param: @return
     * @author: duqiang
     * @return: BaiWangEbillDto
     * @throws
     */
    public BaiWangEbillDto assemInvoice(ApplyInvoiceVo applyInvoiceVo) {
    	 EntityWrapper<FundCouponTaxCode> entityWrapper =  new EntityWrapper<FundCouponTaxCode>();
         entityWrapper.eq("coupon_id",applyInvoiceVo.getCouponId());
         entityWrapper.eq("main_item_code",applyInvoiceVo.getItemCode());
         entityWrapper.eq("sub_item_code",applyInvoiceVo.getSubItemCode());
         entityWrapper.eq("del_flag","0");
         FundCouponTaxCode taxCode=fundCouponTaxCodeService.selectOne(entityWrapper);
         log.info("查询消费券对应的税收编码为:"+JSON.toJSONString(taxCode));
         if(ObjectUtils.isEmpty(taxCode) || StringUtils.isEmpty(taxCode.getSubTaxCode())) {
             log.info("没有查询到消费券对应的税收编码");
     		throw new BaoxiaoException("没有查询到消费券对应的税收编码");
         }
    	
    	BaiWangEbillDto ebillDto = new BaiWangEbillDto();
        ebillDto.setKplx(BaiWangEnum.KPLX.baiwang_kplx_blue.getCode());
        ebillDto.setXsf_nsrsbh(applyInvoiceVo.getSalesNumber());
        ebillDto.setXsf_mc(applyInvoiceVo.getSalesName());
        ebillDto.setXsf_dzdh(applyInvoiceVo.getSalesAddr());
        ebillDto.setXsf_yhzh(applyInvoiceVo.getSalesBankaccount());
        ebillDto.setGmf_nsrsbh(applyInvoiceVo.getBuyerNumber());
        ebillDto.setGmf_dzdh(applyInvoiceVo.getBuyerAddr());
        ebillDto.setGmf_mc(applyInvoiceVo.getBuyerName());
        ebillDto.setGmf_yhzh(applyInvoiceVo.getBuyerBankaccount());
        ebillDto.setGmf_sjh(applyInvoiceVo.getReceivingPhone());
        ebillDto.setGmf_dzyx(applyInvoiceVo.getMailBox());
        ebillDto.setKpr("杨雪");
        ebillDto.setSkr("朱玲");
        ebillDto.setFhr("朱玲");
        ebillDto.setHjje(applyInvoiceVo.getInvoiceAmt().stripTrailingZeros().toPlainString());
        ebillDto.setHjse(applyInvoiceVo.getTaxIncludedPrice().subtract(applyInvoiceVo.getInvoiceAmt()).stripTrailingZeros().toPlainString());
        if (null != applyInvoiceVo && null != applyInvoiceVo.getInvoiceAmt()) {
            ebillDto.setJshj(applyInvoiceVo.getTaxIncludedPrice().toString());
        }
        //ebillDto.setZsfs("0");
        //ebillDto.setKplx("0");
        //ebillDto.setTspz("00");
        BaiWangXMXXVo ebillDetailDto = new BaiWangXMXXVo();
        List<BaiWangXMXXItemVo> itemVoList = Lists.newArrayList();

        BaiWangXMXXItemVo itemVo = new BaiWangXMXXItemVo();
        itemVo.setXmmc("*" + applyInvoiceVo.getItemName() + "*" + applyInvoiceVo.getSubItemName());
        itemVo.setXmsl("1");
        //itemVo.setFphxz("0");
       
        //itemVo.setSpbm(taxCode.getSubTaxCode().substring(0, 19));
        itemVo.setZxbm(taxCode.getSubTaxCode());
        itemVo.setSl(applyInvoiceVo.getRate().stripTrailingZeros().toPlainString());
        itemVo.setXmje(applyInvoiceVo.getInvoiceAmt().stripTrailingZeros().toPlainString());
        itemVo.setXmdj(applyInvoiceVo.getInvoiceAmt().stripTrailingZeros().toPlainString());
        itemVo.setSe(applyInvoiceVo.getTaxIncludedPrice().subtract(applyInvoiceVo.getInvoiceAmt()).stripTrailingZeros().toPlainString());
        //itemVo.setGgxh("500");
        //itemVo.setDw("3");
        itemVoList.add(itemVo);
        ebillDetailDto.setCommon_fpkj_xmxx(itemVoList);
        ebillDto.setCommon_fpkj_xmxxs(ebillDetailDto);
     
        return ebillDto;
    }
    public OrderDetailDTO addOrderDetail(TbInvoiceApply applyInvoiceDto) {
        OrderDetailDTO detail=new OrderDetailDTO();

        detail.setPaymentCate(BillItemSubCate.BILL_ITEM_SUBCATE_XFXJ.getCateCode());
        detail.setPaymentAmount(applyInvoiceDto.getTaxIncludedPrice());
        detail.setBusiModle(BusiModelEnums.BUSI_MODEL_NONE.getCateCode());
        detail.setPaymentMemberCate(MemberCateEnums.MEMBER_CATE_ALO.getCateCode());
        detail.setPaymentAcctCate(MemberCateEnums.MEMBER_CATE_ALO.getCateCode()+AcctCateEnums.ACCT_CATE_COUPON.getCateCode());
        detail.setPaymentItemNo("");
        detail.setPaymentItemName("");
      //电子发票直接为开票成功  
      	if(applyInvoiceDto.getTypeId().equals(InvoiceTypeNew.E_INVOICE.getCateCode())) {
      		detail.setStatus(OrderDetailStatus.SUCCESS.getCateCode());
      	}else {
      		detail.setStatus(OrderDetailStatus.PROCESS.getCateCode());
      	}
        //detail.setStatus(OrderDetailStatus.PROCESS.getCateCode());
        detail.setPaymentMemberId(applyInvoiceDto.getApplyId());

        detail.setPaymentVendorId(applyInvoiceDto.getVendorId());
        detail.setPaymentVendorName(applyInvoiceDto.getVendorName());
        detail.setBillNo(applyInvoiceDto.getId());//开票申请id
        //detail.setRemark();
        detail.setPaymentIndustryId("");//行业
        detail.setPaymentIndustryName("");
        log.info("添加购买订单明细的入参参数:{}",JSON.toJSONString(detail));
        return detail;
    }

    /**
     * 根据id查询发票申请
     *
     * @param applyInvoiceDto
     * @return
     */
    @Override
    public R<ApplyInvoiceVo> findInvoiceInfo(ApplyInvoiceDto applyInvoiceDto) {
        R<ApplyInvoiceVo> result = new R<ApplyInvoiceVo>();
        ApplyInvoiceVo applyInvoiceVo = new ApplyInvoiceVo();
        TbInvoiceApply tbInvoiceApply = new TbInvoiceApply();
        BeanUtil.copyProperties(applyInvoiceDto, tbInvoiceApply);
        TbInvoiceApply invoiceApply = this.baseMapper.selectOne(tbInvoiceApply);
        BeanUtil.copyProperties(invoiceApply, applyInvoiceVo);
        if (null != applyInvoiceVo) {
            result.setCode(R.SUCCESS);
            result.setData(applyInvoiceVo);
        } else {
            result.setCode(R.FAIL);
            result.setMsg("查询发票失败");
        }
        return result;
    }

    /**
     * 构建开票对象
     *
     * @param applyInvoiceVo
     * @return
     */
    public BaiWangEbillDto processFetchEbill(ApplyInvoiceVo applyInvoiceVo) {
        BaiWangEbillDto ebillDto = new BaiWangEbillDto();
        ebillDto.setKplx(BaiWangEnum.KPLX.baiwang_kplx_blue.getCode());
        ebillDto.setXsf_nsrsbh(applyInvoiceVo.getSalesNumber());
        ebillDto.setXsf_mc(applyInvoiceVo.getSalesName());
        ebillDto.setXsf_dzdh(applyInvoiceVo.getSalesAddr());
        ebillDto.setXsf_yhzh(applyInvoiceVo.getSalesBankaccount());
        ebillDto.setGmf_nsrsbh(applyInvoiceVo.getBuyerNumber());
        ebillDto.setGmf_dzdh(applyInvoiceVo.getBuyerAddr());
        ebillDto.setGmf_mc(applyInvoiceVo.getBuyerName());
        ebillDto.setGmf_yhzh(applyInvoiceVo.getBuyerBankaccount());
        ebillDto.setGmf_sjh(applyInvoiceVo.getReceivingPhone());
        ebillDto.setGmf_dzyx("");
        ebillDto.setKpr(applyInvoiceVo.getDrawer());
        ebillDto.setSkr(applyInvoiceVo.getPayee());
        ebillDto.setFhr(applyInvoiceVo.getChecker());
        BaiWangXMXXVo ebillDetailDto = new BaiWangXMXXVo();
        List<String> orderDetailPaymentNos = Lists.newArrayList();
        if (null != applyInvoiceVo && CollUtil.isNotEmpty(applyInvoiceVo.getOrderApplyInvoices())) {
            applyInvoiceVo.getOrderApplyInvoices().forEach(item -> {
                orderDetailPaymentNos.add(item.getOrderPaymentNo());
            });
        }
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setDetailPaymentNos(orderDetailPaymentNos);
        List<OrderDetailVo> orderDetailVos = orderDetailMapper.selectOrderDetailByIds(dto);
        List<BaiWangXMXXItemVo> itemVoList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(orderDetailVos)) {
            orderDetailVos.forEach(item -> {
                BaiWangXMXXItemVo itemVo = new BaiWangXMXXItemVo();
                itemVo.setXmmc("*" + applyInvoiceVo.getItemName() + "*" + applyInvoiceVo.getSubItemName() + "*" + applyInvoiceVo.getTypeName());
                itemVo.setXmsl("1");
                if (null != item && null != item.getPaymentAmount()) {
                    itemVo.setXmje(item.getPaymentAmount().toString());
                    itemVo.setXmdj(item.getPaymentAmount().toString());
                }
            });
        }
        ebillDetailDto.setCommon_fpkj_xmxx(itemVoList);
        ebillDto.setCommon_fpkj_xmxxs(ebillDetailDto);
        if (null != applyInvoiceVo && null != applyInvoiceVo.getTaxIncludedPrice()) {
            ebillDto.setJshj(MoneyUtils.moneyExpand1000(applyInvoiceVo.getTaxIncludedPrice()).toString());
        }
        return ebillDto;
    }

    /**
     * 调用百望开票
     * @param applyInvoiceVo
     * @return
     */
    @Override
    public R<ApplyInvoiceVo> openInvoiceByInfo(ApplyInvoiceVo applyInvoiceVo) {
        R<ApplyInvoiceVo> returnRe = new R<ApplyInvoiceVo>();
        ApplyInvoiceVo reApplyInvoiceVo = new ApplyInvoiceVo();
        BaiWangEbillDto ebillDto = this.processFetchEbill(applyInvoiceVo);
        Integer isEdited = 0;
        log.info("调用百望开票 openInvoiceByInfo===start======>>>>>"+JSONObject.toJSONString(baiWangSwitch));
        if("N".equals(baiWangSwitch)){
            TbInvoiceApply tbInvoiceApply = new TbInvoiceApply();
            BeanUtil.copyProperties(applyInvoiceVo, tbInvoiceApply);
            tbInvoiceApply.setInvoiceStatus("1");
            isEdited = this.baseMapper.updateById(tbInvoiceApply);
            BeanUtil.copyProperties(tbInvoiceApply, reApplyInvoiceVo);
        }else{
            log.info("调用百望开票 openInvoiceByInfo===start======>>>>>"+JSONObject.toJSONString(ebillDto));
            R<BaiWangEbillResponseContent> result = refactorOpenBaiWangService.createEbill(ebillDto);
            log.info("调用百望开票 openInvoiceByInfo===end======>>>>>"+JSONObject.toJSONString(result));
            TbInvoiceApply tbInvoiceApply = new TbInvoiceApply();
            BeanUtil.copyProperties(applyInvoiceVo, tbInvoiceApply);
            if (null != result && R.SUCCESS == result.getCode() && null != result.getData()) {
                BaiWangEbillResponseContent responseContent = result.getData();
                if ("0000".equals(responseContent.getReturnCode())) {
                    tbInvoiceApply.setInvoiceNo(responseContent.getFP_HM());
                    tbInvoiceApply.setInvoiceCode(responseContent.getFP_DM());
                    tbInvoiceApply.setInvoiceDate(DateUtil.parseDate(responseContent.getKPRQ()));
                    tbInvoiceApply.setInvoiceStatus("2");
                } else {
                    tbInvoiceApply.setInvoiceStatus("3");
                }
            } else {
                tbInvoiceApply.setInvoiceStatus("3");
            }
            if (null != tbInvoiceApply) {
                isEdited = this.baseMapper.updateById(tbInvoiceApply);
                BeanUtil.copyProperties(tbInvoiceApply, reApplyInvoiceVo);
            }
        }
        log.info("调用百望开票 openInvoiceByInfo===start======>>>>>"+JSONObject.toJSONString(reApplyInvoiceVo));
        String orderId = reApplyInvoiceVo.getOrderId();
        Map<String,Object> detailMap = Maps.newHashMap();
        detailMap.put("invoice_apply_code", reApplyInvoiceVo.getInvoiceApplyCode());
        List<InvoiceApplyDetail> detailList = iInvoiceApplyDetailService.selectByMap(detailMap);
        log.info("编辑电子发票状态===openInvoiceByInfo===>>>>"+JSONObject.toJSONString(detailList));
        for(InvoiceApplyDetail applyInvoiceDetail:detailList){
            applyInvoiceDetail.setInvoiceStatus(reApplyInvoiceVo.getInvoiceStatus());
            Boolean detailFlag = iInvoiceApplyDetailService.updateById(applyInvoiceDetail);
            log.info("编辑电子发票状态===openInvoiceByInfo==detailFlag=>>>>"+JSONObject.toJSONString(detailFlag));
        }
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderNo(orderId);
        List<OrderVo> orderVoList = Lists.newArrayList();
        R<List<OrderVo>> orderRe = iOrderService.queryOrderList(orderDTO);
        if(null != orderRe && R.SUCCESS == orderRe.getCode()){
            orderVoList = orderRe.getData();
            log.info("编辑电子发票状态===openInvoiceByInfo==>>>>>"+JSONObject.toJSONString(orderVoList));
            if(CollUtil.isNotEmpty(orderVoList)){
                OrderVo orderVo = orderVoList.get(0);
                if(null != orderVo){
                    OrderDTO orderDto = new OrderDTO();
                    BeanUtil.copyProperties(orderVo,orderDto);
                    if(null != reApplyInvoiceVo && StrUtil.isNotBlank(reApplyInvoiceVo.getInvoiceStatus())){
                        if( "2".equals(reApplyInvoiceVo.getInvoiceStatus())) {
                            orderDto.setStatus(OrderBusiStatus.TICKET_OPENING_SUCCESS.getCateCode());
                        }else if("1".equals(reApplyInvoiceVo.getInvoiceStatus())){
                            orderDto.setStatus(OrderBusiStatus.COMPLETED.getCateCode());
                        }else{
                            orderDto.setStatus(OrderBusiStatus.TICKET_OPENING_FAIl.getCateCode());
                        }
                    }
                    iOrderService.uodateOrderStatus(orderDto);
                }
            }
        }
        if (isEdited > 0) {
            returnRe.setCode(R.SUCCESS);
            returnRe.setData(reApplyInvoiceVo);
        } else {
            returnRe.setCode(R.FAIL);
            returnRe.setMsg("开票失败");
        }
        log.info("调用百望开票 openInvoiceByInfo===end returnRe======>>>>>"+JSONObject.toJSONString(returnRe));
        return returnRe;
    }

    /**
     * 编辑纸质发票状态
     * @param applyInvoiceDto
     * @return
     */
    @Override
    @Transactional
    public R<ApplyInvoiceVo> editPaperInvoiceState(ApplyInvoiceDto applyInvoiceDto) throws Exception{
        log.info("编辑纸质发票状态====editPaperInvoiceState==>>>>"+JSONObject.toJSONString(applyInvoiceDto));
        R<ApplyInvoiceVo> result = new R<ApplyInvoiceVo>();
        ApplyInvoiceVo  invoiceVo = new ApplyInvoiceVo();
        TbInvoiceApply applyInvoice = new TbInvoiceApply();
        applyInvoice.setId(applyInvoiceDto.getId());
        TbInvoiceApply invoiceApply = this.baseMapper.selectOne(applyInvoice);
        invoiceApply.setInvoiceStatus(applyInvoiceDto.getInvoiceStatus());
        if(null != applyInvoiceDto && StrUtil.isNotBlank(applyInvoiceDto.getInvoiceStatus())
                &&  ("2".equals(applyInvoiceDto.getInvoiceStatus()) || "4".equals(applyInvoiceDto.getInvoiceStatus()))){
        	boolean  b=subCompanyAmount(invoiceApply);
        	log.info("纸质扣减开票额度返回值为:{}",b);
        }
        Integer editCount = this.baseMapper.updateById(invoiceApply);
        Map<String,Object> detailMap = Maps.newHashMap();
        detailMap.put("invoice_apply_code", invoiceApply.getInvoiceApplyCode());
        List<InvoiceApplyDetail> detailList = iInvoiceApplyDetailService.selectByMap(detailMap);
        log.info("编辑纸质发票状态===editPaperInvoiceState===>>>>"+JSONObject.toJSONString(detailList));
        for(InvoiceApplyDetail applyInvoiceDetail:detailList){
            applyInvoiceDetail.setInvoiceStatus(applyInvoiceDto.getInvoiceStatus());
            Boolean detailFlag = iInvoiceApplyDetailService.updateById(applyInvoiceDetail);
            log.info("编辑纸质发票状态===editPaperInvoiceState==detailFlag=>>>>"+JSONObject.toJSONString(detailFlag));
        }
        log.info("编辑纸质发票状态===editPaperInvoiceState==setOrderNo=>>>>"+JSONObject.toJSONString(invoiceApply.getOrderId()));
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderNo(invoiceApply.getOrderId());
        R<List<OrderVo>> orderRe = iOrderService.queryOrderList(orderDTO);
        log.info("编辑纸质发票状态===editPaperInvoiceState=orderRe=====>>>>>"+JSONObject.toJSONString(orderRe));
        List<OrderVo> orderVoList = Lists.newArrayList();
        if(null != orderRe && R.SUCCESS == orderRe.getCode()){
            orderVoList = orderRe.getData();
            log.info("编辑纸质发票状态===editPaperInvoiceState==>>>>>"+JSONObject.toJSONString(orderVoList));
            if(CollUtil.isNotEmpty(orderVoList)){
                OrderVo orderVo = orderVoList.get(0);
                if(null != orderVo){
                    OrderDTO orderDto = new OrderDTO();
                    BeanUtil.copyProperties(orderVo,orderDto);
                    if(null != applyInvoiceDto && StrUtil.isNotBlank(applyInvoiceDto.getInvoiceStatus())
                            &&  ("2".equals(applyInvoiceDto.getInvoiceStatus()) || "4".equals(applyInvoiceDto.getInvoiceStatus()))){
                        orderDto.setStatus(OrderBusiStatus.TICKET_OPENING_SUCCESS.getCateCode());
                    }else{
                        orderDto.setStatus(OrderBusiStatus.TICKET_OPENING_FAIl.getCateCode());
                    }
                    iOrderService.uodateOrderStatus(orderDto);
                }
            }
        }
        Map<String,Object> applyOrderMap=Maps.newHashMap();
        applyOrderMap.put("source_code", invoiceApply.getInvoiceApplyCode());
        applyOrderMap.put("del_flag", "0");
        applyOrderMap.put("status", "10");
        List<Order> orList=iOrderService.selectByMap(applyOrderMap);
        for (Order order : orList) {
        	order.setStatus("11");
        	iOrderService.updateById(order);
		}
        log.info("编辑纸质发票状态===editPaperInvoiceState==editCount====>>>>>"+JSONObject.toJSONString(editCount));
        if(editCount > 0 ){
            result.setCode(R.SUCCESS);
            log.info("编辑纸质发票状态===editPaperInvoiceState==invoiceApply====>>>>>"+JSONObject.toJSONString(invoiceApply));
            BeanUtil.copyProperties(invoiceApply,invoiceVo);
            result.setData(invoiceVo);
        }else{
            result.setCode(R.FAIL);
            result.setMsg("修改纸质开票状态失败");
        }
        log.info("编辑纸质发票状态===editPaperInvoiceState==result====>>>>>"+JSONObject.toJSONString(result));
        return result;
    }

    /**
     * 
     * @Title: subCompanyAmount   
     * @Description: 扣减发票抬头额度
     * @param: @param dto
     * @param: @return
     * @param: @throws Exception    
     * @author: duqiang     
     * @return: boolean      
     * @throws
     */
    public boolean subCompanyAmount(TbInvoiceApply dto) throws Exception{
    	 InvoiceAmountConfigDto configDto=new InvoiceAmountConfigDto();
         configDto.setOrgTaxNo(dto.getBuyerId());
         configDto.setCompanyId(dto.getVendorId());
         configDto.setCompanyName(dto.getVendorName());
         //configDto.setInvoiceApplyCode("ZW");
         configDto.setCouponId(dto.getCouponId());
         configDto.setCouponName(dto.getCouponName());
         configDto.setMemberId(dto.getApplyId());
         configDto.setTradeAmount(dto.getTaxIncludedPrice());
         configDto.setBuyType("1");//立即开票
         configDto.setInvoiceApplyCode(dto.getInvoiceApplyCode());
         R<Boolean> configR=fundInvoiceAmountConfigService.invoiceAmountCheck(configDto);
         log.info("添加发票抬头开票记录返回值:{}",JSON.toJSONString(configR));
	     if(null == configR || configR.getCode() != R.SUCCESS) {
	    	log.info("添加发票抬头开票记录失败");
			throw new BaoxiaoException("开票失败");
	     }
	     return true;
    }
    /**
     *  编辑稍后开票
     * @param applyInvoiceDto
     * @return
     */
    @Override
    public R<ApplyInvoiceVo> editInvoiceAfterApply(ApplyInvoiceDto applyInvoiceDto) {
        R<ApplyInvoiceVo> result = new R<ApplyInvoiceVo>();
        ApplyInvoiceVo  invoiceVo = new ApplyInvoiceVo();
        TbInvoiceApply applyInvoice = new TbInvoiceApply();
        applyInvoice.setId(applyInvoiceDto.getId());
        TbInvoiceApply invoiceApply = this.baseMapper.selectOne(applyInvoice);
        invoiceApply.setInvoiceStatus(applyInvoiceDto.getInvoiceStatus());
        Integer editCount = this.baseMapper.updateById(invoiceApply);
        Map<String,Object> detailMap = Maps.newHashMap();
        detailMap.put("invoice_apply_code", invoiceApply.getInvoiceApplyCode());
        List<InvoiceApplyDetail> detailList=iInvoiceApplyDetailService.selectByMap(detailMap);
        for(InvoiceApplyDetail applyInvoiceDetail:detailList){
            applyInvoiceDetail.setInvoiceStatus(applyInvoiceDto.getInvoiceStatus());
            Boolean detailFlag = iInvoiceApplyDetailService.updateById(applyInvoiceDetail);
        }
        if(editCount > 0 ){
            result.setCode(R.SUCCESS);
            BeanUtil.copyProperties(invoiceApply,invoiceVo);
            result.setData(invoiceVo);
        }else{
            result.setCode(R.FAIL);
            result.setMsg("修改稍后开票状态失败");
        }
        return result;
    }

    /**
     * 查询券已开票金额
     * @param applyInvoiceDto
     * @return
     */
    @Override
    public List<ApplyInvoiceVo> countHadOpenInvoiceAmountByCoupon(ApplyInvoiceDto applyInvoiceDto){
        List<ApplyInvoiceVo> applyInvoiceVoList = tbInvoiceApplyMapper.countHadOpenInvoiceAmountByCoupon(applyInvoiceDto);
        return applyInvoiceVoList;
    }

    /**
     * 查询订单与发票申请关系
     * @param invoiceApplyCode
     * @return
     */
    @Override
    public List<OrderApplyInvoiceVo> findOrderApplyInvoiceList(String invoiceApplyCode) {
        List<OrderApplyInvoiceVo> invoiceVos = Lists.newArrayList();
        EntityWrapper<TbOrderInvoiceApply> entityWrapper =  new EntityWrapper<TbOrderInvoiceApply>();
        entityWrapper.eq("invoice_apply_code",invoiceApplyCode);
        List<TbOrderInvoiceApply> orderInvoiceApplyList = tbOrderInvoiceApplyMapper.selectList(entityWrapper);
        if(CollUtil.isNotEmpty(orderInvoiceApplyList)){
            orderInvoiceApplyList.forEach(item->{
                OrderApplyInvoiceVo invoiceVo = new OrderApplyInvoiceVo();
                BeanUtil.copyProperties(item,invoiceVo);
                invoiceVos.add(invoiceVo);
            });
        }
        return invoiceVos;
    }
    
    
    /**
     * 
     * @Title: billInvoice   
     * @Description: 个人借款开票 
     * @param: @param applyInvoiceDto
     * @param: @return    
     * @author: duqiang     
     * @return: R<ApplyInvoiceVo>      
     * @throws
     */
    @Override
    @Transactional
    public R<ApplyInvoiceVo> billInvoice(ApplyInvoiceDto applyInvoiceDto) throws Exception{
    	ApplyInvoiceVo rtnVo=new ApplyInvoiceVo();
    	//电子开票，直接调用百望开票，纸质，线下开票，初始化为待开票
        log.info("个人借款开票billInvoice"+JSONObject.toJSONString(applyInvoiceDto));
		R<ApplyInvoiceVo> result = new R<ApplyInvoiceVo>();
        String invoiceStatus=CommonConstant.STATUS_YES;
		applyInvoiceDto.setApplyDate(new Date());
		//电子发票、纸质发票且运费为0 直接为开票成功
		if(applyInvoiceDto.getTypeId().equals(InvoiceTypeNew.E_INVOICE.getCateCode()) 
				|| ( (applyInvoiceDto.getTypeId().equals(InvoiceTypeNew.VAT_INVOICE.getCateCode()) 
						|| applyInvoiceDto.getTypeId().equals(InvoiceTypeNew.VAT_SPECIAL_INVOICE.getCateCode())) 
						&& (applyInvoiceDto.getExpressFee() == null || applyInvoiceDto.getExpressFee().doubleValue() == 0 ))) {
			invoiceStatus=CommonConstant.BILL_ITEM_STATUS_PAYFULL;//已开票
		}
		applyInvoiceDto.setInvoiceAmt(applyInvoiceDto.getTaxIncludedPrice().divide(applyInvoiceDto.getRate().add(new BigDecimal(10000)),2,BigDecimal.ROUND_HALF_DOWN).divide(new BigDecimal(10000),2,BigDecimal.ROUND_HALF_DOWN));
		applyInvoiceDto.setInvoiceStatus(invoiceStatus);
		applyInvoiceDto.setCreator(applyInvoiceDto.getApplyId());
		applyInvoiceDto.setUpdator(applyInvoiceDto.getApplyId());
        PlatformVo memberVo=iRefactorMemberPlatService.queryByMemberId(applyInvoiceDto.getApplyId(),null);
		if(!ObjectUtils.isEmpty(memberVo)) {
			applyInvoiceDto.setApplyName(memberVo.getRealName());
		}
		// 设置销售方  也就是消费券的开票商户
        MemberInvoiceComRuleDto invoiceComRuleDto = new MemberInvoiceComRuleDto();
        MemberInvoiceComRuleVo invoiceComRuleVo = null;
        invoiceComRuleDto.setInvoiceIndustryCode(applyInvoiceDto.getItemCode());
        invoiceComRuleDto.setInvoiceCategoryCode(applyInvoiceDto.getSubItemCode());
        invoiceComRuleDto.setTaxRate(applyInvoiceDto.getRate());
        invoiceComRuleDto.setInvoiceTypeCode(applyInvoiceDto.getTypeId());
        R<MemberInvoiceComRuleVo> invoiceComRe = iRefactorMemberComInvoiceApi.findOpenCompanyBySubjectAndRate(invoiceComRuleDto);
        log.info("通过科目信息查询开票商户返回为："+JSON.toJSONString(invoiceComRe));
        if(null != invoiceComRe && R.SUCCESS == invoiceComRe.getCode()){
            invoiceComRuleVo = invoiceComRe.getData();
        }else {
        	throw new BaoxiaoException("通过科目查询开票商户错误");
        }
        if(null != invoiceComRuleVo){
            //VendorVo vendorVo = iRefactorVendorService.queryVendorDetail(invoiceComRuleVo.getVendorId());
            log.info("通过商户id查询商户信息为："+JSON.toJSONString(invoiceComRuleVo));
            if(ObjectUtils.isEmpty(invoiceComRuleVo)) {
            	throw new BaoxiaoException("没有查询到开票商户信息");
            }
            applyInvoiceDto.setSalesId(invoiceComRuleVo.getId());
            applyInvoiceDto.setSalesName(invoiceComRuleVo.getCompanyName());
            applyInvoiceDto.setSalesAddr(invoiceComRuleVo.getAddress());
            applyInvoiceDto.setSalesNumber(invoiceComRuleVo.getIdentificationNumber());
            if(StrUtil.isNotBlank(invoiceComRuleVo.getSubbranchBankName())){
                applyInvoiceDto.setSalesBankaccount(invoiceComRuleVo.getSubbranchBankName());
            }
        }
        log.info("设置销售方  也就是消费券的开票商户为："+JSON.toJSONString(applyInvoiceDto));
        //设置购买方 暂由前端传值
		TbInvoiceApply tbInvoiceApply = new TbInvoiceApply();
		BeanUtil.copyProperties(applyInvoiceDto, tbInvoiceApply);
        if(null != applyInvoiceDto &&  StrUtil.isNotBlank(applyInvoiceDto.getTypeId()) ){
            if(DictionaryEnum.InvoiceTypeNew.VAT_INVOICE.getCateCode().equals(applyInvoiceDto.getTypeId())){
                tbInvoiceApply.setTypeName(DictionaryEnum.InvoiceTypeNew.VAT_INVOICE.getCateName());
            }else if(DictionaryEnum.InvoiceTypeNew.VAT_SPECIAL_INVOICE.getCateCode().equals(applyInvoiceDto.getTypeId())){
                tbInvoiceApply.setTypeName(DictionaryEnum.InvoiceTypeNew.VAT_SPECIAL_INVOICE.getCateName());
            }else{
                tbInvoiceApply.setTypeName(DictionaryEnum.InvoiceTypeNew.E_INVOICE.getCateName());
            }
        }
		String code = CodeUtils.genneratorShort(Sequence.KP.getPerfix());
		tbInvoiceApply.setInvoiceApplyCode(code);
		log.info("设置购买方 暂由前端传值为："+JSON.toJSONString(tbInvoiceApply));
		// 金额拆分
		List<BigDecimal> bigList = amountSplit(applyInvoiceDto.getTaxIncludedPrice());
		log.info("金额拆分====》》》》》"+JSONObject.toJSONString(bigList));
		if (null != bigList && bigList.size() > 0) {
			// 添加订单主表参数组装
			
			OrderDTO orderDto = addOrder(applyInvoiceDto);
			tbInvoiceApply.setOrderId(orderDto.getOrderNo());
            this.baseMapper.insert(tbInvoiceApply);
           /* rtnVo.setId(tbInvoiceApply.getId());
            rtnVo.setInvoiceApplyCode(tbInvoiceApply.getInvoiceApplyCode());*/
            log.info("tbInvoiceApply===insert=》》》》》"+JSONObject.toJSONString(tbInvoiceApply));
			List<OrderDetailDTO> detailDtoList = Lists.newArrayList();
			for (BigDecimal bigDecimal : bigList) {
				InvoiceApplyDetail applyDetail = new InvoiceApplyDetail();
				BeanUtil.copyProperties(tbInvoiceApply, applyDetail,"id");
				applyDetail.setTaxIncludedPrice(bigDecimal);
				BigDecimal invoiceAmount=bigDecimal.multiply(new BigDecimal(10000)).divide(applyInvoiceDto.getRate().add(new BigDecimal(10000)),2,BigDecimal.ROUND_HALF_UP);
				applyDetail.setInvoiceAmt(invoiceAmount);
				//电子发票才去调用百望
				if(applyInvoiceDto.getTypeId().equals(InvoiceTypeNew.E_INVOICE.getCateCode())) {
					//开票
					ApplyInvoiceVo startInvoice=new ApplyInvoiceVo();
					BeanUtil.copyProperties(applyDetail, startInvoice);
					applyDetail=bwInvoice(startInvoice, applyDetail);
				}
				
		        // 金额拆分之后添加开票申请明细
				invoiceApplyDetailMapper.insert(applyDetail);
			}
			
			OrderDetailDTO detailDto = addOrderDetail(tbInvoiceApply);
			detailDtoList.add(detailDto);
			orderDto.setDetailDtoList(detailDtoList);
			log.info("添加所有订单明细入参参数为:{}", JSON.toJSONString(orderDto));
			iOrderService.addOrder(orderDto);
		} else {
			throw new BaoxiaoException("开票总金额有误");
		}
		if (null != applyInvoiceDto && CollUtil.isNotEmpty(applyInvoiceDto.getOrderIds())) {
			applyInvoiceDto.getOrderIds().forEach(item -> {
				TbOrderInvoiceApply orderInvoiceApply = new TbOrderInvoiceApply();
				orderInvoiceApply.setOrderPaymentNo(item);
				orderInvoiceApply.setInvoiceApplyCode(code);
                orderInvoiceApply.setCreator(applyInvoiceDto.getApplyId());
                orderInvoiceApply.setCreatedTime(new Date());
                orderInvoiceApply.setUpdator(applyInvoiceDto.getApplyId());
                orderInvoiceApply.setUpdatedTime(new Date());
				tbOrderInvoiceApplyMapper.insert(orderInvoiceApply);
			});
		}
	
		
		result.setCode(R.SUCCESS);
		ApplyInvoiceVo applyInvoiceVo = new ApplyInvoiceVo();
		log.info("addInvoiceApply=====>>>>>>"+JSONObject.toJSONString(tbInvoiceApply));
		BeanUtil.copyProperties(tbInvoiceApply, applyInvoiceVo);
		result.setData(applyInvoiceVo);
		return result ;
    }
    
    
    public  InvoiceApplyDetail  bwInvoice(ApplyInvoiceVo  startInvoice,InvoiceApplyDetail applyDetail) throws Exception{
    	BaiWangEbillDto ebillDto = this.assemInvoice(startInvoice);
        log.info("组装百望接口参数为:{}",JSON.toJSON(ebillDto));
        R<BaiWangEbillResponseContent> baiwRe = refactorOpenBaiWangService.createEbill(ebillDto);
        log.info("调用百望接口返回为:{}",JSON.toJSON(baiwRe));
        if (null != baiwRe && R.SUCCESS == baiwRe.getCode() && null != baiwRe.getData()) {
        BaiWangEbillResponseContent responseContent = baiwRe.getData();
             //if ("0000".equals(responseContent.getReturnCode())) {
            	 applyDetail.setInvoiceNo(responseContent.getFP_HM());
            	 applyDetail.setInvoiceCode(responseContent.getFP_DM());
            	 applyDetail.setCheckCode(responseContent.getJYM());
            	 applyDetail.setInvoiceDate(DateUtil.parse(responseContent.getKPRQ()));
            	 applyDetail.setInvoiceStatus(InvoiceApplyStatus.INVOICE_SUCCESS.getCateCode());
            	 applyDetail.setPicUrl(responseContent.getPDF_URL());
            	 applyDetail.setMinPicUrl(responseContent.getSP_URL());
            	  //Boolean flag = false;
                /* SmsDto smsDto = new SmsDto();
                 smsDto.setSmsCode("SMS0020");
                 smsDto.setChannel(DictionaryEnum.SmsChannelType.SMS_CHANNEL_TYPE_JF.getCateCode());
                 smsDto.setType(DictionaryEnum.SmsType.SMS_TYPE_YY.getCateCode());
                 Map<String,Object> map = Maps.newHashMap();
                 if(null != startInvoice){
                     smsDto.setPhone(startInvoice.getReceivingPhone());
                     map.put("url",responseContent.getPDF_URL());
                 }
                 smsDto.setMsg(map);
                 R<Boolean> rBool = refactorOpenCompanyInServiceApi.smsVerification(smsDto);
                 log.info("开票发送短信返回结果:{}",JSON.toJSONString(rBool));*/
            /* } else {
            	 applyDetail.setInvoiceStatus(PaymentStatus.PAYMENT_FAIL.getCateCode());
            	  throw new BaoxiaoException("开票失败");
             }*/
        } else {
        	applyDetail.setInvoiceStatus(PaymentStatus.PAYMENT_FAIL.getCateCode());
        	throw new BaoxiaoException("开票失败");
        }
        return applyDetail;
    }
}



