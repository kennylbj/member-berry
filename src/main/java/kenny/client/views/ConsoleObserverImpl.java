package kenny.client.views;

import kenny.client.MemberBerryClientHandler;
import kenny.proto.Message.User;
import kenny.proto.Message.Record;

import java.util.List;
import java.util.Scanner;

/**
 * Created by kennylbj on 2017/3/24.
 * Interact with client from command line
 */
public class ConsoleObserverImpl implements Observer, Runnable {
    private final Scanner reader;
    private final MemberBerryClientHandler handler;

    public ConsoleObserverImpl(MemberBerryClientHandler handler) {
        reader = new Scanner((System.in));
        this.handler = handler;
    }

    private void printRecord(Record record) {
        System.out.println("Id          : " + record.getId());
        System.out.println("Description : " + record.getDescription());
        System.out.println("Name        : " + record.getName());
        System.out.println("Password    : " + record.getPassword());
        System.out.println("Link        : " + record.getLink() + "\n");
    }

    @Override
    public void onRetrieveAll(List<Record> records) {
        System.out.println("Retrieve all records result:\n");
        records.forEach(this::printRecord);
    }

    @Override
    public void onLookup(Record record) {
        System.out.println("Lookup result:");
        if (record != null) {
            printRecord(record);
        } else {
            System.out.println("No this record.");
        }
    }

    @Override
    public void onRegister(boolean flag) {
        if (flag) {
            System.out.println("Register succeed.");
        } else {
            System.out.println("Register failed.");
        }
    }

    @Override
    public void onLogin(boolean flag) {
        if (flag) {
            System.out.println("Log in succeed.");
        } else {
            System.out.println("Log in failed.");
        }
    }

    @Override
    public void onAdd(boolean flag) {
        if (flag) {
            System.out.println("Add record succeed.");
        } else {
            System.out.println("Add record failed.");
        }
    }

    @Override
    public void onDelete(boolean flag) {
        if (flag) {
            System.out.println("Delete record succeed.");
        } else {
            System.out.println("Delete record failed.");
        }
    }

    @Override
    public void onAlter(boolean flag) {
        if (flag) {
            System.out.println("Alter record succeed.");
        } else {
            System.out.println("Alter record failed.");
        }
    }

    @Override
    public void onLogout(boolean flag) {
        if (flag) {
            System.out.println("Logout record succeed.");
        } else {
            System.out.println("Logout record failed.");
        }
        // todo shutdown
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        usage();
        while (true) {
            String cmd = reader.nextLine();
            switch (cmd.trim().toLowerCase()) {
                case "register":
                    register();
                    break;
                case "login":
                    login();
                    break;
                case "lookup":
                    lookup();
                    break;
                case "add":
                    add();
                    break;
                case "delete":
                    delete();
                    break;
                case "alter":
                    alter();
                    break;
                case "retrieve":
                    retrieveAll();
                    break;
                case "exit":
                case "quit":
                case "logout":
                    exit();
                    break;
                case "help":
                    help();
                    break;
                default:
                    System.out.println("Invalid command, please try again.");
            }
        }
    }


    private boolean isBlank(String str) {
        return str == null || str.isEmpty();
    }

    private void register() {
        System.out.println("Register, please input a name:");
        String name = reader.nextLine();
        System.out.println("Please input a password");
        String password = reader.nextLine();
        if (isBlank(name) || isBlank(password)) {
            System.out.println("Invalid register arguments.");
            return;
        }
        handler.register(User.newBuilder().setName(name).setPassword(password).build());

    }

    private void login() {
        if (handler.isLogin()) {
            System.out.println("You have been logged in.");
            return;
        }
        System.out.println("Login to Member berry, please input a name:");
        String name = reader.nextLine();
        System.out.println("Please input a password:");
        String password = reader.nextLine();
        if (isBlank(name) || isBlank(password)) {
            System.out.println("Invalid register arguments.");
            return;
        }
        handler.login(User.newBuilder().setName(name).setPassword(password).build());
    }

    // lookup by record's name
    private void lookup() {
        if (!handler.isLogin()) {
            System.out.println("Please log in first.");
            login();
            return;
        }
        System.out.println("Please input record Id:");
        String id = reader.nextLine();
        if (isBlank(id)) {
            System.out.println("Invalid lookup arguments.");
            return;
        }
        handler.lookup(id);

    }

    private void add() {
        if (!handler.isLogin()) {
            System.out.println("Please log in first.");
            login();
            return;
        }
        System.out.println("Please input a name:");
        String name = reader.nextLine();
        System.out.println("Please input a password:");
        String password = reader.nextLine();
        System.out.println("Please input a description:");
        String description = reader.nextLine();
        System.out.println("Please input a link:");
        String link = reader.nextLine();
        if (isBlank(name) || isBlank(password) || isBlank(description) || isBlank(link)) {
            System.out.println("Invalid add arguments.");
            return;
        }

        handler.add(Record.newBuilder()
                .setName(name)
                .setPassword(password)
                .setDescription(description)
                .setLink(link)
                .build());
    }

    private void delete() {
        if (!handler.isLogin()) {
            System.out.println("Please log in first.");
            login();
            return;
        }
        System.out.println("Please input record Id:");
        String id = reader.nextLine();
        if (isBlank(id)) {
            System.out.println("Invalid lookup arguments.");
            return;
        }
        handler.delete(id);
    }

    private void alter() {
        if (!handler.isLogin()) {
            System.out.println("Please log in first.");
            login();
            return;
        }
        System.out.println("Please input record Id to be alter:");
        String id = reader.nextLine();

        System.out.println("New record \t please input a name:");
        String name = reader.nextLine();
        System.out.println("Please input a password:");
        String password = reader.nextLine();
        System.out.println("Please input a description:");
        String description = reader.nextLine();
        System.out.println("Please input a link:");
        String link = reader.nextLine();
        if (isBlank(name) || isBlank(password) || isBlank(description) || isBlank(link)) {
            System.out.println("Invalid add arguments.");
            return;
        }
        handler.alert(id, Record.newBuilder()
                .setName(name)
                .setPassword(password)
                .setDescription(description)
                .setLink(link)
                .build());
    }

    private void retrieveAll() {
        if (!handler.isLogin()) {
            System.out.println("Please login first.");
            login();
            return;
        }
        handler.retrieveAll();
    }

    private void exit() {
        if (!handler.isLogin()) {
            System.out.println("You haven't login");
            return;
        }
        handler.logout();
    }

    private void usage() {
        System.out.println("Welcome to Member-Berry. A password keeper.");
        System.out.println("Enter help for more information.");
    }

    private void help() {
        System.out.println("Usages   :  command : explain");
        System.out.println("register : register message to server.");
        System.out.println("login    : login to server.");
        System.out.println("add      : add a record.");
        System.out.println("delete   : delete a record.");
        System.out.println("lookup   : lookup a record.");
        System.out.println("alter    : modify a record.");
        System.out.println("retrieve : get all records of login user.");
        System.out.println("logout   : logout from server.");
    }
}
