package com.taolue.baoxiao.fund.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.fund.api.dto.OrderDetailDTO;
import com.taolue.baoxiao.fund.api.vo.OrderDetailVo;
import com.taolue.baoxiao.fund.entity.OrderDetail;
import com.taolue.baoxiao.fund.mapper.OrderDetailMapper;
import com.taolue.baoxiao.fund.service.IOrderDetailService;

/**
 * <p>
 * 订单的支付信息明细 服务实现类
 * </p>
 *
 * @author duqiang
 * @since 2019-03-08
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements IOrderDetailService {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 根据订单编号集合去查询
     * @param dto
     * @return
     */
    @Override
    public List<OrderDetailVo> selectOrderDetailByIds(OrderDetailDTO dto) {
        return orderDetailMapper.selectOrderDetailByIds(dto);
    }


    /**
     * 编辑订单状态
     * @param dto
     * @return
     */
    @Override
    public Boolean editOrderDetailbyIds(OrderDetailDTO dto) {
        return orderDetailMapper.editOrderDetailbyIds(dto);
    }
}
