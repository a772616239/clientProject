package server.event;

import common.SyncExecuteFunction;
import model.obj.BaseObj;
import util.LogUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class EventTask implements Runnable {
    Event event;
    List<BaseObj> objList;

    protected EventTask() {
    }

    protected EventTask(BaseObj baseObj, Event event) {
        this.setParam(baseObj, event);
    }

    protected EventTask(List<BaseObj> baseObjList, Event event) {
        this.setParam(baseObjList, event);
    }

    public void setParam(BaseObj baseObj, Event event) {
        this.event = event;
        if (this.objList == null) {
            this.objList = new LinkedList();
        }
        this.objList.clear();
        this.objList.add(baseObj);
    }

    public void setParam(List<BaseObj> baseObjList, Event event) {
        this.event = event;
        if (this.objList == null) {
            this.objList = new LinkedList();
        }
        this.objList.clear();
        this.objList.addAll(baseObjList);
    }

    protected void clear() {
        this.event = null;
        if (this.objList != null) {
            this.objList.clear();
        }
    }

    @Override
    public void run() {
        try {
            if (this.event == null) {
                LogUtil.error("dispatch event is null");
                return;
            }
            long startTime = System.currentTimeMillis();
            if (this.objList == null || this.objList.isEmpty()) {
                LogUtil.error("dispatch event[" + this.event.getEventId() + "] objList is null");
                return;
            }
            BaseObj baseObj;
            Iterator<BaseObj> iter = this.objList.iterator();
            while (iter.hasNext()) {
                baseObj = iter.next();
                if (!SyncExecuteFunction.executePredicate(baseObj, obj -> EventManager.getInstance().onEvent(obj, this.event))) {
                    LogUtil.error("event[" + event.getEventId()
                            + "] handle failed targetId=" + baseObj.getBaseIdx());
                }
            }
            long costTime = System.currentTimeMillis() - startTime;
            LogUtil.debug("Event[" + this.event.getEventId() + "] costTime=" + costTime);
            if (costTime > 100) {
                LogUtil.warn("Event[" + this.event.getEventId() + "] costTime=" + costTime);
            }
        } catch (Exception e) {
            LogUtil.error("EventTask error, eventId={}", this.event.getEventId());
            LogUtil.printStackTrace(e);
        }
    }

    public static EventTask valueOf(BaseObj baseObj, Event event) {
        EventTask task = new EventTask(baseObj, event);
        return task;
    }

    public static EventTask valueOf(List<BaseObj> baseObjList, Event event) {
        EventTask task = new EventTask(baseObjList, event);
        return task;
    }
}
