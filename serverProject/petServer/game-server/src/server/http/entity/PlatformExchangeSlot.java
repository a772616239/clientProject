package server.http.entity;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/12/2
 */
public class PlatformExchangeSlot {
    /**
     * 兑换条件
     */
    private List<PlatformApposeAddition> apposeAddition;

    public List<PlatformApposeAddition> getApposeAddition() {
        return apposeAddition;
    }

    public void setApposeAddition(List<PlatformApposeAddition> apposeAddition) {
        this.apposeAddition = apposeAddition;
    }
}
