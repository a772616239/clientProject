package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.stoneRift.StoneRiftUtil;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.DbStoneRiftFactory;
import model.stoneRift.stoneriftEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift;
import protocol.StoneRift.CS_ClaimStoneFactoryDetail;
import protocol.StoneRift.SC_ClaimStoneFactoryDetail;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_ClaimStoneFactoryDetail_VALUE;

@MsgId(msgId = MsgIdEnum.CS_ClaimStoneFactoryDetail_VALUE)
public class ClaimStoneRiftFactoryDetailHandler extends AbstractBaseHandler<CS_ClaimStoneFactoryDetail> {

    @Override
    protected CS_ClaimStoneFactoryDetail parse(byte[] bytes) throws Exception {
        return CS_ClaimStoneFactoryDetail.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimStoneFactoryDetail req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimStoneFactoryDetail.Builder msg = buildMsg(playerId, req.getId());

        GlobalData.getInstance().sendMsg(playerId, SC_ClaimStoneFactoryDetail_VALUE, msg);

    }

    private SC_ClaimStoneFactoryDetail.Builder buildMsg(String playerId, int factoryId) {
        SC_ClaimStoneFactoryDetail.Builder msg = SC_ClaimStoneFactoryDetail.newBuilder();

        stoneriftEntity stonerift = stoneriftCache.getByIdx(playerId);
        if (stonerift == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            return msg;
        }


        combineFactoryVo(stonerift, msg, factoryId);

        return msg;
    }

    private void combineFactoryVo(stoneriftEntity entity, SC_ClaimStoneFactoryDetail.Builder msg, int factoryId) {
        DbStoneRiftFactory factory = entity.getDB_Builder().getFactoryMap().get(factoryId);
        if (factory == null) {
            //矿未解锁
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            return;
        }
        msg.setStoneVo(StoneRiftUtil.toFactoryVo(entity, factoryId));

        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimStoneFactoryDetail_VALUE, StoneRift.SC_ClaimStoneFactoryDetail.newBuilder().setRetCode(retCode));

    }
}
