package auxiliary;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimestampFactory {
    public static long getTimestamp() {
        return ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
    }
}
