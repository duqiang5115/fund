package com.taolue.baoxiao.fund.service.remote.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.taolue.baoxiao.common.constant.CommonConstant;
import com.taolue.baoxiao.common.constant.enums.DictionaryEnum.BusinessApplyStatus;
import com.taolue.baoxiao.common.util.Exceptions;
import com.taolue.baoxiao.common.util.R;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;
import com.taolue.baoxiao.fund.api.openplatform.IDockOpenPlatformService;
import com.taolue.baoxiao.fund.api.vo.RemoteResultVo;
import com.taolue.baoxiao.fund.common.exception.FundServiceException;
import com.taolue.baoxiao.fund.common.exception.FundServiceExceptionGenerator;
import com.taolue.baoxiao.fund.service.remote.IOpenPlatformService;
import com.taolue.dock.api.dto.CashOrderDto;
import com.taolue.dock.api.vo.CashOrderVo;
import com.taolue.dock.api.vo.OrderQueryVo;
import com.taolue.dock.api.vo.UserVo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @ClassName:  OpenPlatformServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:53:38   
 *     
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Service
@Slf4j
public class OpenPlatformServiceImpl implements IOpenPlatformService {
    private static final boolean isDebug = log.isDebugEnabled();
    
    @Autowired
    private IDockOpenPlatformService dockOpenPlatformService;
    
    @Value("${application.model}")
	private String applicationModel = "";
    
    
	@Override
	public BigDecimal findCompanyRecharge(String memberId) {
		R<UserVo> userVoR = this.dockOpenPlatformService.accountGetById(memberId, CommonConstant.STRING_BLANK);
		if (userVoR.getCode() != 0) {
			throw new BaoxiaoException(904, userVoR.getMsg());
		}
		return userVoR.getData().getSalary_balance();
	}
	
	@Override
	public void tradeRefundnr(BigDecimal amount, String flowNo, String orderNo) {
		try {
			log.info("当前接口对象为：{}",new Object[]{this.dockOpenPlatformService});
			this.dockOpenPlatformService.tradeRefundnr(amount, flowNo, orderNo);
		} catch (Exception e) {
			throw new BaoxiaoException(e);
		}
	}
	
	public RemoteResultVo<CashOrderVo> cashOrder(CashOrderDto cashOrderDto) {
		RemoteResultVo<CashOrderVo> remoteResult = new RemoteResultVo<>();
		if (ObjectUtil.isNull(cashOrderDto) || StrUtil.isBlank(cashOrderDto.getBizId())) {
			FundServiceExceptionGenerator.FundServiceException(HttpStatus.HTTP_UNAVAILABLE, 
					"发送提现请求失败，原因：提现请求为空或者请求编号为空");
		} else {
			if (isDebug) {
				log.debug("提现请求参数为{}", JSON.toJSONString(cashOrderDto));
			}
		}
		
		R<CashOrderVo> r = new R<>();
		try {
			if ("test".equals(this.applicationModel)) {
				int sed = RandomUtil.randomInt(1,6);
				switch (sed) {
				case 1:
					r.setCode(6011);
					r.setMsg("测试模拟出非6012错误");
					break;
				case 2:
					r.setCode(6012);
					r.setMsg("测试模拟出6012错误");
					break;
				case 3:
					r.setCode(0);
					r.setMsg("测试模拟成功");
					CashOrderVo vo = new CashOrderVo();
					vo.setBizId(cashOrderDto.getBizId());
					vo.setOrderId(cashOrderDto.getBizId());
					vo.setAmount(cashOrderDto.getAmount());
					vo.setPayAmount(cashOrderDto.getAmount());
					r.setData(vo);
					break;
				case 4:
					r =null;
					break;
				case 5:
					r.setCode(1);
					r.setMsg("测试模拟错误");
					break;
				case 6:
					throw new NullPointerException("测试直接异常");
//					break;
				default:
					break;
				}
			} else {
				r = this.dockOpenPlatformService.cashOrder(cashOrderDto);
			}
			if (ObjectUtil.isNull(r)) {
				FundServiceExceptionGenerator.FundServiceException(HttpStatus.HTTP_UNAVAILABLE, "发送编号为{}的提现请求失败，原因：无返回结果",cashOrderDto.getBizId());
			} else {
				if (isDebug) {
					log.debug("提现请求返回值为{}", JSON.toJSONString(r));
				}
				if (R.SUCCESS != r.getCode()) {
					remoteResult.setMessage(r.getMsg());
					if (6012 == r.getCode()) {
						remoteResult.setCode(HttpStatus.HTTP_GATEWAY_TIMEOUT);
						remoteResult.setStatus(BusinessApplyStatus.PAUSE);
					} else {
						remoteResult.setCode(HttpStatus.HTTP_UNAVAILABLE);
						remoteResult.setStatus(BusinessApplyStatus.REFUSE);
					}
				} else {
					remoteResult.setCode(HttpStatus.HTTP_OK);
					remoteResult.setStatus(BusinessApplyStatus.PROCESS);
					remoteResult.setData(r.getData());
					remoteResult.setMessage(r.getMsg());
				}
			}
		} catch (Exception e) {
			log.error(Exceptions.getStackTraceAsString(e));
			remoteResult.setStatus(BusinessApplyStatus.REFUSE);
			if (e instanceof FundServiceException) {
				remoteResult.setCode(((FundServiceException) e).getCode());
				remoteResult.setMessage(((FundServiceException) e).getMsg());
			} else {
				remoteResult.setCode(HttpStatus.HTTP_UNAVAILABLE);
				remoteResult.setMessage(e.getMessage());
			}
		}
		return remoteResult;
	}

	/**
	 * 
	 * <p>名称:类OpenPlatformServiceImpl中的orderQuery方法</br>    
	 * <p>描述:(这里用一句话描述这个方法的作用)</br> 
	 * <p>作者: shilei</br> 
	 * <p>日期: Mar 28, 2019 2:05:52 AM</br>
	 * @throws Exception
	 * @param withdrawNo 提现单据号码
	 * @return  R<BigDecimal> 
	 * 
	 * code=500 成功；code=503 处理中；code=505 失败；
	 * code = 501 需换卡；code=502 需改额
	 */
	public RemoteResultVo<BigDecimal> orderQuery(String withdrawNo) {
		
		R<OrderQueryVo> r = new R<>();
		RemoteResultVo<BigDecimal> result = new RemoteResultVo<>();
		
		try {
			
			if ("test".equals(this.applicationModel)) {
				int sed = RandomUtil.randomInt(1,8);
				OrderQueryVo data = new OrderQueryVo();
				
				switch (sed) {
				case 1:
					r.setCode(0);
					data.setState("3");
					data.setErrorMsg("该卡已达本日限额,可用额度99000.00,已使用99000.00");
					r.setData(data);
					break;
				case 2:
					r.setCode(0);
					data.setState("2");
					data.setBizMsg("提现处理中，请稍后尝试！！");
					r.setData(data);
					break;
				case 3:
					r.setCode(0);
					data.setState("1");
					data.setBizMsg("提现成功！！");
					r.setData(data);
					break;
				case 4:
					r =null;
					break;
				case 5:
					r.setCode(0);
					data.setState("3");
					data.setErrorMsg("该卡已达本日限额,可用额度99000.00,已使用50000.00");
					r.setData(data);
					break;
				case 6:
					r.setCode(0);
					data.setState("3");
					data.setErrorMsg("已达本日限额,可用额度99000.00,已使用99000.00");
					r.setData(data);
					break;
				case 7:
					r.setCode(0);
					data.setState("3");
					data.setErrorMsg("已达本日限额,可用额度99000.00,已使用50000.00");
					r.setData(data);
					break;
				case 8:
					throw new Exception("测试抛出异常！！");
					//break;
				default:
					break;
				}
			} else {
				r = this.getDockOpenPlatformService().orderQuery(withdrawNo);
			}
			
			if (ObjectUtil.isNull(r)) {
				FundServiceExceptionGenerator.FundServiceException(HttpStatus.HTTP_VERSION, 
						"查询提现单据{}的提现结果出现问题,无法获取该单据的结果",withdrawNo);
			}
			
			if (isDebug) {
				log.debug("查询提现单据{}的提现结果为{}",withdrawNo, JSON.toJSONString(r));
			}
			if (r.getCode() != R.SUCCESS) {
				result.setCode(HttpStatus.HTTP_VERSION);
				result.setStatus(BusinessApplyStatus.FAULT);
				result.setMessage(r.getMsg());
			} else {
				OrderQueryVo data = r.getData();
				String status = data.getState();
				String error = StrUtil.isEmpty(data.getErrorMsg()) ? 
						CommonConstant.STRING_BLANK : data.getErrorMsg();
				String msg = StrUtil.isEmpty(data.getBizMsg()) ? 
						CommonConstant.STRING_BLANK : data.getBizMsg();
				
				//成功
				if ("1".equals(status)) {
					//do success
					result.setCode(HttpStatus.HTTP_OK);
					result.setStatus(BusinessApplyStatus.COMPLETED);
					result.setMessage(msg);
				} else if ("2".equals(status)) {
					//process
					result.setCode(HttpStatus.HTTP_UNAVAILABLE);
					result.setStatus(BusinessApplyStatus.PROCESS);
					result.setMessage(msg);
				} else if ("3".equals(status)){
					result = this.getError(error);
				}
			}
		} catch (Exception e) {
			log.error("提现结果查询出现问题{}",Exceptions.getStackTraceAsString(e));
			result.setCode(HttpStatus.HTTP_VERSION);
			result.setMessage(e.getMessage());
			result.setStatus(BusinessApplyStatus.FAULT);
		}
		return result;
	}
	
	private RemoteResultVo<BigDecimal> getError(String errorMsg) {
		/*已达本日限额。可用额度xxxx，已使用xxxx
		已达本月限额。可用额度xxxx，已使用xxxx
		该卡已达本日限额。可用额度xxxx，已使用xxxx
		该卡已达本月限额。可用额度xxxx，已使用xxxx
		 */
		RemoteResultVo<BigDecimal> result = new RemoteResultVo<>(CommonConstant.NO_AMOUNT);
		//-无法分析
		result.setCode(HttpStatus.HTTP_VERSION);
		result.setStatus(BusinessApplyStatus.FAULT);
		result.setMessage(errorMsg);
		
		List<String> results = ReUtil.getAllGroups(Pattern.compile(CommonConstant.WITDRAW_RESULT_REGEX_STR),
				errorMsg, false);
		if (CollUtil.isNotEmpty(results) && results.size() == 3) {
			String first = results.get(0);
			BigDecimal canUsed = new BigDecimal(results.get(1));
			BigDecimal used = new BigDecimal(results.get(2));
			BigDecimal left = canUsed.subtract(used);
			result.setData(left.multiply(new BigDecimal(1000)));
			if (first.indexOf("卡")>=0) {
				//-卡限额
				result.setCode(HttpStatus.HTTP_NOT_IMPLEMENTED);
				result.setStatus(BusinessApplyStatus.FAULT);
			} else {
				result.setCode(HttpStatus.HTTP_BAD_GATEWAY);
				//-非卡限额
				if (left.compareTo(CommonConstant.NO_AMOUNT)<=0) {
					result.setStatus(BusinessApplyStatus.STOP);
				}
			}
		}
		return result;
	}
	/**  
	 * <p>Title:getDockOpenPlatformService</p><BR>  
	 * <p>Description:获取属性dockOpenPlatformService的值<BR>  
	 * @return IDockOpenPlatformService <BR>  
	 */
	public IDockOpenPlatformService getDockOpenPlatformService() {
		return dockOpenPlatformService;
	}
}
