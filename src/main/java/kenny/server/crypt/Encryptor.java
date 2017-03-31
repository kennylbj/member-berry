package kenny.server.crypt;

import java.io.IOException;

/**
 * Created by kennylbj on 2017/3/23.
 */
public interface Encryptor {
    // encrypt a byte array
    byte[] encrypt(byte[] key, byte[] value) throws IOException;
}
