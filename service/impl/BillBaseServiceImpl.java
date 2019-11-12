package com.taolue.baoxiao.fund.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.MqQueueConstant;
import com.taolue.baoxiao.common.constant.enums.BusiModelEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.UserUtils;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.constant.Constants;
import com.taolue.baoxiao.fund.api.dto.BillBaseDto;
import com.taolue.baoxiao.fund.api.dto.OrderBusiComposeDto;
import com.taolue.baoxiao.fund.api.dto.OrderPaymentDto;
import com.taolue.baoxiao.fund.api.vo.BillBaseVo;
import com.taolue.baoxiao.fund.entity.BillBase;
import com.taolue.baoxiao.fund.entity.BillItem;
import com.taolue.baoxiao.fund.entity.TbBillOverdueAmount;
import com.taolue.baoxiao.fund.entity.TbBillOverdueAmountDetail;
import com.taolue.baoxiao.fund.entity.TbBillRate;
import com.taolue.baoxiao.fund.entity.TbBillRateRuleList;
import com.taolue.baoxiao.fund.mapper.BillBaseMapper;
import com.taolue.baoxiao.fund.service.IBillBaseService;
import com.taolue.baoxiao.fund.service.IBillItemService;
import com.taolue.baoxiao.fund.service.IOrderPaymentService;
import com.taolue.baoxiao.fund.service.ITbBillOverdueAmountDetailService;
import com.taolue.baoxiao.fund.service.ITbBillOverdueAmountService;
import com.taolue.baoxiao.fund.service.ITbBillRateRuleListService;
import com.taolue.baoxiao.fund.service.ITbBillRateService;
import com.taolue.baoxiao.fund.service.composite.IOrderBusiComposeService;
import com.taolue.baoxiao.fund.service.remote.IRefactorMemberServiceFactory;
import com.taolue.member.api.vo.MemberFundRateRuleListVo;
import com.xiaoleilu.hutool.bean.copier.BeanCopier;
import com.xiaoleilu.hutool.bean.copier.CopyOptions;
import com.xiaoleilu.hutool.collection.CollUtil;
import com.xiaoleilu.hutool.date.DateField;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 账单主表，记录账单基础信息
 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2018-08-22
 */
@Slf4j
@Service
public class BillBaseServiceImpl extends ServiceImpl<BillBaseMapper, BillBase> implements IBillBaseService {
	private Log logger = LogFactory.getLog(BillBaseServiceImpl.class);
	@Autowired
	private IOrderBusiComposeService orderBusiComposeService;
	

	
	@Autowired
    private RabbitTemplate rabbitTemplate;
	@Autowired
	private BillBaseMapper billBaseMapper;
	
	@Autowired
	private IOrderPaymentService iOrderPaymentService;
	
    @Autowired
    private IRefactorMemberServiceFactory refactorMemberServiceFactory;
    
    @Autowired
    private ITbBillRateService iTbBillRateService;
    
    @Autowired
    private ITbBillRateRuleListService iTbBillRateRuleListService;
    
    @Autowired
    private ITbBillOverdueAmountService iTbBillOverdueAmountService;
    
    @Autowired
    private IBillItemService iBillItemService;
    
    @Autowired
    private ITbBillOverdueAmountDetailService iTbBillOverdueAmountDetailService;
  
	
	/**
	 * 
	 * <p>Title: findBillBasePageByParams</p>   
	 * <p>Description: </p>   
	 * @param page
	 * @param size
	 * @param queryParams
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.IBillBaseService#findBillBasePageByParams(int, int, com.taolue.baoxiao.fund.api.dto.BillBaseDto)
	 */
	@Override
	public Page<BillBase> findBillBasePageByParams(int page, int size, BillBaseDto queryParams) {
		BeanCopier<BillBase> copire = new BeanCopier<BillBase>(queryParams, new BillBase(), new CopyOptions());
		BillBase bill = copire.copy();
		
		EntityWrapper<BillBase> wrapper = new EntityWrapper<BillBase>(bill);
		
		if (StringUtils.isNotBlank(queryParams.getBaseIsPaid())) {
			if(queryParams.getBaseIsPaid().equals("1")){//已交
				wrapper.in("status","('4','5')");
			}else if(queryParams.getBaseIsPaid().equals("2")) {
				wrapper.in("status","('1','2','3')");
			}
			
		}
		//公司
		if (StringUtils.isNotBlank(bill.getCompanyId())) {
			wrapper.eq("company_id", bill.getCompanyId());
		}
		//账单最小金额
		if (queryParams.getBillMinAmount() != null && queryParams.getBillMinAmount().longValue()>0) {
			wrapper.ge("bill_amount", queryParams.getBillMinAmount());
		}
		//账单最大金额
		if (queryParams.getBillMaxAmount() != null && queryParams.getBillMaxAmount().longValue()>0) {
			wrapper.le("bill_amount", queryParams.getBillMaxAmount());
		}
		//账单结清开始时间
		if (queryParams.getSettledBeginTime() != null ) {
			wrapper.ge("updated_time", 
					DateUtil.format(queryParams.getSettledBeginTime(), "yyyy-MM-dd HH:mm:ss"));
		}
		//账单结清结束时间
		if (queryParams.getSettledEndTime() != null ) {
			wrapper.le("updated_time", 
					DateUtil.format(queryParams.getSettledEndTime(), "yyyy-MM-dd HH:mm:ss"));
			wrapper.in("status", "('4','5')");//4:结清;5:逾期结清',
		}
		//账单生成开始时间
		if (bill.getBeginTime() != null ) {
			wrapper.ge("create_time", 
					DateUtil.format(bill.getBeginTime(), "yyyy-MM-dd HH:mm:ss"));
		}
		//账单生成结束时间
		if (bill.getEndTime() != null ) {
			wrapper.le("create_time", 
					DateUtil.format(bill.getEndTime(), "yyyy-MM-dd HH:mm:ss"));
		}
		//状态
		if (StringUtils.isNotBlank(bill.getStatus())) {
			wrapper.eq("status", bill.getStatus());
		}
		wrapper.orderBy("create_time", true);
		bill.setBeginTime(null);
		bill.setEndTime(null);
		bill.setBillPaymentAmount(null);
		bill.setBillAmount(null);
		
		Page<BillBase> pageQuery = new Page<>(page, size);
		return this.selectPage(pageQuery, wrapper);
	}
	
	public Page<BillBase> findBillBasePageByMap(int page, int size, BillBaseDto queryParams) {
		BeanCopier<BillBase> copire = new BeanCopier<BillBase>(queryParams, new BillBase(), new CopyOptions());
		BillBase bill = copire.copy();
		
		EntityWrapper<BillBase> wrapper = new EntityWrapper<BillBase>(bill);
		
		if (bill.getBillPaymentAmount() != null && bill.getBillPaymentAmount().longValue()>0) {
			wrapper.ge("bill_amount", bill.getBillPaymentAmount());
		}
		
		if (bill.getBillAmount() != null && bill.getBillAmount().longValue()>0) {
			wrapper.le("bill_amount", bill.getBillAmount());
		}
		
		if (bill.getBeginTime() != null ) {
			wrapper.ge("create_time", 
					DateUtil.format(bill.getBeginTime(), "yyyy-MM-dd HH:mm:ss"));
		}
		
		if (bill.getEndTime() != null ) {
			wrapper.le("create_time", 
					DateUtil.format(bill.getEndTime(), "yyyy-MM-dd HH:mm:ss"));
		}
		
		bill.setBeginTime(null);
		bill.setEndTime(null);
		bill.setBillPaymentAmount(null);
		bill.setBillAmount(null);
		
		Page<BillBase> pageQuery = new Page<>(page, size);
		return this.selectPage(pageQuery, wrapper);
	}
	
	public boolean hasUnfinishBill(String companyId) {
		EntityWrapper<BillBase> wrapper = new EntityWrapper<>();
		wrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
		wrapper.eq("company_id", companyId);
		wrapper.ne("status", Constants.Bill.BILL_STATUS_FINASH);
		wrapper.ne("status", Constants.Bill.BILL_STATUS_OVER_FINASH);
		wrapper.ne("parent_id","0");
		int count = this.selectCount(wrapper);
		return count>0 ? true :false;
	}
	
	@Transactional
	public String creaetBillBaseByOrders(Date orderBegin, Date orderEnd, String companyId,String busiModle) {
		Map<String,Object> params = Maps.newHashMap();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		params.put("orderBegin", sdf.format(orderBegin));
		params.put("orderEnd", sdf.format(orderEnd));
		params.put("companyId", companyId);//订单详情归属公司
		params.put("isCreateBillBase", "1");//未生成账单的
		params.put("busiModle", busiModle);//业务模式
		params.put("status", DictionaryEnum.OrderBusiStatus.PAYMENT_SUCCESS.getCateCode());//已完成的
		//params.put("mainType", DictionaryEnum.OrderType.ORDER_TYPE_CASH.getCateCode());//卷消费类型
		
		
		Query<OrderBusiComposeDto> query = new Query<>(params);
		query.setSize(Integer.MAX_VALUE);
		Page<OrderBusiComposeDto> results = orderBusiComposeService
					.selectOrderBusiComposeDtoPage(query);
		
		if (null != results && CollUtil.isNotEmpty(results.getRecords())) {
			BillBase bill = new BillBase();
			bill.setBillNo("BN"+IdWorker.getIdStr());
			
			//String billName=DateUtil.format(orderBegin, "yyyy年MM月")+""+BusiModelEnums.lookupByCode(busiModle).getCateName()+ "账单";
			String billName="";//账单名称
			BillBaseDto BillCompanydto=new BillBaseDto();
			BillCompanydto.setCompanyId(companyId);
			BillCompanydto.setBusiModle(busiModle);
			//查询已经结清的账单
			List<BillBase> billBaseList=baseMapper.queryBillBaseByCompanyId(BillCompanydto);	
			if(null!=billBaseList && billBaseList.size()>0) {
				if(billBaseList.get(0).getEndTime().compareTo(orderEnd)>0) {
					String qBillName=billBaseList.get(0).getBillName();
					String newBillName=qBillName.substring(0, 17);
					logger.info("得到的最新的账单名称:"+newBillName);
					String[] billDate=newBillName.split("-");
					billName=billDate[1]+"-"+DateUtil.format(orderEnd, "yyyyMMdd")+""+BusiModelEnums.lookupByCode(busiModle).getCateName()+ "账单";
				}else {
					billName=DateUtil.format(orderBegin, "yyyyMMdd")+"-"+DateUtil.format(orderEnd, "yyyyMMdd")+""+BusiModelEnums.lookupByCode(busiModle).getCateName()+ "账单";
				}
				
			}else {
				billName=DateUtil.format(orderBegin, "yyyyMMdd")+"-"+DateUtil.format(orderEnd, "yyyyMMdd")+""+BusiModelEnums.lookupByCode(busiModle).getCateName()+ "账单";
			}
			
			
			bill.setBillName(billName);
			bill.setCompanyId(companyId);
			bill.setCompanyName(results.getRecords().get(0).getOrderPayments().get(0).getOriginalCompanyName());
			bill.setBeginTime(orderBegin);
			bill.setEndTime(orderEnd);
			bill.setLatestTime(orderEnd);
			bill.setStatus("1");
			bill.setParentId("0");
			log.info("in service UserUtils.getuser = "+UserUtils.getUser());
			String userName=UserUtils.getUser();
			bill.setCreator(userName);
			bill.setUpdator(userName);
			this.insert(bill);
			
			for (OrderBusiComposeDto orderBusi : results.getRecords()) {
				for (OrderPaymentDto orderPaymentDto : orderBusi.getOrderPayments()) {
					Map<String,Object> orderParams = Maps.newHashMap();
					orderParams.put("billNo", bill.getBillNo());
					orderParams.put("paymentOrderNo", orderPaymentDto.getPaymentOrderNo());
					orderParams.put("busiModle", busiModle);
					if ("0".equals(orderBusi.getParentId())) {
						bill.setCompanyName(orderBusi.getCompanyName());
					}
					//this.orderBusiService.updateBillNo(orderParams);
					this.iOrderPaymentService.updateBillPaymentNo(orderParams);
				}
			}
			//convertAndSendBillMessage(bill.getBillNo());
			return bill.getBillNo();
		}
		return "";
	}
	@Override
	public BillBaseVo preBillBaseByOrders(Date orderBegin, Date orderEnd, String companyId,String busiModle) {
		logger.info("预生成的参数》》》orderBegin="+orderBegin+",orderEnd="+orderEnd+",companyId="+companyId+",busiModle="+busiModle);
		Map<String,Object> params = Maps.newHashMap();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		params.put("orderBegin", sdf.format(orderBegin));
		params.put("orderEnd", sdf.format(orderEnd));
		params.put("companyId", companyId);//订单详情归属公司
		params.put("isCreateBillBase", "1");//未生成账单的
		params.put("busiModle", busiModle);//业务模式
		params.put("status", DictionaryEnum.OrderBusiStatus.COMPLETED.getCateCode());//已完成的
		//params.put("mainType", DictionaryEnum.OrderType.ORDER_TYPE_CASH.getCateCode());//卷消费类型

		
		Query<OrderBusiComposeDto> query = new Query<>(params);
		query.setSize(Integer.MAX_VALUE);
		Page<OrderBusiComposeDto> results = orderBusiComposeService
					.selectOrderBusiComposeDtoPage(query);
		BigDecimal billAmount=new BigDecimal(0);//账单总额
		if (null != results && CollUtil.isNotEmpty(results.getRecords())) {
			BillBaseVo billVo=new BillBaseVo();
			String billName="";//账单名称
		
			BillBaseDto BillCompanydto=new BillBaseDto();
			BillCompanydto.setCompanyId(companyId);
			BillCompanydto.setBusiModle(busiModle);
			//查询已经结清的账单
			List<BillBase> billBaseList=baseMapper.queryBillBaseByCompanyId(BillCompanydto);	
			if(null!=billBaseList && billBaseList.size()>0) {
				if(billBaseList.get(0).getEndTime().compareTo(orderEnd)>0) {
					String qBillName=billBaseList.get(0).getBillName();
					String newBillName=qBillName.substring(0, 17);
					logger.info("得到的最新的账单名称:"+newBillName);
					String[] billDate=newBillName.split("-");
					String remark=BusiModelEnums.lookupByCode(busiModle).getCateName()+"类的账单在购券时已经缴清全款，请在已缴账单列表查看发票。\r\n" + 
					""+qBillName+"已经生成，此次不重复生成该时间段订单。";
					billVo.setRemark(remark);
					billName=billDate[1]+"-"+DateUtil.format(orderEnd, "yyyyMMdd")+""+BusiModelEnums.lookupByCode(busiModle).getCateName()+ "账单";
					billVo.setBeginTime(billBaseList.get(0).getBeginTime());
					billVo.setEndTime(billBaseList.get(0).getEndTime()); 
				}else {
					billName=DateUtil.format(orderBegin, "yyyyMMdd")+"-"+DateUtil.format(orderEnd, "yyyyMMdd")+""+BusiModelEnums.lookupByCode(busiModle).getCateName()+ "账单";
				}

			}else {
				billName=DateUtil.format(orderBegin, "yyyyMMdd")+"-"+DateUtil.format(orderEnd, "yyyyMMdd")+""+BusiModelEnums.lookupByCode(busiModle).getCateName()+ "账单";
			}
	
			boolean isBX=true;//默认报销
			boolean isYBX=false;//是否有報銷
			billVo.setBillName(billName);
			for (OrderBusiComposeDto orderBusi : results.getRecords()) {
				for (OrderPaymentDto orderPaymentDto : orderBusi.getOrderPayments()) {
					BigDecimal dzBillAmount=new BigDecimal(0);//垫资金额
					//消费
					if(BillItemSubCate.BILL_ITEM_SUBCATE_XFSV.getCateCode().equals(orderPaymentDto.getPaymentCate())) {
						isBX=false;
					}
					if(BillItemSubCate.BILL_ITEM_SUBCATE_XFBX.getCateCode().equals(orderPaymentDto.getPaymentCate())) {
						isYBX=true;
					}
					billAmount=billAmount.add(orderPaymentDto.getPaymentAmount());
					if(busiModle.equals(BusiModelEnums.BUSI_MODEL_HFPE.getCateCode()) || busiModle.equals(BusiModelEnums.BUSI_MODEL_HFCM.getCateCode())) {//垫资个人报销
	              		//添加服务费，得到账单项总金额
	              		Map<String,Object> rtnMap=getCharge(billAmount,busiModle,"1",companyId);//添加垫资服务费
	              		if(rtnMap.get("status").equals("fail")) {
	              			log.info("得到费用失败,");
	              			throw new BaoxiaoException(1001, "得到费用失败失败》》");
	              		}
	              		dzBillAmount=new BigDecimal(rtnMap.get("billAmount").toString());
	              			
	              	}
				    billAmount=billAmount.add(dzBillAmount);
				}
				
			}
			//预充值个人报销账单,或者预充值企业都是报销订单
			if(busiModle.equals(BusiModelEnums.BUSI_MODEL_YCPE.getCateCode()) || (isBX && busiModle.equals(BusiModelEnums.BUSI_MODEL_YCCM.getCateCode()))) {
				//结清
				billVo.setStatus(DictionaryEnum.BillStatus.SETTLED.getCateCode());
			}else if(!isBX && busiModle.equals(BusiModelEnums.BUSI_MODEL_YCCM.getCateCode())){
				//如果是预充值企业且有消费订单,默认为部分缴费
				//部分缴费
				billVo.setStatus(DictionaryEnum.BillStatus.PARTIAL_PAYMENT.getCateCode());
				if(!isYBX) {//代表沒有报销，未缴费
					billVo.setStatus(DictionaryEnum.BillStatus.UN_PAID.getCateCode());
				}
			}else{
				//未缴费
				billVo.setStatus(DictionaryEnum.BillStatus.UN_PAID.getCateCode());
			}
			billVo.setBillAmount(billAmount);
			billVo.setCompanyId(companyId);
			billVo.setBusiModle(busiModle);
			log.info("预生成的账单信息>>"+JSON.toJSONString(billVo));
			return billVo;
		}
		return null;
	}
	
	@Override
	public BillBase  queryBaseByModel (String companyId,String busiModle,Date beginTime) {
		BillBaseDto dto=new BillBaseDto();
		dto.setCompanyId(companyId);
		dto.setBusiModle(busiModle);
		dto.setBeginTime(beginTime);
		BillBase billBase=baseMapper.queryBillBaseByDate(dto);
		log.info("根据业务模式查询账单>>>billBase="+JSON.toJSONString(billBase));
		return billBase;
	}
	   public Map<String,Object> getCharge(BigDecimal billPaymentAmount,String  busiModle,String chargeType,String companyId){
	    	Map<String,Object> rtnMap=new HashMap<String,Object>();
	    	BigDecimal rtnAmount=new BigDecimal(0);
	    	try {
	        	rtnMap.put("status", "success");
	    		if(chargeType.equals("1")) {//垫资服务费
	    	 		//根据companyId得到垫资服务费的费率
	        		MemberFundRateRuleListVo ruleVo=refactorMemberServiceFactory
	        				.getRefactorMemberFundRateRuleListService()
	        				.queryFundRateRuleByCompanyId(companyId, busiModle, billPaymentAmount);
	        		if(null==ruleVo) {
	        			log.info("没有获取到服务费率>>>>>>>>>>");
	        			rtnMap.put("status", "fail");
	        			return rtnMap;
	        		}
	        		log.info("根据companyId得到垫资服务费的费率="+ruleVo.getRate());
	        		//rtnAmount=billPaymentAmount.multiply(ruleVo.getRate().divide(new BigDecimal(10000)));//账单项总金额
	        		rtnAmount=billPaymentAmount.multiply(ruleVo.getRate().divide(new BigDecimal(10000))).divide(new BigDecimal(1000)).setScale(2, BigDecimal.ROUND_HALF_UP);
	        		
	    		}
	    		rtnMap.put("billAmount", rtnAmount);
	        	return rtnMap;
			} catch (Exception e) {
				log.error("得到费用失败",e);
				rtnMap.put("status", "fail");
				return rtnMap;
			}
	  
	    }
	public void convertAndSendBillMessage(String billNo,String billNos) {
		JSONObject contextJson = new JSONObject();
		String userName=UserUtils.getUser();
        contextJson.put("billNo", billNo);
        contextJson.put("userName", userName);
        contextJson.put("billNos", billNos);
        contextJson.put("product", "Baoxiao");
        log.info("发送新账单生成消息 -> 新账单编号:{}", billNo);
        rabbitTemplate.convertAndSend(MqQueueConstant.BILL_CREATED_QUEUE,contextJson.toJSONString());
	}

	@Override
	public Page<BillBase> queryBillBasePageByParams(Query query, BillBaseDto queryParams) {
		query.setRecords(baseMapper.queryBillBasePageByParams(query, queryParams));
		return query;
	}
	@Override
	public List<BillBaseVo> queryUnclearedBill(BillBaseDto queryParams){
		List<BillBase> billBaseList=baseMapper.queryUnclearedBill(queryParams);
		List<BillBaseVo> dtlList =Lists.newArrayList();
		if(billBaseList!=null && billBaseList.size()>0) {
			for (BillBase billBase : billBaseList) {
				BillBaseVo vo=new BillBaseVo();
				BeanUtils.copyProperties(billBase, vo);
				dtlList.add(vo);
			}
		}
		return dtlList;
	}

	@Override
	public boolean updateBillBusiDelFlag(Map<String, Object> map) {
		return baseMapper.updateBillBusiDelFlag(map);
	}

	@Override
	@Transactional(rollbackFor = {Exception.class })  
	public boolean overdueAmountJobTask() {
		
		try {
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
			Map<String,Object> map=Maps.newHashMap();
			map.put("newDateStr", sdf.format(new Date()));
			List<BillBase> billBaseList=baseMapper.queryOverdueBill(map);
			for (BillBase billBase : billBaseList) {
				if(null!=billBase.getOverdueTime()) {
					//如果今天已经算过逾期费用
					if(sdf.format(billBase.getOverdueTime()).equals(sdf.format(new Date()))) {
						logger.info("今天已经跑过这个逾期费用计算>>>");
						continue;
					}
				}
				Map<String,Object> billRateMap=Maps.newHashMap();
				billRateMap.put("company_id", billBase.getCompanyId());
				billRateMap.put("business_type", billBase.getBusiModle());
				billRateMap.put("bill_no", billBase.getBillNo());
				List<TbBillRate> billRateList=iTbBillRateService.selectByMap(billRateMap);
				BigDecimal overdueAmount=new BigDecimal(0);//逾期服务费
				BigDecimal billAmount=new BigDecimal(0);//账单金额
				 long days=DateUtil.between(sdf.parse(sdf.format(billBase.getLatestTime())), sdf.parse(sdf.format(new Date())), DateUnit.DAY);
				
				 if(null==billRateList || billRateList.size()<=0) {
					logger.info("没有逾期费用计算方式");
					continue;
				 }
				 boolean isOverdue=true;//代表阶段里面是否是第一次，只计算第一次
				 BigDecimal overRate=new BigDecimal(0); 
				//逾期费用计算方式，1：按阶段；2：按天
				 if(billRateList.get(0).getOverdueType().equals("1")) {
							Map<String,Object> billRateruleListMap=Maps.newHashMap();
							billRateruleListMap.put("days", days);
							billRateruleListMap.put("billNo", billBase.getBillNo());
							//查询阶段的配置
							List<TbBillRateRuleList> ruleList=iTbBillRateRuleListService.queryRateRuleByDay(billRateruleListMap);
							if(null==ruleList || ruleList.size()<=0) {
								logger.info("没有查询到规则配置方式bill_no=0"+billBase.getBillNo()+",days="+days);
								continue;
							}
							if(ruleList.get(0).getOverdueStartDays()==days) {
								BigDecimal rate=ruleList.get(0).getRate();//费率
								overRate=rate;
								//逾期结算金额方式，1：账单全额；2：账单未结算金额
								if(billRateList.get(0).getAmountType().equals("1")) {
									billAmount=billBase.getBillAmount();//账单金额
								}else {
									billAmount=billBase.getBillAmount().subtract(billBase.getBillPayAmount());//账单未缴金额
								}
								
								overdueAmount=billAmount.multiply(rate).divide(new BigDecimal(10000000)).setScale(2, BigDecimal.ROUND_HALF_UP);
								logger.info("阶段逾期费用》》billAmount="+billAmount+","+rate+","+overdueAmount);
								overdueAmount=overdueAmount.multiply(new BigDecimal(1000));
							}else {
								isOverdue=false;
							}
							
					}else if(billRateList.get(0).getOverdueType().equals("2")) {
						BigDecimal rate=billRateList.get(0).getRate();//费率
						  overRate=rate;
						  //逾期天数
						  if(billRateList.get(0).getAmountType().equals("1")) {
							  billAmount=billBase.getBillAmount();//账单金额
							  //延期服务费=延期天数*延期服务费率*除去延期服务费的账单金额。
						  }else {
							  billAmount=billBase.getBillAmount().subtract(billBase.getBillPayAmount());//账单未缴金额
						  }   
						  overdueAmount=billAmount.multiply(rate).divide(new BigDecimal(10000000)).setScale(2, BigDecimal.ROUND_HALF_UP);
						  logger.info("按天逾期费用》》billAmount="+billAmount+","+rate+","+overdueAmount);
						  overdueAmount=overdueAmount.multiply(new BigDecimal(1000));
				  }
				 String overAmountNo="";//逾期编号
				 if(isOverdue) {
					 Map<String,Object> overAmountMap=Maps.newHashMap();
					 overAmountMap.put("bill_no", billBase.getBillNo());
					 List<TbBillOverdueAmount> overAmountList=iTbBillOverdueAmountService.selectByMap(overAmountMap);
					 //查询账单对应的逾期记录，有修改，没则增加
					 if(null!=overAmountList && overAmountList.size()>0) {
						 logger.info("开始修改逾期金额overdueAmount="+overdueAmount);
						 TbBillOverdueAmount tbBillOverdueAmount=overAmountList.get(0);
						 tbBillOverdueAmount.setOverdueAmount(overAmountList.get(0).getOverdueAmount().add(overdueAmount));
						 iTbBillOverdueAmountService.updateById(tbBillOverdueAmount);
						 overAmountNo=tbBillOverdueAmount.getOverdueAmountNo();
					 }else {
						 logger.info("开始添加逾期金额overdueAmount="+overdueAmount);
						 overAmountNo="OA"+IdWorker.getIdStr();
						 TbBillOverdueAmount tbBillOverdueAmount=new TbBillOverdueAmount();
						 tbBillOverdueAmount.setOverdueAmountNo(overAmountNo);
						 tbBillOverdueAmount.setBillNo(billBase.getBillNo());
						 tbBillOverdueAmount.setOverdueAmount(overdueAmount);
						 tbBillOverdueAmount.setOverdueType(billRateList.get(0).getOverdueType());
						 tbBillOverdueAmount.setPaidAmount(new BigDecimal(0));
						 tbBillOverdueAmount.setStatus("1");
						 //tbBillOverdueAmount.setRate(overRate);
						 iTbBillOverdueAmountService.insert(tbBillOverdueAmount);
					 }
					 //添加逾期明细
					 TbBillOverdueAmountDetail  detailEntity=new TbBillOverdueAmountDetail();
					 detailEntity.setOverdueAmountNo(overAmountNo);
					 detailEntity.setOverdueInterest(overdueAmount);
					 detailEntity.setOverduePrincipal(billAmount);
					 detailEntity.setRate(overRate);
					 iTbBillOverdueAmountDetailService.insert(detailEntity);	 
				 }
				
				 Map<String,Object> billItemMap=Maps.newHashMap();
				 billItemMap.put("bill_no", billBase.getBillNo());
				 billItemMap.put("bill_item_cate",DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_YQSV.getCateCode());
				 List<BillItem> billItemList=iBillItemService.selectByMap(billItemMap);
				 //回写账单明细的逾期金额  因为表设计问题，需要多次维护
				 if(null!=billItemList && billItemList.size()>0) {
					 BillItem billItem=billItemList.get(0);
					 billItem.setBillAmount(billItem.getBillAmount().add(overdueAmount));
					 billItem.setBillLeftAmount(billItem.getBillLeftAmount().add(overdueAmount));
					 iBillItemService.updateById(billItem);
				 }
				 billItemMap.put("bill_item_cate",DictionaryEnum.BillItemSubCate.BILL_ITEM_SUBCATE_AllSV.getCateCode());
				 List<BillItem> billItemAllList=iBillItemService.selectByMap(billItemMap);
				 //回写账单明细的服务费汇总
				 if(null!=billItemAllList && billItemAllList.size()>0) {
					 BillItem billItem=billItemAllList.get(0);
					 billItem.setBillAmount(billItem.getBillAmount().add(overdueAmount));
					 billItem.setBillLeftAmount(billItem.getBillLeftAmount().add(overdueAmount));
					 iBillItemService.updateById(billItem);
				 }
				 
				 billBase.setOverdueAmount(billBase.getOverdueAmount().add(overdueAmount));
				 billBase.setOverdueTime(new Date());
				 billBase.setStatus(DictionaryEnum.BillStatus.BE_OVERDUE.getCateCode());//逾期
				 billBase.setOverdueDays(new BigDecimal(days));
				// billBase.setBillAmount(billBase.getBillAmount().add(overdueAmount));
				 billBase.setBiilServiceAmount(billBase.getBiilServiceAmount().add(overdueAmount));
				 baseMapper.updateById(billBase);	 
			} 
			return true;
		} catch (Exception e) {
			logger.error("逾期费用计算job系统错误>>>",e);
			return false;
		}
	

	}

	/**
	 * 
	 *
	 * @Title TbMemberReimburseAmountServiceImpl.getLastMonthStr
	 * @Description: 得到当前时间的上个月字符串日期
	 *
	 * @return
	 * 
	 * @version: 1.0 
	 * @author duqiang
	 */
	public String getLastMonthStr(Integer month) {
		Date date=new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
        Calendar rightNow = Calendar.getInstance();  
        rightNow.setTime(date);  
        rightNow.add(Calendar.MONTH, -month);  
        Date dt1 = rightNow.getTime();  
        return sdf.format(dt1); 
	}
	@Override
	public String createBillJobTask(String companyId,String busiModle) {
		//业务模式，商户，交易时间，归属企业，订单状态去生成账单
		
		Map<String,Object> params = Maps.newHashMap();
		//SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		params.put("orderMonth", getLastMonthStr(1));
		params.put("companyId", companyId);//订单详情归属公司
		params.put("isCreateBillBase", "1");//未生成账单的
		params.put("busiModle", busiModle);//业务模式
		params.put("status", DictionaryEnum.OrderBusiStatus.PAYMENT_SUCCESS.getCateCode());//已完成的
		//params.put("mainType", DictionaryEnum.OrderType.ORDER_TYPE_CASH.getCateCode());//卷消费类型
		
		
		Query<OrderBusiComposeDto> query = new Query<>(params);
		query.setSize(Integer.MAX_VALUE);
		Page<OrderBusiComposeDto> results = orderBusiComposeService
					.selectOrderBusiComposeDtoPage(query);
		
		if (null != results && CollUtil.isNotEmpty(results.getRecords())) {
			BillBase bill = new BillBase();
			bill.setBillNo("BN"+IdWorker.getIdStr());

			String billName=getLastMonthStr(1)+""+BusiModelEnums.lookupByCode(busiModle).getCateName()+ "账单";
			
			
			
			bill.setBillName(billName);
			bill.setCompanyId(companyId);
			bill.setCompanyName(results.getRecords().get(0).getCompanyName());
			
			BillBaseDto baseDto=new BillBaseDto();
			baseDto.setCompanyId(companyId);
			baseDto.setBusiModle(busiModle);
			baseDto.setBaseMonth(getLastMonthStr(1));
			List<BillBase> billBaseList=billBaseMapper.queryBillBaseByCompanyIdLastMonth(baseDto);
			if(null!=billBaseList && billBaseList.size()>0) {
				//上个月最后一次生成账单的结束日期+1
				bill.setBeginTime(com.xiaoleilu.hutool.date.DateUtil.offset(billBaseList.get(0).getEndTime(),DateField.DAY_OF_MONTH, 1));
			}else {
				
				bill.setBeginTime(com.xiaoleilu.hutool.date.DateUtil.beginOfMonth(com.xiaoleilu.hutool.date.DateUtil.offset(new Date(),DateField.MONTH,-1)));
			}
			
			bill.setEndTime(com.xiaoleilu.hutool.date.DateUtil.endOfMonth(com.xiaoleilu.hutool.date.DateUtil.offset(new Date(),DateField.MONTH,-1)));
			bill.setLatestTime(com.xiaoleilu.hutool.date.DateUtil.endOfMonth(com.xiaoleilu.hutool.date.DateUtil.offset(new Date(),DateField.MONTH,-1)));
			bill.setStatus("1");
			bill.setParentId("0");
			bill.setCreator("admin");
			bill.setUpdator("admin");
			this.insert(bill);
			
			for (OrderBusiComposeDto orderBusi : results.getRecords()) {
				for (OrderPaymentDto orderPaymentDto : orderBusi.getOrderPayments()) {
					Map<String,Object> orderParams = Maps.newHashMap();
					orderParams.put("billNo", bill.getBillNo());
					orderParams.put("paymentOrderNo", orderPaymentDto.getPaymentOrderNo());
					orderParams.put("busiModle", busiModle);
					if ("0".equals(orderBusi.getParentId())) {
						bill.setCompanyName(orderBusi.getCompanyName());
					}
					//this.orderBusiService.updateBillNo(orderParams);
					this.iOrderPaymentService.updateBillPaymentNo(orderParams);
				}
			}
			return bill.getBillNo();
		}
		return "";
	}


	
}
