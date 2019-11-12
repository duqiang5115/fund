package com.taolue.baoxiao.fund.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.AcctCateEnums;
import com.taolue.baoxiao.common.util.CodeUtils;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.vo.FundAcctVo;
import com.taolue.baoxiao.fund.entity.TbFundAcct;
import com.taolue.baoxiao.fund.mapper.TbFundAcctMapper;
import com.taolue.baoxiao.fund.service.ITbFundAcctService;
import com.taolue.baoxiao.fund.service.composite.IBalanceBusiService;
import com.taolue.member.api.api.MemberApi;
import com.taolue.member.api.api.MemberCompanyApi;
import com.taolue.member.api.vo.CompanyDetailVo;
import com.taolue.member.api.vo.MemberVo;

/**
 * 
 * @ClassName:  TbFundAcctServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:52:23   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
public class TbFundAcctServiceImpl extends ServiceImpl<TbFundAcctMapper, TbFundAcct> implements ITbFundAcctService {
	@Autowired
	private IBalanceBusiService balanceBusiService;
	
    @Autowired
    private MemberCompanyApi memberCompanyApi;
    
    @Autowired
    private MemberApi memberApi;

	@Override
	public R<FundAcctVo> AddCompanyAcc(FundAcctVo dto) {
		R<FundAcctVo> r = new R<FundAcctVo>();
		if(AcctCateEnums.ACCT_CATE_COMPANY_JF_ACCOUNT.getCateCode().equals(dto.getAcctCate())) {
			dto.setAcctInstNo(CodeUtils.genneratorShort("CJF"));
			dto.setName(DictionaryEnum.AcctCateEnums.ACCT_CATE_COMPANY_JF_ACCOUNT.getCateName());
			CompanyDetailVo queryCompanyDetail = memberCompanyApi.queryCompanyDetail(dto.getMemberId());
			dto.setMemberName(queryCompanyDetail.getCompanyName());
		}
		if(AcctCateEnums.ACCT_CATE_COMPANY_DYQ_ACCOUNT.getCateCode().equals(dto.getAcctCate())) {
			dto.setAcctInstNo(CodeUtils.genneratorShort("CDYQ"));
			dto.setName(DictionaryEnum.AcctCateEnums.ACCT_CATE_COMPANY_DYQ_ACCOUNT.getCateName());
			CompanyDetailVo queryCompanyDetail = memberCompanyApi.queryCompanyDetail(dto.getMemberId());
			if(StringUtils.isNotBlank(queryCompanyDetail.getCompanyName())) {
				dto.setMemberName(queryCompanyDetail.getCompanyName());
			}
		}
		if(AcctCateEnums.ACCT_CATE_MEMBER_DYQ_ACCOUNT.getCateCode().equals(dto.getAcctCate())) {
			dto.setAcctInstNo(CodeUtils.genneratorShort("UDYQ"));
			dto.setName(DictionaryEnum.AcctCateEnums.ACCT_CATE_MEMBER_DYQ_ACCOUNT.getCateName());
			MemberVo queryCompanyDetail = memberApi.findMemberInfoByMemberId(dto.getMemberId());
			if(StringUtils.isNotBlank(queryCompanyDetail.getNickName())) {
				dto.setMemberName(queryCompanyDetail.getNickName());
			}
		}
		TbFundAcct vo = new TbFundAcct();
		BeanUtils.copyProperties(dto, vo);
		boolean flag = this.insert(vo);
		FundAcctVo accvo = balanceBusiService.findCompanyIntegralAcc(dto);
		if(flag) {
			r.setCode(R.SUCCESS);
			r.setData(accvo);
			r.setMsg("创建成功");
		}else {
			r.setCode(R.FAIL);
			r.setMsg("创建失败");
		}
		return r;
	}
	
//	@Autowired
//	private ITbFundAcctRelateService fundAccttRelateService;
//	
//	public TbFundAcct findAcctByMemberIdAndMemberCateAndAcctCate(String memberId, MemberCateEnums membeCateCode, AcctCateEnums acctCateCode) {
//    	EntityWrapper<TbFundAcct> acctWrapper = new EntityWrapper<>();
//    	acctWrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
//    	acctWrapper.eq("member_id", memberId);
//    	acctWrapper.eq("acct_cate", membeCateCode.getCateCode()+acctCateCode.getCateCode());
//    	
//    	TbFundAcct fundAcct = this.selectOne(acctWrapper);
//    	if (null != fundAcct) {
//    		return fundAcct;
//    	}
//    	
//    	return null;
//    }
//	
//	public TbFundAcct findFundAcctByMemberIdFundAcctCate(String memberId, String fundAcctCate) {
//    	EntityWrapper<TbFundAcct> acctWrapper = new EntityWrapper<>();
//    	acctWrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
//    	acctWrapper.eq("member_id", memberId);
//    	acctWrapper.eq("acct_cate", fundAcctCate);
//    	TbFundAcct fundAcct = this.selectOne(acctWrapper);
//    	if (null != fundAcct) {
//    		return fundAcct;
//    	}
//    	
//    	return null;
//    }
//	
//	public TbFundAcct findAcctByMemberIdAndMemberCateAndAcctCate(String memberId, String membeCateCode, String acctCateCode) {
//    	EntityWrapper<TbFundAcct> acctWrapper = new EntityWrapper<>();
//    	acctWrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
//    	acctWrapper.eq("member_id", memberId);
//    	acctWrapper.eq("acct_cate", membeCateCode+acctCateCode);
//    	
//    	TbFundAcct fundAcct = this.selectOne(acctWrapper);
//    	if (null != fundAcct) {
//    		return fundAcct;
//    	}
//    	
//    	return null;
//    }
//	
//	public String hadAccount(String memberId, String acctCate) {
//		EntityWrapper<TbFundAcct> wrapper = new EntityWrapper<TbFundAcct>();
//        wrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
//        wrapper.eq("member_id", memberId);
//        wrapper.eq("acct_cate", acctCate);
//		List<TbFundAcct> fundAccts = this.selectList(wrapper);
//		if (CollUtil.isNotEmpty(fundAccts)) {
//			return fundAccts.get(0).getAcctInstNo();
//		}
//		return null; 
//	}
//	
//	public int hadAccount(String memberId) {
//		EntityWrapper<TbFundAcct> wrapper = new EntityWrapper<TbFundAcct>();
//        wrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
//        wrapper.eq("member_id", memberId);
//		List<TbFundAcct> fundAccts = this.selectList(wrapper);
//		if (CollUtil.isNotEmpty(fundAccts)) {
//			return fundAccts.size();
//		}
//		return 0; 
//	}
//	
//	public void createRelate(String mainAcctNo, String relateAcctNo, AcctRelateCateEnums relateCate) {
//		TbFundAcctRelate relate = new TbFundAcctRelate();
//		relate.setAcctInstNo(mainAcctNo);
//		relate.setRelateCate(relateCate.getCateCode());
//		relate.setRelateInstNo(relateAcctNo);
//		relate.setRemark(relateCate.getCateName());
//		this.fundAccttRelateService.insert(relate);
//	}
//	
//	public TbFundAcct findAcctByAcctInstNo(String acctInstNo) {
//    	EntityWrapper<TbFundAcct> acctWrapper = new EntityWrapper<>();
//    	acctWrapper.eq(CommonConstant.DEL_FLAG, CommonConstant.STATUS_NORMAL);
//    	acctWrapper.eq("acct_inst_no", acctInstNo);
//    	TbFundAcct fundAcct = this.selectOne(acctWrapper);
//    	if (null != fundAcct) {
//    		return fundAcct;
//    	}
//    	return null;
//    }
}
