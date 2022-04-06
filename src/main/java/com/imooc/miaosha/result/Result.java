package com.imooc.miaosha.result;

/*用来封装结果,成功的时候只传数据，失败的时候串CodeMsg对象
* 为了更好的封装，set方法删除，不能new出来,只能调用方法*/
public class Result<T> {
    private int code; //状态码
    private String msg;
    private T data; //数据

    private Result(T data) {
        this.code=0;
        this.msg="success";
        this.data = data;
    }

    private Result(CodeMsg cm) {
        if(cm == null){
            return;
        }
        this.code=cm.getCode();
        this.msg=cm.getMsg();
    }
    /*成功时候调用*/
    public static <T> Result<T> success(T data){
        return new Result<T>(data);
    }

    /*失败的时候调用*/
    public static <T> Result<T> error(CodeMsg cm){
        return new Result<T>(cm);
    }



    public int getCode() {
        return code;
    }



    public String getMsg() {
        return msg;
    }



    public T getData() {
        return data;
    }


}
