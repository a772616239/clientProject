package server.handler.mistforest;

import cfg.CrossArenaLvCfg;
import cfg.CrossArenaLvCfgObject;
import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.crossarena.CrossArenaManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.ReasonManager;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.CS_BuyMistStamina;
import protocol.MistForest.MistRetCode;
import protocol.MistForest.SC_BuyMistStamina;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_BuyMistStamina_VALUE)
public class BuyMistStaminaHandler extends AbstractBaseHandler<CS_BuyMistStamina> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_BuyMistStamina_VALUE, SC_BuyMistStamina.newBuilder().setRetCode(MistRetCode.MRC_AbnormalMaintenance));
    }

    @Override
    protected CS_BuyMistStamina parse(byte[] bytes) throws Exception {
        return CS_BuyMistStamina.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyMistStamina req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        SC_BuyMistStamina.Builder builder = SC_BuyMistStamina.newBuilder();
        MistRetCode retCode = SyncExecuteFunction.executeFunction(player, entity -> {
            int dailyBuyTimes = entity.getDb_data().getMistForestDataBuilder().getDailyButStaminaTimes();
            if (dailyBuyTimes > GameConfig.getById(GameConst.CONFIG_ID).getBuymiststaminamaxtimes()) {
                return MistRetCode.MRC_MaxDailyBuyStaminaTimes;
            }
            Consume consume = ConsumeUtil.parseConsume(GameConfig.getById(GameConst.CONFIG_ID).getBuymiststaminaconsume());
            if (consume != null) {
                Consume newConsume = consume;
                int crossVipLv = CrossArenaManager.getInstance().findPlayerGradeLv(playerId);
                CrossArenaLvCfgObject cfg = CrossArenaLvCfg.getByLv(crossVipLv);
                if (cfg != null) {
                    int newCount = consume.getCount() - consume.getCount() * cfg.getMist_buystaminaconsume() / 1000;
                    newCount = Math.max(0, newCount);
                    newConsume = Consume.newBuilder().mergeFrom(consume).setCount(newCount).build();
                }
                if (!ConsumeManager.getInstance().consumeMaterial(playerId, newConsume,
                        ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistForest))) {
                    return MistRetCode.MRC_StaminaNotEnough;
                }
            }
            entity.addMistStamina(GameConfig.getById(GameConst.CONFIG_ID).getBuymiststaminanum(), true);
            entity.getDb_data().getMistForestDataBuilder().setDailyButStaminaTimes(++dailyBuyTimes);

            EventUtil.triggerUpdateTargetProgress(entity.getIdx(), TargetTypeEnum.TTE_Mist_BuyMistStaminaTimes, 1, 0);
            return MistRetCode.MRC_Success;
        });
        builder.setRetCode(retCode);
        builder.setDailyBuyTimes(player.getDb_data().getMistForestData().getDailyButStaminaTimes());
        gsChn.send(MsgIdEnum.SC_BuyMistStamina_VALUE, builder);
    }
}
