package server.handler.bossTower;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.bosstower.dbCache.bosstowerCache;
import model.bosstower.entity.bosstowerEntity;
import model.player.util.PlayerUtil;
import protocol.BossTower.BossTowerPassCondition;
import protocol.BossTower.BossTowerPassCondition.Builder;
import protocol.BossTower.CS_ClaimBossTowerInfo;
import protocol.BossTower.PassInfo;
import protocol.BossTower.SC_ClaimBossTowerInfo;
import protocol.BossTowerDB.DB_BossTowerPassCondition;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/06/28
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimBossTowerInfo_VALUE)
public class ClaimBossTowerInfoHandler extends AbstractBaseHandler<CS_ClaimBossTowerInfo> {
    @Override
    protected CS_ClaimBossTowerInfo parse(byte[] bytes) throws Exception {
        return CS_ClaimBossTowerInfo.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimBossTowerInfo req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimBossTowerInfo.Builder resultBuilder = SC_ClaimBossTowerInfo.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.BossTower)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_FunctionNotUnLock));
            gsChn.send(MsgIdEnum.SC_ClaimBossTowerInfo_VALUE, resultBuilder);
            return;
        }

        bosstowerEntity entity = bosstowerCache.getInstance().getEntity(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimBossTowerInfo_VALUE, resultBuilder);
            return;
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        Collection<DB_BossTowerPassCondition> values = entity.getDbBuilder().getPassMap().values();
        for (DB_BossTowerPassCondition value : values) {
            BossTowerPassCondition condition = parseToClientCondition(value);
            if (condition != null) {
                resultBuilder.addPassCondition(condition);
            }
        }
        resultBuilder.setTodayVipBuyTime(entity.getDbBuilder().getTodayVipBuyTime());
        resultBuilder.setItemBuyTime(entity.getDbBuilder().getTodayItemBuy());
        resultBuilder.setTodayVipUseTime(entity.getDbBuilder().getTodayVipUseTime());
        resultBuilder.setTodayAlreadyChallengeTimes(entity.getDbBuilder().getTodayAlreadyChallengeTimes());
        gsChn.send(MsgIdEnum.SC_ClaimBossTowerInfo_VALUE, resultBuilder);
    }

    public static BossTowerPassCondition parseToClientCondition(DB_BossTowerPassCondition condition) {
        if (condition == null) {
            return null;
        }
        Builder result = BossTowerPassCondition.newBuilder();
        result.setConfigId(condition.getConfigId());
        if (condition.getPassInfoCount() > 0) {
            for (Entry<Integer, Integer> entry : condition.getPassInfoMap().entrySet()) {
                PassInfo.Builder subBuilder = PassInfo.newBuilder();
                subBuilder.setFightMakeId(entry.getKey());
                subBuilder.setPassTimes(entry.getValue());
                subBuilder.setFirst(1);
//                subBuilder.setTodayBuy(condition.getBuyInfoOrDefault(condition.getConfigId(), 0));
//                subBuilder.setOtherTime(condition.getOtherTimeOrDefault(condition.getConfigId(), 0));
                result.addPassInfo(subBuilder);
            }
        }
        result.addAllCanSweepId(condition.getCanSweepIdList());
        return result.build();
    }

    public static List<BossTowerPassCondition> parseToClientConditionList(Collection<DB_BossTowerPassCondition> conditionList) {
        if (CollectionUtils.isEmpty(conditionList)) {
            return Collections.emptyList();
        }
        return conditionList.stream().map(ClaimBossTowerInfoHandler::parseToClientCondition).collect(Collectors.toList());
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.BossTower;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimBossTowerInfo_VALUE, SC_ClaimBossTowerInfo.newBuilder().setRetCode(retCode));
    }
}
