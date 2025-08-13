package common.entity;

/**
 * 平台返回实体公共参数
 *
 * @author xiao_FL
 * @date 2019/8/30
 */
public class HttpCommonResponse {
    private String retDes;

    private int retCode;

    public String getRetDes() {
        return retDes;
    }

    public void setRetDes(String retDes) {
        this.retDes = retDes;
    }

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }
}
