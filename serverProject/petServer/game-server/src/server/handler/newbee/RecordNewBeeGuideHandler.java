package server.handler.newbee;

import protocol.Common.EnumFunction;
import common.AbstractBaseHandler;
import common.HttpRequestUtil;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.LogService;
import platform.logs.entity.NewBeeLog;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.CS_RecordNewBeeGuide;
import protocol.PlayerInfo.SC_RecordNewBeeGuide;
import protocol.PlayerInfo.SC_RecordNewBeeGuide.Builder;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/03/24
 */
@MsgId(msgId = MsgIdEnum.CS_RecordNewBeeGuide_VALUE)
public class RecordNewBeeGuideHandler extends AbstractBaseHandler<CS_RecordNewBeeGuide> {
    @Override
    protected CS_RecordNewBeeGuide parse(byte[] bytes) throws Exception {
        return CS_RecordNewBeeGuide.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RecordNewBeeGuide req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);

        Builder resultBuilder = SC_RecordNewBeeGuide.newBuilder();
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_RecordNewBeeGuide_VALUE, resultBuilder);
            return;
        }

        if (!player.getDb_data().getNewBeeInfo().getRecordNewBeeStepList().contains(req.getGuideIndex())) {
            LogService.getInstance().submit(new NewBeeLog(String.valueOf(gsChn.getPlayerId1()), req.getGuideIndex()));

            SyncExecuteFunction.executeConsumer(player, p -> {
                player.getDb_data().getNewBeeInfoBuilder().addRecordNewBeeStep(req.getGuideIndex());
            });

            //玩家appsflyer等级更新
            HttpRequestUtil.platformAppsflyerTutorial(player, req.getGuideIndex());
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_RecordNewBeeGuide_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
