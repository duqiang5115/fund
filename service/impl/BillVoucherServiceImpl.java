package com.taolue.baoxiao.fund.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.Condition;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.fund.api.vo.BillVoucherComposeVo;
import com.taolue.baoxiao.fund.api.vo.BillVoucherFilesVo;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.entity.BillBase;
import com.taolue.baoxiao.fund.entity.BillItem;
import com.taolue.baoxiao.fund.entity.BillVoucher;
import com.taolue.baoxiao.fund.entity.BillVoucherFiles;
import com.taolue.baoxiao.fund.entity.OrderPayment;
import com.taolue.baoxiao.fund.factory.IBusinessFlowHandle;
import com.taolue.baoxiao.fund.factory.IBusinessFlowHandleFactory;
import com.taolue.baoxiao.fund.mapper.BillVoucherMapper;
import com.taolue.baoxiao.fund.service.IBillBaseService;
import com.taolue.baoxiao.fund.service.IBillItemService;
import com.taolue.baoxiao.fund.service.IBillVoucherFilesService;
import com.taolue.baoxiao.fund.service.IBillVoucherService;
import com.taolue.baoxiao.fund.service.IOrderPaymentService;

import ch.qos.logback.classic.Logger;
import cn.hutool.core.bean.copier.BeanCopier;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 账单明细支付信息，针对某一账单项目，缴费的记录；目前此表只做记录，并不进行入账和对账 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2018-08-24
 */
@Service
@Slf4j
public class BillVoucherServiceImpl extends ServiceImpl<BillVoucherMapper, BillVoucher> implements IBillVoucherService {

	@Autowired
	private IBillVoucherFilesService billVoucherFilesService;
	
	@Autowired
	private IBillItemService billItemService;
	
	@Autowired
	private IBillBaseService billBaseService;
	
	@Autowired
	private IOrderPaymentService orderPaymentService;
	
	@Autowired
	private IBusinessFlowHandleFactory businessFlowHandleFactory;
	
//	private static final String BUSI_MODEL_YCCM_FLOW_NO = "CM10006JQ";
//	
//	private static final String BUSI_MODEL_HFCM_FLOW_NO = "CM10007JQ";
	
	private static final String FLOW_NO_BILLCLEAR = "BILLCLEAR";
	
	/**   
	 * <p>Title: addelVouchers</p>   
	 * <p>Description: </p>   
	 * @param vouchers
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.IBillVoucherService#addelVouchers(java.util.List)   
	 */  
	@Override
	@Transactional
	public List<BillVoucher> addelVouchers(List<BillVoucherComposeVo> vouchers) {
		 List<BillVoucher> billVouchers = Lists.newArrayList();
		if (CollUtil.isNotEmpty(vouchers)) {
			for (BillVoucherComposeVo voucherCompose : vouchers) {
				
				//存在id和voucherno 则为删除
				if (StrUtil.isNotBlank(voucherCompose.getId())) {
					
					BeanCopier<BillVoucher> copier = new BeanCopier<BillVoucher>(voucherCompose, new BillVoucher(), 
							new CopyOptions());
					BillVoucher delVoucher = copier.copy();
					
					BillItem billItem = findBillItem(delVoucher.getBillItemNo());
					BillBase billBase = findBillBase(billItem.getBillNo());
					
					boolean  isClear = CommonConstant.BILL_ITEM_STATUS_PAYFULL.equals(billItem.getStatus());
					boolean  isInvoice = CommonConstant.BILL_VOUCHER_TYPE_INVOIC.equals(delVoucher.getVoucherCate());
					
					if (isInvoice || (!isInvoice && !isClear)) {
						BillVoucherFiles voucherFileEntity = new BillVoucherFiles();
						voucherFileEntity.setDelFlag(CommonConstant.STATUS_DEL);
						
						EntityWrapper<BillVoucherFiles> voucherFileWrapper = new EntityWrapper<>();
						voucherFileWrapper.eq("voucher_no", voucherCompose.getBillVoucherNo());
						
						billVoucherFilesService.update(voucherFileEntity, voucherFileWrapper);
						
						BigDecimal voucherAmount = delVoucher.getVoucherAmount();
						
						if (null == delVoucher.getVoucherAmount()) {
							voucherAmount = CommonConstant.NO_AMOUNT;
						}
						
						delVoucher.setVoucherAmount(voucherAmount.multiply(new BigDecimal(-1)));
						delVoucher.setDelFlag(CommonConstant.STATUS_DEL);
						log.info("删除的voucher={}",delVoucher);
						
						delVoucher.updateById();
						
						if (CommonConstant.BILL_VOUCHER_TYPE_PAYIN.equals(delVoucher.getVoucherCate())) {
							
							billItem.setBillPayAmount(billItem.getBillPayAmount().add(delVoucher.getVoucherAmount()));
							billItem.setBillLeftAmount(billItem.getBillLeftAmount().subtract(delVoucher.getVoucherAmount()));
							
							billBase.setBillPayAmount(billBase.getBillPayAmount().add(delVoucher.getVoucherAmount()));
							
							if (billItem.getBillPayAmount().compareTo(CommonConstant.NO_AMOUNT)==0) {
								billItem.setStatus(CommonConstant.BILL_ITEM_STATUS_INIT);
							} else {
								billItem.setStatus(CommonConstant.BILL_ITEM_STATUS_PAYPARTY);
							}
							
							if (billBase.getBillPayAmount().compareTo(CommonConstant.NO_AMOUNT)==0) {
								billBase.setStatus(CommonConstant.BILL_BASE_STATUS_INIT);
							} else {
								billBase.setStatus(CommonConstant.BILL_BASE_STATUS_PART);
							}
							
							billItem.updateById();
							billBase.updateById();
						}
					}
					
				//否则为新增
				} else {
					//新增voucher
					BeanCopier<BillVoucher> voucherCopyer = new BeanCopier<BillVoucher>(voucherCompose, new BillVoucher(), new CopyOptions(null,true,new String[] {"billVoucherFiles"}));
					BillVoucher voucher = voucherCopyer.copy();
					voucher.setBillVoucherNo(CommonConstant.KEY_BILL_VOUCHE_NO+IdWorker.getIdStr());
					if (CommonConstant.BILL_VOUCHER_TYPE_PAYIN.equals(voucher.getVoucherCate())) {
						voucher.setStatus(CommonConstant.BILL_VOUCHER_STATUS_BILLDING);
					} else {
						voucher.setStatus(CommonConstant.BILL_VOUCHER_STATUS_BILLLESS);
					}
					this.insert(voucher);
					
					//新增voucherFiles
					if (CollUtil.isNotEmpty(voucherCompose.getBillVoucherFiles())) {
						for (BillVoucherFilesVo voucherFileVo : voucherCompose.getBillVoucherFiles()) {
							BeanCopier<BillVoucherFiles> voucherFileCopyer = new BeanCopier<BillVoucherFiles>(voucherFileVo, new BillVoucherFiles(), new CopyOptions());
							BillVoucherFiles voucherFile = voucherFileCopyer.copy();
							voucherFile.setVoucherNo(voucher.getBillVoucherNo());
							voucherFile.setVoucherFileNo(CommonConstant.KEY_BILL_VOUCHE_FILE_NO+IdWorker.getIdStr());
							this.billVoucherFilesService.insert(voucherFile);
						}
					}
					billVouchers.add(voucher);
				}
			}
			
		}
		return billVouchers;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public void calculateBill(String billItemNo, BigDecimal payAmount) {
		BigDecimal totalPayAmount = CommonConstant.NO_AMOUNT;
		
		BillItem billItem = findBillItem(billItemNo);
		
		BillBase billBase = findBillBase(billItem.getBillNo());
		
		Map<String, Object> paymentSum = this.selectMap(Condition.create()
				.setSqlSelect("SUM(voucher_amount) as voucher_amount ")
				.where("del_flag='0'").or().where("del_flag='1'").andNew("bill_item_no={0}", billItemNo)
				.eq("voucher_cate", CommonConstant.BILL_VOUCHER_TYPE_PAYIN)
				);
		
		if (CollUtil.isNotEmpty(paymentSum)) {
			Object amount = paymentSum.get("voucher_amount");
			if (null != amount) {
				totalPayAmount = (BigDecimal)amount;
			}
		}
		log.info("账单项目收款总金额，totalPayAmount={}",totalPayAmount);
		
		billItem.setBillPayAmount(billItem.getBillPayAmount().add(payAmount));
		billItem.setBillLeftAmount(billItem.getBillLeftAmount().subtract(payAmount));
		billItem.setStatus(CommonConstant.BILL_ITEM_STATUS_PAYPARTY);
		
		billBase.setBillPayAmount(billBase.getBillPayAmount().add(payAmount));
		
		BigDecimal billAmount = CommonConstant.NO_AMOUNT;
		billAmount = billItem.getBillAmount();
		String billItemCate = billItem.getBillItemCate();
		
		String internalVendorId = billItem.getInternalVendorId();
		String vendorId = billItem.getBillItemKind();
		String usedVendorId = vendorId;
		if (StrUtil.isNotBlank(internalVendorId)) {
			usedVendorId = internalVendorId;
		}
		
		String businessModel = billBase.getBusiModle();
		String companyId = billBase.getCompanyId();
		
		//账单科目结清处理
		if (totalPayAmount.compareTo(billAmount)>=0) {
			billItem.setStatus(CommonConstant.BILL_ITEM_STATUS_PAYFULL);
			if(businessModel.equals(BusiModelEnums.BUSI_MODEL_HFPE.getCateCode())) {
				
				
				log.info("开始进入后付费个人账单冲账》》》》");
				
				Map<String,Object> columnMap=Maps.newHashMap();
				columnMap.put("billNo", billItem.getBillNo());
				columnMap.put("busiType", "GR");
				//通过账单号查询可以冲账的账单明细，针对后付费个人
				List<BillItem> itemList=billItemService.selectBillItemByBillNo(columnMap);
				for (BillItem billItem2 : itemList) {
					log.info("开始进入后付费个人账单冲账》》》》明细>>>"+billItem2.getBillItemNo()+","+billItem2.getBillItemCateName());
					String internalVendorId1 = billItem2.getInternalVendorId();
					String vendorId1 = billItem2.getBillItemKind();
					String usedVendorId1 = vendorId1;
					if (StrUtil.isNotBlank(internalVendorId1)) {
						usedVendorId1 = internalVendorId1;
					}
					clearBillItem(billItem2.getBillItemCate(), billItem2.getBillItemNo(), companyId, usedVendorId1, billItem2.getBillAmount(),businessModel);
					billItem2.setStatus(CommonConstant.BILL_ITEM_STATUS_PAYFULL);
					billItem2.setBillPayAmount(billItem2.getBillAmount());
					billItem2.setBillLeftAmount(new BigDecimal(0));
					log.info("开始进入后付费个人账单冲账》》》》明细结束>>>"+JSON.toJSONString(billItem2));
					billItem2.updateById();
				}
			}else if(businessModel.equals(BusiModelEnums.BUSI_MODEL_HFCM.getCateCode()))  {
				log.info("开始进入后付费企业账单冲账》》》》");
				if(billItem.getBillItemCate().equals(BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode())) {
					log.info("开始进入后付费企业账单冲账开始商户》》》》");
					clearBillItem(billItemCate, billItemNo, companyId, usedVendorId, totalPayAmount,businessModel);
				}else {
					log.info("开始进入后付费企业账单冲账开始砾洲平台》》》》");
					Map<String,Object> columnMap=Maps.newHashMap();
					columnMap.put("billNo", billItem.getBillNo());
					columnMap.put("busiType", "QY");
					//通过账单号查询可以冲账的账单明细，针对后付费个人
					List<BillItem> itemList=billItemService.selectBillItemByBillNo(columnMap);
					for (BillItem billItem2 : itemList) {
						log.info("开始进入后付费企业账单冲账》》》》明细>>>"+billItem2.getBillItemNo()+","+billItem2.getBillItemCateName());
						String internalVendorId1 = billItem2.getInternalVendorId();
						String vendorId1 = billItem2.getBillItemKind();
						String usedVendorId1 = vendorId1;
						if (StrUtil.isNotBlank(internalVendorId1)) {
							usedVendorId1 = internalVendorId1;
						}
						clearBillItem(billItem2.getBillItemCate(), billItem2.getBillItemNo(), companyId, usedVendorId1, billItem2.getBillAmount(),businessModel);
						billItem2.setStatus(CommonConstant.BILL_ITEM_STATUS_PAYFULL);
						billItem2.setBillPayAmount(billItem2.getBillAmount());
						billItem2.setBillLeftAmount(new BigDecimal(0));
						log.info("开始进入后付费企业账单冲账》》》》明细结束>>>"+JSON.toJSONString(billItem2));
						billItem2.updateById();
					}
				}
				
			
			}else {
				clearBillItem(billItemCate, billItemNo, companyId, usedVendorId, totalPayAmount,businessModel);
			}
		}
		log.info("开始更新账单项目billItem={}",billItem);
		billItem.updateById();
		
		EntityWrapper<BillItem> wrapperItem = new EntityWrapper<>();
		wrapperItem.eq("bill_no", billBase.getBillNo());
		wrapperItem.gt("bill_left_amount", CommonConstant.NO_AMOUNT);
		
		String itemCate=BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode()+","+BillItemSubCate.BILL_ITEM_SUBCATE_DZSV.getCateCode()
		+","+BillItemSubCate.BILL_ITEM_SUBCATE_YQSV.getCateCode()+","+BillItemSubCate.BILL_ITEM_SUBCATE_XFBX.getCateCode();
		wrapperItem.in("bill_item_cate", itemCate.split(","));
		//wrapperItem.ne("bill_item_cate", BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode());
		wrapperItem.gt("bill_amount", CommonConstant.NO_AMOUNT);
	
		//wrapperItem.ne("bill_item_cate", BillItemSubCate.BILL_ITEM_SUBCATE_DZSV.getCateCode());
		
		//wrapperItem.ne("bill_item_cate", BillItemSubCate.BILL_ITEM_SUBCATE_YQSV.getCateCode());
		//wrapperItem.ne("bill_item_cate", BillItemSubCate.BILL_ITEM_SUBCATE_XFBX.getCateCode());
		wrapperItem.andNew()
		.isNull("internal_vendor_id")
		.or()
		.eq("internal_vendor_id", "");
		
		int count = billItemService.selectCount(wrapperItem);
		log.info("未结清账单科目数量 noclearBillItemCount={}",count);
		
		//已经结清
		if (count<=0) {
			if (CommonConstant.BILL_BASE_STATUS_OVERDUE.equals(billBase.getStatus())) {
				billBase.setStatus(CommonConstant.BILL_BASE_STATUS_OVCLEAR);
			} else {
				billBase.setStatus(CommonConstant.BILL_BASE_STATUS_CLEAR);
			}
			
			//账单结清处理
			wrapperItem = new EntityWrapper<>();
			String billNo = billBase.getBillNo();
			wrapperItem.eq("bill_no", billBase.getBillNo());
			wrapperItem.le("bill_left_amount", 0);
			List<BillItem> items = billItemService.selectList(wrapperItem);
			
			if (CollUtil.isNotEmpty(items)) {
				log.info("账单所有账单项目，BillItems={}",JSON.toJSONString(items));
				BigDecimal totalBillAmount = CommonConstant.NO_AMOUNT;
				
				//计算消费场景的账单项目总额
				for (BillItem item : items) {
					
					//if (BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode().equals(item.getBillItemCate())) {
						log.info("账单消费项目，BillItems={}",JSON.toJSONString(item));
						totalBillAmount = totalBillAmount.add(item.getBillLeftAmount());
					//}
				}
				log.info("账单结清流水，参数totalBillAmount={}", totalBillAmount);
				if (totalBillAmount.compareTo(CommonConstant.NO_AMOUNT)==0) {
					BigDecimal xzAmount =billBase.getBillPaymentAmount().add(billBase.getBillReimburseAmount());
					log.info("账单未结清金额开始账单销账"+xzAmount);
					clearBill(billNo, companyId, businessModel, xzAmount);
				}
			}
			
		} else {
			if (CommonConstant.BILL_BASE_STATUS_INIT.equals(billBase.getStatus())) {
				billBase.setStatus(CommonConstant.BILL_BASE_STATUS_PART);
			}
		}
		
		updateBillInfo(billBase, billItem, count<=0);
	}

	private BillItem findBillItem(String billItemNo) {
		EntityWrapper<BillItem> wrapper = new EntityWrapper<>();
		wrapper.eq("bill_item_no", billItemNo);
		BillItem billItem = billItemService.selectOne(wrapper);
		if (billItem == null) {
			FundServiceExceptionGenerator.FundServiceException("9080",billItemNo);
		}
		log.info("销账账单项目，BillItem={}",JSON.toJSONString(billItem));
		return billItem;
	}
	
	private BillBase findBillBase(String billNo) {
		EntityWrapper<BillBase> wrapperBase = new EntityWrapper<>();
		wrapperBase.eq("bill_no", billNo);
		BillBase billBase = billBaseService.selectOne(wrapperBase);
		if (billBase == null) {
			FundServiceExceptionGenerator.FundServiceException("9081",billNo);
		}
		log.info("销账账单，BillBase={}",JSON.toJSONString(billBase));
		return billBase;
	}
	private void clearBill(String billNo, String companyId, String businessModel,
			BigDecimal totalBillAmount) {
		Map<String, Object> handleParams = Maps.newHashMap();
		//订单类型
	    handleParams.put("orderType", OrderType.ORDER_TYPE_CROSSOFF.getCateCode());
	    //业务模式
	    handleParams.put("busiModel", businessModel);
	    //流程阶段
	    handleParams.put("flowNo", FLOW_NO_BILLCLEAR);
	    //请求来源
	    handleParams.put("source", "system");
	    //订单编码
	    handleParams.put("busiOrderNo", billNo);
	    
	    //交易主体
	    Map<String,String> memberIds = Maps.newConcurrentMap();
	    memberIds.put(MemberCateEnums.MEMBER_CATE_CMP.getCateCode(), companyId);
	    handleParams.put("totalPayAmount", totalBillAmount);
	    
	    handleParams.put("MEMBER_CATE_MEMBERID", memberIds);
	    log.info("开始记录账单结清流水，参数handleParams=", JSON.toJSONString(handleParams));
	    IBusinessFlowHandle handle = businessFlowHandleFactory.getBusinessFlowHandle(OrderType.ORDER_TYPE_CROSSOFF.getCateCode());
	    handle.handleBusiFlow(handleParams);
	}
	
	private void clearBillItem(String billItemCate, String billItemNo, String companyId, String usedVendorId,
			BigDecimal totalPayAmount,String businessModel) {
		Map<String, Object> handleParams = Maps.newHashMap();
		//订单类型
	    handleParams.put("orderType", OrderType.ORDER_TYPE_CROSSOFF.getCateCode());
	    //后付费个人传模式，消费金额需要打款给平台
	    if(businessModel.equals(BusiModelEnums.BUSI_MODEL_HFPE.getCateCode()) && BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode().equals(billItemCate)) {
	    	   handleParams.put("busiModel", businessModel);
	    }else {
	    	 //业务模式，传NONE。消费金额打到商户
		    handleParams.put("busiModel", CommonConstant.BALANCE_BUSI_MODEL_NONE);
	    }
	 
	    //流程阶段
	    handleParams.put("flowNo", billItemCate);
	    
	    //请求来源
	    handleParams.put("source", "system");
	    //订单编码
	    handleParams.put("busiOrderNo", billItemNo);
	    
	    //交易主体
	    Map<String,String> memberIds = Maps.newConcurrentMap();
	    if (BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode().equals(billItemCate)) {
	    	memberIds.put(MemberCateEnums.MEMBER_CATE_CMP.getCateCode(), companyId);
	    	memberIds.put(MemberCateEnums.MEMBER_CATE_SUP.getCateCode(), usedVendorId);
	    }
	    
	    if (BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode().equals(billItemCate)
	    		|| BillItemSubCate.BILL_ITEM_SUBCATE_DZSV.getCateCode().equals(billItemCate) 
	    		|| BillItemSubCate.BILL_ITEM_SUBCATE_XFBX.getCateCode().equals(billItemCate) ) {
	    	memberIds.put(MemberCateEnums.MEMBER_CATE_CMP.getCateCode(), companyId);
	    	memberIds.put(MemberCateEnums.MEMBER_CATE_PT.getCateCode(), CommonConstant.PLANTFORM_ACCT_MEMBER_ID);
	    }
	    
	    handleParams.put("totalPayAmount", totalPayAmount);
	    
	    handleParams.put("MEMBER_CATE_MEMBERID", memberIds);
	    
	    log.info("开始记录账单项目结清流水，参数handleParams=", JSON.toJSONString(handleParams));
	    IBusinessFlowHandle handle = businessFlowHandleFactory.getBusinessFlowHandle(OrderType.ORDER_TYPE_CROSSOFF.getCateCode());
	    handle.handleBusiFlow(handleParams);
	}

	private void updateOrderPaymentTicketStatus(String billNo) {
		log.info("开始更新订单billNo={}",billNo);
		EntityWrapper<OrderPayment> wrapperOrderPay = new EntityWrapper<>();
		wrapperOrderPay.eq("bill_no", billNo);
		OrderPayment opm = new OrderPayment();
		opm.setStatus(CommonConstant.ORDER_PAYMENT_STATUS_TICKETED);
		this.orderPaymentService.update(opm, wrapperOrderPay);
	}
	
	private void updateBillInfo(BillBase billBase, BillItem billItem, boolean isClear) {
		
		if (isClear) {
			updateOrderPaymentTicketStatus(billBase.getBillNo());
		}
		
		log.info("开始更新账单billBase={}",billBase);
		billBaseService.updateById(billBase);
		
//		log.info("开始更新账单项目billItem={}",billItem);
//		this.billItemService.updateById(billItem);
	}
}
