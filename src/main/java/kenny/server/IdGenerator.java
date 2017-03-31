package kenny.server;

import java.util.UUID;

/**
 * Created by kennylbj on 2017/3/30.
 * Generate Id for {@link kenny.proto.Message.Record}
 */
public final class IdGenerator {

    public static String nextId() {
        return UUID.randomUUID().toString();
    }

}
