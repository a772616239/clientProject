package model.battle;

import cfg.GameConfig;
import common.GameConst;
import lombok.Getter;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.warpServer.battleServer.BattleServerManager;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.CS_BattleResult.Builder;
import protocol.Battle.CS_EnterFight;
import protocol.Battle.SC_BattleResult;
import protocol.BattleMono.BattleFrameTypeEnum;
import protocol.BattleMono.CS_FrameData;
import protocol.BattleMono.SC_FrameData;
import protocol.Common.Consume;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huhan
 * @date 2020/04/27
 */
public abstract class AbstractPvpBattleController extends AbstractBattleController {

    @Override
    public RetCodeEnum initBattle(CS_EnterFight enterFight) {
        return RetCodeEnum.RCE_Success;
    }

    @Override
    public CS_BattleResult checkBattle(CS_BattleResult resultData) {
        return resultData;
    }

    @Override
    public void timeoutSettle() {
        Builder builder = CS_BattleResult.newBuilder();
        builder.setBattleId(getBattleId());
        builder.setWinnerCamp(-1);
        builder.setPlaybackVersion(GameConfig.getById(GameConst.CONFIG_ID).getFightplaybackversion());

        settleBattle(builder.build());
    }

    @Override
    public BattleTypeEnum getBattleType() {
        return BattleTypeEnum.BTE_PVP;
    }

    @Override
    public void handleBattleFrameData(CS_FrameData req) {
        if (getBattleId() <= 0) {
            LogUtil.error("player[" + getPlayerIdx() + "] handle battle frame data error,no in battle");
            return;
        }
        if (req.getOperation().getFramType() == BattleFrameTypeEnum.BFTE_UseAssistance) {
            Consume consume = ConsumeUtil.parseConsume(RewardTypeEnum.RTE_Item_VALUE, GameConst.MineFriendHelpItemId, 1);
            if (!ConsumeManager.getInstance().consumeMaterial(getPlayerIdx(), consume,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MineFight, "pvp助阵"))) {
                return;
            }
            BattleUtil.addHeldMailToFriend(getPlayerIdx());

            EventUtil.triggerUpdateTargetProgress(getPlayerIdx(), TargetTypeEnum.TEE_Mine_FinishedFriendHelp, 1, 0);
        }
        BattleServerManager.getInstance().transferMsgToBattleServer(
                getPlayerIdx(), MsgIdEnum.CS_FrameData_VALUE, req.toByteString(), false);
    }

    @Override
    protected boolean directVictory() {
        return false;
    }

    @Override
    public void saveBattlePlayBack(CS_BattleResult battleResult, SC_BattleResult.Builder toClientResult) {
        internalSaveBattleRecord(battleResult, toClientResult, getFrameDataList());
    }

    /**
     *
     */
    @Getter
    private List<SC_FrameData> frameDataList;

    public void addAllFrameData(List<SC_FrameData> frameList) {
        if (CollectionUtils.isEmpty(frameList)) {
            return;
        }

        if (this.frameDataList == null) {
            this.frameDataList = new ArrayList<>();
        }

        this.frameDataList.addAll(frameList);
    }

    @Override
    public void clear() {
        super.clear();
        if (this.frameDataList != null) {
            this.frameDataList.clear();
        }
    }
}
