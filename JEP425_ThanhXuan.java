import java.util.concurrent.*;
/**
 Lớp này so sánh cách tạo và quản lý luồng (thread) trước và sau khi có JEP 425 (Virtual Threads – Java 19+).
 JEP 425 giới thiệu "Virtual Threads" – luồng nhẹ (lightweight threads) giúp xử lý đồng thời hàng chục nghìn tác vụ mà vẫn tiết kiệm tài nguyên.
 */
public class VirtualThreadComparison {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("---Cách làm cũ (Trước JEP 425 / Platform Threads) ---");
        demoPlatformThreads();
        System.out.println("\n---Cách làm mới (Với JEP 425 / Virtual Threads) ---");
        demoVirtualThreads();
        System.out.println("\n---So sánh và chứng minh sự khác biệt ---");
        demoPerformanceComparison();
    }
    /**
     Minh họa cách tạo và chạy nhiều luồng trước khi có Virtual Threads.
     Sử dụng Fixed Thread Pool thông thường.
     */
    public static void demoPlatformThreads() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10); // 10 luồng vật lý thật
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " chạy trên " + Thread.currentThread());
                try {
                    Thread.sleep(500); // mô phỏng tác vụ I/O
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        long end = System.currentTimeMillis();
        System.out.println("Thời gian (Platform Threads): " + (end - start) + " ms");
        System.out.println("* Nhận xét: Bị giới hạn bởi số lượng luồng thật, tiêu tốn bộ nhớ.");
    }
    /**
     Minh họa cách tạo và chạy nhiều luồng ảo (Virtual Threads) với JEP 425.
     Mỗi tác vụ được chạy trên một Virtual Thread riêng.
     */
    public static void demoVirtualThreads() throws InterruptedException {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                int taskId = i;
                executor.submit(() -> {
                    System.out.println("Task " + taskId + " chạy trên " + Thread.currentThread());
                    try {
                        Thread.sleep(500); // mô phỏng I/O
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
// executor sẽ tự shutdown() khi thoát khỏi try-with-resources
            long end = System.currentTimeMillis();
            System.out.println("Thời gian (Virtual Threads): " + (end - start) + " ms");
        }
        System.out.println("* Nhận xét: Có thể tạo hàng nghìn luồng mà không lo tràn bộ nhớ.");
    }

    /**
     So sánh hiệu năng khi tạo số lượng lớn luồng (1000 luồng).
     Cho thấy Virtual Threads nhẹ và hiệu quả hơn nhiều.
     */
    public static void demoPerformanceComparison() {
        int numTasks = 1000;
        System.out.println("So sánh tạo " + numTasks + " luồng (Platform vs Virtual):");

//Platform Threads
        long start1 = System.currentTimeMillis();
        Thread[] threads1 = new Thread[numTasks];
        for (int i = 0; i < numTasks; i++) {
            threads1[i] = new Thread(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
            });
            threads1[i].start();
        }
        for (Thread t : threads1) {
            try { t.join(); } catch (InterruptedException ignored) {}
        }
        long end1 = System.currentTimeMillis();
//Virtual Threads
        long start2 = System.currentTimeMillis();
        Thread[] threads2 = new Thread[numTasks];
        for (int i = 0; i < numTasks; i++) {
            threads2[i] = Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
            });
        }
        for (Thread t : threads2) {
            try { t.join(); } catch (InterruptedException ignored) {}
        }
        long end2 = System.currentTimeMillis();

        System.out.println("Platform Threads: " + (end1 - start1) + " ms");
        System.out.println("Virtual Threads : " + (end2 - start2) + " ms");

        System.out.println("* Kết luận: Virtual Threads khởi tạo nhanh, nhẹ và có thể mở rộng tốt hơn nhiều.");
    }
}