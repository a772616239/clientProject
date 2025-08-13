package server.handler.drawCard;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.drawCard.DrawCardManager;
import model.drawCard.DrawPetRateBean;
import model.mainLine.dbCache.mainlineCache;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import protocol.Common.EnumFunction;
import protocol.DrawCard;
import protocol.DrawCard.CS_DrawRateInfo;
import protocol.DrawCard.SC_DrawRateInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;

import java.util.Map;

/**
 * 高级抽卡图鉴信息获取
 */
@MsgId(msgId = MsgIdEnum.CS_DrawRateInfo_VALUE)
public class DrawRateInfoHandler extends AbstractBaseHandler<CS_DrawRateInfo> {
    @Override
    protected CS_DrawRateInfo parse(byte[] bytes) throws Exception {
        return CS_DrawRateInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_DrawRateInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_DrawRateInfo.Builder resultBuilder = SC_DrawRateInfo.newBuilder();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            LogUtil.info("playerIdx [" + playerIdx + "] entity or itemBag is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DrawRateInfo_VALUE, resultBuilder);
            return;
        }
        if (!player.functionUnLock(EnumFunction.DrawCard)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_DrawRateInfo_VALUE, resultBuilder);
            return;
        }
        // 需要获取玩家当前关卡
        int playergk = mainlineCache.getInstance().getPlayerCurCheckPoint(playerIdx);
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerIdx);
        if (cache == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_DrawRateInfo_VALUE, resultBuilder);
            return;
        }
        for (Map.Entry<Integer, Map<Integer, DrawPetRateBean>> ent : DrawCardManager.getInstance().getTujianCfg().getCoreKeyCommon().entrySet()) {
            DrawCard.DrawRateInfo.Builder msg = DrawCard.DrawRateInfo.newBuilder();
            msg.setPetCore(ent.getKey());
            for (DrawPetRateBean ent1 : ent.getValue().values()) {
                DrawCard.DrawPetLimit.Builder msg2 = DrawCard.DrawPetLimit.newBuilder();
                msg2.setPetId(ent1.getPetId());
                if (cache.collectionContainsPet(ent1.getPetId())) {
                    msg2.setIsOwn(1);
                }
                if (ent1.getUnlocklimit() == 0) {
                    msg2.setLimit(0);
                } else {
                    if (ent1.getUnlocklimit() <= playergk) {
                        msg2.setLimit(-1);
                    } else {
                        msg2.setLimit(ent1.getUnlocklimit());
                    }
                }
                msg.addPetinfo(msg2);
            }
            resultBuilder.addInfos(msg);
        }
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_DrawRateInfo_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.DrawCard_AncientCall;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_DrawRateInfo_VALUE, SC_DrawRateInfo.newBuilder().setRetCode(retCode));
    }
}
