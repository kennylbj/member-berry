package kenny.server.crypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;

/**
 * Created by kennylbj on 2017/3/24.
 * FIXME iv?
 */
public class AesEncryptor implements Encryptor {
    private static final byte[] iv = new Md5FixedLengthHashImpl().hash("member-berry init vector");
    @Override
    public byte[] encrypt(byte[] key, byte[] value) throws IOException {
        try {
            IvParameterSpec ivps = new IvParameterSpec(iv);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivps);
            return cipher.doFinal(value);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new byte[0];
    }
}
