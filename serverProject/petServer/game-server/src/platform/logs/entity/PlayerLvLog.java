package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class PlayerLvLog extends AbstractPlayerLog {
    private int playerLv;
    private int beforeLv;

    public PlayerLvLog(playerEntity player, int beforeLv) {
        super(player);
        this.beforeLv = beforeLv;
        if (player == null) {
            return;
        }
        this.playerLv = player.getLevel();
    }
}
