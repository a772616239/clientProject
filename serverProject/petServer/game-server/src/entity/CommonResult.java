package entity;

import protocol.RetCodeId.RetCodeEnum;

/**
 * @author xiao_FL
 * @date 2019/7/15
 */
public class CommonResult {
    /**
     * 操作结果：true成功，false失败
     */
    private boolean success;

    /**
     * 返回码
     */
    private RetCodeEnum code;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public RetCodeEnum getCode() {
        return code;
    }

    public void setCode(RetCodeEnum code) {
        this.code = code;
    }

    public CommonResult() {
        success = false;
    }
}
