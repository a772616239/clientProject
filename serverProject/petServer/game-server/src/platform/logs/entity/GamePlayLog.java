package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.util.PlayerUtil;
import platform.logs.AbstractPlayerLog;
import protocol.Common.EnumFunction;

@Getter
@Setter
@NoArgsConstructor
public class GamePlayLog extends AbstractPlayerLog {
    /**
     * 格式  枚举值-枚举特殊参数值
     */
    private String gamePlayNum;
    private int playerLv;
    private int count;

    public GamePlayLog(String playerIdx, EnumFunction function) {
        this(playerIdx, function, 1, 0);
    }

    public GamePlayLog(String playerIdx, EnumFunction function, int param) {
        this(playerIdx, function, 1, param);
    }

    /**
     * 在boss塔中表示难度值
     * @param playerIdx
     * @param function
     * @param count
     * @param param
     */
    public GamePlayLog(String playerIdx, EnumFunction function, int count, int param) {
        super(playerIdx);
        if (function != null) {
            this.gamePlayNum = String.valueOf(function.getNumber());
        }

        if (param > 0) {
            this.gamePlayNum = this.gamePlayNum + "-" +  param;
        }

        this.playerLv = PlayerUtil.queryPlayerLv(playerIdx);
        this.count = count;
    }
}
