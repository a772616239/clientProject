package server.handler.stoneRift;

import common.AbstractBaseHandler;
import common.GlobalData;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;

import model.stoneRift.StoneRiftManager;
import model.stoneRift.StoneRiftUtil;
import model.stoneRift.entity.StoneRiftMsg;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift.CS_ClaimStoneRiftMsg;
import protocol.StoneRift.SC_ClaimStoneRiftMsg;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_ClaimStoneRiftMsg_VALUE;

@MsgId(msgId = MsgIdEnum.CS_ClaimStoneRiftMsg_VALUE)
public class ClaimStoneRiftMsgHandler extends AbstractBaseHandler<CS_ClaimStoneRiftMsg> {

    @Override
    protected CS_ClaimStoneRiftMsg parse(byte[] bytes) throws Exception {
        return CS_ClaimStoneRiftMsg.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimStoneRiftMsg req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimStoneRiftMsg.Builder msg = claimStoneRiftMsg(playerId, req);
        GlobalData.getInstance().sendMsg(playerId, SC_ClaimStoneRiftMsg_VALUE, msg);

    }

    private SC_ClaimStoneRiftMsg.Builder claimStoneRiftMsg(String playerId, CS_ClaimStoneRiftMsg req) {
        SC_ClaimStoneRiftMsg.Builder msg = SC_ClaimStoneRiftMsg.newBuilder();

        List<StoneRiftMsg> stoneRiftMsgs = StoneRiftManager.getInstance().claimStoneRiftMsg(playerId, req.getPage());

        for (StoneRiftMsg stoneRiftMsg : stoneRiftMsgs) {
            msg.addMsg(StoneRiftUtil.toVo(stoneRiftMsg));
        }
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
        gsChn.send(SC_ClaimStoneRiftMsg_VALUE, SC_ClaimStoneRiftMsg.newBuilder().setRetCode(retCode));

    }
}
