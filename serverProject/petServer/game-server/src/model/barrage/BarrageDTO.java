package model.barrage;

import java.io.Serializable;
import lombok.Data;

@Data
public class BarrageDTO implements Serializable {
    private static final long serialVersionUID = -1595366211702710102L;
    private String playerIdx;
    private String message;

    public BarrageDTO(String playerIdx, String message) {
        this.playerIdx = playerIdx;
        this.message = message;
    }
}
