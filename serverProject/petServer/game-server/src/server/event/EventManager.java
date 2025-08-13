package server.event;

import common.GlobalThread;
import common.load.ServerConfig;
import java.util.HashMap;
import model.obj.BaseObj;
import util.LogUtil;

public class EventManager {
    private static EventManager instance = new EventManager();
    protected HashMap<Integer, EventHandler> handlers = new HashMap<>();

    public static EventManager getInstance() {
        return instance;
    }

    public boolean listenEvent(int eventId, EventHandler handler) {
        if (handlers.containsKey(eventId)) {
            LogUtil.error("repeated add the same eventId handler, eventId=" + eventId);
            return false;
        }
        handlers.put(eventId, handler);
        return true;
    }

    public boolean init() {
        return EventListener.listenEvent();
    }

    public boolean onEvent(BaseObj baseEntity, Event event) {
        if (baseEntity != null && event != null) {
            int eventId = event.getEventId();
            EventHandler handler = handlers.get(eventId);
            if (handler != null) {
                return handler.onEvent(baseEntity, event);
            }
        }
        return false;
    }

    public boolean dispatchEvent(Event event) {
        if (event != null) {
            BaseObj srcObj = event.getSource();
            BaseObj targetObj = event.getTarget();
            if (srcObj == null) {
                LogUtil.error(String.format("cannot process event:%d, event source is null", event.getEventId()));
                return false;
            } else if (targetObj == null) {
                LogUtil.error(String.format("cannot process event:%d, event target is null", event.getEventId()));
                return false;
            } else {
                if (!srcObj.getBaseIdx().equals("system") && srcObj.getBaseIdx().equals(targetObj.getBaseIdx())
                        && srcObj.getClassType().equals(targetObj.getClassType())) {
                    EventManager.getInstance().onEvent(event.getSource(), event);
                } else {
                    if (GlobalThread.getInstance().getExecutor() == null) {
                        LogUtil.error(String.format("executor is null,cannot process event:%d,event target:%s",
                                event.getEventId(), event.getTarget().toString()));
                        return false;
                    }
                    if (isSingleThread()) {
                        EventManager.getInstance().onEvent(event.getTarget(), event);
                    } else {
                        GlobalThread.getInstance().getExecutor().execute(EventTask.valueOf(event.getTarget(), event));
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean isSingleThread() {
        return ServerConfig.getInstance().getThreadCount() <= 1;
    }
}
