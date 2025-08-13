package platform.logs.entity;

import common.GlobalData;
import lombok.Getter;
import lombok.Setter;
import platform.logs.AbstractServerLog;

@Getter
@Setter
public class OnLinePlayerNumLog extends AbstractServerLog {
    private int onlineNum;

    public OnLinePlayerNumLog() {
        onlineNum = GlobalData.getInstance().getOnlinePlayerNum();
    }
}
