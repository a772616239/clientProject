package model.pet.entity;

import protocol.PetMessage.Pet;
import entity.CommonResult;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/7/25
 */
public class PetAddResult extends CommonResult {
    private List<Pet> result;

    public List<Pet> getResult() {
        return result;
    }

    public void setResult(List<Pet> result) {
        this.result = result;
    }
}
