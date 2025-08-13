package util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Description
 * @Author hanx
 * @Date2020/8/6 0006 15:05
 **/
public class TagWrapper {
    private String expression;
    private List<TagWrapper> wrappers = new ArrayList<>();
    private String tag;
    private int deep;
    public TagWrapper() {
        this.deep = 1;
    }
    private TagWrapper(int deep) {
        this.deep = deep;
    }
    /**
     * and和or操作符的最大操作数：10
     */
    private static final int OPERATE_NUM = 10;
    /**
     * and和or操作符的最大嵌套层数：2
     */
    private static final int NEST_NUM = 2;
    /**
     * 且操作，同对象只能操作 @see(OPERATE_NUM) 次
     *
     * @param consumer
     * @return
     */
    public TagWrapper and(Consumer<TagWrapper> consumer) {
        //嵌套层数限制
        if (this.deep > NEST_NUM) {
            return this;
        }
        return doCond(consumer, Operate.AND);
    }
    /**
     * 或操作 同对象只能操作 @see(OPERATE_NUM) 次
     *
     * @param consumer
     * @return
     */
    public TagWrapper or(Consumer<TagWrapper> consumer) {
        //嵌套层数限制
        if (this.deep > NEST_NUM) {
            return this;
        }
        return doCond(consumer, Operate.OR);
    }
    /**
     * 非操作,同级只有最新的有效
     *
     * @param consumer
     */
    public void not(Consumer<TagWrapper> consumer) {
        doCond(consumer, Operate.NOT);
    }
    /**
     * 设置标签
     *
     * @param tag
     */
    public void tag(String tag) {
        if (!(StringUtils.isEmpty(expression) || Operate.TAG.match(expression))) {
            return;
        }
        expression = Operate.TAG.value();
        this.tag = tag;
    }
    public TagWrapper doCond(Consumer<TagWrapper> consumer, Operate operate) {
        if (!(StringUtils.isEmpty(expression) || operate.match(expression))) {
            return this;
        }
        if (wrappers.size() > OPERATE_NUM) {
            return this;
        }
        this.expression = operate.value();
        TagWrapper instance = instance();
        consumer.accept(instance);
        wrappers.add(instance);
        return this;
    }
    private JSONObject build() {
        JSONObject root = new JSONObject();
        if (Operate.TAG.match(expression)) {
            root.put(expression, tag);
        } else if (Operate.AND.match(expression)
                || Operate.OR.match(expression)) {
            JSONArray array = new JSONArray();
            for (TagWrapper wrapper : wrappers) {
                if (wrapper.build().size() > 0) {
                    array.add(wrapper.build());
                }
            }
            root.put(expression, array);
        } else if (Operate.NOT.match(expression)) {
            TagWrapper wrapper = wrappers.get(wrappers.size() - 1);
            if (wrapper.build().size() > 0) {
                root.put(expression, wrapper.build());
            }
        }
        return root;
    }
    public String buildJsonString() {
        return build().toJSONString();
    }
    public static void main(String[] args) {
        //表达式将筛选出标签符合”男性、非90后、活跃或非国外“的用户
        TagWrapper tagWrapper = new TagWrapper()
                .and(and -> {
                    and.tag("男性");
                })
                .and(and -> {
                    and.not(not -> {
                        not.tag("90后");
                    });
                })
                .and(and -> {
                    and.or(or -> {
                        or.not(not -> {
                            not.tag("国外");
                        });
                    }).or(or -> {
                        or.tag("活跃");
                    });
                });
        System.out.println(tagWrapper.buildJsonString());
    }
    private TagWrapper instance() {
        return new TagWrapper(this.deep + 1);
    }
    public enum Operate {
        AND("and"),
        OR("or"),
        NOT("not"),
        TAG("tag");
        private String operate;
        Operate(String operate) {
            this.operate = operate;
        }
        public boolean match(String operate) {
            return this.operate.equals(operate);
        }
        public String value() {
            return operate;
        }
    }
}
