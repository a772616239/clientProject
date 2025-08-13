package server.handler.endlessSpire;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import protocol.Common.EnumFunction;
import protocol.EndlessSpire.CS_ClaimEndlessSpireInfo;
import protocol.EndlessSpire.SC_ClaimEndlessSpireInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_PlayerData;
import protocol.PlayerDB.EndlessSpireInfo;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimEndlessSpireInfo_VALUE)
public class ClaimEndlessSpireInfoHandler extends AbstractBaseHandler<CS_ClaimEndlessSpireInfo> {
    @Override
    protected CS_ClaimEndlessSpireInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimEndlessSpireInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimEndlessSpireInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimEndlessSpireInfo.Builder builder = SC_ClaimEndlessSpireInfo.newBuilder();
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimEndlessSpireInfo_VALUE, builder);
            return;
        }

        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.Endless)) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionIsLock));
            gsChn.send(MsgIdEnum.SC_ClaimEndlessSpireInfo_VALUE, builder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_PlayerData.Builder dbPlayerData = player.getDb_data();
            if (dbPlayerData == null) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ClaimEndlessSpireInfo_VALUE, builder);
                return;
            }
            EndlessSpireInfo endlessSpireInfo = dbPlayerData.getEndlessSpireInfo();
            builder.setMaxSpireLv(endlessSpireInfo.getMaxSpireLv());
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            builder.addAllAchiementInfo(endlessSpireInfo.getClaimedAchievementList());
            gsChn.send(MsgIdEnum.SC_ClaimEndlessSpireInfo_VALUE, builder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.NullFuntion;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimEndlessSpireInfo_VALUE, SC_ClaimEndlessSpireInfo.newBuilder().setRetCode(retCode));
    }
}
