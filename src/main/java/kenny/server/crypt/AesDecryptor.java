package kenny.server.crypt;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;

/**
 * Created by kennylbj on 2017/3/24.
 * FIXME iv?
 */
public class AesDecryptor implements Decryptor {
    private static final byte[] iv = new Md5FixedLengthHashImpl().hash("member-berry init vector");
    @Override
    public byte[] decrypt(byte[] key, byte[] value) throws IOException {
        try {

            IvParameterSpec ivps = new IvParameterSpec(iv);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivps);
            return cipher.doFinal(value);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new byte[0];
    }

    public static void main(String[] args) throws Exception {
        String msg = "test aes encryptor and decryptor";
        String pwd = "i am key";
        byte[] key = new Md5FixedLengthHashImpl().hash(pwd);
        printArray(key);

        byte[] encrypted = new AesEncryptor()
                .encrypt(key, msg.getBytes("UTF-8"));
        printArray(encrypted);

        byte[] decrypted = new AesDecryptor()
                .decrypt(key, encrypted);
        printArray(decrypted);

        System.out.println("result: " + new String(decrypted));
    }

    static void printArray(byte[] arr) {
        for (byte anArr : arr) {
            System.out.print(anArr + " ");
        }
        System.out.println();
    }
}
