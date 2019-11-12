package com.taolue.baoxiao.fund.service.impl;

import java.io.Serializable;

import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.taolue.baoxiao.common.bean.SpringBeanUtils;
import com.taolue.baoxiao.common.util.exception.BaoxiaoException;

import cn.hutool.core.bean.copier.BeanCopier;
import cn.hutool.core.bean.copier.CopyOptions;

/**
 * 
 * @ClassName:  HisSupportServiceImpl   
 * @Description:TODO(这里用一句话描述这个类的作用)   
 * @Author: shilei
 * @date:   2018年8月20日 下午2:55:37   
 *   
 * @param <M>
 * @param <T>
 * @param <H>  
 * @Copyright: 2018 www.jia-fu.cn Inc. All rights reserved. 
 * 注意：本内容仅限于上海淘略数据处理有限公司信息技术部内部传阅，禁止外泄以及用于其他的商业目的。
 */
public class HisSupportServiceImpl<M extends BaseMapper<T>, T, H> extends ServiceImpl<M, T> {
	public static String PER_FIX_HIS_STR = "His";
	public static String PER_FIX_HIS_BEAN_STR = "his";
	private ServiceImpl<? extends BaseMapper<H>, H> hisServices;
	
	@SuppressWarnings("unchecked")
	private  H fetchHisClass(T entity) {
		String packageName = entity.getClass().getPackage().getName();
		String classHisName = PER_FIX_HIS_STR+entity.getClass().getSimpleName();
		try {
			return (H)Class.forName(packageName+"."+classHisName).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException cause) {
			throw new BaoxiaoException(cause);
		} 
	};
	
	private  ServiceImpl<? extends BaseMapper<H>, H> fetchHisServiceBean() {
		if (this.hisServices == null) { 
			String classHisName = PER_FIX_HIS_BEAN_STR+this.getClass().getSimpleName();
			this.hisServices =  SpringBeanUtils.getBean(classHisName);
		}
		return this.hisServices;
	};
	
	/**
	 * 
	 * @Title: insertHis   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   2018年8月20日 下午2:55:51  
	 * @param: @param entity
	 * @param: @return      
	 * @return: boolean      
	 * @throws
	 */
	@Transactional(rollbackFor = Exception.class)
    public boolean insertHis(T entity) {
		if (entity != null) { 
			BeanCopier<H> copier = new BeanCopier<>(entity, fetchHisClass(entity), CopyOptions.create());
			return fetchHisServiceBean().insert(copier.copy()); 
		}
		return false;
    }	
	
	/**
	 * 
	 * @Title: selectInsertHis   
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @Author: shilei
	 * @date:   2018年8月20日 下午2:55:55  
	 * @param: @param key
	 * @param: @return      
	 * @return: boolean      
	 * @throws
	 */
	@Transactional(rollbackFor = Exception.class)
    public boolean selectInsertHis(Serializable key) {
		T entity = this.selectById(key);
		if (entity != null) { 
			BeanCopier<H> copier = new BeanCopier<>(entity, fetchHisClass(entity), CopyOptions.create());
			return fetchHisServiceBean().insert(copier.copy()); 
		}
		return false;
    }
}
