package server.handler.gift;

import protocol.Common.EnumFunction;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.CS_GetAllTimeLimitGift_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_GetAllTimeLimitGift_VALUE;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_GetAllTimeLimitGift;
import protocol.TargetSystem.ClientTimeLimitGift;
import protocol.TargetSystem.SC_GetAllTimeLimitGift;
import protocol.TargetSystem.SC_GetAllTimeLimitGift.Builder;
import protocol.TargetSystemDB.DB_TimeLimitGiftItem;
import util.GameUtil;

import java.util.Map;

/**
 * @Description
 * @Author hanx
 * @Date2020/7/8 0008 10:27
 **/
@MsgId(msgId = CS_GetAllTimeLimitGift_VALUE)
public class GetAllTimeLimitGiftHandler extends AbstractBaseHandler<CS_GetAllTimeLimitGift> {
    @Override
    protected CS_GetAllTimeLimitGift parse(byte[] bytes) throws Exception {
        return CS_GetAllTimeLimitGift.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_GetAllTimeLimitGift req, int i) {
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        Builder result = SC_GetAllTimeLimitGift.newBuilder();
        if (target == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gameServerTcpChannel.send(SC_GetAllTimeLimitGift_VALUE, result);
            return;
        }
        Map<Integer, DB_TimeLimitGiftItem> giftsMap = target.getDb_Builder().getTimeLimitGiftInfo().getGiftsMap();
        for (DB_TimeLimitGiftItem value : giftsMap.values()) {
            if (!value.getBuy() && value.getLimitTime() > GlobalTick.getInstance().getCurrentTime()) {
                result.addGifts(ClientTimeLimitGift.newBuilder().setGiftId(value.getId()).setRemainTime(value.getLimitTime()));
            }
        }
        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));

        GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_GetAllTimeLimitGift_VALUE, result);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
