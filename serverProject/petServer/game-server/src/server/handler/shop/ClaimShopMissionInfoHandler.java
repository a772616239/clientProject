package server.handler.shop;

import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Common;
import protocol.MessageId;
import protocol.Shop;

import static protocol.MessageId.MsgIdEnum.SC_ShopMissionInfo_VALUE;

@MsgId(msgId = MessageId.MsgIdEnum.CS_ShopMissionInfo_VALUE)
public class ClaimShopMissionInfoHandler extends AbstractBaseHandler<Shop.CS_ShopMissionInfo> {
    @Override
    protected Shop.CS_ShopMissionInfo parse(byte[] bytes) throws Exception {
        return Shop.CS_ShopMissionInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Shop.CS_ShopMissionInfo req, int i) {
        Shop.SC_ShopMissionInfo.Builder msg = Shop.SC_ShopMissionInfo.newBuilder();
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            gsChn.send(SC_ShopMissionInfo_VALUE, msg);
            return;
        }
        msg.addAllCompleteMissionId(target.getCompleteMissionIds());
        gsChn.send(SC_ShopMissionInfo_VALUE, msg);
    }

    @Override
    public Common.EnumFunction belongFunction() {
        return Common.EnumFunction.Shop;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(SC_ShopMissionInfo_VALUE, Shop.SC_ShopMissionInfo.newBuilder());
    }
}

