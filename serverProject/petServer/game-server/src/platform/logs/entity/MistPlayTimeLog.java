package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class MistPlayTimeLog extends AbstractPlayerLog {
    int mistLevel;
    int mistStamina;
    boolean join;

    public MistPlayTimeLog(String playerIdx, int mistLevel, int mistStamina, boolean join) {
        super(playerIdx);
        setMistLevel(mistLevel);
        setMistStamina(mistStamina);
        setJoin(join);
    }
}
