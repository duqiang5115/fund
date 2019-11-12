package com.taolue.baoxiao.fund.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.SceneCodeStatus;
import com.taolue.baoxiao.common.util.MoneyUtils;
import com.taolue.baoxiao.fund.api.dto.OrderConsumeDetailDto;
import com.taolue.baoxiao.fund.api.vo.OrderConsumeDetailVo;
import com.taolue.baoxiao.fund.entity.OrderConsumeDetail;
import com.taolue.baoxiao.fund.mapper.OrderConsumeDetailMapper;
import com.taolue.baoxiao.fund.service.IOrderConsumeDetailService;
import com.xiaoleilu.hutool.bean.BeanUtil;
import com.xiaoleilu.hutool.collection.CollUtil;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 场景详情表 服务实现类
 * </p>
 *
 * @author baoxiao
 * @since 2019-06-06
 */
@Slf4j
@Service
public class OrderConsumeDetailServiceImpl extends ServiceImpl<OrderConsumeDetailMapper, OrderConsumeDetail> implements IOrderConsumeDetailService {
	@Autowired
	OrderConsumeDetailMapper orderConsumeDetailMapper;
	
	@Override
	public boolean addOrderConsumeDetailBySceneCode(String SceneCode,String remark,String paymentOrderNo,Date operationTime) {
		/**通过业务场景编码解析数据并保存到场景详情表**/
		OrderConsumeDetailVo returnVo;
		try {
			returnVo = this.JsonConvert(SceneCode,remark);
			log.info("通过业务场景编码解析数据》》解析完成，返回结果return result returnVo:{}",JSON.toJSON(returnVo));
			if(com.xiaoleilu.hutool.util.ObjectUtil.isNotNull(returnVo) && StringUtils.isNotBlank(returnVo.getSceneCode()) 
					&& StringUtils.isNotBlank(returnVo.getOperationParams())) {
				returnVo.setPaymentOrderNo(paymentOrderNo);
				returnVo.setRemark(remark);
				returnVo.setOperationTime(operationTime == null ? new Date() : operationTime);
				OrderConsumeDetail entity = new OrderConsumeDetail();
				BeanUtil.copyProperties(returnVo, entity);
				log.info("通过业务场景编码解析数据返回结果 保存到业务场景详情表中 请求参数entity :{}",JSON.toJSON(entity));
				OrderConsumeDetailDto detailDto = new OrderConsumeDetailDto();
				detailDto.setSceneCode(SceneCode);
				detailDto.setPaymentOrderNo(paymentOrderNo);
				List<OrderConsumeDetailVo> returnList = orderConsumeDetailMapper.findOrderConsumeDetail(detailDto);
				if(CollUtil.isEmpty(returnList)) {
					log.info("通过场景编码和订单号查询场景信息，返回结果不存在，直接保存》》》开始》》请求参数 request param entity:{}",JSON.toJSON(entity));
					return this.insert(entity);
				}else {
					OrderConsumeDetailDto updateEntity = new OrderConsumeDetailDto();
					BeanUtil.copyProperties(returnVo, updateEntity);
					log.info("通过场景编码和订单号查询场景信息，返回结果存在，修改》》》开始》》请求参数 request param updateEntity:{}",JSON.toJSON(updateEntity));
					return orderConsumeDetailMapper.updateOrderConsumeDetail(updateEntity);
				}
			}
			return true;	
		} catch (Exception e) {
			log.error("通过业务场景编码解析数据，返回异常e:{}",JSON.toJSON(e));
			return false;
		}
	
	}

	/**
	 * 
	 * 
	 * @Title OrderConsumeDetailServiceImpl.JsonConvert
	 * @Description: JSON转换成对象
	 *
	 * @param sceneCode
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月06日 下午12:14:24
	 * 修改内容 :
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private OrderConsumeDetailVo JsonConvert(String sceneCode, String JsonRemark) {
		log.info("总部传入的json字符串，sceneCode:{},JsonRemark:{}",sceneCode, JsonRemark);
		Map<String,Object> mapStr = new HashMap<String, Object>();
		List<Map> mapListJson = Lists.newArrayList();
		OrderConsumeDetailVo dto = new OrderConsumeDetailVo();
		if(sceneCode.equals(SceneCodeStatus.ELECTRON_CARD_BILL.getCateCode())) {
			List<Object> array = JSONArray.parseArray(JsonRemark);
			log.info("电子卡订单转成list<map>,返回结果array:{} ",JSON.toJSON(array));
			if(CollUtil.isNotEmpty(array)) {
				mapListJson = (List)array;
			}else {
				return null;
			}
		}else if(sceneCode.equals(SceneCodeStatus.MOBILE_RECHARGE_BILL.getCateCode()) 
				|| sceneCode.equals(SceneCodeStatus.FLIGHT_BILL.getCateCode())
				|| sceneCode.equals(SceneCodeStatus.TAKEOUT_MEITUAN.getCateCode())
				|| sceneCode.equals(SceneCodeStatus.BUSINESS_SCAN_CODE_BILL.getCateCode())
				|| sceneCode.equals(SceneCodeStatus.LIFE_PAY_BILL.getCateCode())
				|| sceneCode.equals(SceneCodeStatus.SHOPPING_MALL_BILL.getCateCode())
				|| sceneCode.equals(SceneCodeStatus.DRIP_TRIPS_BILL.getCateCode())
				|| sceneCode.equals(SceneCodeStatus.CHINA_BANK_CARD_CONSUMEBILL.getCateCode())
				|| sceneCode.equals(SceneCodeStatus.ENTITY_CARD_BILL.getCateCode()) ){
			Map<String,Object> map = JSON.parseObject(JsonRemark);
			log.info("非电子卡的其他订单转成map，返回结果 map:{}",JSON.toJSON(map));
			if(map == null || map.isEmpty()) {
				return null;
			}
			mapStr = map;
		}
		if(StringUtils.isNotBlank(sceneCode)) {
			dto.setSceneCode(sceneCode);
			switch (sceneCode) {
			case "0000":
				//{"num":"2","img":"https://static.jia-fu.cn/goods/20190219/7739cfd4f3741aad0a871414d9a9ebd8.jpg",
				//"merchName":"易通卡","faceValue":"1000","merchId":"100000002640"}
				//易通卡1000 x2
//				(merchName) (faceValue) x(num)
				List<Map> listStr = (List<Map>)mapStr.get("merchs");
				log.info("实体卡订单取到key(merchs)对应实体卡的list,返回结果 return result listStr:{}",JSON.toJSON(listStr));
				StringBuffer sbstr = new StringBuffer();
				Map<String, Object> result2 = new HashMap<String, Object>();
				if(CollUtil.isNotEmpty(listStr)) {
					for (Map map : listStr) {
						if(map != null && !map.isEmpty() 
								&& ObjectUtil.isNotNull(map.get("merchName")) && StringUtils.isNotBlank(map.get("merchName").toString()) 
								&& ObjectUtil.isNotNull(map.get("faceValue")) && StringUtils.isNotBlank(map.get("faceValue").toString()) 
								&& ObjectUtil.isNotNull(map.get("num")) && StringUtils.isNotBlank(map.get("num").toString())) {
							log.info("实体卡订单json解析开始》》》数据无误》》》start》》");
							String itemName = map.get("merchName").toString();
							String specValue = map.get("faceValue").toString();
							StringBuffer sbf1 = new StringBuffer();
							sbf1.append(itemName).append(specValue);
							Long num = Long.parseLong(map.get("num").toString());
							if(result2.containsKey(sbf1.toString())){
								Long temp = Long.parseLong(result2.get(sbf1.toString()).toString());
								num += temp;
							}
							result2.put(sbf1.toString(), num);
						}else {
							log.info("实体卡订单json解析开始》》》数据有误，key=merchName、faceValue、num对应的值为空，则内容为消费》》》start》》");
							result2.put("实体卡订单", "消费");
						}
					}
					log.info("实体卡订单最后保存到map中,返回结果 return result map:{}",JSON.toJSON(result2));
					log.info("实体卡订单json解析结束》》》end》》");
					if(result2 != null  && !result2.isEmpty()) {
						for (Entry<String, Object> map : result2.entrySet()) {
							if(map.getKey().equals("实体卡订单")) {
								sbstr.append(map.getValue());
							}else if(StringUtils.isNotBlank(map.getKey()) && ObjectUtil.isNotNull(map.getValue()) && StringUtils.isNotBlank(map.getValue().toString())) {
								sbstr.append(map.getKey());
								sbstr.append("x");
								sbstr.append(map.getValue());
								sbstr.append("，");
							}
						}
					}
					if(sbstr != null && StringUtils.isNotBlank(sbstr.toString())) {
						dto.setOperationParams(sbstr.toString());
					}
				}
				break;
			case "0004":
//				[{"itemName":"京东电子卡","num":"1","specValue":"500","img":"https://static.jia-fu.cn/goods/20190218/d625d59ac2d3ecfeb56612933288cca2.jpg","stockId":"1000142075","orderId":"573660531675","detailId":"100000000471"},{"itemName":"京东电子卡","num":"1","specValue":"500","img":"https://static.jia-fu.cn/goods/20190218/d625d59ac2d3ecfeb56612933288cca2.jpg","stockId":"1000142259","orderId":"573660531675","detailId":"100000000471"},{"itemName":"京东电子卡","num":"1","specValue":"500","img":"https://static.jia-fu.cn/goods/20190218/d625d59ac2d3ecfeb56612933288cca2.jpg","stockId":"1000142077","orderId":"573660531675","detailId":"100000000471"}]
				// 京东电子卡 500 x2 \n 京东电子卡 100 x1
//				(itemName) (specValue) x(num)
				Map<String, Object> result1 = new HashMap<String, Object>();
				if(CollUtil.isNotEmpty(mapListJson)) {
					for (Map map1 : mapListJson){
						if(map1 != null && !map1.isEmpty() 
								&& ObjectUtil.isNotNull(map1.get("type")) && StringUtils.isNotBlank(map1.get("type").toString())
								&& ObjectUtil.isNotNull(map1.get("num")) && StringUtils.isNotBlank(map1.get("num").toString())
								&& ObjectUtil.isNotNull(map1.get("merchName")) && StringUtils.isNotBlank(map1.get("merchName").toString())
								&& ObjectUtil.isNotNull(map1.get("faceValue")) && StringUtils.isNotBlank(map1.get("faceValue").toString())) {
							/***电子卡中的特殊的嘉福E卡，type不为空**/
//								[{"num":"1","img":"https://static.jia-fu.cn/goods/20190222/d2ea84111f81eacad17f6af27c20227a.jpeg","merchName":"嘉福E卡","faceValue":"100","type":"1012","merchId":"100000000342"}]
							log.info("电子卡订单json解析开始》》》数据无误》》》start》》");
							String itemName = map1.get("merchName").toString();
							String specValue = map1.get("faceValue").toString();
							StringBuffer sbf1 = new StringBuffer();
							sbf1.append(itemName).append(specValue);
							Long num = Long.parseLong(map1.get("num").toString());
							if(result1.containsKey(sbf1.toString())){
								Long temp = Long.parseLong(result1.get(sbf1.toString()).toString());
								num += temp;
							}
							result1.put(sbf1.toString(), num);
							log.info("电子卡订单>>嘉福E卡>>json解析返回结果,type:{},num:{},merchName:{},faceValue:{}",map1.get("type"), map1.get("num"), map1.get("merchName"), map1.get("faceValue"));
						}else if(map1 != null && !map1.isEmpty() 
								&& ObjectUtil.isNotNull(map1.get("itemName")) && StringUtils.isNotBlank(map1.get("itemName").toString())
								&& ObjectUtil.isNotNull(map1.get("specValue")) && StringUtils.isNotBlank(map1.get("specValue").toString())
								&& ObjectUtil.isNotNull(map1.get("num")) && StringUtils.isNotBlank(map1.get("num").toString()) ) {
							String itemName = map1.get("itemName").toString();
							String specValue = map1.get("specValue").toString();
							StringBuffer sbf1 = new StringBuffer();
							sbf1.append(itemName).append(specValue);
							Long num = Long.parseLong(map1.get("num").toString());
							if(result1.containsKey(sbf1.toString())){
								Long temp = Long.parseLong(result1.get(sbf1.toString()).toString());
								num += temp;
							}
							result1.put(sbf1.toString(), num);
							log.info("电子卡订单json解析返回结果,itemName:{},specValue:{},num:{}", map1.get("itemName"), map1.get("specValue"), map1.get("num"));
						}else {
							log.info("电子卡订单json解析开始》》》数据有误，key=type、merchName、faceValue、itemName、specValue、num对应的值为空，则内容为消费》》》start》》");
							result1.put("电子卡订单", "消费");
						}
						log.info("电子卡订单json解析结束》》》end》》");
					}
					StringBuilder sb = new StringBuilder();
					if(result1 != null && !result1.isEmpty()) {
						for (Entry<String, Object> map : result1.entrySet()) {
							if(map.getKey().equals("电子卡订单")) {
								sb.append(map.getValue());
							}else if(StringUtils.isNotBlank(map.getKey()) && ObjectUtil.isNotNull(map.getValue()) && StringUtils.isNotBlank(map.getValue().toString())){
								sb.append(map.getKey());
								sb.append("x");
								sb.append(map.getValue());
						    	sb.append("，");
							}
							
						}
					}
					if(sb != null && StringUtils.isNotBlank(sb.toString())) {
						dto.setOperationParams(sb.toString());
					}
				}
				break;
			case "0007":
				// 暂时没有此场景
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				break;
			case "0012":
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				// 暂时没有此场景
				break;
			case "0008":
//			{"faceValue":"5000","mobileLocal":"浙江宁波","mobileNumber":"18368498281","mobileOperator":"移动","orderId":"897540420437"} (mobileNumber)(faceValue)
				StringBuffer sdf = new StringBuffer();
				if(mapStr != null && !mapStr.isEmpty() ) {
					if(ObjectUtil.isNotNull(mapStr.get("mobileNumber")) && StringUtils.isNotBlank(mapStr.get("mobileNumber").toString()) 
						&& ObjectUtil.isNotNull(mapStr.get("faceValue")) && StringUtils.isNotBlank(mapStr.get("faceValue").toString())) {
						log.info("手机充值订单json解析开始》》数据没问题》》start》》");
						log.info("手机充值订单json解析返回结果，mobileNumber:{},faceValue:{}",mapStr.get("mobileNumber"), mapStr.get("faceValue"));
						sdf.append(mapStr.get("mobileNumber") + "，"+ stringAmountShrinkBigDecimal(mapStr.get("faceValue").toString(), "2") ); // 原金额是扩大100倍的
						dto.setOperationAmount(stringAmountShrinkBigDecimal(mapStr.get("faceValue").toString(), "3")); 
						dto.setOperationParams(sdf.toString());
						log.info("手机充值订单json解析结束》》》end》》");
					}else {
						log.info("手机充值订单json解析》》key=mobileNumber、faceValue的某个值为空，不保存空值");
						log.info("手机充值订单json解析返回结果，mobileNumber:{},faceValue:{}",mapStr.get("mobileNumber"), mapStr.get("faceValue"));
						if( (ObjectUtil.isNull(mapStr.get("mobileNumber")) || StringUtils.isEmpty(mapStr.get("mobileNumber").toString()))
								&& (ObjectUtil.isNotNull(mapStr.get("faceValue")) && StringUtils.isNotBlank(mapStr.get("faceValue").toString()))  ) {
							sdf.append("手机充值-"+ stringAmountShrinkBigDecimal(mapStr.get("faceValue").toString(), "2"));
							dto.setOperationAmount(stringAmountShrinkBigDecimal(mapStr.get("faceValue").toString(), "3")); 
							dto.setOperationParams(sdf.toString());
							dto.setOperationAmount(stringAmountShrinkBigDecimal(mapStr.get("faceValue").toString(), "3")); 
						}else if( (ObjectUtil.isNull(mapStr.get("faceValue")) || StringUtils.isEmpty(mapStr.get("faceValue").toString()) )
								&& (ObjectUtil.isNotNull(mapStr.get("mobileNumber")) && StringUtils.isNotBlank(mapStr.get("mobileNumber").toString())) ) {
							sdf.append(mapStr.get("mobileNumber"));
							dto.setOperationParams(sdf.toString());
						}else if(ObjectUtil.isNull(mapStr.get("mobileNumber")) || StringUtils.isEmpty(mapStr.get("mobileNumber").toString()) 
								&& ObjectUtil.isNull(mapStr.get("faceValue")) || StringUtils.isEmpty(mapStr.get("faceValue").toString()) ){
							mapStr.put("手机充值订单", "消费");
							sdf.append(mapStr.get("手机充值订单"));
							dto.setOperationParams(sdf.toString());
						}
					}
				}else {
					mapStr.put("手机充值订单", "消费");
					sdf.append(mapStr.get("手机充值订单"));
					dto.setOperationParams(sdf.toString());
				}
				break;
			case "0021":
				// 暂时没有此场景
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				break;
				
			case "0051":
				// 暂时没有此场景
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				break;
				
			case "0052":
				// 暂时没有此场景
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				break;
				
			case "0053":
				// 暂时没有此场景
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				break;
				
			case "0037":
				// 暂时没有此场景
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				break;
				
			case "0042":
				// 航班管家订单
				/*(trainNo) (trainDate) (trainTime)

				(fromStation)

				(toStation)*/
				log.info("航班管家充值订单json解析开始》》》start》》");
				StringBuffer sbf = new StringBuffer();
				if(mapStr != null && !mapStr.isEmpty()) {
					sbf.append(mapStr.get("trainNo") != null && StringUtils.isNotBlank(mapStr.get("trainNo").toString()) ? mapStr.get("trainNo")+" " :"").
					append(mapStr.get("trainDate") != null && StringUtils.isNotBlank(mapStr.get("trainDate").toString()) ? mapStr.get("trainDate")+" " :"").
					append(mapStr.get("trainTime") != null && StringUtils.isNotBlank(mapStr.get("trainTime").toString()) ? mapStr.get("trainTime") +"，" : "").
					append(mapStr.get("fromStation") != null && StringUtils.isNotBlank(mapStr.get("fromStation").toString()) ? mapStr.get("fromStation") +"，": "").
					append(mapStr.get("toStation") != null && StringUtils.isNotBlank(mapStr.get("toStation").toString()) ? mapStr.get("toStation") : "");
					if(sbf == null || StringUtils.isEmpty(sbf.toString())) {
						dto.setOperationParams("消费");
					}else {
						dto.setOperationParams(sbf.toString());
					}
					log.info("航班管家充值订单json解析结束,trainNo:{},trainDate:{},trainTime:{},fromStation:{},toStation:{}",mapStr.get("trainNo"),mapStr.get("trainDate"),
							mapStr.get("trainTime"),mapStr.get("fromStation"),mapStr.get("toStation"));
					log.info("航班管家充值订单json解析结束》》》end》》");
				}else {
					mapStr.put("航班管家充值订单", "消费");
				}
				break;
			case "0069":
				// TODO 不知道怎么取
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				//肯德基
//			{"code":"00","ecodes":"https://card.yumchina.com/thirdparty/queryCard?p=2100100223350369462&t=5BB5A06D03A32213E14","txnTime":"20190417181805"}
				break;
			case "0068":
				// 家乐福
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				// TODO 不知道怎么取
//			{"code":"00","ecodes":"https://cardup.cn/Z0RXkv05Z2pmZ4u5","txnTime":"20190425130708"}
				break;
			case "0067":
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				// 麦德龙
//			{"cardNo":"2336340020000093131","respCode":"0000","orderBalance":"500.00","orderState":"Y","expireDate":"20220419","respMsg":"下单成功","shortUrl":"https://cardup.cn/CleXy6kUD2VUg3Kw","orderTimes":"0"}
				break;
			case "0039":
				// 美团外卖
//			{"returnUrl":"https://h5.waimai.meituan.com/waimai/mindex/order-detail?mtOrderViewId=31348620235616983","orderAmount":"1880","extOrderId":"4753K4NOLO","userId":"710828","appid":"正宗淮南牛肉汤（龚华路店）-31348620235616983","orderId":"555010419186","productType":"美团外卖"}
//				(appid)
				log.info("美团外卖订单json解析开始》》》start》》");
				if(mapStr != null && !mapStr.isEmpty()) {
					if(ObjectUtil.isNotNull(mapStr.get("appid")) && StringUtils.isNotBlank(mapStr.get("appid").toString())) {
						StringBuffer sbfStr = new StringBuffer();
						sbfStr.append(mapStr.get("appid"));
						if(sbfStr != null || StringUtils.isNotBlank(sbfStr.toString())) {
							String appid = sbfStr.toString();
							if(appid.contains("-")) {
								dto.setOperationParams(appid.substring(0,appid.lastIndexOf("-")));
							}else {
								dto.setOperationParams(appid);
							}
						}
					}else {
						dto.setOperationParams("消费");
					}
					if(ObjectUtil.isNotNull(mapStr.get("orderAmount")) && StringUtils.isNotBlank(mapStr.get("orderAmount").toString())) {
						dto.setOperationAmount(stringAmountShrinkBigDecimal(mapStr.get("orderAmount").toString(), "3")); 
					}
				}else {
					log.info("美团外卖订单json解析结束返回结果,appid为空，则保存为消费");
					mapStr.put("美团外卖订单", "消费");
					dto.setOperationParams(mapStr.get("美团外卖订单").toString());
				}
				log.info("美团外卖订单json解析结束返回结果,appid:{}", mapStr.get("appid"));
				log.info("美团外卖订单json解析结束》》》end》》");
				break;
			case "0083":
				// 商户扫码
//			{"entName":"山城重庆小面","geid":"ge_0878ba28_75657120619d18c56cec","guid":"gu_ba28_a34b5e2a5863876906a478a3","mobile":"15821202315","storeName":"山城重庆小面"}
//				(storeName)
				if(mapStr != null && !mapStr.isEmpty()) {
					if(ObjectUtil.isNotNull(mapStr.get("storeName")) && StringUtils.isNotBlank(mapStr.get("storeName").toString())) {
						log.info("商户扫码订单json解析开始》》》start》》");
						dto.setOperationParams(mapStr.get("storeName").toString());
					}else {
						dto.setOperationParams("消费");
					}
				}else {
					log.info("商户扫码订单json解析结束返回结果,storeName为空，则保存为消费");
					mapStr.put("商户扫码订单", "消费");
					dto.setOperationParams(mapStr.get("商户扫码订单").toString());
				}
				log.info("商户扫码订单json解析结束返回结果,storeName:{}", mapStr.get("storeName"));
				log.info("商户扫码订单json解析结束》》》end》》");
				break;
			case "0062":
				//生活缴费
//			{"timestamp":"20190417204249","billKey":"310508315","totalAmount":"5180","tid":"XZ20190417204248b7a505468a4ac379","billState":"2","orderId":"217380417867","itemCode":"上海市城投水务（条形码）"}
//				(itemCode)
				log.info("生活缴费订单json解析开始》》》start》》");
				if(mapStr != null && !mapStr.isEmpty()) {
					if(ObjectUtil.isNotNull(mapStr.get("itemCode")) && StringUtils.isNotBlank(mapStr.get("itemCode").toString())) {
						dto.setOperationParams(mapStr.get("itemCode").toString());		
					}else {
						dto.setOperationParams("消费");
					}
					if(ObjectUtil.isNotNull(mapStr.get("totalAmount")) && StringUtils.isNotBlank(mapStr.get("totalAmount").toString())) {
						dto.setOperationAmount(stringAmountShrinkBigDecimal(mapStr.get("totalAmount").toString(), "3")); 
					}
				}else {
					dto.setOperationParams("消费");
				}
				log.info("生活缴费订单json解析结束返回结果,itemCode:{}", mapStr.get("itemCode"));
				log.info("生活缴费订单json解析结束》》》end》》");
				break;
			case "0046":
				// 惠购商城订单
//			{"coupon_amount":"0.00","details":"[{\"item_name\":\"COACH 蔻驰 奢侈品 女士卡其橙色PVC手提肩背斜挎桶包 F31383 IMO6F\",\"price\":1710.0,\"sku\":\"13686513\"}]","eid":"null","order_id":"JF90423121436a07f0","sign":"b0b21a72d95a9ac6366366e06e66a3ec","timestamp":"1555992909869","total_fee":"1710.00","uid":"1204267"}
//				(item_name)
				log.info("惠购商城订单json解析开始》》》start》》");
				StringBuffer sbfItem = new StringBuffer();
				if(mapStr == null || mapStr.isEmpty()) {
					dto.setOperationParams("消费");
				}else {
					if(ObjectUtil.isNull(mapStr.get("details")) || StringUtils.isEmpty(mapStr.get("details").toString())) {
						dto.setOperationParams("消费");
					}else {
						List<Object> detailList = JSONArray.parseArray(mapStr.get("details").toString());
						List<Map> detailMap = Lists.newArrayList();
						if(CollUtil.isEmpty(detailList)) {
							dto.setOperationParams("消费");
						}else {
							detailMap = (List)detailList;
							log.info("惠购商城订单json map转list返回结果,detailMap:{}", JSON.toJSON(detailMap));
							for (Map map2 : detailMap) {
								if(map2 == null || map2.isEmpty()) {
									dto.setOperationParams("消费");
								}else {
									if(ObjectUtil.isNull(map2.get("item_name")) || StringUtils.isEmpty(map2.get("item_name").toString())) {
										
									}else {
										sbfItem.append(map2.get("item_name")+"，");
									}
								}
								log.info("惠购商城订单list取其中的item_name拼接字符串》》,item_name:{}", map2.get("item_name"));
							}
							if(sbfItem != null && StringUtils.isNotBlank(sbfItem.toString())) {
								dto.setOperationParams(sbfItem.toString());
							}else {
								dto.setOperationParams("消费");
							}
						}
					}
					if(ObjectUtil.isNotNull(mapStr.get("total_fee")) ) {
						dto.setOperationAmount(stringAmountShrinkBigDecimal(mapStr.get("total_fee").toString(), "3"));
					}
				}
				
				
				/*List<Object> detailStr = JSONArray.parseArray(mapStr.get("details").toString());
				List<Map> detailList = (List)detailStr;
				log.info("惠购商城订单json map转list返回结果,detailList:{}",JSON.toJSON(detailList));
				StringBuffer sbfItem = new StringBuffer();
				if(CollUtil.isNotEmpty(detailList)) {
					for (Map map : detailList) {
						if(ObjectUtil.isNotNull(map.get("item_name"))) {
							sbfItem.append(map.get("item_name")+"，");
						}
						log.info("惠购商城订单list取其中的item_name拼接字符串》》,item_name:{}",map.get("item_name"));
					}
					if(StringUtils.isNotBlank(sbfItem.toString())) {
						dto.setOperationParams(sbfItem.toString());
					}
				}
				if(ObjectUtil.isNotNull(mapStr.get("total_fee")) ) {
					dto.setOperationAmount(stringAmountShrinkBigDecimal(mapStr.get("total_fee").toString(), "3"));
				}*/
				log.info("惠购商城订单json解析结束》》》end》》");
				break;
				
			case "0072":
				// 永辉超市
				// 暂时没有此场景
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				break;
			case "0081":
				// 云闪付主扫消费订单
				//{"appId":"49996005","comInfo":"e0YwPTAyMDAmRjI1PTAwJkYzPTAwMDAwMCZGMzc9ODA1NjM3MDczMTIwJkY2MD0wMzAwMDAwMDAwMDA3MDAxMDAwMzAwMDQwMjUwMDEwMDB9","orderId":"394950420805","payeeOrderId":"1903201539556810028007","reqReserved":"e2NvbXBhbnk95LiK5rW35reY55Wl5pWw5o2u5aSE55CG5pyJ6ZmQ5YWs5Y+4fQ==","respCode":"00000","respMsg":"成功[ZF]","settleDate":"0420","settleKey":"48020000   00049992   2005700420133344","signature":"kd9OuecLkTiYUVhpwYor1ZmXi9f0uUljzUNmYKN6Z/N0dtvdRWT8bk6RGvWbW5hvSf8e7Zvo9T1BXIuyc4LcltKweNZqFnHQ2MhFseF15fVji5sjYuGAhY2EXqgsSHDCuy5+n0sfSUqiUe93phAh5GXxxkMI3ahYKtFK+3qQFBgQ4iNrOWPAzLYVlaY/NS+EKLFs0nF6slnjZfQgFg/pvgoPxsbx2ddysaJ76hzAmt2TYEQkqMGCoSbOKfkUCtrQAJ9MueXQRDKaVmCqlwrLWjESbpCp6aARjGts5N5nOUSoqPPpB4oVLBARUycb24PLbm2vxVKzCBTwbfcBv4nyzg==","txnAmt":"6000","txnTime":"20190420133344","txnType":"12","version":"1.0.3","voucherNum":"98190420488056370731"}
				// TODO 暂时没定义
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				break;
			case "0040":
				// 便利店付款
				// 暂时没有此场景
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				break;
			case "0060":
				// 沃尔玛
				dto.setDelFlag(CommonConstant.STATUS_DEL);
//			{"cardNo":"2346990600000503771234","respCode":"0000","orderBalance":"100.00","orderState":"Y","expireDate":"20220417","respMsg":"下单成功","shortUrl":"https://cardup.cn/KnXOb9bfQPAQZa2D","orderTimes":"0"}
				break;
			case "0041":
				// 滴滴出行
				/*(departure_time)

				(city)(start_name)

				(end_city)(end_name)*/
				log.info("滴滴出行订单json解析开始》》》start》》");
				StringBuffer sbfStr2 = new StringBuffer();
				if(mapStr == null || mapStr.isEmpty()) {
					dto.setOperationParams("消费");
				}else {
					if(ObjectUtil.isNull(mapStr.get("data")) || StringUtils.isEmpty(mapStr.get("data").toString())) {
						dto.setOperationParams("消费");
					}else {
						Map<String,Object> mapStr1 = JSON.parseObject(mapStr.get("data").toString());
						if(mapStr1 == null || mapStr1.isEmpty()) {
							dto.setOperationParams("消费");
						}else {
							if(ObjectUtil.isNotNull(mapStr1.get("price")) && StringUtils.isNotBlank(mapStr1.get("price").toString())) {
								Map<String,Object> priceMap = JSON.parseObject(mapStr1.get("price").toString());
								if(!priceMap.isEmpty() && ObjectUtil.isNotNull(priceMap.get("total_price"))) {
									dto.setOperationAmount(MoneyUtils.moneyExpand1000(new BigDecimal(priceMap.get("total_price").toString())));
								}
							}
							
							if(ObjectUtil.isNull(mapStr1.get("order")) || StringUtils.isEmpty(mapStr1.get("order").toString())) {
								dto.setOperationParams("消费");
							}else {
								Map<String,Object> mapStr2 = JSON.parseObject(mapStr1.get("order").toString());
								if(mapStr2 == null || mapStr2.isEmpty()) {
									dto.setOperationParams("消费");
								}else {
									if(StringUtils.isEmpty(sbfStr2.toString())) {
										String separator = "，";
										if(ObjectUtil.isNull(mapStr2.get("departure_time")) && StringUtils.isBlank(mapStr2.get("departure_time").toString()) 
												&& ObjectUtil.isNull(mapStr2.get("city")) && StringUtils.isBlank(mapStr2.get("city").toString()) 
												&& ObjectUtil.isNull(mapStr2.get("start_name")) && StringUtils.isBlank(mapStr2.get("start_name").toString()) 
												&& ObjectUtil.isNull(mapStr2.get("end_city")) && StringUtils.isBlank(mapStr2.get("end_city").toString())
												&& ObjectUtil.isNull(mapStr2.get("end_name")) && StringUtils.isBlank(mapStr2.get("end_name").toString()) ) {
											dto.setOperationParams("消费");
										}else {
											sbfStr2.append(mapStr2.get("departure_time") != null && StringUtils.isNotBlank(mapStr2.get("departure_time").toString()) ? mapStr2.get("departure_time")+ separator:"").
											append(mapStr2.get("city") != null && StringUtils.isNotBlank(mapStr2.get("city").toString()) ? mapStr2.get("city")+" " :"").
											append(mapStr2.get("start_name") != null && StringUtils.isNotBlank(mapStr2.get("start_name").toString()) ? mapStr2.get("start_name")+separator : "").
											append(mapStr2.get("end_city") != null && StringUtils.isNotBlank(mapStr2.get("end_city").toString()) ? mapStr2.get("end_city")+" " : "").
											append(mapStr2.get("end_name") != null && StringUtils.isNotBlank(mapStr2.get("end_name").toString()) ? mapStr2.get("end_name") : "");
											
											
											/*sbfStr2.append(mapStr2.get("departure_time") 
													+ separator 
													+ mapStr2.get("city")  +" "
													+ mapStr2.get("start_name") 
													+ separator 
													+ mapStr2.get("end_city") +" " 
													+ mapStr2.get("end_name") );*/
											if(sbfStr2 !=null && StringUtils.isNotBlank(sbfStr2.toString())) {
												dto.setOperationParams(sbfStr2.toString());
											}else {
												dto.setOperationParams("消费");
											}
										}
									}
									log.info("滴滴出行订单json解析结束，map下面取值拼凑 departure_time:{},city:{},start_name:{},end_city:{},end_name:{}",mapStr2.get("departure_time"),
											mapStr2.get("city"),mapStr2.get("start_name"),mapStr2.get("end_city"),mapStr2.get("end_name"));
								}
							}
						}
					}
				}
				
				
				
				
				/*log.info("滴滴出行订单json解析开始》》》start》》");
				Object object = mapStr.get("data");
				Map<String,Object> mapStr1 = JSON.parseObject(object.toString());
				log.info("滴滴出行订单json解析开始,对象转map返回结果 mapStr1:{}", JSON.toJSON(mapStr1));
				Map<String,Object> mapStr2 = JSON.parseObject(mapStr1.get("order").toString());
				log.info("滴滴出行订单json解析开始,对象order转map 返回结果 mapStr2:{}", JSON.toJSON(mapStr2));
				StringBuffer sbfStr2 = new StringBuffer();
				if(StringUtils.isEmpty(sbfStr2.toString())) {
					String separator = "，";
					sbfStr2.append(mapStr2.get("departure_time")+ separator + mapStr2.get("city")+" "+mapStr2.get("start_name")+ separator + mapStr2.get("end_city")+" "+mapStr2.get("end_name"));
				}
				log.info("滴滴出行订单json解析结束，map下面取值拼凑 departure_time:{},city:{},start_name:{},end_city:{},end_name:{}",mapStr2.get("departure_time"),
						mapStr2.get("city"),mapStr2.get("start_name"),mapStr2.get("end_city"),mapStr2.get("end_name"));
				if(StringUtils.isNotBlank(sbfStr2.toString())) {
					dto.setOperationParams(sbfStr2.toString());
				}
				
				if(ObjectUtil.isNotNull(mapStr1.get("price"))) {
					Map<String,Object> priceMap = JSON.parseObject(mapStr1.get("price").toString());
					if(!priceMap.isEmpty() && ObjectUtil.isNotNull(priceMap.get("total_price"))) {
						dto.setOperationAmount(new BigDecimal(priceMap.get("total_price").toString()));
					}
				}*/
				log.info("滴滴出行订单json解析结束》》》end》》");
				break;
			case "0066":
				dto.setDelFlag(CommonConstant.STATUS_DEL);
				break;
			case "0063":
				// 中银通卡消费
//				北京港佳好邻居连锁便利店有限责任公司
				log.info("中银通卡消费订单json解析开始》》》start》》");
				if(mapStr == null || mapStr.isEmpty()) {
					dto.setOperationParams("消费");
				}else {
					log.info("中银通卡消费订单json解析结束,map下面取值拼凑 payeeMerName:{},txnAmt:{}", mapStr.get("payeeMerName"), mapStr.get("txnAmt") );
					if(ObjectUtil.isNotNull(mapStr.get("payeeMerName")) && StringUtils.isNotBlank(mapStr.get("payeeMerName").toString())) {
						dto.setOperationParams(mapStr.get("payeeMerName").toString());
					}else {
						dto.setOperationParams("消费");
					}
					if(ObjectUtil.isNotNull(mapStr.get("txnAmt")) && StringUtils.isNotBlank(mapStr.get("txnAmt").toString())) {
						dto.setOperationAmount(stringAmountShrinkBigDecimal(mapStr.get("txnAmt").toString(), "3"));
					}
				}
				log.info("中银通卡消费订单json解析结束》》》end》》");
				break;
			case "0061": 
				 dto.setOperationParams("银联充值");
//				 dto.setDelFlag(CommonConstant.STATUS_DEL);
				break;
			}
		}else {
			log.info("业务场景编码为空，直接返回");
			return null;
		}
		return dto;
	}

	@Override
	public List<OrderConsumeDetailVo> findOrderConsumeDetail(OrderConsumeDetailDto dto) {
		List<OrderConsumeDetailVo> result = orderConsumeDetailMapper.findOrderConsumeDetail(dto);
		return result;
	}

	/**
	 * 
	 * 
	 * @Title OrderConsumeDetailServiceImpl.stringAmountExchangeBigDecimal
	 * @Description: 金额做扩缩小,type为1缩小1000倍， type=2缩小100倍,type=3扩大10倍
	 *
	 * @param amount
	 * @return
	 * 
	 * @version: 1.0 
	 * @author zangjintian
	 * 修改历史: 
	 * 修改人: zangjintian, 修改日期 : 2019年06月14日 下午6:50:59
	 * 修改内容 :
	 */
	public BigDecimal stringAmountShrinkBigDecimal(String amount,String type) {
		if(StringUtils.isNotBlank(amount) && StringUtils.isNotBlank(type)) {
			if(type.equals("1")) {
				BigDecimal returnBigDecimal = new BigDecimal(amount);
				returnBigDecimal = returnBigDecimal.divide(new BigDecimal(1000)).setScale(2, RoundingMode.DOWN);
				return  returnBigDecimal;
			}else if(type.equals("2")){
				BigDecimal returnBigDecimal = new BigDecimal(amount);
				returnBigDecimal = returnBigDecimal.divide(new BigDecimal(100)).setScale(2, RoundingMode.DOWN);
				return  returnBigDecimal;
			}else if(type.equals("3")) {
				BigDecimal returnBigDecimal = new BigDecimal(amount);
				return MoneyUtils.moneyExpand10(returnBigDecimal);
			}
		}
		return new BigDecimal(0);
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0000(OrderConsumeDetailDto dto) {
		OrderConsumeDetailVo returnVo = new OrderConsumeDetailVo();
		if(StringUtils.isNotBlank(dto.getSceneCode()) && StringUtils.isNotBlank(dto.getRemark()) && dto.getSceneCode().equals(SceneCodeStatus.ENTITY_CARD_BILL.getCateCode())) {
			String json = dto.getRemark();
			Map<String,Object> map = JSON.parseObject(json);
			if(map == null || map.isEmpty()) {
				log.error("消费场景-实体卡类型订单json转为map为空");
				returnVo.setRemark("消费场景-实体卡类型订单json转为map为空");
				return returnVo;
			}
			if(ObjectUtil.isNull(map.get("merchs")) || StringUtils.isEmpty(map.get("merchs").toString())) {
				log.error("消费场景-实体卡类型订单json转map后取key值为merchs的订单数组集合信息为空，merchs:{}", map.get("merchs"));
				returnVo.setRemark("消费场景-实体卡类型订单json转map后取key值为merchs数组集合信息为空");
				return returnVo;
			}
			List<Map> listStr = (List<Map>)map.get("merchs");
			if(CollUtil.isEmpty(listStr)) {
				log.error("消费场景-实体卡类型订单json转map后取key值为merchs的订单数组集合信息为空,转为list<map后> listStr:{}", listStr);
				returnVo.setRemark("消费场景-实体卡类型订单json转map后取key值为merchs数组集合信息为空");
				return returnVo;
			}
			for (Map map2 : listStr) {
				if(ObjectUtil.isNull(map2.get("merchName")) || StringUtils.isEmpty(map2.get("merchName").toString()) 
						|| ObjectUtil.isNull(map2.get("faceValue")) || StringUtils.isEmpty(map2.get("faceValue").toString()) 
						|| ObjectUtil.isNull(map2.get("num")) || StringUtils.isEmpty(map2.get("num").toString())) {
					log.error("消费场景-实体卡类型订单json取key为merchs的集合信息，key为merchName或key为faceValue或key为num的值为空,merchName:{},faceValue:{},num:{}",map2.get("merchName"),map2.get("faceValue"),map2.get("num"));
					returnVo.setRemark("消费场景-实体卡类型订单json取key为merchs的集合信息，key为merchName或key为faceValue或key为num的值为空");
					return returnVo;
				}
			}
		}else {
			log.error("场景编码为空或此场景编码对应的不是实体卡类型订单");
		}
		return returnVo;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0004(OrderConsumeDetailDto dto) {
		/*(itemName) (specValue) x(num)*/
		OrderConsumeDetailVo returnVo = new OrderConsumeDetailVo();
		if(StringUtils.isNotBlank(dto.getSceneCode()) && StringUtils.isNotBlank(dto.getRemark()) && dto.getSceneCode().equals(SceneCodeStatus.ELECTRON_CARD_BILL.getCateCode())) {
			List<Object> array = JSONArray.parseArray(dto.getRemark());
			List<Map> mapList = Lists.newArrayList();
			if(CollUtil.isEmpty(array)) {
				log.error("消费场景-电子卡类型订单json转list后返回结果为空,array:{}", array);
				returnVo.setRemark("消费场景-电子卡类型订单json转list后返回结果为空");
				return returnVo;
			}
			mapList = (List)array;
			if(CollUtil.isEmpty(mapList)) {
				log.error("消费场景-电子卡类型订单json转List<map>类型返回结果为空，mapList:{}", mapList);
				returnVo.setRemark("消费场景-电子卡类型订单json转list后返回结果为空");
				return returnVo;
			}
			for (Map map : mapList) {
				/***电子卡中的特殊的嘉福E卡，type不为空**/
				if(ObjectUtil.isNotNull(map.get("type"))) {
					if(ObjectUtil.isNull(map.get("num")) || StringUtils.isEmpty(map.get("num").toString()) 
							|| ObjectUtil.isNull(map.get("merchName")) || StringUtils.isEmpty(map.get("merchName").toString()) 
							|| ObjectUtil.isNull(map.get("faceValue")) || StringUtils.isEmpty(map.get("faceValue").toString()) ) {
						log.error("消费场景-电子卡类型的嘉福E卡，key为num或key为merchName或key为faceValue的值为空，num:{},merchName:{},faceValue:{}",map.get("num"),map.get("merchName"),map.get("faceValue"));
						returnVo.setRemark("消费场景-电子卡类型的嘉福E卡，key为num或key为merchName或key为faceValue的值为空");
						return returnVo;
					}
				}else {
					/***其他类型电子卡***/
					if(ObjectUtil.isNull(map.get("itemName")) || StringUtils.isEmpty(map.get("itemName").toString()) 
							|| ObjectUtil.isNull(map.get("specValue")) || StringUtils.isEmpty(map.get("specValue").toString()) 
							|| ObjectUtil.isNull(map.get("num")) || StringUtils.isEmpty(map.get("num").toString())) {
						log.error("消费场景-电子卡类型订单，key为itemName或key为specValue或key为num的值为空，itemName:{},specValue:{},num:{}",map.get("itemName"),map.get("specValue"),map.get("num"));
						returnVo.setRemark("消费场景-电子卡类型订单，key为itemName或key为specValue或key为num的值为空");
						return returnVo;
					}
				}
			}
		}else {
			log.error("场景编码为空或此场景编码对应的不是电子卡类型订单");
		}
		return returnVo;
		
	}

	@Override
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0008(OrderConsumeDetailDto dto) {
		/*(mobileNumber)
		(faceValue)*/
		OrderConsumeDetailVo returnVo = new OrderConsumeDetailVo();
		if(StringUtils.isNotBlank(dto.getSceneCode()) && StringUtils.isNotBlank(dto.getRemark()) && dto.getSceneCode().equals(SceneCodeStatus.MOBILE_RECHARGE_BILL.getCateCode())) {
			Map<String,Object> map = JSON.parseObject(dto.getRemark());
			if(map == null || map.isEmpty()) {
				log.error("消费场景-手机充值类订单json转map结果为空，map:{}",map);
				returnVo.setRemark("消费场景-手机充值类订单json转map结果为空");
				return returnVo;
			}
			if(ObjectUtil.isNull(map.get("mobileNumber")) || StringUtils.isEmpty(map.get("mobileNumber").toString()) 
					|| ObjectUtil.isNull(map.get("faceValue")) || StringUtils.isEmpty(map.get("faceValue").toString()) ) {
				log.error("消费场景-手机充值类订单key值为mobileNumber或key值为faceValue的值为空，mobileNumber:{},faceValue:{}",map.get("mobileNumber"), map.get("faceValue"));
				returnVo.setRemark("消费场景-手机充值类订单key值为mobileNumber或key值为faceValue的值为空");
				return returnVo;
			}
		}else {
			log.error("场景编码为空或此场景编码对应的手机充值类型订单");
		}
		return returnVo;
	}

	@Override
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0042(OrderConsumeDetailDto dto) {
	/*	(trainNo) (trainDate) (trainTime)

		(fromStation)

		(toStation)*/
		OrderConsumeDetailVo returnVo = new OrderConsumeDetailVo();
		if(StringUtils.isNotBlank(dto.getSceneCode()) && StringUtils.isNotBlank(dto.getRemark()) && dto.getSceneCode().equals(SceneCodeStatus.FLIGHT_BILL.getCateCode())) {
			Map<String,Object> map = JSON.parseObject(dto.getRemark());
			if(map == null || map.isEmpty()) {
				log.error("消费场景-航班管家类订单json转map结果为空,map:{}",map);
				returnVo.setRemark("消费场景-航班管家类订单json转map结果为空");
				return returnVo;
			}
			if(ObjectUtil.isNull(map.get("trainNo")) || StringUtils.isEmpty(map.get("trainNo").toString()) 
					|| ObjectUtil.isNull(map.get("trainDate")) || StringUtils.isEmpty(map.get("trainDate").toString()) 
					|| ObjectUtil.isNull(map.get("trainTime")) || StringUtils.isEmpty(map.get("trainTime").toString()) 
					|| ObjectUtil.isNull(map.get("fromStation")) || StringUtils.isEmpty(map.get("fromStation").toString())
					|| ObjectUtil.isNull(map.get("toStation")) ||  StringUtils.isEmpty(map.get("toStation").toString())) {
				log.error("消费场景-航班管家类订单key值为trainNo或key值为trainDate或key值为trainTime或key值为fromStation或key值为toStation的值为空，"
						+ "trainNo:{},trainDate:{},trainTime:{},fromStation:{},toStation:{}",map.get("trainNo"), map.get("trainDate"), map.get("trainTime"), map.get("fromStation"), map.get("toStation"));
				returnVo.setRemark("消费场景-航班管家类订单key值为trainNo或key值为trainDate或key值为trainTime或key值为fromStation或key值为toStation的值为空");
				return returnVo;
			}
		}else {
			log.error("场景编码为空或此场景编码对应的航班管家类型订单");
		}
		return returnVo;
	}

	@Override
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0039(OrderConsumeDetailDto dto) {
//		(appid)
		OrderConsumeDetailVo returnVo = new OrderConsumeDetailVo();
		if(StringUtils.isNotBlank(dto.getSceneCode()) && StringUtils.isNotBlank(dto.getRemark()) && dto.getSceneCode().equals(SceneCodeStatus.TAKEOUT_MEITUAN.getCateCode())) {
			Map<String,Object> map = JSON.parseObject(dto.getRemark());
			if(map == null || map.isEmpty()) {
				log.error("消费场景-美团外卖类订单json转map结果为空,map:{}", map);
				returnVo.setRemark("消费场景-美团外卖类订单json转map结果为空");
				return returnVo;
			}
			if(ObjectUtil.isNull(map.get("appid")) || StringUtils.isEmpty(map.get("appid").toString()) ) {
				log.error("消费场景-美团外卖类订单key值为appid的值为空，appid:{}",map.get("appid"));
				returnVo.setRemark("消费场景-美团外卖类订单key值为appid的值为空");
				return returnVo;
			}
		}else {
			log.error("场景编码为空或此场景编码对应的美团外卖类型订单");
		}
		return returnVo;
	}

	@Override
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0083(OrderConsumeDetailDto dto) {
//		(storeName)
		OrderConsumeDetailVo returnVo = new OrderConsumeDetailVo();
		if(StringUtils.isNotBlank(dto.getSceneCode()) && StringUtils.isNotBlank(dto.getRemark()) && dto.getSceneCode().equals(SceneCodeStatus.BUSINESS_SCAN_CODE_BILL.getCateCode())) {
			Map<String,Object> map = JSON.parseObject(dto.getRemark());
			if(map == null || map.isEmpty()) {
				log.error("消费场景-商户扫码类订单json转map结果为空,map:{}", map);
				returnVo.setRemark("消费场景-商户扫码类订单json转map结果为空");
				return returnVo;
			}
			if(ObjectUtil.isNull(map.get("storeName")) || StringUtils.isEmpty(map.get("storeName").toString())) {
				log.error("消费场景-商户扫码类订单key值为storeName的值为空，storeName:{}",map.get("storeName"));
				returnVo.setRemark("消费场景-商户扫码类订单key值为storeName的值为空");
				return returnVo;
			}
		}else {
			log.error("场景编码为空或此场景编码对应的商户扫码类型订单");
		}
		return returnVo;
	}

	@Override
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0062(OrderConsumeDetailDto dto) {
//		(itemCode)
		OrderConsumeDetailVo returnVo = new OrderConsumeDetailVo();
		if(StringUtils.isNotBlank(dto.getSceneCode()) && StringUtils.isNotBlank(dto.getRemark()) && dto.getSceneCode().equals(SceneCodeStatus.LIFE_PAY_BILL.getCateCode())) {
			Map<String,Object> map = JSON.parseObject(dto.getRemark());
			if(map == null || map.isEmpty()) {
				log.error("消费场景-生活缴费类订单json转map结果为空,map:{}", map);
				returnVo.setRemark("消费场景-生活缴费类订单json转map结果为空");
				return returnVo;
			}
			if(ObjectUtil.isNull(map.get("itemCode")) || StringUtils.isEmpty(map.get("itemCode").toString()) ) {
				// TODO 消费
				log.info("消费场景-生活缴费类订单key值为itemCode的值为空，<设值为：消费> itemCode:{}",map.get("itemCode"));
				/*returnVo.setRemark("消费场景-生活缴费类订单key值为itemCode的值为空");
				return returnVo;*/
			}
		}else {
			log.error("场景编码为空或此场景编码对应的生活缴费类型订单");
		}
		return returnVo;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0046(OrderConsumeDetailDto dto) {
//		(item_name)
		OrderConsumeDetailVo returnVo = new OrderConsumeDetailVo();
		if(StringUtils.isNotBlank(dto.getSceneCode()) && StringUtils.isNotBlank(dto.getRemark()) && dto.getSceneCode().equals(SceneCodeStatus.SHOPPING_MALL_BILL.getCateCode())) {
			Map<String,Object> map = JSON.parseObject(dto.getRemark());
			if(map == null || map.isEmpty()) {
				log.error("消费场景-惠购商城类订单json转map结果为空,map:{}",map);
				returnVo.setRemark("消费场景-惠购商城类订单json转map结果为空");
				return returnVo;
			}
			if(ObjectUtil.isNull(map.get("details")) || StringUtils.isEmpty(map.get("details").toString())) {
				log.error("消费场景-惠购商城类订单key值为details的值为空，details:{}",map.get("details"));
				returnVo.setRemark("消费场景-惠购商城类订单key值为details的值为空");
				return returnVo;
			}
			List<Object> detailList = JSONArray.parseArray(map.get("details").toString());
			List<Map> detailMap = Lists.newArrayList();
			if(CollUtil.isEmpty(detailList)) {
				log.error("消费场景-惠购商城类订单key值为details的值为空，string转数组details:{}",JSON.toJSON(detailList));
				returnVo.setRemark("消费场景-惠购商城类订单key值为details的值为空");
				return returnVo;
			}
			detailMap = (List)detailList;
			for (Map map2 : detailMap) {
				if(ObjectUtil.isNull(map2.get("item_name")) || StringUtils.isEmpty(map2.get("item_name").toString())) {
					log.error("消费场景-惠购商城类订单key值为details的详细信息item_name为空，item_name:{}", map2.get("item_name"));
					returnVo.setRemark("消费场景-惠购商城类订单key值为details的详细信息item_name为空");
					return returnVo;
				}
			}
		}else {
			log.error("场景编码为空或此场景编码对应的惠购商城类型订单");
		}
		return returnVo;
	}

	@Override
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0041(OrderConsumeDetailDto dto) {
		/*(departure_time)

		(city)(start_name)

		(end_city)(end_name)*/
		OrderConsumeDetailVo returnVo = new OrderConsumeDetailVo();
		if(StringUtils.isNotBlank(dto.getSceneCode()) && StringUtils.isNotBlank(dto.getRemark()) && dto.getSceneCode().equals(SceneCodeStatus.DRIP_TRIPS_BILL.getCateCode())) {
			Map<String,Object> map = JSON.parseObject(dto.getRemark());
			if(map == null || map.isEmpty()) {
				log.error("消费场景-滴滴出行类订单json转map结果为空，map:{}",map);
				returnVo.setRemark("消费场景-滴滴出行类订单json转map结果为空");
				return returnVo;
			}
			if(ObjectUtil.isNull(map.get("data")) || StringUtils.isEmpty(map.get("data").toString())) {
				log.error("消费场景-滴滴出行类订单key为data的值为空，data:{}", map.get("data"));
				returnVo.setRemark("消费场景-滴滴出行类订单key为data的值为空");
				return returnVo;
			}
			Map<String,Object> mapStr = JSON.parseObject(map.get("data").toString());
			if(mapStr == null || mapStr.isEmpty()) {
				log.error("消费场景-滴滴出行类订单key为data的转map结果为空，mapStr:{}", mapStr);
				returnVo.setRemark("消费场景-滴滴出行类订单key为data的转map结果为空");
				return returnVo;
			}
			if(ObjectUtil.isNull(mapStr.get("order")) || StringUtils.isEmpty(mapStr.get("order").toString())) {
				log.error("消费场景-滴滴出行类订单key为order的值为空，order:{}", mapStr);
				returnVo.setRemark("消费场景-滴滴出行类订单key为order的值为空");
				return returnVo;
			}
			Map<String,Object> mapStr1 = JSON.parseObject(mapStr.get("order").toString());
			if(mapStr1 == null || mapStr1.isEmpty()) {
				log.error("消费场景-滴滴出行类订单key为order的值转map结果为空，mapStr1:{}", mapStr1);
				returnVo.setRemark("消费场景-滴滴出行类订单key为order的值转map结果为空");
				return returnVo;
			}
			if(ObjectUtil.isNull(mapStr1.get("departure_time")) || StringUtils.isEmpty(mapStr1.get("departure_time").toString())
					|| ObjectUtil.isNull(mapStr1.get("city")) || StringUtils.isEmpty(mapStr1.get("city").toString())
					|| ObjectUtil.isNull(mapStr1.get("start_name")) || StringUtils.isEmpty(mapStr1.get("start_name").toString())
					|| ObjectUtil.isNull(mapStr1.get("end_city")) || StringUtils.isEmpty(mapStr1.get("end_city").toString())
					|| ObjectUtil.isNull(mapStr1.get("end_name")) || StringUtils.isEmpty(mapStr1.get("end_name").toString()) ) {
				log.error("消费场景-滴滴出行类订单order详细信息，key值为departure_time或key值为city或key值为start_name或key值为end_city或key值为end_name的值为空，"
						+ "departure_time:{},city:{},start_name:{},end_city:{},end_name:{}", mapStr1.get("departure_time"), mapStr1.get("city"), mapStr1.get("start_name"),
						mapStr1.get("end_city"), mapStr1.get("end_name"));
				returnVo.setRemark("消费场景-滴滴出行类订单order详细信息，key值为departure_time或key值为city或key值为start_name或key值为end_city或key值为end_name的值为空");
				return returnVo;
			}
		}else {
			log.error("场景编码为空或此场景编码对应的滴滴出行类型订单");
		}
		return returnVo;
	}

	@Override
	public OrderConsumeDetailVo checkJsonFormatBySceneCode0063(OrderConsumeDetailDto dto) {
//		(payeeMerName)
		OrderConsumeDetailVo returnVo = new OrderConsumeDetailVo();
		if(StringUtils.isNotBlank(dto.getSceneCode()) && StringUtils.isNotBlank(dto.getRemark()) && dto.getSceneCode().equals(SceneCodeStatus.CHINA_BANK_CARD_CONSUMEBILL.getCateCode())) {
			Map<String,Object> map = JSON.parseObject(dto.getRemark());
			if(map == null || map.isEmpty()) {
				log.error("消费场景-中银卡通消费类订单json转map结果为空，map:{}",map);
				returnVo.setRemark("消费场景-中银卡通消费类订单json转map结果为空");
				return returnVo;
			}
			if(ObjectUtil.isNull(map.get("payeeMerName")) || StringUtils.isEmpty(map.get("payeeMerName").toString())) {
				log.error("消费场景-中银卡通消费类订单key为payeeMerName的值为空，payeeMerName:{}", map.get("payeeMerName"));
				returnVo.setRemark("消费场景-中银卡通消费类订单key为payeeMerName的值为空");
				return returnVo;
			}
		}else {
			log.error("场景编码为空或此场景编码对应的中银卡通消费类型订单");
		}
		return returnVo;
	}
}
