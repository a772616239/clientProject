package server.handler.arena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.time.Instant;
import java.util.List;
import model.arena.ArenaManager;
import model.arena.ArenaPlayerManager;
import model.arena.util.ArenaUtil;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Arena.ArenaOpponentTotalInfo;
import protocol.ArenaDB.DB_ArenaPlayerInfo;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.ServerTransfer.CS_GS_RefreshOpponent;
import protocol.ServerTransfer.CS_GS_RefreshOpponent.Builder;
import protocol.ServerTransfer.GS_CS_RefreshOpponent;
import util.GameUtil;
import util.JedisUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/05/13
 */
@MsgId(msgId = MsgIdEnum.GS_CS_RefreshOpponent_VALUE)
public class RefreshOpponentHandler extends AbstractHandler<GS_CS_RefreshOpponent> {
    @Override
    protected GS_CS_RefreshOpponent parse(byte[] bytes) throws Exception {
        return GS_CS_RefreshOpponent.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_RefreshOpponent req, int i) {
        DB_ArenaPlayerInfo entity = ArenaPlayerManager.getInstance().getPlayerBaseInfo(req.getPlayerIdx());

        Builder resultBuilder = CS_GS_RefreshOpponent.newBuilder();
        resultBuilder.setPlayerIdx(req.getPlayerIdx());

        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.CS_GS_RefreshOpponent_VALUE, resultBuilder);
            return;
        }

        long startRandom = Instant.now().toEpochMilli();
        List<ArenaOpponentTotalInfo> opponents = ArenaManager.getInstance().randomOpponent(entity.getRoomId(), req.getPlayerIdx());
        LogUtil.debug("======random player opponent use time:" + (Instant.now().toEpochMilli() - startRandom));
        if (CollectionUtils.isEmpty(opponents)) {
            LogUtil.error("can not random player opponent, playerIdx:" + req.getPlayerIdx() + ", roomId:" + entity.getRoomId());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.CS_GS_RefreshOpponent_VALUE, resultBuilder);
            return;
        }

        //构建指定玩家信息
        List<Integer> definedTeams = ArenaUtil.getDanUseDefinedTeams(entity.getDan());
        if (req.getSpecifyIdxCount() > 0) {
            for (String specifyIdx : req.getSpecifyIdxList()) {
                ArenaOpponentTotalInfo totalInfo =
                        ArenaPlayerManager.getInstance().buildArenaOpponentTotalInfo(specifyIdx, false, definedTeams);

                if (totalInfo != null) {
                    resultBuilder.putSpecifyOpponent(totalInfo.getOpponnentInfo().getSimpleInfo().getPlayerIdx(), totalInfo);
                }
            }
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.addAllOpponent(opponents);
        gsChn.send(MsgIdEnum.CS_GS_RefreshOpponent_VALUE, resultBuilder);
    }
}
