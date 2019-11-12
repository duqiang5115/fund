package com.taolue.baoxiao.fund.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.MqQueueConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillPaymentFlow;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.IntelligentReimburseFlow;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.InvoiceApplyStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.InvoiceTypeNew;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.LZVendorInfo;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderBusiStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderDetailStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.SmsChannelType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TransType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.billSettleAccountsStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.paymentType;
import com.taolue.baoxiao.common.dto.AssignCouponDto;
import com.taolue.baoxiao.common.dto.AssignDto;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.UserUtils;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.coupon.IRefactorCouponRelationService;
import com.taolue.baoxiao.fund.api.coupon.RefactorCouponService;
import com.taolue.baoxiao.fund.api.coupon.RefactorMemberCouponRateRuleListSercice;
import com.taolue.baoxiao.fund.api.dock.IOpenJiaBaiTiaoBuyCouponService;
import com.taolue.baoxiao.fund.api.dto.ApplyInvoiceDetailDto;
import com.taolue.baoxiao.fund.api.dto.ApplyInvoiceDto;
import com.taolue.baoxiao.fund.api.dto.BillBaseNewDto;
import com.taolue.baoxiao.fund.api.dto.BillItemNewDto;
import com.taolue.baoxiao.fund.api.dto.BillSettleAccountsDto;
import com.taolue.baoxiao.fund.api.dto.FundBillInvoiceSettingDto;
import com.taolue.baoxiao.fund.api.dto.FundCouponTaxCodeDto;
import com.taolue.baoxiao.fund.api.dto.MakeMoneySendSmsDto;
import com.taolue.baoxiao.fund.api.dto.OrderCouponSceneDto;
import com.taolue.baoxiao.fund.api.dto.OrderDTO;
import com.taolue.baoxiao.fund.api.dto.OrderDetailDTO;
import com.taolue.baoxiao.fund.api.invoice.IRefactorInvoiceReimburseService;
import com.taolue.baoxiao.fund.api.invoice.IRefactorInvoiceServiceRateConfigService;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberComInvoiceApi;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberCompanyService;
import com.taolue.baoxiao.fund.api.member.RefactorMemberCouponRateService;
import com.taolue.baoxiao.fund.api.member.RefactorMemberPlatformService;
import com.taolue.baoxiao.fund.api.openplatform.IDockOpenPlatformService;
import com.taolue.baoxiao.fund.api.vo.ApplyInvoiceDetailVo;
import com.taolue.baoxiao.fund.api.vo.ApplyInvoiceVo;
import com.taolue.baoxiao.fund.api.vo.BillBaseNewVo;
import com.taolue.baoxiao.fund.api.vo.BillItemNewVo;
import com.taolue.baoxiao.fund.api.vo.FundBillInvoiceSettingVo;
import com.taolue.baoxiao.fund.api.vo.FundCouponTaxCodeVo;
import com.taolue.baoxiao.fund.api.vo.FundFlowRecordVo;
import com.taolue.baoxiao.fund.api.vo.FundIntelligentReimburseRecordVo;
import com.taolue.baoxiao.fund.api.vo.OrderCouponSceneVo;
import com.taolue.baoxiao.fund.entity.BillBaseNew;
import com.taolue.baoxiao.fund.entity.BillItemNew;
import com.taolue.baoxiao.fund.entity.BillSettleAccounts;
import com.taolue.baoxiao.fund.entity.FundBillInvoiceSetting;
import com.taolue.baoxiao.fund.entity.FundCouponTaxCode;
import com.taolue.baoxiao.fund.entity.InvoiceApplyDetail;
import com.taolue.baoxiao.fund.entity.Order;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.mapper.BillBaseNewMapper;
import com.taolue.baoxiao.fund.mapper.FundBillInvoiceSettingMapper;
import com.taolue.baoxiao.fund.mapper.FundCouponTaxCodeMapper;
import com.taolue.baoxiao.fund.mapper.InvoiceApplyDetailMapper;
import com.taolue.baoxiao.fund.mapper.OrderCouponSceneMapper;
import com.taolue.baoxiao.fund.mapper.OrderMapper;
import com.taolue.baoxiao.fund.mapper.TbFundTradeFlowMapper;
import com.taolue.baoxiao.fund.service.IApplyInvoiceService;
import com.taolue.baoxiao.fund.service.IBillBaseNewService;
import com.taolue.baoxiao.fund.service.IBillItemNewService;
import com.taolue.baoxiao.fund.service.IBillSettleAccountsService;
import com.taolue.baoxiao.fund.service.IFundFlowRecordService;
import com.taolue.baoxiao.fund.service.IInvoiceApplyDetailService;
import com.taolue.baoxiao.fund.service.IOrderService;
import com.taolue.coupon.api.dto.CouponRelationDto;
import com.taolue.coupon.api.vo.CouponRelationVo;
import com.taolue.coupon.api.vo.CouponVo;
import com.taolue.dict.api.service.ICommonDictApi;
import com.taolue.dock.api.common.DockEnum;
import com.taolue.dock.api.common.DockR;
import com.taolue.dock.api.dto.AccountAssignDto;
import com.taolue.dock.api.dto.SmsDto;
import com.taolue.dock.api.vo.AssignItemVo;
import com.taolue.dock.api.vo.DockChargebackRecordVo;
import com.taolue.dock.api.vo.JiaBaiTiaoReturnVo;
import com.taolue.dock.api.vo.TransferOrderVo;
import com.taolue.dock.api.vo.UserVo;
import com.taolue.invoice.api.common.ConstantInvoice;
import com.taolue.invoice.api.dto.InvoiceServiceRateConfigDto;
import com.taolue.invoice.api.dto.ReimburseDTO;
import com.taolue.invoice.api.vo.InvoiceServiceRateConfigVo;
import com.taolue.member.api.common.constant.enums.ResultEnum;
import com.taolue.member.api.dto.MemberInvoiceComRuleDto;
import com.taolue.member.api.dto.QueryCouponRateRuleDto;
import com.taolue.member.api.vo.MemberCompanyVo;
import com.taolue.member.api.vo.MemberCouponRateRuleListVo;
import com.taolue.member.api.vo.MemberInvoiceComRuleVo;
import com.taolue.member.api.vo.PlatformVo;
import com.xiaoleilu.hutool.collection.CollUtil;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;

/**
 * <p>
 * 账单主表，记录账单基础信息
 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-06-13
 */
@Service
public class BillBaseNewServiceImpl extends ServiceImpl<BillBaseNewMapper, BillBaseNew> implements IBillBaseNewService {
	private Log logger = LogFactory.getLog(BillBaseNewServiceImpl.class);
	
	@Autowired
	private IBillSettleAccountsService billSettleAccountsService;

	
	
	@Autowired
	private OrderCouponSceneMapper orderCouponSceneMapper;
	
	@Autowired
	private RefactorMemberCouponRateService refactorMemberCouponRateService;
	
	@Autowired
	private FundCouponTaxCodeMapper fundCouponTaxCodeMapper;
	
	@Autowired
	private IBillItemNewService billItemNewService;
	
	@Autowired
	private FundBillInvoiceSettingMapper fundBillInvoiceSettingMapper;
	
	@Autowired
	private IRefactorMemberComInvoiceApi refactorMemberComInvoiceApi;
	
    @Autowired
    private IOrderService iOrderService;
    
    @Autowired
    private IDockOpenPlatformService dockOpenPlatformService;
    
    @Autowired
    private IFundFlowRecordService fundFlowRecordService;
    

    @Autowired
    private ICommonDictApi commonDictApi;
    @Autowired
    private InvoiceApplyDetailMapper invoiceApplyDetailMapper;
    @Autowired
	private OrderMapper  orderMapper;
    @Autowired
    private IRefactorInvoiceReimburseService refactorInvoiceReimburseService;
    @Autowired
    private TbFundTradeFlowMapper fundTradeFlowMapper;
    @Autowired
    private IApplyInvoiceService applyInvoiceService;
    @Autowired
    private IRefactorMemberCompanyService refactorMemberCompanyService;

    @Autowired
    private IRefactorInvoiceServiceRateConfigService invoiceServiceRateConfigService;
    
    @Autowired
    private IInvoiceApplyDetailService invoiceApplyDetailService;
    
    @Autowired
    private RefactorMemberPlatformService refactorMemberPlatformService;
    
    @Autowired
    private IOpenJiaBaiTiaoBuyCouponService openJiaBaiTiaoBuyCouponService;
    
    @Autowired
	public  RabbitTemplate rabbitTemplate;
    @Autowired
	private IRefactorCouponRelationService refactorCouponRelationService;
    @Autowired
	private RefactorCouponService tbCouponService;
    @Autowired
	private RefactorMemberCouponRateRuleListSercice refactorMemberCouponRateRuleListSercice;

	@Override
	@Transactional
	public R<Boolean> createBill(BillBaseNewDto dto) throws Exception{
		//入参 companyId，companyName,settleList,beginTime,endTime
		R<Boolean> r=new R<Boolean>();
		try {
			String userName=UserUtils.getUser();
			BigDecimal reimburseAmount=new BigDecimal(0);
			BigDecimal billAmount=new BigDecimal(0);
			BigDecimal buyAmount=new BigDecimal(0);
			BigDecimal paymentAmount=new BigDecimal(0);
			//账单名称
			//String billName=sdf.format(dto.getBeginTime())+"-"+sdf.format(dto.getEndTime())+"账单";
			String billName=getBillName();
			BillSettleAccountsDto setDto=new BillSettleAccountsDto();
			setDto.setSettleNoList(dto.getSettleNoList());
			List<BillSettleAccountsDto> accountDtoList=billSettleAccountsService.findSettlementAmountByNo(setDto);
			logger.info("通过结算单号查询结算总金额返回值:"+JSON.toJSONString(accountDtoList));
			if(null==accountDtoList || accountDtoList.size()<=0) {
				logger.info("通过结算单号查询结算总金额为空");
				throw new BaoxiaoException("通过结算单号查询结算总金额为空");
			}
			List<BillSettleAccountsDto> xfList=Lists.newArrayList();//现金消费
			List<BillSettleAccountsDto> buyList=Lists.newArrayList();//购买
			for (BillSettleAccountsDto billSettleAccountsDto : accountDtoList) {
				billAmount=billAmount.add(billSettleAccountsDto.getSettleAmount());
				if("1".equals(billSettleAccountsDto.getSettleType())) {//简易报销
					reimburseAmount=reimburseAmount.add(billSettleAccountsDto.getSettleAmount());
				}else if("2".equals(billSettleAccountsDto.getSettleType())) {//现金消费
					paymentAmount=paymentAmount.add(billSettleAccountsDto.getSettleAmount());
					xfList.add(billSettleAccountsDto);
				}else if("3".equals(billSettleAccountsDto.getSettleType())) {//购买
					buyAmount=buyAmount.add(billSettleAccountsDto.getSettleAmount());
					buyList.add(billSettleAccountsDto);
				}
			}
			String billNo=CodeUtils.genneratorShort("ZD");
			BigDecimal invoiceServiceAmount=new BigDecimal(0);
			BigDecimal settleServiceAmount=new BigDecimal(0);
			if(xfList.size()>0 || buyList.size()>0) {
				//服务费跟添加账单明细
				Map<String,BigDecimal> serviceMap=getSettleServiceAmount(xfList,buyList, dto.getCompanyId(),billNo,userName);
				logger.info("计算服务费跟添加账单明细返回值为:"+JSON.toJSONString(serviceMap));
				if(null==serviceMap || serviceMap.size()<=0) {
					throw new BaoxiaoException("计算服务费跟添加账单明细有误");
				}
				invoiceServiceAmount=serviceMap.get("invoiceServiceAmount");
				settleServiceAmount=serviceMap.get("settleServiceAmount");
			}
			

			BigDecimal reimburseTotalAmount=reimburseAmount.add(invoiceServiceAmount).add(settleServiceAmount);
			BigDecimal serviceAmount=invoiceServiceAmount.add(settleServiceAmount);//总服务费金额
			billAmount=billAmount.add(serviceAmount);
			logger.info("砾洲商户的需要打款金额为:"+reimburseTotalAmount+",serviceAmount"+serviceAmount);
			BillBaseNew base=new BillBaseNew();
			base.setStatus(BillStatus.UN_PAID.getCateCode());
			base.setBillNo(billNo);
			base.setBillName(billName);
			base.setCompanyId(dto.getCompanyId());
			base.setCompanyName(dto.getCompanyName());
			base.setBeginTime(new Date());
			base.setEndTime(new Date());
			base.setLatestTime(new Date());
			base.setBillReimburseAmount(reimburseAmount);
			base.setBillAmount(billAmount);
			base.setBiilBuyAmount(buyAmount);
			base.setBillPaymentAmount(paymentAmount);
			base.setCreator(userName);
			base.setUpdator(userName);
			base.setBiilInvoiceServiceAmount(invoiceServiceAmount);
			base.setBiilSettleServiceAmount(settleServiceAmount);
			logger.info("添加主张单的参数："+JSON.toJSONString(base));
			this.baseMapper.insert(base);
			//添加返现订单
			/*if(base.getBiilInvoiceServiceAmount().compareTo(new BigDecimal(0))==1) {
				addOrder(base);
			}*/
			
			//当报销金额跟服务金额大于0才去添加砾洲的商户账单明细
			if(reimburseAmount.compareTo(new BigDecimal(0))==1) {
				//添加砾洲的商户账单明细
				BillItemNew item=new BillItemNew();
				item.setBillNo(billNo);
				item.setBillItemNo(CodeUtils.genneratorShort("ZDMX"));
				item.setBillItemKind(BillItemSubCate.BILL_ITEM_VENDOR_AMOUNT.getCateCode());
				item.setBillItemKindName(BillItemSubCate.BILL_ITEM_VENDOR_AMOUNT.getCateName());
				item.setInternalVendorId(LZVendorInfo.LZ_VENDOR_INFO.getCateCode());
				item.setInternalVendorName(LZVendorInfo.LZ_VENDOR_INFO.getCateName());
				item.setBillItemCate(LZVendorInfo.LZ_VENDOR_INFO.getCateMgn());
				item.setBillItemCateName(LZVendorInfo.LZ_VENDOR_INFO.getCateAccount());
				item.setBillAmount(reimburseAmount); 
				item.setBillLeftAmount(reimburseAmount); 
				item.setBillPayAmount(new BigDecimal(0));
				item.setStatus("3");//待打款
				item.setCreator(userName);
				item.setUpdator(userName);
				logger.info("添加砾洲账单明细入参参数:"+JSON.toJSONString(item));
				billItemNewService.insert(item);
			}
		/*	//当服务费金额大于0才去添加砾洲开票配置
			if(serviceAmount.compareTo(new BigDecimal(0))==1) {
				//添加砾洲的账单开票配置
				//砾洲的配置信息
				FundCouponTaxCode taxCode=fundCouponTaxCodeMapper.selectById("1073058372583604235");
				logger.info("查询砾洲的配置信息返回为:"+JSON.toJSONString(taxCode));
				FundBillInvoiceSetting invoiceSet=new FundBillInvoiceSetting();
				invoiceSet.setBillNo(billNo);
				//invoiceSet.setCouponId();
				invoiceSet.setMainItemCode(taxCode.getMainItemCode());
				invoiceSet.setMainItemName(taxCode.getMainItemName());
				invoiceSet.setSubItemCode(taxCode.getSubItemCode());
				invoiceSet.setSubItemName(taxCode.getSubItemName());
				invoiceSet.setInvoiceAmount(serviceAmount);
				invoiceSet.setInvoiceForm("1");
				invoiceSet.setInvoiceItemContent("*"+taxCode.getMainItemName()+"*"+taxCode.getSubItemName()+" (税率"+taxCode.getTaxPoint().divide(new BigDecimal(100))+"%)");
				invoiceSet.setInvoiceStatus("1");
				invoiceSet.setInvoiceType("0");
				invoiceSet.setVendorId(taxCode.getVendorId());
				invoiceSet.setVendorName(taxCode.getVendorName());
				invoiceSet.setUpdator(userName);
				invoiceSet.setCreator(userName);
				logger.info("添加砾洲账单开票配置入参参数:"+JSON.toJSONString(invoiceSet));
				fundBillInvoiceSettingMapper.insert(invoiceSet);
			}*/
		
			//批量修改结算单状态
			BillSettleAccountsDto settleDto=new BillSettleAccountsDto();
			settleDto.setSettleNoList(dto.getSettleNoList());
			settleDto.setStatus(billSettleAccountsStatus.ALREADY_ORDER.getId());
			settleDto.setBillNumber(billNo);
			billSettleAccountsService.editStatusByNos(settleDto);
			
		
		} catch (Exception e) {
			logger.error("生成账单系统错误",e);
			r.setCode(R.FAIL);
			r.setData(false);
			r.setMsg("生成账单系统错误");
		}
		return r;
	}
	
	public String getBillName() throws Exception{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
		String billName=sdf.format(new Date())+"生成的账单";
		BillBaseNewDto newDto=new BillBaseNewDto();
		newDto.setDateStr(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		BillBaseNewVo billVo=this.baseMapper.queryEndNameByDay(newDto);
		logger.info("根据当前日期查询出最后一次生成的账单名称为："+JSON.toJSONString(billVo));
		String rtnBillName="";
		String qBillName="";
		if(!ObjectUtils.isEmpty(billVo)) {
			qBillName=billVo.getBillName();
		}
		if(StringUtils.isBlank(qBillName)) {
			rtnBillName=billName;
		}else if(qBillName.contains(billName) && billName.length()<qBillName.length()) {
			String subStr=qBillName.replace(billName+"_", "");
			logger.info("billName对比的索引为："+subStr);
			rtnBillName=billName+"_"+(Integer.parseInt(subStr)+1);
		}else if(billName.length() == qBillName.length()){
			rtnBillName=billName+"_1";
		}else {
			rtnBillName=billName;
		}
		logger.info("最终的出来的账单名称为："+rtnBillName);
		return rtnBillName;
	}
	/**
	 * 
	 * @Title: getSettleService   
	 * @Description: 结算服务费用
	 * @param: @param accountDtoList
	 * @param: @param companyId
	 * @param: @return
	 * @param: @throws Exception    
	 * @author: duqiang     
	 * @return: BigDecimal      
	 * @throws
	 */
	public Map<String,BigDecimal> getSettleServiceAmount(List<BillSettleAccountsDto> accountDtoList ,List<BillSettleAccountsDto> buyList,String companyId,String billNo,String userName) throws Exception{
		//企业设置的消费券购买服务费率比平台标准服务费率高，若费率相等则没有超额返佣，不同券的费率不同
		logger.info("开始计算服务费用>>》》》》》》》》》》》》》》》》");
		Map<String,BigDecimal> rtnMap=Maps.newHashMap();
		//List<BillItemNew> itemList=Lists.newArrayList();
		BigDecimal settleServiceAmount=new BigDecimal(0);//结算服务金额
		BigDecimal invoiceServiceAmount=new BigDecimal(0);//发票服务费
		List<OrderCouponSceneVo> sceneList=Lists.newArrayList();
		List<String> sceneCodeList = accountDtoList.stream().map(BillSettleAccountsDto::getBillService)
				.collect(Collectors.toList());
		if(null!=sceneCodeList && sceneCodeList.size()>0) {
			OrderCouponSceneDto sceneDto=new OrderCouponSceneDto();
			sceneDto.setSceneCodeList(sceneCodeList);
			sceneList=orderCouponSceneMapper.findCouponIdBySceneNo(sceneDto);
			logger.info("通过结算单的场景code获取消费券id返回值："+JSON.toJSONString(sceneList));
			if(null==sceneList || sceneList.size()<=0) {
				throw new BaoxiaoException("通过场景code没有查询到券id");
			}
		}

		Map<String,BigDecimal> couponAmountMap=Maps.newHashMap();
		Map<String,BigDecimal> settleAmountMap=Maps.newHashMap();
		Map<String,BigDecimal> vendorAmountMap=Maps.newConcurrentMap();
		Map<String,BigDecimal> vendorMap=Maps.newConcurrentMap();//企业商户对应的费率map
		Map<String,BigDecimal> vendorSysMap=Maps.newConcurrentMap();//平台商户对应的费率map
		//Map<String,FundCouponTaxCodeVo> accountMap=Maps.newHashMap();
		//吧结算单的场景跟金额放到map中
		for (BillSettleAccountsDto dto : accountDtoList) {
			invoiceServiceAmount=invoiceServiceAmount.add(dto.getCashBackAmount());
			if(!ObjectUtils.isEmpty(settleAmountMap.get(dto.getBillService()))) {
				settleAmountMap.put(dto.getBillService(), settleAmountMap.get(dto.getBillService()).add(dto.getSettleAmount()));
			}else {
				settleAmountMap.put(dto.getBillService(), dto.getSettleAmount());
			}
			
		}
		logger.info("结算单场景对应的金额map值："+JSON.toJSONString(settleAmountMap));
		//购买结算单查询购买的消费券
		for (BillSettleAccountsDto sellte : buyList) {
			if(!ObjectUtils.isEmpty(couponAmountMap.get(sellte.getBillService()))) {
				couponAmountMap.put(sellte.getBillService(), couponAmountMap.get(sellte.getBillService()).add(sellte.getSettleAmount()));
			}else {
				couponAmountMap.put(sellte.getBillService(),sellte.getSettleAmount());
			}
		}
		
		logger.info("购买消费券对应的金额值："+JSON.toJSONString(couponAmountMap));
		//把场景对应的券放到map中
		for (OrderCouponSceneVo orderCouponSceneVo2 : sceneList) {
			if(!ObjectUtils.isEmpty(couponAmountMap.get(orderCouponSceneVo2.getCouponId()))) {
				couponAmountMap.put(orderCouponSceneVo2.getCouponId(), couponAmountMap.get(orderCouponSceneVo2.getCouponId()).add(settleAmountMap.get(orderCouponSceneVo2.getSceneCode())));
			}else {
				couponAmountMap.put(orderCouponSceneVo2.getCouponId(), settleAmountMap.get(orderCouponSceneVo2.getSceneCode()));
			}
		}
		logger.info("场景对应的券的金额map值："+JSON.toJSONString(couponAmountMap));
		
	
		//吧券map的券id转成List
		List<String> couponIdList = new ArrayList<String>(couponAmountMap.keySet());
		
		BigDecimal invoiceAmount=new BigDecimal(0);
		BigDecimal serviceRate=new BigDecimal(0);
		InvoiceServiceRateConfigDto rateConfigDto=new InvoiceServiceRateConfigDto();
		rateConfigDto.setCompanyId(companyId);
		rateConfigDto.setIsPlatform("1");
		rateConfigDto.setIsSet("1");
		R<InvoiceServiceRateConfigVo> rateConfig=invoiceServiceRateConfigService.findInvoiceServiceRateConfigByParam(rateConfigDto);
		logger.info("查询发票服务费率返回值:"+JSON.toJSONString(rateConfig));
		if(null==rateConfig || rateConfig.getCode()!=R.SUCCESS) {
			throw new BaoxiaoException("查询发票服务费率失败");
		}
		if(!ObjectUtils.isEmpty(rateConfig.getData())) {
			serviceRate=rateConfig.getData().getServiceRate(); //发票服务费率
		}
		/*QueryCouponRateRuleDto  ruleDto=new QueryCouponRateRuleDto();
		ruleDto.setBusinessType(BusiModelEnums.BUSI_MODEL_YCCM.getCateCode());
		ruleDto.setCompanyId(companyId);
		R<List<MemberCouponRateRuleListVo>> ruleListR=refactorMemberCouponRateService.findCouponRateRuleByWhere(ruleDto);
		logger.info("查询企业服务费率:"+JSON.toJSONString(ruleListR));
		if(null==ruleListR || ruleListR.getCode()!=R.SUCCESS) {
			throw new BaoxiaoException("查询企业服务费率失败");
		}*/
		//吧券对应的商户金额放到Map中
		
		FundCouponTaxCodeDto taxDto=new FundCouponTaxCodeDto();
		taxDto.setCouponIdList(couponIdList);
		List<FundCouponTaxCodeVo> taxVoList=fundCouponTaxCodeMapper.findCouponTaxCode(taxDto);
		logger.info("得到消费券新商户费率等信息:"+JSON.toJSONString(taxVoList));
		
		//转map
		Map<String, FundCouponTaxCodeVo> fundCouponTaxCodeVoMap = taxVoList.stream()
				.collect(Collectors.toMap(FundCouponTaxCodeVo::getCouponId,o -> o));
		Map<String,FundCouponTaxCodeVo> accountMap = taxVoList.stream()
				.collect(Collectors.toMap(FundCouponTaxCodeVo::getVendorId,v -> v, (k,v)-> v));
		
		for (String  couponId : couponIdList) {
			logger.info("开始循环券得到服务费"+couponId);
			FundCouponTaxCodeVo codeVo=fundCouponTaxCodeVoMap.get(couponId);
			logger.info("查询这个消费券对应新商户费率信息为:"+JSON.toJSONString(codeVo));
			R<CouponVo> cR = tbCouponService.get(codeVo.getCompanyCouponId());
			logger.info("查询消费券关联信息:"+JSON.toJSONString(cR));
			if(null==cR || cR.getCode()!=R.SUCCESS) {
				throw new BaoxiaoException("查询消费券关联信息失败");
			}
			CouponVo c=cR.getData();
			// 普通卷
			Integer isP = 0;
			if (c.getInternalVendorList() != null && c.getInternalVendorList().size() == 1
					&& "-1".equals(c.getInternalVendorList().get(0).getBusinessId())) {
				// 商户不限的卷，非平台通用卷
				isP = 1;
				if ("-1".equals(c.getIndustryList().get(0).getBusinessId())
						&& "-1".equals(c.getCategoryList().get(0).getBusinessId())
						&& "-1".equals(c.getProvinceList().get(0).getBusinessId())) {
					// 平台通用卷：（行业，类别，商户，地域）都是不限
					isP = 2;
				}
			}
			BigDecimal rateCount=new BigDecimal(0);//费率
			if (isP == 1) {
				try {
					rateCount = refactorMemberCouponRateRuleListSercice
							.queryMaxServiceRateCe(companyId, "BIM00001");
					logger.info("账单生成计得到商户最大费率isP == 1差额为：" + JSON.toJSONString(rateCount));
					//企业平台差额大于0
				
				} catch (Exception e) {
					logger.error("账单生成计得到商户最大费率差额异常：", e);
					throw new RuntimeException("获取企业费率失败");
				}
			} else {
				for (CouponRelationVo ivo : c.getInternalVendorList()) {
	
					try {
						MemberCouponRateRuleListVo	rateCompanyVo = refactorMemberCouponRateRuleListSercice.queryCouponRateRule(
								companyId, ivo.getBusinessId(),
								"BIM00001");
						MemberCouponRateRuleListVo	rateSysVo = refactorMemberCouponRateRuleListSercice.queryCouponRateRule(
								"system", ivo.getBusinessId(),
								"BIM00001");
						logger.info("账单生成得到企业费率为isP == 2：" + JSON.toJSONString(rateCompanyVo));
						logger.info("账单生成得到平台费率为isP == 2：" + JSON.toJSONString(rateSysVo));
						if (rateCompanyVo != null && rateCompanyVo.getInvalidDate() != null
								&& (!new Date().after(rateCompanyVo.getInvalidDate()))
								&& rateSysVo != null && rateSysVo.getInvalidDate() != null
										&& (!new Date().after(rateSysVo.getInvalidDate()))) {
							// 结束时间在当前时间后
							rateCount = rateCompanyVo.getServiceRate().subtract(rateSysVo.getServiceRate());
						}
						logger.info("账单生成计得到商户最大费率isP == 2差额为：" + JSON.toJSONString(rateCount));
					} catch (Exception e) {
						logger.error("账单生成得到企业，平台费率>异常：", e);
						throw new RuntimeException("获取企业费率失败！");
					}
					
				}
				
			}	
			if(ObjectUtil.isNotNull(vendorAmountMap.get(codeVo.getVendorId())) ) {
				vendorAmountMap.put(codeVo.getVendorId(), vendorAmountMap.get(codeVo.getVendorId()).add(couponAmountMap.get(couponId)) );
			}else {
				vendorAmountMap.put(codeVo.getVendorId(),couponAmountMap.get(couponId));
			}
			if(rateCount.compareTo(new BigDecimal(0))==1) {
				//券金额*费率
				BigDecimal seAmpunt=couponAmountMap.get(couponId).multiply(rateCount).divide(new BigDecimal(10000));
				logger.info("当前券对应的结算金额为:"+couponId+">>"+seAmpunt);
				settleServiceAmount=settleServiceAmount.add(seAmpunt);

				if(ObjectUtil.isNotNull(vendorAmountMap.get(codeVo.getVendorId())) ) {
					vendorAmountMap.put(codeVo.getVendorId(), vendorAmountMap.get(codeVo.getVendorId()).add(seAmpunt) );
				}else {
					vendorAmountMap.put(codeVo.getVendorId(),seAmpunt);
				}
			}
			
			BigDecimal invoiceServiceAmountAlone = new BigDecimal(0); //单个发票服务费率
			
			//计算发票服务费
			if("1".equals(codeVo.getInvoiceType()) || "2".equals(codeVo.getInvoiceType())) {//专票
				//invoiceAmount = invoiceAmount.add(couponAmountMap.get(couponId));

				if(serviceRate != null && serviceRate.doubleValue() > 0 ) {
					invoiceServiceAmountAlone = couponAmountMap.get(couponId).multiply(serviceRate).divide(new BigDecimal(10000)).setScale(2,BigDecimal.ROUND_HALF_UP);
					logger.info("计算单个当前商户对应的发票金额为:"+invoiceServiceAmountAlone);
				}
				if(ObjectUtil.isNotNull(vendorAmountMap.get(codeVo.getVendorId())) ) {
					vendorAmountMap.put(codeVo.getVendorId(), vendorAmountMap.get(codeVo.getVendorId()).add(invoiceServiceAmountAlone));
				}else {
					vendorAmountMap.put(codeVo.getVendorId(),invoiceServiceAmountAlone);
				}
			}
			invoiceServiceAmount=invoiceServiceAmount.add(invoiceServiceAmountAlone);
		}
		logger.info("最终累计结算金额为:"+settleServiceAmount);
		rtnMap.put("settleServiceAmount", settleServiceAmount);
		logger.info("发票服务费用为:"+invoiceServiceAmount);
		rtnMap.put("invoiceServiceAmount", invoiceServiceAmount);

		Set<Map.Entry<String, BigDecimal>> entrySet1 = vendorAmountMap.entrySet();
		Iterator<Map.Entry<String, BigDecimal>> iter1 = entrySet1.iterator();
		while (iter1.hasNext()){
		    Map.Entry<String, BigDecimal> entry = iter1.next();
		    logger.info("每个商户对应的钱为:"+entry.getKey() + "," + entry.getValue());
			BillItemNew item=new BillItemNew();
			item.setBillNo(billNo);
			item.setBillItemNo(CodeUtils.genneratorShort("ZDMX"));
			item.setBillItemKind(BillItemSubCate.BILL_ITEM_VENDOR_AMOUNT.getCateCode());
			item.setBillItemKindName(BillItemSubCate.BILL_ITEM_VENDOR_AMOUNT.getCateName());
			item.setInternalVendorId(entry.getKey());
			item.setInternalVendorName(accountMap.get(entry.getKey()).getVendorName());
			item.setBillItemCate(accountMap.get(entry.getKey()).getOpenBank());
			item.setBillItemCateName(accountMap.get(entry.getKey()).getBankAccount());
			item.setBillAmount(vendorAmountMap.get(entry.getKey()));
			item.setBillLeftAmount(vendorAmountMap.get(entry.getKey()));
			item.setBillPayAmount(new BigDecimal(0));
			item.setStatus("3");//待打款
			item.setCreator(userName);
			item.setUpdator(userName);
		    logger.info("开始添加账单明细入参参数为:"+JSON.toJSONString(item));
		    billItemNewService.insert(item);
		    
		}
		
		Set<Map.Entry<String, BigDecimal>> couponEntrySet = couponAmountMap.entrySet();
		Iterator<Map.Entry<String, BigDecimal>> iterCoupon = couponEntrySet.iterator();
		while (iterCoupon.hasNext()){
			Map.Entry<String, BigDecimal> entry = iterCoupon.next();
			logger.info("每个券对应的钱为:"+entry.getKey() + "," + entry.getValue());
			FundCouponTaxCode taxCode=findVendorIdNameByCouponId(entry.getKey());
			logger.info("通过券id查询商户信息为："+JSON.toJSONString(taxCode));
	        if(ObjectUtils.isEmpty(taxCode)) {
	            throw new BaoxiaoException("没有查询到开票商户信息");
	        }
			FundBillInvoiceSetting invoiceSet=new FundBillInvoiceSetting();
			invoiceSet.setBillNo(billNo);
			invoiceSet.setCouponId(entry.getKey());
			invoiceSet.setInvoiceAmount(vendorAmountMap.get(taxCode.getVendorId()));
			invoiceSet.setInvoiceStatus("1");
			invoiceSet.setVendorId(taxCode.getVendorId());
			invoiceSet.setVendorName(taxCode.getVendorName());
			invoiceSet.setUpdator(userName);
			invoiceSet.setCreator(userName);
			fundBillInvoiceSettingMapper.insert(invoiceSet);
		}
		
		
		logger.info("计算完毕之后map:"+JSON.toJSONString(rtnMap));
		return rtnMap;
	}
	
	@Override
	public R<Page<BillBaseNewVo>> queryBillBasePageByParams(Query query, BillBaseNewDto queryParams) {
		query.setRecords(baseMapper.queryBillBasePageByParams(query, queryParams));
		return new R<>(query);
	}

	@Override
	public R<BillBaseNewVo> queryBillBaseInfo(BillBaseNewDto queryParams) {
		R<BillBaseNewVo> r=new R<BillBaseNewVo>();
		String billNo=queryParams.getBillNo();
		BillBaseNew base=new BillBaseNew();
		base.setBillNo(billNo);
		BillBaseNew baseNew=baseMapper.selectOne(base);
		logger.info("通过账单编号查询账单信息返回值:"+JSON.toJSONString(baseNew));
		if(ObjectUtils.isEmpty(baseNew)) {
			logger.info("没有查询到改账单号的数据:"+billNo);
			r.setCode(R.FAIL);
			r.setData(null);
			r.setMsg("没有改账单信息");
			return r;
		}
		BillItemNewDto itemDto=new BillItemNewDto();
		itemDto.setBillNo(billNo);
		List<BillItemNewVo> itemVoList=billItemNewService.queryBillItem(itemDto);
		logger.info("查询账单详情返回值:"+JSON.toJSONString(itemVoList));
		BillBaseNewVo baseNewVo=new BillBaseNewVo();
		BeanUtils.copyProperties(baseNew, baseNewVo);
		baseNewVo.setBillItemList(itemVoList);
		//通过账单号查询账单开票配置信息
		FundBillInvoiceSettingDto settingDto=new FundBillInvoiceSettingDto();
		settingDto.setBillNo(billNo);
		List<FundBillInvoiceSettingVo> settingVoList=fundBillInvoiceSettingMapper.findInvoiceSettingByBillNo(settingDto);
		logger.info("账单开票配置信息返回值:"+JSON.toJSONString(settingVoList));
		baseNewVo.setSettingVoList(settingVoList);
		logger.info("账单全部明细信息为："+JSON.toJSONString(baseNewVo));
		return new R<>(baseNewVo);
	}

	@Override
	public R<List<MemberInvoiceComRuleVo>> getItemInfo(FundBillInvoiceSettingVo set) throws Exception{
		List<MemberInvoiceComRuleVo> ruleRtnList=Lists.newArrayList();
		Map<String,String> itemMap=Maps.newHashMap();
		MemberInvoiceComRuleDto ruleDto=new MemberInvoiceComRuleDto();
		List<String> vendorIdList=Lists.newArrayList(set.getVendorId().split(","));
		ruleDto.setVendorIdList(vendorIdList);
		String invoiceTypeCode="";
		if("2".equals(set.getInvoiceForm())) {//如果是电子发票
			invoiceTypeCode=InvoiceTypeNew.E_INVOICE.getCateCode();
		}else {
			if("0".equals(set.getInvoiceType())) {//普票
				invoiceTypeCode=InvoiceTypeNew.VAT_INVOICE.getCateCode();
			}else{//专票
				invoiceTypeCode=InvoiceTypeNew.VAT_SPECIAL_INVOICE.getCateCode();
			}
		}
		
		ruleDto.setInvoiceTypeCode(invoiceTypeCode);
		List<MemberInvoiceComRuleVo> ruleList=refactorMemberComInvoiceApi.selectByCategoryByCouponId(ruleDto);
		logger.info("通过商户信息查询科目信息返回值为:"+JSON.toJSONString(ruleList));

		for (MemberInvoiceComRuleVo memberInvoiceComRuleVo : ruleList) {
			if(StringUtils.isNotBlank(memberInvoiceComRuleVo.getInvoiceIndustryName())) {
				String items="*"+memberInvoiceComRuleVo.getInvoiceIndustryName()+"*"+
						memberInvoiceComRuleVo.getInvoiceCategoryName()+" (税率"+memberInvoiceComRuleVo.getTaxRate().divide(new BigDecimal(100))+"%)";
				logger.info("费率组装信息为:"+items);
				if(StringUtils.isBlank(itemMap.get(items))) {
					itemMap.put(items, items);
					memberInvoiceComRuleVo.setRemark(items);
				}
			}
			ruleRtnList.add(memberInvoiceComRuleVo);
		}
		return new R<>(ruleRtnList);
	}

	@Override
	public R<Boolean> invoiceSetting(FundBillInvoiceSettingDto dto) {
		FundBillInvoiceSetting setEntity=new FundBillInvoiceSetting();
		setEntity.setId(dto.getId());
		setEntity.setInvoiceForm(dto.getInvoiceForm());
		setEntity.setInvoiceType(dto.getInvoiceType());
		setEntity.setInvoiceItemContent(dto.getInvoiceItemContent());
		setEntity.setMainItemCode(dto.getMainItemCode());
		setEntity.setMainItemName(dto.getMainItemName());
		setEntity.setSubItemCode(dto.getSubItemCode());
		setEntity.setSubItemName(dto.getSubItemName());
		setEntity.setTaxPoint(dto.getTaxPoint());
		Integer setInt=fundBillInvoiceSettingMapper.updateById(setEntity);
		logger.info("修改发票设置结果："+setInt);
		return new R<>(true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R<BillBaseNew> toMakeMoney(BillBaseNewDto queryParams) throws Exception {
		R<BillBaseNew> r = new R<BillBaseNew>();
		//查询是否首次发起打款
		FundFlowRecordVo fundVo = new FundFlowRecordVo();
		fundVo.setBusiCode(queryParams.getBillNo());
		fundVo.setMemberId(queryParams.getMemberId());
		fundVo.setCompanyId(queryParams.getCompanyId());
		fundVo.setStatus("2");
		logger.info("开始查询是否首次发起打款传入参数》》》》{}"+JSON.toJSONString(queryParams));
		FundFlowRecordVo findFlowRecord = fundFlowRecordService.findFundFlowRecord(fundVo);

		logger.info("查询是否首次发起打款返回结果》》》》{}"+JSON.toJSONString(findFlowRecord));
		if(ObjectUtil.isNotNull(findFlowRecord) && "2".equals(findFlowRecord.getStatus())) {
			r = reLaunchToMakeMoney(findFlowRecord);
			logger.info("第二次发起打款传入参数》》》》{}"+JSON.toJSONString(findFlowRecord));
		}else{
			//查询是否首次发起打款 如果是说明是首次发起
			r = firstToMakeMoney(queryParams);
			logger.info("首次发起打款传入参数》》》》{}"+JSON.toJSONString(queryParams));
		}
		return r;
	}
	
	@Transactional(rollbackFor = Exception.class)
	private R<BillBaseNew> reLaunchToMakeMoney(FundFlowRecordVo queryParams) throws Exception{
		String step = queryParams.getFlowNo();
		String tid="";
		if("JBT_RECOVER_AMOUNT".equals(step)) {
			FundFlowRecordVo fundTidvo = new FundFlowRecordVo();
			fundTidvo.setBusiCode(queryParams.getBusiCode());
			fundTidvo.setMemberId(queryParams.getMemberId());
			fundTidvo.setCompanyId(queryParams.getCompanyId());
			fundTidvo.setSort(BillPaymentFlow.ZB_CHARGEBACK.getCateMgn()+"");
			fundTidvo.setFlowNo(BillPaymentFlow.ZB_CHARGEBACK.getCateCode());
			logger.info("开始查询嘉福还款流水号传入参数》》》》{}"+JSON.toJSONString(fundTidvo));
			FundFlowRecordVo findTid = fundFlowRecordService.findFundFlowRecord(fundTidvo);
			logger.info("查询嘉福还款流水号返回结果》》》》{}"+JSON.toJSONString(findTid));
			String js = findTid.getBusiReturn();
			JSONObject jsonObj = new JSONObject(js);
			if(null != jsonObj.get("data").toString()) {
				JSONObject jsonObjvo = new JSONObject(jsonObj.get("data").toString());
				if(null != jsonObjvo.get("tid")) {
					tid = jsonObjvo.get("tid").toString();
				}
			}
            logger.info("最后一步获取tid:" + JSON.toJSONString(tid));
		}
		logger.info("账单打款开始>>>>>>");
		MakeMoneySendSmsDto makeMoneySendSmsDto = new MakeMoneySendSmsDto(); //打款成功发送短信通知dto
		List<String> memberIdList = Lists.newArrayList(); // 需要发送短信的memberIdList
		FundFlowRecordVo addFundFlowRecord = new FundFlowRecordVo();//账单打款记录返回对象
		addFundFlowRecord.setFlowCode(queryParams.getFlowCode());
		addFundFlowRecord.setId(queryParams.getId());
		addFundFlowRecord.setDetailId(queryParams.getDetailId());
		R<BillBaseNew> r=new R<BillBaseNew>();
		String billNo = queryParams.getBusiCode();
		BillBaseNew base = new BillBaseNew();
		base.setBillNo(billNo);
		BillBaseNew baseNew = baseMapper.selectOne(base);
		logger.info("根据billNo查询账单信息为:" + JSON.toJSONString(baseNew));
		r.setData(baseNew);
		// 查询企业的工资账户
		R<UserVo> vo = dockOpenPlatformService.accountGetById(baseNew.getCompanyId(), null);
		logger.info("查询企业的工资账户返回参数:" + JSON.toJSONString(vo));
		if (null == vo || vo.getCode() != R.SUCCESS) {
			throw new BaoxiaoException("查询企业的工资账户有误");
		}
		makeMoneySendSmsDto.setCompanyId(baseNew.getCompanyId());
		makeMoneySendSmsDto.setCompanyName(baseNew.getCompanyName());
		BigDecimal salAmount = vo.getData().getSalary_balance();
		// 当个人账户额度大于等于账单总金额
		if (baseNew.getBillAmount().compareTo(salAmount) > 0) {
			throw new BaoxiaoException("企业账户余额为" + salAmount.divide(new BigDecimal(1000)) + "元，无法完成账单缴纳，请及时打款");
		}
		Map<String, Object> setMap = Maps.newHashMap();
		setMap.put("bill_no", billNo);
		setMap.put("del_flag", "0");
		List<FundBillInvoiceSetting> setList = fundBillInvoiceSettingMapper.selectByMap(setMap);
		logger.info("查询账单发票设置信息为:" + JSON.toJSONString(setList));
		for (FundBillInvoiceSetting fundBillInvoiceSetting : setList) {
			if (StringUtils.isBlank(fundBillInvoiceSetting.getMainItemCode())
					|| StringUtils.isBlank(fundBillInvoiceSetting.getInvoiceType())) {
				throw new BaoxiaoException("请先查看开票设置汇总模块，进行开票设置。");
			}
		}
		
		BillSettleAccountsDto setAccountDto = new BillSettleAccountsDto();
		setAccountDto.setBillNumber(billNo);
		List<BillSettleAccountsDto> setAccountVoList = billSettleAccountsService.findAmountByMember(setAccountDto);
		logger.info("查询账单下每个员工对应的总报销金额为:" + JSON.toJSONString(setAccountVoList));
		if (null == setAccountVoList || setAccountVoList.size() <= 0) {
			throw new BaoxiaoException("没有查到账单对应的结算单数据!");
		}
		
		Map<String, BigDecimal> memberIdMap = Maps.newHashMap();// 每个员工对应的返现总额
		AccountAssignDto accountAssignDto = new AccountAssignDto();
		List<String> reimburseCodeList=Lists.newArrayList();
		List<AssignItemVo> assignList = Lists.newArrayList();
		for (BillSettleAccountsDto billSettleAccountsDto : setAccountVoList) {
			memberIdList.add(billSettleAccountsDto.getMemberId());
			AssignItemVo assignItemVo = new AssignItemVo();
			assignItemVo.setMemberId(billSettleAccountsDto.getMemberId());
			assignItemVo.setAmount(billSettleAccountsDto.getSettleAmount());
			assignList.add(assignItemVo);
			memberIdMap.put(billSettleAccountsDto.getMemberId(), billSettleAccountsDto.getCashBackAmount());
		}
		makeMoneySendSmsDto.setMemberIds(memberIdList);
		logger.info("给员工打款报销的钱跟员工对应值为:" + JSON.toJSONString(assignList));
		Map<String,Object> accMap=Maps.newHashMap();
		accMap.put("bill_number", billNo);
		accMap.put("del_flag", "0");
		List<BillSettleAccounts> accList=billSettleAccountsService.selectByMap(accMap);
		logger.info("查询账单下结算单信息为:" + JSON.toJSONString(accList));
		if (null == accList || accList.size() <= 0) {
			throw new BaoxiaoException("没有查到账单对应的结算单数据!");
		}
		for (BillSettleAccounts billSettleAccounts : accList) {
			/*Order orderEntity=new Order();
		orderEntity.setSourceCode(billSettleAccounts.getSettleNo());
		orderEntity.setDelFlag("0");*/
			/*Map<String,Object> orderMap=Maps.newHashMap();
		orderMap.put("source_code", billSettleAccounts.getSettleNo());
		orderMap.put("del_flag", "0");
		List<Order> orderList=orderMapper.selectByMap(orderMap);
		for (Order order : orderList) {
			order.setStatus(OrderBusiStatus.PAYMENT_SUCCESS.getCateCode());
			orderMapper.updateById(order);
		}*/
			if("1".equals(billSettleAccounts.getSettleType())) {//简易报销
				reimburseCodeList.add(billSettleAccounts.getBillNo());
			}
		}
		
		accountAssignDto.setCompanyId(baseNew.getCompanyId());
		accountAssignDto.setBiz_id(billNo);
		accountAssignDto.setAct_type(DockEnum.actType.act_type_salary.getCode());
		if (assignList.size() > 0) {
			if("MAKE_MONEY_REIMBURSE".equals(step)) {
				accountAssignDto.setAssign(assignList);
				logger.info("给员工打款报销的钱入参参数:" + JSON.toJSONString(accountAssignDto));
				R<Boolean> accountR = dockOpenPlatformService.accountAssignByCId(accountAssignDto);

				logger.info("给员工打款报销的钱返回值:" + JSON.toJSONString(accountR));
				
				BillBaseNewDto dto = new BillBaseNewDto();
				dto.setCompanyId(baseNew.getCompanyId());
				dto.setBusiCode(billNo);
				dto.setFlowCode(addFundFlowRecord.getFlowCode());
				dto.setMainId(addFundFlowRecord.getId());
				dto.setDetailId(addFundFlowRecord.getDetailId());
				dto.setFlowName(BillPaymentFlow.MAKE_MONEY_REIMBURSE.getCateName());
				dto.setFlowNo(BillPaymentFlow.MAKE_MONEY_REIMBURSE.getCateCode()+"");
				dto.setFundFlowStatus(accountR.getData() ? "1" : "2");
				dto.setSort(BillPaymentFlow.MAKE_MONEY_REIMBURSE.getCateMgn()+"");
				dto.setBusiReturn(JSON.toJSONString(accountR));
				logger.info("账单打款记录给员工打报销的钱传入参数》》》》"+JSON.toJSONString(dto));
				addFundFlowRecord = this.addFundFlowRecord(dto);
				logger.info("账单打款记录给员工打报销的钱返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
				
				if (null == accountR || accountR.getCode() != R.SUCCESS) {
					throw new BaoxiaoException("给员工打报销款失败!");
				}
				
				for (BillSettleAccountsDto billSettleAccountsDto : setAccountVoList) {
					TbFundTradeFlow flowEntity=new TbFundTradeFlow();
					flowEntity.setSource("给员工打报销款");
					flowEntity.setTradeAmount(billSettleAccountsDto.getSettleAmount());
					flowEntity.setTradeBusiCode(billSettleAccountsDto.getMemberId());
					flowEntity.setTradeBusiCate(billNo);
					logger.info("添加给员工打报销款账单流水交易记录传入参数》》》》"+JSON.toJSONString(flowEntity));
					addTradeFlow(flowEntity);
				}
			}
		}
		
		BigDecimal fxSumAmount=new BigDecimal(0);
		List<AssignItemVo> assignFXList = Lists.newArrayList();
		Set<Map.Entry<String, BigDecimal>> entrySet = memberIdMap.entrySet();
		Iterator<Map.Entry<String, BigDecimal>> iter = entrySet.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, BigDecimal> entry = iter.next();
			logger.info("每个员工对应返现金额为:" + entry.getKey() + "," + entry.getValue());
			fxSumAmount=fxSumAmount.add(entry.getValue());
			if (entry.getValue().compareTo(new BigDecimal(0)) == 1) {
				AssignItemVo assignItemVo = new AssignItemVo();
				assignItemVo.setMemberId(entry.getKey());
				assignItemVo.setAmount(entry.getValue());
				assignFXList.add(assignItemVo);
			}
		}
		logger.info("给员工打款返现的钱对应员工值为:" + JSON.toJSONString(assignFXList));
		if (assignFXList.size() > 0) {
			accountAssignDto.setBiz_id(billNo + "_1");
			accountAssignDto.setAssign(assignFXList);
			logger.info("给员工打款返现的钱入参参数:" + JSON.toJSONString(accountAssignDto));
			if("MAKE_MONEY_REIMBURSE".equals(step) || "MAKE_MONEY_CASH_RETURN".equals(step)) {
				R<Boolean> accountFxR = dockOpenPlatformService.accountAssignByCId(accountAssignDto);

				logger.info("给员工打款返现的钱返回值:" + JSON.toJSONString(accountFxR));
				
				BillBaseNewDto dto = new BillBaseNewDto();
				dto.setCompanyId(baseNew.getCompanyId());
				dto.setBusiCode(billNo);
				dto.setFlowName(BillPaymentFlow.MAKE_MONEY_CASH_RETURN.getCateName());
				dto.setFlowNo(BillPaymentFlow.MAKE_MONEY_CASH_RETURN.getCateCode()+"");
				dto.setFundFlowStatus(accountFxR.getData() ? "1" : "2");
				dto.setSort(BillPaymentFlow.MAKE_MONEY_CASH_RETURN.getCateMgn()+"");
				dto.setFlowCode(addFundFlowRecord.getFlowCode());
				dto.setMainId(addFundFlowRecord.getId());
				dto.setDetailId(addFundFlowRecord.getDetailId());
				dto.setBusiReturn(JSON.toJSONString(accountFxR));
				logger.info("账单打款记录给员工打返现的钱传入参数》》》》"+JSON.toJSONString(dto));
				addFundFlowRecord = this.addFundFlowRecord(dto);
				logger.info("账单打款记录给员工打返现的钱返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
				
				if (null == accountFxR || accountFxR.getCode() != R.SUCCESS) {
					throw new BaoxiaoException("给员工打返现款失败!");
				}
				
				while (iter.hasNext()) {
					Map.Entry<String, BigDecimal> entry = iter.next();
					if (entry.getValue().compareTo(new BigDecimal(0)) == 1) {
						addOrder(baseNew, entry.getKey(), entry.getValue());
						TbFundTradeFlow flowEntity=new TbFundTradeFlow();
						flowEntity.setSource("给员工打返现款");
						flowEntity.setTradeAmount(entry.getValue());
						flowEntity.setTradeBusiCode(entry.getKey());
						flowEntity.setTradeBusiCate(billNo + "_1");
						logger.info("添加给员工打返现款账单流水交易记录传入参数》》》》"+JSON.toJSONString(flowEntity));
						addTradeFlow(flowEntity);
					}
				}
				/**打款成功发送短信通知   start  by zangjintian **/
				logger.info("给员工打款成功后批量发送短息请求参数，request param dto:"+ JSON.toJSONString(makeMoneySendSmsDto));
				boolean isOK = this.makeMoneyAfterSendSms(makeMoneySendSmsDto);
				logger.info("给员工打款成功后批量发送短息返回结果，return result isOK:"+ JSON.toJSONString(isOK));
				if(!isOK) {
					logger.error("给员工打款成功后批量发送短息异常，>>>-----error----->>>>返回结果 return result isOK:{}"+ isOK);
				}
				/**打款成功发送短信通知   end  by zangjintian **/
			}
		}
		
		// 账单总金额-所有员工报销金额加总-所有员工专票返现金额加总=需要砾洲打的钱
		BigDecimal companyAmount = baseNew.getBillAmount()
				.subtract(baseNew.getBillReimburseAmount())
				.subtract(baseNew.getBiilBuyAmount())
				.subtract(baseNew.getBillPaymentAmount())
				.subtract(fxSumAmount);
		logger.info("给砾洲打款金额为:" + companyAmount);
		if (companyAmount.compareTo(new BigDecimal(0)) == 1) {
			if("MAKE_MONEY_REIMBURSE".equals(step) || "MAKE_MONEY_CASH_RETURN".equals(step) || "MAKE_MONEY_LIZHOU".equals(step)) {
				// 给砾洲企业打款
				R<TransferOrderVo> comR = dockOpenPlatformService.inTransferOrder(baseNew.getCompanyId(),
						AcctCateEnums.ACCT_CATE_PTLZ.getCateMgn(), billNo + "_2", companyAmount,
						DockEnum.actType.act_type_salary.getCode());

				logger.info("给砾洲打款返回值:" + JSON.toJSONString(comR));
				
				BillBaseNewDto dto = new BillBaseNewDto();
				dto.setCompanyId(baseNew.getCompanyId());
				dto.setBusiCode(billNo);
				dto.setFlowName(BillPaymentFlow.MAKE_MONEY_LIZHOU.getCateName());
				dto.setFlowNo(BillPaymentFlow.MAKE_MONEY_LIZHOU.getCateCode()+"");
				dto.setFundFlowStatus(comR.getCode() == R.SUCCESS? "1" : "2");
				dto.setSort(BillPaymentFlow.MAKE_MONEY_LIZHOU.getCateMgn()+"");
				dto.setFlowCode(addFundFlowRecord.getFlowCode());
				dto.setMainId(addFundFlowRecord.getId());
				dto.setDetailId(addFundFlowRecord.getDetailId());
				dto.setBusiReturn(JSON.toJSONString(comR));
				logger.info("账单打款记录给砾洲打款传入参数》》》》"+JSON.toJSONString(dto));
				addFundFlowRecord = this.addFundFlowRecord(dto);
				logger.info("账单打款记录给砾洲打款返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
				
				if (null == comR || comR.getCode() != R.SUCCESS) {
					throw new BaoxiaoException("给砾洲打款失败!");
				}
				TbFundTradeFlow flowEntity=new TbFundTradeFlow();
				flowEntity.setSource("给企业打款");
				flowEntity.setTradeAmount(companyAmount);
				flowEntity.setTradeBusiCode(AcctCateEnums.ACCT_CATE_PTLZ.getCateMgn());
				flowEntity.setTradeBusiCate(billNo + "_2");
				logger.info("添加给企业打款账单流水交易记录传入参数》》》》"+JSON.toJSONString(flowEntity));
				addTradeFlow(flowEntity);
			}
		}
		
		
		//查询这个账单含有嘉白条的金额 按人分组
		R<List<BillBaseNewVo>> rJia=this.queryJiaStripQuota(billNo);
		if(null==rJia || R.SUCCESS!=rJia.getCode()) {
			logger.info("查询这个账单含有嘉白条的金额错误"+JSON.toJSONString(rJia));
			throw new BaoxiaoException("查询这个账单含有嘉白条的金额失败!");
		}
		//开始消费券额度解冻
		List<BillBaseNewVo> jiaOrder=this.baseMapper.queryZdOrder(billNo);
		if(!CollUtil.isEmpty(jiaOrder)) {
			for (BillBaseNewVo billBaseNewVo : jiaOrder) {
				logger.info("开始循环参数"+JSON.toJSONString(billBaseNewVo));
				String billNewNo=billBaseNewVo.getBillNo();	//购买单号
				if(!"JBT_RECOVER_AMOUNT".equals(step) && !"UPDATE_REIMBURSE_STATUS".equals(step)) {
					logger.info("给员工扣嘉白条额度入参参数"+billBaseNewVo.getBillAmount()+"，"+billNewNo+","+billBaseNewVo.getMemberId()+","+billBaseNewVo.getCompanyId());
					DockR<DockChargebackRecordVo> btOpen=openJiaBaiTiaoBuyCouponService.chargeback(billBaseNewVo.getBillAmount(), billBaseNewVo.getMemberId(), 
							billBaseNewVo.getCompanyId(), billNewNo, paymentType.act_type_salary.getCode());
					logger.info("给员工扣嘉白条额度返回值"+JSON.toJSONString(btOpen));
				
					BillBaseNewDto dto = new BillBaseNewDto();
					dto.setCompanyId(baseNew.getCompanyId());
					dto.setMemberId(billBaseNewVo.getMemberId());
					dto.setBusiCode(billNo);
					dto.setFlowName(BillPaymentFlow.ZB_CHARGEBACK.getCateName());
					dto.setFlowNo(BillPaymentFlow.ZB_CHARGEBACK.getCateCode()+"");
					dto.setFundFlowStatus(btOpen.getCode().equals(DockR.SUCCESS) ? "1" : "2");
					dto.setSort(BillPaymentFlow.ZB_CHARGEBACK.getCateMgn()+"");
					dto.setFlowCode(addFundFlowRecord.getFlowCode());
					dto.setMainId(addFundFlowRecord.getId());
					dto.setDetailId(addFundFlowRecord.getDetailId());
					dto.setBusiReturn(JSON.toJSONString(btOpen));
					logger.info("账单打款记录调用总部扣款接口传入参数》》》》"+JSON.toJSONString(dto));
					addFundFlowRecord = this.addFundFlowRecord(dto);
					logger.info("账单打款记录调用总部扣款接口返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
					
					if(null == btOpen || !DockR.SUCCESS.equals(btOpen.getCode())) {
						throw new BaoxiaoException("给员工扣嘉白条额度失败!");
					}
					tid=btOpen.getData().getTid();//充值流水号
				}
				//嘉白条恢复额度
				logger.info("嘉白条恢复额度入参参数"+billBaseNewVo.getBillAmount()+"，"+billBaseNewVo.getJbtId()+","+billBaseNewVo.getMemberId()+","+billBaseNewVo.getCompanyId()+","+billBaseNewVo.getExtRefOrderId()+","+tid);
				DockR<JiaBaiTiaoReturnVo> hfR=openJiaBaiTiaoBuyCouponService.jiaBaiTiaoRepayment(billBaseNewVo.getBillAmount(), billBaseNewVo.getJbtId(), "02",billBaseNewVo.getExtRefOrderId(), tid, billBaseNewVo.getMemberId(), billBaseNewVo.getCompanyId());
				logger.info("嘉白条恢复额度返回值"+JSON.toJSONString(hfR));
				
				BillBaseNewDto jbtdto = new BillBaseNewDto();
				jbtdto.setCompanyId(baseNew.getCompanyId());
				jbtdto.setMemberId(billBaseNewVo.getMemberId());
				jbtdto.setBusiCode(billNo);
				jbtdto.setFlowCode(addFundFlowRecord.getFlowCode());
				jbtdto.setFlowName(BillPaymentFlow.JBT_RECOVER_AMOUNT.getCateName());
				jbtdto.setFlowNo(BillPaymentFlow.JBT_RECOVER_AMOUNT.getCateCode()+"");
				jbtdto.setFundFlowStatus(hfR.getCode().equals(DockR.SUCCESS) ? "1" : "2");
				jbtdto.setSort(BillPaymentFlow.JBT_RECOVER_AMOUNT.getCateMgn()+"");
				jbtdto.setMainId(addFundFlowRecord.getId());
				jbtdto.setDetailId(addFundFlowRecord.getDetailId());
				jbtdto.setBusiReturn(JSON.toJSONString(hfR));
				logger.info("账单打款记录嘉白条额度恢复传入参数》》》》"+JSON.toJSONString(jbtdto));
				addFundFlowRecord = this.addFundFlowRecord(jbtdto);
				logger.info("账单打款记录嘉白条额度恢复返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
				
				if(null == hfR || !DockR.SUCCESS.equals(hfR.getCode())) {
					throw new BaoxiaoException("给员工扣嘉白条额度失败!");
				}
				
				//解冻消费券额度
				freeCouponAmount(billBaseNewVo);
			}
		}
		BillBaseNew baseEntity = new BillBaseNew();
		baseEntity.setId(baseNew.getId());
		baseEntity.setStatus(BillStatus.TOBEINVOICE.getCateCode());
		baseEntity.setBillPayAmount(baseNew.getBillAmount());
		this.baseMapper.updateById(baseEntity);
		Map<String,Object> itemMap=Maps.newHashMap();
		itemMap.put("bill_no", billNo);
		itemMap.put("del_flag", "0");
		List<BillItemNew> itemList=billItemNewService.selectByMap(itemMap);
		//打款完毕之后给账单明细修改状态跟打款金额
		for (BillItemNew billItemNew : itemList) {
			BillItemNew updateItem=new BillItemNew();
			updateItem.setId(billItemNew.getId());
			updateItem.setStatus("2");
			updateItem.setBillLeftAmount(new BigDecimal(0));
			updateItem.setBillPayAmount(billItemNew.getBillAmount());
			billItemNewService.updateById(updateItem);
		}
		//给账单含有报销单子修改报销状态
		if(null!=reimburseCodeList && reimburseCodeList.size()>0) {
			ReimburseDTO reimburseDto=new ReimburseDTO();
			reimburseDto.setIdList(reimburseCodeList);
			if(!"UPDATE_REIMBURSE_STATUS".equals(step)) {
				logger.info("修改报销单状态传入参数》》》》"+JSON.toJSONString(reimburseDto));
                R<Boolean> paySuccess = refactorInvoiceReimburseService.paySuccess(reimburseDto);
				logger.info("修改报销单状态返回结果》》》》"+JSON.toJSONString(paySuccess));
				
				BillBaseNewDto dto = new BillBaseNewDto();
				dto.setCompanyId(baseNew.getCompanyId());
				dto.setBusiCode(billNo);
				dto.setFlowName(BillPaymentFlow.UPDATE_REIMBURSE_STATUS.getCateName());
				dto.setFlowNo(BillPaymentFlow.UPDATE_REIMBURSE_STATUS.getCateCode()+"");
				dto.setFundFlowStatus(paySuccess.getData() ? "1" : "2");
				dto.setSort(BillPaymentFlow.UPDATE_REIMBURSE_STATUS.getCateMgn()+"");
				dto.setFlowCode(addFundFlowRecord.getFlowCode());
				dto.setMainId(addFundFlowRecord.getId());
				dto.setDetailId(addFundFlowRecord.getDetailId());
				dto.setBusiReturn(JSON.toJSONString(paySuccess));
				logger.info("账单打款记录修改报销单状态传入参数》》》》"+JSON.toJSONString(dto));
				addFundFlowRecord = this.addFundFlowRecord(dto);
				logger.info("账单打款记录修改报销单状态返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
				
				if (null == paySuccess || !paySuccess.getData()) {
					throw new BaoxiaoException("修改报销单状态失败!");
				}
			}
		}
		
		for (BillSettleAccounts billSettleAccounts : accList) {
			billSettleAccounts.setStatus(billSettleAccountsStatus.STATUS_2.getCode());
			logger.info("修改结算单为已打款》》》》"+JSON.toJSONString(billSettleAccounts));
			billSettleAccountsService.updateById(billSettleAccounts);
		}
		logger.info("账单打款结束>>>>>>");
		return r;
	}
	
	@Transactional(rollbackFor = Exception.class)
	private R<BillBaseNew> firstToMakeMoney(BillBaseNewDto queryParams) throws Exception{
		logger.info("账单打款开始>>>>>>");
		MakeMoneySendSmsDto makeMoneySendSmsDto = new MakeMoneySendSmsDto(); //打款成功发送短信通知dto
		List<String> memberIdList = Lists.newArrayList(); // 需要发送短信的memberIdList
		FundFlowRecordVo addFundFlowRecord = new FundFlowRecordVo();//账单打款记录返回对象
		R<BillBaseNew> r=new R<BillBaseNew>();
		String billNo = queryParams.getBillNo();
		BillBaseNew base = new BillBaseNew();
		base.setBillNo(billNo);
		BillBaseNew baseNew = baseMapper.selectOne(base);
		logger.info("根据billNo查询账单信息为:" + JSON.toJSONString(baseNew));
		r.setData(baseNew);
		// 查询企业的工资账户
		R<UserVo> vo = dockOpenPlatformService.accountGetById(baseNew.getCompanyId(), null);
		logger.info("查询企业的工资账户返回参数:" + JSON.toJSONString(vo));
		if (null == vo || vo.getCode() != R.SUCCESS) {
			throw new BaoxiaoException("查询企业的工资账户有误");
		}
		makeMoneySendSmsDto.setCompanyId(baseNew.getCompanyId());
		makeMoneySendSmsDto.setCompanyName(baseNew.getCompanyName());
		BigDecimal salAmount = vo.getData().getSalary_balance();
		// 当个人账户额度大于等于账单总金额
		if (baseNew.getBillAmount().compareTo(salAmount) > 0) {
			throw new BaoxiaoException("企业账户余额为" + salAmount.divide(new BigDecimal(1000)) + "元，无法完成账单缴纳，请及时打款");
		}
		Map<String, Object> setMap = Maps.newHashMap();
		setMap.put("bill_no", billNo);
		setMap.put("del_flag", "0");
		List<FundBillInvoiceSetting> setList = fundBillInvoiceSettingMapper.selectByMap(setMap);
		logger.info("查询账单发票设置信息为:" + JSON.toJSONString(setList));
		for (FundBillInvoiceSetting fundBillInvoiceSetting : setList) {
			if (StringUtils.isBlank(fundBillInvoiceSetting.getMainItemCode())
					|| StringUtils.isBlank(fundBillInvoiceSetting.getInvoiceType())) {
				throw new BaoxiaoException("请先查看开票设置汇总模块，进行开票设置。");
			}
		}
		
		BillSettleAccountsDto setAccountDto = new BillSettleAccountsDto();
		setAccountDto.setBillNumber(billNo);
		List<BillSettleAccountsDto> setAccountVoList = billSettleAccountsService.findAmountByMember(setAccountDto);
		logger.info("查询账单下每个员工对应的总报销金额为:" + JSON.toJSONString(setAccountVoList));
		if (null == setAccountVoList || setAccountVoList.size() <= 0) {
			throw new BaoxiaoException("没有查到账单对应的结算单数据!");
		}
		
		Map<String, BigDecimal> memberIdMap = Maps.newHashMap();// 每个员工对应的返现总额
		List<AssignItemVo> assignList = Lists.newArrayList();
		for (BillSettleAccountsDto billSettleAccountsDto : setAccountVoList) {
			memberIdList.add(billSettleAccountsDto.getMemberId());
			AssignItemVo assignItemVo = new AssignItemVo();
			assignItemVo.setMemberId(billSettleAccountsDto.getMemberId());
			assignItemVo.setAmount(billSettleAccountsDto.getSettleAmount());
			assignList.add(assignItemVo);
			memberIdMap.put(billSettleAccountsDto.getMemberId(), billSettleAccountsDto.getCashBackAmount());
		}
		makeMoneySendSmsDto.setMemberIds(memberIdList);
		logger.info("给员工打款报销的钱跟员工对应值为:" + JSON.toJSONString(assignList));
		Map<String,Object> accMap=Maps.newHashMap();
		accMap.put("bill_number", billNo);
		accMap.put("del_flag", "0");
		List<BillSettleAccounts> accList=billSettleAccountsService.selectByMap(accMap);
		logger.info("查询账单下结算单信息为:" + JSON.toJSONString(accList));
		if (null == accList || accList.size() <= 0) {
			throw new BaoxiaoException("没有查到账单对应的结算单数据!");
		}
		List<String> reimburseCodeList=Lists.newArrayList();
		for (BillSettleAccounts billSettleAccounts : accList) {
			/*Order orderEntity=new Order();
			orderEntity.setSourceCode(billSettleAccounts.getSettleNo());
			orderEntity.setDelFlag("0");*/
			/*Map<String,Object> orderMap=Maps.newHashMap();
			orderMap.put("source_code", billSettleAccounts.getSettleNo());
			orderMap.put("del_flag", "0");
			List<Order> orderList=orderMapper.selectByMap(orderMap);
			for (Order order : orderList) {
				order.setStatus(OrderBusiStatus.PAYMENT_SUCCESS.getCateCode());
				orderMapper.updateById(order);
			}*/
			if("1".equals(billSettleAccounts.getSettleType())) {//简易报销
				reimburseCodeList.add(billSettleAccounts.getBillNo());
			}
		}
		
		
		AccountAssignDto accountAssignDto = new AccountAssignDto();
		accountAssignDto.setCompanyId(baseNew.getCompanyId());
		accountAssignDto.setBiz_id(billNo);
		accountAssignDto.setAct_type(DockEnum.actType.act_type_salary.getCode());
		if (assignList.size() > 0) {
			accountAssignDto.setAssign(assignList);
			logger.info("给员工打款报销的钱入参参数:" + JSON.toJSONString(accountAssignDto));
			R<Boolean> accountR = dockOpenPlatformService.accountAssignByCId(accountAssignDto);
			logger.info("给员工打款报销的钱返回值:" + JSON.toJSONString(accountR));
			
			BillBaseNewDto dto = new BillBaseNewDto();
			dto.setBusiCode(billNo);
			dto.setFlowName(BillPaymentFlow.MAKE_MONEY_REIMBURSE.getCateName());
			dto.setFlowNo(BillPaymentFlow.MAKE_MONEY_REIMBURSE.getCateCode()+"");
			dto.setFundFlowStatus(accountR.getData() ? "1" : "2");
			dto.setSort(BillPaymentFlow.MAKE_MONEY_REIMBURSE.getCateMgn()+"");
			dto.setBusiReturn(JSON.toJSONString(accountR));
			dto.setBusiCode(billNo);
			dto.setCompanyId(baseNew.getCompanyId());
			logger.info("账单打款记录给员工打报销的钱传入参数》》》》"+JSON.toJSONString(dto));
			addFundFlowRecord = this.addFundFlowRecord(dto);
			logger.info("账单打款记录给员工打报销的钱返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
			
			if (null == accountR || accountR.getCode() != R.SUCCESS) {
				throw new BaoxiaoException("给员工打报销款失败!");
			}
			for (BillSettleAccountsDto billSettleAccountsDto : setAccountVoList) {
				TbFundTradeFlow flowEntity=new TbFundTradeFlow();
				flowEntity.setSource("给员工打报销款");
				flowEntity.setTradeAmount(billSettleAccountsDto.getSettleAmount());
				flowEntity.setTradeBusiCode(billSettleAccountsDto.getMemberId());
				flowEntity.setTradeBusiCate(billNo);
				logger.info("添加给员工打报销款账单流水交易记录传入参数》》》》"+JSON.toJSONString(flowEntity));
				addTradeFlow(flowEntity);
			}
		}
		
		List<AssignItemVo> assignFXList = Lists.newArrayList();
		Set<Map.Entry<String, BigDecimal>> entrySet = memberIdMap.entrySet();
		Iterator<Map.Entry<String, BigDecimal>> iter = entrySet.iterator();
		BigDecimal fxSumAmount=new BigDecimal(0);
		while (iter.hasNext()) {
			Map.Entry<String, BigDecimal> entry = iter.next();
			logger.info("每个员工对应返现金额为:" + entry.getKey() + "," + entry.getValue());
			fxSumAmount=fxSumAmount.add(entry.getValue());
			if (entry.getValue().compareTo(new BigDecimal(0)) == 1) {
				AssignItemVo assignItemVo = new AssignItemVo();
				assignItemVo.setMemberId(entry.getKey());
				assignItemVo.setAmount(entry.getValue());
				assignFXList.add(assignItemVo);
			}
		}
		logger.info("给员工打款返现的钱对应员工值为:" + JSON.toJSONString(assignFXList));
		if (assignFXList.size() > 0) {
			accountAssignDto.setBiz_id(billNo + "_1");
			accountAssignDto.setAssign(assignFXList);
			logger.info("给员工打款返现的钱入参参数:" + JSON.toJSONString(accountAssignDto));
			R<Boolean> accountFxR = dockOpenPlatformService.accountAssignByCId(accountAssignDto);
			logger.info("给员工打款返现的钱返回值:" + JSON.toJSONString(accountFxR));
			
			BillBaseNewDto dto = new BillBaseNewDto();
			dto.setCompanyId(baseNew.getCompanyId());
			dto.setBusiCode(billNo);
			dto.setFlowName(BillPaymentFlow.MAKE_MONEY_CASH_RETURN.getCateName());
			dto.setFlowNo(BillPaymentFlow.MAKE_MONEY_CASH_RETURN.getCateCode()+"");
			dto.setFundFlowStatus(accountFxR.getData() ? "1" : "2");
			dto.setSort(BillPaymentFlow.MAKE_MONEY_CASH_RETURN.getCateMgn()+"");
			dto.setFlowCode(addFundFlowRecord.getFlowCode());
			dto.setMainId(addFundFlowRecord.getId());
			dto.setDetailId(addFundFlowRecord.getDetailId());
			dto.setBusiReturn(JSON.toJSONString(accountFxR));
			logger.info("账单打款记录给员工打返现的钱传入参数》》》》"+JSON.toJSONString(dto));
			addFundFlowRecord = this.addFundFlowRecord(dto);
			logger.info("账单打款记录给员工打返现的钱返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
			
			if (null == accountFxR || accountFxR.getCode() != R.SUCCESS) {
				throw new BaoxiaoException("给员工打返现款失败!");
			}
			while (iter.hasNext()) {
				Map.Entry<String, BigDecimal> entry = iter.next();
				logger.info("每个员工对应返现金额为:" + entry.getKey() + "," + entry.getValue());
				fxSumAmount=fxSumAmount.add(entry.getValue());
				if (entry.getValue().compareTo(new BigDecimal(0)) == 1) {
					addOrder(baseNew, entry.getKey(), entry.getValue());
					TbFundTradeFlow flowEntity=new TbFundTradeFlow();
					flowEntity.setSource("给员工打返现款");
					flowEntity.setTradeAmount(entry.getValue());
					flowEntity.setTradeBusiCode(entry.getKey());
					flowEntity.setTradeBusiCate(billNo + "_1");
					logger.info("添加给员工打返现款账单流水交易记录传入参数》》》》"+JSON.toJSONString(flowEntity));
					addTradeFlow(flowEntity);
				}
			}
		}

		/**打款成功发送短信通知   start  by zangjintian **/
		logger.info("给员工打款成功后批量发送短息请求参数，request param dto:"+ JSON.toJSONString(makeMoneySendSmsDto));
		boolean isOK = this.makeMoneyAfterSendSms(makeMoneySendSmsDto);
		logger.info("给员工打款成功后批量发送短息返回结果，return result isOK:"+ JSON.toJSONString(isOK));
		if(!isOK) {
			logger.error("给员工打款成功后批量发送短息异常，>>>-----error----->>>>返回结果 return result isOK:{}"+ isOK);
		}
		/**打款成功发送短信通知   end  by zangjintian **/
		
		// 账单总金额-所有员工报销金额加总-所有员工专票返现金额加总=需要砾洲打的钱
		BigDecimal companyAmount = baseNew.getBillAmount()
				.subtract(baseNew.getBillReimburseAmount())
				.subtract(baseNew.getBiilBuyAmount())
				.subtract(baseNew.getBillPaymentAmount())
				.subtract(fxSumAmount);
		logger.info("给砾洲打款金额为:" + companyAmount);
		if (companyAmount.compareTo(new BigDecimal(0)) == 1) {
			// 给砾洲企业打款
			R<TransferOrderVo> comR = dockOpenPlatformService.inTransferOrder(baseNew.getCompanyId(),
					AcctCateEnums.ACCT_CATE_PTLZ.getCateMgn(), billNo + "_2", companyAmount,
					DockEnum.actType.act_type_salary.getCode());
			logger.info("给砾洲打款返回值:" + JSON.toJSONString(comR));
			
			BillBaseNewDto dto = new BillBaseNewDto();
			dto.setCompanyId(baseNew.getCompanyId());
			dto.setBusiCode(billNo);
			dto.setFlowName(BillPaymentFlow.MAKE_MONEY_LIZHOU.getCateName());
			dto.setFlowNo(BillPaymentFlow.MAKE_MONEY_LIZHOU.getCateCode()+"");
			dto.setFundFlowStatus(comR.getCode() == R.SUCCESS? "1" : "2");
			dto.setSort(BillPaymentFlow.MAKE_MONEY_LIZHOU.getCateMgn()+"");
			dto.setFlowCode(addFundFlowRecord.getFlowCode());
			dto.setMainId(addFundFlowRecord.getId());
			dto.setDetailId(addFundFlowRecord.getDetailId());
			dto.setBusiReturn(JSON.toJSONString(comR));
			logger.info("账单打款记录给砾洲打款传入参数》》》》"+JSON.toJSONString(dto));
			addFundFlowRecord = this.addFundFlowRecord(dto);
			logger.info("账单打款记录给砾洲打款返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
			
			if (null == comR || comR.getCode() != R.SUCCESS) {
				throw new BaoxiaoException("给砾洲打款失败!");
			}
			TbFundTradeFlow flowEntity=new TbFundTradeFlow();
			flowEntity.setSource("给企业打款");
			flowEntity.setTradeAmount(companyAmount);
			flowEntity.setTradeBusiCode(AcctCateEnums.ACCT_CATE_PTLZ.getCateMgn());
			flowEntity.setTradeBusiCate(billNo + "_2");
			logger.info("添加给企业打款账单流水交易记录传入参数》》》》"+JSON.toJSONString(flowEntity));
			addTradeFlow(flowEntity);
		}

		
		//查询这个账单含有嘉白条的金额 按人分组 段吧
		R<List<BillBaseNewVo>> rJia=this.queryJiaStripQuota(billNo);
		if(null==rJia || R.SUCCESS!=rJia.getCode()) {
			logger.info("查询这个账单含有嘉白条的金额错误"+JSON.toJSONString(rJia));
			throw new BaoxiaoException("查询这个账单含有嘉白条的金额失败!");
		}
		//开始消费券额度解冻
		List<BillBaseNewVo> jiaOrder=this.baseMapper.queryZdOrder(billNo);
		if(!CollUtil.isEmpty(jiaOrder)) {
			for (BillBaseNewVo billBaseNewVo : jiaOrder) {
				logger.info("开始循环参数"+JSON.toJSONString(billBaseNewVo));
				String billNewNo=billBaseNewVo.getBillNo();	//购买单号
				logger.info("给员工扣嘉白条额度入参参数"+billBaseNewVo.getBillAmount()+"，"+billNewNo+","+billBaseNewVo.getMemberId()+","+billBaseNewVo.getCompanyId());
				DockR<DockChargebackRecordVo> btOpen=openJiaBaiTiaoBuyCouponService.chargeback(billBaseNewVo.getBillAmount(), billBaseNewVo.getMemberId(), 
						billBaseNewVo.getCompanyId(), billNewNo, paymentType.act_type_salary.getCode());
				logger.info("给员工扣嘉白条额度返回值"+JSON.toJSONString(btOpen));
				
				BillBaseNewDto dto = new BillBaseNewDto();
				dto.setCompanyId(baseNew.getCompanyId());
				dto.setMemberId(billBaseNewVo.getMemberId());
				dto.setBusiCode(billNo);
				dto.setFlowName(BillPaymentFlow.ZB_CHARGEBACK.getCateName());
				dto.setFlowNo(BillPaymentFlow.ZB_CHARGEBACK.getCateCode()+"");
				dto.setFundFlowStatus(btOpen.getCode().equals(DockR.SUCCESS) ? "1" : "2");
				dto.setSort(BillPaymentFlow.ZB_CHARGEBACK.getCateMgn()+"");
				dto.setFlowCode(addFundFlowRecord.getFlowCode());
				dto.setMainId(addFundFlowRecord.getId());
				dto.setDetailId(addFundFlowRecord.getDetailId());
				dto.setBusiReturn(JSON.toJSONString(btOpen));
				logger.info("账单打款记录调用总部扣款接口传入参数》》》》"+JSON.toJSONString(dto));
				addFundFlowRecord = this.addFundFlowRecord(dto);
				logger.info("账单打款记录调用总部扣款接口返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
				
				if(null == btOpen || !DockR.SUCCESS.equals(btOpen.getCode())) {
					throw new BaoxiaoException("给员工扣嘉白条额度失败!");
				}
				
				String tid=btOpen.getData().getTid();//充值流水号
				//嘉白条恢复额度
				logger.info("嘉白条恢复额度入参参数"+billBaseNewVo.getBillAmount()+"，"+billBaseNewVo.getJbtId()+","+billBaseNewVo.getMemberId()+","+billBaseNewVo.getCompanyId()+","+billBaseNewVo.getExtRefOrderId()+","+tid);
				DockR<JiaBaiTiaoReturnVo> hfR=openJiaBaiTiaoBuyCouponService.jiaBaiTiaoRepayment(billBaseNewVo.getBillAmount(), billBaseNewVo.getJbtId(), "02",billBaseNewVo.getExtRefOrderId(), tid, billBaseNewVo.getMemberId(), billBaseNewVo.getCompanyId());
				logger.info("嘉白条恢复额度返回值"+JSON.toJSONString(hfR));
				
				BillBaseNewDto jbtdto = new BillBaseNewDto();
				jbtdto.setCompanyId(baseNew.getCompanyId());
				jbtdto.setMemberId(billBaseNewVo.getMemberId());
				jbtdto.setBusiCode(billNo);
				jbtdto.setFlowName(BillPaymentFlow.JBT_RECOVER_AMOUNT.getCateName());
				jbtdto.setFlowNo(BillPaymentFlow.JBT_RECOVER_AMOUNT.getCateCode()+"");
				jbtdto.setFundFlowStatus(hfR.getCode().equals(DockR.SUCCESS) ? "1" : "2");
				jbtdto.setSort(BillPaymentFlow.JBT_RECOVER_AMOUNT.getCateMgn()+"");
				jbtdto.setFlowCode(addFundFlowRecord.getFlowCode());
				jbtdto.setMainId(addFundFlowRecord.getId());
				jbtdto.setDetailId(addFundFlowRecord.getDetailId());
				jbtdto.setBusiReturn(JSON.toJSONString(hfR));
				logger.info("账单打款记录嘉白条额度恢复传入参数》》》》"+JSON.toJSONString(jbtdto));
				addFundFlowRecord = this.addFundFlowRecord(jbtdto);
				logger.info("账单打款记录嘉白条额度恢复返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
				
				if(null == hfR || !DockR.SUCCESS.equals(hfR.getCode())) {
					throw new BaoxiaoException("给员工扣嘉白条额度失败!");
				}
				
				//解冻消费券额度
				freeCouponAmount(billBaseNewVo);
			}
		}

		BillBaseNew baseEntity = new BillBaseNew();
		baseEntity.setId(baseNew.getId());
		baseEntity.setStatus(BillStatus.TOBEINVOICE.getCateCode());
		baseEntity.setBillPayAmount(baseNew.getBillAmount());
		this.baseMapper.updateById(baseEntity);
		Map<String,Object> itemMap=Maps.newHashMap();
		itemMap.put("bill_no", billNo);
		itemMap.put("del_flag", "0");
		List<BillItemNew> itemList=billItemNewService.selectByMap(itemMap);
		//打款完毕之后给账单明细修改状态跟打款金额
		for (BillItemNew billItemNew : itemList) {
			BillItemNew updateItem=new BillItemNew();
			updateItem.setId(billItemNew.getId());
			updateItem.setStatus("2");
			updateItem.setBillLeftAmount(new BigDecimal(0));
			updateItem.setBillPayAmount(billItemNew.getBillAmount());
			billItemNewService.updateById(updateItem);
		}
		//给账单含有报销单子修改报销状态
		if(null!=reimburseCodeList && reimburseCodeList.size()>0) {
			ReimburseDTO reimburseDto=new ReimburseDTO();
			reimburseDto.setIdList(reimburseCodeList);
			R<Boolean> paySuccess = refactorInvoiceReimburseService.paySuccess(reimburseDto);
			
			BillBaseNewDto dto = new BillBaseNewDto();
			dto.setCompanyId(baseNew.getCompanyId());
			dto.setBusiCode(billNo);
			dto.setFlowName(BillPaymentFlow.UPDATE_REIMBURSE_STATUS.getCateName());
			dto.setFlowNo(BillPaymentFlow.UPDATE_REIMBURSE_STATUS.getCateCode()+"");
			dto.setFundFlowStatus(paySuccess.getData() ? "1" : "2");
			dto.setSort(BillPaymentFlow.UPDATE_REIMBURSE_STATUS.getCateMgn()+"");
			dto.setFlowCode(addFundFlowRecord.getFlowCode());
			dto.setMainId(addFundFlowRecord.getId());
			dto.setDetailId(addFundFlowRecord.getDetailId());
			dto.setBusiReturn(JSON.toJSONString(paySuccess));
			logger.info("账单打款记录修改报销单状态传入参数》》》》"+JSON.toJSONString(dto));
			addFundFlowRecord = this.addFundFlowRecord(dto);
			logger.info("账单打款记录修改报销单状态返回结果》》》》"+JSON.toJSONString(addFundFlowRecord));
			
			if (null == paySuccess || !paySuccess.getData()) {
				throw new BaoxiaoException("修改报销单状态失败!");
			}
		}
		
		
		for (BillSettleAccounts billSettleAccounts : accList) {
			billSettleAccounts.setStatus(billSettleAccountsStatus.STATUS_2.getCode());
			logger.info("修改结算单为已打款》》》》"+JSON.toJSONString(billSettleAccounts));
			billSettleAccountsService.updateById(billSettleAccounts);
		}
		logger.info("账单打款结束>>>>>>");
		return r;
	}
	
	public void freeCouponAmount(BillBaseNewVo billBaseNewVo) {
		List<AssignCouponDto> assignCouponDtoList = new ArrayList<AssignCouponDto>();
		AssignCouponDto couponDto= new AssignCouponDto();
		couponDto.setCouponId(billBaseNewVo.getCouponId());
		couponDto.setAmount(billBaseNewVo.getBillAmount());
		assignCouponDtoList.add(couponDto);
		AssignDto  mqDto= new AssignDto();
		mqDto.setOrderType(DictionaryEnum.OrderType.ORDER_TYPE_REIMBURSE.getCateCode());
		mqDto.setSource("自动购买券消费券额度解冻");
		mqDto.setBusiModel("*");
		mqDto.setFlowNo(CommonConstant.BUSI_FLOW_NO_REFUSE);
		mqDto.setMemberId(billBaseNewVo.getMemberId());
		mqDto.setCompanyId(billBaseNewVo.getCompanyId());
		mqDto.setBusiOrderNo(billBaseNewVo.getBillNo());
		mqDto.setOrderAmount(billBaseNewVo.getBillAmount());
		mqDto.setCouponAmount(billBaseNewVo.getBillAmount());
		mqDto.setCoupons(assignCouponDtoList);
		mqDto.setCanTicket(CommonConstant.STATUS_YES);
		logger.info("自动购买券消费券额度解冻入参"+JSON.toJSONString(mqDto));
		rabbitTemplate.convertAndSend(MqQueueConstant.FUND_EXCHANGE, MqQueueConstant.REIMBURSE_COUPON_TRANDE_TOPIC, mqDto);
	
	}
	public void addTradeFlow(TbFundTradeFlow flowEntity) {
		flowEntity.setBalanceCode("code");
		flowEntity.setBeginTime(new Date());
		flowEntity.setBillItemCate(BillItemSubCate.BILL__MAKE_AMOUNT.getCateCode());
		flowEntity.setBusiModel("NONE");
		flowEntity.setEndTime(new Date());
		flowEntity.setStatus("0");
		flowEntity.setTradeCate("DK");
		flowEntity.setTransCateName("账单打款");
		flowEntity.setTradeFlowCode(CodeUtils.genneratorShort("DK"));
		logger.info("账单打款记录值："+JSON.toJSONString(flowEntity));
		fundTradeFlowMapper.insert(flowEntity);
	}
	//返现订单
	public  boolean addOrder(BillBaseNew base,String memberId,BigDecimal fxAmount) throws Exception{
	 	 //添加订单
        OrderDTO order=new OrderDTO();
		String orderNo=CodeUtils.genneratorShort("FX");
		order.setOrderCode(orderNo);
		order.setOrderNo(orderNo);
		//order.setBusinessId(queryParams.getVendorId());
		//order.setBusinessName(applyInvoiceDto.getVendorName());
		order.setCompanyId(base.getCompanyId());
		order.setCompanyName(base.getCompanyName());
		order.setMainType(TransType.CASHBACK.getSysCode());
		order.setMainTypeName(TransType.CASHBACK.getSysName());
		order.setSubType(TransType.CASHBACK.getSysCode());
		order.setSubTypeName(TransType.CASHBACK.getSysName());
		order.setOrderAmount(fxAmount);
		//order.setPayAmount(applyInvoiceDto.getTaxIncludedPrice());
		order.setCouponAmount(new BigDecimal(0));
		order.setMemberId(memberId);
		order.setSourceType("1");
		order.setSourceCode(base.getBillNo());
		order.setStatus(OrderBusiStatus.PAYMENT_SUCCESS.getCateCode());
	
		logger.info("添加返现订单的入参参数:{}"+JSON.toJSONString(order));
		
		
		OrderDetailDTO detail=new OrderDetailDTO();

	    detail.setPaymentCate(BillItemSubCate.BILL_ITEM_SUBCATE_XFXJ.getCateCode());
	    detail.setPaymentAmount(fxAmount);
	    detail.setBusiModle(BusiModelEnums.BUSI_MODEL_NONE.getCateCode());
	    detail.setPaymentMemberCate(MemberCateEnums.MEMBER_CATE_EMP.getCateCode());
	    detail.setPaymentAcctCate(MemberCateEnums.MEMBER_CATE_EMP.getCateCode()+AcctCateEnums.ACCT_CATE_SALARY.getCateCode());
	    detail.setPaymentItemNo("");
	    detail.setPaymentItemName("");
	    detail.setStatus(OrderDetailStatus.SUCCESS.getCateCode());
	    detail.setPaymentMemberId(base.getCreator());

	    //detail.setPaymentVendorId(applyInvoiceDto.getVendorId());
	    //detail.setPaymentVendorName(applyInvoiceDto.getVendorName());
	    //detail.setBillNo(base.getId());//申请id
	    detail.setPaymentIndustryId("");//行业
	    detail.setPaymentIndustryName("");
	    logger.info("添加返现订单明细的入参参数:{}"+JSON.toJSONString(detail));
	    List<OrderDetailDTO> detailDtoList = Lists.newArrayList();

	    detailDtoList.add(detail);
	    order.setDetailDtoList(detailDtoList);
	    logger.info("添加所有订单明细入参参数为:{}"+JSON.toJSONString(order));
		iOrderService.addOrder(order);
		return true;
	}

	/**   
	 * <p>Title: invoicePhone</p>   
	 * <p>Description:发票图片查看 </p>   
	 * @param queryParams
	 * @return
	 * @throws Exception   
	 * @see com.taolue.baoxiao.fund.service.IBillBaseNewService#invoicePhone(com.taolue.baoxiao.fund.api.dto.ApplyInvoiceDetailDto)   
	 */  
	@Override
	public R<List<ApplyInvoiceDetailVo>> invoicePhone(ApplyInvoiceDetailDto queryParams) throws Exception {
		 logger.info("查询图片参数 》》》》》"+JSON.toJSONString(queryParams));
         List<ApplyInvoiceDetailVo> invoiceDetailList=invoiceApplyDetailMapper.invoicePhone(queryParams);
         logger.info("查询发票图片返回结果 》》》》》"+JSON.toJSONString(invoiceDetailList));
         if (null == invoiceDetailList || invoiceDetailList.size() <= 0) {
 			throw new BaoxiaoException("没有查到对应的发票图片!请查看原因");
 		}
		return new R<>(invoiceDetailList);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R<Boolean> toInvoice(BillBaseNew baseNew) throws Exception{
		R<Boolean> r=new R<Boolean>();
		loadInvoice(baseNew);
		return r;
	}

	@Override
	public R<Boolean> baseCheckInvoice() throws Exception{
		Map<String,Object> columnMap=Maps.newHashMap();
		columnMap.put("status", BillStatus.TOBEINVOICE.getCateCode());//待开票
		columnMap.put("del_flag","0");
		List<BillBaseNew> baseList=this.baseMapper.selectByMap(columnMap);
		logger.info("账单定时开票查询需要开票的账单信息为:"+JSON.toJSONString(baseList));
		for (BillBaseNew baseNew : baseList) {
			loadInvoice(baseNew);
		}
		return new R<>(true);
	}
	
	public void loadInvoice(BillBaseNew baseNew) throws Exception{
		FundBillInvoiceSettingDto settingDto = new FundBillInvoiceSettingDto();
		settingDto.setBillNo(baseNew.getBillNo());
		List<FundBillInvoiceSettingVo> settingVoList = fundBillInvoiceSettingMapper
				.findInvoiceSettingByBillNo(settingDto);
		logger.info("账单开票配置信息返回值:" + JSON.toJSONString(settingVoList));
		for (FundBillInvoiceSettingVo fundBillInvoiceSettingVo : settingVoList) {
			if ("2".equals(fundBillInvoiceSettingVo.getInvoiceForm())
					&& StringUtils.isNotEmpty(fundBillInvoiceSettingVo.getMainItemCode())) {// 如果是电子发票
				// 开票
				ApplyInvoiceDto applyInvoiceDto = new ApplyInvoiceDto();
				applyInvoiceDto.setCouponId(fundBillInvoiceSettingVo.getCouponId());
				applyInvoiceDto.setCouponName(fundBillInvoiceSettingVo.getCouponName());
				applyInvoiceDto.setItemCode(fundBillInvoiceSettingVo.getMainItemCode());
				applyInvoiceDto.setItemName(fundBillInvoiceSettingVo.getMainItemName());
				applyInvoiceDto.setSubItemCode(fundBillInvoiceSettingVo.getSubItemCode());
				applyInvoiceDto.setSubItemName(fundBillInvoiceSettingVo.getSubItemName());
				applyInvoiceDto.setInvoiceContents("*" + fundBillInvoiceSettingVo.getMainItemName() + "*"
						+ fundBillInvoiceSettingVo.getSubItemName());
				applyInvoiceDto.setTypeId(InvoiceTypeNew.E_INVOICE.getCateCode());
				applyInvoiceDto.setTypeName(InvoiceTypeNew.E_INVOICE.getCateName());
				applyInvoiceDto.setBuyerId(baseNew.getCompanyId());
				applyInvoiceDto.setBuyerName(baseNew.getCompanyName());
				applyInvoiceDto.setMailBox("shenjinquan@jia-fu.cn");
				MemberCompanyVo companyVo = refactorMemberCompanyService.get(baseNew.getCompanyId());
				logger.info("查询当前登录企业的信息返回值:" + JSON.toJSONString(companyVo));
				if (!ObjectUtils.isEmpty(companyVo)) {
					applyInvoiceDto.setBuyerAddr(companyVo.getAddress());
					applyInvoiceDto.setBuyerNumber(companyVo.getIdentificationNumber());
				}
				applyInvoiceDto.setVendorId(fundBillInvoiceSettingVo.getVendorId());
				applyInvoiceDto.setVendorName(fundBillInvoiceSettingVo.getVendorName());
				applyInvoiceDto.setRate(fundBillInvoiceSettingVo.getTaxPoint());
				applyInvoiceDto.setTaxIncludedPrice(fundBillInvoiceSettingVo.getInvoiceAmount());
				R<ApplyInvoiceVo> invoiceR = applyInvoiceService.billInvoice(applyInvoiceDto);
				logger.info("开票返回值:" + JSON.toJSONString(invoiceR));
				if (null == invoiceR || invoiceR.getCode() != R.SUCCESS) {
					throw new BaoxiaoException("开票失败!");
				}
				String invoiceCode = invoiceR.getData().getInvoiceApplyCode();
				FundBillInvoiceSetting setting = new FundBillInvoiceSetting();
				setting.setId(fundBillInvoiceSettingVo.getId());
				setting.setInvoiceStatus(InvoiceApplyStatus.INVOICE_SUCCESS.getCateCode());
				setting.setInvoiceCode(invoiceCode);
				
				fundBillInvoiceSettingMapper.updateById(setting);
			}

		}
		BillBaseNew baseEntity = new BillBaseNew();
		baseEntity.setId(baseNew.getId());
		baseEntity.setStatus(BillStatus.SETTLED.getCateCode());
		baseEntity.setBillPayAmount(baseNew.getBillAmount());
		this.baseMapper.updateById(baseEntity);
	}

	
	public FundCouponTaxCode findVendorIdNameByCouponId(String couponId) {
		if(StringUtils.isNotBlank(couponId)) {
			Wrapper<FundCouponTaxCode> wrapper = new EntityWrapper<>();
			wrapper.eq("coupon_id", couponId);
			wrapper.eq("del_flag", CommonConstant.STATUS_NORMAL);
			List<FundCouponTaxCode> selectList = fundCouponTaxCodeMapper.selectList(wrapper);
			logger.info("通过券id去tb_fund_coupon_tax_code 查询商户信息返回结果，return result list:"+JSON.toJSON(selectList));
			if(CollUtil.isEmpty(selectList)) {
				logger.info("通过券id去tb_fund_coupon_tax_code 查询商户信息返回结果，return result list is null");
				return null;
			}
			FundCouponTaxCode fundCouponTaxCode = selectList.get(0);
			return fundCouponTaxCode;
		}
		return null;
	}

	@Override
	public R<List<BillBaseNewVo>> queryBaseSummary() {
		logger.info("开始账单汇总查询>>>>>>>>>>>");
		List<BillBaseNewVo> baseList=this.baseMapper.queryBaseSummary();
		logger.info("账单汇总返回值:"+JSON.toJSONString(baseList));
		return new R<>(baseList);
	}

	@Override
	public R<List<FundBillInvoiceSettingVo>> queryInvoiceApplySummary() {
		logger.info("开始发票申请汇总查询>>>>>>>>>>>");
		List<FundBillInvoiceSettingVo> setList=this.baseMapper.queryInvoiceApplySummary();
		List<FundBillInvoiceSettingVo> newSetList=Lists.newArrayList();
		for (FundBillInvoiceSettingVo fundBillInvoiceSettingVo : setList) {
			if(StringUtils.isNotBlank(fundBillInvoiceSettingVo.getInvoiceCode())) {
				logger.info("发票申请汇总需要查询开票下载地址的invoiceCode:"+fundBillInvoiceSettingVo.getInvoiceCode());
				Map<String,Object> invoDetailMap=Maps.newHashMap();
				invoDetailMap.put("invoice_apply_code", fundBillInvoiceSettingVo.getInvoiceCode());
				List<InvoiceApplyDetail> invoiceDetailList=invoiceApplyDetailService.selectByMap(invoDetailMap);
				logger.info("发票申请汇总需要查询开票下载地址返回值:"+JSON.toJSONString(invoiceDetailList));
				List<String> strList=Lists.newArrayList();
				for (InvoiceApplyDetail detail : invoiceDetailList) {
					if(detail.getInvoiceStatus().equals("2")) {//开票成功
						strList.add(detail.getPicUrl());
					}
				}
				fundBillInvoiceSettingVo.setUrlList(strList);
			}
			newSetList.add(fundBillInvoiceSettingVo);
			
		}
		logger.info("查询发票申请汇总最终结果为:"+JSON.toJSONString(newSetList));

		return new R<>(newSetList);
	}
	/**
	 * 
	 * 
	 * @Title BillBaseNewServiceImpl.makeMoneySendSms
	 * @Description: 打款成功后给员工发送短信通知
	 *
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年07月30日 上午11:13:52
	 * 修改内容 :
	 * @throws Exception 
	 */
	public boolean makeMoneyAfterSendSms(MakeMoneySendSmsDto sendSmsDto) throws Exception {
		List<SmsDto> smsDtoList = Lists.newArrayList();
		logger.info("打款成功后发送短信通知用户，请求参数 request param sendSmsDto:"+ JSON.toJSON(sendSmsDto));
		if(ObjectUtil.isNull(sendSmsDto)) {
			logger.error("参数不能为空");
			return false;
		}
		if(StringUtils.isEmpty(sendSmsDto.getCompanyId())) {
			logger.error("参数-公司id不能为空");
			return false;
		}
		if(StringUtils.isEmpty(sendSmsDto.getCompanyName())) {
			logger.error("参数-公司名称不能为空");
			return false;
		}
		List<String> memberIds = sendSmsDto.getMemberIds();
		if(CollUtil.isEmpty(memberIds)) {
			logger.error("参数-员工id标识集合不能为空");
			return false;
		}
		for (String memberId : memberIds) {
			if(StringUtils.isEmpty(memberId)) {
				logger.error("参数-员工id标识不能为空");
				return false;
			}
		}
		String[] ids = memberIds.toArray(new String[memberIds.size()]);
		logger.info("打款接口通过memberId集合查询realName,fund服务开始调用member服务开始》》请求参数 request param ids:"+JSON.toJSON(ids));
		List<PlatformVo> platformList = refactorMemberPlatformService.queryMemberListByIds(ids);
		logger.info("打款接口通过memberId集合查询realName,fund服务调用member服务结束》》返回结果 return result platformList:"+JSON.toJSON(platformList));
		for (PlatformVo platformVo : platformList) {
			for (String memberId : memberIds) {
				if(platformVo.getId().equals(memberId) &&
						platformVo.getCompanyId().equals(sendSmsDto.getCompanyId())) {
					SmsDto smsDto = new SmsDto();
					Map<String,Object> map = Maps.newHashMap();
					map.put("realName", platformVo.getRealName());
					map.put("companyName", sendSmsDto.getCompanyName());
					smsDto.setSmsCode("SMS0032");
					smsDto.setChannel(SmsChannelType.SMS_CHANNEL_TYPE_JF.getCateCode());
					smsDto.setType(DictionaryEnum.SmsType.SMS_TYPE_YY.getCateCode());
					smsDto.setPhone(platformVo.getMobile());
					smsDto.setMsg(map);
					smsDtoList.add(smsDto);
					
				}
			}
		}
		/*for (Map map : mapList) {
			for (Map map2 : mapList1) {
				if(map2.get("phoneAndRealName").equals(map.get("phoneAndRealName"))) {
					
				}else {
					SmsDto smsDto = new SmsDto();
					Map<String,Object> map1 = Maps.newHashMap();
					map1.put("realName", map.get("realName"));
					map1.put("companyName", map.get("companyName"));
					smsDto.setMsg(map1);
					smsDto.setSmsCode(map.get("smsCode").toString());
					smsDto.setChannel(Integer.parseInt(map.get("channel").toString()));
					smsDto.setType(Integer.parseInt(map.get("type").toString()));
					smsDto.setPhone(map.get("phone").toString());
					smsDtoList.add(smsDto);
				}
			}
			
		}*/
		logger.info("打款成功后发送短信通知用户，fund服务开始调用dock服务开始》》请求参数 request param smsDtoList:"+ JSON.toJSON(smsDtoList));
		Boolean isOK = dockOpenPlatformService.sendSmsBatch(smsDtoList);
		logger.info("打款成功后发送短信通知用户，fund服务调用dock服务结束》》返回结果 return result isOK:"+ JSON.toJSON(isOK));
		if(!isOK) {
			logger.error("给员工打款成功后批量发送短信异常》》调用dock服务返回失败》》");
		}
		return isOK;
	}

	@Override
	public R<List<BillBaseNewVo>> queryJiaStripQuota(String billNo) {
		logger.info("根据账单号查询账单中嘉白条额度入参参数:"+JSON.toJSONString(billNo));
    	R<List<BillBaseNewVo>> r = new R<List<BillBaseNewVo>>();
    	List<BillBaseNewVo> billVo = this.baseMapper.queryJiaStripQuota(billNo);
    	if(ObjectUtil.isNotNull(billVo)) {
    		r.setData(billVo);
    	}
    	logger.info("根据账单号查询账单中嘉白条额度返回参数:"+JSON.toJSONString(r));
    	return r;
   }
	/**   
	 * <p>Title: queryBillBase</p>   
	 * <p>Description:运营平台账单导出（禁止删除和修改） </p>   
	 * @param queryParams
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.IBillBaseNewService#queryBillBase(com.taolue.baoxiao.fund.api.dto.BillBaseNewDto)   
	 */  
	@Override
	public R<List<BillBaseNewVo>> queryBillBase(BillBaseNewDto queryParams) {
		logger.info("账单列表的入参参数"+JSON.toJSONString(queryParams));
		List<BillBaseNewVo> baseList=this.baseMapper.queryBillBase(queryParams);
		logger.info("账单列表结果为:"+JSON.toJSONString(baseList));
		return new R<>(baseList);
	}

	private FundFlowRecordVo addFundFlowRecord(BillBaseNewDto dto) {
		FundFlowRecordVo fundVo = new FundFlowRecordVo();
		fundVo.setFlowName(dto.getFlowName());
		fundVo.setFlowNo(dto.getFlowNo());
		fundVo.setStatus(dto.getFundFlowStatus());
		fundVo.setSort(dto.getSort());
		if(StringUtils.isNotBlank(dto.getBusiCode())) {
			fundVo.setBusiCode(dto.getBusiCode());
		}
		if(StringUtils.isNotBlank(dto.getFlowCode())) {
			fundVo.setFlowCode(dto.getFlowCode());
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
		fundVo.setBusiParams(JSON.toJSONString(dto));
		FundFlowRecordVo isture = fundFlowRecordService.addFundFlowRecord(fundVo);
		return isture;
	}
}
