package com.taolue.baoxiao.fund.service.impl;

import com.taolue.baoxiao.fund.entity.SearchConfig;
import com.taolue.baoxiao.fund.mapper.SearchConfigMapper;
import com.taolue.baoxiao.fund.service.ISearchConfigService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 账单明细表，每一条为账单中的没一个账单项 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2018-12-18
 */
@Service
public class SearchConfigServiceImpl extends ServiceImpl<SearchConfigMapper, SearchConfig> implements ISearchConfigService {

}
