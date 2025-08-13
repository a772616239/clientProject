package http.entity;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/12/2
 */
public class PlatformApposeAddition {
    /**
     * index同一活动内不能重复
     */
    private int index;
    private int type;
    private int count;
    /**
     * 满足所有条件
     */
    private List<PlatformAddition> addition;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<PlatformAddition> getAddition() {
        return addition;
    }

    public void setAddition(List<PlatformAddition> addition) {
        this.addition = addition;
    }
}
