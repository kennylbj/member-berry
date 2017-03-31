package kenny.server.crypt;

/**
 * Created by kennylbj on 2017/3/30.
 */
public interface FixedLengthHash {

    byte[] hash(String message);

    int getHashLength();
}
