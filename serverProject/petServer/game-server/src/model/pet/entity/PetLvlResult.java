package model.pet.entity;

import protocol.Common.Consume;
import protocol.PetMessage.Pet;
import entity.CommonResult;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/11/21
 */
public class PetLvlResult extends CommonResult {
    private String petId;

    private int upType;

    private List<Consume> consumes;

    private List<Pet> consumePets;

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public int getUpType() {
        return upType;
    }

    public void setUpType(int upType) {
        this.upType = upType;
    }

    public List<Consume> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<Consume> consumes) {
        this.consumes = consumes;
    }

    public List<Pet> getConsumePets() {
        return consumePets;
    }

    public void setConsumePets(List<Pet> consumePets) {
        this.consumePets = consumePets;
    }
}
