package platform.logs.statistics;

import cfg.PetGemConfig;
import com.alibaba.fastjson.annotation.JSONField;
import db.entity.BaseEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import model.pet.dbCache.petCache;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import model.team.dbCache.teamCache;
import model.team.entity.teamEntity;
import protocol.PetMessage.Gem;
import util.MapUtil;

/**
 * @Description
 * @Author hanx
 * @Date2020/10/14 0014 14:51
 **/
@Getter
public class GemStatistics extends AbstractStatistics {
    private static final GemStatistics instance = new GemStatistics();

    public static GemStatistics getInstance() {
        return instance;
    }

    private GemStatistics() {
    }


    /**
     * 已装备宝石等级
     */
    @JSONField(serialize = false)
    private long totalEquipLv;


    /**
     * 已装备宝石总个数
     */
    @JSONField(serialize = false)
    private int totalEquipNum;

    /**
     * 装备宝石map<品质，数量>(需求改为上阵)
     */
    private final Map<Integer, Long> equipGemRarityMap = new ConcurrentHashMap<>();

    /**
     * 拥有宝石map<品质，数量>
     */
    private final Map<Integer, Long> ownGemRarityMap = new ConcurrentHashMap<>();


    @Override
    public void init() {
        for (BaseEntity entity : petgemCache.getInstance()._ix_id.values()) {
            petgemEntity runeEntity = (petgemEntity) entity;
            teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(runeEntity.getPlayeridx());
            for (Gem gem : runeEntity.getGemListBuilder().getGemsMap().values()) {
                int lv = PetGemConfig.queryEnhanceLv(gem.getGemConfigId());
                int gemRarity = PetGemConfig.queryRarity(gem.getGemConfigId());
                if (petCache.getInstance().gemInFightPet(teamEntity, gem)) {
                    totalEquipLv += lv;
                    totalEquipNum++;
                    MapUtil.add2LongMapValue(equipGemRarityMap, gemRarity, 1L);

                }
                MapUtil.add2LongMapValue(ownGemRarityMap, gemRarity, 1L);
            }
        }
    }


    public double getEquipAvgLv() {
        return getAvg(totalEquipLv, totalEquipNum);
    }

    public synchronized void updateOwnGemRarityMap(Map<Integer, Long> adds) {
        MapUtil.mergeLongMaps(ownGemRarityMap, adds);
    }


    public synchronized void updateEquipGemRarityMap(Map<Integer, Long> adds) {
        MapUtil.mergeLongMaps(equipGemRarityMap, adds);
    }

    public synchronized void updateEquipEnhanceLv(int adds) {
        totalEquipLv += adds;
    }

    public synchronized void updateTotalEquipNum(int adds) {
        totalEquipNum += adds;
    }
}
