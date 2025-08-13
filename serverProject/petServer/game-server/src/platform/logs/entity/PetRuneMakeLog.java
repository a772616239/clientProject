package platform.logs.entity;

import cfg.PetRuneProperties;
import cfg.PetRunePropertiesObject;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.AbstractPlayerLog;
import platform.logs.LogClass.ConsumeLog;
import platform.logs.StatisticsLogUtil;
import protocol.Common.Consume;
import protocol.PetMessage.Rune;
import protocol.PetMessage.RuneProperties;
import protocol.PetMessage.RunePropertieyEntity;

@Getter
@Setter
@NoArgsConstructor
public class PetRuneMakeLog extends AbstractPlayerLog {

    private RuneItemLog oldRuneLog;
    private RuneItemLog newRuneLog;
    private List<ConsumeLog> consumeLogList;

    public PetRuneMakeLog(String playerId, Rune oldRune, Rune newRune, List<Consume> consumeList) {
        super(playerId);
        if (oldRune != null) {
            oldRuneLog = new RuneItemLog(oldRune);
        }
        if (newRune != null) {
            newRuneLog = new RuneItemLog(newRune);
        }

        if (CollectionUtils.isNotEmpty(consumeList)) {
            consumeLogList = StatisticsLogUtil.buildConsumeByList(consumeList);
        }
    }

}

@Data
class RuneItemLog {
    private String name;
    private String id;
    private String rarity;
    private int level;
    private int exp;
    private List<Integer> exPropertyTypeList = new ArrayList<>();

    public RuneItemLog(Rune rune) {
        this.id = rune.getId();
        this.level = rune.getRuneLvl();
        this.exp = rune.getRuneExp();
        PetRunePropertiesObject petRuneCfg = PetRuneProperties.getByRuneid(rune.getRuneBookId());
        if (petRuneCfg != null) {
            this.name = petRuneCfg.getSevername();
            this.rarity = StatisticsLogUtil.getQualityName(petRuneCfg.getRunerarity());
        }
        RuneProperties runeExProperty = rune.getRuneExProperty();
        for (RunePropertieyEntity runePropertieyEntity : runeExProperty.getPropertyList()) {
            if (runePropertieyEntity != null) {
                exPropertyTypeList.add(runePropertieyEntity.getPropertyType());
            }

        }
    }

}
