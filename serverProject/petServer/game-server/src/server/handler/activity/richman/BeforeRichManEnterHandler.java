package server.handler.activity.richman;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ClaimRichManInfo;
import protocol.Activity.SC_ClaimRichManInfo;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB;
import util.GameUtil;

/**
 * 获取大富翁玩家部分信息
 */
@MsgId(msgId = MsgIdEnum.CS_BeforeRichManEnter_VALUE)
public class BeforeRichManEnterHandler extends AbstractBaseHandler<Activity.CS_BeforeRichManEnter> {
    @Override
    protected Activity.CS_BeforeRichManEnter parse(byte[] bytes) throws Exception {
        return Activity.CS_BeforeRichManEnter.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_BeforeRichManEnter req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity != null) {
            entity.sendBeforeRichManInfo();
        }
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
