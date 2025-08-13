package platform.logs.entity.thewar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class TheWarCurrencyLog extends AbstractPlayerLog {
    private String mapName;
    private int roleLevel;
    private boolean consume;
    private String currencyType;
    private int beforeCount;
    private int amount;
    private String reason;

    public TheWarCurrencyLog(playerEntity player, String mapName, boolean consume, String currencyType, int beforeAmount, int amount, String reason) {
        super(player);
        setMapName(mapName);
        setRoleLevel(player.getLevel());
        setConsume(consume);
        setCurrencyType(currencyType);
        setBeforeCount(beforeAmount);
        setAmount(amount);
        setReason(reason);
    }
}
