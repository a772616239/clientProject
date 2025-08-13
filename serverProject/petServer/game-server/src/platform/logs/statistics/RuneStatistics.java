package platform.logs.statistics;

import cfg.PetRuneProperties;
import db.entity.BaseEntity;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import org.apache.commons.lang.StringUtils;
import protocol.PetMessage.Rune;
import util.MapUtil;

import java.util.Map;

/**
 * @Description
 * @Author hanx
 * @Date2020/10/14 0014 14:51
 **/
@Getter
public class RuneStatistics extends AbstractStatistics {
    private final static RuneStatistics instance = new RuneStatistics();

    public static RuneStatistics getInstance() {
        return instance;
    }

    private RuneStatistics() {
    }

    private final Map<Integer, Long> ownRarityMap = new ConcurrentHashMap<>();

    private final Map<Integer, Long> equipRarityMap = new ConcurrentHashMap<>();


    @Override
    public void init() {
        for (BaseEntity entity : petruneCache.getInstance()._ix_id.values()) {
            petruneEntity runeEntity = (petruneEntity) entity;
            for (Rune rune : runeEntity.getRuneListBuilder().getRuneMap().values()) {
                int rarity = PetRuneProperties.getQualityByCfgId(rune.getRuneBookId());
                MapUtil.add2LongMapValue(ownRarityMap, rarity, 1L);
                if (StringUtils.isNotBlank(rune.getRunePet())) {
                    MapUtil.add2LongMapValue(equipRarityMap, rarity, 1L);
                }
            }
        }
    }


    public synchronized void updateOwnRarityMap(Map<Integer, Long> adds) {
        MapUtil.mergeLongMaps(ownRarityMap, adds);
    }


    public synchronized void updateEquipRarityMap(Map<Integer, Long> adds) {
        MapUtil.mergeLongMaps(equipRarityMap, adds);
    }


}
