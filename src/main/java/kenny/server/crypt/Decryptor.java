package kenny.server.crypt;

import java.io.IOException;

/**
 * Created by kennylbj on 2017/3/23.
 */
public interface Decryptor {
    // decrypt a bytes array
    byte[] decrypt(byte[] key, byte[] value) throws IOException;
}
