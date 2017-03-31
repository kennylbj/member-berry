package kenny.server;

import kenny.proto.Message;

import java.util.List;
import java.util.Optional;

/**
 * Created by kennylbj on 2017/3/30.
 */
public interface DataManipulator {

    boolean addUser(Message.User user);

    Optional<Message.User> getUser(String name);

    boolean addRecord(Message.User user, Message.Record record);

    boolean deleteRecord(Message.User user, String recordId);

    Optional<Message.Record> getRecord(Message.User user, String recordId);

    List<Message.Record> getAllRecords(Message.User user);

}
