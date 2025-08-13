package petrobot.system.mistForest.obj;

import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
@Setter
public class MistObjPool {
    protected ObjCreator objCreator;
    protected Queue<MistObj> objPool;

    public MistObjPool(ObjCreator objCreator) {
        this.objCreator = objCreator;
        this.objPool = new ConcurrentLinkedQueue<>();
    }

    public <T extends MistObj> T createObj(long id) {
        MistObj newObj = objPool.poll();
        if (newObj == null) {
            newObj = this.objCreator.createObj();
        }
        newObj.setId(id);
        return (T) newObj;
    }

    public void release(MistObj obj) {
        if (obj == null) {
            return;
        }
        objPool.add(obj);
    }
}
