package model.mistforest.mistobj;

import cfg.MistBusinessManReward;
import cfg.MistBusinessManRewardObject;
import common.GameConst.EventType;
import common.GlobalData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.mistforest.MistConst.MistGambleResult;
import model.mistforest.room.entity.MistRoom;
import model.mistplayer.entity.MistPlayer;
import protocol.MessageId.MsgIdEnum;
import protocol.MistForest.MistTaskTargetType;
import protocol.MistForest.MistUnitPropTypeEnum;
import protocol.MistForest.SC_BusinessManResult;
import protocol.MistForest.SC_BusinessManReward;
import protocol.MistForest.UnitMetadata;
import protocol.ServerTransfer.CS_GS_ReqChangeMistStamina;
import protocol.TransServerCommon.MistBusinessManData;
import protocol.TransServerCommon.MistBusinessManData.Builder;
import server.event.Event;
import server.event.EventManager;

public class MistBusinessMan extends MistObject {
    protected HashMap<Long, MistBusinessManData.Builder> playerDataMap;

    public MistBusinessMan(MistRoom room, int objType) {
        super(room, objType);
        playerDataMap = new HashMap<>();
    }

    @Override
    public void clear() {
        super.clear();
        playerDataMap.clear();
    }

    @Override
    public void reborn() {
        super.reborn();
        playerDataMap.clear();
    }

    @Override
    protected boolean isSpecialProp(int propType) {
        return super.isSpecialProp(propType) || propType == MistUnitPropTypeEnum.MUPT_BusinessManCardDigit_VALUE
                || propType == MistUnitPropTypeEnum.MUPT_BusinessManRemainTimes_VALUE;
    }

    @Override
    public UnitMetadata getMetaData(MistFighter fighter) {
        UnitMetadata.Builder metaData = super.getMetaData(fighter).toBuilder();
        if (fighter != null) {
            long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
            Builder playerData = playerDataMap.get(playerId);
            if (playerData == null) {
                metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_BusinessManRemainTimes_VALUE)
                        .addValues(getAttribute(MistUnitPropTypeEnum.MUPT_BusinessManMaxTimes_VALUE));
            } else {
                metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_BusinessManRemainTimes_VALUE)
                        .addValues(playerData.getRemainTimes());
                metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_BusinessManCardDigit_VALUE)
                        .addValues(playerData.getPlayerCardDigit());
            }
        } else {
            metaData.getPropertiesBuilder().addKeysValue(MistUnitPropTypeEnum.MUPT_BusinessManRemainTimes_VALUE)
                    .addValues(getAttribute(MistUnitPropTypeEnum.MUPT_BusinessManMaxTimes_VALUE));
        }
        return metaData.build();
    }

    public void startGamble(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        if (playerId <= 0) {
            return;
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        if (getAttribute(MistUnitPropTypeEnum.MUPT_BusinessManMaxTimes_VALUE) <= 0) {
            return;
        }
        int needStamina = 0;
        Builder playerData = playerDataMap.get(playerId);
        if (playerData == null) {
            int curStamina = player.getMistStamina();
            needStamina = (int) getAttribute(MistUnitPropTypeEnum.MUPT_OpenBoxNeedStamina_VALUE);
            if (needStamina > 0  && curStamina < needStamina) {
                return;
            }
            fighter.getNpcTask().doNpcTask(MistTaskTargetType.MTTT_JoinBusinessManGame_VALUE, 1, 0);
        } else {
            if (playerData.getRemainTimes() <= 0) {
                return;
            }
            if (playerData.getPlayerCardDigit() > 0) {
                return;
            }
        }
        if (needStamina > 0) {
            CS_GS_ReqChangeMistStamina.Builder builder = CS_GS_ReqChangeMistStamina.newBuilder();
            builder.setPlayerIdx(player.getIdx());
            builder.setChangeValue(-needStamina);
            GlobalData.getInstance().sendMsgToServer(player.getServerIndex(), MsgIdEnum.CS_GS_ReqChangeMistStamina_VALUE, builder);
        }
        generatePlayerCard(fighter, playerId);
    }

    protected void generatePlayerCard(MistFighter fighter, long playerId) {
        List<Integer> intList = new ArrayList<>(9);
        for (int i = 1; i < 9; i++) { // 玩法写死1-9
            intList.add(i);
        }
        Collections.shuffle(intList);
        int playerDigit = intList.get(0);
        int systemDigit = intList.get(1);
        Builder playerData = playerDataMap.get(playerId);
        if (playerData == null) {
            playerData = MistBusinessManData.newBuilder();
            playerData.setRemainTimes((int) getAttribute(MistUnitPropTypeEnum.MUPT_BusinessManMaxTimes_VALUE));
            playerDataMap.put(playerId, playerData);
        }
        playerData.setPlayerCardDigit(playerDigit);
        playerData.setSystemCardDigit(systemDigit);

        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_BusinessManCardDigit_VALUE, playerDigit);
        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_BusinessManRemainTimes_VALUE, playerData.getRemainTimes());
    }

    public void getGambleResult(MistFighter fighter, int playerChoice) {
        if (!isAlive()) {
            return;
        }
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        if (playerId <= 0) {
            return;
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        Builder playerData = playerDataMap.get(playerId);
        if (playerData == null || playerData.getSystemCardDigit() <= 0) {
            return;
        }

        boolean result = (playerChoice == MistGambleResult.bigger && (playerData.getPlayerCardDigit() < playerData.getSystemCardDigit()))
                || (playerChoice == MistGambleResult.smaller && (playerData.getPlayerCardDigit() > playerData.getSystemCardDigit()));

        int maxTimes = (int) getAttribute(MistUnitPropTypeEnum.MUPT_BusinessManMaxTimes_VALUE);
        int remainTimes = playerData.getRemainTimes() - 1;
        SC_BusinessManResult.Builder builder = SC_BusinessManResult.newBuilder();
        builder.setTargetId(getId());
        builder.setResult(result);
        builder.setResultDigit(playerData.getSystemCardDigit());
        builder.setRemainTimes(remainTimes);

        if (result) {
            if (remainTimes <= 0) {
                Map<Integer, Integer> rewardMap = generateRewardByGambleTimes(maxTimes);
                if (rewardMap != null) {
                    Event event = Event.valueOf(EventType.ET_GainMistCarryReward, getRoom(), player);
                    event.pushParam(rewardMap, false);
                    EventManager.getInstance().dispatchEvent(event);
                }
                playerData.clearRemainTimes();
                playerData.clearSystemCardDigit();
                addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_BusinessManRemainTimes_VALUE, 0);
            } else {
                playerData.setRemainTimes(remainTimes);
                addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_BusinessManRemainTimes_VALUE, remainTimes);
            }
        } else {
            playerData.clearRemainTimes();
            playerData.clearSystemCardDigit();
            addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_BusinessManRemainTimes_VALUE, 0);

            Map<Integer, Integer> rewardMap = generateRewardByGambleTimes(-1); // 失败直接发默认奖励
            if (rewardMap != null) {
                Event event = Event.valueOf(EventType.ET_GainMistCarryReward, getRoom(), player);
                event.pushParam(rewardMap, false);
                EventManager.getInstance().dispatchEvent(event);
            }
        }
        playerData.clearPlayerCardDigit();
        player.sendMsgToServer(MsgIdEnum.SC_BusinessManResult_VALUE, builder);
    }

    public void getGambleReward(MistFighter fighter) {
        if (!isAlive()) {
            return;
        }
        long playerId = fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE);
        if (playerId <= 0) {
            return;
        }
        MistPlayer player = fighter.getOwnerPlayerInSameRoom();
        if (player == null) {
            return;
        }
        Builder playerData = playerDataMap.get(playerId);
        if (playerData == null) {
            return;
        }
        int maxGambleTimes = (int) getAttribute(MistUnitPropTypeEnum.MUPT_BusinessManMaxTimes_VALUE);
        if (playerData.getRemainTimes() <= 0 || playerData.getRemainTimes() >= maxGambleTimes) {
            return;
        }
        if (playerData.getSystemCardDigit() <= 0) {
            return;
        }
        HashMap<Integer, Integer> rewardMap = generateRewardByGambleTimes(maxGambleTimes - playerData.getRemainTimes());
        if (rewardMap != null) {
            Event event = Event.valueOf(EventType.ET_GainMistCarryReward, getRoom(), player);
            event.pushParam(rewardMap, false);
            EventManager.getInstance().dispatchEvent(event);
        }
        SC_BusinessManReward.Builder builder = SC_BusinessManReward.newBuilder();
        builder.setTargetId(getId());
        builder.setGuessTimes(maxGambleTimes - playerData.getRemainTimes());
        player.sendMsgToServer(MsgIdEnum.SC_BusinessManReward_VALUE, builder);

        playerData.clearPlayerCardDigit();
        playerData.clearSystemCardDigit();
        playerData.clearRemainTimes();

        addPrivatePropCmd(fighter, MistUnitPropTypeEnum.MUPT_BusinessManRemainTimes_VALUE, 0);
    }

    protected HashMap<Integer, Integer> generateRewardByGambleTimes(int times) {
        if (times == 0) {
            return null;
        }
        int cfgId = (int) getAttribute(MistUnitPropTypeEnum.MUPT_UnitConfigId_VALUE);
        MistBusinessManRewardObject rewardCfg = MistBusinessManReward.getById(cfgId);
        if (rewardCfg == null) {
            return null;
        }
        HashMap<Integer, Integer> rewards = new HashMap<>();
        if (times > 0) {
            int[][] rewardList = rewardCfg.getFinishreward();
            if (rewardList == null || rewardList.length <= 0) {
                return null;
            }
            if (times > rewardList.length) {
                return null;
            }
            if (rewardList[times - 1] == null) {
                return null;
            }
            if (rewardList[times - 1].length <= 0) {
                return null;
            }
            if (rewardList[times - 1].length % 2 != 0) {
                return null;
            }
            for (int i = 0; i < rewardList[times - 1].length; i++) {
                if (i % 2 != 0) {
                    continue;
                }
                rewards.put(rewardList[times - 1][i], rewardList[times - 1][i+1]);
            }
        } else {
            if (rewardCfg.getFailedreward() == null || rewardCfg.getFailedreward().length <= 0) {
                return null;
            }
            for (int i = 0; i < rewardCfg.getFailedreward().length; i++) {
                if (rewardCfg.getFailedreward()[i] == null || rewardCfg.getFailedreward()[i].length < 2) {
                    continue;
                }
                rewards.put(rewardCfg.getFailedreward()[i][0],rewardCfg.getFailedreward()[i][1]);
            }
        }
        return rewards;
    }
}
