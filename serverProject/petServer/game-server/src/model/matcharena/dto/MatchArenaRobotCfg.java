package model.matcharena.dto;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

@Data
public class MatchArenaRobotCfg implements Serializable {
    private static final long serialVersionUID = 8919183396239890942L;
    private int rarity;
    private int level;
    private Map<Integer, Integer> exProperty;
    private long expireTime;
}
