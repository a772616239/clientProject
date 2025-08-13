package server.handler.activity;


import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import model.FunctionManager;
import model.activity.ActivityManager;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ClaimActivity;
import protocol.Activity.ClientActivity;
import protocol.Activity.ClientActivity.Builder;
import protocol.Activity.ClientActivityNotice;
import protocol.Activity.SC_ClaimActivity;
import protocol.Common.EnumFunction;
import protocol.Common.LanguageEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PlayerInfo.SC_TotalAdsInfo;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimActivity_VALUE)
public class ClaimActivityHandler extends AbstractBaseHandler<CS_ClaimActivity> {

    @Override
    protected CS_ClaimActivity parse(byte[] bytes) throws Exception {
        return CS_ClaimActivity.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimActivity req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        SC_ClaimActivity.Builder resultBuilder = SC_ClaimActivity.newBuilder();
        resultBuilder.setIsResume(req.getIsResume());
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimActivity_VALUE, resultBuilder);
            return;
        }

        List<ClientActivity> totalActivity = new ArrayList<>();
        Collection<ClientActivity> allSpecialActivities = getSpecialActivity(playerIdx);
        if (CollectionUtils.isNotEmpty(allSpecialActivities)) {
            totalActivity.addAll(allSpecialActivities);
        }

        List<ClientActivityNotice> allActivityNotice =
                ActivityManager.getInstance().getAllActivityNotice(PlayerUtil.queryPlayerLanguage(playerIdx));

        SyncExecuteFunction.executeConsumer(target, entity -> {
            List<ClientActivity> activities = target.getActivities();
            if (activities != null) {
                totalActivity.addAll(activities);
            }

            List<ClientActivity> playerSpecialActivity = target.getPlayerSpecialActivity();
            if (!playerSpecialActivity.isEmpty()) {
                totalActivity.addAll(playerSpecialActivity);
            }
        });

        //过滤掉已经屏蔽的活动
        List<ClientActivity> filterActivities = totalActivity.stream()
                .filter(e -> FunctionManager.getInstance().activityIsOpen(e.getActivityType()))
                .collect(Collectors.toList());

        //拆分
        int splitSize = 10;
        List<List<ClientActivity>> activityLists = GameUtil.splitList(filterActivities, splitSize);
        List<List<ClientActivityNotice>> noticeLists = GameUtil.splitList(allActivityNotice, splitSize);

        int activityPageSize = CollectionUtils.size(activityLists);
        int noticePageSize = CollectionUtils.size(noticeLists);
        int pageSize = Math.max(activityPageSize, noticePageSize);
        if (pageSize <= 0) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimActivity_VALUE, resultBuilder);
        } else {
            resultBuilder.setTotalPage(pageSize);
            for (int j = 0; j < pageSize; j++) {
                //pageNum 从1开始
                resultBuilder.setPageNum(j + 1);

                resultBuilder.clearNotices();
                resultBuilder.clearActivitys();

                if (j < activityPageSize) {
                    resultBuilder.addAllActivitys(activityLists.get(j));
                }
                if (j < noticePageSize) {
                    resultBuilder.addAllNotices(noticeLists.get(j));
                }
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
                gsChn.send(MsgIdEnum.SC_ClaimActivity_VALUE, resultBuilder);
            }
        }

        sendOtherActivityInfo(target);
    }

    private void sendOtherActivityInfo(targetsystemEntity target) {
        target.sendTotalActivityGoodsInfo();
        target.sendTotalStageRewardInfo();
    }

    private List<ClientActivity> getSpecialActivity(String playerIdx) {
        List<ClientActivity> result = new ArrayList<>();

        //广告
        ClientActivity ads = getAdsActivity(playerIdx);
        if (ads != null) {
            result.add(ads);
        }

        LanguageEnum languageEnum = PlayerUtil.queryPlayerLanguage(playerIdx);
        Collection<ClientActivity> allSpecialActivities = ActivityManager.getInstance().getAllSpecialActivities();
        if (CollectionUtils.isNotEmpty(allSpecialActivities)) {
            //设置帮助字符串
            List<ClientActivity> collect = allSpecialActivities.stream()
                    .map(e -> {
                        Builder builder = e.toBuilder();
                        builder.setDetail(GameUtil.getLanguageStr(e.getDetail(), languageEnum));
                        return builder.build();
                    })
                    .collect(Collectors.toList());
            result.addAll(collect);
        }
        return result;
    }

    private ClientActivity getAdsActivity(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return null;
        }

        SC_TotalAdsInfo.Builder builder = player.buildAdsInfo();
        if (builder == null) {
            return null;
        }

        if (builder.getFreeGiftTimes() > 0
                || builder.getFreeWheelBonusTimes() > 0
                || builder.getWatchWheelBonusAdsTimes() > 0) {
            return ClientActivity.newBuilder().setActivityType(ActivityTypeEnum.ATE_Ads).build();
        }

        return null;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Activity;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimActivity_VALUE, SC_ClaimActivity.newBuilder().setRetCode(retCode));
    }
}
