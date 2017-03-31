package kenny.client;

import kenny.proto.Message.*;

/**
 * Created by kennylbj on 2017/3/25.
 */
public interface Actions {

    void register(User user);

    void login(User user);

    void add(Record record);

    void delete(String id);

    void lookup(String id);

    void alert(String id, Record record);

    void retrieveAll();

    void logout();
}
