package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class PlayerVipLog extends AbstractPlayerLog {

    public PlayerVipLog(playerEntity player) {
        super(player);
    }
}
