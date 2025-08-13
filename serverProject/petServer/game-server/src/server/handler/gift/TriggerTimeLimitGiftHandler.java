package server.handler.gift;

import protocol.Common.EnumFunction;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import static protocol.MessageId.MsgIdEnum.CS_TriggerTimeLimitGift_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_TriggerTimeLimitGift_VALUE;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_TriggerTimeLimitGift;
import protocol.TargetSystem.SC_TriggerTimeLimitGift;
import protocol.TargetSystem.SC_TriggerTimeLimitGift.Builder;
import protocol.TargetSystem.TimeLimitGiftType;
import util.EventUtil;
import util.GameUtil;

/**
 * @Description
 * @Author hanx
 * @Date2020/7/8 0008 10:27
 **/
@MsgId(msgId = CS_TriggerTimeLimitGift_VALUE)
public class TriggerTimeLimitGiftHandler extends AbstractBaseHandler<CS_TriggerTimeLimitGift> {
    @Override
    protected CS_TriggerTimeLimitGift parse(byte[] bytes) throws Exception {
        return CS_TriggerTimeLimitGift.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_TriggerTimeLimitGift req, int i) {
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        Builder result = SC_TriggerTimeLimitGift.newBuilder();

        if (req.getGiftType() != TimeLimitGiftType.TLG_AncientCall) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gameServerTcpChannel.send(SC_TriggerTimeLimitGift_VALUE, result);
            return;
        }
        EventUtil.triggerTimeLimitGift(playerId, req.getGiftType(), 0);
        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gameServerTcpChannel.send(SC_TriggerTimeLimitGift_VALUE, result);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
