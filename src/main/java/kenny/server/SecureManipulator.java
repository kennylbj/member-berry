package kenny.server;

import kenny.proto.Message;
import kenny.server.crypt.Decryptor;
import kenny.server.crypt.Encryptor;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by kennylbj on 2017/3/30.
 * Abstract class for manipulate persistence operation.
 * The persistence level can be local file or database and so on.
 * All platform operation are delegating to abstract method
 * such as {@code load(String namespace, String key)}
 */
public abstract class SecureManipulator implements DataManipulator {

    private final Decryptor decryptor;
    private final Encryptor encryptor;
    private final String userNamespace = "User";

    public SecureManipulator(Encryptor encryptor, Decryptor decryptor) {
        this.encryptor = encryptor;
        this.decryptor = decryptor;
    }
    @Override
    public boolean addUser(Message.User user) {
        return save(userNamespace, user.getName(), user.toByteArray());
    }

    @Override
    public Optional<Message.User> getUser(String name) {
        Optional<Message.User> user = Optional.empty();
        try {
            byte[] loaded = load(userNamespace, name);
            if (loaded.length > 0) {
                user = Optional.of(Message.User.parseFrom(load(userNamespace, name)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public boolean addRecord(Message.User user, Message.Record record) {
        try {
            return save(user.getName(), record.getId(),
                    encryptor.encrypt(user.getHash().toByteArray(), record.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteRecord(Message.User user, String recordId) {
        return delete(user.getName(), recordId);
    }

    @Override
    public Optional<Message.Record> getRecord(Message.User user, String recordId) {
        byte[] encrypted = load(user.getName(), recordId);
        if (encrypted.length > 0) {
            return Optional.ofNullable(parseRecord(encrypted, user.getHash().toByteArray()));
        }
        return Optional.empty();
    }

    @Override
    public List<Message.Record> getAllRecords(Message.User user) {
        return loadAll(user.getName()).stream()
                .map(encryptor -> parseRecord(encryptor, user.getHash().toByteArray()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Message.Record parseRecord(byte[] encrypted, byte[] key) {
        try {
            return Message.Record.parseFrom(decryptor.decrypt(key, encrypted));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract boolean save(String namespace, String key, byte[] value);

    protected abstract byte[] load(String namespace, String key);

    protected abstract boolean delete(String namespace, String key);

    protected abstract List<byte[]> loadAll(String namespace);
}
