package server.handler.arena;

import hyzNet.GameServerTcpChannel;
import hyzNet.message.AbstractHandler;
import hyzNet.message.MsgId;
import model.arena.ArenaPlayerManager;
import model.arena.util.ArenaUtil;
import protocol.ArenaDB.DB_ArenaDefinedTeamsInfo;
import protocol.ArenaDB.DB_ArenaPlayerInfo.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.GS_CS_UpdateArenaTeamsInfo;
import util.JedisUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/05/18
 */
@MsgId(msgId = MsgIdEnum.GS_CS_UpdateArenaTeamsInfo_VALUE)
public class UpdateArenaTeamsInfoHandler extends AbstractHandler<GS_CS_UpdateArenaTeamsInfo> {
    @Override
    protected GS_CS_UpdateArenaTeamsInfo parse(byte[] bytes) throws Exception {
        return GS_CS_UpdateArenaTeamsInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, GS_CS_UpdateArenaTeamsInfo req, int i) {
        LogUtil.debug("UpdateArenaTeamsInfoHandler, playerIdx:" + req.getPlayerIdx() + ",receive team num:"
                + req.getTeamsInfo().getTeanNumValue() + ", pet info:" + req.getTeamsInfo().getPetsList().toString());


        JedisUtil.syncExecBooleanSupplier(ArenaUtil.getPlayerLockKey(req.getPlayerIdx()), () -> {
            //更新队伍
            DB_ArenaDefinedTeamsInfo.Builder teamsBuilder = ArenaPlayerManager.getInstance().getPlayerDefinedTeamsInfoBuilder(req.getPlayerIdx());
            if (teamsBuilder == null) {
                teamsBuilder = DB_ArenaDefinedTeamsInfo.newBuilder().setPlayerIdx(req.getPlayerIdx());
            }

            if (req.hasTeamsInfo()) {
                teamsBuilder.putDefinedTeams(req.getTeamsInfo().getTeanNumValue(), req.getTeamsInfo());
            }
            ArenaPlayerManager.getInstance().updatePlayerDefinedTeamsInfoToRedis(teamsBuilder.build());


            //更新战力,计算所有队伍的战力
            Builder infoBuilder = ArenaPlayerManager.getInstance().getPlayerBaseInfoBuilder(req.getPlayerIdx());
            if (infoBuilder == null) {
                return false;
            }
            infoBuilder.getBaseInfoBuilder().setFightAbility(ArenaUtil.calculateTotalAbility(teamsBuilder.getDefinedTeamsMap().values()));
            ArenaPlayerManager.getInstance().updatePlayerInfoToRedis(infoBuilder.build());
            return true;
        });
    }
}
