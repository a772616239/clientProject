package server.handler.player;

import protocol.Common.EnumFunction;
import cfg.GameConfig;
import common.GameConst;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import platform.logs.ReasonManager;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_PlayerData;
import protocol.PlayerInfo.CS_AlterName;
import protocol.PlayerInfo.SC_AlterName;
import protocol.RetCodeId.RetCodeEnum;
import common.AbstractBaseHandler;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

@MsgId(msgId = MsgIdEnum.CS_AlterName_VALUE)
public class AlterNameHandler extends AbstractBaseHandler<CS_AlterName> {

    @Override
    protected CS_AlterName parse(byte[] bytes) throws Exception {
        return CS_AlterName.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_AlterName req, int codeNum) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_AlterName.Builder resultBuilder = SC_AlterName.newBuilder();

        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_AlterName_NotFoundPlayer));
            gsChn.send(MsgIdEnum.SC_AlterName_VALUE, resultBuilder);
            return;
        }

        String newName = req.getNewName();
        RetCodeEnum retCodeEnum = PlayerUtil.checkName(newName);
        if (retCodeEnum != RetCodeEnum.RCE_Success) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
            gsChn.send(MsgIdEnum.SC_AlterName_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, entity -> {
            DB_PlayerData.Builder db_dataBuilder = player.getDb_data();
            if (db_dataBuilder == null) {
                LogUtil.error("playerIdx[" + playerId + "] DB_PlayerData is null");
                return;
            }

            long curTime = GlobalTick.getInstance().getCurrentTime();

            if (db_dataBuilder.getNextRenameTime() > curTime) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_AlterName_DuringRenameCD));
                gsChn.send(MsgIdEnum.SC_AlterName_VALUE, resultBuilder);
                return;
            }

            if (!player.consumeCurrency(RewardTypeEnum.RTE_Diamond, GameConfig.getById(GameConst.CONFIG_ID).getRenameexpend(),
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_AlterName))) {

                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Player_DiamondNotEnought));
                gsChn.send(MsgIdEnum.SC_AlterName_VALUE, resultBuilder);
                return;
            }

            long nextRenameTime = curTime + TimeUtil.MS_IN_A_HOUR * GameConfig.getById(GameConst.CONFIG_ID).getRenamelimittime();
            db_dataBuilder.setNextRenameTime(nextRenameTime);
            player.setName(newName);

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setNextRenameTime(nextRenameTime);
            gsChn.send(MsgIdEnum.SC_AlterName_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
