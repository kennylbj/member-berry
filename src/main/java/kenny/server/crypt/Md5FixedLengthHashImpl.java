package kenny.server.crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by kennylbj on 2017/3/30.
 */
public class Md5FixedLengthHashImpl implements FixedLengthHash {

    private static final MessageDigest MD5 = create();
    private static final int REPEAT_TIME = 100;

    private static MessageDigest create() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] hash(String message) {
        byte[] hash = message.getBytes();
        for (int i = 0; i < REPEAT_TIME; i++) {
            hash = md5(hash);
        }
        return hash;
    }

    @Override
    public int getHashLength() {
        assert (MD5 != null);
        return MD5.getDigestLength();
    }

    private byte[] md5(byte[] message) {
        assert (MD5 != null);
        return MD5.digest(message);
    }
}
