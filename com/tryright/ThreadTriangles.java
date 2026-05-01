package com.tryright;

import java.io.File;
import java.io.IOException;

/**
 * ThreadTriangles - counts right triangles using multiple threads
 * Uses PointStore interface to support both text and binary formats
 *
 * Usage: java com.tryright.ThreadTriangles <input_file> <num_threads>
 *
 * Unlike ProcessTriangles which uses separate processes and pipes for IPC,
 * this implementation uses threads that share the same memory space.
 * Communication is done through a shared results array; each thread
 * writes its count to its own slot, and the main thread sums them.
 */
public class ThreadTriangles {

    // Shared memory: array where each thread stores its result
    // Thread i writes to results[i], so no synchronization needed
    private static int[] results;

    // Shared memory: the PointStore (read-only for worker threads)
    private static PointStore store;

    // Shared immutable cached coordinates (loaded once)
    private static int[] xCoords;
    private static int[] yCoords;

    public static void main(String[] args) {
        String filename = validateAndGetFilename(args);
        int numThreads = parseAndValidateThreadCount(args);
        validateInputFile(filename);

        try {
            runTriangleCounting(filename, numThreads);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Permission denied")) {
                System.err.println("Error: Permission denied");
            } else {
                System.err.println("Error: " + e.getMessage());
            }
            System.exit(2);
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }

    private static String validateAndGetFilename(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java com.tryright.ThreadTriangles <input_file> <num_threads>");
            System.exit(1);
        }
        return args[0];
    }

    private static int parseAndValidateThreadCount(String[] args) {
        int numThreads;
        try {
            numThreads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: Number of threads must be an integer");
            System.exit(1);
            return -1;
        }

        if (numThreads <= 0) {
            System.err.println("Error: Number of threads must be positive");
            System.exit(1);
        }
        if (numThreads > 256) {
            System.err.println("Error: Number of threads cannot exceed 256");
            System.exit(1);
        }
        return numThreads;
    }

    private static void validateInputFile(String filename) {
        File inputFile = new File(filename);
        if (!inputFile.exists()) {
            System.err.println("Error: No such file or directory");
            System.exit(2);
        }
        if (!inputFile.canRead()) {
            System.err.println("Error: Permission denied");
            System.exit(2);
        }
    }

    private static void runTriangleCounting(String filename, int numThreads) throws IOException {
        // Create PointStore (appropriate implementation based on file extension)
        store = TrianglesUtils.createPointStore(filename);
        int numPoints = store.numPoints();

        // Handle small datasets with single thread
        if (numPoints < 3 || numThreads == 1) {
            int count = TrianglesUtils.countRightTriangles(store, 0, numPoints);
            System.out.println(count);
            return;
        }

        // Operational cap: don't exceed logical CPU cores (keep 256 as hard cap above)
        int logicalCores = Math.max(1, Runtime.getRuntime().availableProcessors());

        // Limit threads by dataset size and CPU cores
        int actualThreads = Math.min(numThreads, Math.min(numPoints, logicalCores));

        // Allocate shared results array - each thread gets one slot
        results = new int[actualThreads];

        // Load all points once so worker threads don't recopy them
        xCoords = new int[numPoints];
        yCoords = new int[numPoints];
        for (int k = 0; k < numPoints; k++) {
            xCoords[k] = store.getX(k);
            yCoords[k] = store.getY(k);
        }

        Thread[] workers = createWorkers(actualThreads, numPoints);
        startWorkers(workers);
        waitForWorkers(workers);
        System.out.println(sumResults());
    }

    private static Thread[] createWorkers(int actualThreads, int numPoints) {
        Thread[] workers = new Thread[actualThreads];
        int pointsPerThread = (numPoints + actualThreads - 1) / actualThreads;

        for (int i = 0; i < actualThreads; i++) {
            final int threadIndex = i;
            final int startIdx = i * pointsPerThread;
            final int endIdx = Math.min((i + 1) * pointsPerThread, numPoints);

            if (startIdx >= numPoints) {
                break;
            }

            // Each thread reads from shared 'store' and writes to results[threadIndex]
            workers[i] = new Thread(null, () -> {
                int count = TrianglesUtils.countRightTriangles(xCoords, yCoords, startIdx, endIdx);
                results[threadIndex] = count;
            }, "Worker-" + i, 512 * 1024); // 512KB stack
        }
        return workers;
    }

    private static void startWorkers(Thread[] workers) {
        for (Thread worker : workers) {
            if (worker != null) {
                worker.start();
            }
        }
    }

    private static void waitForWorkers(Thread[] workers) {
        for (Thread worker : workers) {
            if (worker != null) {
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    System.err.println("Error: Thread interrupted");
                    System.exit(1);
                }
            }
        }
    }

    private static int sumResults() {
        int totalCount = 0;
        for (int count : results) {
            totalCount += count;
        }
        return totalCount;
    }
}
