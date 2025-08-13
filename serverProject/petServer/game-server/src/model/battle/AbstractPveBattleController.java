package model.battle;

import cfg.FightMake;
import cfg.GameConfig;
import common.GameConst;
import common.HttpRequestUtil;
import common.IdGenerator;
import common.SyncExecuteFunction;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import lombok.Getter;
import lombok.Setter;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.mainLine.dbCache.mainlineCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.playerrecentpass.dbCache.playerrecentpassCache;
import model.recentpassed.RecentPassedUtil;
import model.recentpassed.dbCache.recentpassedCache;
import platform.logs.LogService;
import platform.logs.entity.BattleLog;
import platform.logs.entity.UnusualBattleLog;
import platform.logs.entity.UnusualBattleLog.UnusualCondition;
import protocol.Battle.BattleCheckParam;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattlePlayerInfo;
import protocol.Battle.BattleTypeEnum;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.CS_EnterFight;
import protocol.Battle.SC_BattleResult;
import protocol.Battle.SC_EnterFight.Builder;
import protocol.BattleMono.BattleFrameTypeEnum;
import protocol.BattleMono.CS_FrameData;
import protocol.BattleMono.FightParamTypeEnum;
import protocol.BattleMono.SC_FrameData;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;
import protocol.PetMessage.PetProperty;
import protocol.PlayerDB.DB_MonsterDifficultyInfo;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huhan
 * @date 2020/04/23
 * <p>
 * 战斗进入参数在自己的子类定义
 */
@Getter
@Setter
public abstract class AbstractPveBattleController extends AbstractBattleController {

    @Override
    public BattleTypeEnum getBattleType() {
        return BattleTypeEnum.BTE_PVE;
    }

    /**
     * 初始化通用信息
     */
    public void initCommon() {
        setBattleId(IdGenerator.getInstance().generateIdNum());
        setCamp(1);
        setRandSeed(GameUtil.randomPositiveLong());
    }

    /**
     * 初始化战斗所需参数
     *
     * @return
     */
    @Override
    public RetCodeEnum initBattle(CS_EnterFight req) {
        if (req == null) {
            LogUtil.warn("player:{} initBattle fail ,req is null", getPlayerIdx());
            return RetCodeEnum.RCE_ErrorParam;
        }

        RetCodeEnum enterResult = canEnterBattle();
        if (enterResult != RetCodeEnum.RCE_Success) {
            return enterResult;
        }

        setSkipBattle(req.getSkipBattle());

        initCommon();

        //处理参数
        if (!enterParamsSettle(req.getParamListList())) {
            LogUtil.warn("player:{} initBattle check enterParamsSettle fail ,req:{} ", getPlayerIdx(), req);
            return RetCodeEnum.RCE_ErrorParam;
        }

        //处理玩家信息
        RetCodeEnum initPlayerRet = initPlayerBattleData();
        if (initPlayerRet != RetCodeEnum.RCE_Success) {
            return initPlayerRet;
        }

        //处理战斗相关信息
        RetCodeEnum fightRet = initFightInfo();
        if (fightRet != RetCodeEnum.RCE_Success) {
            return fightRet;
        }

        RetCodeEnum checkParams = checkParams();
        if (checkParams != RetCodeEnum.RCE_Success) {
            return checkParams;
        }

        //初始化战斗时间
        initTime();

        //初始化完成
        initSuccess();

        return RetCodeEnum.RCE_Success;
    }

    /**
     * @return
     */
    @Override
    protected boolean directVictory() {
        for (BattlePlayerInfo battlePlayerInfo : getPlayerBattleData()) {
            if (battlePlayerInfo.getCamp() == getCamp()) {
                continue;
            }
            if (battlePlayerInfo.getPetListCount() <= 0
                    && battlePlayerInfo.getFriendHelpPetsCount() <= 0) {
                return true;
            }
            boolean petRemainHp = false;
            for (BattlePetData petData : battlePlayerInfo.getPetListList()) {
                for (int i = 0; i < petData.getPropDict().getKeysCount(); i++) {
                    PetProperty propType = petData.getPropDict().getKeys(i);
                    long propVal = petData.getPropDict().getValues(i);
                    if (propType == PetProperty.Current_Health && propVal > 0) {
                        petRemainHp = true;
                        break;
                    }
                }
            }
            if (!petRemainHp) {
                for (BattlePetData petData : battlePlayerInfo.getFriendHelpPetsList()) {
                    for (int i = 0; i < petData.getPropDict().getKeysCount(); i++) {
                        PetProperty propType = petData.getPropDict().getKeys(i);
                        long propVal = petData.getPropDict().getValues(i);
                        if (propType == PetProperty.Current_Health && propVal > 0) {
                            petRemainHp = true;
                            break;
                        }
                    }
                }
            }
            if (!petRemainHp) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断是否可以进入战斗
     *
     * @return
     */
    protected RetCodeEnum canEnterBattle() {
        playerEntity player = playerCache.getByIdx(getPlayerIdx());
        if (player == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        long enterBattleInterval = GameConfig.getById(GameConst.CONFIG_ID).getBattlecooldown() * TimeUtil.MS_IN_A_S;
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        return SyncExecuteFunction.executeFunction(player, p -> {
            if ((currentTime - player.getLastEnterFightTime()) <= enterBattleInterval) {
                return RetCodeEnum.RCE_Battle_BattleTooFast;
            } else {
                player.setLastEnterFightTime(currentTime);
                return RetCodeEnum.RCE_Success;
            }
        });
    }

    /**
     * 检查参数
     *
     * @return
     */
    private RetCodeEnum checkParams() {
        if (!PlayerUtil.playerIsExist(getPlayerIdx())) {
            LogUtil.error("AbstractPveBattleController.checkParams, playerIdx:" + getPlayerIdx()
                    + ", entity is not exist");
            return RetCodeEnum.RCE_Player_QueryPlayerNotExist;
        }

        if (FightMake.getById(getFightMakeId()) == null) {
            return RetCodeEnum.RCE_Battle_FightMakeIsNotExist;
        }

        return RetCodeEnum.RCE_Success;
    }

    /**
     * 战斗进入参数处理
     *
     * @param enterParams
     * @return
     */
    public abstract boolean enterParamsSettle(List<String> enterParams);

    /**
     * 初始化玩家的战斗信息
     *
     * @return
     */
    public RetCodeEnum initPlayerBattleData() {
        return addPlayerBattleData(getPlayerIdx(), getUseTeamType(), 1);
    }

    /**
     * 初始化fightMake信息， 此处设置fightMakeId,玩家怪物增强以及其他战斗中的数值
     *
     * @return
     */
    protected abstract RetCodeEnum initFightInfo();

    /**
     * 初始化成功
     */
    protected abstract void initSuccess();


    @Override
    public void timeoutSettle() {
        CS_BattleResult.Builder builder = CS_BattleResult.newBuilder();
        builder.setBattleId(getBattleId());
        // TODO 怪物胜利阵营
        builder.setWinnerCamp(2);
        builder.setPlaybackVersion(GameConfig.getById(GameConst.CONFIG_ID).getFightplaybackversion());

        settleBattle(builder.build());
    }


    /**
     * 战斗日志
     *
     * @param winCamp
     * @param rewards
     */
    @Override
    public void battleLog(int winCamp, List<Reward> rewards) {
        if (winCamp == getCamp()) {
            addRecentPassed();
        }

        long battleUseTime = GlobalTick.getInstance().getCurrentTime() - getEnterBattleTime();
        LogService.getInstance().submit(new BattleLog(getPlayerIdx(), winCamp, rewards, getPveEnterFightData(), getSubBattleType(), getPointId(), battleUseTime));
    }

    /**
     * 添加最近通关
     */
    private void addRecentPassed() {
        EnumFunction function = BattleUtil.getFunctionTypeByBattleType(getSubBattleType());
        if (RecentPassedUtil.allowFunction(function)) {
            recentpassedCache.getInstance().addRecentPassed(getPlayerIdx(), function, getPointId());
        }

        if (RecentPassedUtil.playerRecentAllowFunction(function)) {
            playerrecentpassCache.getInstance().updateRecentPassTeam(getPlayerIdx(), function);
        }
    }

    @Override
    public CS_BattleResult checkBattle(CS_BattleResult clientResult) {
        int clientBattleTime = getClientBattleTime(clientResult.getEndFrame());
        long realBattleTime = GlobalTick.getInstance().getCurrentTime() - getEnterBattleTime();

        UnusualBattleLog unusualBattleLog
                = new UnusualBattleLog(getPlayerIdx(), getSubBattleType(), clientBattleTime, realBattleTime, clientResult.getWinnerCamp());

        PreBattleCheckRet preRet = preBaseCheck(clientResult);
        if (preRet.isNoNeedToCheck()) {
            String strBuilder = "checkBattle[" + getBattleId() + "] noNeedToCheck ,playeridx=" + getPlayerIdx() +
                    ",fightMakeId=" + getFightMakeId();
            LogUtil.warn(strBuilder);
            return clientResult;
        } else if (preRet.isCheated()) {
            String strBuilder = "checkBattle[" + getBattleId() + "] failed UseGmEnd,playeridx=" + getPlayerIdx() +
                    ",fightMakeId=" + getFightMakeId();
            LogUtil.warn(strBuilder);
            if (!preRet.isMustCheck()) {
                //GM异常
                LogService.getInstance().submit(unusualBattleLog.setUnusualCondition(UnusualCondition.GM));
                return clientResult.toBuilder().setWinnerCamp(2).build();
            }
        }

        //检查是否加速作弊
        if (!preRet.isCheated() && !checkSpeedUp(clientResult, realBattleTime)) {
            preRet.setCheated(true);
        }

        if (!preRet.isMustCheck()) {
            //输了也有可能需要校验:对boss造成伤害
            if (clientResult.getWinnerCamp() != 1) {
                return clientResult;
            }
            if (preRet.isCheated()) {
                //加速异常
                LogService.getInstance().submit(unusualBattleLog.setUnusualCondition(UnusualCondition.SPEECH));
                return clientResult.toBuilder().setWinnerCamp(2).build();
            } else if (checkFightPower()) {
                return clientResult;
            }
        }

        CS_BattleResult realResult = HttpRequestUtil.checkBattle(getPlayerIdx(), getBattleId(), generateBattleCheckParam());
        if (realResult == null) {
            LogUtil.warn("checkBattle[" + getBattleId() + "] noResult,playeridx=" + getPlayerIdx() + ",isCheated=" + preRet.isCheated());
            if (preRet.isCheated()) {
                //校验失败,作弊
                LogService.getInstance().submit(unusualBattleLog.setUnusualCondition(UnusualCondition.CHEATED));
                return clientResult.toBuilder().setWinnerCamp(2).build();
            } else {
                return clientResult;
            }
        }

        if (preRet.isCheated()) {
            //作弊
            LogService.getInstance().submit(unusualBattleLog.setUnusualCondition(UnusualCondition.CHEATED));
            return realResult.toBuilder().setWinnerCamp(2).build();
        }

        if (realResult.getWinnerCamp() != clientResult.getWinnerCamp() || realResult.getEndFrame() != clientResult.getEndFrame()) {
            StringBuilder strBuilder = new StringBuilder("checkBattle[" + getBattleId() + "] result hugeDiff: clientFightVersion=" + clientResult.getFightVersion());
            strBuilder.append("\nSC_EnterFight=" + getPveEnterFightData().toString());
            strBuilder.append("\nrealResult=" + realResult);
            strBuilder.append("\nclientResult=" + clientResult);
            strBuilder.append("\nServerBattleTime:" + realBattleTime);
            LogUtil.warn(strBuilder.toString());
            //作弊
            LogService.getInstance().submit(unusualBattleLog.setUnusualCondition(UnusualCondition.CHEATED));
            return realResult;
        }
        if (!realResult.toByteString().equals(clientResult.toByteString())) {
            StringBuilder strBuilder = new StringBuilder("checkBattle[" + getBattleId() + "] result tinyDiff: clientFightVersion=" + clientResult.getFightVersion());
            strBuilder.append("\nSC_EnterFight=" + getPveEnterFightData().toString());
            strBuilder.append("\nrealResult=" + realResult);
            strBuilder.append("\nclientResult=" + clientResult);
            strBuilder.append("\nServerBattleTime:" + realBattleTime);
            LogUtil.warn(strBuilder.toString());
//            return realResult.toBuilder().setWinnerCamp(2).build();
        }
        if (!checkRealResultSpeedUp(realResult, realBattleTime)) {
            //加速
            LogService.getInstance().submit(unusualBattleLog.setUnusualCondition(UnusualCondition.SPEECH));
            return realResult.toBuilder().setWinnerCamp(2).build();
        }
        return realResult;
    }

    /**
     * 获取战斗力
     *
     * @param camp 阵营
     * @return
     */
    public long getFightPower(int camp) {
        long totalPower = 0;
        List<BattlePlayerInfo> playerBattleData = getPlayerBattleData();
        if (GameUtil.collectionIsEmpty(playerBattleData)) {
            return totalPower;
        }

        for (BattlePlayerInfo playerInfo : playerBattleData) {
            if (playerInfo.getCamp() == camp) {
                for (BattlePetData battlePetData : playerInfo.getPetListList()) {
                    totalPower += battlePetData.getAbility();
                }
                break;
            }
        }

        return totalPower;
    }

    protected BattleCheckParam generateBattleCheckParam() {
        BattleCheckParam.Builder builder = BattleCheckParam.newBuilder();
        Builder enterFightData = getPveEnterFightData();
        if (enterFightData == null) {
            LogUtil.error("AbstractPveBattleController.generateBattleCheckParam, enter fight data is null, subBattleType:"
                    + getSubBattleType() + ",battleID = " + getBattleId());
        } else {
            builder.setEnterFightData(enterFightData);
        }
        if (getPveFrameMap() != null) {
            for (SC_FrameData.Builder frameData : getPveFrameMap().values()) {
                builder.addFrameData(frameData);
            }
        }
        return builder.build();
    }

    /**
     * 返回关卡id
     *
     * @return
     */
    public abstract int getPointId();

    @Override
    public void handleBattleFrameData(CS_FrameData req) {
        if (req == null) {
            LogUtil.error("handleBattleFrameData, error param req is null,");
            return;
        }
        if (getBattleId() <= 0) {
            LogUtil.error("player[" + getPlayerIdx() + "] handle battle frame data error,no in battle");
            return;
        }

        if (req.getFrameIndex() < getCurPveFrameIndex()) {
            return;
        }
        setCurPveFrameIndex(req.getFrameIndex());
        if (req.getOperation().getFramType() == BattleFrameTypeEnum.BFTE_Null) {
            return;
        }
        if (frameDataContain(req.getFrameIndex())) {
            return;
        }
        if (req.getOperation().getFramType() == BattleFrameTypeEnum.BFTE_UseAssistance) {
            Consume consume = ConsumeUtil.parseConsume(RewardTypeEnum.RTE_Item_VALUE, GameConst.MineFriendHelpItemId, 1);
            if (!ConsumeManager.getInstance().materialIsEnough(getPlayerIdx(), consume)) {
                return;
            }
        }
        SC_FrameData.Builder builder = SC_FrameData.newBuilder().setFrameIndex(req.getFrameIndex()).addOperation(req.getOperation());
        putPveFrameMap(builder);
    }

    /**
     * 预检查
     *
     * @param clientResult
     * @return
     */
    public PreBattleCheckRet preBaseCheck(CS_BattleResult clientResult) {
        PreBattleCheckRet ret = new PreBattleCheckRet();
        //可以直接胜利时不需要校验
        if (directVictory()) {
            LogUtil.info("PreBaseCheck directWin playerId={},battleId={},subBattleType={},fightMakeId={}",getPlayerIdx(),getBattleId(),getSubBattleType(),getFightMakeId());
            ret.setNoNeedToCheck(true);
            return ret;
        }

        if (!ServerConfig.getInstance().isOpenBattleCheck()) {
            LogUtil.info("PreBaseCheck notOpenBattleCheck playerId={},battleId={},subBattleType={},fightMakeId={}",getPlayerIdx(),getBattleId(),getSubBattleType(),getFightMakeId());
            ret.setNoNeedToCheck(true);
            return ret;
        }
        if (GameUtil.isNewbieMap(getFightMakeId())) {
            LogUtil.info("PreBaseCheck newBieMap playerId={},battleId={},subBattleType={},fightMakeId={}",getPlayerIdx(),getBattleId(),getSubBattleType(),getFightMakeId());
            ret.setNoNeedToCheck(true);
            return ret;
        }

        if (clientResult.getIsGMEnd()) {
            if (ServerConfig.getInstance().isCanGmEndBattle()) {
                LogUtil.info("PreBaseCheck gmWin playerId={},battleId={},subBattleType={},fightMakeId={}",getPlayerIdx(),getBattleId(),getSubBattleType(),getFightMakeId());
                ret.setNoNeedToCheck(true);
                return ret;
            } else {
                ret.setCheated(true);
            }
        }

        if (getPveEnterFightData() != null) {
            for (FightParamTypeEnum param : getPveEnterFightData().getFightParamsList()) {
                if (param == FightParamTypeEnum.FPTE_PM_BossDamage || param == FightParamTypeEnum.FPTE_FightStar ||
                        param == FightParamTypeEnum.FPTE_BossRemainHpRate) {
                    ret.setMustCheck(true);
                    break;
                }
            }
        }
        return ret;
    }

    public boolean canPlayerSpeedUp() {
        playerEntity owner = playerCache.getByIdx(getPlayerIdx());
        return owner != null && mainlineCache.getInstance().getCurOnHookNode(getPlayerIdx()) >= GameConfig.getById(GameConst.CONFIG_ID).getBattlespeedupunlocknode();
    }

    public boolean checkSpeedUp(CS_BattleResult clientResult, long realBattleTime) {
        boolean canSpeedUp = canPlayerSpeedUp();
        long playerSpeedUpRate = 1500l;
        long baseSpeedUpRate = 1500l;
        long minBattleTime = 10000l * 1000l / baseSpeedUpRate;
        if (canSpeedUp) {
            minBattleTime = minBattleTime * 1000 / playerSpeedUpRate;
        }
        if (realBattleTime <= minBattleTime) { // 最短时间内结束算失败
            StringBuilder strBuilder = new StringBuilder("checkBattle[" + getBattleId() + "] failed svrBattleTime<" + minBattleTime / 1000 + "s,playeridx=");
            strBuilder.append(getPlayerIdx());
            strBuilder.append(",fightMakeId=" + getFightMakeId());
            strBuilder.append(",serverTime:" + realBattleTime);
            LogUtil.warn(strBuilder.toString());
            return false;
        }

        long beforeClientBattleTime = clientResult.getEndFrame() * ServerConfig.getInstance().getBattleTickCycle() * 1000l / baseSpeedUpRate;
        if (canSpeedUp) {
            beforeClientBattleTime = beforeClientBattleTime * 1000l / playerSpeedUpRate;
        }

        if (clientResult.getEndFrame() <= 100 || beforeClientBattleTime - 3000 > realBattleTime) {
            StringBuilder checkClientTime = new StringBuilder("checkBattle[" + getBattleId() + "] failed clientBattleTime too fast,playeridx=");
            checkClientTime.append(getPlayerIdx());
            checkClientTime.append(",fightMakeId=" + getFightMakeId());
            checkClientTime.append(",serverTime:" + realBattleTime);
            checkClientTime.append(",clientTime:" + beforeClientBattleTime);
            checkClientTime.append(",canSpeedUp:" + canSpeedUp);
            LogUtil.warn(checkClientTime.toString());
            return false;
        }
        return true;
    }

    public boolean checkRealResultSpeedUp(CS_BattleResult realResult, long realBattleTime) {
        if (realResult.getWinnerCamp() == 1) {
            boolean canSpeedUp = canPlayerSpeedUp();
            long clientBattleTime = realResult.getEndFrame() * ServerConfig.getInstance().getBattleTickCycle() * 10 / 12;
            if (canSpeedUp) {
                clientBattleTime /= 2;
            }
            if (clientBattleTime * 9 / 10 > realBattleTime) { // 未到时间直接判负，10%容错
                StringBuilder strBuilder = new StringBuilder("checkBattle[" + getBattleId() + "] failed, but player play too fast,playeridx=");
                strBuilder.append(getPlayerIdx());
                strBuilder.append(",fightMakeId=" + getFightMakeId());
                strBuilder.append(",serverTime:" + realBattleTime);
                strBuilder.append(",clientTime:" + clientBattleTime);
                strBuilder.append(",canSpeedUp:" + canSpeedUp);
                LogUtil.warn(strBuilder.toString());
                return false;
            }
        }
        return true;
    }

    /**
     * 检查战力
     *
     * @return true战力大于所需要的的战力
     */
    public boolean checkFightPower() {
        int level = 1;
        int star = 1;
        playerEntity player = playerCache.getByIdx(getPlayerIdx());
        if (player != null) {
            DB_MonsterDifficultyInfo.Builder difficulty = player.getMonsterDiffByFunction(getFunctionEnum());
            if (difficulty != null) {
                level = difficulty.getLevel();
            }
        }
        long ownerPower = getFightPower(getCamp());
        long needFightPower = FightMake.getInstance().getNeedFightPowerById(getFightMakeId(), level, star);
        return needFightPower > 0 && ownerPower > needFightPower * 900 / 1000;
    }

    public int getClientBattleTime(int endFrame) {
        return endFrame * ServerConfig.getInstance().getBattleTickCycle();
    }

    /**
     * 战斗记录保存
     */
    @Override
    public void saveBattlePlayBack(CS_BattleResult battleResult, SC_BattleResult.Builder toClientResult) {
        List<SC_FrameData> frameColl = getPveFrameMap().values().stream()
                .map(SC_FrameData.Builder::build)
                .collect(Collectors.toList());
        internalSaveBattleRecord(battleResult, toClientResult, frameColl);
    }

}
