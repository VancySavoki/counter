package org.trivia.counter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class CounterServiceImpl implements CounterService {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final Logger log = LoggerFactory.getLogger(CounterService.class);
    private static final Path data = Paths.get("counter.txt");
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final AtomicBoolean dirty = new AtomicBoolean(false);
    @Override
    public int get() {
        return counter.get();
    }

    @Override
    public int incrementAndGet() {
        dirty.getAndSet(true);
        return counter.incrementAndGet();
    }

    /**
     * 每分钟持久化一次
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void persist() {
        if(dirty.get()) {
            Lock writeLock = this.lock.writeLock();

            try {
                if(writeLock.tryLock(1, TimeUnit.SECONDS)) {
                    if(Files.isWritable(data)) {
                        int i = counter.get();
                        Files.write(data, Integer.toString(i, 16).getBytes());
                        dirty.getAndSet(false);
                        log.info("Save data success, current value is {}", i);
                    } else {
                        throw new IllegalStateException("Files counter.txt could not be override!");
                    }
                }
            } catch (Throwable e) {
                log.error("Failed to persist counter data", e);
            } finally {
                writeLock.unlock();

            }
        }

    }
}
