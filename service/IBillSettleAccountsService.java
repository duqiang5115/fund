package com.taolue.baoxiao.fund.service;
import java.util.List;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.Query;
import com.taolue.baoxiao.common.util.Result;
import com.taolue.baoxiao.fund.api.dto.BillSettleAccountsDto;
import com.taolue.baoxiao.fund.api.vo.BillSettleAccountsVo;
import com.taolue.baoxiao.fund.entity.BillSettleAccounts;
import com.taolue.invoice.api.dto.ReimburseAmountSetDto;
import com.taolue.member.api.vo.PlatformVo;
import com.taolue.member.api.vo.RelationVo;

/**
 * <p>
 * 结算订单表 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-05-27
 */
public interface IBillSettleAccountsService extends IService<BillSettleAccounts> {
	/**
	 * 
	 * 
	 * @Title IBillSettleAccountsService.addBillSettleAccounts
	 * @Description: 添加结算订单
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月03日 下午2:27:48
	 * 修改内容 :
	 */
	public boolean addBillSettleAccounts(BillSettleAccountsDto dto) throws Exception;
	/**
	 * 
	 * 
	 * @Title IBillSettleAccountsService.findBillSettleAccounts
	 * @Description: 查询结算订单信息
	 *
	 * @param id
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月03日 下午3:39:37
	 * 修改内容 :
	 */
	public List<BillSettleAccountsVo> findBillSettleAccounts(BillSettleAccountsDto dto);
	
	/**
	 * 
	 * 
	 * @Title IBillSettleAccountsService.findBillSettleAccounts
	 * @Description: 查询结算订单信息
	 *
	 * @param id
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月03日 下午3:39:37
	 * 修改内容 :
	 */
	public List<BillSettleAccountsVo> findBillSettleInfoList(BillSettleAccountsDto dto);
	
	/**
	 * 
	 * 
	 * @Title IBillSettleAccountsService.findPageBillSettleAccounts
	 * @Description: 员工申请账单列表接口
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author fbh
	 * 修改历史: 
	 * 修改人: fbh, 修改日期 : 2019年06月13日 上午14:17:15
	 * 修改内容 :
	 */
	public Page<BillSettleAccountsDto> findPageBillSettleAccounts(Query query, BillSettleAccountsDto queryParams);
	
	/**
	 * 根据companyId查询用户信息
	 * @param vo
	 * @return
	 */
	public List<PlatformVo> findMemberPlatform(PlatformVo vo);
	/**
	 * 
	 * 
	 * @Title IBillSettleAccountsService.findSettlementDetailList
	 * @Description: 员工申请账单列表明细列表接口
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author fbh
	 * 修改历史: 
	 * 修改人: fbh, 修改日期 : 2019年06月14日 上午10:17:15
	 * 修改内容 :
	 */
	public Page<BillSettleAccountsDto> findSettlementDetailList(Query query, BillSettleAccountsDto queryParams);
	/**
	 * 
	 * 
	 * @Title IBillSettleAccountsService.queryDistinctPostByMemberId
	 * @Description: 查询员工岗位信息
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author fbh
	 * 修改历史: 
	 * 修改人: fbh, 修改日期 : 2019年06月14日 上午11:17:15
	 * 修改内容 :
	 */
	public RelationVo queryDistinctPostByMemberId(String memberId,String companyId);
	/**
	 * 
	 * 
	 * @Title IBillSettleAccountsService.queryDistinctPostByMemberId
	 * @Description: 查询员工职级信息
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author fbh
	 * 修改历史: 
	 * 修改人: fbh, 修改日期 : 2019年06月14日 上午11:29:15
	 * 修改内容 :
	 */
	public RelationVo queryDistinctRankByMemberId(String memberId,String companyId);
	/**
	 * 
	 * 
	 * @Title IBillSettleAccountsService.queryDistinctPostByMemberId
	 * @Description: 查询员工结算金额信息
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author fbh
	 * 修改历史: 
	 * 修改人: fbh, 修改日期 : 2019年06月14日 上午11:39:15
	 * 修改内容 :
	 */
	public BillSettleAccountsDto findSettlementDetail(BillSettleAccountsDto queryParams);
	
	List<BillSettleAccountsDto> findSettlementAmountByNo(BillSettleAccountsDto queryParams);
	boolean reduceMemberAmount(ReimburseAmountSetDto dto) throws Exception ;
	
	/**
	 * 修改结算单状态
	 * @Title: editStatusByNos   
	 * @Description: TODO(这里用一句话描述这个方法的作用)   
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: boolean      
	 * @throws
	 */
	boolean editStatusByNos(BillSettleAccountsDto dto);
	/**
	 * 
	 * @Title: findPageBillBillDetail   
	 * @Description:员工报销汇总
	 * @author: zyj
	 * @date:   2019年6月19日 下午3:16:47  
	 * @param: @param query
	 * @param: @param queryParams
	 * @param: @return      
	 * @return: Page<BillSettleAccountsDto>      
	 * @throws
	 */
	public Page<BillSettleAccountsDto> findPageBillDetail(Query query, BillSettleAccountsDto queryParams);
	/**
	 * 
	 * @Title: findPageBillDetailBybillNumber   
	 * @Description: 报销明细
	 * @author: zyj
	 * @date:   2019年6月19日 下午3:46:07  
	 * @param: @param query
	 * @param: @param queryParams
	 * @param: @return      
	 * @return: Page<BillSettleAccountsDto>      
	 * @throws
	 */
	public Page<BillSettleAccountsDto> findPageBillDetailBybillNumber(Query query, BillSettleAccountsDto queryParams);
	
	/**
	 * 
	 * @Title: findAmountByMember   
	 * @Description: 通过账单号查询每个人的报销金额
	 * @param: @param dto
	 * @param: @return    
	 * @author: duqiang     
	 * @return: List<BillSettleAccountsVo>      
	 * @throws
	 */
	List<BillSettleAccountsDto> findAmountByMember(BillSettleAccountsDto dto);
	
	/**
	 * 开始智能报销
	 * @param dto
	 * @return
	 */
	public Result<Boolean> intelligentReimburse(BillSettleAccountsDto dto);
	
	public BillSettleAccountsDto separateBill(BillSettleAccountsDto dto,String settleNo) throws Exception;
	
	
	/**
	 * 
	 * 
	 * @Title IBillSettleAccountsService.revokeBillSettleAccount
	 * @Description: 撤销、拒绝结算单信息
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年09月16日 下午6:02:30
	 * 修改内容 :
	 */
	boolean revokeBillSettleAccount(BillSettleAccountsDto dto) throws Exception;
	
	
	/**
	 * 
	 * 
	 * @Title IBillSettleAccountsService.updateBatchBillSettleAccounts
	 * @Description: 批量修改结算单信息
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年09月17日 下午4:10:15
	 * 修改内容 :
	 */
	boolean updateBatchBillSettleAccounts(BillSettleAccountsVo dto) throws Exception;
	/**
	 * 查询结算订单列表
	 * @param dto
	 * @return
	 */
	public List<BillSettleAccountsDto> findBillSettleAccountDetail(BillSettleAccountsDto dto);
	
}
