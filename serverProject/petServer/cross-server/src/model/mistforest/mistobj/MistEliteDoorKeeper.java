package model.mistforest.mistobj;

import java.util.HashSet;
import java.util.Set;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.cache.MistPlayerCache;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.BattleCMD_UpdateComplexProperty;
import protocol.MistForest.BattleCmdData;
import protocol.MistForest.ComplexPropTypeEnum;
import protocol.MistForest.ComplexPropertyValues;
import protocol.MistForest.EnumUpdateComplexPropType;
import protocol.MistForest.MistBattleCmdEnum;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.SC_BattleCmd;
import protocol.MistForest.UnitMetadata;
import protocol.ServerTransfer.EnumMistPveBattleType;
import util.GameUtil;

public class MistEliteDoorKeeper extends MistObject {
    protected Set<Long> defeatedPlayerSet;

    public MistEliteDoorKeeper(MistRoom room, int objType) {
        super(room, objType);
        defeatedPlayerSet = new HashSet<>();
    }

    @Override
    public void clear() {
        super.clear();
        defeatedPlayerSet.clear();
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata.Builder metaData = UnitMetadata.newBuilder();
        metaData.mergeFrom(super.getMetaData(fighter));
        if (fighter != null && !defeatedPlayerSet.isEmpty()) {
            long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
            if (defeatedPlayerSet.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE))) {
                metaData.addComplexKeys(ComplexPropTypeEnum.CPTE_BeatMonsterPlayerList);
                ComplexPropertyValues.Builder builder = ComplexPropertyValues.newBuilder();
                builder.addValues(playerId);
                metaData.addComplexValues(builder);
            }
        }
        return metaData.build();
    }

    @Override
    public void beTouch(MistFighter toucher) {
        long playerId = toucher.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        if (defeatedPlayerSet.contains(playerId)) {
            return;
        }
        toucher.enterPveBattle(EnumMistPveBattleType.EMPBT_EliteMonsterBattle_VALUE, this);
    }

    public void settleDamage(MistFighter fighter, boolean isWinner) {
        if (!isAlive()) {
            return;
        }
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        if (isWinner) {
            defeatedPlayerSet.add(playerId);
            MistPlayer player = MistPlayerCache.getInstance().queryObject(GameUtil.longToString(playerId, ""));
            if (player != null && player.isOnline()) {
                addComplexPropertyCmd(player);
            }
        }
    }

    protected void addComplexPropertyCmd(MistPlayer player) {
        SC_BattleCmd.Builder builder = SC_BattleCmd.newBuilder();
        BattleCmdData.Builder batCmdBuilder = BattleCmdData.newBuilder();
        batCmdBuilder.setCMDType(MistBattleCmdEnum.MBC_UpdateComplexProperty);
        BattleCMD_UpdateComplexProperty.Builder cmdBuilder = BattleCMD_UpdateComplexProperty.newBuilder();
        cmdBuilder.setTargetUnitID(getId());
        cmdBuilder.setUpdateType(EnumUpdateComplexPropType.EUCPT_Add);
        cmdBuilder.addComplexKeys(ComplexPropTypeEnum.CPTE_BeatMonsterPlayerList);

        ComplexPropertyValues.Builder valBuilder = ComplexPropertyValues.newBuilder();
        valBuilder.addValues(GameUtil.stringToLong(player.getIdx(), 0));
        cmdBuilder.addComplexValues(valBuilder);

        batCmdBuilder.setCMDContent(cmdBuilder.build().toByteString());
        builder.addCMDList(batCmdBuilder);
        player.sendMsgToServer(MsgIdEnum.SC_BattleCmd_VALUE,  builder);
    }
}
