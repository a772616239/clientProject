package model.arena.entity;

import cfg.ArenaDanObject;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import util.LogUtil;

/**
 * 竞技场段位分区(不按照大段位区分)
 *
 * @author huhan
 * @date 2020/05/11
 */
@Getter
@Setter
@ToString
public class ArenaDanGradingConfig {

    /**
     * 分区id
     */
    private int danId;

    private final List<OpponentRange> rangeList = new ArrayList<>();

    /**
     * 人机段位宠物池子
     * <petQuality, petCfgId>
     */
    private final Map<Integer, List<Integer>> petQualityMap = new HashMap<>();

    private ArenaDanGradingConfig() {
    }

    private void addRobotPetPool(int petCfgId) {
        PetBasePropertiesObject petCfg = PetBaseProperties.getByPetid(petCfgId);
        if (petCfg == null) {
            return;
        }

        List<Integer> cfgIdList = petQualityMap.computeIfAbsent(petCfg.getStartrarity(), e -> new ArrayList<>());
        cfgIdList.add(petCfgId);
    }

    public int randomGetPetByQuality(int quality) {
        List<Integer> cfgIdList = petQualityMap.get(quality);
        if (cfgIdList == null) {
            LogUtil.error("dan robot pet pool quality is not exist, dan:" + getDanId());
            return 0;
        }
        return cfgIdList.get(new Random().nextInt(cfgIdList.size()));
    }

    public static ArenaDanGradingConfig createEntity(ArenaDanObject cfgObj) {
        if (cfgObj == null) {
            LogUtil.error("model.arenaplayer.entity.ArenaPartition.createEntity, cfg obj is null");
            return null;
        }

        ArenaDanGradingConfig arenaDan = new ArenaDanGradingConfig();
        arenaDan.setDanId(cfgObj.getId());

        //检查对手随机范围
        int[][] opponentRange = cfgObj.getOpponentrange();
        for (int[] ints : opponentRange) {
            OpponentRange range = OpponentRange.createEntity(ints);
            if (range == null) {
                LogUtil.error("create OpponentRange failed, ArenaPartitionConfigObject cfgId=" + cfgObj.getId());
                return null;
            }
            arenaDan.addRangeList(range);
        }

        //添加宠物池子
        for (int cfgId : cfgObj.getRobotpetpool()) {
            arenaDan.addRobotPetPool(cfgId);
        }

        return arenaDan;
    }

    private void addRangeList(OpponentRange range) {
        if (range == null) {
            return;
        }
        this.rangeList.add(range);
    }
}
