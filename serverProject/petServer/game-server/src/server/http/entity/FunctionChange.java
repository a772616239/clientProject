package server.http.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author huhan
 * @date 2021/2/1
 */
@Getter
@Setter
public class FunctionChange {
    private int functionNum;
    private boolean status;
}
