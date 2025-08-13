package model.petfragment.entity;

import lombok.Data;
import protocol.Common.Reward;
import protocol.PetMessage;
import protocol.PetMessage.PetFragmentUse;
import protocol.RetCodeId.RetCodeEnum;

import java.util.List;

/**
 * @Description
 * @Author hanx
 * @Date2020/6/28 0028 15:16
 **/
@Data
public class FragmentUseResult {
    private RetCodeEnum codeEnum;
    private List<PetMessage.PetReward> gainPet;
    private List<PetFragmentUse> petFragmentUses;
}
