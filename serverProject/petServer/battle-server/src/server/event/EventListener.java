package server.event;

import common.GameConst.EventType;
import model.obj.BaseObj;
import model.player.entity.Player;
import model.room.entity.Room;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import util.LogUtil;

public class EventListener {
    public static boolean listenEvent() {
        int result = 1;
        result &= listenEvent(EventType.ET_Login, new LoginEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_Logout, new LogoutEventHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_Offline, new OfflineEventHandler()) ? 1 : 0;

        result &= listenEvent(EventType.ET_AddSubmitResultCount, new AddSubmitResultCountHandler()) ? 1 : 0;
        result &= listenEvent(EventType.ET_BattleCheck, new BattleCheckHandler()) ? 1 : 0;
        return result == 1;
    }

    public static boolean listenEvent(int eventId, EventHandler handler) {
        return EventManager.getInstance().listenEvent(eventId, handler);
    }
}

final class LoginEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof Player && event.checkParamSize(3)) {
                Player player = (Player) obj;
                PvpBattlePlayerInfo playerInfo = event.getParam(0);
                String roomIdx = event.getParam(1);
                boolean isResume = event.getParam(2);
                player.onPlayerLogin(playerInfo, roomIdx, isResume);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class LogoutEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof Player) {
                Player player = (Player) obj;
                player.onPlayerLogout();
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class OfflineEventHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof Player) {
                Player player = (Player) obj;
                player.offline();
                if (event.getSource() instanceof Room) {
                    Room room = (Room) event.getSource();
                    room.playerOnlineChange(player.getIdx(), false);
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class AddSubmitResultCountHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof Room && event.getSource() instanceof Player) {
                Room room = (Room) obj;
                Player player = (Player) event.getSource();
                room.addSubmitResultCount(player);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}

final class BattleCheckHandler implements EventHandler {

    @Override
    public boolean onEvent(BaseObj obj, Event event) {
        try {
            if (obj instanceof Room && event.checkParamSize(1)) {
                Room room = (Room) obj;
                boolean preCheck = event.getParam(0);
                room.checkBattle(preCheck);
                return true;
            }
            return false;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }
}