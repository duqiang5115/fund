package com.taolue.baoxiao.fund.service;

import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.vo.FundAcctVo;
import com.taolue.baoxiao.fund.entity.TbFundAcct;

/**
 * 
 * @ClassName:  ITbFundAcctService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:48:19   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface ITbFundAcctService extends IService<TbFundAcct> {

	public R<FundAcctVo> AddCompanyAcc(FundAcctVo dto);
	
//	/**
//	 * 
//	 * @Title: findAcctByMemberIdAndMemberCateAndAcctCate   
//	 * @Description: TODO(这里用一句话描述这个方法的作用) 
//	 * @Author: shilei
//	 * @date:   2018年8月20日 下午2:48:48  
//	 * @param: @param memberId
//	 * @param: @param membeCateCode
//	 * @param: @param acctCateCode
//	 * @param: @return      
//	 * @return: TbFundAcct      
//	 * @throws
//	 */
//	public TbFundAcct findAcctByMemberIdAndMemberCateAndAcctCate(String memberId, String membeCateCode, 
//			String acctCateCode);
//	
//	/**
//	 * 
//	 * @Title: findAcctByMemberIdAndMemberCateAndAcctCate   
//	 * @Description: TODO(这里用一句话描述这个方法的作用) 
//	 * @Author: shilei
//	 * @date:   2018年8月20日 下午2:48:53  
//	 * @param: @param memberId
//	 * @param: @param membeCateCode
//	 * @param: @param acctCateCode
//	 * @param: @return      
//	 * @return: TbFundAcct      
//	 * @throws
//	 */
//	public TbFundAcct findAcctByMemberIdAndMemberCateAndAcctCate(String memberId, MemberCateEnums membeCateCode, 
//			AcctCateEnums acctCateCode);
//	
//	/**
//	 * 
//	 * @Title: findFundAcctByMemberIdFundAcctCate   
//	 * @Description: TODO(这里用一句话描述这个方法的作用) 
//	 * @Author: shilei
//	 * @date:   2018年8月20日 下午2:48:57  
//	 * @param: @param memberId
//	 * @param: @param fundAcctCate
//	 * @param: @return      
//	 * @return: TbFundAcct      
//	 * @throws
//	 */
//	public TbFundAcct findFundAcctByMemberIdFundAcctCate(String memberId, String fundAcctCate);
//	
//	/**
//	 * 
//	 * @Title: hadAccount   
//	 * @Description: TODO(这里用一句话描述这个方法的作用) 
//	 * @Author: shilei
//	 * @date:   2018年8月20日 下午2:49:02  
//	 * @param: @param memberId
//	 * @param: @param acctCate
//	 * @param: @return      
//	 * @return: String      
//	 * @throws
//	 */
//	public String hadAccount(String memberId, String acctCate);
//	
//	/**
//	 * 
//	 * @Title: hadAccount   
//	 * @Description: TODO(这里用一句话描述这个方法的作用) 
//	 * @Author: shilei
//	 * @date:   2018年9月3日 下午3:00:39  
//	 * @param: @param memberId
//	 * @param: @return      
//	 * @return: int      
//	 * @throws
//	 */
//	public int hadAccount(String memberId) ;
//	
//	public TbFundAcct findAcctByAcctInstNo(String acctInstNo);
	
}
