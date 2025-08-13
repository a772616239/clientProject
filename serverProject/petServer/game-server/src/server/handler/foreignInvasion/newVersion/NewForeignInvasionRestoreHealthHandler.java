package server.handler.foreignInvasion.newVersion;

import cfg.NewForeignInvasionConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Map;
import java.util.stream.Collectors;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.foreignInvasion.dbCache.foreigninvasionCache;
import model.foreignInvasion.entity.foreigninvasionEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Battle.BattleRemainPet;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.NewForeignInvasion.CS_NewForeignInvasionRestoreHealth;
import protocol.NewForeignInvasion.SC_NewForeignInvasionRestoreHealth;
import protocol.NewForeignInvasionDB.DB_NewForeignInvasionPlayerInfo.Builder;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.11.10
 */
@MsgId(msgId = MsgIdEnum.CS_NewForeignInvasionRestoreHealth_VALUE)
public class NewForeignInvasionRestoreHealthHandler extends AbstractBaseHandler<CS_NewForeignInvasionRestoreHealth> {
    @Override
    protected CS_NewForeignInvasionRestoreHealth parse(byte[] bytes) throws Exception {
        return CS_NewForeignInvasionRestoreHealth.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_NewForeignInvasionRestoreHealth req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        foreigninvasionEntity entity = foreigninvasionCache.getInstance().getEntity(playerIdx);
        SC_NewForeignInvasionRestoreHealth.Builder resultBuilder = SC_NewForeignInvasionRestoreHealth.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_NewForeignInvasionRestoreHealth_VALUE, resultBuilder);
            return;
        }

        Consume consume = getRestoreConsume(entity.getDbBuilder().getRestoreTimes());
        if (consume == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ConfigError));
            gsChn.send(MsgIdEnum.SC_NewForeignInvasionRestoreHealth_VALUE, resultBuilder);
            return;
        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ForeignInvasion);
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, reason)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MsgIdEnum.SC_NewForeignInvasionRestoreHealth_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            Builder builder = entity.getDbBuilder();

            int restoreRate = getRestoreRate(builder.getRestoreTimes());
            if (restoreRate >= GameConst.PetMaxHpRate) {
                builder.clearPetsRemainHp();
            } else {
                Map<String, BattleRemainPet> newHpRate = builder.getPetsRemainHpMap().values().stream()
                        .map(ele -> {
                            BattleRemainPet.Builder petRemainBuilder = ele.toBuilder();
                            int newRate = Math.min(petRemainBuilder.getRemainHpRate() + restoreRate, GameConst.PetMaxHpRate);
                            petRemainBuilder.setRemainHpRate(newRate);
                            return petRemainBuilder.build();
                        })
                        .filter(ele -> ele.getRemainHpRate() < GameConst.PetMaxHpRate)
                        .collect(Collectors.toMap(BattleRemainPet::getPetId, ele -> ele));
                builder.clearPetsRemainHp();
                builder.putAllPetsRemainHp(newHpRate);
            }

            builder.setRestoreTimes(builder.getRestoreTimes() + 1);

            resultBuilder.setRestoreTimes(builder.getRestoreTimes());
            resultBuilder.addAllPetsRemainHp(builder.getPetsRemainHpMap().values());
        });
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_NewForeignInvasionRestoreHealth_VALUE, resultBuilder);
    }

    private Consume getRestoreConsume(int curRestoreTimes) {
        int[][] restoreConsume = NewForeignInvasionConfig.getById(GameConst.CONFIG_ID).getRestoreconsume();
        if (curRestoreTimes >= restoreConsume.length) {
            return ConsumeUtil.parseConsume(restoreConsume[restoreConsume.length - 1]);
        } else {
            return ConsumeUtil.parseConsume(restoreConsume[curRestoreTimes]);
        }
    }

    private int getRestoreRate(int curRestoreTimes) {
        int[] restoreRate = NewForeignInvasionConfig.getById(GameConst.CONFIG_ID).getRestorerate();
        if (curRestoreTimes >= restoreRate.length) {
            return restoreRate[restoreRate.length - 1];
        } else {
            return restoreRate[curRestoreTimes];
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.NewForeignInvasion;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_NewForeignInvasionRestoreHealth_VALUE, SC_NewForeignInvasionRestoreHealth.newBuilder().setRetCode(retCode));
    }
}
