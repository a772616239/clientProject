package model.mistforest.map.AStar;

import protocol.MistForest.ProtoVector;

public class Coord {
    public int x;
    public int y;
    public ProtoVector.Builder toward;

    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
        this.toward = ProtoVector.newBuilder();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Coord) {
            Coord c = (Coord) obj;
            return x == c.x && y == c.y;
        } else if (obj instanceof ProtoVector) {
            ProtoVector pos = (ProtoVector) obj;
            return x == pos.getX() && y == pos.getY();
        } else if (obj instanceof ProtoVector.Builder) {
            ProtoVector.Builder posBuilder = (ProtoVector.Builder) obj;
            return x == posBuilder.getX() && y == posBuilder.getY();
        }
        return false;
    }
}
