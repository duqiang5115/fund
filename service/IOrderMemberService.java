package com.taolue.baoxiao.fund.service;

import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.fund.entity.OrderMember;

/**
 * <p>
 * 订单的支付对象（支付人活组织），可以是人也可以是组织，采用一张子表单独记录； 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2018-08-22
 */
public interface IOrderMemberService extends IService<OrderMember> {

}
