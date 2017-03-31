package kenny.client;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import kenny.client.views.Observer;
import kenny.proto.Message.*;
import kenny.proto.Message.Request;
import kenny.proto.Message.Response;
import kenny.proto.Message.Type;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by kennylbj on 2017/3/24.
 */
public class MemberBerryClientHandler extends SimpleChannelInboundHandler<Response> implements Actions {

    private final List<Observer> observers;
    private volatile boolean login = false;
    private volatile Channel channel;

    public MemberBerryClientHandler() {
        observers = new CopyOnWriteArrayList<>();
    }

    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    public void unregisterObserver(Observer observer) {
        observers.remove(observer);
    }

    public boolean isLogin() {
        return login;
    }

    @Override
    public void register(User user) {
        Request.Builder rb = Request.newBuilder();
        channel.writeAndFlush(rb.setType(Type.REGISTER).setUser(user).build());
    }

    @Override
    public void login(User user) {
        Request.Builder rb = Request.newBuilder();
        channel.writeAndFlush(rb.setType(Type.LOGIN).setUser(user).build());

    }

    @Override
    public void add(Record record) {
        Request.Builder rb = Request.newBuilder();
        channel.writeAndFlush(rb.setType(Type.ADD).setRecord(record)
                .build());
    }

    @Override
    public void delete(String id) {
        Request.Builder rb = Request.newBuilder();
        channel.writeAndFlush(rb.setType(Type.DELETE).setId(id)
                .build());
    }

    @Override
    public void lookup(String id) {
        Request.Builder rb = Request.newBuilder();
        channel.writeAndFlush(rb.setType(Type.LOOKUP).setId(id).build());
    }

    @Override
    public void alert(String id, Record record) {
        Request.Builder rb = Request.newBuilder();
        channel.writeAndFlush(rb.setType(Type.ALTER).setId(id).setRecord(record).build());
    }

    @Override
    public void retrieveAll() {
        Request.Builder rb = Request.newBuilder();
        channel.writeAndFlush(rb.setType(Type.RETRIEVEALL).build());
    }

    @Override
    public void logout() {
        Request.Builder rb = Request.newBuilder();
        channel.writeAndFlush(rb.setType(Type.LOGOUT).build());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
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

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }

    private void handleRegister(ChannelHandlerContext ctx, Response msg) {
        // update observers
        observers.forEach(observer -> observer.onRegister(msg.getFlag()));

    }

    private void handleLogin(ChannelHandlerContext ctx, Response msg) {
        // update observers
        observers.forEach(observer -> observer.onLogin(msg.getFlag()));

        // retrieve all records
        if (msg.getFlag()) {
            checkState(!login);
            login = true;
            Request.Builder builder = Request.newBuilder();
            builder.setType(Type.RETRIEVEALL);
            ctx.writeAndFlush(builder.build());
        }
    }

    private void handleLookup(ChannelHandlerContext ctx, Response msg) {
        if (msg.getRecordsCount() == 0) {
            observers.forEach(observer -> observer.onLookup(null));
            return;
        }
        checkState(msg.getRecordsCount() == 1);
        // update observers
        observers.forEach(observer -> observer.onLookup(msg.getRecords(0)));
    }

    private void handleAdd(ChannelHandlerContext ctx, Response msg) {
        // update observers
        observers.forEach(observer -> observer.onAdd(msg.getFlag()));

    }

    private void handleDelete(ChannelHandlerContext ctx, Response msg) {
        // update observers
        observers.forEach(observer -> observer.onDelete(msg.getFlag()));

    }

    private void handleAlter(ChannelHandlerContext ctx, Response msg) {
        // update observers
        observers.forEach(observer -> observer.onAlter(msg.getFlag()));

    }

    private void handleRetrieveAll(ChannelHandlerContext ctx, Response msg) {
        // update observers
        observers.forEach(observer -> observer.onRetrieveAll(msg.getRecordsList()));

    }

    private void handleLogout(ChannelHandlerContext ctx, Response msg) {
        // update observers
        observers.forEach(observer -> observer.onLogout(msg.getFlag()));
        if (msg.getFlag()) {
            checkState(login);
            login = false;
        }
    }
}