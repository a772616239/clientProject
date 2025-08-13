package util;

import common.GameConst.EventType;
import common.entity.RankingUpdateRequest;
import java.util.List;
import model.thewar.warroom.entity.WarRoom;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import server.event.Event;
import server.event.EventManager;

/**
 * @author huhan
 * @date 2020/05/28
 */
public class EventUtil {

    public static void updateRanking(RankingUpdateRequest updateRequest) {
        if (updateRequest == null) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_RANKING_UPDATE, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        event.pushParam(updateRequest);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void clearRanking(String rankingName, List<String> keys) {
        if (StringUtils.isBlank(rankingName) || CollectionUtils.isEmpty(keys)) {
            return;
        }

        Event event = Event.valueOf(EventType.ET_RANKING_CLEAR, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        event.pushParam(rankingName, keys);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void queryArenaRoomRanking(List<String> roomIdList) {
        if (CollectionUtils.isEmpty(roomIdList)) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_QUERY_ARENA_ROOM_RANKING, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        event.pushParam(roomIdList);
        EventManager.getInstance().dispatchEvent(event);
    }

    public static void initWarRoom(WarRoom room) {
        if (room == null) {
            return;
        }
        Event event = Event.valueOf(EventType.ET_TheWar_InitWarRoom, GameUtil.getDefaultEventSource(), GameUtil.getDefaultEventSource());
        event.pushParam(room);
        EventManager.getInstance().dispatchEvent(event);
    }
}
