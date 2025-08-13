package model.mistforest.mistobj;

import cfg.CrossConstConfig;
import cfg.MistGoblinConfig;
import cfg.MistGoblinConfigObject;
import common.GameConst;
import java.util.HashMap;
import model.mistforest.MistConst.MistTriggerParamType;
import model.mistforest.mistobj.rewardobj.MistCrystalBox;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import org.apache.commons.lang.math.RandomUtils;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.BattleCMD_SnapShotList;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistTaskTargetType;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.MistUnitTypeEnum;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.UnitSnapShot;

public class MistGoblin extends MistObject {
    public MistGoblin(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void afterInit(int[] initialPos, int[] initialToward) {
        super.afterInit(initialPos, initialToward);
        setAttribute(MistUnitPropTypeEnum.MUPT_LifeTime_VALUE, 0);
    }

    public void beTouch(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_PreDeadState_VALUE) > 0) {
            return;
        }
        long visiblePlayerId = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE);
        if (visiblePlayerId != fighter.getId()) {
            return;
        }
        long remainHp = getAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE);
        if (remainHp <= 0) {
            return;
        }
        remainHp -= CrossConstConfig.getById(GameConst.ConfigId).getTouchgoblindechp();
        remainHp = Math.max(0, remainHp);
        setAttribute(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, remainHp);
        addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_UnitRemainHp_VALUE, remainHp);
        if (remainHp == 0) {
            generateRewardObj(fighter);
            preDead();
        }

    }

    protected void preDead() {
        long preDeadBuffId = getAttribute(MistUnitPropTypeEnum.MUPT_PreDeadBuffId_VALUE);
        if (preDeadBuffId > 0) {
            HashMap<Integer, Long> params = new HashMap<>();
            params.put(MistTriggerParamType.RemoveObjId, getId());
            getBufMachine().addBuff((int) preDeadBuffId, this, params);
        } else {
            dead();
        }
    }

    protected void generateRewardObj(MistFighter fighter) {
        int configId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
        MistGoblinConfigObject cfg = MistGoblinConfig.getById(configId);
        if (cfg == null) {
            return;
        }
        int[][] rewardIdList = cfg.getGeneraterewardidlist();
        if (null == rewardIdList || rewardIdList.length <= 0) {
            return;
        }
        int index = RandomUtils.nextInt(rewardIdList.length);
        if (rewardIdList[index].length < 2) {
            return;
        }
        MistCrystalBox box = getRoom().getObjManager().createObj(MistUnitTypeEnum.MUT_CrystalBox_VALUE);
        box.setAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE, fighter.getId());
        box.setAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE, rewardIdList[index][0]);
        box.setAttribute(MistUnitPropTypeEnum.MUPT_ImageId_VALUE, rewardIdList[index][1]);
        box.afterInit(new int[]{getPos().getX(), getPos().getY()}, new int[]{getToward().getX(), getToward().getY()});
        box.setRebornTime(0);

        box.addCreateObjCmd();

        fighter.addSelfVisibleTarget(box.getId());
        fighter.getNpcTask().doNpcTask(MistTaskTargetType.MTTT_CatchGoblin_VALUE, 1, 0);
    }

    @Override
    public void sendSnapShotToPlayers(UnitSnapShot snapShot) {
        long visibleObjId = getAttribute(MistUnitPropTypeEnum.MUPT_VisiblePlayerUnitId_VALUE);
        if (visibleObjId <= 0) {
            return;
        }
        MistFighter fighter = room.getObjManager().getMistObj(visibleObjId);
        if (fighter == null) {
            return;
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null || player.isRobot() || !player.isOnline()) {
            return;
        }
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder cmdBuilder = BattleCmdData.newBuilder().setCMDType(MistBattleCmdEnum.MBC_SnapShotList);
        BattleCMD_SnapShotList.Builder snapShotBuilder = BattleCMD_SnapShotList.newBuilder();
        snapShotBuilder.addSnapShotList(snapShot);
        cmdBuilder.setCMDContent(snapShotBuilder.build().toByteString());
        builder.addCMDList(cmdBuilder);
        player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE,  builder);
    }
}
