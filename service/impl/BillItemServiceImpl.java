package com.taolue.baoxiao.fund.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillingType;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.Result;
import com.taolue.baoxiao.common.util.SequenceNumber;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.dto.BillBaseDto;
import com.taolue.baoxiao.fund.api.vo.BillItemVo;
import com.taolue.baoxiao.fund.api.vo.OrderPaymentVo;
import com.taolue.baoxiao.fund.entity.BillBase;
import com.taolue.baoxiao.fund.entity.BillItem;
import com.taolue.baoxiao.fund.entity.OrderPayment;
import com.taolue.baoxiao.fund.entity.TbBillRate;
import com.taolue.baoxiao.fund.entity.TbBillRateRuleList;
import com.taolue.baoxiao.fund.mapper.BillItemMapper;
import com.taolue.baoxiao.fund.service.IBillBaseService;
import com.taolue.baoxiao.fund.service.IBillItemService;
import com.taolue.baoxiao.fund.service.IOrderBusiService;
import com.taolue.baoxiao.fund.service.IOrderPaymentService;
import com.taolue.baoxiao.fund.service.ITbBillRateRuleListService;
import com.taolue.baoxiao.fund.service.ITbBillRateService;
import com.taolue.baoxiao.fund.service.remote.IRefactorMemberServiceFactory;
import com.taolue.member.api.vo.BillRateDetailVo;
import com.taolue.member.api.vo.MemberBillRateRuleListVo;
import com.taolue.member.api.vo.MemberBillRateVo;
import com.taolue.member.api.vo.MemberFundRateRuleListVo;
import com.taolue.member.api.vo.MemberVendorVo;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 账单明细表，每一条为账单中的没一个账单项 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2018-08-24
 */
@Slf4j
@Service
public class BillItemServiceImpl extends ServiceImpl<BillItemMapper, BillItem> implements IBillItemService {

    
    @Autowired
    private IOrderPaymentService orderPaymentService;

    @Autowired
    private IBillBaseService billBaseService;
    
//    @Autowired
//    private RefactorMemberFundRateRuleListService refactorMemberFundRateRuleListService;
//    
//    @Autowired
//    private RefactorMemberBillRateService refactorMemberBillRateService;
    
    @Autowired
    private  IRefactorMemberServiceFactory refactorMemberServiceFactory;
    
    @Autowired
    private ITbBillRateService iTbBillRateService;
    
     
    @Autowired
    private ITbBillRateRuleListService iTbBillRateRuleListService;
    
    @Autowired
    private IOrderBusiService iOrderBusiService;
    /**
     *	业务逻辑操作描述 
     * 1:前获取订单对应得消费总额等信息
     * 2：根据得到的消费总额信息添加子账单信息，
     * 3：根据子账单的编号添加子账单明细信息
     * 4：生成子账单明细的时候根据业务类型，生成服务费
     * 5: 订单的账单编号修改为子帐单的编号
     * 6：最后修改主张单的消费金额信息
     */
	@Override
    @Transactional(rollbackFor = {Exception.class })  
	public boolean addItem(String billNo) {
		 try {
             BigDecimal billAmount=new BigDecimal(0);//账单总额
             BigDecimal biilServiceAmount=new BigDecimal(0);//服务费总额
             BigDecimal billPaymentAmount=new BigDecimal(0);//账单消费总额
             BigDecimal billPayAmount=new BigDecimal(0);//已交总额
           	 Map<String,Object> columnMap=new HashMap<String,Object>();
           	 columnMap.put("bill_no", billNo);
           	// 根据账单单号得到账单信息
             List<BillBase> billBaseList=billBaseService.selectByMap(columnMap);
             String userName="";
       
            Map<String,String> busiModleMap=Maps.newHashMap();//每个业务模式对应的子帐单编号 	
            
            Map<String,Object> params=new HashMap<String,Object>();
            params.put("billNo", billNo);	
            //根据订单单号得到订单模式分组支付信息
            List<OrderPaymentVo> paymentGroupList=orderPaymentService.showGroupOrderPaymentByParams(params);
            log.info("开根据账订单号查询订单支付信息>>bill_no="+billNo+",size="+paymentGroupList.size());
            
            //前获取一些费用
            for (OrderPaymentVo orderPaymentVo : paymentGroupList) {
            	String busiModle="";//业务模式
            	String sonBillNo="";//子账单的编号
            
            	if(StringUtils.isBlank(orderPaymentVo.getBusiModle())) {
            		log.error("业务类型为空");
            		//订单的账单号置位空
            		Map<String, Object> upBillNoMap=Maps.newHashMap();
            		upBillNoMap.put("orderNo", orderPaymentVo.getOrderNo());
            		upBillNoMap.put("busiModle", orderPaymentVo.getBusiModle());
            		orderPaymentService.updateBillNo(upBillNoMap);
            		return false;
            	}
                billAmount=billAmount.add(orderPaymentVo.getPaymentAmount());
            	billPaymentAmount=billPaymentAmount.add(orderPaymentVo.getPaymentAmount());
            	busiModle=orderPaymentVo.getBusiModle();
            	//个人-已缴金额
           
            	if(billBaseList.size()>0) {
            		log.info("###########查询到billBaseList={}", JSON.toJSONString(billBaseList));
            		userName=billBaseList.get(0).getCreator();
            		log.info("###########查询到userName={}", userName);
            		//sonBillNo="BN"+IdWorker.getIdStr();
            		String idWorker=IdWorker.getIdStr();
    				SequenceNumber sn=new SequenceNumber(idWorker);
    				sonBillNo=sn.getLiteBuinessCode("BL", null);
    				
    				//查询账单对应订单的一个模式的订单数据
    				Map<String,Object> busiModleParams=new HashMap<String,Object>();
                    busiModleParams.put("billNo", billNo);	
                    busiModleParams.put("busiModle", orderPaymentVo.getBusiModle());	
                    List<OrderPaymentVo> busiModlePaymentList=orderPaymentService.selectOrderPaymentByBusiModle(busiModleParams);
    				
            		
                    BigDecimal advanceTotalAmount=new BigDecimal(0);//垫资总金额 后付费个人用
                    BigDecimal xfTotalAmount=new BigDecimal(0);//消费总金额
                    BigDecimal reimburseTotalAmount=new BigDecimal(0);//报销总金额
                    //BigDecimal beforeInvoiceAmount=new BigDecimal(0);//预开票金额
                    for (OrderPaymentVo orderPaymentVo2 : busiModlePaymentList) {
                    	String orderType=orderPaymentVo2.getPaymentCate();//BIS10004消费  BIS10013报销
                    	//是否是个人模式
                    	boolean bool=isXfEndModle(orderPaymentVo2.getBusiModle());
                    	String billItemCate="";
                    	String billItemCateName="";
                    	String status="0";//账单明细默认0
                     	BigDecimal billLeftAmount=new BigDecimal(0);//待交金额
                        BigDecimal billitemPayAmount=new BigDecimal(0);//已交总额
                    	boolean reimburseBool=false;//预充值模式，后付费个人,如果是商户直接变为已交
                    	//消费类型
                		if(isReimburse(orderType)) {
                			xfTotalAmount=xfTotalAmount.add(orderPaymentVo2.getPaymentAmount());
                			billItemCate=DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode();//账单项子分类 卷消费id
                			billItemCateName=DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateName();//账单项子分类名称 卷消费名称
                		}else {
                  			//报销类型
                			reimburseTotalAmount=reimburseTotalAmount.add(orderPaymentVo2.getPaymentAmount());
                			billItemCate=DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_XFBX.getCateCode();//
                			billItemCateName=DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_XFBX.getCateName();//
                		
                			reimburseBool=reimburseEndBusiModle(orderPaymentVo2.getBusiModle());
                		}
                    	if(bool || reimburseBool) {//个人模式 或者报销类型的需要变为已交的业务模式
                    		billitemPayAmount=orderPaymentVo2.getPaymentAmount();
                    		status="2";//全部支付
                    	}else {
                    		billLeftAmount=orderPaymentVo2.getPaymentAmount();
                    	}	
                    	billPayAmount=billPayAmount.add(billitemPayAmount);	
                    	
                    	
                    	//接口回去外部商户对应的内部商户
                    	String internalVendorId=null;
                    	String internalVendorName=null;
                    	List<String> strVendorIdList= Lists.newArrayList();
                    	String strVendorId=orderPaymentVo2.getPaymentVendorId();
                    	strVendorIdList.add(strVendorId);
                    	/*List<MemberVendorVo> memberVendorVoList=refactorMemberServiceFactory
                    			.getRefactorVendorService()
                    			.findVendorListByIdList(strVendorIdList);
                  		if(null!=memberVendorVoList && memberVendorVoList.size()>0) {
                  			internalVendorId=memberVendorVoList.get(0).getId();
                  			internalVendorName=memberVendorVoList.get(0).getVendorName();
                  		}*/
                  		 Map<String,Object> internalVendorParams=new HashMap<String,Object>();
                  		internalVendorParams.put("billNo", billNo);	
                  		internalVendorParams.put("busiModle", orderPaymentVo.getBusiModle());
                  		internalVendorParams.put("paymentVendorId", orderPaymentVo2.getPaymentVendorId());
                  		internalVendorParams.put("internalVendorId", internalVendorId);	
                  		internalVendorParams.put("internalVendorName", internalVendorName);	
                  		orderPaymentService.updateInternalVendor(internalVendorParams);		
                    	Map<String, Object> internalVendorMap=Maps.newHashMap();
                    	BillItem  internalBillItem=null;
                    	if(StringUtils.isNotBlank(internalVendorId)) {
                    		internalVendorMap.put("billNo", sonBillNo);
                      		internalVendorMap.put("internalVendorId",internalVendorId);
                      		internalBillItem=this.baseMapper.selectBillItemByInternalVendor(internalVendorMap);
                      	//如果没有这个内部商户，直接新增，否修改累计额度,多个商户对应的内部商户一样，吧额度累加新增一条内部商户额度信息
                      		if(ObjectUtils.isEmpty(internalBillItem)) {
                            	BillItem billItem=new BillItem();
                            	billItem.setBillNo(sonBillNo);//主账单编号	
                            	String idWorker1=IdWorker.getIdStr();
                            	SequenceNumber sn1=new SequenceNumber(idWorker1);
                            	billItem.setBillItemNo("BL"+IdWorker.getIdStr());//账单项编号
                            	billItem.setBillAmount(orderPaymentVo2.getPaymentAmount());//账单项总金额
            
                            	billItem.setBillPayAmount(billitemPayAmount);//已付费
                            	billItem.setBillLeftAmount(billLeftAmount);//未付费
                            	billItem.setBillItemKind(internalVendorId);//账单项主分类 商户id
                            	billItem.setBillItemKindName(internalVendorName);//账单项主分类名称 商户名称
                        		billItem.setBillItemCate(billItemCate);
                        		billItem.setBillItemCateName(billItemCateName);
                            	billItem.setBillCalculateRule(null);//规则调用接口获取费率，然后写出规则
                            	billItem.setStatus(status);
                            	billItem.setRemark("");
                            	//String userName=UserUtils.getUser();
                            	billItem.setCreator(userName);
                            	billItem.setUpdator(userName);
                            	this.baseMapper.insert(billItem);
                        	}else {
                        		internalBillItem.setBillAmount(internalBillItem.getBillAmount().add(orderPaymentVo2.getPaymentAmount()));
                        		internalBillItem.setBillLeftAmount(internalBillItem.getBillLeftAmount().add(billLeftAmount));
                        		internalBillItem.setBillPayAmount(internalBillItem.getBillPayAmount().add(billitemPayAmount));
                        		this.baseMapper.updateAllColumnById(internalBillItem);
                        	}
                      		//如果有内部商户，外面商户的不变，内部商户变已缴
                    		billitemPayAmount=new BigDecimal(0);
                    		billLeftAmount=orderPaymentVo2.getPaymentAmount();
                    		status="0";
                    	}
                    	//后付费个人或者报销类型的后付费企业，才会给服务费汇总累加
                    	if(orderPaymentVo.getBusiModle().equals(BusiModelEnums.BUSI_MODEL_HFPE.getCateCode()) 
                    			|| (!isReimburse(orderType) 
                    			&& orderPaymentVo.getBusiModle().equals(BusiModelEnums.BUSI_MODEL_HFCM.getCateCode()) )){
                    		advanceTotalAmount=advanceTotalAmount.add(orderPaymentVo2.getPaymentAmount());
                    	}
            			//添加账单明细表
                    	BillItem billItem=new BillItem();
                    	billItem.setBillNo(sonBillNo);//主账单编号
                    	String idWorker2=IdWorker.getIdStr();
                    	SequenceNumber sn2=new SequenceNumber(idWorker2);
                    	billItem.setBillItemNo("BL"+IdWorker.getIdStr());//账单项编号
                    	billItem.setBillAmount(orderPaymentVo2.getPaymentAmount());//账单项总金额
                    	billItem.setBillPayAmount(billitemPayAmount);//已付费
                    	billItem.setBillLeftAmount(billLeftAmount);//未付费
                    	billItem.setBillItemKind(orderPaymentVo2.getPaymentVendorId());//账单项主分类 商户id
                    	billItem.setBillItemKindName(orderPaymentVo2.getPaymentVendorName());//账单项主分类名称 商户名称
                    	billItem.setBillItemCate(billItemCate);//账单项子分类 
                    	billItem.setBillItemCateName(billItemCateName);//账单项子分类名称
                    	//billItem.setInternalVendorId(internalVendorId);
                    	//billItem.setInternalVendorName(internalVendorName);
                    	billItem.setBillCalculateRule(null);//规则调用接口获取费率，然后写出规则
                    	billItem.setStatus(status);
                    	billItem.setRemark("");
                    	//String userName=UserUtils.getUser();
                    	billItem.setCreator(userName);
                    	billItem.setUpdator(userName);
                    	this.baseMapper.insert(billItem);//添加账单明细表	
					}
                 
                    //开始添加子账单
            		Result<Boolean> res=addSonBillBase(sonBillNo, billBaseList, xfTotalAmount,reimburseTotalAmount, busiModle,userName);
            		if(res.getData()==false) {
            			//订单的账单号置位空
            			throw new BaoxiaoException(9999, res.getMsg());
            		}
                    
                	BigDecimal dzBillAmount=new BigDecimal(0);//垫资服务费的账单金额
                   	if(orderPaymentVo.getBusiModle().equals(BusiModelEnums.BUSI_MODEL_HFPE.getCateCode()) || orderPaymentVo.getBusiModle().equals(BusiModelEnums.BUSI_MODEL_HFCM.getCateCode())) {//垫资个人报销
                   		//添加服务费，得到账单项总金额
                   		Map<String,Object> rtnMap=addCharge(billPaymentAmount,orderPaymentVo, sonBillNo, "1",billBaseList.get(0).getCompanyId(),userName);//添加垫资服务费
                   		if(rtnMap.get("status").equals("fail")) {
                   		//订单的账单号置位空
                   
                   			log.info("添加费用失败,");
                   			throw new BaoxiaoException(1001, "添加服务费用失败》》");
                   		}
                   		dzBillAmount=new BigDecimal(rtnMap.get("billAmount").toString());
                   			
                   	}
                   	BigDecimal hffgrAmount=new BigDecimal(0);
                	if(orderPaymentVo.getBusiModle().equals(BusiModelEnums.BUSI_MODEL_HFPE.getCateCode()) || advanceTotalAmount.compareTo(new BigDecimal(0))>0){
                		hffgrAmount=dzBillAmount.add(advanceTotalAmount);
                	}else {
                		hffgrAmount=dzBillAmount;
                	}
                   	//hffgrAmount=dzBillAmount.add(advanceTotalAmount);
                   	//服务费汇总
                   	addAllCharge(hffgrAmount, sonBillNo,userName);
                   	
                   	billAmount=billAmount.add(dzBillAmount);
                	biilServiceAmount=biilServiceAmount.add(dzBillAmount);
                	//添加服务费，得到账单项总金额
                	Map<String,Object> rtnMap=addCharge(billPaymentAmount,orderPaymentVo, sonBillNo, "2",billBaseList.get(0).getCompanyId(),userName);//添加逾期服务费
                	if(rtnMap.get("status").equals("fail")) {
                		//订单的账单号置位空
                	
               			log.info("添加费用失败,");
               			throw new BaoxiaoException(1001, "添加服务费用失败》》");
               		}
            	}
            	busiModleMap.put(busiModle, sonBillNo);  
            }
        	
            List<OrderPaymentVo> paymentList=orderPaymentService.showOrderPaymentByParams(params);
            for (OrderPaymentVo orderPaymentVo2 : paymentList) {
            	
            	//订单的账单编号修改为子账单的编号
                OrderPayment orderPayment=new OrderPayment();
                orderPayment.setId(orderPaymentVo2.getId());
                if(ObjectUtils.isEmpty(busiModleMap.get(orderPaymentVo2.getBusiModle()))) {
                	//订单的账单号置位空
                	log.info("回写订单明细的账单编号失败，没有对应模式的子帐单编号"+busiModleMap.toString());
           			throw new BaoxiaoException(1001, "回写订单明细的账单编号失败");
                }
                orderPayment.setBillNo(busiModleMap.get(orderPaymentVo2.getBusiModle()));
               	//boolean bool=isPerson(orderPaymentVo2.getBusiModle());
             	 Map<String,Object> columnMap2=new HashMap<String,Object>();
               	 columnMap2.put("bill_no", busiModleMap.get(orderPaymentVo2.getBusiModle()));
               	// 根据账单单号得到账单信息
                 List<BillBase> billBaseList2=billBaseService.selectByMap(columnMap2);
               	//根据当前订单的业务模式，得到这个模式的账单号，然后根据这个账单号查询账单状态是否结清
        		if(null!=billBaseList2 && billBaseList2.size()>0 && "4".equals(billBaseList2.get(0).getStatus())) {
            		orderPayment.setStatus("3");//已收票
            		//修改订单为已完成
            		Map<String,Object> param=Maps.newHashMap();
            		param.put("orderNo", orderPayment.getOrderNo());
            		param.put("status","4");//已完成
            		iOrderBusiService.updateOrderStatus(params);
            	}
        		orderPayment.setUpdator(userName);
                orderPaymentService.updateById(orderPayment);
			}
            

/*
             if(billBaseList.size()>0) {
            	  BillBase billBase=new BillBase();
                  billBase.setBillAmount(billAmount);//账单金额
                  billBase.setBillPaymentAmount(billPaymentAmount);//消费金额
                  billBase.setBiilServiceAmount(biilServiceAmount);//服务费
                  billBase.setBillPayAmount(billPayAmount);//已缴金额
                  billBase.setId(billBaseList.get(0).getId());
                  billBase.setCreator(userName);
                  billBase.setUpdator(userName);
                  //修改账单表金额
                  billBaseService.insertOrUpdate(billBase);
             }*/
           
        } catch (Exception e) {
            log.error("账单单号队列异常",e);
         
            throw new BaoxiaoException(1004, "添加账单明细失败");
        }
        return true;
	}

	   /**
     * 
     *
     * @Title SmsAliyunMessageHandler.addSonBillBase
     * @Description: 添加子帐单信息
     *
     * @param sonBillNo
     * @param billBaseList
     * @param sonillPaymentAmount
     * @param busiModle
     * @return
     * 
     * @version: 1.0 
     * @author duqiang
     */
    public Result<Boolean> addSonBillBase(String sonBillNo ,List<BillBase> billBaseList ,BigDecimal xfTotalAmount,BigDecimal reimburseTotalAmount,String busiModle,String userName){
    	log.info("生成子订单入参参数>>>"+sonBillNo+","+xfTotalAmount+","+reimburseTotalAmount+","+busiModle);
    	Result<Boolean> rturn=new Result<Boolean>();
    	try {
        	BillBase bBase=new BillBase();
    		bBase.setBillNo(sonBillNo);
    		bBase.setBillName(billBaseList.get(0).getBillName());
    		bBase.setCompanyId(billBaseList.get(0).getCompanyId());
    		bBase.setCompanyName(billBaseList.get(0).getCompanyName());
    		bBase.setBeginTime(billBaseList.get(0).getBeginTime());
    		bBase.setEndTime(billBaseList.get(0).getEndTime());
    		bBase.setLatestTime(billBaseList.get(0).getLatestTime());
    		bBase.setStatus("1");
    		bBase.setBillPaymentAmount(xfTotalAmount);//消费总额
    		bBase.setBillReimburseAmount(reimburseTotalAmount);//报销总额
    		
    		//消费金额+报销金额
    		BigDecimal raymentAndReimburseAmount=xfTotalAmount.add(reimburseTotalAmount);
    		BigDecimal serviceAmount=new BigDecimal(0);//服务费
    		BigDecimal payAmount=new BigDecimal(0);//已交金额
    		if(busiModle.equals(BusiModelEnums.BUSI_MODEL_HFCM.getCateCode()) || busiModle.equals(BusiModelEnums.BUSI_MODEL_HFPE.getCateCode())) {
    			//根据消费总额获取垫资服务费率
        		MemberFundRateRuleListVo ruleVo=refactorMemberServiceFactory
        				.getRefactorMemberFundRateRuleListService()
        				.queryFundRateRuleByCompanyId(billBaseList.get(0).getCompanyId(), busiModle, 
        						raymentAndReimburseAmount);
        		if(null == ruleVo) {
       				log.error("根据companyId，amount获取服务费率为空>>companyId="+billBaseList.get(0).getCompanyId()+",amount="+raymentAndReimburseAmount);
       				//throw new BaoxiaoException(1002, "获取垫资服务费率失败");
       				rturn.setData(false);
       				rturn.setMsg("没有配置垫资服务费率");
       				return rturn;
        		}
        		//服务费=服务费总金额*费率
        		serviceAmount=raymentAndReimburseAmount.multiply(ruleVo.getRate().divide(new BigDecimal(10000))).divide(new BigDecimal(1000)).setScale(2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(1000));
    		}
    		//如果是预充值个人则已交金额就是总金额
    		if(busiModle.equals(BusiModelEnums.BUSI_MODEL_YCPE.getCateCode()) ) {
    			bBase.setStatus("4");//已结清
    			payAmount=raymentAndReimburseAmount;
    		}
    		//预充值企业 有消费账单进入待缴账单。（若没有报销订单显示未缴费，若有报销订单显示部分缴费）
    		if(busiModle.equals(BusiModelEnums.BUSI_MODEL_YCCM.getCateCode()) ) {
    			//只有报销
    			if(xfTotalAmount.compareTo(new BigDecimal(0))==0) {
    				bBase.setStatus("4");//已结清
        			payAmount=raymentAndReimburseAmount;
    			}
    			//只有消费
    			if(reimburseTotalAmount.compareTo(new BigDecimal(0))==0) {
    				bBase.setStatus("1");//未缴费
    			}
    			//报销，消费都有
    			if(reimburseTotalAmount.compareTo(new BigDecimal(0))>0 && xfTotalAmount.compareTo(new BigDecimal(0))>0) {
    				bBase.setStatus("2");//部分缴费
    				payAmount=reimburseTotalAmount;
    			}
    		}
    	
    		bBase.setBillPayAmount(payAmount);//已交总额
    		bBase.setBiilServiceAmount(serviceAmount);//服务费总额
    		bBase.setBillAmount(serviceAmount.add(raymentAndReimburseAmount));//账单总额
    		bBase.setBusiModle(busiModle);
    		bBase.setParentId(billBaseList.get(0).getId());
    		
    		//查询企业的免息天数
    		MemberBillRateVo memberBillRateVo=refactorMemberServiceFactory.getRefactorMemberBillRateService()
    				.queryValidBillRateFreeDays(billBaseList.get(0).getCompanyId(), busiModle);
           
    		if(!ObjectUtils.isEmpty(memberBillRateVo)) {
    			Integer days=memberBillRateVo.getFreeDays()-1;
                Calendar cl = Calendar.getInstance();
                cl.setTime(billBaseList.get(0).getCreateTime());
                cl.add(Calendar.DATE, days);
                bBase.setLatestTime(cl.getTime());
                bBase.setOverdueExtendDays(new BigDecimal(days));
    		}else {
    			log.error("没有配置免息天数companyId="+billBaseList.get(0).getCompanyId()+",busiModle="+busiModle);
    			rturn.setData(false);
   				rturn.setMsg("没有配置免息天数");
   				return rturn;
    		}
    		bBase.setCreator(userName);
    		bBase.setUpdator(userName);
    	
    		billBaseService.insert(bBase);
    		rturn.setData(true);
    		return rturn;
		} catch (Exception e) {
			log.error("添加账单明细失败",e);
			rturn.setData(false);
			rturn.setMsg("系统异常");
			return rturn;
		}

    }

    /**
     *
     * @Title SmsAliyunMessageHandler.isPerson
     * @Description: 判断业务类型是预充值个人true：个人 false 企业
     *
     * @param busiModle
     * @return
     * 
     * @version: 1.0 
     * @author duqiang
     */
    public boolean isPerson(String busiModle){
    	return busiModle.equals(BusiModelEnums.BUSI_MODEL_YCPE.getCateCode())?true:false;

    }
    
  	
    /**
     * 是否个人模式
     * @param busiModle
     * @return
     */
    public boolean isXfEndModle(String busiModle){
    	return ( busiModle.equals(BusiModelEnums.BUSI_MODEL_YCPE.getCateCode()))?true:false;

    }
    
    /**
     * 是否是消费类型 true:消费  false报销
     * @param orderType
     * @return
     */
    public boolean isReimburse(String orderType){
		return (BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode()).equals(orderType)?true:false;
    }
    
    /**
     * 判断是否是个人模式或者预充值企业
     * @param busiModle
     * @return
     */
    public boolean reimburseEndBusiModle(String busiModle){
    	return ( busiModle.equals(BusiModelEnums.BUSI_MODEL_YCPE.getCateCode()) 
    			|| busiModle.equals(BusiModelEnums.BUSI_MODEL_YCCM.getCateCode()))?true:false;
    }
   /**
    * 
    *
    * @Title SmsAliyunMessageHandler.addCharge
    * @Description: 添加服务费，逾期费，得到账单项总额
    *
    * @param orderPaymentVo
    * @param billNo
    * @param chargeType
    * @return
    * @throws Exception
    * 
    * @version: 1.0 
    * @author duqiang
    */
    public Map<String,Object> addCharge(BigDecimal billPaymentAmount,OrderPaymentVo orderPaymentVo,String billNo,String chargeType,String companyId,String userName){
    	log.info("开始添加费用》》billNo="+billNo+"，chargeType="+chargeType);
    	Map<String,Object> rtnMap=new HashMap<String,Object>();
    	try {
        	rtnMap.put("status", "success");
        	BillItem billItem=new BillItem();
        	billItem.setBillNo(billNo);//主账单编号
    		String idWorker=IdWorker.getIdStr();
			SequenceNumber sn=new SequenceNumber(idWorker);
        	
        	billItem.setBillItemNo("BL"+IdWorker.getIdStr());//账单项编号
    	
    		  
        	billItem.setBillPayAmount(new BigDecimal(0));
    		
    		String billitemKind="";
    		String billitemKindName="";
    		String billitemCate="";
    		String billitemCateName="";
   
    		if(chargeType.equals("1")) {//垫资服务费
    	 		//根据companyId得到垫资服务费的费率
        		MemberFundRateRuleListVo ruleVo=refactorMemberServiceFactory
        				.getRefactorMemberFundRateRuleListService()
        				.queryFundRateRuleByCompanyId(companyId, orderPaymentVo.getBusiModle(), billPaymentAmount);
        		if(null==ruleVo) {
        			log.info("没有获取到服务费率>>>>>>>>>>");
        			rtnMap.put("status", "fail");
        			return rtnMap;
        		}
        		log.info("根据companyId得到垫资服务费的费率="+ruleVo.getRate());
    			billitemKind=DictionaryEnum.AcctCateEnums.ACCT_CATE_PTLZ.getCateMgn();//砾洲平台memberid
    			billitemKindName=DictionaryEnum.AcctCateEnums.ACCT_CATE_PTLZ.getCateName();
    			//billItem.setBillCalculateRule(ruleVo.getRate()+"");//规则调用接口获取费率，然后写出规则 然后用金额*费率
    			//服务费=服务费总金额*费率
    			
    			BigDecimal fAmount=billPaymentAmount.multiply(ruleVo.getRate().divide(new BigDecimal(10000))).divide(new BigDecimal(1000)).setScale(2, BigDecimal.ROUND_HALF_UP);
    		
    			billItem.setBillAmount(fAmount.multiply(new BigDecimal(1000)));//账单项总金额
    			billItem.setBillLeftAmount(fAmount.multiply(new BigDecimal(1000)));
    			billitemCate=DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_DZSV.getCateCode();
    			billitemCateName=DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_DZSV.getCateName();
    		}else if(chargeType.equals("2")){//逾期服务费
    			billitemKind=DictionaryEnum.AcctCateEnums.ACCT_CATE_PTLZ.getCateMgn();//砾洲平台memberid
    			billitemKindName=DictionaryEnum.AcctCateEnums.ACCT_CATE_PTLZ.getCateName();
    			billItem.setBillAmount(new BigDecimal(0));//账单项总金额
    			billItem.setBillLeftAmount(new BigDecimal(0));
    			billitemCate=DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_YQSV.getCateCode();
    			billitemCateName=DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_YQSV.getCateName();
    			//得到逾期配置
    			MemberBillRateVo memberBillRateVo=refactorMemberServiceFactory.getRefactorMemberBillRateService()
    					.queryValidBillRateFreeDays(companyId, orderPaymentVo.getBusiModle());
    			if(!ObjectUtils.isEmpty(memberBillRateVo)) {
    				TbBillRate entity=new TbBillRate();
    				BeanUtils.copyProperties(memberBillRateVo, entity);
    				entity.setBillNo(billNo);
    				entity.setId(null);
    				iTbBillRateService.insert(entity);
    				//逾期费用计算方式，1：按阶段；2：按天
        			if(memberBillRateVo.getOverdueType().equals("1")) {
        				BillRateDetailVo billRateDetailVo=refactorMemberServiceFactory
        						.getRefactorMemberBillRateService()
        						.queryValidBillRateDetail(companyId, orderPaymentVo.getBusiModle(), null);
        				for (MemberBillRateRuleListVo memberBillRateRuleListVo : billRateDetailVo.getBillRateRuleList()) {
        					TbBillRateRuleList ruleList=new TbBillRateRuleList();
        					BeanUtils.copyProperties(memberBillRateRuleListVo, ruleList);
        					ruleList.setBillNo(billNo);
        					ruleList.setId(null);
        					iTbBillRateRuleListService.insert(ruleList);
						}
        			}
    			}
    			
    		}
    		
    		billItem.setBillItemKind(billitemKind);//账单项主分类
    		billItem.setBillItemKindName(billitemKindName);//账单项主分类名称
    		billItem.setBillItemCate(billitemCate);//账单项子分类
    		billItem.setBillItemCateName(billitemCateName);//账单项子分类名称
    		
    		billItem.setStatus("0");
    		billItem.setRemark("");
    		//String userName=UserUtils.getUser();
        	billItem.setCreator(userName);
        	billItem.setUpdator(userName);
    		this.baseMapper.insert(billItem);
    		//费用汇总
    		
    		rtnMap.put("billAmount", billItem.getBillAmount());
        	return rtnMap;
		} catch (Exception e) {
			log.error("添加账单明细服务费失败",e);
			rtnMap.put("status", "fail");
			return rtnMap;
		}
  
    }

    /**
     * 
     *
     * @Title BillItemServiceImpl.addAllCharge
     * @Description: 添加汇总服务费记录
     *
     * @param billPaymentAmount
     * @param orderPaymentVo
     * @param billNo
     * @param chargeType
     * @param companyId
     * 
     * @version: 1.0 
     * @author duqiang
     */
    public void addAllCharge(BigDecimal billAmount,String billNo,String userName){
    	
    	try {
    		String idWorker=IdWorker.getIdStr();
			SequenceNumber sn=new SequenceNumber(idWorker);
        	BillItem billItem=new BillItem();
        	billItem.setBillNo(billNo);//主账单编号
        	billItem.setBillItemNo("BL"+IdWorker.getIdStr());//账单项编号
    	
    		  
        	billItem.setBillPayAmount(new BigDecimal(0));
    		
    		billItem.setBillItemKind(DictionaryEnum.AcctCateEnums.ACCT_CATE_PTLZ.getCateMgn());//砾洲平台memberid
    		billItem.setBillItemKindName(DictionaryEnum.AcctCateEnums.ACCT_CATE_PTLZ.getCateName());//砾洲平台memberid
    		billItem.setBillAmount(billAmount);//账单项总金额
    		billItem.setBillLeftAmount(billAmount);
    		//服务费汇总
    		String billitemCate=DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_AllSV.getCateCode();
    		String billitemCateName=DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_AllSV.getCateName();
    
     		billItem.setBillItemCate(billitemCate);//账单项子分类
    		billItem.setBillItemCateName(billitemCateName);//账单项子分类名称
    	
    		billItem.setStatus("0");
    		billItem.setRemark("");
    		//String userName=UserUtils.getUser();
        	billItem.setCreator(userName);
        	billItem.setUpdator(userName);
    		this.baseMapper.insert(billItem);
    		

		} catch (Exception e) {
			log.error("添加账单明细服务费汇总失败",e);
		}
  
    }
    
	@Override
	public Page<BillItemVo> selectBillItemByVendor(Query query,BillBaseDto dto) {
		//dto.setBillItemCate(DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode());
		return query.setRecords(this.baseMapper.selectBillItemByVendor(query,dto));
	}
	
	@Override
	public Page<BillItemVo> selectBillItemByExpenseon(Query query,BillBaseDto dto) {
		//dto.setBillItemCate(DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode());
		return query.setRecords(this.baseMapper.selectBillItemByExpenseon(query,dto));
	}
	

	@Override
	public List<BillItem> selectBillItemByBillNo(Map<String, Object> params) {
		return this.baseMapper.selectBillItemByBillNo(params);
	}
    
}