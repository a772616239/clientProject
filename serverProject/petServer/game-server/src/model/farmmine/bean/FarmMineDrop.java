package model.farmmine.bean;

import protocol.Common;

import java.util.ArrayList;
import java.util.List;

public class FarmMineDrop {
    public Common.Reward base;
    private long baseTime;
    private long leijiTime;
    private long gailvTime;
    private boolean isGailv = false;
    private List<Common.Reward> gailv = new ArrayList<>();
    private Common.Reward steals;

    public Common.Reward getBase() {
        return base;
    }

    public void setBase(Common.Reward base) {
        this.base = base;
    }

    public long getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(long baseTime) {
        this.baseTime = baseTime;
    }

    public long getLeijiTime() {
        return leijiTime;
    }

    public void setLeijiTime(long leijiTime) {
        this.leijiTime = leijiTime;
    }

    public long getGailvTime() {
        return gailvTime;
    }

    public void setGailvTime(long gailvTime) {
        this.gailvTime = gailvTime;
    }

    public boolean isGailv() {
        return isGailv;
    }

    public void setGailv(boolean gailv) {
        isGailv = gailv;
    }

    public List<Common.Reward> getGailv() {
        return gailv;
    }

    public void setGailv(List<Common.Reward> gailv) {
        this.gailv = gailv;
    }

    public Common.Reward getSteals() {
        return steals;
    }

    public void setSteals(Common.Reward steals) {
        this.steals = steals;
    }
}
