package model.mistforest.task;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import protocol.MistForest.EnumNpcTaskState;

@Getter
@Setter
@ToString
public class MistTaskEntity {
    protected int cfgId;
    protected int taskProgress;
    protected int taskState;
    protected long expireTime;

    public  MistTaskEntity(int cfgId, long expireTime) {
        this.cfgId = cfgId;
        this.taskState = EnumNpcTaskState.ENTS_NotFinish_VALUE;
        this.expireTime = expireTime;
    }
}
