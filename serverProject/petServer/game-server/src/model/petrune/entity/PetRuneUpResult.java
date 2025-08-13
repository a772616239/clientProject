package model.petrune.entity;

import entity.CommonResult;
import lombok.Getter;
import lombok.Setter;
import protocol.Common.Consume;
import protocol.PetMessage;
import protocol.PetMessage.Rune;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2020/1/15
 */
@Getter
@Setter
public class PetRuneUpResult extends CommonResult {
    private Rune.Builder rune;
    private List<PetMessage.RuneExp> convertRuneExp;
    private List<Consume> consume;
}
