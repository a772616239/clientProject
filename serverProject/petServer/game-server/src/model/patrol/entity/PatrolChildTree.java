package model.patrol.entity;

import protocol.Patrol.PatrolPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiao_FL
 * @date 2019/8/2
 */
public class PatrolChildTree {
    /**
     * KV：分支节点（大于1的点）-分支线路数量（除去主线）
     */
    private Map<PatrolTree, Integer> degreeMap = new HashMap<>(8);

    private List<PatrolPoint> branch = new ArrayList<>();

    public Map<PatrolTree, Integer> getDegreeMap() {
        return degreeMap;
    }

    public void setDegreeMap(Map<PatrolTree, Integer> degreeMap) {
        this.degreeMap = degreeMap;
    }

    public List<PatrolPoint> getBranch() {
        return branch;
    }

    public void setBranch(List<PatrolPoint> branch) {
        this.branch = branch;
    }
}
