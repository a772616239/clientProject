package model.thewar.warmap.grid;

import protocol.TheWarDefine.TheWarCellPropertyEnum;

public class PortalGrid extends WarMapGrid {

    public void addOpenProgress(int count) {
        if (count <= 0) {
            return;
        }
        if (getPropValue(TheWarCellPropertyEnum.TWCP_PortalEnable_VALUE) > 0) {
            return;
        }
        long curVal = getPropValue(TheWarCellPropertyEnum.TWCP_CrTowerLoadValue_VALUE);
        long maxVal = getPropValue(TheWarCellPropertyEnum.TWCP_CrTowerLoadMaxValue_VALUE);
        curVal = Math.min(maxVal, curVal + count);
        setPropValue(TheWarCellPropertyEnum.TWCP_CrTowerLoadValue_VALUE, curVal);
        if (curVal >= maxVal) {
            setPropValue(TheWarCellPropertyEnum.TWCP_PortalEnable_VALUE, 1);
        }
        broadcastPropData();
    }
}
