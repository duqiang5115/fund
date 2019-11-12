package com.taolue.baoxiao.fund.service;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.service.IService;
import com.taolue.baoxiao.fund.api.dto.OrderConsumeDetailDto;
import com.taolue.baoxiao.fund.api.vo.OrderConsumeDetailVo;
import com.taolue.baoxiao.fund.entity.OrderConsumeDetail;

/**
 * <p>
 * 场景详情表 服务类
 * </p>
 *
 * @author baoxiao
 * @since 2019-06-06
 */
public interface IOrderConsumeDetailService extends IService<OrderConsumeDetail> {
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.addOrderConsumeDetailBySceneCode
	 * @Description: 通过业务场景编码增加场景详细信息
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月06日 下午12:02:48
	 * 修改内容 :
	 */
	public boolean addOrderConsumeDetailBySceneCode(String SceneCode,String remark,String paymentOrderNo,Date operationTime);
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.findOrderConsumeDetail
	 * @Description: 查询业务场景详细信息
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月11日 下午6:18:41
	 * 修改内容 :
	 */
	public List<OrderConsumeDetailVo> findOrderConsumeDetail(OrderConsumeDetailDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.checkJsonFormatBySceneCode
	 * @Description: 校验实体卡订单json格式是否有误
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月19日 下午5:14:38
	 * 修改内容 :
	 */
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0000(OrderConsumeDetailDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.checkJsonFormatBySceneCode0004
	 * @Description: 校验电子卡订单json格式是否有误
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月19日 下午5:23:16
	 * 修改内容 :
	 */
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0004(OrderConsumeDetailDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.checkJsonFormatBySceneCode0008
	 * @Description: 校验手机充值订单json格式是否有误
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月19日 下午5:23:47
	 * 修改内容 :
	 */
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0008(OrderConsumeDetailDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.checkJsonFormatBySceneCode0042
	 * @Description: 校验航班管家订单json格式是否有误
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月19日 下午5:24:10
	 * 修改内容 :
	 */
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0042(OrderConsumeDetailDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.checkJsonFormatBySceneCode0039
	 * @Description: 校验美团外卖订单json格式是否有误
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月19日 下午5:24:20
	 * 修改内容 :
	 */
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0039(OrderConsumeDetailDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.checkJsonFormatBySceneCode0083
	 * @Description: 校验商户扫码订单json格式是否有误
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月19日 下午5:24:26
	 * 修改内容 :
	 */
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0083(OrderConsumeDetailDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.checkJsonFormatBySceneCode0062
	 * @Description: 校验生活缴费订单json格式是否有误
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月19日 下午5:24:31
	 * 修改内容 :
	 */
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0062(OrderConsumeDetailDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.checkJsonFormatBySceneCode0046
	 * @Description: 校验惠购商城订单json格式是否有误
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月19日 下午5:24:38
	 * 修改内容 :
	 */
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0046(OrderConsumeDetailDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.checkJsonFormatBySceneCode0041
	 * @Description: 校验滴滴出行订单json格式是否有误
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月19日 下午5:24:44
	 * 修改内容 :
	 */
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0041(OrderConsumeDetailDto dto);
	/**
	 * 
	 * 
	 * @Title IOrderConsumeDetailService.checkJsonFormatBySceneCode0063
	 * @Description: 校验中银通卡消费订单json格式是否有误
	 *
	 * @param dto
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月19日 下午5:24:50
	 * 修改内容 :
	 */
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0063(OrderConsumeDetailDto dto);
}
