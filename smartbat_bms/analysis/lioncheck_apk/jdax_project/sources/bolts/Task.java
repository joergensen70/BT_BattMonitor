package bolts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/* JADX INFO: loaded from: classes.dex */
public class Task<TResult> {
    public static final ExecutorService BACKGROUND_EXECUTOR = BoltsExecutors.background();
    private static final Executor IMMEDIATE_EXECUTOR = BoltsExecutors.immediate();
    public static final Executor UI_THREAD_EXECUTOR = AndroidExecutors.uiThread();
    private boolean cancelled;
    private boolean complete;
    private Exception error;
    private TResult result;
    private final Object lock = new Object();
    private List<Continuation<TResult, Void>> continuations = new ArrayList();

    /* JADX WARN: Multi-variable type inference failed */
    public <TOut> Task<TOut> cast() {
        return this;
    }

    private Task() {
    }

    public static <TResult> Task<TResult>.TaskCompletionSource create() {
        return new TaskCompletionSource();
    }

    public boolean isCompleted() {
        boolean z;
        synchronized (this.lock) {
            z = this.complete;
        }
        return z;
    }

    public boolean isCancelled() {
        boolean z;
        synchronized (this.lock) {
            z = this.cancelled;
        }
        return z;
    }

    public boolean isFaulted() {
        boolean z;
        synchronized (this.lock) {
            z = this.error != null;
        }
        return z;
    }

    public TResult getResult() {
        TResult tresult;
        synchronized (this.lock) {
            tresult = this.result;
        }
        return tresult;
    }

    public Exception getError() {
        Exception exc;
        synchronized (this.lock) {
            exc = this.error;
        }
        return exc;
    }

    public void waitForCompletion() throws InterruptedException {
        synchronized (this.lock) {
            if (!isCompleted()) {
                this.lock.wait();
            }
        }
    }

    public static <TResult> Task<TResult> forResult(TResult tresult) {
        TaskCompletionSource taskCompletionSourceCreate = create();
        taskCompletionSourceCreate.setResult(tresult);
        return taskCompletionSourceCreate.getTask();
    }

    public static <TResult> Task<TResult> forError(Exception exc) {
        TaskCompletionSource taskCompletionSourceCreate = create();
        taskCompletionSourceCreate.setError(exc);
        return taskCompletionSourceCreate.getTask();
    }

    public static <TResult> Task<TResult> cancelled() {
        TaskCompletionSource taskCompletionSourceCreate = create();
        taskCompletionSourceCreate.setCancelled();
        return taskCompletionSourceCreate.getTask();
    }

    public Task<Void> makeVoid() {
        return continueWithTask(new Continuation<TResult, Task<Void>>() { // from class: bolts.Task.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // bolts.Continuation
            public Task<Void> then(Task<TResult> task) throws Exception {
                if (task.isCancelled()) {
                    return Task.cancelled();
                }
                if (task.isFaulted()) {
                    return Task.forError(task.getError());
                }
                return Task.forResult(null);
            }
        });
    }

    public static <TResult> Task<TResult> callInBackground(Callable<TResult> callable) {
        return call(callable, BACKGROUND_EXECUTOR);
    }

    public static <TResult> Task<TResult> call(final Callable<TResult> callable, Executor executor) {
        final TaskCompletionSource taskCompletionSourceCreate = create();
        executor.execute(new Runnable() { // from class: bolts.Task.2
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.lang.Runnable
            public void run() {
                try {
                    taskCompletionSourceCreate.setResult(callable.call());
                } catch (Exception e) {
                    taskCompletionSourceCreate.setError(e);
                }
            }
        });
        return taskCompletionSourceCreate.getTask();
    }

    public static <TResult> Task<TResult> call(Callable<TResult> callable) {
        return call(callable, IMMEDIATE_EXECUTOR);
    }

    public static Task<Void> whenAll(Collection<? extends Task<?>> collection) {
        if (collection.size() == 0) {
            return forResult(null);
        }
        final TaskCompletionSource taskCompletionSourceCreate = create();
        final ArrayList arrayList = new ArrayList();
        final Object obj = new Object();
        final AtomicInteger atomicInteger = new AtomicInteger(collection.size());
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        Iterator<? extends Task<?>> it = collection.iterator();
        while (it.hasNext()) {
            it.next().continueWith(new Continuation<Object, Void>() { // from class: bolts.Task.3
                @Override // bolts.Continuation
                public Void then(Task<Object> task) {
                    if (task.isFaulted()) {
                        synchronized (obj) {
                            arrayList.add(task.getError());
                        }
                    }
                    if (task.isCancelled()) {
                        atomicBoolean.set(true);
                    }
                    if (atomicInteger.decrementAndGet() == 0) {
                        if (arrayList.size() != 0) {
                            if (arrayList.size() == 1) {
                                taskCompletionSourceCreate.setError((Exception) arrayList.get(0));
                            } else {
                                ArrayList arrayList2 = arrayList;
                                taskCompletionSourceCreate.setError(new AggregateException(String.format("There were %d exceptions.", Integer.valueOf(arrayList.size())), (Throwable[]) arrayList2.toArray(new Throwable[arrayList2.size()])));
                            }
                        } else if (atomicBoolean.get()) {
                            taskCompletionSourceCreate.setCancelled();
                        } else {
                            taskCompletionSourceCreate.setResult(null);
                        }
                    }
                    return null;
                }
            });
        }
        return taskCompletionSourceCreate.getTask();
    }

    public Task<Void> continueWhile(Callable<Boolean> callable, Continuation<Void, Task<Void>> continuation) {
        return continueWhile(callable, continuation, IMMEDIATE_EXECUTOR);
    }

    public Task<Void> continueWhile(final Callable<Boolean> callable, final Continuation<Void, Task<Void>> continuation, final Executor executor) {
        final Capture capture = new Capture();
        capture.set(new Continuation<Void, Task<Void>>() { // from class: bolts.Task.4
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // bolts.Continuation
            public Task<Void> then(Task<Void> task) throws Exception {
                if (((Boolean) callable.call()).booleanValue()) {
                    return Task.forResult(null).onSuccessTask(continuation, executor).onSuccessTask((Continuation) capture.get(), executor);
                }
                return Task.forResult(null);
            }
        });
        return makeVoid().continueWithTask((Continuation) capture.get(), executor);
    }

    public <TContinuationResult> Task<TContinuationResult> continueWith(final Continuation<TResult, TContinuationResult> continuation, final Executor executor) {
        boolean zIsCompleted;
        final TaskCompletionSource taskCompletionSourceCreate = create();
        synchronized (this.lock) {
            zIsCompleted = isCompleted();
            if (!zIsCompleted) {
                this.continuations.add(new Continuation<TResult, Void>() { // from class: bolts.Task.5
                    @Override // bolts.Continuation
                    public Void then(Task<TResult> task) {
                        Task.completeImmediately(taskCompletionSourceCreate, continuation, task, executor);
                        return null;
                    }
                });
            }
        }
        if (zIsCompleted) {
            completeImmediately(taskCompletionSourceCreate, continuation, this, executor);
        }
        return taskCompletionSourceCreate.getTask();
    }

    public <TContinuationResult> Task<TContinuationResult> continueWith(Continuation<TResult, TContinuationResult> continuation) {
        return continueWith(continuation, IMMEDIATE_EXECUTOR);
    }

    public <TContinuationResult> Task<TContinuationResult> continueWithTask(final Continuation<TResult, Task<TContinuationResult>> continuation, final Executor executor) {
        boolean zIsCompleted;
        final TaskCompletionSource taskCompletionSourceCreate = create();
        synchronized (this.lock) {
            zIsCompleted = isCompleted();
            if (!zIsCompleted) {
                this.continuations.add(new Continuation<TResult, Void>() { // from class: bolts.Task.6
                    @Override // bolts.Continuation
                    public Void then(Task<TResult> task) {
                        Task.completeAfterTask(taskCompletionSourceCreate, continuation, task, executor);
                        return null;
                    }
                });
            }
        }
        if (zIsCompleted) {
            completeAfterTask(taskCompletionSourceCreate, continuation, this, executor);
        }
        return taskCompletionSourceCreate.getTask();
    }

    public <TContinuationResult> Task<TContinuationResult> continueWithTask(Continuation<TResult, Task<TContinuationResult>> continuation) {
        return continueWithTask(continuation, IMMEDIATE_EXECUTOR);
    }

    public <TContinuationResult> Task<TContinuationResult> onSuccess(final Continuation<TResult, TContinuationResult> continuation, Executor executor) {
        return continueWithTask(new Continuation<TResult, Task<TContinuationResult>>() { // from class: bolts.Task.7
            @Override // bolts.Continuation
            public Task<TContinuationResult> then(Task<TResult> task) {
                if (task.isFaulted()) {
                    return Task.forError(task.getError());
                }
                if (task.isCancelled()) {
                    return Task.cancelled();
                }
                return task.continueWith(continuation);
            }
        }, executor);
    }

    public <TContinuationResult> Task<TContinuationResult> onSuccess(Continuation<TResult, TContinuationResult> continuation) {
        return onSuccess(continuation, IMMEDIATE_EXECUTOR);
    }

    public <TContinuationResult> Task<TContinuationResult> onSuccessTask(final Continuation<TResult, Task<TContinuationResult>> continuation, Executor executor) {
        return continueWithTask(new Continuation<TResult, Task<TContinuationResult>>() { // from class: bolts.Task.8
            @Override // bolts.Continuation
            public Task<TContinuationResult> then(Task<TResult> task) {
                if (task.isFaulted()) {
                    return Task.forError(task.getError());
                }
                if (task.isCancelled()) {
                    return Task.cancelled();
                }
                return task.continueWithTask(continuation);
            }
        }, executor);
    }

    public <TContinuationResult> Task<TContinuationResult> onSuccessTask(Continuation<TResult, Task<TContinuationResult>> continuation) {
        return onSuccessTask(continuation, IMMEDIATE_EXECUTOR);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <TContinuationResult, TResult> void completeImmediately(final Task<TContinuationResult>.TaskCompletionSource taskCompletionSource, final Continuation<TResult, TContinuationResult> continuation, final Task<TResult> task, Executor executor) {
        executor.execute(new Runnable() { // from class: bolts.Task.9
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.lang.Runnable
            public void run() {
                try {
                    taskCompletionSource.setResult(continuation.then(task));
                } catch (Exception e) {
                    taskCompletionSource.setError(e);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static <TContinuationResult, TResult> void completeAfterTask(final Task<TContinuationResult>.TaskCompletionSource taskCompletionSource, final Continuation<TResult, Task<TContinuationResult>> continuation, final Task<TResult> task, Executor executor) {
        executor.execute(new Runnable() { // from class: bolts.Task.10
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Task task2 = (Task) continuation.then(task);
                    if (task2 == null) {
                        taskCompletionSource.setResult(null);
                    } else {
                        task2.continueWith(new Continuation<TContinuationResult, Void>() { // from class: bolts.Task.10.1
                            @Override // bolts.Continuation
                            public Void then(Task<TContinuationResult> task3) {
                                if (task3.isCancelled()) {
                                    taskCompletionSource.setCancelled();
                                    return null;
                                }
                                if (task3.isFaulted()) {
                                    taskCompletionSource.setError(task3.getError());
                                    return null;
                                }
                                taskCompletionSource.setResult(task3.getResult());
                                return null;
                            }
                        });
                    }
                } catch (Exception e) {
                    taskCompletionSource.setError(e);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void runContinuations() {
        synchronized (this.lock) {
            Iterator<Continuation<TResult, Void>> it = this.continuations.iterator();
            while (it.hasNext()) {
                try {
                    it.next().then(this);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e2) {
                    throw new RuntimeException(e2);
                }
            }
            this.continuations = null;
        }
    }

    public class TaskCompletionSource {
        private TaskCompletionSource() {
        }

        public Task<TResult> getTask() {
            return Task.this;
        }

        public boolean trySetCancelled() {
            synchronized (Task.this.lock) {
                if (Task.this.complete) {
                    return false;
                }
                Task.this.complete = true;
                Task.this.cancelled = true;
                Task.this.lock.notifyAll();
                Task.this.runContinuations();
                return true;
            }
        }

        public boolean trySetResult(TResult tresult) {
            synchronized (Task.this.lock) {
                if (Task.this.complete) {
                    return false;
                }
                Task.this.complete = true;
                Task.this.result = tresult;
                Task.this.lock.notifyAll();
                Task.this.runContinuations();
                return true;
            }
        }

        public boolean trySetError(Exception exc) {
            synchronized (Task.this.lock) {
                if (Task.this.complete) {
                    return false;
                }
                Task.this.complete = true;
                Task.this.error = exc;
                Task.this.lock.notifyAll();
                Task.this.runContinuations();
                return true;
            }
        }

        public void setCancelled() {
            if (!trySetCancelled()) {
                throw new IllegalStateException("Cannot cancel a completed task.");
            }
        }

        public void setResult(TResult tresult) {
            if (!trySetResult(tresult)) {
                throw new IllegalStateException("Cannot set the result of a completed task.");
            }
        }

        public void setError(Exception exc) {
            if (!trySetError(exc)) {
                throw new IllegalStateException("Cannot set the error on a completed task.");
            }
        }
    }
}
