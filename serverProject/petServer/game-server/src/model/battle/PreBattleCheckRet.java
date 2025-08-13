package model.battle;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PreBattleCheckRet {
    /**
     * 是否需要检查(gm胜利,新手地图等),  无需检查
     */
    public boolean NoNeedToCheck;
    public boolean Cheated;
    /**
     * 必须进行校验
     */
    public boolean MustCheck;
}
