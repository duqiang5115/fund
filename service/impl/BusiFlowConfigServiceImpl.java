package com.taolue.baoxiao.fund.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.fund.entity.BusiFlowConfig;
import com.taolue.baoxiao.fund.mapper.BusiFlowConfigMapper;
import com.taolue.baoxiao.fund.service.IBusiFlowConfigService;

/**
 * <p>
 * 定义资金变更流，如转账，消费，配额，对账，交易等等 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2018-09-02
 */
@Service
public class BusiFlowConfigServiceImpl extends ServiceImpl<BusiFlowConfigMapper, BusiFlowConfig> implements IBusiFlowConfigService {

}
