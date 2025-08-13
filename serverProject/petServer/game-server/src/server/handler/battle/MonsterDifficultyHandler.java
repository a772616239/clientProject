package server.handler.battle;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common.CS_MonsterDifficulty;
import protocol.Common.EnumFunction;
import protocol.Common.SC_MonsterDifficulty;
import protocol.Common.SC_MonsterDifficulty.Builder;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB.DB_MonsterDifficultyInfo;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/06/01
 */
@MsgId(msgId = MsgIdEnum.CS_MonsterDifficulty_VALUE)
public class MonsterDifficultyHandler extends AbstractBaseHandler<CS_MonsterDifficulty> {
    @Override
    protected CS_MonsterDifficulty parse(byte[] bytes) throws Exception {
        return CS_MonsterDifficulty.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MonsterDifficulty req, int i) {
        Builder resultBuilder = SC_MonsterDifficulty.newBuilder();
        resultBuilder.setFunction(req.getFunction());
        if (req.getFunction() != EnumFunction.Patrol
                && req.getFunction() != EnumFunction.ForeignInvasion) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_MonsterDifficulty_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_MonsterDifficulty_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(player, p -> {
            DB_MonsterDifficultyInfo.Builder diff = player.getMonsterDiffByFunction(req.getFunction());
            if (diff == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_MonsterDifficulty_VALUE, resultBuilder);
                return;
            }

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.setLevel(diff.getLevel());
            gsChn.send(MsgIdEnum.SC_MonsterDifficulty_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Battle;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        doAction(gsChn, codeNum);
    }
}
