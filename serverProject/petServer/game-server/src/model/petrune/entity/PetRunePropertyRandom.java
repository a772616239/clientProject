package model.petrune.entity;

/**
 * 获得随机符文属性实体
 *
 * @author xiao_FL
 * @date 2019/6/3
 */
public class PetRunePropertyRandom {
    /**
     * 属性种类
     */
    private int propertyType;

    /**
     * 属性值
     */
    private int propertyValue;

    public int getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(int propertyType) {
        this.propertyType = propertyType;
    }

    public int getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(int propertyValue) {
        this.propertyValue = propertyValue;
    }
}
