package com.lidroid.xutils.task;

import android.support.v7.widget.ActivityChooserView;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/* JADX INFO: loaded from: classes.dex */
public class PriorityObjectBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, Serializable {
    private static final long serialVersionUID = -6903933977591709194L;
    private final int capacity;
    private final AtomicInteger count;
    transient Node<E> head;
    private transient Node<E> last;
    private final Condition notEmpty;
    private final Condition notFull;
    private final ReentrantLock putLock;
    private final ReentrantLock takeLock;

    private void signalNotEmpty() {
        ReentrantLock reentrantLock = this.takeLock;
        reentrantLock.lock();
        try {
            this.notEmpty.signal();
        } finally {
            reentrantLock.unlock();
        }
    }

    private void signalNotFull() {
        ReentrantLock reentrantLock = this.putLock;
        reentrantLock.lock();
        try {
            this.notFull.signal();
        } finally {
            reentrantLock.unlock();
        }
    }

    private synchronized E opQueue(Node<E> node) {
        if (node == null) {
            return _dequeue();
        }
        _enqueue(node);
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void _enqueue(Node<E> node) {
        for (Node node2 = this.head; node2.next != null; node2 = node2.next) {
            Node<T> node3 = node2.next;
            if (node3.getPriority().ordinal() > node.getPriority().ordinal()) {
                node2.next = node;
                node.next = node3;
                return;
            }
        }
        this.last.next = node;
        this.last = node;
    }

    private E _dequeue() {
        Node<E> node = this.head;
        Node<E> node2 = (Node<E>) node.next;
        node.next = node;
        this.head = node2;
        E value = node2.getValue();
        node2.setValue(null);
        return value;
    }

    void fullyLock() {
        this.putLock.lock();
        this.takeLock.lock();
    }

    void fullyUnlock() {
        this.takeLock.unlock();
        this.putLock.unlock();
    }

    public PriorityObjectBlockingQueue() {
        this(ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
    }

    public PriorityObjectBlockingQueue(int i) {
        this.count = new AtomicInteger();
        ReentrantLock reentrantLock = new ReentrantLock();
        this.takeLock = reentrantLock;
        this.notEmpty = reentrantLock.newCondition();
        ReentrantLock reentrantLock2 = new ReentrantLock();
        this.putLock = reentrantLock2;
        this.notFull = reentrantLock2.newCondition();
        if (i <= 0) {
            throw new IllegalArgumentException();
        }
        this.capacity = i;
        Node<E> node = new Node<>(null);
        this.head = node;
        this.last = node;
    }

    public PriorityObjectBlockingQueue(Collection<? extends E> collection) {
        this(ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
        ReentrantLock reentrantLock = this.putLock;
        reentrantLock.lock();
        try {
            int i = 0;
            for (E e : collection) {
                if (e == null) {
                    throw new NullPointerException();
                }
                if (i == this.capacity) {
                    throw new IllegalStateException("Queue full");
                }
                opQueue(new Node<>(e));
                i++;
            }
            this.count.set(i);
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override // java.util.AbstractCollection, java.util.Collection
    public int size() {
        return this.count.get();
    }

    @Override // java.util.concurrent.BlockingQueue
    public int remainingCapacity() {
        return this.capacity - this.count.get();
    }

    @Override // java.util.concurrent.BlockingQueue
    public void put(E e) throws InterruptedException {
        e.getClass();
        Node<E> node = new Node<>(e);
        ReentrantLock reentrantLock = this.putLock;
        AtomicInteger atomicInteger = this.count;
        reentrantLock.lockInterruptibly();
        while (atomicInteger.get() == this.capacity) {
            try {
                this.notFull.await();
            } finally {
                reentrantLock.unlock();
            }
        }
        opQueue(node);
        int andIncrement = atomicInteger.getAndIncrement();
        if (andIncrement + 1 < this.capacity) {
            this.notFull.signal();
        }
        if (andIncrement == 0) {
            signalNotEmpty();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public boolean offer(E e, long j, TimeUnit timeUnit) throws InterruptedException {
        e.getClass();
        long nanos = timeUnit.toNanos(j);
        ReentrantLock reentrantLock = this.putLock;
        AtomicInteger atomicInteger = this.count;
        reentrantLock.lockInterruptibly();
        while (atomicInteger.get() == this.capacity) {
            try {
                if (nanos > 0) {
                    nanos = this.notFull.awaitNanos(nanos);
                } else {
                    reentrantLock.unlock();
                    return false;
                }
            } finally {
                reentrantLock.unlock();
            }
        }
        opQueue(new Node<>(e));
        int andIncrement = atomicInteger.getAndIncrement();
        if (andIncrement + 1 < this.capacity) {
            this.notFull.signal();
        }
        if (andIncrement != 0) {
            return true;
        }
        signalNotEmpty();
        return true;
    }

    @Override // java.util.Queue, java.util.concurrent.BlockingQueue
    public boolean offer(E e) {
        int andIncrement;
        e.getClass();
        AtomicInteger atomicInteger = this.count;
        if (atomicInteger.get() == this.capacity) {
            return false;
        }
        Node<E> node = new Node<>(e);
        ReentrantLock reentrantLock = this.putLock;
        reentrantLock.lock();
        try {
            if (atomicInteger.get() < this.capacity) {
                opQueue(node);
                andIncrement = atomicInteger.getAndIncrement();
                if (andIncrement + 1 < this.capacity) {
                    this.notFull.signal();
                }
            } else {
                andIncrement = -1;
            }
            if (andIncrement == 0) {
                signalNotEmpty();
            }
            return andIncrement >= 0;
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public E take() throws InterruptedException {
        AtomicInteger atomicInteger = this.count;
        ReentrantLock reentrantLock = this.takeLock;
        reentrantLock.lockInterruptibly();
        while (atomicInteger.get() == 0) {
            try {
                this.notEmpty.await();
            } catch (Throwable th) {
                reentrantLock.unlock();
                throw th;
            }
        }
        E eOpQueue = opQueue(null);
        int andDecrement = atomicInteger.getAndDecrement();
        if (andDecrement > 1) {
            this.notEmpty.signal();
        }
        reentrantLock.unlock();
        if (andDecrement == this.capacity) {
            signalNotFull();
        }
        return eOpQueue;
    }

    @Override // java.util.concurrent.BlockingQueue
    public E poll(long j, TimeUnit timeUnit) throws InterruptedException {
        long nanos = timeUnit.toNanos(j);
        AtomicInteger atomicInteger = this.count;
        ReentrantLock reentrantLock = this.takeLock;
        reentrantLock.lockInterruptibly();
        while (atomicInteger.get() == 0) {
            try {
                if (nanos <= 0) {
                    return null;
                }
                nanos = this.notEmpty.awaitNanos(nanos);
            } finally {
                reentrantLock.unlock();
            }
        }
        E eOpQueue = opQueue(null);
        int andDecrement = atomicInteger.getAndDecrement();
        if (andDecrement > 1) {
            this.notEmpty.signal();
        }
        reentrantLock.unlock();
        if (andDecrement == this.capacity) {
            signalNotFull();
        }
        return eOpQueue;
    }

    @Override // java.util.Queue
    public E poll() {
        int andDecrement;
        AtomicInteger atomicInteger = this.count;
        E eOpQueue = null;
        if (atomicInteger.get() == 0) {
            return null;
        }
        ReentrantLock reentrantLock = this.takeLock;
        reentrantLock.lock();
        try {
            if (atomicInteger.get() > 0) {
                eOpQueue = opQueue(null);
                andDecrement = atomicInteger.getAndDecrement();
                if (andDecrement > 1) {
                    this.notEmpty.signal();
                }
            } else {
                andDecrement = -1;
            }
            reentrantLock.unlock();
            if (andDecrement == this.capacity) {
                signalNotFull();
            }
            return eOpQueue;
        } catch (Throwable th) {
            reentrantLock.unlock();
            throw th;
        }
    }

    @Override // java.util.Queue
    public E peek() {
        if (this.count.get() == 0) {
            return null;
        }
        ReentrantLock reentrantLock = this.takeLock;
        reentrantLock.lock();
        try {
            Node<E> node = this.head.next;
            if (node == null) {
                return null;
            }
            return node.getValue();
        } finally {
            reentrantLock.unlock();
        }
    }

    void unlink(Node<E> node, Node<E> node2) {
        node.setValue(null);
        node2.next = node.next;
        if (this.last == node) {
            this.last = node2;
        }
        if (this.count.getAndDecrement() == this.capacity) {
            this.notFull.signal();
        }
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.concurrent.BlockingQueue
    public boolean remove(Object obj) {
        if (obj == null) {
            return false;
        }
        fullyLock();
        try {
            Node<E> node = this.head;
            Node<E> node2 = node.next;
            while (true) {
                Node<E> node3 = node2;
                Node<E> node4 = node;
                node = node3;
                if (node == null) {
                    return false;
                }
                if (!obj.equals(node.getValue())) {
                    node2 = node.next;
                } else {
                    unlink(node, node4);
                    fullyUnlock();
                    return true;
                }
            }
        } finally {
            fullyUnlock();
        }
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.util.concurrent.BlockingQueue
    public boolean contains(Object obj) {
        if (obj == null) {
            return false;
        }
        fullyLock();
        try {
            Node node = this.head;
            do {
                node = node.next;
                if (node == null) {
                    return false;
                }
            } while (!obj.equals(node.getValue()));
            fullyUnlock();
            return true;
        } finally {
            fullyUnlock();
        }
    }

    @Override // java.util.AbstractCollection, java.util.Collection
    public Object[] toArray() {
        fullyLock();
        try {
            Object[] objArr = new Object[this.count.get()];
            Node node = this.head.next;
            int i = 0;
            while (node != null) {
                int i2 = i + 1;
                objArr[i] = node.getValue();
                node = node.next;
                i = i2;
            }
            return objArr;
        } finally {
            fullyUnlock();
        }
    }

    @Override // java.util.AbstractCollection, java.util.Collection
    public <T> T[] toArray(T[] tArr) {
        fullyLock();
        try {
            int i = this.count.get();
            if (tArr.length < i) {
                tArr = (T[]) ((Object[]) Array.newInstance(tArr.getClass().getComponentType(), i));
            }
            Node node = this.head.next;
            int i2 = 0;
            while (node != null) {
                tArr[i2] = node.getValue();
                node = node.next;
                i2++;
            }
            if (tArr.length > i2) {
                tArr[i2] = null;
            }
            return tArr;
        } finally {
            fullyUnlock();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v11 */
    /* JADX WARN: Type inference failed for: r0v12 */
    /* JADX WARN: Type inference failed for: r0v2, types: [com.lidroid.xutils.task.Node, com.lidroid.xutils.task.Node<T>] */
    @Override // java.util.AbstractQueue, java.util.AbstractCollection, java.util.Collection
    public void clear() {
        fullyLock();
        try {
            ?? r0 = this.head;
            while (true) {
                Node node = r0.next;
                if (node == null) {
                    break;
                }
                r0.next = r0;
                node.setValue(null);
                r0 = (Node<E>) node;
            }
            this.head = this.last;
            if (this.count.getAndSet(0) == this.capacity) {
                this.notFull.signal();
            }
        } finally {
            fullyUnlock();
        }
    }

    @Override // java.util.concurrent.BlockingQueue
    public int drainTo(Collection<? super E> collection) {
        return drainTo(collection, ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
    }

    @Override // java.util.concurrent.BlockingQueue
    public int drainTo(Collection<? super E> collection, int i) {
        int andAdd;
        int i2;
        collection.getClass();
        if (collection == this) {
            throw new IllegalArgumentException();
        }
        boolean z = false;
        if (i <= 0) {
            return 0;
        }
        ReentrantLock reentrantLock = this.takeLock;
        reentrantLock.lock();
        try {
            int iMin = Math.min(i, this.count.get());
            Node<E> node = this.head;
            int i3 = 0;
            while (i3 < iMin) {
                try {
                    Node node2 = node.next;
                    collection.add((Object) node2.getValue());
                    node2.setValue(null);
                    node.next = (Node<T>) node;
                    i3++;
                    node = (Node<E>) node2;
                } finally {
                    if (i3 > 0) {
                        this.head = (Node<E>) node;
                        if (this.count.getAndAdd(-i3) == this.capacity) {
                        }
                    }
                }
            }
            if (i3 > 0) {
                if (andAdd == i2) {
                    z = true;
                }
            }
            return iMin;
        } finally {
            reentrantLock.unlock();
            if (0 != 0) {
                signalNotFull();
            }
        }
    }

    @Override // java.util.AbstractCollection, java.util.Collection, java.lang.Iterable
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        private Node<E> current;
        private E currentElement;
        private Node<E> lastRet;

        Itr() {
            PriorityObjectBlockingQueue.this.fullyLock();
            try {
                Node<E> node = PriorityObjectBlockingQueue.this.head.next;
                this.current = node;
                if (node != null) {
                    this.currentElement = node.getValue();
                }
            } finally {
                PriorityObjectBlockingQueue.this.fullyUnlock();
            }
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.current != null;
        }

        private Node<E> nextNode(Node<E> node) {
            Node<E> node2;
            while (true) {
                node2 = node.next;
                if (node2 == node) {
                    return PriorityObjectBlockingQueue.this.head.next;
                }
                if (node2 == null || node2.getValue() != null) {
                    break;
                }
                node = node2;
            }
            return node2;
        }

        @Override // java.util.Iterator
        public E next() {
            PriorityObjectBlockingQueue.this.fullyLock();
            try {
                Node<E> node = this.current;
                if (node == null) {
                    throw new NoSuchElementException();
                }
                E e = this.currentElement;
                this.lastRet = node;
                Node<E> nodeNextNode = nextNode(node);
                this.current = nodeNextNode;
                this.currentElement = nodeNextNode == null ? null : nodeNextNode.getValue();
                return e;
            } finally {
                PriorityObjectBlockingQueue.this.fullyUnlock();
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:9:0x001b, code lost:
        
            r4.this$0.unlink(r1, r2);
         */
        /* JADX WARN: Type inference fix 'apply assigned field type' failed
        java.lang.UnsupportedOperationException: ArgType.getObject(), call class: class jadx.core.dex.instructions.args.ArgType$UnknownArg
        	at jadx.core.dex.instructions.args.ArgType.getObject(ArgType.java:593)
        	at jadx.core.dex.attributes.nodes.ClassTypeVarsAttr.getTypeVarsMapFor(ClassTypeVarsAttr.java:35)
        	at jadx.core.dex.nodes.utils.TypeUtils.replaceClassGenerics(TypeUtils.java:177)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.insertExplicitUseCast(FixTypesVisitor.java:397)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryFieldTypeWithNewCasts(FixTypesVisitor.java:359)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.applyFieldType(FixTypesVisitor.java:309)
        	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:94)
         */
        @Override // java.util.Iterator
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void remove() {
            /*
                r4 = this;
                com.lidroid.xutils.task.Node<E> r0 = r4.lastRet
                if (r0 == 0) goto L31
                com.lidroid.xutils.task.PriorityObjectBlockingQueue r0 = com.lidroid.xutils.task.PriorityObjectBlockingQueue.this
                r0.fullyLock()
                com.lidroid.xutils.task.Node<E> r0 = r4.lastRet     // Catch: java.lang.Throwable -> L2a
                r1 = 0
                r4.lastRet = r1     // Catch: java.lang.Throwable -> L2a
                com.lidroid.xutils.task.PriorityObjectBlockingQueue r1 = com.lidroid.xutils.task.PriorityObjectBlockingQueue.this     // Catch: java.lang.Throwable -> L2a
                com.lidroid.xutils.task.Node<E> r1 = r1.head     // Catch: java.lang.Throwable -> L2a
                com.lidroid.xutils.task.Node<T> r2 = r1.next     // Catch: java.lang.Throwable -> L2a
            L14:
                r3 = r2
                r2 = r1
                r1 = r3
                if (r1 == 0) goto L24
                if (r1 != r0) goto L21
                com.lidroid.xutils.task.PriorityObjectBlockingQueue r0 = com.lidroid.xutils.task.PriorityObjectBlockingQueue.this     // Catch: java.lang.Throwable -> L2a
                r0.unlink(r1, r2)     // Catch: java.lang.Throwable -> L2a
                goto L24
            L21:
                com.lidroid.xutils.task.Node<T> r2 = r1.next     // Catch: java.lang.Throwable -> L2a
                goto L14
            L24:
                com.lidroid.xutils.task.PriorityObjectBlockingQueue r0 = com.lidroid.xutils.task.PriorityObjectBlockingQueue.this
                r0.fullyUnlock()
                return
            L2a:
                r0 = move-exception
                com.lidroid.xutils.task.PriorityObjectBlockingQueue r1 = com.lidroid.xutils.task.PriorityObjectBlockingQueue.this
                r1.fullyUnlock()
                throw r0
            L31:
                java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
                r0.<init>()
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.lidroid.xutils.task.PriorityObjectBlockingQueue.Itr.remove():void");
        }
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        fullyLock();
        try {
            objectOutputStream.defaultWriteObject();
            Node node = this.head;
            while (true) {
                node = node.next;
                if (node != null) {
                    objectOutputStream.writeObject(node.getValue());
                } else {
                    objectOutputStream.writeObject(null);
                    return;
                }
            }
        } finally {
            fullyUnlock();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void readObject(ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {
        objectInputStream.defaultReadObject();
        this.count.set(0);
        Node<E> node = new Node<>(null);
        this.head = node;
        this.last = node;
        while (true) {
            Object object = objectInputStream.readObject();
            if (object == null) {
                return;
            } else {
                add(object);
            }
        }
    }
}
