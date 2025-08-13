package platform.logs.entity;

import cfg.PetRuneProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.petrune.PetRuneManager;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.ConsumeLog;
import platform.logs.LogClass.PetPropertyLog;
import platform.logs.StatisticsLogUtil;
import protocol.Common.Consume;
import protocol.PetMessage.Rune;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiao_FL
 * @date 2019/12/25
 */
@Getter
@Setter
@NoArgsConstructor
public class PetRuneLvlLog extends AbstractPlayerLog {
    private String name;
    private int id;
    private int originLvl;
    private int nowLvl;
    private List<RuneLvInfo> consumeRuneList = new ArrayList<>();
    private List<ConsumeLog> consumeItem;

    public PetRuneLvlLog(String playerIdx, int cfgId, int originLvl, int nowLvl, List<Rune> consumeRuneList, List<Consume> consume) {
        super(playerIdx);
        this.name = PetRuneProperties.getByRuneid(cfgId).getSevername();
        this.id = cfgId;
        this.originLvl = originLvl;
        this.nowLvl = nowLvl;
        if (consumeRuneList != null) {
            for (Rune rune : consumeRuneList) {
                this.consumeRuneList.add(new RuneLvInfo(rune));
            }
        }
        if (CollectionUtils.isNotEmpty(consume)) {
            consume = consume.stream().filter(e -> e.getCount() > 0).collect(Collectors.toList());
            this.consumeItem = StatisticsLogUtil.buildConsumeByList(consume);
        }
    }

}

@Getter
@Setter
@NoArgsConstructor
class RuneLvInfo {
    private String id;
    private int bookId;
    private String name;
    private int lv;
    private String quality;
    private List<PetPropertyLog> baseProperties = new ArrayList<>();

    public RuneLvInfo(Rune rune) {
        if (rune == null) {
            return;
        }

        this.id = rune.getId();
        this.bookId = rune.getRuneBookId();
        this.name = PetRuneProperties.getNameById(rune.getRuneBookId());
        this.lv = rune.getRuneLvl();
        int rarity = PetRuneProperties.getQualityByCfgId(rune.getRuneBookId());
        this.quality = StatisticsLogUtil.getQualityName(rarity);

        baseProperties = PetRuneManager.getInstance().queryBasePropertiesLog(rarity, lv, PetRuneProperties.getRuneType(bookId));
    }
}