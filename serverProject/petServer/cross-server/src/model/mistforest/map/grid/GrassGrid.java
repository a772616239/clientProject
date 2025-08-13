package model.mistforest.map.grid;

import model.mistforest.mistobj.MistFighter;
import protocol.MistForest.MistUnitPropTypeEnum;

public class GrassGrid extends Grid {
    private int grassGroup;

    public GrassGrid(int gridType) {
        super(gridType);
    }

    public int getGrassGroup() {
        return grassGroup;
    }

    public void setGrassGroup(int grassGroup) {
        this.grassGroup = grassGroup;
    }

    @Override
    public boolean onObjEnter(MistFighter fighter) {
        if (!super.onObjEnter(fighter)) {
            return false;
        }
        fighter.setAttribute(MistUnitPropTypeEnum.MUPT_GrassGroup_VALUE, getGrassGroup());
        fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_GrassGroup_VALUE, getGrassGroup());
        return true;
    }

    @Override
    public boolean onObjLeave(MistFighter fighter) {
        if (!super.onObjLeave(fighter)) {
            return false;
        }
        fighter.setAttribute(MistUnitPropTypeEnum.MUPT_GrassGroup_VALUE, 0);
        fighter.addAttributeChangeCmd(MistUnitPropTypeEnum.MUPT_GrassGroup_VALUE, 0);
        return true;
    }
}
