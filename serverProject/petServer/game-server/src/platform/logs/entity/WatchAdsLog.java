package platform.logs.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platform.logs.AbstractPlayerLog;

@Getter
@Setter
@NoArgsConstructor
public class WatchAdsLog extends AbstractPlayerLog {
    String adsType;

    public WatchAdsLog(String playerIdx, String adsType) {
        super(playerIdx);

        this.adsType = adsType;
    }
}
