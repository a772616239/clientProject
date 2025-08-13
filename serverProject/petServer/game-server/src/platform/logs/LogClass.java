package platform.logs;

import cfg.Item;
import cfg.PetBaseProperties;
import cfg.PetRuneProperties;
import cfg.PetRunePropertiesObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import protocol.Battle.BattlePetData;
import protocol.Common.Consume;
import protocol.Common.Reward;
import protocol.PetMessage;
import protocol.PetMessage.Pet;
import protocol.PetMessage.PetPropertyEntity;
import protocol.PetMessage.Rune;
import protocol.PetMessage.RunePropertieyEntity;

import java.util.Arrays;
import java.util.List;

/**
 * @author huhan
 */
public class LogClass {

    @Setter
    @Getter
    public static class ItemChangeLog {
        private String name;
        private int changed;
        private int remain;

        public ItemChangeLog(int cfgId, int changed, int remain) {
            this.name = Item.getItemName(cfgId);
            this.changed = changed;
            this.remain = remain;
        }
    }

    @Setter
    @Getter
    public static class PetLog {
        private int id;
        private String name;
        private int lv;
        private int rarity;
        private int awake;
        private long ability;
        private int evolveLv;

        public PetLog(BattlePetData data) {
            if (data == null) {
                return;
            }
            this.id = data.getPetCfgId();
            this.name = PetBaseProperties.getNameById(data.getPetCfgId());
            this.lv = data.getPetLevel();
            this.rarity = data.getPetRarity();
            this.awake = data.getAwake();
            this.ability = data.getAbility();
            this.evolveLv =data.getEvolveLv();
        }

        public PetLog(Pet pet) {
            if (pet == null) {
                return;
            }
            this.id = pet.getPetBookId();
            this.name = PetBaseProperties.getNameById(pet.getPetBookId());
            this.lv = pet.getPetLvl();
            this.rarity = pet.getPetRarity();
            this.awake = pet.getPetUpLvl();
            this.ability = pet.getAbility();
        }
    }


    @Setter
    @Getter
    @NoArgsConstructor
    public static class RewardLog {
        protected int type;
        protected int id;
        protected String name;
        protected int count;

        public RewardLog(Reward reward) {
            if (reward == null) {
                return;
            }
            this.type = reward.getRewardTypeValue();
            this.id = reward.getId();
            this.name = StatisticsLogUtil.getNameByTypeAndId(reward.getRewardType(), reward.getId());
            this.count = reward.getCount();
        }

        public RewardLog(int typeValue, int id, int count) {
            this.type = typeValue;
            this.id = id;
            this.name = StatisticsLogUtil.getNameByTypeValueAndId(typeValue, id);
            this.count = count;
        }
    }

    public static class ConsumeLog extends RewardLog {
        public ConsumeLog(Consume consume) {
            if (consume == null) {
                return;
            }

            this.type = consume.getRewardTypeValue();
            this.id = consume.getId();
            this.name = StatisticsLogUtil.getNameByTypeAndId(consume.getRewardType(), consume.getId());
            this.count = consume.getCount();
        }
    }

    @Getter
    public static class ConsumeRemainLog extends ConsumeLog {
        private long remainCount;

        public ConsumeRemainLog( String playerIdx,Consume consume) {
            super(consume);
            itembagEntity itemBag = itembagCache.getInstance().getItemBagByPlayerIdx(playerIdx);
            if (itemBag != null) {
                this.remainCount = itemBag.getItemCount(consume.getCount());
            }
        }
    }

    @Getter
    @Setter
    public static class PetPropertyLog {
        private String name;
        private double value;

        public PetPropertyLog(RunePropertieyEntity entity) {
            if (entity == null) {
                return;
            }
            this.name = StatisticsLogUtil.getPropertyName(entity.getPropertyType());
            this.value = transformValue(entity.getPropertyType(),entity.getPropertyValue());
        }

        public PetPropertyLog(PetPropertyEntity entity) {
            if (entity == null) {
                return;
            }
            this.name = StatisticsLogUtil.getPropertyName(entity.getPropertyType());
            this.value = transformValue(entity.getPropertyType(),entity.getPropertyValue());
        }

        public PetPropertyLog(int type, int value) {
            this.name = StatisticsLogUtil.getPropertyName(type);
            this.value = transformValue(type,value);
        }
    }

    private static final List<Integer> originValueList =
            Arrays.asList(PetMessage.PetProperty.ATTACK_VALUE, PetMessage.PetProperty.HEALTH_VALUE, PetMessage.PetProperty.DEFENSIVE_VALUE);

    public static double transformValue(int type, float origin) {
        if (originValueList.contains(type)) {
            return origin;
        }
        return origin / 10000.0;
    }

    @Getter
    @Setter
    public static class PetRuneLog {
        private int rarity;
        private int runeLvl;
        private String runeName;

        public PetRuneLog(Rune rune) {
            if (rune == null) {
                return;
            }
            this.runeLvl = rune.getRuneLvl();

            PetRunePropertiesObject runeCfg = PetRuneProperties.getByRuneid(rune.getRuneBookId());
            if (runeCfg != null) {
                this.rarity = runeCfg.getRunerarity();
                this.runeName = runeCfg.getSevername();
            }
        }
    }
}
