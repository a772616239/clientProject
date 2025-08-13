package server.handler.activity.growthFoud;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.entity.PayActivityLog;
import platform.logs.entity.PayActivityLog.PayActivityEnum;
import protocol.Activity;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.RetCodeId;
import util.GameUtil;

/**
 * @Description 购买成长基金
 * @Author hanx
 * @Date2020/6/3 0003 15:08
 **/
@MsgId(msgId = MessageId.MsgIdEnum.CS_BuyGrowthFund_VALUE)
public class BuyGrowthFoundHandler extends AbstractBaseHandler<Activity.CS_BuyGrowthFund> {
    @Override
    protected Activity.CS_BuyGrowthFund parse(byte[] bytes) throws Exception {
        return Activity.CS_BuyGrowthFund.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_BuyGrowthFund req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Activity.SC_BuyGrowthFund.Builder result = Activity.SC_BuyGrowthFund.newBuilder();
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_BuyGrowthFund_VALUE, result);
            return;
        }
        if (target.getDb_Builder().getSpecialInfo().getGrowthFund().getBuy()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_RepeatedBuy));
            gsChn.send(MessageId.MsgIdEnum.SC_BuyGrowthFund_VALUE, result);
            return;
        }

        Common.Consume consume = ConsumeUtil.parseConsume(GameConfig.getById(GameConst.CONFIG_ID).getGrowthfundprice());
        //购买消耗
        if (!ConsumeManager.getInstance().consumeMaterial(playerIdx, consume, ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_GrowthFund))) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(MessageId.MsgIdEnum.SC_BuyGrowthFund_VALUE, result);
            return;
        }
        SyncExecuteFunction.executeConsumer(target, entity -> target.getDb_Builder().getSpecialInfoBuilder().getGrowthFundBuilder().setBuy(true));
        result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        if (consume != null && consume.getCount() > 0) {
            LogService.getInstance().submit(new PayActivityLog(playerIdx, consume, PayActivityEnum.GrowthFund));
        }
        gsChn.send(MessageId.MsgIdEnum.SC_BuyGrowthFund_VALUE, result);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
