package server.handler.stoneRift;

import cfg.StoneRiftLevel;
import cfg.StoneRiftScience;
import cfg.StoneRiftScienceObject;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;

import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.stoneRift.StoneRiftCfgManager;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.stoneriftEntity;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId;
import protocol.StoneRift.CS_StudyStoneRiftScience;
import protocol.StoneRift.SC_StudyStoneRiftScience;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.SC_StudyStoneRiftScience_VALUE;

/**
 * 点科技点
 */
@Slf4j
@MsgId(msgId = MsgIdEnum.CS_StudyStoneRiftScience_VALUE)
public class StudyStoneRiftScienceHandler extends AbstractBaseHandler<CS_StudyStoneRiftScience> {

    @Override
    protected CS_StudyStoneRiftScience parse(byte[] bytes) throws Exception {
        return CS_StudyStoneRiftScience.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_StudyStoneRiftScience req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_StudyStoneRiftScience.Builder msg = studyStoneRiftScience(playerId, req);
        GlobalData.getInstance().sendMsg(playerId, SC_StudyStoneRiftScience_VALUE, msg);

    }

    private SC_StudyStoneRiftScience.Builder studyStoneRiftScience(String playerId, CS_StudyStoneRiftScience req) {
        SC_StudyStoneRiftScience.Builder msg = SC_StudyStoneRiftScience.newBuilder();
        int scienceId = req.getScienceId();
        stoneriftEntity stoneRift = stoneriftCache.getByIdx(playerId);
        if (stoneRift == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            return msg;
        }
        StoneRiftScienceObject cfg = StoneRiftScience.getById(scienceId);
        if (cfg == null) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_ErrorParam));
            return msg;
        }
        if (!checkPreSkill(scienceId, stoneRift, cfg)
                || !StoneRiftCfgManager.getInstance().checkStoneRiftLvEnough(stoneRift.getStoneRiftLv(), scienceId)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRiftScienceStudyConditionNotMatch));
            return msg;
        }
        if (StoneRiftCfgManager.getInstance().checkLevelMax(scienceId, stoneRift, cfg)) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRiftScienceLevelMaxLimit));
            return msg;
        }
        Common.Consume consume = ConsumeUtil.parseConsume(cfg.getStudyconsume());
        if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume,
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_StoneRift))) {
            msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Player_CurrencysNotEnought));
            return msg;

        }

        SyncExecuteFunction.executeConsumer(stoneRift, ex -> {
            stoneRift.studyScience(cfg);
        });

        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_StoneRift_InvalidWord));
        msg.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        return msg;
    }

    private boolean checkPreSkill(int scienceId, stoneriftEntity stoneRift, StoneRiftScienceObject cfg) {
        Map<Integer, Integer> skillLvMap = stoneRift.getDB_Builder().getDbScience().getSkillLvMap();
        Integer skillLv = skillLvMap.get(scienceId);
        if (skillLv != null && skillLv > 0) {
            return true;
        }
        int[] prveInfo = cfg.getPrveinfo();
        if (prveInfo.length >= 2) {
            int lv = skillLvMap.getOrDefault(prveInfo[0], 0);
            return lv >= prveInfo[1];
        }
        return true;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.StoneRift;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(SC_StudyStoneRiftScience_VALUE, SC_StudyStoneRiftScience.newBuilder().setRetCode(retCode));

    }
}
