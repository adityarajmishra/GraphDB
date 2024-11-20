/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.graphdb.server;

import com.graphdb.core.GraphDatabase;
import com.graphdb.core.CommandProcessor;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class App {
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        GraphDatabase graphDb = new GraphDatabase();
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        CommandProcessor processor = new CommandProcessor(graphDb);

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine().trim();
                Future<String> future = executorService.submit(() -> processor.processCommand(command));

                try {
                    String result = future.get(5, TimeUnit.SECONDS);
                    System.out.println(result);

                    if (command.equalsIgnoreCase("STOP")) {
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("INVALID_COMMAND");
                }
            }
        } finally {
            shutdownExecutor(executorService);
        }
    }

    private static void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
