package com.taolue.baoxiao.fund.service;

import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.fund.api.dto.ApplyInvoiceDto;
import com.taolue.baoxiao.fund.api.vo.*;
import com.taolue.baoxiao.fund.entity.TbInvoiceApply;

import java.math.BigDecimal;
import java.util.List;


public interface IApplyInvoiceService extends IService<TbInvoiceApply> {


    /**
     * 根据人员id,消费券id获取开票历史记录
     * @Param applyInvoiceDto
     * @return
     */
    public R<List<ApplyInvoiceVo>> findOpenInvoiceHistory(ApplyInvoiceDto applyInvoiceDto);

    /**
     * 根据开票申请获取详情
     * @param applyInvoiceDto 传id或者reimburse_code
     * @return
     */
    public R<ApplyInvoiceVo> findInvoiceDetailById(ApplyInvoiceDto applyInvoiceDto);

    /**
     * 根据成员id查询订单交易集合
     * @Param applyInvoiceDto
     * @return
     */
    public R<List<OrderDetailVo>> findOrderListByMemberId(ApplyInvoiceDto applyInvoiceDto);

    /**
     * 根据订单NO查询订单详情
     */
    public R<List<OrderBusiVo>> findOrderDetailByOrderNo(ApplyInvoiceDto applyInvoiceDto);


    /**
     * 查询人（子公司或者商户）月已开票额度
     */
    public BigDecimal getAmountByParams(ApplyInvoiceDto applyInvoiceDto);


    /**
     * 新增开票申请
     * @param applyInvoiceDto
     * @return
     */
    public R<ApplyInvoiceVo> addInvoiceApply(ApplyInvoiceDto applyInvoiceDto);


    /**
     * 编辑开票申请
     * @param applyInvoiceDto
     * @return
     */
    public R<ApplyInvoiceVo> editInvoiceApply(ApplyInvoiceDto applyInvoiceDto);


    /**
     * 根据id查询发票申请
     * @param applyInvoiceDto
     * @return
     */
    public R<ApplyInvoiceVo>  findInvoiceInfo(ApplyInvoiceDto applyInvoiceDto);
    
    /**
     * 
     * @Title: editInvoiceApply   
     * @Description: 回调开票申请   
     * @param: @param applyInvoiceDto
     * @param: @return    
     * @return: boolean
     * @throws
     */
    public R<ApplyInvoiceVo> openInvoiceByInfo(ApplyInvoiceVo applyInvoiceVo);


    /**
     * 编辑纸质发票状态
     * @param applyInvoiceDto
     * @return
     */
    public R<ApplyInvoiceVo>  editPaperInvoiceState(ApplyInvoiceDto applyInvoiceDto)throws Exception;


    /**
     * 编辑稍后发票状态
     * @param applyInvoiceDto
     * @return
     */
    public R<ApplyInvoiceVo> editInvoiceAfterApply( ApplyInvoiceDto applyInvoiceDto);

    /**
     * 查询券已开票金额
     * @param applyInvoiceDto
     * @return
     */
    public List<ApplyInvoiceVo> countHadOpenInvoiceAmountByCoupon(ApplyInvoiceDto applyInvoiceDto);


    /**
     * 查询发票申请与订单关系
     * @param invoiceApplyCode
     * @return
     */
    public List<OrderApplyInvoiceVo> findOrderApplyInvoiceList(String invoiceApplyCode);
    
    /**
     * 
     * @Title: billInvoice   
     * @Description: 个人借款开票
     * @param: @param applyInvoiceDto
     * @param: @return    
     * @author: duqiang     
     * @return: R<Boolean>      
     * @throws
     */
    public R<ApplyInvoiceVo> billInvoice(ApplyInvoiceDto applyInvoiceDto)throws Exception;


 }
