package petrobot.system.mistForest.map.AStar;

public class Node {

    public Coord coord;
    public int G; // 起点到当前结点的代价
    public int H; //当前结点到目的结点的估计代价
    public Node parent;

    public Node(int x, int y) {
        this.coord = new Coord(x, y);
    }

    public Node(Coord coord, int G, int H, Node parent) {
        this.coord = coord;
        this.G = G;
        this.H = H;
        this.parent = parent;
    }
}
