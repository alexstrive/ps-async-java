package com.alexstrive;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class MainClass {

    record Quotation(String server, int amount) {
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        syncRun();
        executorServiceRun();
        completableFutureRun();
    }

    public static void syncRun() {
        var random = new Random();

        Callable<Quotation> fetchQuotationA = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("Server A", random.nextInt(40, 60));
        };

        Callable<Quotation> fetchQuotationB = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("Server B", random.nextInt(30, 70));
        };

        Callable<Quotation> fetchQuotationC = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("Server B", random.nextInt(40, 80));
        };

        var tasks = List.of(fetchQuotationA, fetchQuotationB, fetchQuotationC);

        var begin = Instant.now();

        var bestQuotation = tasks.stream()
                .map(MainClass::fetchQuotation)
                .min(Comparator.comparing(Quotation::amount))
                .orElseThrow();

        var end = Instant.now();
        var duration = Duration.between(begin, end);
        System.out.println("Best quotation [SYNC]  = " + bestQuotation + "(" + duration.toMillis() + ")");
    }

    public static void executorServiceRun() throws ExecutionException, InterruptedException {
        var random = new Random();

        Callable<Quotation> fetchQuotationA = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("Server A", random.nextInt(40, 60));
        };

        Callable<Quotation> fetchQuotationB = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("Server B", random.nextInt(30, 70));
        };

        Callable<Quotation> fetchQuotationC = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("Server B", random.nextInt(40, 80));
        };

        var tasks = List.of(fetchQuotationA, fetchQuotationB, fetchQuotationC);

        var executor = Executors.newFixedThreadPool(4);
        var begin = Instant.now();

        var futures = new ArrayList<Future<Quotation>>();
        for (var task : tasks) {
            Future<Quotation> future = executor.submit(task);
            futures.add(future);
        }

        var quotations = new ArrayList<Quotation>();
        for (var future : futures) {
            Quotation quotation = future.get();
            quotations.add(quotation);
        }

        Quotation bestQuotation = quotations.stream()
                .min(Comparator.comparing(Quotation::amount))
                .orElseThrow();

        var end = Instant.now();
        var duration = Duration.between(begin, end);
        System.out.println("Best quotation [ES]    = " + bestQuotation + "(" + duration.toMillis() + ")");
        executor.shutdown();
    }

    public static void completableFutureRun() {
        var random = new Random();

        Supplier<Quotation> fetchQuotationA = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return new Quotation("Server A", random.nextInt(40, 60));
        };

        Supplier<Quotation> fetchQuotationB = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return new Quotation("Server A", random.nextInt(30, 70));
        };

        Supplier<Quotation> fetchQuotationC = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return new Quotation("Server A", random.nextInt(40, 80));
        };

        var quotationsTasks = List.of(fetchQuotationA, fetchQuotationB, fetchQuotationC);

        var begin = Instant.now();

        var futures = new ArrayList<CompletableFuture<Quotation>>();
        for (var quotationTask : quotationsTasks) {
            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(quotationTask);
            futures.add(future);
        }

        var quotations = new ArrayList<Quotation>();
        for (var future : futures) {
            var quotation = future.join();
            quotations.add(quotation);
        }

        var bestQuotation = quotations.stream()
                .min(Comparator.comparing(Quotation::amount))
                .orElseThrow();

        var end = Instant.now();
        var duration = Duration.between(begin, end);
        System.out.println("Best quotation [ASYNC] = " + bestQuotation + "(" + duration.toMillis() + ")");
    }

    private static Quotation fetchQuotation(Callable<Quotation> task) {
        try {
            return task.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}