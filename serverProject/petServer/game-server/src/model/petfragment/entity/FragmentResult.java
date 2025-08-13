package model.petfragment.entity;

import entity.CommonResult;

/**
 * @author xiao_FL
 * @date 2019/7/23
 */
public class FragmentResult extends CommonResult {
    private int fragmentCount;

    public int getFragmentCount() {
        return fragmentCount;
    }

    public void setFragmentCount(int fragmentCount) {
        this.fragmentCount = fragmentCount;
    }
}
