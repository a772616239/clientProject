package model.stoneRift.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DbPlayerWorldMap implements Serializable {
    private static final long serialVersionUID = 6493352721722180050L;

    private String uniqueMapId;
    private List<String> alreadyGoMapId = new ArrayList<>();
    private int userFreeRefreshTime;
    private int buyRefreshTime;
    private int useStealTime;
    private int buyStealTime;


    public String getUniqueMapId() {
        return uniqueMapId;
    }

    public void setUniqueMapId(String uniqueMapId) {
        this.uniqueMapId = uniqueMapId;
    }

    public List<String> getAlreadyGoMapId() {
        return alreadyGoMapId;
    }

    public void setAlreadyGoMapId(List<String> alreadyGoMapId) {
        this.alreadyGoMapId = alreadyGoMapId;
    }

    public int getUserFreeRefreshTime() {
        return userFreeRefreshTime;
    }

    public void setUserFreeRefreshTime(int userFreeRefreshTime) {
        this.userFreeRefreshTime = userFreeRefreshTime;
    }

    public int getBuyRefreshTime() {
        return buyRefreshTime;
    }

    public void setBuyRefreshTime(int buyRefreshTime) {
        this.buyRefreshTime = buyRefreshTime;
    }

    public int getUseStealTime() {
        return useStealTime;
    }

    public void setUseStealTime(int useStealTime) {
        this.useStealTime = useStealTime;
    }

    public int getBuyStealTime() {
        return buyStealTime;
    }

    public void setBuyStealTime(int buyStealTime) {
        this.buyStealTime = buyStealTime;
    }
}
