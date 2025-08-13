package model.stoneRift;

import model.stoneRift.entity.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import protocol.Common;
import protocol.StoneRift;
import util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoneRiftUtil {

    public static StoneRift.StoneFactoryVo toFactoryVo(stoneriftEntity entity, int factoryId) {
        DbStoneRiftFactory factory = entity.getDB_Builder().getFactoryMap().get(factoryId);
        if (factory == null) {
            return null;
        }
        StoneRift.StoneFactoryVo.Builder vo = StoneRift.StoneFactoryVo.newBuilder();
        vo.setCfgId(factoryId);
        vo.setMaxDurable(factory.getMaxDurable());
        vo.setCurStore(factory.getCurStore());
        vo.setCurDurable(entity.getFactoryCurDurable(factory));
        vo.setMaxStore(entity.getMaxCanStore(factory));
        vo.setEfficiency(entity.getFactoryEfficiency(factory));
        vo.setClaimTime(factory.getClaimTimes());
        vo.setPetCfgId(factory.getPetCfgId());
        vo.setPetRarity(factory.getPetRarity());
        vo.setLevel(factory.getLevel());
        if (StringUtils.isNotBlank(factory.getPetId())) {
            vo.setPetId(factory.getPetId());
        }
        for (Common.Reward reward : factory.getBaseReward()) {
            int settleTimeInHour = (int) (TimeUtil.MS_IN_A_HOUR / StoneRiftCfgManager.getInstance().getSettleInterval());
            int count = (int) (reward.getCount() * ((vo.getEfficiency() * 1.0) / 1000.0) * settleTimeInHour);
            vo.addReward(reward.toBuilder().setCount(count).build());
        }
        vo.setNextCanClaimTime(factory.getNextCanClaimTime());
        return vo.build();
    }

    public static StoneRift.OverLoadInfo.Builder toOverLoadInfo(DbStoneRift db) {
        StoneRift.OverLoadInfo.Builder overload = StoneRift.OverLoadInfo.newBuilder();
        overload.setStartTime(db.getOverLoadStart());
        overload.setEndTime(db.getOverLoadExpire());
        overload.setNextCanUseTime(db.getNextCanOverLoad());
        return overload;

    }

    public static StoneRift.WorldMapInfo.Builder toMapInfo(DbPlayerWorldMap dbPlayerWorldMap, String seePlayerIdx) {
        StoneRift.WorldMapInfo.Builder worldMapInfo = StoneRift.WorldMapInfo.newBuilder();
        worldMapInfo.setMapId(StoneRiftWorldMapManager.getInstance().parseMapId(dbPlayerWorldMap.getUniqueMapId()));
        worldMapInfo.setUniqueId(dbPlayerWorldMap.getUniqueMapId());
        worldMapInfo.setUseFreeRefreshTime(dbPlayerWorldMap.getUserFreeRefreshTime());
        worldMapInfo.setBuyRefreshTime(dbPlayerWorldMap.getBuyRefreshTime());
        worldMapInfo.addAllPlayers(toPlayersVo(StoneRiftWorldMapManager.getInstance().findAllPlayersByMapId(dbPlayerWorldMap.getUniqueMapId()),seePlayerIdx));
        worldMapInfo.setBuyStealTime(dbPlayerWorldMap.getBuyStealTime());
        worldMapInfo.setUseStealTime(dbPlayerWorldMap.getUseStealTime());
        return worldMapInfo;
    }

    public static Iterable<StoneRift.StoneRiftWorldPlayer> toPlayersVo(List<StoneRiftWorldMapPlayer> allPlayersByMapId, String seePlayerIdx) {
        if (CollectionUtils.isEmpty(allPlayersByMapId)) {
            return Collections.emptyList();
        }
        if (seePlayerIdx == null) {
            seePlayerIdx = "";
        }

        List<StoneRift.StoneRiftWorldPlayer> result = new ArrayList<>();
        for (StoneRiftWorldMapPlayer mapPlayer : allPlayersByMapId) {
            StoneRift.StoneRiftWorldPlayer.Builder vo = StoneRift.StoneRiftWorldPlayer.newBuilder();
            vo.setIconId(mapPlayer.getIcon());
            vo.setPlayerName(mapPlayer.getPlayerName());
            vo.setPlayerIdx(mapPlayer.getPlayerIdx());
            vo.setLevel(mapPlayer.getRiftLv());
            vo.setCanSteal(mapPlayer.isCanSteal(seePlayerIdx));
            result.add(vo.build());
        }
        return result;
    }

    public static StoneRift.StoneRiftMsg.Builder toVo(StoneRiftMsg stoneRiftMsg) {
        if (stoneRiftMsg == null) {
            return null;
        }
        StoneRift.StoneRiftMsg.Builder builder = StoneRift.StoneRiftMsg.newBuilder();
        builder.setTime(stoneRiftMsg.getCreateTime());
        builder.setPlayerIdx(stoneRiftMsg.getPlayerIdx());
        builder.setPlayerName(stoneRiftMsg.getName());
        builder.setMsg(stoneRiftMsg.getMsg());
        return builder;
    }
}
