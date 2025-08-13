package server.handler.activity.growthFoud;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

/**
 * @Description 获取成长基金信息
 * @Author hanx
 * @Date2020/6/3 0003 15:31
 **/
@MsgId(msgId = MessageId.MsgIdEnum.CS_GrowthFundInfo_VALUE)
public class GetGrowthFundInfoHandler extends AbstractBaseHandler<Activity.CS_GrowthFundInfo> {
    @Override
    protected Activity.CS_GrowthFundInfo parse(byte[] bytes) throws Exception {
        return Activity.CS_GrowthFundInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_GrowthFundInfo req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);

        if (target == null) {
            Activity.SC_GrowthFundInfo.Builder result = Activity.SC_GrowthFundInfo.newBuilder();
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_GetWishingWellInfo_VALUE, result);
            return;
        }
        target.sendGrowFundInfo();
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
