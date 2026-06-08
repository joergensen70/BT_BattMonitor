package com.squareup.okhttp.internal.allocations;

import com.facebook.common.time.Clock;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.internal.Internal;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public final class Connection {
    private boolean noNewAllocations;
    private final ConnectionPool pool;
    private final List<StreamAllocationReference> allocations = new ArrayList();
    private int allocationLimit = 1;
    long idleAt = Clock.MAX_TIME;

    public Connection(ConnectionPool connectionPool) {
        this.pool = connectionPool;
    }

    public StreamAllocation reserve(String str) {
        synchronized (this.pool) {
            if (!this.noNewAllocations && this.allocations.size() < this.allocationLimit) {
                StreamAllocation streamAllocation = new StreamAllocation();
                this.allocations.add(new StreamAllocationReference(streamAllocation, str));
                return streamAllocation;
            }
            return null;
        }
    }

    public void release(StreamAllocation streamAllocation) {
        synchronized (this.pool) {
            if (streamAllocation.released) {
                throw new IllegalStateException("already released");
            }
            streamAllocation.released = true;
            if (streamAllocation.stream == null) {
                remove(streamAllocation);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void remove(StreamAllocation streamAllocation) {
        int size = this.allocations.size();
        for (int i = 0; i < size; i++) {
            if (this.allocations.get(i).get() == streamAllocation) {
                this.allocations.remove(i);
                if (this.allocations.isEmpty()) {
                    this.idleAt = System.nanoTime();
                    return;
                }
                return;
            }
        }
        throw new IllegalArgumentException("unexpected allocation: " + streamAllocation);
    }

    public void noNewStreams() {
        synchronized (this.pool) {
            this.noNewAllocations = true;
            for (int i = 0; i < this.allocations.size(); i++) {
                this.allocations.get(i).rescind();
            }
        }
    }

    public void setAllocationLimit(int i) {
        synchronized (this.pool) {
            try {
                if (i < 0) {
                    throw new IllegalArgumentException();
                }
                this.allocationLimit = i;
                while (i < this.allocations.size()) {
                    this.allocations.get(i).rescind();
                    i++;
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void pruneLeakedAllocations() {
        synchronized (this.pool) {
            Iterator<StreamAllocationReference> it = this.allocations.iterator();
            while (it.hasNext()) {
                StreamAllocationReference next = it.next();
                if (next.get() == null) {
                    Internal.logger.warning("Call " + next.name + " leaked a connection. Did you forget to close a response body?");
                    this.noNewAllocations = true;
                    it.remove();
                    if (this.allocations.isEmpty()) {
                        this.idleAt = System.nanoTime();
                    }
                }
            }
        }
    }

    int size() {
        int size;
        synchronized (this.pool) {
            size = this.allocations.size();
        }
        return size;
    }

    public final class StreamAllocation {
        private boolean released;
        private boolean rescinded;
        private Stream stream;

        private StreamAllocation() {
        }

        public Stream newStream(String str) {
            synchronized (Connection.this.pool) {
                if (this.stream != null || this.released) {
                    throw new IllegalStateException();
                }
                if (this.rescinded) {
                    return null;
                }
                Stream stream = new Stream(str);
                this.stream = stream;
                return stream;
            }
        }

        public void streamComplete(Stream stream) {
            synchronized (Connection.this.pool) {
                if (stream != null) {
                    if (stream == this.stream) {
                        this.stream = null;
                        if (this.released) {
                            Connection.this.remove(this);
                        }
                    }
                }
                throw new IllegalArgumentException();
            }
        }
    }

    private static final class StreamAllocationReference extends WeakReference<StreamAllocation> {
        private final String name;

        public StreamAllocationReference(StreamAllocation streamAllocation, String str) {
            super(streamAllocation);
            this.name = str;
        }

        public void rescind() {
            StreamAllocation streamAllocation = (StreamAllocation) get();
            if (streamAllocation != null) {
                streamAllocation.rescinded = true;
            }
        }
    }

    public static class Stream {
        public final String name;

        public Stream(String str) {
            this.name = str;
        }

        public String toString() {
            return this.name;
        }
    }
}
