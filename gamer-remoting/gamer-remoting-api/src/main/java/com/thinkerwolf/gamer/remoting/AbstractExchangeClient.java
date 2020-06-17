package com.thinkerwolf.gamer.remoting;

import com.thinkerwolf.gamer.common.CauseHolder;
import com.thinkerwolf.gamer.common.URL;
import com.thinkerwolf.gamer.common.concurrent.DefaultPromise;
import com.thinkerwolf.gamer.common.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

@SuppressWarnings("rawtypes")
public abstract class AbstractExchangeClient<T> extends ChannelHandlerAdapter implements ExchangeClient<T> {

    private static final Object START = new Object();
    private static final Object STOP = new Object();
    protected static final AtomicReferenceFieldUpdater<AbstractExchangeClient, Object> statusUpdater
            = AtomicReferenceFieldUpdater.newUpdater(AbstractExchangeClient.class, Object.class, "status");
    protected volatile Object status = START;

    private URL url;
    private Client client;
    private final AtomicInteger idGenerator = new AtomicInteger();
    private final Map<Object, DefaultPromise<T>> waitResultMap = new ConcurrentHashMap<>();

    public AbstractExchangeClient(URL url) {
        this.url = url;
    }

    public AbstractExchangeClient(URL url, Client client) {
        this.url = url;
        this.client = client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public Promise<T> request(Object message) {
        return request(message, 0, null);
    }

    @Override
    public Promise<T> request(Object message, long timeout, TimeUnit unit) {
        DefaultPromise<T> promise = new DefaultPromise<>();
        if (status == STOP || status instanceof CauseHolder) {
            promise.setFailure(getCause(status));
            return promise;
        }

        final int requestId = idGenerator.incrementAndGet();
        promise.setAttachment(message);
        Object msg;
        try {
            msg = encodeRequest(message, requestId);
        } catch (Exception e) {
            promise.setFailure(e);
            return promise;
        }

        waitResultMap.put(requestId, promise);
        try {
            client.send(msg);
        } catch (RemotingException e) {
            waitResultMap.remove(requestId);
            promise.setFailure(e);
        }

        if (timeout > 0 && unit != null) {
            try {
                promise.await(timeout, unit);
            } catch (InterruptedException ignored) {
            }
            if (!promise.isDone()) {
                waitResultMap.remove(requestId);
                promise.setFailure(new TimeoutException());
            }
        }
        return promise;
    }

    /**
     * Encode request
     *
     * @param message   request message
     * @param requestId requestId
     * @return encoded message
     * @throws Exception encode error
     */
    protected abstract Object encodeRequest(Object message, int requestId) throws Exception;

    /**
     * Decode response id
     *
     * @param message msg
     * @return requestId
     */
    protected abstract Integer decodeResponseId(Object message);

    /**
     * Decode response
     *
     * @param message response message
     * @param promise request promise
     * @return <T> decoded message
     * @throws Exception decode error
     */
    protected abstract T decodeResponse(Object message, DefaultPromise<T> promise) throws Exception;

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        final Integer rid = decodeResponseId(message);
        DefaultPromise<T> promise = waitResultMap.get(rid);
        if (promise != null) {
            try {
                T res = decodeResponse(message, promise);
                promise.setSuccess(res);
            } catch (Exception e) {
                promise.setFailure(e);
                throw new RemotingException(e);
            } finally {
                waitResultMap.remove(rid);
            }
        }
    }

    @Override
    public void caught(Channel channel, Throwable e) throws RemotingException {
        // 发生异常，拒绝请求
        CauseHolder holder = new CauseHolder(e);
        Object status = statusUpdater.getAndSet(this, holder);
        if (!(status == STOP || status instanceof CauseHolder)) {
            for (DefaultPromise<T> promise : waitResultMap.values()) {
                promise.setFailure(e);
            }
            waitResultMap.clear();
        }
    }

    private static Throwable getCause(Object status) {
        if (!(status instanceof CauseHolder)) {
            return null;
        }
        return ((CauseHolder) status).cause();
    }

}
