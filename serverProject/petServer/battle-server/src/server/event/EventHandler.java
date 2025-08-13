package server.event;


import model.obj.BaseObj;

public interface EventHandler {
    boolean onEvent(BaseObj obj, Event event);
}
