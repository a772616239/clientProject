package model.patrol.entity;

import lombok.Getter;
import lombok.Setter;
import protocol.PetMessage;
import protocol.PetMessage.Rune;

import java.util.List;
import java.util.Map;

/**
 * @author xiao_FL
 * @date 2019/10/30
 */
@Getter
@Setter
public class PatrolMapInitResult {
    /**
     * 起始点
     */
    private PatrolTree initPoint;

    /**
     * 地图编号
     */
    private int mapId;

    /**
     * 玩家预读信息
     */
    List<Rune> runeList;

    private List<protocol.PlayerBase.SimpleArtifact> artifacts;

    private Map<Integer,Integer> artifactAddition;

    private List<Integer> titleIds ;

}
