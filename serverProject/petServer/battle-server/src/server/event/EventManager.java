package server.event;

import common.GlobalThread;
import common.load.ServerConfig;
import model.obj.BaseObj;
import server.event.crossarena.CrossArenaEventAbstractCommand;
import server.event.leitai.LeitaiEventAbstractCommand;
import util.LogUtil;

import java.util.HashMap;

public class EventManager {
    private static EventManager instance = new EventManager();
    protected HashMap<Integer, EventHandler> handlers = new HashMap<>();

    public static EventManager getInstance() {
        return instance;
    }

    /**
     * 游戏逻辑线程池
     */
    public static final OrderedQueuePoolExecutor LEITAI_EXECUTOR = new OrderedQueuePoolExecutor("擂台赛逻辑", 2, 5000);


    public boolean listenEvent(int eventId, EventHandler handler) {
        if (handlers.containsKey(eventId)) {
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
            LogUtil.debug("handle onEvent eventid="+event.getEventId()+",targetId="+baseEntity.getIdx());
            int eventId = event.getEventId();
            EventHandler handler = handlers.get(eventId);
            if (handler != null) {
                return handler.onEvent(baseEntity, event);
            }
        }
        LogUtil.error(" handle onEnvet invalid param");
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
                if (!srcObj.getIdx().equals("system") && srcObj.getIdx().equals(targetObj.getIdx())
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

    public void dealLeitaiEvent(LeitaiEventAbstractCommand event, int leitaiId) {
        event.setLeitaiId(leitaiId);
        LEITAI_EXECUTOR.addTask(Long.valueOf(leitaiId), event);
    }

    public void dealCrossArenaEvent(CrossArenaEventAbstractCommand event, int tableId) {
        event.setTableId(tableId);
        LEITAI_EXECUTOR.addTask(Long.valueOf(tableId), event);
    }

}
