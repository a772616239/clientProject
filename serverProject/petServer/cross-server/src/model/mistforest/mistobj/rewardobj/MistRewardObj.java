package model.mistforest.mistobj.rewardobj;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import model.mistforest.mistobj.MistFighter;
import model.mistforest.mistobj.MistObject;
import model.mistforest.room.entity.MistRoom;
import org.apache.commons.collections4.CollectionUtils;
import protocol.MistForest.MistUnitPropTypeEnum;

public class MistRewardObj extends MistObject {
    protected Set<Long> qualifiedPlayers;

    public MistRewardObj(MistRoom room, int objType) {
        super(room, objType);
    }

    @Override
    public void clear() {
        super.clear();
        if (qualifiedPlayers != null) {
            qualifiedPlayers.clear();
        }
    }

    public void addQualifiedPlayers(Collection<Long> playerIds) {
        if (qualifiedPlayers == null) {
            qualifiedPlayers = new HashSet<>();
        }
        qualifiedPlayers.addAll(playerIds);
    }

    public void addQualifiedPlayer(long playerId) {
        if (qualifiedPlayers == null) {
            qualifiedPlayers = new HashSet<>();
        }
        qualifiedPlayers.add(playerId);
    }

    public boolean isQualifiedPlayer(MistFighter fighter) {
        if (CollectionUtils.isEmpty(qualifiedPlayers)) {
            return true;
        }
        return qualifiedPlayers.contains(fighter.getAttribute(MistUnitPropTypeEnum.MUPT_PlayerId_VALUE));
    }

    public boolean isQualifiedPlayer(long playerId) {
        if (CollectionUtils.isEmpty(qualifiedPlayers)) {
            return true;
        }
        return qualifiedPlayers.contains(playerId);
    }
}
