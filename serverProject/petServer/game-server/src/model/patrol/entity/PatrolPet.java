package model.patrol.entity;

import java.util.List;

/**
 * 巡逻队存储临时pet，不用处理所有状态、来源信息、一键装备类型
 */
public class PatrolPet {
    public PatrolPet() {
    }

    private String id;
    private int petBookId;
    private int petLvl;
    private int petRarity;
    private int petUpLvl;
    private List<PetProperty> petPropertyList;
    private long ability;
    private String gemId;

    public String getGemId() {
        return gemId;
    }

    public void setGemId(String gemId) {
        this.gemId = gemId;
    }

    public static class PetProperty {
        private int propertyType;
        private int propertyValue;

        PetProperty() {
        }

        public PetProperty(int petPropertyType, int petPropertyValue) {
            propertyType = petPropertyType;
            propertyValue = petPropertyValue;
        }

        public int getPropertyType() {
            return propertyType;
        }

        public void setPropertyType(int propertyType) {
            this.propertyType = propertyType;
        }

        public int getPropertyValue() {
            return propertyValue;
        }

        public void setPropertyValue(int propertyValue) {
            this.propertyValue = propertyValue;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPetBookId() {
        return petBookId;
    }

    public void setPetBookId(int petBookId) {
        this.petBookId = petBookId;
    }

    public int getPetLvl() {
        return petLvl;
    }

    public void setPetLvl(int petLvl) {
        this.petLvl = petLvl;
    }

    public int getPetRarity() {
        return petRarity;
    }

    public void setPetRarity(int petRarity) {
        this.petRarity = petRarity;
    }

    public int getPetUpLvl() {
        return petUpLvl;
    }

    public void setPetUpLvl(int petUpLvl) {
        this.petUpLvl = petUpLvl;
    }

    public List<PetProperty> getPetPropertyList() {
        return petPropertyList;
    }

    public void setPetPropertyList(List<PetProperty> petPropertyList) {
        this.petPropertyList = petPropertyList;
    }

    public long getAbility() {
        return ability;
    }

    public void setAbility(long ability) {
        this.ability = ability;
    }
}
