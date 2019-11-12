/**  
 * All rights Reserved, Designed By baoxiao
 * @Title:  IAccountService.java   
 * @Package com.taolue.baoxiao.fund.service   
 * @Description:    TODO(用一句话描述该文件做什么)   
 * @Author: shilei  
 * @date:   2018年8月28日 上午10:39:33   
 * @version V1.0 
 * @Copyright: 2018 www。jia-fu.cn. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */  
package com.taolue.baoxiao.fund.service.composite;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;

import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.FundAcctDto;
import com.taolue.baoxiao.fund.api.dto.FundBalanceDto;
import com.taolue.baoxiao.fund.api.vo.FundAcctVo;

/**   
 * @ClassName:  IAccountService   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月28日 上午10:39:33   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public interface IAccountService {
	
	/**
	 * 创建账户接口，为指定的对象批量创建账户或者单独创建指定类型的账户
	 * 如果账户类型参数不传，则按以下处理
	 * 1、如果是企业对象，则会创建如下账户
	 * 	  a、保证金账户
	 *    b、报销账户
	 *    c、券账户
	 *    d、福利账户
	 *    e、薪资账户
	 * 2、如果是部门对象，则会创建如下账户
	 *    a、券账户
	 * 3、如果是员工对象，则会创建如下账户
	 * 	  a、薪资账户
	 *    b、券账户
	 *    c、福利账户
	 *    d、福豆账户
	 * 4、如果是商户对象，则会创建如下账户
	 *    a、平台对应商户额度账户
	 *    b、平台对应商户现金账户
	 * 5、如果是平台对象，则会创建如下账户
	 * 	  a、跞洲服务账户
	 *    b、垫资服务账户
	 *    c、延期服务账户  
	 * 如果账户类型指定，则只会为对象创建相应类型账户    
	 * @Title: createAccount   
	 * @Author: shilei
	 * @date:   2018年8月27日 下午9:47:01  
	 * @param: @param memberId String 必传 预创建账户对象的标识
	 * @param: @param memberCate String 必传 预创建账户对象的类型，参见enum值
	 * @param: @param acctCate 预创建的账户类型，参见enum值     
	 * @return: R<List<FundAcctVo>> 创建成功的账户对象列表   
	 *   
	 * @throws
	 */
	List<FundAcctVo> createAccount(String memberId, String memberCate, String acctCate); 
	
	Map<String, Object> repairAccounts();
	
	/**
	 * 
	 * <p>名称:类IAccountService中的createAccountWithCompany方法</br>    
	 * <p>描述: 创建账户</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Jan 22, 2019 8:20:08 PM</br>
	 * @throws Exception
	 * @param memberId {@link String} 会员编号 必传
	 * @param memberCate {@link String} 会员类型 必传
	 * @param companyId {@link String} 会员归属公司，如果会员类型为企业，则该字段为会员编号；必传
	 * @param acctCates {@link String[]} 需要创建的账户类型 可选；若指定了需要创建的账户类型，则只针对这些类型创建账户
	 * @return {@link List}{@literal <}FundAcctVo{@literal >} 本次创建的账户列表
	 */
	
    List<FundAcctVo> createAccountWithCompany(String memberId, String memberCate, 
			String companyId, String... acctCates);
    
    /**
     * 
     * @Title: toActiveVoucher   
     * @Description: 工作日激活员工抵用券
     * @param: @param 如果是定时跑批不用传任何参数,如果是员工发放激活memberIdList里面放入memberID
     * @param: @return    
     * @author: duqiang     
     * @return: R<Boolean>      
     * @throws
     */
    public R<Boolean> toActiveVoucher(FundBalanceDto dto);
    
    /**
     * 
     * @Title: loseVoucher   
     * @Description: 抵用券失效
     * @param: @param dto
     * @param: @return    
     * @author: duqiang     
     * @return: R<Boolean>      
     * @throws
     */
    public R<Boolean> loseVoucher(FundBalanceDto dto);
    
    /**
     * 
     * @Title: toThreeWaitLoseWain   
     * @Description: 抵用券三天到期提醒
     * @param: @param dto
     * @param: @return    
     * @author: duqiang     
     * @return: FundBalanceDto      
     * @throws
     */
	public  boolean toThreeWaitLoseWain(FundBalanceDto dto);
}
