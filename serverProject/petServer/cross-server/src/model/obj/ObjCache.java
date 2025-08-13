package model.obj;

import datatool.StringHelper;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import util.LogUtil;

public class ObjCache<T extends BaseObj> {
    protected Map<String, T> objMap = new ConcurrentHashMap<>();
    protected ObjPool<T> objPool = null;

    public void setObjPool(ObjPool<T> pool) {
        this.objPool = pool;
    }

    public void clearObjMap() {
        this.objMap.clear();
    }

    public T lockObject(String id) {
        T obj = this.queryObject(id);
        if (obj != null) {
            obj.lockObj();
            return obj;
        } else {
            return null;
        }
    }

    public boolean unlockObject(String id) {
        T obj = this.queryObject(id);
        if (obj != null) {
            obj.unlockObj();
            return true;
        } else {
            return false;
        }
    }

    public T queryObject(String id) {
        if (StringHelper.isNull(id)) {
            return null;
        }
        return this.objMap.get(id);
    }

    public T createObject() {
        if (this.objPool != null) {
            T obj = this.objPool.create();
            if (obj != null) {
                return obj;
            }
        }

        LogUtil.error("create obj failed, ObjPool is null");
        return null;
    }

    public T createObject(String id) {
        if (this.objPool != null) {
            T obj = this.objPool.create();
            if (obj != null) {
                obj.setIdx(id);
                return obj;
            }
        }

        LogUtil.error("create obj failed, ObjPool is null, id : " + id);
        return null;
    }

    public boolean manageObject(T obj) {
        try {
            if (obj == null || obj.getIdx() == null) {
                return false;
            }
            this.objMap.put(obj.getIdx(), obj);
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

    public boolean manageObjectList(Collection<T> objList) {
        try {
            if (objList == null) {
                return false;
            }
            for (T obj : objList) {
                if (obj.getIdx() != null) {
                    this.objMap.put(obj.getIdx(), obj);
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return false;
        }
    }

    public T createAndManageObject(String id) {
        T obj = createObject(id);
        if (obj != null) {
            this.objMap.put(id, obj);
            return obj;
        }
        LogUtil.error("createAndManageObj failed, Obj is null, id : " + id);
        return null;
    }

    public boolean addObject(T obj) {
        if (obj != null) {
            this.objMap.put(obj.getIdx(), obj);
            return true;
        } else {
            LogUtil.error("add obj failed, obj is null");
            return false;
        }
    }

    public boolean removeObject(T obj) {
        if (this.objMap.remove(obj.getIdx()) != null) {
            this.objPool.release(obj);
        }
        return true;
    }

    public int collectObjKey(Collection<String> keys) {
        Iterator iter = this.objMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, BaseObj> entry = (Map.Entry) iter.next();
            keys.add(entry.getKey());
        }
        return keys.size();
    }

    public int collectObj(Collection<T> objs) {
        Iterator iter = this.objMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long, T> entry = (Map.Entry) iter.next();
            objs.add(entry.getValue());
        }
        return objs.size();
    }

    public int getObjCount() {
        return this.objMap.size();
    }
}
