package model.mistforest.trigger;

import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;
import util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class Trigger {
    public ArrayList<ArrayList<Condition>> conditionList = new ArrayList<>();
    public ArrayList<Command> commandList = new ArrayList<>();

    public boolean fire(MistObject user, MistObject target, HashMap<Integer, Long> params) {
        if (user == null && target == null) {
            return false;
        }
        MistRoom room = user != null ? user.getRoom() : null;
        if (room == null) {
            room = target != null ? target.getRoom() : null;
        }
        if (room == null) {
            return false;
        }
        if (checkCondition(room, user, target, params)) {
            for (Command cmd : commandList) {
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append("RoomId=" + user.getRoom().getIdx());
                strBuilder.append(",user=" + user.getId());
                long targetId = target != null ? target.getId() : 0;
                strBuilder.append(",target=" + targetId);
                strBuilder.append(",trigger cmd params:");
                for (Integer param : cmd.cmdParams) {
                    strBuilder.append(param + ",");
                }
                LogUtil.debug(strBuilder.toString());
                cmd.ExecuteCmd(room, user, target, params);
            }
            return true;
        }
        return false;
    }

    public boolean checkCondition(MistRoom room, MistObject user, MistObject target, HashMap<Integer, Long> params) {
        if (conditionList == null || conditionList.isEmpty()) {
            return true;
        }
        for (ArrayList<Condition> condSubList : conditionList) {
            boolean subListCond = true;
            for (Condition condition : condSubList) {
                if (!condition.check(room, user, target, params)) {
                    subListCond = false;
                    break;
                }
            }
            if (subListCond) {
                return true;
            }
        }
        return false;
    }
}
