package server.handler.activity.wishingWell;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity;
import protocol.Activity.SC_GetWishWellInfo;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;


/**
 * @Description 获取许愿池信息
 * @Author hanx
 * @Date2020/4/26 0026 20:02
 **/
@MsgId(msgId = MsgIdEnum.CS_GetWishWellInfo_VALUE)
public class GetWishingWellInfoHandler extends AbstractBaseHandler<Activity.CS_GetWishWellInfo> {
    @Override
    protected Activity.CS_GetWishWellInfo parse(byte[] bytes) throws Exception {
        return Activity.CS_GetWishWellInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_GetWishWellInfo req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        Activity.SC_GetWishingWellInfo.Builder result = Activity.SC_GetWishingWellInfo.newBuilder();
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (target == null || player == null || !player.functionUnLock(EnumFunction.WishingWell)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_GetWishWellInfo_VALUE, result);
            return;
        }

        target.sendWishingWellInfo();

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.WishingWell;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_GetWishWellInfo_VALUE, SC_GetWishWellInfo.newBuilder().setRetCode(retCode));
    }
}
