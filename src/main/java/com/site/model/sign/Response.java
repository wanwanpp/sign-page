package com.site.model.sign;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 王萍
 * @date 2018/1/31 0031
 */
@Getter
@Setter
public class Response {

    /**
     * 0 失败
     * 1 成功
     * 2 签到失效
     * 3 已经签到
     */
    private int status;
    private String msg;

    public Response(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }
}
