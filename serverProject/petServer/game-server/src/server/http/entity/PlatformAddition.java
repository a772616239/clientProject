package server.http.entity;

/**
 * @author xiao_FL
 * @date 2019/12/2
 */
public class PlatformAddition {
    private int additionType;
    /**
     * 上限与下限相等为唯一ID
     */
    private int upLimit;
    private int lowerLimit;

    public int getAdditionType() {
        return additionType;
    }

    public void setAdditionType(int additionType) {
        this.additionType = additionType;
    }

    public int getUpLimit() {
        return upLimit;
    }

    public void setUpLimit(int upLimit) {
        this.upLimit = upLimit;
    }

    public int getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(int lowerLimit) {
        this.lowerLimit = lowerLimit;
    }
}
