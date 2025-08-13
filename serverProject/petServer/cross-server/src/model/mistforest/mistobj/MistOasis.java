package model.mistforest.mistobj;

import common.GlobalTick;
import java.util.HashMap;
import java.util.Map;
import model.mistforest.MistConst.MistSkillTiming;
import model.mistforest.room.entity.MistRoom;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.UnitMetadata;
import util.TimeUtil;

public class MistOasis extends MistObject {
    protected Map<Long, Long> playerRecoverTimeData;

    public MistOasis(MistRoom room, int objType) {
        super(room, objType);
        playerRecoverTimeData = new HashMap<>();
    }

    @Override
    public void clear() {
        super.clear();
        playerRecoverTimeData.clear();
    }

    @Override
    public void reborn() {
        super.reborn();
        playerRecoverTimeData.clear();
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_OasisRecoverTimestamp_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        if (fighter == null || playerRecoverTimeData.isEmpty()) {
            return super.getMetaData(fighter);
        } else {
            long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
            Long canClickTimestamp = playerRecoverTimeData.get(playerId);
            if (canClickTimestamp == null || canClickTimestamp <= GlobalTick.getInstance().getCurrentTime()) {
                return super.getMetaData(fighter);
            }

            UnitMetadata.Builder metaData = UnitMetadata.newBuilder();
            metaData.mergeFrom(super.getMetaData(fighter));
            metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_OasisRecoverTimestamp_VALUE).addValues(canClickTimestamp);
            return metaData.build();
        }
    }

    public void clickByFighter(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        if (!fighter.checkTouchDis(this, fighter.isMoving())) {
            return;
        }

        long curTime = GlobalTick.getInstance().getCurrentTime();
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        Long canClickTimestamp = playerRecoverTimeData.get(playerId);
        if (canClickTimestamp != null && canClickTimestamp > curTime) {
            return;
        }
        long recoverInterval = getAttribute(MistUnitPropTypeEnum.MUPT_OasisRecoverInterval_VALUE);
        fighter.getSkillMachine().triggerPassiveSkills(MistSkillTiming.ClickOasis, fighter, null);

        long extBuffId = getAttribute(MistUnitPropTypeEnum.MUPT_OasisExtBuffId_VALUE);
        if (extBuffId > 0) {
            fighter.getBufMachine().addBuff((int) extBuffId,this, null);
        }
        long expireTime = curTime + recoverInterval * TimeUtil.MS_IN_A_S;
        playerRecoverTimeData.put(playerId, expireTime);
        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_OasisRecoverTimestamp_VALUE, expireTime);
    }
}
