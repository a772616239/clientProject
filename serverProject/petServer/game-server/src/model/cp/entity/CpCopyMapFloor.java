package model.cp.entity;

import java.io.Serializable;
import java.util.*;

import lombok.Data;

public class CpCopyMapFloor implements Serializable {
    private static final long serialVersionUID = -8267823049069491256L;
    private int floor;
    private Map<Integer, CpCopyMapPoint> points = new LinkedHashMap<>();

    public void addPoint(CpCopyMapPoint point) {
        if (point != null) {
            this.points.put(point.getId(), point);
        }
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public Map<Integer, CpCopyMapPoint> getPoints() {
        return points;
    }

    public void setPoints(Map<Integer, CpCopyMapPoint> points) {
        this.points = points;
    }
}
