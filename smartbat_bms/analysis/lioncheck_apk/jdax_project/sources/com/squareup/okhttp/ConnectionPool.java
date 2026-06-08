package com.squareup.okhttp;

import com.squareup.okhttp.internal.Platform;
import com.squareup.okhttp.internal.Util;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: classes.dex */
public final class ConnectionPool {
    private static final long DEFAULT_KEEP_ALIVE_DURATION_MS = 300000;
    private static final ConnectionPool systemDefault;
    private final long keepAliveDurationNs;
    private final int maxIdleConnections;
    private final Deque<Connection> connections = new ArrayDeque();
    private Executor executor = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), Util.threadFactory("OkHttp ConnectionPool", true));
    private final Runnable connectionsCleanupRunnable = new Runnable() { // from class: com.squareup.okhttp.ConnectionPool.1
        @Override // java.lang.Runnable
        public void run() {
            ConnectionPool.this.runCleanupUntilPoolIsEmpty();
        }
    };

    static {
        String property = System.getProperty("http.keepAlive");
        String property2 = System.getProperty("http.keepAliveDuration");
        String property3 = System.getProperty("http.maxConnections");
        long j = property2 != null ? Long.parseLong(property2) : DEFAULT_KEEP_ALIVE_DURATION_MS;
        if (property != null && !Boolean.parseBoolean(property)) {
            systemDefault = new ConnectionPool(0, j);
        } else if (property3 != null) {
            systemDefault = new ConnectionPool(Integer.parseInt(property3), j);
        } else {
            systemDefault = new ConnectionPool(5, j);
        }
    }

    public ConnectionPool(int i, long j) {
        this.maxIdleConnections = i;
        this.keepAliveDurationNs = j * 1000000;
    }

    public static ConnectionPool getDefault() {
        return systemDefault;
    }

    public synchronized int getConnectionCount() {
        return this.connections.size();
    }

    @Deprecated
    public synchronized int getSpdyConnectionCount() {
        return getMultiplexedConnectionCount();
    }

    public synchronized int getMultiplexedConnectionCount() {
        int i;
        Iterator<Connection> it = this.connections.iterator();
        i = 0;
        while (it.hasNext()) {
            if (it.next().isFramed()) {
                i++;
            }
        }
        return i;
    }

    public synchronized int getHttpConnectionCount() {
        return this.connections.size() - getMultiplexedConnectionCount();
    }

    public synchronized Connection get(Address address) {
        Connection next;
        Iterator<Connection> itDescendingIterator = this.connections.descendingIterator();
        while (true) {
            if (!itDescendingIterator.hasNext()) {
                next = null;
                break;
            }
            next = itDescendingIterator.next();
            if (next.getRoute().getAddress().equals(address) && next.isAlive() && System.nanoTime() - next.getIdleStartTimeNs() < this.keepAliveDurationNs) {
                itDescendingIterator.remove();
                if (next.isFramed()) {
                    break;
                }
                try {
                    Platform.get().tagSocket(next.getSocket());
                    break;
                } catch (SocketException e) {
                    Util.closeQuietly(next.getSocket());
                    Platform.get().logW("Unable to tagSocket(): " + e);
                }
            }
        }
        if (next != null && next.isFramed()) {
            this.connections.addFirst(next);
        }
        return next;
    }

    void recycle(Connection connection) {
        if (!connection.isFramed() && connection.clearOwner()) {
            if (!connection.isAlive()) {
                Util.closeQuietly(connection.getSocket());
                return;
            }
            try {
                Platform.get().untagSocket(connection.getSocket());
                synchronized (this) {
                    addConnection(connection);
                    connection.incrementRecycleCount();
                    connection.resetIdleStartTime();
                }
            } catch (SocketException e) {
                Platform.get().logW("Unable to untagSocket(): " + e);
                Util.closeQuietly(connection.getSocket());
            }
        }
    }

    private void addConnection(Connection connection) {
        boolean zIsEmpty = this.connections.isEmpty();
        this.connections.addFirst(connection);
        if (zIsEmpty) {
            this.executor.execute(this.connectionsCleanupRunnable);
        } else {
            notifyAll();
        }
    }

    void share(Connection connection) {
        if (!connection.isFramed()) {
            throw new IllegalArgumentException();
        }
        if (connection.isAlive()) {
            synchronized (this) {
                addConnection(connection);
            }
        }
    }

    public void evictAll() {
        ArrayList arrayList;
        synchronized (this) {
            arrayList = new ArrayList(this.connections);
            this.connections.clear();
            notifyAll();
        }
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            Util.closeQuietly(((Connection) arrayList.get(i)).getSocket());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runCleanupUntilPoolIsEmpty() {
        while (performCleanup()) {
        }
    }

    boolean performCleanup() {
        synchronized (this) {
            if (this.connections.isEmpty()) {
                return false;
            }
            ArrayList arrayList = new ArrayList();
            long jNanoTime = System.nanoTime();
            long jMin = this.keepAliveDurationNs;
            Iterator<Connection> itDescendingIterator = this.connections.descendingIterator();
            int i = 0;
            while (itDescendingIterator.hasNext()) {
                Connection next = itDescendingIterator.next();
                long idleStartTimeNs = (next.getIdleStartTimeNs() + this.keepAliveDurationNs) - jNanoTime;
                if (idleStartTimeNs <= 0 || !next.isAlive()) {
                    itDescendingIterator.remove();
                    arrayList.add(next);
                } else if (next.isIdle()) {
                    i++;
                    jMin = Math.min(jMin, idleStartTimeNs);
                }
            }
            Iterator<Connection> itDescendingIterator2 = this.connections.descendingIterator();
            while (itDescendingIterator2.hasNext() && i > this.maxIdleConnections) {
                Connection next2 = itDescendingIterator2.next();
                if (next2.isIdle()) {
                    arrayList.add(next2);
                    itDescendingIterator2.remove();
                    i--;
                }
            }
            if (arrayList.isEmpty()) {
                try {
                    long j = jMin / 1000000;
                    wait(j, (int) (jMin - (1000000 * j)));
                    return true;
                } catch (InterruptedException unused) {
                }
            }
            int size = arrayList.size();
            for (int i2 = 0; i2 < size; i2++) {
                Util.closeQuietly(((Connection) arrayList.get(i2)).getSocket());
            }
            return true;
        }
    }

    void replaceCleanupExecutorForTests(Executor executor) {
        this.executor = executor;
    }

    synchronized List<Connection> getConnections() {
        return new ArrayList(this.connections);
    }
}
