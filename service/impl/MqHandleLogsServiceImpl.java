package com.taolue.baoxiao.fund.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.fund.entity.MqHandleLogs;
import com.taolue.baoxiao.fund.mapper.MqHandleLogsMapper;
import com.taolue.baoxiao.fund.service.IMqHandleLogsService;

/**
 * <p>
 * 定义资金变更流，如转账，消费，配额，对账，交易等等 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2018-09-03
 */
@Service
public class MqHandleLogsServiceImpl extends ServiceImpl<MqHandleLogsMapper, MqHandleLogs> implements IMqHandleLogsService {

}
