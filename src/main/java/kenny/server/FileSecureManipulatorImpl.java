package kenny.server;
import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import kenny.server.crypt.*;

import java.io.*;
import java.nio.file.*;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by kennylbj on 2017/3/30.
 * Interact all message with local file.
 * All message are treated as key and value.
 * For the {@link kenny.proto.Message.User} object, the storing format is:
 * "username, splitter, data".
 * which means user name is treated as identification.
 *
 * For {@link kenny.proto.Message.Record} object, the storing format is:
 * "record id, splitter, data".
 * Although record contains id filed, we duplicate it
 * in order to speed up load operation.
 */
public class FileSecureManipulatorImpl extends SecureManipulator {
    private static final String PATH = "data/";
    private static final String SPLIT = "\t";
    public FileSecureManipulatorImpl(Encryptor encryptor, Decryptor decryptor) {
        super(encryptor, decryptor);
    }

    @Override
    protected boolean save(String namespace, String key, byte[] value) {
        Path path = Paths.get(getFileName(namespace));
        ensureFileExists(path);
        try (BufferedWriter writer = Files.newBufferedWriter(path,
                StandardOpenOption.APPEND)){
            writer.append(key).append(SPLIT).append(Base64.getEncoder().encodeToString(value));
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected byte[] load(String namespace, String key) {
        Path path = Paths.get(getFileName(namespace));
        ensureFileExists(path);
        try (Stream<String> lines = Files.lines(path)){
            return lines.filter(line -> key.equals(getFirst(line)))
                    .findFirst()
                    .map(this::getSecond)
                    .map(Base64.getDecoder()::decode)
                    .orElse(new byte[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    protected boolean delete(String namespace, String key) {
        Path path = Paths.get(getFileName(namespace));
        checkState(Files.exists(path));
        Path tmpPath = Paths.get(getFileName(namespace) + ".tmp");
        checkState(!Files.exists(tmpPath));

        try (Stream<String> src = Files.lines(path);
            PrintWriter dst = new PrintWriter(Files.newBufferedWriter(tmpPath))) {
            src.filter(line -> !key.equals(getFirst(line))).forEach(dst::println);
            Files.move(tmpPath, path, REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
          e.printStackTrace();
        }
        return false;

    }

    @Override
    protected List<byte[]> loadAll(String namespace) {
        Path path = Paths.get(getFileName(namespace));
        ensureFileExists(path);
        try (Stream<String> lines = Files.lines(path)) {
            return lines.map(this::getSecond)
                    .map(Base64.getDecoder()::decode)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }


    private String getFileName(String namespace) {
        return PATH + namespace + ".mb";
    }

    private String getFirst(String line) {
        return line.split(SPLIT)[0];
    }

    private String getSecond(String line) {
        return line.split(SPLIT)[1];
    }

    private void ensureFileExists(Path path) {
        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        FileSecureManipulatorImpl fm = new FileSecureManipulatorImpl(new AesEncryptor(), new AesDecryptor());
        String msg = "Test FileSecureManipulatorImpl.";
        String msg2 = "Are you ok?";
        fm.save("test", "1", msg.getBytes());
        fm.save("test", "2", msg2.getBytes());

        byte[] loaded = fm.load("test", "2");
        System.out.println(new String(loaded));

        fm.delete("test", "1");

    }
}
