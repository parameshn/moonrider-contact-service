package com.moonrider.zamazon.performance;

import com.moonrider.zamazon.dto.IdentifyRequest;
import com.moonrider.zamazon.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@ActiveProfiles("test")

public class ContactServicePerformanceTest {

    @Autowired
    private ContactService contactService;

    @Test
    void testConcurrentRequests() throws InterruptedException {
        int numberOfThreads = 10;
        int requestsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        IdentifyRequest request = new IdentifyRequest(
                                "user" + threadId + "_" + j + "@timelab.com",
                                "555" + String.format("%03d", threadId) + String.format("%03d", j));
                        contactService.identify(request);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Performance Test Results:");
        System.out.println("Total requests: " + (numberOfThreads * requestsPerThread));
        System.out.println("Successful: " + successCount.get());
        System.out.println("Failed: " + errorCount.get());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("Requests per second: " + (successCount.get() * 1000.0 / duration));

        // Assert reasonable performance
        assert successCount.get() > (numberOfThreads * requestsPerThread * 0.95); // 95% success rate
        assert duration < 10000; // Complete within 10 seconds
    }
}
