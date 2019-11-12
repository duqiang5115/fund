package com.taolue.baoxiao.fund.service;

import com.taolue.baoxiao.fund.api.dto.OrderDetailDTO;
import com.taolue.baoxiao.fund.api.vo.OrderDetailVo;
import com.taolue.baoxiao.fund.entity.OrderDetail;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

/**
 * <p>
 * 订单的支付信息明细 服务类
 * </p>
 *
 * @author duqiang
 * @since 2019-03-08
 */
public interface IOrderDetailService extends IService<OrderDetail> {


    /**
     * 根据订单编号集合去查询
     * @param dto
     * @return
     */
    public List<OrderDetailVo> selectOrderDetailByIds(OrderDetailDTO dto);


    /**
     * 编辑状态 status
     * @param dto
     * @return
     */
    public Boolean  editOrderDetailbyIds(OrderDetailDTO dto);

}
