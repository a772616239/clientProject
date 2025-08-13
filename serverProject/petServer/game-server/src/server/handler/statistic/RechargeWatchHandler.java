package server.handler.statistic;

import protocol.Common.EnumFunction;
import common.AbstractBaseHandler;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import platform.logs.LogService;
import platform.logs.entity.RechargeWatchLog;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Statistic.CS_RechargeWatch;
import protocol.Statistic.SC_RechargeWatch;
import protocol.Statistic.SC_RechargeWatch.Builder;
import util.GameUtil;
import util.TimeUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统计用户查看充值相关内容
 */
@MsgId(msgId = MsgIdEnum.CS_RechargeWatch_VALUE)
public class RechargeWatchHandler extends AbstractBaseHandler<CS_RechargeWatch> {

    private Map<String, Long> map = new ConcurrentHashMap<>();
    private static final long minUploadCycle = 9 * TimeUtil.MS_IN_A_MIN;

    @Override
    protected CS_RechargeWatch parse(byte[] bytes) throws Exception {
        return CS_RechargeWatch.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_RechargeWatch req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Builder result = SC_RechargeWatch.newBuilder();
        Long lastUploadTime = map.get(playerIdx);
        if (lastUploadTime != null && GlobalTick.getInstance().getCurrentTime() - lastUploadTime < minUploadCycle) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UploadTooFast));
            gsChn.send(MsgIdEnum.SC_RechargeWatch_VALUE, result);
        }
        LogService.getInstance().submit(new RechargeWatchLog(playerIdx, req.getType(), req.getName(), req.getNum()));
        map.put(playerIdx, GlobalTick.getInstance().getCurrentTime());
        result.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_RechargeWatch_VALUE, result);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
    }


 }
