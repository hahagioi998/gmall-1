package com.atguigu.gmall.model.activity;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀时 发消息的对象
 *    此对象  用户的ID 、库存的ID
 */
@Data
public class UserRecode implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long skuId;
	
	private String userId;
}
