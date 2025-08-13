package server.handler.arena;

import cfg.ArenaDan;
import datatool.StringHelper;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import java.util.Collections;
import model.arena.ArenaManager;
import model.arena.ArenaPlayerManager;
import model.arena.util.ArenaUtil;
import protocol.ArenaDB.DB_ArenaPlayerInfo.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.CS_GS_RefreshArena;
import protocol.ServerTransfer.GS_CS_ArenaGm;
import util.JedisUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/05/20
 */
@MsgId(msgId = MsgIdEnum.GS_CS_ArenaGm_VALUE)
public class ArenaGmHandler extends AbstractHandler<GS_CS_ArenaGm> {
    @Override
    protected GS_CS_ArenaGm parse(byte[] bytes) throws Exception {
        return GS_CS_ArenaGm.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_ArenaGm req, int i) {
        LogUtil.debug("server.handler.arena.ArenaGmHandler.execute, receive gm:" + req.getGmParams());
        String[] split = req.getGmParams().split("\\|");
        if (split.length < 1) {
            return;
        }

        JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(req.getPlayerIdx()), () -> {
            Builder entity = ArenaPlayerManager.getInstance().getPlayerBaseInfoBuilder(req.getPlayerIdx());
            if (entity == null) {
                return false;
            }

            if ("arenaDan".equalsIgnoreCase(split[0])) {
                if (split.length < 2) {
                    return false;
                }
                int newDan = StringHelper.stringToInt(split[1], -1);
                if (ArenaDan.getById(newDan) == null || entity.getDan() == newDan) {
                    return false;
                }

                if (ArenaManager.getInstance().removePlayerFromRoom(entity.getRoomId(), Collections.singletonList(req.getPlayerIdx()))) {
                    ArenaManager.getInstance().submitPlayerDanUp(Collections.singletonList(req.getPlayerIdx()), newDan, true);
                }

                //发送直升消息到服务器
                //model.arena.ArenaManager.playerDanUpEvent
//                gsChn.send(MsgIdEnum.CS_GS_ArenaDirectUp_VALUE,
//                        CS_GS_ArenaDirectUp.newBuilder().setPlayerIdx(req.getPlayerIdx()).setNewDan(newDan));

            } else if ("arenaScore".equalsIgnoreCase(split[0])) {
                if (split.length < 2) {
                    return false;
                }
                int newScore = Math.max(StringHelper.stringToInt(split[1], 0), 0);
                entity.setScore(newScore);
                //更新玩家排行榜
                ArenaManager.getInstance().updatePlayerRanking(entity.build());

                //刷新消息回服务器
                CS_GS_RefreshArena.Builder resultBuilder = CS_GS_RefreshArena.newBuilder();
                resultBuilder.setPlayerIdx(req.getPlayerIdx());
                resultBuilder.setNewScore(entity.getScore());
                resultBuilder.setDirectUpKillCount(entity.getKillDirectUpCount());
                gsChn.send(MsgIdEnum.CS_GS_RefreshArena_VALUE, resultBuilder);
            }

            //更新玩家信息
            ArenaPlayerManager.getInstance().updatePlayerInfoToRedis(entity.build());
            return true;
        });
    }
}
