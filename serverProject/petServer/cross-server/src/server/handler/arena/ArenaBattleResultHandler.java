package server.handler.arena;

import cfg.ArenaDan;
import cfg.ArenaDanObject;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.Collections;
import model.arena.ArenaManager;
import model.arena.ArenaPlayerManager;
import model.arena.util.ArenaUtil;
import org.apache.commons.lang.StringUtils;
import protocol.ArenaDB.DB_ArenaPlayerInfo;
import protocol.ArenaDB.DB_ArenaPlayerInfo.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_RefreshArena;
import protocol.ServerTransfer.GS_CS_ArenaBattleResult;
import util.JedisUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/05/15
 */
@MsgId(msgId = MsgIdEnum.GS_CS_ArenaBattleResult_VALUE)
public class ArenaBattleResultHandler extends AbstractHandler<GS_CS_ArenaBattleResult> {
    @Override
    protected GS_CS_ArenaBattleResult parse(byte[] bytes) throws Exception {
        return GS_CS_ArenaBattleResult.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ArenaBattleResult req, int i) {
        //处理对手信息
        if (!doOpponent(req)) {
            LogUtil.error("ArenaBattleResultHandler.execute, do opponent failed");
        }

        JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(req.getPlayerIdx()), () -> {
            LogUtil.info("ArenaBattleResultHandler, start settle battle result, req:" + req.toString());
            DB_ArenaPlayerInfo.Builder playerInfo = ArenaPlayerManager.getInstance().getPlayerBaseInfoBuilder(req.getPlayerIdx());
            if (playerInfo == null) {
                LogUtil.info("ArenaBattleResultHandler, player entity is not exist, playerIdx:" + req.getPlayerIdx());
                return false;
            }

            //设置直升
            if (req.getPlayerWin()) {
                if (req.getIsDirectUp()) {
                    playerInfo.setKillDirectUpCount(playerInfo.getKillDirectUpCount() + 1);
                }
            } else {
                playerInfo.clearKillDirectUpCount();
            }
            playerInfo.setBattleCount(playerInfo.getBattleCount() + 1);
            //积分变化
            playerInfo.setScore(Math.max(playerInfo.getScore() + req.getScoreChange(), 0));
            //更新玩家信息
            ArenaPlayerManager.getInstance().updatePlayerInfoToRedis(playerInfo.build());

            //判断玩家是否直升
            ArenaDanObject danCfg = ArenaDan.getById(playerInfo.getDan());
            if (danCfg != null && danCfg.getUpgradedirectcount() != -1
                    && playerInfo.getKillDirectUpCount() >= danCfg.getUpgradedirectcount()) {

                LogUtil.info("server.handler.arena.ArenaBattleResultHandler.execute, player direct dan up:" + playerInfo.getBaseInfo().getPlayerIdx());
                if (ArenaManager.getInstance().removePlayerFromRoom(playerInfo.getRoomId(), Collections.singletonList(playerInfo.getBaseInfo().getPlayerIdx()))) {
                    ArenaManager.getInstance().submitPlayerDanUp(Collections.singletonList(playerInfo.getBaseInfo().getPlayerIdx()), danCfg.getNextdan(), true);
                }

                //发送直升消息到服务器
                //放置model.arena.ArenaManager.playerDanUpEvent
//                gsChn.send(MsgIdEnum.CS_GS_ArenaDirectUp_VALUE,
//                        CS_GS_ArenaDirectUp.newBuilder().setPlayerIdx(req.getPlayerIdx()).setNewDan(danCfg.getNextdan()));
            } else {
                ArenaManager.getInstance().updatePlayerRanking(playerInfo.build());
            }

            //刷新消息回服务器
            CS_GS_RefreshArena.Builder resultBuilder = CS_GS_RefreshArena.newBuilder();
            resultBuilder.setPlayerIdx(req.getPlayerIdx());
            resultBuilder.setNewScore(playerInfo.getScore());
            resultBuilder.setDirectUpKillCount(playerInfo.getKillDirectUpCount());
            gsChn.send(MsgIdEnum.CS_GS_RefreshArena_VALUE, resultBuilder);

            LogUtil.info("ArenaBattleResultHandler, settle battle result finished, playerIdx:" + req.getPlayerIdx());
            return true;
        });
    }

    private boolean doOpponent(GS_CS_ArenaBattleResult req) {
        return JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(req.getOpponentIdx()), () -> {
            Builder builder = ArenaPlayerManager.getInstance().getPlayerBaseInfoBuilder(req.getOpponentIdx());
            builder.setScore(Math.max(builder.getScore() + req.getOpponentRecord().getScoreChange(), 0));

            DB_ArenaPlayerInfo build = builder.build();
            //更新玩家数据
            ArenaPlayerManager.getInstance().updatePlayerInfoToRedis(build);
            //更新排行
            ArenaManager.getInstance().updatePlayerRanking(build);

            //机器人不用转发战斗记录
            if (ArenaUtil.isRobot(build)) {
                return true;
            }
            //转发战斗记录
            int opponentSvrIndex = builder.getLastLoginSIndex();
            if (opponentSvrIndex <= 0) { // 兼容代码
                opponentSvrIndex = GlobalData.getInstance().getServerIndexByIp(builder.getLastLoginIp());
            }
            if (opponentSvrIndex <= 0) {
                LogUtil.error("ArenaBattleResultHandler.doOpponent, player idx:" + build.getBaseInfo().getPlayerIdx()
                        + ", last logIn ip is null, can not send battle record");
                return false;
            }

            CS_GS_RefreshArena.Builder recordBuilder = CS_GS_RefreshArena.newBuilder();
            recordBuilder.setPlayerIdx(req.getOpponentIdx());
            recordBuilder.setRecord(req.getOpponentRecord());
            recordBuilder.setNewScore(builder.getScore());
            recordBuilder.setDirectUpKillCount(builder.getKillDirectUpCount());
            GlobalData.getInstance().sendMsgToServer(opponentSvrIndex, MsgIdEnum.CS_GS_RefreshArena_VALUE, recordBuilder);
            return true;
        });
    }
}

