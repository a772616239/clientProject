package model.mistforest.map.grid;

import common.GameConst.EventType;
import common.GlobalTick;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.gridobj.MistGridObj;
import model.mistplayer.entity.MistPlayer;
import protocol.MistForest.MistAttackModeEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import server.event.Event;
import server.event.EventManager;
import util.GameUtil;

@Getter
@Setter
public class Grid {
    public Grid(int gridType) {
        this.gridType = gridType;
    }

    private int gridType;
    private boolean blocked;
    private boolean safeRegion;

    private MistGridObj gridObj;

    private Map<Long, MistFighter> fighterMap;

    public boolean isGridBlockForFighter(MistFighter fighter) {
        if (blocked) {
            return true;
        }
        return gridObj != null && gridObj.isGridBlock(fighter);
    }

    public boolean onObjEnter(MistFighter fighter) {
        if (fighter == null) {
            return false;
        }

        if (isSafeRegion()) {
            long attackMode = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE);
            if (attackMode == MistAttackModeEnum.EAME_Attack_VALUE) {
                long expireTime = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_ChangeAttackModeExpire_VALUE);
                if (expireTime > GlobalTick.getInstance().getCurrentTime()) {
                    MistPlayer owner = fighter.getOwnerPlayerInSameRoom();
                    Event event = Event.valueOf(EventType.ET_CalcPlayerDropItem, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
                    event.pushParam(true, owner, null);
                    EventManager.getInstance().dispatchEvent(event);
                }
            }
            if (attackMode!= MistAttackModeEnum.EAME_Peace_VALUE) {
                fighter.setAttribute(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE, MistAttackModeEnum.EAME_Peace_VALUE);
                fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_PlayerAttackMode_VALUE, MistAttackModeEnum.EAME_Peace_VALUE);
            }
        }

        if (gridObj != null) {
            gridObj.onPlayerEnter(fighter);
        }
        addFighter(fighter);
        return true;
    }

    public boolean onObjLeave(MistFighter fighter) {
        if (fighter == null) {
            return false;
        }

        if (gridObj != null) {
            gridObj.onPlayerLeave(fighter);
        }

        removeFighter(fighter);
        return true;
    }

    protected void addFighter(MistFighter fighter) {
        if (fighterMap == null) {
            fighterMap = new HashMap<>();
        }
        fighterMap.put(fighter.getId(), fighter);
    }

    protected void removeFighter(MistFighter fighter) {
        if (fighterMap != null) {
            fighterMap.remove(fighter.getId());
        }
    }
}
