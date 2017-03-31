package kenny.server;

import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import kenny.proto.Message.*;
import kenny.server.crypt.FixedLengthHash;
import kenny.server.crypt.Md5FixedLengthHashImpl;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by kennylbj on 2017/3/24.
 * FIXME thread-safety?
 */
public class MemberBerryServerHandler extends SimpleChannelInboundHandler<Request> {
    private final DataManipulator manipulator;

    // Maintain the map between channel and user info
    // Visited by event loop thread only, since it's alright without synchronize.
    private final Map<Channel, User> clients;

    private final FixedLengthHash hash = new Md5FixedLengthHashImpl();

    public MemberBerryServerHandler(DataManipulator saver) {
        this.manipulator = saver;
        clients = new HashMap<>();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request msg) throws Exception {
        checkNotNull(msg.getType());
        switch (msg.getType()) {
            case REGISTER:
                handleRegister(ctx, msg);
                break;
            case LOGIN:
                handleLogin(ctx, msg);
                break;
            case LOOKUP:
                handleLookup(ctx, msg);
                break;
            case ADD:
                handleAdd(ctx, msg);
                break;
            case DELETE:
                handleDelete(ctx, msg);
                break;
            case ALTER:
                handleAlter(ctx, msg);
                break;
            case RETRIEVEALL:
                handleRetrieveAll(ctx, msg);
                break;
            case LOGOUT:
                handleLogout(ctx, msg);
                break;
            default:
                throw new RuntimeException("Invalid message type: " + msg.getType());
        }
    }

    // Set hash filed by the hash value of password. Erase password field.
    private User hashUserFiled(User user) {
        return User.newBuilder(user)
                .setHash(ByteString.copyFrom(hash.hash(user.getPassword())))
                .clearPassword()
                .build();
    }


    private void handleRegister(ChannelHandlerContext ctx, Request msg) {
        User user = checkNotNull(msg.getUser());
        System.out.println(user.toString() + " register");
        boolean flag = false;
        if (!manipulator.getUser(user.getName()).isPresent()) {
            flag = manipulator.addUser(hashUserFiled(user));
        }
        ctx.writeAndFlush(Response.newBuilder().setType(Type.REGISTER).setFlag(flag).build());
    }

    private void handleLogin(ChannelHandlerContext ctx, Request msg) {
        User hashedUser = hashUserFiled(checkNotNull(msg.getUser()));
        System.out.println(msg.getUser().toString() + " login");
        boolean flag = false;
        Optional<User> savedUser = manipulator.getUser(hashedUser.getName());
        if (savedUser.filter(user -> user.getHash().equals(hashedUser.getHash())).isPresent()) {
            flag = true;
            clients.put(ctx.channel(), hashedUser);
        }
        ctx.writeAndFlush(Response.newBuilder().setType(Type.LOGIN).setFlag(flag).build());
    }

    private void handleLookup(ChannelHandlerContext ctx, Request msg) {
        System.out.println("Lookup: " + msg.getId());
        User user = clients.get(ctx.channel());
        Optional<Record> record = Optional.empty();
        if (user != null) {
            record = manipulator.getRecord(user, msg.getId());
        }
        Response.Builder builder = Response.newBuilder().setType(Type.LOOKUP);
        record.ifPresent(builder::addRecords);
        ctx.writeAndFlush(builder.build());
    }

    private void handleAdd(ChannelHandlerContext ctx, Request msg) {
        System.out.println("Add: " + msg.getRecord().toString());
        User user = clients.get(ctx.channel());
        boolean flag = false;
        if (user != null) {
            flag = manipulator.addRecord(user, pendRecordId(msg.getRecord()));
        }
        ctx.writeAndFlush(Response.newBuilder().setType(Type.ADD).setFlag(flag).build());
    }

    private void handleDelete(ChannelHandlerContext ctx, Request msg) {
        User user = clients.get(ctx.channel());
        boolean flag = false;
        if (user != null) {
            flag = manipulator.deleteRecord(user, msg.getId());
        }
        ctx.writeAndFlush(Response.newBuilder().setType(Type.DELETE).setFlag(flag).build());

    }

    private void handleAlter(ChannelHandlerContext ctx, Request msg) {
        User user = clients.get(ctx.channel());
        boolean flag = false;
        if (user != null) {
            if (manipulator.getRecord(user, msg.getId()).isPresent()) {
                flag = manipulator.deleteRecord(user, msg.getId());
                flag &= manipulator.addRecord(user, pendRecordId(msg.getRecord()));
            }
        }
        ctx.writeAndFlush(Response.newBuilder().setType(Type.ALTER).setFlag(flag).build());

    }

    private void handleRetrieveAll(ChannelHandlerContext ctx, Request msg) {
        User user = clients.get(ctx.channel());
        List<Record> records = new ArrayList<>();
        if (user != null) {
            records = manipulator.getAllRecords(user);
        }
        ctx.writeAndFlush(Response.newBuilder().setType(Type.RETRIEVEALL).addAllRecords(records).build());
    }

    private void handleLogout(ChannelHandlerContext ctx, Request msg) {
        boolean flag = false;
        if (clients.containsKey(ctx.channel())) {
            clients.remove(ctx.channel());
            flag = true;
        }
        ctx.writeAndFlush(Response.newBuilder().setType(Type.LOGOUT).setFlag(flag));
    }

    private Record pendRecordId(Record record) {
        return Record.newBuilder(record).setId(IdGenerator.nextId()).build();
    }
}
