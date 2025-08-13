package server.event;

import common.tick.GlobalTick;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.obj.BaseObj;
import util.LogUtil;

public class Event {
    private int eventId;
    private long time;
    private BaseObj target;
    private BaseObj source;
    private List<Object> params;

    protected Event() {
    }

    protected Event(int eventId) {
        this.eventId = eventId;
        this.setTime(GlobalTick.getInstance().getCurrentTime());
    }

    public int getEventId() {
        return this.eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public Event setTarget(BaseObj objId) {
        this.target = objId;
        return this;
    }

    public BaseObj getTarget() {
        return this.target;
    }

    public Event setSource(BaseObj objId) {
        this.source = objId;
        return this;
    }

    public BaseObj getSource() {
        return this.source;
    }

    public long getTime() {
        return this.time;
    }

    public Event setTime(long time) {
        this.time = time;
        return this;
    }

    public boolean isValid() {
        return this.eventId > 0;
    }

    public Event pushParam(Object... params) {
        if (this.params == null) {
            this.params = new ArrayList(params.length);
        }
        this.params.addAll(Arrays.asList(params));
        return this;
    }

    public <T> T getParam(int idx) {
        try {
            return this.params != null ? (T) (this.params.get(idx)) : null;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }

    }

    public List<Object> getParams() {
        return this.params;
    }

    public boolean checkParamSize(int size) {
        return this.params != null && this.params.size() >= size;
    }

//    public static Event valueOf(int eventId) {
//        Event event = new Event(eventId);
//        return event;
//    }

//    @Deprecated
//    public static Event valueOf(int eventId, BaseObj source) {
//        Event event = new Event(eventId);
//        event.setSource(source);
//        event.setTarget(source);
//        return event;
//    }

    /**
     * 如果没有sourceEntity,请使用util.Game#getDefaultEventSource()传递一个空的src
     *
     * @param eventId
     * @param source
     * @param target
     * @return
     */
    public static Event valueOf(int eventId, BaseObj source, BaseObj target) {
        Event event = new Event(eventId);
        event.setSource(source);
        event.setTarget(target);
        return event;
    }
}
