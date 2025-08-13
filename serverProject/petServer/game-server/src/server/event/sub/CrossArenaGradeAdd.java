package server.event.sub;

import model.crossarena.CrossArenaManager;
import model.obj.BaseObj;
import model.player.entity.playerEntity;
import platform.logs.ReasonManager;
import server.event.Event;
import server.event.EventHandler;
import util.LogUtil;

public class CrossArenaGradeAdd implements EventHandler {
    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof playerEntity && event.checkParamSize(2)) {
                playerEntity player = (playerEntity) obj;
                int addCount = event.getParam(0);
                ReasonManager.Reason reason = event.getParam(1);
                CrossArenaManager.getInstance().addGrade(player.getIdx(), addCount);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}
