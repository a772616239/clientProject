package server.handler.stoneRift;

import com.hyz.platform.sdk.utils.sensi.SensiWordsUtils;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.player.util.PlayerUtil;
import model.stoneRift.StoneRiftManager;
import model.stoneRift.entity.StoneRiftMsg;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift.CS_AddStoneRiftMsg;
import protocol.StoneRift.SC_AddStoneRiftMsg;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_AddStoneRiftMsg_VALUE;

/**
 * 给其他玩家留言
 */
@MsgId(msgId = MsgIdEnum.CS_AddStoneRiftMsg_VALUE)
public class AddStoneRiftMsgHandler extends AbstractBaseHandler<CS_AddStoneRiftMsg> {

    @Override
    protected CS_AddStoneRiftMsg parse(byte[] bytes) throws Exception {
        return CS_AddStoneRiftMsg.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_AddStoneRiftMsg req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_AddStoneRiftMsg.Builder msg = addStoneRiftMsg(playerId, req);
        GlobalData.getInstance().sendMsg(playerId, SC_AddStoneRiftMsg_VALUE, msg);

    }

    private SC_AddStoneRiftMsg.Builder addStoneRiftMsg(String playerId, CS_AddStoneRiftMsg req) {
        SC_AddStoneRiftMsg.Builder msg = SC_AddStoneRiftMsg.newBuilder();
        if (playerId.equals(req.getSendPlayerIdx())) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            return msg;
        }

        if (!SensiWordsUtils.isLegal(req.getMsg())) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_InvalidWord));
            return msg;
        }

        String finalMsg = SensiWordsUtils.filter(req.getMsg());

        StoneRiftMsg stoneRiftMsg = new StoneRiftMsg();
        stoneRiftMsg.setMsg(finalMsg);
        stoneRiftMsg.setCreateTime(GlobalTick.getInstance().getCurrentTime());
        stoneRiftMsg.setPlayerIdx(playerId);
        stoneRiftMsg.setName(PlayerUtil.queryPlayerName(playerId));
        StoneRiftManager.getInstance().addPlayerMsg(req.getSendPlayerIdx(), stoneRiftMsg);
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        msg.setMsg(finalMsg);
        return msg;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_AddStoneRiftMsg_VALUE, SC_AddStoneRiftMsg.newBuilder().setRetCode(retCode));

    }
}
