package petrobot.system.mistForest.map.AStar;

import petrobot.system.mistForest.MistConst;
import petrobot.system.mistForest.map.grid.Grid;
import petrobot.util.LogUtil;
import protocol.MistForest.ProtoVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Astar {
    public final static int DIRECT_VALUE = 10; // 横移代价
    public final static int OBLIQUE_VALUE = 14; // 斜移代价
    public final static int MAX_PATH_SIZE = 50 * 50; // 最大路径长度
    List<Node> openList = new LinkedList<>(); // 优先队列(升序)
    List<Node> closeList = new ArrayList<>();

    public void clear() {
        openList.clear();
        closeList.clear();
    }

    public boolean prepareFindPath(Grid[][] mapData, int weight, int height, Node start, Node end) {
        if (mapData == null || weight <= 0 || height <= 0) {
            return false;
        }
        if (start == null || end == null) {
            return false;
        }
        openList.clear();
        closeList.clear();
        openList.add(start);
        return true;
    }

    public List<Coord> findPath(Grid[][] mapData, int weight, int height, Node start, Node end) {
        if (!prepareFindPath(mapData, weight, height, start, end)) {
            return null;
        }
        while (!openList.isEmpty()) {
            if (closeList.size() >= MAX_PATH_SIZE) {
                clear();
                break;
            }
            if (isCoordInCloseList(end.coord)) {
                break;
            }
            Node current = openList.get(0);
            openList.remove(0);
            closeList.add(current);
            addNeighborNodeInOpen(mapData, weight, height, end, current);
            openList.sort((Node node1, Node node2) -> (node1.G + node1.H) - (node2.G + node2.H));
        }
        return drawPath(end);
    }

    protected boolean isCoordInCloseList(int x, int y) {
        if (closeList.isEmpty()) {
            return false;
        }
        for (Node node : closeList) {
            if (node.coord.x == x && node.coord.y == y) {
                return true;
            }
        }
        return false;
    }

    protected boolean isCoordInCloseList(Coord coord) {
        if (coord == null) {
            return false;
        }
        return isCoordInCloseList(coord.x, coord.y);
    }

    protected boolean canNeighNodeAchieve(Grid[][] mapData, int weight, int height, Node current, int deltaX, int deltaY) {
        if (deltaX == 0 || deltaY == 0) {
            return true;
        }
        int currentX = current.coord.x;
        int currentY = current.coord.y;
        if (currentX < 0 || currentX >= weight || currentY < 0 || currentY >= height) {
            return false;
        }
        int checkX = currentX + deltaX;
        int checkY = currentY + deltaY;
        if (checkX < 0 || checkX >= weight || checkY < 0 || checkY >= height) {
            return false;
        }
        if (mapData[currentX][checkY].isBlocked() && mapData[checkX][currentY].isBlocked()) {
            return false;
        }
        return true;
    }

    protected void addNeighborNodeInOpen(Grid[][] mapData, int weight, int height, Node end, Node current) {
        int x = current.coord.x;
        int y = current.coord.y;
        addNeighborNodeInOpen(mapData, weight, height, end, current, x - 1, y, DIRECT_VALUE);
        addNeighborNodeInOpen(mapData, weight, height, end, current, x, y - 1, DIRECT_VALUE);
        addNeighborNodeInOpen(mapData, weight, height, end, current, x + 1, y, DIRECT_VALUE);
        addNeighborNodeInOpen(mapData, weight, height, end, current, x, y + 1, DIRECT_VALUE);
        if (canNeighNodeAchieve(mapData, weight, height, current, -1, -1)) {
            addNeighborNodeInOpen(mapData, weight, height, end, current, x - 1, y - 1, OBLIQUE_VALUE);
        }
        if (canNeighNodeAchieve(mapData, weight, height, current, 1, -1)) {
            addNeighborNodeInOpen(mapData, weight, height, end, current, x + 1, y - 1, OBLIQUE_VALUE);
        }
        if (canNeighNodeAchieve(mapData, weight, height, current, 1, 1)) {
            addNeighborNodeInOpen(mapData, weight, height, end, current, x + 1, y + 1, OBLIQUE_VALUE);
        }
        if (canNeighNodeAchieve(mapData, weight, height, current, -1, 1)) {
            addNeighborNodeInOpen(mapData, weight, height, end, current, x - 1, y + 1, OBLIQUE_VALUE);
        }
    }

    protected void addNeighborNodeInOpen(Grid[][] mapData, int weight, int height, Node end, Node current, int x, int y, int value) {
        if (canAddNodeToOpen(mapData, weight, height, x, y)) {
            Coord coord = new Coord(x, y);
            int G = current.G + value; // 计算邻结点的G值
            Node child = findNodeInOpen(coord);
            if (child == null) {
                int H = calcH(end.coord, coord); // 计算H值
                if (isEndNode(end.coord, coord)) {
                    child = end;
                    child.parent = current;
                    child.G = G;
                    child.H = H;
                } else {
                    child = new Node(coord, G, H, current);
                }
                openList.add(child);
            } else if (child.G > G) {
                child.G = G;
                child.parent = current;
                openList.add(child);
            }
        }
    }

    protected Node findNodeInOpen(Coord coord) {
        if (coord == null || openList.isEmpty()) return null;
        for (Node node : openList) {
            if (node.coord.equals(coord)) {
                return node;
            }
        }
        return null;
    }

    /**
     * 计算H的估值：“曼哈顿”法，坐标分别取差值相加
     */
    protected int calcH(Coord end, Coord coord) {
        return Math.abs(end.x - coord.x)
                + Math.abs(end.y - coord.y);
    }

    protected boolean isEndNode(Coord end, Coord coord) {
        return coord != null && end.equals(coord);
    }

    protected boolean canAddNodeToOpen(Grid[][] mapData, int weight, int height, int x, int y) {
        if (x < 0 || x >= weight || y < 0 || y >= height) {
            return false;
        }
        // 判断是否阻挡
        if (mapData[x][y].isBlocked()) {
            return false;
        }
        return !isCoordInCloseList(x, y);
    }

    protected List<Coord> drawPath(Node end) {
        if (end == null || closeList.isEmpty()) {
            return null;
        }
        LogUtil.debug("total cost：" + end.G);
        List<Coord> nodeList = new ArrayList<>();
        ProtoVector.Builder toward;
        for (int i = 0; i < MAX_PATH_SIZE; i++) {
            if (end.parent == null) {
                nodeList.add(end.coord);
                break;
            }
            toward = MistConst.calcStanderCoordVector(end.parent.coord.x, end.parent.coord.y,
                    end.coord.x, end.coord.y);
            end.coord.toward = toward;
            nodeList.add(end.coord);
            end = end.parent;
        }
        Collections.reverse(nodeList);
        return nodeList;
    }
}
