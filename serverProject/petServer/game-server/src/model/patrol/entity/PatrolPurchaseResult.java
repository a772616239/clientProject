package model.patrol.entity;

import entity.CommonResult;

/**
 * @author xiao_FL
 * @date 2019/8/27
 */
public class PatrolPurchaseResult extends CommonResult {
    /**
     * 物品购买数
     */
    private int itemCount;

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
