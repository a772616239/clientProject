package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.stoneRift.StoneRiftUtil;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRiftAchievement;
import model.stoneRift.entity.DbStoneRiftFactory;
import model.stoneRift.stoneriftEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift.CS_ClaimStoneAchievement;
import protocol.StoneRift.SC_ClaimStoneAchievement;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_ClaimStoneAchievement_VALUE;

@MsgId(msgId = MsgIdEnum.CS_ClaimStoneAchievement_VALUE)
public class ClaimStoneAchievementHandler extends AbstractBaseHandler<CS_ClaimStoneAchievement> {

    @Override
    protected CS_ClaimStoneAchievement parse(byte[] bytes) throws Exception {
        return CS_ClaimStoneAchievement.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimStoneAchievement req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimStoneAchievement.Builder msg = buildMsg(playerId);

        GlobalData.getInstance().sendMsg(playerId, SC_ClaimStoneAchievement_VALUE, msg);

    }

    private SC_ClaimStoneAchievement.Builder buildMsg(String playerId) {
        SC_ClaimStoneAchievement.Builder msg = SC_ClaimStoneAchievement.newBuilder();

        stoneriftEntity stoneRift = stoneriftCache.getByIdx(playerId);
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (stoneRift == null || target == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            return msg;
        }
        msg.addAllMissions(target.getDb_Builder().getStoneRiftAchievementMap().values());
        DbStoneRiftAchievement achievement = stoneRift.getDB_Builder().getAchievement();
        msg.addAllClaimedIds(achievement.getClaimedIds());
        msg.addAllCompleteAchievementIds(achievement.getCompleteAchievementIds());
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        return msg;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimStoneAchievement_VALUE, SC_ClaimStoneAchievement.newBuilder().setRetCode(retCode));

    }
}
