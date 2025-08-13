package model.crazyDuel.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import protocol.CrayzeDuel;

public class CrazyDuelPage implements Serializable {
    private static final long serialVersionUID = -5603335436429314463L;
    private Map<CrayzeDuel.CrazyDuelTeamType, List<String>> pageTop = new ConcurrentHashMap<>();
    private List<String> normalTeam = Collections.synchronizedList(new ArrayList<>());
    private List<String> alreadyBattle;

    public Map<CrayzeDuel.CrazyDuelTeamType, List<String>> getPageTop() {
        return pageTop;
    }

    public void setPageTop(Map<CrayzeDuel.CrazyDuelTeamType, List<String>> pageTop) {
        this.pageTop = pageTop;
    }

    public List<String> getNormalTeam() {
        return normalTeam;
    }

    public void setNormalTeam(List<String> normalTeam) {
        this.normalTeam = normalTeam;
    }

    public List<String> getAlreadyBattle() {
        return alreadyBattle;
    }

    public void setAlreadyBattle(List<String> alreadyBattle) {
        this.alreadyBattle = alreadyBattle;
    }
}
