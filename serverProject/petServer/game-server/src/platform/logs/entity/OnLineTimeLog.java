package platform.logs.entity;

import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class OnLineTimeLog extends AbstractPlayerLog {
    private long timeLength;

    public OnLineTimeLog(String playerIdx) {
        this(playerCache.getByIdx(playerIdx));
    }

    public OnLineTimeLog(playerEntity entity) {
        super(entity);
        if (entity == null) {
            return;
        }
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        SyncExecuteFunction.executeConsumer(entity, e -> {
            long lastSettleTime = entity.getDb_data().getLastSettleOnlineTime();
            if (lastSettleTime > 0) {
                this.timeLength = Math.max(0, currentTime - lastSettleTime);
            }
            entity.getDb_data().setLastSettleOnlineTime(currentTime);
        });
    }
}
