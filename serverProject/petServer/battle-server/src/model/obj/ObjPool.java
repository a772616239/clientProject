package model.obj;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ObjPool<T> {
    CreateObj<T> createObj;
    Queue<T> objQueue;

    public ObjPool(CreateObj createObj) {
        this(createObj, 0);
    }

    public ObjPool(CreateObj createObj, int count) {
        this.createObj = createObj;
        this.objQueue = new ConcurrentLinkedQueue<T>();
        for (int i = 0; i < count; ++i) {
            T newObj = this.createObj.createObj();
            this.objQueue.add(newObj);
        }
    }

    public T create() {
        T newObj = this.objQueue.poll();
        if (newObj == null) {
            newObj = this.createObj.createObj();
        }
        return newObj;
    }

    public void release(T obj) {
        if (obj != null) {
            this.objQueue.add(obj);
        }
    }
}
