package server.handler.activity.zeroCostPurchase;

import cfg.TimeLimitActivity;
import cfg.TimeLimitActivityObject;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Map;
import model.activity.ActivityUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import protocol.Activity;
import protocol.Activity.SC_ZeroCostPurchaseInfo;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_ZeroCostPurchaseInfo_VALUE)
public class ClaimZeroCostPurchaseInfoHandler extends AbstractBaseHandler<Activity.CS_ZeroCostPurchaseInfo> {
    @Override
    protected Activity.CS_ZeroCostPurchaseInfo parse(byte[] bytes) throws Exception {
        return Activity.CS_ZeroCostPurchaseInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ZeroCostPurchaseInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        Activity.SC_ZeroCostPurchaseInfo.Builder result = Activity.SC_ZeroCostPurchaseInfo.newBuilder();
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_BuyZeroCostPurchase_VALUE, result);
            return;
        }
        TimeLimitActivityObject activityConfig = TimeLimitActivity.getById(ActivityUtil.LocalActivityId.ZeroCostPurchase);
        if (activityConfig == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_ConfigNotExist));
            gsChn.send(MessageId.MsgIdEnum.SC_BuyZeroCostPurchase_VALUE, result);
            return;
        }
        Map<Integer, Activity.ZeroCostPurchaseItem> zeroCostPurchaseMap = target.getDb_Builder().getSpecialInfo().getZeroCostPurchase().getZeroCostPurchaseMap();
        result.addAllPurchase(zeroCostPurchaseMap.values());

        result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_ZeroCostPurchaseInfo_VALUE, result);

    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ZeroCostPurchase;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ZeroCostPurchaseInfo_VALUE, SC_ZeroCostPurchaseInfo.newBuilder().setRetCode(retCode));
    }
}