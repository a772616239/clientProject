package server.handler.gift;

import protocol.Common.EnumFunction;
import cfg.NewBeeGiftCfg;
import cfg.NewBeeGiftCfgObject;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity.CS_ClaimNewBeeGift;
import protocol.Activity.NeeBeeGift;
import protocol.Activity.SC_ClaimNewBeeGift;
import protocol.Activity.SC_ClaimNewBeeGift.Builder;
import protocol.MessageId.MsgIdEnum;
import static protocol.MessageId.MsgIdEnum.CS_ClaimNewBeeGift_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_ClaimNewBeeGift_VALUE;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystemDB.DB_NeeBeeGift;
import util.GameUtil;

import java.util.Map;
import java.util.Map.Entry;

/**
 * @Description
 * @Author hanx
 * @Date2020/7/8 0008 10:27
 **/
@MsgId(msgId = CS_ClaimNewBeeGift_VALUE)
public class GetNeeBeeGiftHandler extends AbstractBaseHandler<CS_ClaimNewBeeGift> {
    @Override
    protected CS_ClaimNewBeeGift parse(byte[] bytes) throws Exception {
        return CS_ClaimNewBeeGift.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_ClaimNewBeeGift req, int i) {
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        Builder result = SC_ClaimNewBeeGift.newBuilder();
        if (target == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gameServerTcpChannel.send(SC_ClaimNewBeeGift_VALUE, result);
            return;
        }
        DB_NeeBeeGift newBeeGift = target.getDb_Builder().getSpecialInfo().getNewBeeGift();
        if (newBeeGift.getEndTime() < GlobalTick.getInstance().getCurrentTime()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_MissionOutOfTime));
            gameServerTcpChannel.send(SC_ClaimNewBeeGift_VALUE, result);
            return;
        }

        Map<Integer, Integer> giftsMap = newBeeGift.getGiftsMap();
        for (Entry<Integer, Integer> entry : giftsMap.entrySet()) {
            NewBeeGiftCfgObject giftConfig = NewBeeGiftCfg.getById(entry.getKey());
            if (giftConfig != null) {
                boolean soldOut = giftConfig.getLimit() <= entry.getValue();
                result.addGiftInfo(NeeBeeGift.newBuilder().setGiftId(entry.getKey()).setSoldOut(soldOut).build());
            }
        }
        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));

        GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_ClaimNewBeeGift_VALUE, result);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
