/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  AccountServiceImpl.java   
 * @Package com.taolue.baoxiao.fund.service.impl   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   2018年8月28日 上午10:42:24   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.Condition;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.EnumJiaFu;
import com.taolue.baoxiao.common.constant.enums.EnumJiaFu.TemplateMsgIdEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.ActionType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BalanceStatus;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BillItemSubCate;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.MemberCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.OrderType;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.TradeCateEnums;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.sourceCode;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.DateToolUtils;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.UserUtils;
import com.taolue.baoxiao.fund.api.coupon.RefactorVoucherService;
import com.taolue.baoxiao.fund.api.dict.RefactorCommonDictService;
import com.taolue.baoxiao.fund.api.dock.IOpenJiaBaiTiaoBuyCouponService;
import com.taolue.baoxiao.fund.api.dto.BalanceSearchParams;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.member.IRefactorMemberService;
import com.taolue.baoxiao.fund.api.openplatform.IDockOpenPlatformService;
import com.taolue.baoxiao.fund.api.vo.FundAcctVo;
import com.taolue.baoxiao.fund.api.vo.FundBalanceVo;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.entity.TbFundAcct;
import com.taolue.baoxiao.fund.entity.TbFundBalance;
import com.taolue.baoxiao.fund.entity.TbFundTradeFlow;
import com.taolue.baoxiao.fund.service.ITbFundAcctRelateService;
import com.taolue.baoxiao.fund.service.ITbFundAcctService;
import com.taolue.baoxiao.fund.service.ITbFundBalanceService;
import com.taolue.baoxiao.fund.service.ITbFundTradeFlowService;
import com.taolue.baoxiao.fund.service.composite.IAccountService;
import com.taolue.baoxiao.fund.service.composite.IAcctBalanceBusiService;
import com.taolue.baoxiao.fund.service.composite.IBalanceBusiService;
import com.taolue.dict.api.dto.CommonDictDto;
import com.taolue.dict.api.vo.CommonDictVo;
import com.taolue.dock.api.common.DockEnum;
import com.taolue.dock.api.common.DockR;
import com.taolue.member.api.vo.MemberVo;

import cn.hutool.core.bean.copier.BeanCopier;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;

/**   
 * @ClassName:  AccountServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月28日 上午10:42:24   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Slf4j
@Service
public class AccountServiceImpl implements IAccountService {
	private Log logger = LogFactory.getLog(AccountServiceImpl.class);
	/**
	 * 企业所需账户
	 */
	private static String[] CM_ACCTCATES = new String[] {"10001","10002","10010","10006", "10007", "10017", "10019", "10020"};
	
	/**
	 * 部门所需账户
	 */
	private static String[] OG_ACCTCATES = new String[] {"10001","10002", "10019", "10020"};
	
	/**
	 * 项目所需账户
	 */
	private static String[] PJ_ACCTCATES = new String[] {"10001","10002", "10019", "10020"};
	
	/**
	 * 人员（独立C或者企业员工）所需账户
	 */
	private static String[] CPPE_ACCTCATES = new String[] {"10001","10002","10003","10004", "10005", "10019","10020", "10022"};
	
	/**
	 * 供应商所需账户
	 */
	private static String[] SP_ACCTCATES = new String[] {"10001", "10014", "10015", "10018"};
	
	/**
	 * 平台所需账户
	 */
	private static String[] PT_ACCTCATES = new String[] {"10001", "10011", "10012", "10013"};
	
	
	/**
	 * 个人账户体系
	 */
	private static String[] PERSON_HIERARCHY = new String[] {"10001","10002","10003","10004", 
			"10005", "10019","10020", "10022"};
	
	/**
	 * 组织账户体系
	 */
	private static String[] ORGANIZATION_HIERARCHY = new String[] {"10001","10002","10006", "10007", "10014", "10015", "10017", "10019", "10020"};
	
	/**
	 * 账户类型对应的资金科目编号
	 */
	private static Map<String, String[]> ACCT_BALANCEITEM_MAP = Maps.newHashMap();
	
	/**
	 * 创建账户是需要初始化对应资金记录的账户类型
	 */
	private static Map<String, String[]> INIT_ACCT_BALANCE_MAP = Maps.newHashMap();
	static {
		ACCT_BALANCEITEM_MAP.put("10006", new String[] {"10006","10006BZJ","10006BXJ"});
		ACCT_BALANCEITEM_MAP.put("10007", new String[] {"10007","10007BZJ","10007BXJ"});
		ACCT_BALANCEITEM_MAP.put("10022", new String[] {"10022SR","10022RU"});
		ACCT_BALANCEITEM_MAP.put("10011", new String[] {"10021","10012","10013"});
		
		INIT_ACCT_BALANCE_MAP.put("PERSON", new String[] {"10003","10004","10005","10022"});
		INIT_ACCT_BALANCE_MAP.put("ORGANIZATION", new String[] {"10003","10004","10006","10007", "10014","10015","10017"});
	}
	
	@Autowired
	private ITbFundAcctService fundAcctService;
	
	@Autowired
	private ITbFundAcctRelateService fundAccttRelateService;
	
	@Autowired
	private IAcctBalanceBusiService acctBalanceBusiService;
	
	@Autowired
	private ITbFundBalanceService  fundBalanceService;
	
	@Autowired
	private IBalanceBusiService balanceBusiService;
	@Autowired
	private ITbFundTradeFlowService fundTradeFlowService;
	
	@Autowired
	private IOpenJiaBaiTiaoBuyCouponService iOpenJiaBaiTiaoBuyCouponService;
	
	@Autowired
	private IDockOpenPlatformService iOpenCompanyInService;
	
	@Autowired
	private IRefactorMemberService refactorMemberService;
	
	@Autowired
	private RefactorCommonDictService refactorCommonDictService;

	/**   
	 * <p>Title: createAccount</p>   
	 * <p>Description: </p>   
	 * @param memberId
	 * @param memberCate
	 * @param acctCate
	 * @return   
	 * @see com.taolue.baoxiao.fund.service.composite.IAccountService#createAccount(java.lang.String, java.lang.String, java.lang.String)   
	 */  
	@Override
	@Transactional
	public List<FundAcctVo> createAccount(String memberId, String memberCate, String acctCate) {
		
		List<FundAcctVo> fundAcctVos = null;
//		List<TbFundAcct> fundAccts = this.createAccounts(memberId, memberCate, acctCate);
		
//		if (CollUtil.isNotEmpty(fundAccts)) {
//			fundAcctVos = Lists.newArrayList();
//			for (TbFundAcct fundAcct : fundAccts) {
//				fundAcctVos.add(new BeanCopier<FundAcctVo>(fundAcct, new FundAcctVo(), new CopyOptions()).copy());
//			}
//		}
		
		return fundAcctVos;
	}
	
	@Override
	public Map<String, Object> repairAccounts() {
		List<FundAcctVo> results = Lists.newArrayList();
		//1 修复企业的CM10007资金
//		Map<String, List<TbFundBalance>> result = repair10007Balance();
		
		
		//2 对已有账户会员新建10021账户
		List<FundAcctVo> accts = repair10021Acct();
		
		Map<String, Object> ret = Maps.newHashMap();
//		ret.put("B10007", result);
		ret.put("A10021", accts);
		return ret;
	}
	
	private List<FundAcctVo> repair10021Acct() {
		List<FundAcctVo> accts = Lists.newArrayList();
		List<String> inparam = Lists.newArrayList();
//		inparam.add("CM");
//		inparam.add("OG");
		inparam.add("CP");
		inparam.add("PE");
		List<Map<String,Object>> result = this.fundAcctService.selectMaps(Condition.create().setSqlSelect("DISTINCT member_cate, member_id ")
				.in("member_cate", inparam).orderBy("member_cate"));
		if (CollUtil.isNotEmpty(result)) {
			for (Map<String,Object> row : result) {
				EntityWrapper<TbFundAcct> wrapper = new EntityWrapper<>();
				wrapper.eq("acct_cate", row.get("member_cate").toString()+AcctCateEnums.ACCT_CATE_REIMBURSE.getCateCode());
				wrapper.eq("member_id", row.get("member_id").toString());
				List<TbFundAcct> hasAcct = this.fundAcctService.selectList(wrapper);
				if (CollUtil.isEmpty(hasAcct)) {
					List<FundAcctVo> resultAcct = this.createAccount(row.get("member_id").toString(), 
							row.get("member_cate").toString(), AcctCateEnums.ACCT_CATE_REIMBURSE.getCateCode());
					if (CollUtil.isNotEmpty(resultAcct)) {
						accts.addAll(resultAcct);
					}
				}
				
			}
		}
		return accts;
	}
	
	private Map<String, List<TbFundBalance>> repair10007Balance() {
    	EntityWrapper<TbFundBalance> wrapper = new EntityWrapper<>();
    	wrapper.eq("balance_item_code", CommonConstant.BALANCE_ITEM_NO_FULL_HFBZJ);
    	List<TbFundBalance> fundBalances = this.fundBalanceService.selectList(wrapper);
    	
    	Map<String, List<TbFundBalance>> result = Maps.newHashMap();
    	
    	List<TbFundBalance> toCreateBalances = result.get("TO");
    	if (CollUtil.isEmpty(toCreateBalances)) {
    		toCreateBalances = Lists.newArrayList();
    	}
    		
    	List<TbFundBalance> hadBalances = result.get("HS");
    	if (CollUtil.isEmpty(toCreateBalances)) {
    		hadBalances = Lists.newArrayList();
    	}
    	
    	if (CollUtil.isNotEmpty(fundBalances)) {
    		for (TbFundBalance fundBalance : fundBalances) {
    			wrapper = new EntityWrapper<>();
    	    	wrapper.eq("balance_item_code", CommonConstant.BALANCE_ITEM_NO_HFF);
    	    	wrapper.eq("acct_inst_no", fundBalance.getAcctInstNo());
    	    	List<TbFundBalance> balancehff = this.fundBalanceService.selectList(wrapper);
    	    	if (CollUtil.isEmpty(balancehff)) {
    	    		BeanCopier<TbFundBalance> coper = new BeanCopier<TbFundBalance>(fundBalance, new TbFundBalance(), 
        					new CopyOptions());
        			TbFundBalance tocreate = coper.copy();
        			tocreate.setId(null);
        			tocreate.setBalanceCode(CommonConstant.KEY_PERFIX_BALANCE_NO+IdWorker.getIdStr());
        			tocreate.setBalanceItemCode(CommonConstant.BALANCE_ITEM_NO_HFF);
        			tocreate.setBusiModel(CommonConstant.BALANCE_BUSI_MODEL_NONE);
        			tocreate.setRemark("data repair");
        			toCreateBalances.add(tocreate);
    	    	} else {
    	    		hadBalances.add(balancehff.get(0));
    	    	}
    			
    		}
    		if (CollUtil.isNotEmpty(toCreateBalances)) {
    			this.fundBalanceService.insertBatch(toCreateBalances);
    		}
    	}
    	
    	result.put("TO",toCreateBalances);
    	result.put("HS",hadBalances);
    	return result;
	}
	
//	private List<TbFundAcct> createAccounts(String memberId, String memberCate, String acctCate) {
//		List<TbFundAcct> fundAccts = Lists.newArrayList();
//		String[] inUseAcctCates = CM_ACCTCATES;
//		
//		//判断创建账号类型
//		//创建公司账户
//		if (MemberCateEnums.lookupByCode(memberCate).equals(MemberCateEnums.MEMBER_CATE_CMP)) {
//			inUseAcctCates = CM_ACCTCATES;
//		
//		//创建组织账户
//		} else if (MemberCateEnums.lookupByCode(memberCate).equals(MemberCateEnums.MEMBER_CATE_ORG)) {
//			inUseAcctCates = OG_ACCTCATES;
//		
//		//创建供应商账户
//		} else if (MemberCateEnums.lookupByCode(memberCate).equals(MemberCateEnums.MEMBER_CATE_SUP)) {
//			inUseAcctCates = SP_ACCTCATES;
//		
//		//创建平台账户
//		} else if (MemberCateEnums.lookupByCode(memberCate).equals(MemberCateEnums.MEMBER_CATE_PT)) {
//			inUseAcctCates = PT_ACCTCATES;
//			
//		//创建独立C或者企业员工账户
//		} else if (MemberCateEnums.lookupByCode(memberCate).equals(MemberCateEnums.MEMBER_CATE_ALO)
//				|| MemberCateEnums.lookupByCode(memberCate).equals(MemberCateEnums.MEMBER_CATE_EMP)) {
//			inUseAcctCates = CPPE_ACCTCATES;
//		} else if (MemberCateEnums.lookupByCode(memberCate).equals(MemberCateEnums.MEMBER_CATE_PJ)){
//			inUseAcctCates = PJ_ACCTCATES;
//		}
//		
//		if (StrUtil.isBlank(acctCate)) {
//			if (this.acctBalanceService.hadAccount(memberId)>0) {
//				FundServiceExceptionGenerator.FundServiceException("9002", 
//						new Object[] {memberId});
//			}
//			String mainAcctNo = null;
//			
//			List<TbFundAcctRelate> relates = Lists.newArrayList();
//			
//			for (String item : inUseAcctCates) {
//				TbFundAcct fundAcct = this.createSingleAccount(memberId, memberCate, item);
//				if (AcctCateEnums.ACCT_CATE_MASTER.getCateCode().equals(item)) {
//					mainAcctNo = fundAcct.getAcctInstNo();
//				} else {
//					TbFundAcctRelate relate = new TbFundAcctRelate();
//					relate.setRelateInstNo(fundAcct.getAcctInstNo());
//					relate.setRelateCate(AcctRelateCateEnums.ACCT_REL_CATE_PARENT.getCateCode());
//					relates.add(relate);
//				}
//				this.fundAcctService.insert(fundAcct);
//				fundAccts.add(fundAcct);	
////				this.initAcctBalance(fundAcct.getAcctInstNo(), fundAcct.getAcctCate(), item);
//			}
//			if (CollUtil.isNotEmpty(relates) && StrUtil.isNotBlank(mainAcctNo)) {
//				for (TbFundAcctRelate relate : relates) {
//					relate.setAcctInstNo(mainAcctNo);
//				}
//				this.fundAccttRelateService.insertBatch(relates);
//			}
//			
//		} else {
//			TbFundAcct mainFundAcct = this.acctBalanceService.findFundAcct(memberId, memberCate
//					+AcctCateEnums.ACCT_CATE_MASTER.getCateCode());
//			
//			if (null == mainFundAcct && !AcctCateEnums.ACCT_CATE_MASTER.getCateCode().equals(acctCate)) {
//				FundServiceExceptionGenerator.FundServiceException("9004", 
//						new Object[] {memberId, "", AcctCateEnums.lookupByCode(acctCate).getCateName()});
//			} 
//			
//			TbFundAcct fundAcct = this.createSingleAccount(memberId, memberCate, acctCate);
//			
//			this.fundAcctService.insert(fundAcct);
//			fundAccts.add(fundAcct);
////			this.initAcctBalance(fundAcct.getAcctInstNo(), fundAcct.getAcctCate(), acctCate);
//			
//			if (!AcctCateEnums.ACCT_CATE_MASTER.getCateCode().equals(acctCate)) {
//				TbFundAcctRelate relate = new TbFundAcctRelate();
//				relate.setAcctInstNo(mainFundAcct.getAcctInstNo());
//				relate.setRelateInstNo(fundAcct.getAcctInstNo());
//				relate.setRelateCate(AcctRelateCateEnums.ACCT_REL_CATE_PARENT.getCateCode());
//				this.fundAccttRelateService.insert(relate);
//			}
//		}
//		return fundAccts;
//	}
	
//	private void initAcctBalance(String acctInstNo, String fundAcctCate, String acctCate) {
//		//总账户和券账户，不初始化资金记录
//		if (AcctCateEnums.lookupByCode(acctCate).equals(AcctCateEnums.ACCT_CATE_COUPON)
//				|| AcctCateEnums.lookupByCode(acctCate).equals(AcctCateEnums.ACCT_CATE_MASTER)
//				|| AcctCateEnums.lookupByCode(acctCate).equals(AcctCateEnums.ACCT_CATE_DIRECT_COUPON)
//				|| AcctCateEnums.lookupByCode(acctCate).equals(AcctCateEnums.ACCT_CATE_NCOUNT_COUPON)
//				|| AcctCateEnums.lookupByCode(acctCate).equals(AcctCateEnums.ACCT_CATE_REIMBURSE)) {
//			return;
//			
//		//预充值账户和后付费账户需要单独创建资金记录，保证金资金记录和报销资金记录
//		} else if (AcctCateEnums.lookupByCode(acctCate).equals(AcctCateEnums.ACCT_CATE_PERGM)
//				|| AcctCateEnums.lookupByCode(acctCate).equals(AcctCateEnums.ACCT_CATE_AFTGM)) {
//			
//			this.creatSingleFundBalance(acctInstNo, fundAcctCate); 	 	
//			this.creatSingleFundBalance(acctInstNo, fundAcctCate+CommonConstant.BALANCE_ITEM_NO_SUFFX_BZJ);
//			this.creatSingleFundBalance(acctInstNo, fundAcctCate+CommonConstant.BALANCE_ITEM_NO_SUFFX_GBX);
//			
//		//其他类型账户创建fundAcctCate类型的资金记录
//		} else {
//			this.creatSingleFundBalance(acctInstNo, fundAcctCate);
//		}
//	}
	
//	private void creatSingleFundBalance(String acctInstNo, String fundAcctCate) {
//		try {
//			TbFundBalance oriFundBalance = acctBalanceService.findFundBalance(acctInstNo, fundAcctCate);
//			if (null != oriFundBalance && StrUtil.isNotBlank(oriFundBalance.getId())) {
//				FundServiceExceptionGenerator.FundServiceException("9005", 
//						new Object[] {acctInstNo, fundAcctCate});
//			}
//			TbFundBalance fundBalance = acctBalanceService.createFundBalance(acctInstNo, fundAcctCate,
//					CommonConstant.NO_AMOUNT, 
//					CommonConstant.NO_AMOUNT, CommonConstant.NO_AMOUNT);
//			
//		} catch (Exception e) {
//			if (e instanceof BaoxiaoException) {
//				log.error(e.getMessage());
//			} else {
//				throw new BaoxiaoException(e);
//			}
//		}
//	}
	
	private TbFundAcct createSingleAccount(String memberId, String memberCate, String acctCate) {
		TbFundAcct oriFundAdct = this.acctBalanceBusiService.findFundAcct(memberId, memberCate+acctCate);
		if (null != oriFundAdct) {
			FundServiceExceptionGenerator.FundServiceException("9003", 
					new Object[] {memberId, "", AcctCateEnums.lookupByCode(acctCate).getCateName()});
		}
		TbFundAcct fundAcct = new TbFundAcct();
		fundAcct.setAcctInstNo(CommonConstant.KEY_PERFIX_ACCT_NO+IdWorker.getIdStr());
		
		fundAcct.setAcctCate(acctCate);
		fundAcct.setName(MemberCateEnums.lookupByCode(memberCate).getCateName()
				+AcctCateEnums.lookupByCode(acctCate).getCateName());
		fundAcct.setMemberId(memberId);
		fundAcct.setMemberCate(memberCate);
		return fundAcct;
	}
	
	public List<FundAcctVo> createAccountWithCompany(String memberId, String memberCate, 
			String companyId, String... acctCates) {
		//默认创建组织账户体系
		String[] accountHierarchy = ORGANIZATION_HIERARCHY;
		String mapKey = "ORGANIZATION";
		//若会员类型为独立C或者企业员工，则创建个人账户体系
		if (MemberCateEnums.MEMBER_CATE_ALO.getCateCode().equals(memberCate) 
				|| MemberCateEnums.MEMBER_CATE_EMP.getCateCode().equals(memberCate)) {
			accountHierarchy = PERSON_HIERARCHY;
			mapKey = "PERSON";
		}
		
		//若指定了创建的账户类型
		if (ArrayUtil.isNotEmpty(acctCates)) {
			//则判断此账户类型是否和会员类型匹配
			for (String acctCate : acctCates) {
				//若账户类型和当前会员类型不匹配，则抛出异常
				if (!ArrayUtil.contains(accountHierarchy, acctCate)) {
					FundServiceExceptionGenerator.FundServiceException("90000", memberCate, acctCate); 
				}
			}
			//否则，以指定的账户类型创建账户体系
			accountHierarchy = acctCates;
		}
		List<FundAcctVo> returns = Lists.newArrayList();
		//创建账户体系
		for (String acctCate : accountHierarchy) {
			//查询当前账户类型是否已经创建
			TbFundAcct fundAcct = findOrCreateAcctByParams(mapKey, memberId, memberCate, companyId, acctCate);
			FundAcctVo fundAcctVo = new BeanCopier<FundAcctVo>(fundAcct, new FundAcctVo(), 
					new CopyOptions()).copy();
			returns.add(fundAcctVo);
		}
		return returns;
	}
	
	private TbFundAcct findOrCreateAcctByParams(String mapKey, String memberId, String memberCate, String companyId, String acctCate) {
		//查询当前账户类型是否已经创建
		TbFundAcct fundAcct = this.acctBalanceBusiService.findFundAcct(memberId, acctCate);
		if (null == fundAcct) {
			fundAcct = new TbFundAcct();
			fundAcct.setAcctInstNo(CommonConstant.KEY_PERFIX_ACCT_NO+IdWorker.getIdStr());
			fundAcct.setAcctCate(acctCate);
			fundAcct.setName(MemberCateEnums.lookupByCode(memberCate).getCateName()
					+AcctCateEnums.lookupByCode(acctCate).getCateName());
			fundAcct.setMemberId(memberId);
			fundAcct.setMemberCate(memberCate);
			fundAcct.insertOrUpdate();
		}
		List<TbFundBalance> balances = this.findOrCreateBalanceByParams(mapKey, 
				fundAcct.getAcctInstNo(), memberId, memberCate, companyId, acctCate);
		
		return fundAcct;
	}
	
	private String[] hasBalanceItemCodes(String mapKey, String acctCate) {
		String[] balanceItemCodes = null;
		//是否是需要初始化balance
		if (ArrayUtil.contains(INIT_ACCT_BALANCE_MAP.get(mapKey),acctCate)) {
			balanceItemCodes = new String[] {acctCate};
			if (ArrayUtil.isNotEmpty(ACCT_BALANCEITEM_MAP.get(acctCate))) {
				balanceItemCodes = ACCT_BALANCEITEM_MAP.get(acctCate);
			}
		}
		return balanceItemCodes;
	}
	
	private List<TbFundBalance> findOrCreateBalanceByParams(String mapKey, String acctInstNo, String memberId, String memberCate, 
			String companyId, String acctCate) {
		List<TbFundBalance> balances = Lists.newArrayList();
		String[] balanceItemCodes = hasBalanceItemCodes(mapKey, acctCate);
		if (ArrayUtil.isNotEmpty(balanceItemCodes)) {
			 for (String balanceItemCode : balanceItemCodes) {
				 BalanceSearchParams params = new BalanceSearchParams();
				 params.setAcctCate(acctCate);
				 params.setAcctInstNo(acctInstNo);
				 params.setCompanyId(companyId);
				 params.addMemberCate(memberCate);
				 params.addBalanceItemCode(balanceItemCode);
				 List<FundBalanceDto> fundBalanceDtos = this.acctBalanceBusiService.findBalancesByParams(params);
				 TbFundBalance balance = null;
				 if (CollUtil.isEmpty(fundBalanceDtos)) {
					 //新建balance
					 FundBalanceDto balanceDto = new FundBalanceDto(
								acctInstNo, null, balanceItemCode, CommonConstant.BALANCE_BUSI_MODEL_NONE, 
								CommonConstant.NO_AMOUNT, 
								CommonConstant.NO_AMOUNT, 
								CommonConstant.NO_AMOUNT, 
								null, null, null, null, null, null, null);
					 
					 balanceDto.setExtendAttre(memberCate);
					 balanceDto.setCompanyId(companyId);
					 balanceDto.setOwnerId(memberId);
					 balance = new BeanCopier<TbFundBalance>(balanceDto, new TbFundBalance(), 
								new CopyOptions()).copy();
				     this.fundBalanceService.insert(balance);
						
				 } else {
					 FundBalanceDto balanceDto = fundBalanceDtos.get(0);
					 balance = new BeanCopier<TbFundBalance>(balanceDto, new TbFundBalance(), 
								new CopyOptions()).copy();
				 }
				 
				 balances.add(balance);
			 }
		}
		return balances;
	}

	@Override
	@Transactional
	public R<Boolean> toActiveVoucher(FundBalanceDto dto) {
		//1:判断是否是工作日，
		//2：然后查询需要激活的账户信息
		//3:激活
		R<Boolean> r=new R<Boolean>();
		//判断是否是工作日
		boolean isWeek=isweekDays();
		CommonDictDto dictDto=new CommonDictDto();
		dictDto.setType("9");
		String newDateStr="";//时间
		R<List<CommonDictVo>> dictR=refactorCommonDictService.getDictList(dictDto);
		logger.info("读取测试的日期返回值:"+JSON.toJSONString(dictR));
		if(null == dictR || dictR.getCode()!=R.SUCCESS || dictR.getData().size()<=0) {
			newDateStr=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}else {
			try {
				newDateStr=new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyy-MM-dd").parse(dictR.getData().get(0).getValue()));
			} catch (ParseException e) {
				logger.error("读取测试转换时间错误",e);
			}
		}

		//boolean isWeek=true;
		
		if(!isWeek) {
			logger.info("当前日期不是工作日不做激活账户处理:"+newDateStr);
			return r;
		}
		dto.setComposAttr(newDateStr);
		//查询需要类型是工作日可激活的账户信息
		List<FundBalanceVo> balanceList=balanceBusiService.fundToActiveVoucherAccount(dto);
		logger.info("查询可激活的账户的数据为:"+JSON.toJSONString(balanceList));
		if(CollectionUtil.isEmpty(balanceList)) {
			r.setMsg("没有可激活的账户数据");
			return r;
		}
		//需要激活的balanceID
		List<TbFundBalance> acticeBalanceList=Lists.newArrayList();
		List<TbFundAcct> acctList=Lists.newArrayList();
		List<TbFundTradeFlow> tradeFlowList=Lists.newArrayList();
		BigDecimal flowOrder=new BigDecimal(1);//交易记录序号
		//list转memberIDList
		List<String> memberIds = balanceList.stream().map(FundBalanceVo::getMemberId)
				.collect(Collectors.toList());
		//转数组
		String[] ids=memberIds.toArray(new String[memberIds.size()]);
		//根据memberids查询信息
		List<MemberVo> memberVoList=refactorMemberService.queryMemberByIds(ids);
		
		//员工信息转map
		Map<String, MemberVo> memberMap = memberVoList.stream()
				.collect(Collectors.toMap(MemberVo::getId,o -> o));
		
		for (FundBalanceVo fundBalanceVo : balanceList) {
			logger.info("单次fundBalanceVo:"+JSON.toJSONString(fundBalanceVo));
			if(StringUtils.isBlank(fundBalanceVo.getExtendAttrc()) && 
					null==fundBalanceVo.getExpireTime() && null==fundBalanceVo.getValidTime()) {
				logger.info("这个人的资金账户使用有效期配置都为空，数据有误!");
				continue;
			}
			TbFundAcct acct=new TbFundAcct();
			acct.setId(fundBalanceVo.getAcctId());
			acct.setActiveTime(new Date());
			acct.setToActiveCount((fundBalanceVo.getToActiveCount()-1)+"");
			acctList.add(acct);
		}
		if(acctList.size()>0) {
			logger.info("激活批量修改acct的list为:"+JSON.toJSONString(acctList));
			boolean b2=fundAcctService.updateBatchById(acctList);
			logger.info("激活批量修改acct="+b2);
			if(!b2) {
				logger.info("激活失败");
				r.setCode(R.FAIL);
				r.setData(false);
				r.setMsg("激活失败");
				return r;
			}
		}
		for (FundBalanceVo fundBalanceVo : balanceList) {
			logger.info("单次fundBalanceVo:"+JSON.toJSONString(fundBalanceVo));
			if(StringUtils.isBlank(fundBalanceVo.getExtendAttrc()) && 
					null==fundBalanceVo.getExpireTime() && null==fundBalanceVo.getValidTime()) {
				logger.info("这个人的资金账户使用有效期配置都为空，数据有误!");
				continue;
			}
			
			TbFundBalance balanceVo=new TbFundBalance();
			balanceVo.setId(fundBalanceVo.getId());
			TbFundAcct acct=new TbFundAcct();
			acct.setId(fundBalanceVo.getAcctId());
			acct.setActiveTime(new Date());
			acct.setToActiveCount((fundBalanceVo.getToActiveCount()-1)+"");
			TbFundTradeFlow flow=loadTradeFlow(fundBalanceVo,"JH");
			flow.setTradeFlowOrder(flowOrder);
			flowOrder=flowOrder.add(new BigDecimal(1));
			
			//当使用有效天数不为空却大于哦
			if(StringUtils.isNotBlank(fundBalanceVo.getExtendAttrc()) && 
					Integer.parseInt(fundBalanceVo.getExtendAttrc())>0) {
				balanceVo.setValidTime(new Date());
				Date endDate=DateToolUtils.toNewDateAddDays(new Date(), Integer.parseInt(fundBalanceVo.getExtendAttrc()));
				logger.info("根据当前时间获取的失效时间为:"+endDate);
				balanceVo.setExpireTime(endDate);
			}else{
				balanceVo.setValidTime(fundBalanceVo.getValidTime());
				balanceVo.setExpireTime(fundBalanceVo.getExpireTime());
			}
			balanceVo.setStatus(BalanceStatus.IS_SHOP_0.getCateCode());
			acticeBalanceList.add(balanceVo);
			tradeFlowList.add(flow);
			acctList.add(acct);
			//查询到员工手机号
			MemberVo memberVo=memberMap.get(fundBalanceVo.getMemberId());
			fundBalanceVo.setPhone(memberVo.getMobile());
			//开始发送消息
			sendJHMessage(fundBalanceVo);
		}
	
		
		//当有激活数据区激活
		if(acticeBalanceList.size()>0) {
			//批量修改balance的激活
			logger.info("激活修改批量修改的balance的list为:"+JSON.toJSONString(acticeBalanceList));
			boolean b=fundBalanceService.updateBatchById(acticeBalanceList);
			logger.info("激活批量修改返回值为"+b);
			logger.info("激活批量添加tradeflow的list为:"+JSON.toJSONString(tradeFlowList));
			boolean b1=fundTradeFlowService.insertBatch(tradeFlowList);
			logger.info("批量修改balance的激活b1:"+b1+",b="+b);
			if(!b || !b1) {
				logger.info("激活失败");
				r.setCode(R.FAIL);
				r.setData(false);
				r.setMsg("激活失败");
			}
		
		}
		
		return r;
	}
	
	public TbFundTradeFlow  loadTradeFlow(FundBalanceVo fundBalanceVo,String type) {
		TbFundTradeFlow flow=new TbFundTradeFlow();
	
		
		flow.setTradeBusiCode("");
		flow.setBusiModel("BIM00001");
		flow.setBalanceCode(fundBalanceVo.getBalanceCode());
		if(type.equals("JH")) {//激活
			String jhCode=CodeUtils.genneratorShort("JH");
			flow.setTradeFlowCode(jhCode);
			flow.setTradePreAmount(new BigDecimal(0));
			flow.setTradeAmount(fundBalanceVo.getBalanceAmount());
			flow.setTradeLastAmount(fundBalanceVo.getBalanceAmount());
			flow.setTradeBusiCate(OrderType.ORDER_TYPE_MEMBER_DYQ_ACTIVE.getCateCode());
			flow.setTransBusiCateName(OrderType.ORDER_TYPE_MEMBER_DYQ_ACTIVE.getCateName());
			flow.setTradeCate(TradeCateEnums.TRADE_CATE_ACTIVE.getCateCode());
			flow.setTransCateName(TradeCateEnums.TRADE_CATE_ACTIVE.getCateName());
			flow.setSource("工作日激活");
			flow.setRemark("员工账户工作日激活抵用券");
		}else {
			String jhCode=CodeUtils.genneratorShort("SX");
			flow.setTradeFlowCode(jhCode);
			flow.setTradePreAmount(fundBalanceVo.getBalanceAmount());
			flow.setTradeAmount(fundBalanceVo.getBalanceAmount());
			flow.setTradeLastAmount(new BigDecimal(0));
			flow.setTradeBusiCate(OrderType.ORDER_TYPE_MEMBER_DYQ_LOSE.getCateCode());
			flow.setTransBusiCateName(OrderType.ORDER_TYPE_MEMBER_DYQ_LOSE.getCateName());
			flow.setTradeCate(TradeCateEnums.TRADE_CATE_LOSE.getCateCode());
			flow.setTransCateName(TradeCateEnums.TRADE_CATE_LOSE.getCateName());
			flow.setSource("账户失效");
			flow.setRemark("员工账户抵用券失效");
		}
		flow.setTransActCate(ActionType.ACTION_TYPE_UPD.getCateCode());
		flow.setTransActName(ActionType.ACTION_TYPE_UPD.getCateName());


		flow.setStatus("0");
		if(StringUtils.isNotBlank(UserUtils.getUser())) {
			flow.setCreator(UserUtils.getUser());
			flow.setUpdator(UserUtils.getUser());
		}else {
			flow.setCreator("admin");
			flow.setUpdator("admin");
		}
		return flow;
	}
	
    public  String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
        	logger.error("计算是否是工作日读取文件错误", e);
        } catch (IOException e) {
        	logger.error("计算是否是工作日读取文件错误", e);
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
        	logger.error("计算是否是工作日读取文件错误", e);
            return null;
        }
    }
    
    public boolean isweekDays() {

    	//文件目录
    /*	String dirUrl=System.getProperty("user.dir")+"\\src\\main\\resources\\riqi.txt";
    	logger.info("当前日期的文件目录:"+dirUrl);
    	if(StringUtils.isBlank(dirUrl)) {
    	   logger.info("读取工作日文件错误");
    	   return false;
    	}*/
    	try {
    		ClassLoader cl = this.getClass().getClassLoader();
    		InputStream in = cl.getResourceAsStream("riqi.txt");
    		String gzr = IOUtils.toString(in);
        	//String gzr=readToString(dirUrl);
        	logger.info("工作日文件读取的内容为:"+gzr);
        	Date newDate=new Date();
        	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        	String newDateStr=sdf.format(newDate);
        	//如果有这个日期就是工作日
        	if(gzr.contains(newDateStr)) {
        	   logger.info("当前是工作日");
           	   return true;	
        	}else {
        	   logger.info("当前不是工作日");
           	   return false;
        	}
		} catch (Exception e) {
			logger.error("读取工作日文件有误",e);
			return false;
		}
    
    }
    
    /**
     * 
     * @Title: sendJHMessage   
     * @Description:发送激活消息
     * @param: @param fundBalanceVo    
     * @author: duqiang     
     * @return: void      
     * @throws
     */
    public void sendJHMessage(FundBalanceVo fundBalanceVo) {
    	logger.info("开始根据来源发送模板消息:"+fundBalanceVo.getExtendAttrb());
    	String memberId=fundBalanceVo.getMemberId();
    	String phone=fundBalanceVo.getPhone();//手机号
    	BigDecimal amount=new BigDecimal(fundBalanceVo.getExtendAttre()).divide(new BigDecimal(1000)).setScale(2, BigDecimal.ROUND_HALF_UP);//面额
    	String voucherName=fundBalanceVo.getComposAttr();//抵用券名称
    	//当为嘉福
    	if(fundBalanceVo.getExtendAttrb().equals(sourceCode.source_code_FULI.getCateCode()) || fundBalanceVo.getExtendAttrb().equals(sourceCode.source_code_BAOXIAO.getCateCode())) {
    	/*	您的嘉福账户：xxxxx@jia-fu.cn有一张抵用券到账！
    		到账金额：5.00元
    		到账时间：2019-10-22 19:30:30
    		到账详情：新用户5元抵用券
    		备注：请登陆嘉福商城进入”我的-我的卡券-抵用券“查看并使用！*/
    		String first=phone;
    		String dzDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    	
    		String msgType=DockEnum.dataMsgType.data_Msg_Type_ALL.getCode();//ALL
        	String templateId=EnumJiaFu.TemplateMsgIdEnum.template_msg_jiafuwx_dztx.getId();//微信到账提醒
        	String templateData=first+"|"+amount+"|"+dzDate+"|"+voucherName;
        	String url="";
        	String app_templateId=EnumJiaFu.TemplateMsgIdEnum.template_msg_jiafuapp_dztx.getId();//app到账提醒
        	String app_templateData="{\"title\":\"抵用券到账提醒\",\"text\":\""+phone+"\"}";
        	logger.info("激活嘉福消息发送入参:msgType="+msgType+",templateId="+templateId+",templateData="+templateData);
        	logger.info("激活嘉福消息发送入参:app_templateId="+app_templateId+",app_templateData="+app_templateData);
        	//嘉福
        	DockR<Boolean> r=iOpenCompanyInService.taolueMessageSend(msgType, templateId, templateData, memberId, url, app_templateId, app_templateData);
        	logger.info("发送嘉福消息返回值:"+JSON.toJSONString(r));
    	}else if(fundBalanceVo.getExtendAttrb().equals(sourceCode.source_code_JIAXIN.getCateCode())){
    		//嘉薪
    		
        	String service="discount.card.reach";
        	String couponName=voucherName;
         	Long newDateLong=null;
			try {
				newDateLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).getTime();
			} catch (ParseException e) {
				logger.error("时间错误", e);
			}
        	String orderNo=fundBalanceVo.getId();
        	logger.info("发送嘉薪激活消息参数为:amount="+amount+",service="+service+",couponName="+couponName
        			+",memberId="+memberId+",paymentDate="+newDateLong+",orderNo="+orderNo);
        	//嘉薪
        	DockR r=iOpenJiaBaiTiaoBuyCouponService.couponInform(amount, service, couponName, memberId, newDateLong, orderNo);
        	logger.info("发送嘉薪消息返回值:"+JSON.toJSONString(r));
    	}
    }

	@Override
	@Transactional
	public R<Boolean> loseVoucher(FundBalanceDto dto) {
		R<Boolean> r=new R<Boolean>();
		CommonDictDto dictDto=new CommonDictDto();
		dictDto.setType("9");
		Date nowDate=new Date();//时间
		R<List<CommonDictVo>> dictR=refactorCommonDictService.getDictList(dictDto);
		logger.info("读取测试的日期返回值:"+JSON.toJSONString(dictR));
		if(dictR.getCode()==R.SUCCESS && dictR.getData().size()>0) {
			try {
				nowDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dictR.getData().get(0).getValue());
			} catch (ParseException e) {
				logger.error("时间转换异常", e);
			}
		}
		dto.setNowDate(nowDate);
		List<FundBalanceVo> voList=balanceBusiService.findWaitLoseVoucher(dto);
		logger.info("查询待失效的数据为："+JSON.toJSONString(voList));
		List<TbFundBalance> acticeBalanceList=Lists.newArrayList();
		List<TbFundTradeFlow> tradeFlowList=Lists.newArrayList();

		BigDecimal flowOrder=new BigDecimal(1);//交易记录序号
		for (FundBalanceVo fundBalanceVo : voList) {
			TbFundBalance balance=new TbFundBalance();
			balance.setId(fundBalanceVo.getId());
			balance.setStatus(BalanceStatus.EXPIRED_3.getCateCode());
			TbFundTradeFlow flow=loadTradeFlow(fundBalanceVo,"SX");
			flow.setTradeFlowOrder(flowOrder);
			flowOrder=flowOrder.add(new BigDecimal(1));
			tradeFlowList.add(flow);
			acticeBalanceList.add(balance);
		}
		if(acticeBalanceList.size()>0) {
			//批量修改balance的
			logger.info("失效修改批量修改的balance的list为:"+JSON.toJSONString(acticeBalanceList));
			boolean b=fundBalanceService.updateBatchById(acticeBalanceList);
			logger.info("失效批量修改返回值为"+b);
			logger.info("失效批量添加tradeflow的list为:"+JSON.toJSONString(tradeFlowList));
			boolean b1=fundTradeFlowService.insertBatch(tradeFlowList);
			if(!b || !b1) {
				logger.info("失效失败");
				r.setCode(R.FAIL);
				r.setData(false);
				r.setMsg("失效失败");
			}
		}
		
		return r;
	}

	@Override
	public boolean toThreeWaitLoseWain(FundBalanceDto dto) {
		
		CommonDictDto dictDto=new CommonDictDto();
		dictDto.setType("9");
		Date nowDate=new Date();//时间
		R<List<CommonDictVo>> dictR=refactorCommonDictService.getDictList(dictDto);
		logger.info("读取测试的日期返回值:"+JSON.toJSONString(dictR));
		if(dictR.getCode()==R.SUCCESS && dictR.getData().size()>0) {
			try {
				nowDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dictR.getData().get(0).getValue());
			} catch (ParseException e) {
				logger.error("时间转换异常", e);
			}
		}
		dto.setNowDate(nowDate);
		List<FundBalanceVo> voList=balanceBusiService.findThreeWaitLoseVoucher(dto);
		logger.info("查询需要三天提醒的数据返回值:"+JSON.toJSONString(voList));
		
		if(null !=voList && voList.size()>0) {
			List<String> memberIds = voList.stream().map(FundBalanceVo::getMemberId)
					.collect(Collectors.toList());
			//转数组
			String[] ids=memberIds.toArray(new String[memberIds.size()]);
			//根据memberids查询信息
			List<MemberVo> memberVoList=refactorMemberService.queryMemberByIds(ids);
			
			//员工信息转map
			Map<String, MemberVo> memberMap = memberVoList.stream()
					.collect(Collectors.toMap(MemberVo::getId,o -> o));
			
			for (FundBalanceVo fundBalanceVo : voList) {
				//查询到员工手机号
				MemberVo memberVo=memberMap.get(fundBalanceVo.getMemberId());
				fundBalanceVo.setPhone(memberVo.getMobile());
				fundBalanceVo.setNowDate(nowDate);
				//开始发送消息
				sendDQMessage(fundBalanceVo);
			}
		}
		return true;
	}
	
	
	/**
	 * 
	 * @Title: sendDQMessage   
	 * @Description: 发送到期提醒
	 * @param: @param fundBalanceVo    
	 * @author: duqiang     
	 * @return: void      
	 * @throws
	 */
	 public void sendDQMessage(FundBalanceVo fundBalanceVo) {
	    	logger.info("开始根据来源发送到期模板消息:"+fundBalanceVo.getExtendAttrb());
	    	String memberId=fundBalanceVo.getMemberId();
	    	String phone=fundBalanceVo.getPhone();//手机号
	    	BigDecimal amount=new BigDecimal(fundBalanceVo.getExtendAttre()).divide(new BigDecimal(1000)).setScale(2, BigDecimal.ROUND_HALF_UP);//面额
	    	String voucherName=fundBalanceVo.getComposAttr();//抵用券名称
	    	String  exDate=new SimpleDateFormat("yyyy-MM-dd").format(fundBalanceVo.getExpireTime());//到期时间
	    	//当为嘉福
	    	if(fundBalanceVo.getExtendAttrb().equals(sourceCode.source_code_FULI.getCateCode()) || fundBalanceVo.getExtendAttrb().equals(sourceCode.source_code_BAOXIAO.getCateCode())) {
	    	/*	您的嘉福账户：xxxxx@jia-fu.cn有一张抵用券即将过期！
				业务号码：
				业务类型：新用户5元抵用券
				到期时间：2019-10-31
				备注：请尽快登陆使用！*/
	    		String first=phone;
	    	
	    		String msgType="ALL";
	        	String templateId=TemplateMsgIdEnum.template_msg_jiafuwx_dqtx.getId();//到期提醒
	        	String templateData=first+"|"+fundBalanceVo.getId()+"|"+voucherName+"-"+new BigDecimal(fundBalanceVo.getExtendAttre()).divide(new BigDecimal(1000))+"元"+"|"+exDate;
	        	String url="";
	        	String app_templateId=TemplateMsgIdEnum.template_msg_jiafuapp_dqtx.getId();
	        	String app_templateData="{\"title\":\"抵用券过期提醒\",\"text\":\""+phone+"|"+amount+"|"+exDate+"\"}";
	        	logger.info("到期嘉福消息发送入参:msgType="+msgType+",templateId="+templateId+",templateData="+templateData);
	        	logger.info("到期嘉福消息发送入参:app_templateId="+app_templateId+",app_templateData="+app_templateData);
	        	//嘉福
	        	DockR<Boolean> r=iOpenCompanyInService.taolueMessageSend(msgType, templateId, templateData, memberId, url, app_templateId, app_templateData);
	        	logger.info("发送到期嘉福消息返回值:"+JSON.toJSONString(r));
	    	}else if(fundBalanceVo.getExtendAttrb().equals(sourceCode.source_code_JIAXIN.getCateCode())){
	    		//嘉薪
	    		
	        	String service="discount.card.expire";
	        	String couponName=voucherName+"-"+new BigDecimal(fundBalanceVo.getExtendAttre()).divide(new BigDecimal(1000))+"元";
	        	//Date paymentDate=new Date();
	        	Long newDateLong=null;
				try {
					newDateLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fundBalanceVo.getExpireTime())).getTime();
				} catch (ParseException e) {
					logger.error("时间错误", e);
				}
	        	String orderNo=fundBalanceVo.getId();
	        	logger.info("发送嘉薪到期消息参数为:amount="+amount+",service="+service+",couponName="+couponName
	        			+",memberId="+memberId+",paymentDate="+newDateLong+",orderNo="+orderNo);
	        	//嘉薪
	        	
	        	DockR r=iOpenJiaBaiTiaoBuyCouponService.couponInform(amount, service, couponName, memberId, newDateLong, orderNo);
	        	logger.info("发送到期嘉薪消息返回值:"+JSON.toJSONString(r));
	    	}
	    }
	 public static void main(String[] args) {
		 try {
			 BigDecimal b=new BigDecimal(5000).divide(new BigDecimal(1000));
		     System.out.println("url="+b);
		    
		     System.out.println(b.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	 
}
