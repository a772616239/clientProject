package model.mistforest.mistobj.gridobj;

import cfg.GameConfig;
import cfg.MistLootPackCarryConfig;
import common.GameConst;
import common.GameConst.EventType;
import java.util.HashSet;
import java.util.Set;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.UnitMetadata;
import server.event.Event;
import server.event.EventManager;

public class MistMazeDoor extends MistGridObj {
    private Set<Long> openedFighters;

    public MistMazeDoor(MistRoom room, int objType) {
        super(room, objType);
        openedFighters = new HashSet<>();
    }

    @Override
    public void clear() {
        super.clear();
        openedFighters.clear();
    }
    @Override
    public void reborn() {
        super.reborn();
        openedFighters.clear();
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_MazeDoorOpenState_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata metaData = super.getMetaData(fighter);
        if (fighter == null || !isAlive()) {
            return metaData;
        }
        if (!openedFighters.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE))) {
            return metaData;
        }
        UnitMetadata.Builder builder = metaData.toBuilder();
        builder.getPropertiesBuilder().addKeys(MistUnitPropTypeEnum.MUPT_MazeDoorOpenState).addValues(1);
        return builder.build();
    }

    @Override
    public boolean isGridBlock(MistFighter fighter) {
        if (fighter == null) {
            return false;
        }
        if (!isAlive()) {
            return false;
        }
        return !openedFighters.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
    }

    protected int getOpenMazeDoorNeedRewardId(int race) {
        int[] items = GameConfig.getById(GameConst.ConfigId).getMistmazeitems();
        if (items == null || items.length <= 0) {
            return 0;
        }
        for (int i = 0; i < items.length; i++) {
            if ((i + 1) == race) {
                return items[i];
            }
        }
        return 0;
    }

    public void openMazeDoor(MistFighter fighter) {
        if (fighter == null) {
            return;
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        int needRewardId = getOpenMazeDoorNeedRewardId((int) getAttribute(MistUnitPropTypeEnum.MUPT_MazeDoorRace_VALUE));
        if (MistLootPackCarryConfig.getById(needRewardId) == null) {
            return;
        }
        if (!player.checkRewardEnough(needRewardId, 1)) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_ConsumeLootPackReward, getRoom(), player);
        event.pushParam(needRewardId, 1);
        EventManager.getInstance().dispatchEvent(event);

        openedFighters.add(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_MazeDoorOpenState_VALUE, 1);
    }
}
