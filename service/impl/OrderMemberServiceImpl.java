package com.taolue.baoxiao.fund.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.fund.entity.OrderMember;
import com.taolue.baoxiao.fund.mapper.OrderMemberMapper;
import com.taolue.baoxiao.fund.service.IOrderMemberService;

/**
 * <p>
 * 订单的支付对象（支付人活组织），可以是人也可以是组织，采用一张子表单独记录； 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2018-08-22
 */
@Service
public class OrderMemberServiceImpl extends ServiceImpl<OrderMemberMapper, OrderMember> implements IOrderMemberService {

}
