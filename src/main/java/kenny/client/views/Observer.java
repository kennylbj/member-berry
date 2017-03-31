package kenny.client.views;

import kenny.proto.Message.Record;

import java.util.List;

/**
 * Created by kennylbj on 2017/3/24.
 * Observe the change of MemberBerryClient's state.
 * Any view of member-berry should implement this interface
 */
public interface Observer {

    void onRetrieveAll(List<Record> records);

    void onLookup(Record record);

    void onRegister(boolean flag);

    void onLogin(boolean flag);

    void onAdd(boolean flag);

    void onDelete(boolean flag);

    void onAlter(boolean flag);

    void onLogout(boolean flag);

}
