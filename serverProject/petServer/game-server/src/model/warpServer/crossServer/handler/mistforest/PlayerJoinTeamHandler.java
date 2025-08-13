package model.warpServer.crossServer.handler.mistforest;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.LogService;
import platform.logs.entity.MistJoinTeamLog;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_JoinMistTeamLog;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;

@MsgId(msgId = MsgIdEnum.CS_GS_JoinMistTeamLog_VALUE)
public class PlayerJoinTeamHandler extends AbstractHandler<CS_GS_JoinMistTeamLog> {
    @Override
    protected CS_GS_JoinMistTeamLog parse(byte[] bytes) throws Exception {
        return CS_GS_JoinMistTeamLog.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_GS_JoinMistTeamLog req, int i) {
        playerEntity player = playerCache.getByIdx(req.getPlayerIdx());
        if (player == null) {
            return;
        }
        LogService.getInstance().submit(new MistJoinTeamLog(player.getIdx()));

        //目标：迷雾深林组成x次
        EventUtil.triggerUpdateTargetProgress(playerCache.getInstance().getIdxByUserId(req.getPlayerIdx()),
                TargetTypeEnum.TTE_Mist_CumuFormATeam, 1, 0);
    }
}
