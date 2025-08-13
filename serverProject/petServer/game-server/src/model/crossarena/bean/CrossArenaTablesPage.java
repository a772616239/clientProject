package model.crossarena.bean;

import protocol.CrossArena;
import protocol.RetCodeId;

import java.util.Collections;
import java.util.List;

public class CrossArenaTablesPage {

    private int totalPage;
    private List<CrossArena.CrossArenaOneInfo> tables = Collections.emptyList();
    private RetCodeId.RetCodeEnum codeEnum;

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<CrossArena.CrossArenaOneInfo> getTables() {
        return tables;
    }

    public void setTables(List<CrossArena.CrossArenaOneInfo> tables) {
        this.tables = tables;
    }

    public RetCodeId.RetCodeEnum getCodeEnum() {
        return codeEnum;
    }

    public void setCodeEnum(RetCodeId.RetCodeEnum codeEnum) {
        this.codeEnum = codeEnum;
    }
}
