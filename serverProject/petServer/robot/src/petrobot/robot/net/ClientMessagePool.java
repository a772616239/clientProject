package petrobot.robot.net;

import clazz.subClassUtil;
import hyzNet.message.AbstractHandler;
import hyzNet.message.IMessagePool;
import hyzNet.message.MsgId;
import petrobot.util.LogUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClientMessagePool implements IMessagePool {
    private final Map<Integer, Class<?>> handlers = new HashMap<Integer, Class<?>>();

    public ClientMessagePool(String packageName) {
        //登录注册
        Set<Class<AbstractHandler>> ret = subClassUtil.getSubClasses(packageName, AbstractHandler.class);
        for (Class<AbstractHandler> handlerClass : ret) {
            try {
                MsgId msgId = handlerClass.getAnnotation(MsgId.class);
                if (msgId != null) {
                    register(msgId.msgId(), handlerClass);
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }
    }

    @Override
    public AbstractHandler<?> getHandler(int messageId) {
        Class<?> clazz = handlers.get(messageId);
        if (clazz != null) {
            try {
                return (AbstractHandler<?>) clazz.newInstance();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void register(int messageId, Class<? extends AbstractHandler> handlerClazz) {
        try {
            handlers.put(messageId, handlerClazz);
        } catch (Exception e) {
            throw new RuntimeException("消息注册错误....");
        }
    }
}
