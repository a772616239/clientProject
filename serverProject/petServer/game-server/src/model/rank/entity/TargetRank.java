package model.rank.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 排行榜成就项
 */
@Data
@AllArgsConstructor
public class TargetRank implements Serializable {
    private static final long serialVersionUID = 1109957367654812743L;

    /**
     * 玩家id
     */
    private String playerId;

    /**
     * 目标id
     */
    private int targetId;
}
