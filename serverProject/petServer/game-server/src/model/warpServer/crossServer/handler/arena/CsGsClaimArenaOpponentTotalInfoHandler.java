package model.warpServer.crossServer.handler.arena;

import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.arena.ArenaUtil;
import model.arena.dbCache.arenaCache;
import model.arena.entity.arenaEntity;
import model.player.util.PlayerUtil;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.Arena.SC_ClaimArenaOpponentTotalInfo;
import protocol.Arena.SC_ClaimArenaOpponentTotalInfo.Builder;
import protocol.Common.LanguageEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_ClaimArenaOpponentTotalInfo;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/05/19
 */
@MsgId(msgId = MsgIdEnum.CS_GS_ClaimArenaOpponentTotalInfo_VALUE)
public class CsGsClaimArenaOpponentTotalInfoHandler extends AbstractHandler<CS_GS_ClaimArenaOpponentTotalInfo> {

    @Override
    protected CS_GS_ClaimArenaOpponentTotalInfo parse(byte[] bytes) throws Exception {
        return CS_GS_ClaimArenaOpponentTotalInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gaChn, CS_GS_ClaimArenaOpponentTotalInfo req, int i) {
        Builder resultBuilder = SC_ClaimArenaOpponentTotalInfo.newBuilder();

        if(req.getRetCode().getRetCode() != RetCodeEnum.RCE_Success) {
            resultBuilder.setRetCode(req.getRetCode());
            GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_ClaimArenaOpponentTotalInfo_VALUE, resultBuilder);
            return;
        }

        arenaEntity entity = arenaCache.getInstance().getEntity(req.getPlayerIdx());
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_ClaimArenaOpponentTotalInfo_VALUE, resultBuilder);
            return;
        }


        ArenaOpponentTotalInfo tempInfo = req.getTotalInfo();
        LanguageEnum language = PlayerUtil.queryPlayerLanguage(req.getPlayerIdx());
        if (language != LanguageEnum.LE_SimpleChinese) {
            ArenaOpponentTotalInfo newInfo = ArenaUtil.dealRobotName(req.getTotalInfo(), language);
            if (newInfo != null) {
                tempInfo = newInfo;
            }
        }

        final ArenaOpponentTotalInfo temp = tempInfo;
        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.addTempOpponent(temp);
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setTotalInfo(tempInfo);
        GlobalData.getInstance().sendMsg(req.getPlayerIdx(), MsgIdEnum.SC_ClaimArenaOpponentTotalInfo_VALUE, resultBuilder);
    }
}
