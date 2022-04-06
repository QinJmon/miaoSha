package com.imooc.miaosha.exception;

import com.imooc.miaosha.result.CodeMsg;
import com.imooc.miaosha.result.Result;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.BindException;
import java.util.List;

@ControllerAdvice
@ResponseBody   //全局异常处理器
public class GlobalExceptionHandler {

    @ExceptionHandler(value=Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
        e.printStackTrace();  //将异常信息打印出来
        if(e instanceof GlobalException){
            GlobalException ex=(GlobalException)e; //将e强转为ex
            return Result.error(ex.getCm());//将错误结果输出
        }else if(e instanceof BindException){ //如果是绑定异常
            BindException ex = (BindException)e;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError error=errors.get(0); //只获取数组中的第一个异常
            String msg=error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));

        }else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }


}
