package bean;

import code.TypeConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @date
 */
public class RowData {
    private List<ConfigData> rowData;

    public List<ConfigData> getRowData() {
        return rowData;
    }

    public void setRowData(List<ConfigData> rowData) {
        this.rowData = rowData;
    }

    public void addRowData(ConfigData configData) {
        if(configData == null) {
            return;
        }

        if (rowData == null) {
            rowData = new ArrayList<>();
        }
        rowData.add(configData);
    }

    /**
     * 返回是主键的数据
     * @return
     */
    public List<ConfigData> getKeyDataList() {
        if (rowData == null) {
            return null;
        }
        return rowData.stream()
                .filter(e -> Objects.equals(e.getFieldKey(), TypeConfig.FIELD_PRIMARY_KEY))
                .collect(Collectors.toList());
    }

    public String getFirstKey() {
        if (rowData == null) {
            return null;
        }
        ConfigData data = rowData.stream()
                .filter(e -> Objects.equals(e.getFieldKey(), TypeConfig.FIELD_PRIMARY_KEY)).findFirst().get();
        return data != null ? data.getFieldValue() : null;
    }
}
