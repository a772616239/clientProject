package server.handler.resRecycle;

import common.AbstractBaseHandler;
import common.GameConst;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerDB;
import protocol.ResourceCopy.SC_BuyTimes;
import protocol.ResourceRecycle;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimResourceRecycleInfo_VALUE)
public class ResourceCycleInfoHandler extends AbstractBaseHandler<ResourceRecycle.CS_ClaimResourceRecycleInfo> {
    @Override
    protected ResourceRecycle.CS_ClaimResourceRecycleInfo parse(byte[] bytes) throws Exception {
        return ResourceRecycle.CS_ClaimResourceRecycleInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, ResourceRecycle.CS_ClaimResourceRecycleInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            ResourceRecycle.SC_ClaimResourceRecycleInfo.Builder msg = ResourceRecycle.SC_ClaimResourceRecycleInfo.newBuilder();
            msg.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimResourceRecycleInfo_VALUE, msg);
            return;
        }
        player.sendResourceRecycleInfo();
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ResCopy;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_BuyTimes_VALUE, SC_BuyTimes.newBuilder().setRetCode(retCode));
    }
}
