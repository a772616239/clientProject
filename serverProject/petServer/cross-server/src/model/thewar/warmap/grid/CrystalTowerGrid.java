package model.thewar.warmap.grid;

import common.GameConst.EventType;
import model.thewar.warmap.WarMapData;
import model.thewar.warplayer.entity.WarPlayer;
import protocol.TargetSystem.TargetTypeEnum;
import protocol.TheWarDefine.Position;
import protocol.TheWarDefine.TheWarCellPropertyEnum;
import protocol.TheWarDefine.TheWarRetCode;
import server.event.Event;
import server.event.EventManager;

public class CrystalTowerGrid extends WarMapGrid {

    public TheWarRetCode submitDpResource(WarMapData warMap, WarPlayer warPlayer, int count) {
        if (isBlock()) {
            return TheWarRetCode.TWRC_GridIsBlock; // 阻挡格子
        }
        if (count <= 0) {
            return TheWarRetCode.TWRC_SubmitDpCountError; // 提交的开门点数错误
        }
        long curResource = getPropValue(TheWarCellPropertyEnum.TWCP_CrTowerLoadValue_VALUE);
        long maxResource = getPropValue(TheWarCellPropertyEnum.TWCP_CrTowerLoadMaxValue_VALUE);
        long exchangeGdFactor = getPropValue(TheWarCellPropertyEnum.TWCP_CrTowerWGExchangeFactor_VALUE);
        long exchangeHwFactor = getPropValue(TheWarCellPropertyEnum.TWCP_HolyWaterExchangeFactor_VALUE);
        if (maxResource - curResource <= 0) {
            return TheWarRetCode.TWRC_CristalGridDpIsFull; // 水晶格子开门资源点已满
        }
        long subMitCount = Math.min(count, (maxResource - curResource));
        if (warPlayer.getPlayerData().getWarDP() < subMitCount) {
            return TheWarRetCode.TWRC_WarDpNotEnough; // 开门资源点不足
        }
        long exchangeGold = subMitCount * exchangeGdFactor / 1000;
        long exchangeHolyWater = subMitCount * exchangeHwFactor / 1000;
        Event event = Event.valueOf(EventType.ET_TheWar_RemoveDpResource, this, warPlayer);
        event.pushParam((int) subMitCount, (int) exchangeGold, (int) exchangeHolyWater);
        EventManager.getInstance().dispatchEvent(event);

        long targetPortalPos = getPropValue(TheWarCellPropertyEnum.TWCP_CrTowerHostPortalPos_VALUE);
        Position.Builder portalPos = Position.newBuilder();
        portalPos.setX((int) (targetPortalPos >>> 32)).setY((int) targetPortalPos);
        WarMapGrid portalGrid = warMap.getMapGridByPos(portalPos.build());
        if (portalGrid instanceof PortalGrid && portalGrid.getPropValue(TheWarCellPropertyEnum.TWCP_PortalEnable_VALUE) <= 0) {
            Event portalEvent = Event.valueOf(EventType.ET_TheWar_AddPortalGridProgress, this, portalGrid);
            portalEvent.pushParam((int) subMitCount);
            EventManager.getInstance().dispatchEvent(portalEvent);
        }

        Event addTargetProgEvent = Event.valueOf(EventType.ET_TheWar_AddTargetProgress, this, warPlayer);
        addTargetProgEvent.pushParam(TargetTypeEnum.TTE_TheWar_CumuSubmitDp, 0, (int) subMitCount);
        EventManager.getInstance().dispatchEvent(addTargetProgEvent);

        setPropValue(TheWarCellPropertyEnum.TWCP_CrTowerLoadValue_VALUE, curResource + subMitCount);

        broadcastPropData();
        return TheWarRetCode.TWRC_Success;
    }

}
