package com.imooc.miaosha.vo;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import com.imooc.miaosha.validator.IsMobile;

/*
获取表单提交的数据
* 看表单中参数是否能正确传递
* */
public class LoginVo {
	
	@NotNull
	@IsMobile
	private String mobile;

	@NotNull
	@Length(min = 32)
	private String password;
	
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return "LoginVo [mobile=" + mobile + ", password=" + password + "]";
	}
}
