/*
 * Copyright(C) 2025 ACCELaero
 * All rights reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
 * Information Systems Associates (pvt) Ltd.
 *
 * This copy of the Source Code is intended for Information Systems Associates (pvt) Ltd's internal
 * use only and is intended for view by persons duly authorized by the management of
 * Information Systems Associates (pvt) Ltd. No part of this file may be reproduced or distributed
 * in any form or by any means without the written approval of the Management of
 * Information Systems Associates (pvt) Ltd.
 */

package com.hexnotech.commons.type.async;

import com.hexnotech.commons.exception.HexnotechProcessException;
import com.hexnotech.commons.type.generic.ListSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class AsyncProcessor {

    private long timeout = 120000; // 2 minutes
    private final List<AsyncProcess> processors = new ArrayList<>();
    private final List<AsyncDataProvider<?>> asyncDataProviders = new ArrayList<>();

    public AsyncProcessor() {
    }

    public AsyncProcessor(long timeout) {
        this.timeout = timeout;
    }

    public AsyncProcessor addProcessor(AsyncProcess processor) {
        processors.add(processor);
        return this;
    }

    public <T> AsyncDataListProvider<T> getProviderByList(ListSupplier<T> supplier) {
        AsyncDataListProvider<T> listSupplier = new AsyncDataListProvider<>(supplier);
        asyncDataProviders.add(listSupplier);
        return listSupplier;
    }

    public <T> AsyncDataProvider<T> getProviderBySupplier(Supplier<T> supplier) {
        AsyncDataProvider<T> asyncDataProvider = new AsyncDataProvider<>(supplier);
        asyncDataProviders.add(asyncDataProvider);
        return asyncDataProvider;
    }

    public void execute() throws HexnotechProcessException {

        int totalProcessors = processors.size() + asyncDataProviders.size();

        final CountDownLatch latch = new CountDownLatch(totalProcessors);

        ExecutorService executorService = Executors.newFixedThreadPool(totalProcessors);

        List<Future<?>> futures = new ArrayList<>(asyncDataProviders.size());

        //Processing async suppliers
        for (final AsyncDataProvider<?> asyncDataProvider : asyncDataProviders) {
            Future<?> submitted = executorService.submit((Callable<Object>) () -> {
                try {
                    return asyncDataProvider.process();
                } catch (Exception ex) {
                    throw new HexnotechProcessException("Error processing async task", ex);
                } finally {
                    latch.countDown();
                }
            });
            asyncDataProvider.setReference(submitted);
            futures.add(submitted);
        }

        // Processing async processors
        for (final AsyncProcess processor : processors) {
            executorService.submit(() -> {
                try {
                    processor.process();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            boolean await = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (!await) {
                for (Future<?> future : futures) {
                    if (!future.isDone()) {
                        future.cancel(true);
                    }
                }
                throw new HexnotechProcessException("Timeout while waiting for async tasks to complete");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HexnotechProcessException("Thread interrupted while waiting for async tasks to complete", e);
        } catch (Exception e) {
            throw new HexnotechProcessException("Error while executing async tasks", e);
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

    }
}
