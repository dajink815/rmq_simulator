package com.uangel.executor;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.List;
import java.util.concurrent.*;


/**
 *
 * @author kangmoo Heo
 */

public class UExecutorManager {
    private static final UExecutorManager INSTANCE = new UExecutorManager();
    private UScheduledExecutorService executor;
    private int corePoolSize = Runtime.getRuntime().availableProcessors();
    private ThreadFactory threadFactory = new BasicThreadFactory.Builder()
            .namingPattern("UExecutor-%d")
            .daemon(true)
            .priority(Thread.MAX_PRIORITY)
            .build();

    private UExecutorManager() {
    }

    public static UExecutorManager getInstance() {
        return INSTANCE;
    }

    public void init(){
        executor = new UScheduledExecutorService(corePoolSize, threadFactory);
    }

    public void init(int corePoolSize){
        this.corePoolSize = corePoolSize;
        this.init();
    }

    public void init(int corePoolSize, ThreadFactory threadFactory){
        this.corePoolSize = corePoolSize;
        this.threadFactory = threadFactory;
        this.init();
    }

    public void execute(Runnable command) {
        executor.execute(command);
    }

    public CompletableFuture<?> submit(Runnable command) {
        return executor.submit(command);
    }

    public CompletableFuture<?> submit(Callable command) {
        return executor.submit(command);
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executor.schedule(command, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public void shutdown() {
        executor.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }

    public UScheduledExecutorService getExecutor() {
        return executor;
    }
}
