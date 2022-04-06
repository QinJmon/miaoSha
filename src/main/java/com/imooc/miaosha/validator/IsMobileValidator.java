package com.imooc.miaosha.validator;

import com.imooc.miaosha.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

//继承ConstraintValidator，第一个参数是注解名，第二个是注解修饰的变量类型
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {

    private boolean required=false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required=constraintAnnotation.required();

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(required){ //如果值是必须的，做合法校验
            return ValidatorUtil.isMobile(value);
        }else { //如果不是必须的，判断是否为空
            if(StringUtils.isEmpty(value)){
                return true;
            }else {
                return ValidatorUtil.isMobile(value);
            }

        }
    }
}
