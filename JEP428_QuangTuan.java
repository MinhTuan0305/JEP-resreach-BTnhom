/*
==========================================================
JEP 428 - Structured Concurrency (Incubator)
Người thực hiện: Quang Tuấn
Mục tiêu: Đơn giản hóa lập trình đa luồng bằng cách giới thiệu API cho tính đồng thời có cấu trúc.
         Xử lý nhiều tác vụ chạy trong các luồng khác nhau như một đơn vị công việc duy nhất,
         hợp lý hóa việc xử lý và hủy lỗi, cải thiện độ tin cậy và nâng cao khả năng quan sát.
Phiên bản áp dụng: Java 19 (dưới dạng ươm tạo)
==========================================================
*/

// Cần import các thư viện ươm tạo
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class JEP428_QuangTuan {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Ví dụ 1: Tính đồng thời có cấu trúc (JEP 428) ===");
        runStructuredTasks();

        System.out.println("\n=== Ví dụ 2: Xử lý lỗi (JEP 428) ===");
        runStructuredTasksWithError();
    }

    // ===================== Ví dụ 1: Tính đồng thời có cấu trúc =====================
    static void runStructuredTasks() throws InterruptedException {

        // Giải thích: Tạo một đối tượng StructuredTaskScope để quản lý các tác vụ con.
        // Sử dụng cấu trúc try-with-resources đảm bảo rằng scope sẽ tự động được đóng.
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            // Giải thích: Phương thức fork() được sử dụng để khởi động một tác vụ con mới.
            scope.fork(() -> {
                System.out.println("Tác vụ 1 bắt đầu");
                Thread.sleep(500); // Giả lập công việc
                System.out.println("Tác vụ 1 kết thúc");
                return null; // Tác vụ <Void> trả về null
            });

            scope.fork(() -> {
                System.out.println("Tác vụ 2 bắt đầu");
                Thread.sleep(300); // Giả lập công việc
                System.out.println("Tác vụ 2 kết thúc");
                return null;
            });

            // Giải thích: Chờ tất cả các tác vụ con hoàn thành
            // (Phần này bắt buộc để scope cha đợi các tác vụ con)
            scope.join();

            // Giải thích: Kiểm tra nếu có lỗi và ném ra (thực hành tốt)
            scope.throwIfFailed();

            System.out.println("Tất cả tác vụ con đã hoàn thành.");

        } catch (ExecutionException e) {
            // Xử lý lỗi nếu throwIfFailed() ném ra
            e.printStackTrace();
        }
        // Khi khối try kết thúc, phương thức close() của scope sẽ được gọi tự động.
        // Điều này đảm bảo tất cả tác vụ con đã hoàn thành trước khi kết thúc phạm vi.
    }

    // ===================== Ví dụ 2: Xử lý lỗi =====================
    static void runStructuredTasksWithError() throws InterruptedException {

        // Giải thích: Khối catch bên ngoài sẽ bắt bất kỳ ngoại lệ
        // nào xảy ra trong khối try.
        try {
            // Giải thích: Tạo một đối tượng StructuredTaskScope để quản lý các tác vụ con.
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

                // Giải thích: Phương thức fork() được sử dụng để khởi động một tác vụ con mới.
                scope.fork(() -> {
                    System.out.println("Tác vụ con (sẽ lỗi) bắt đầu...");
                    // Giải thích: Ném ra ngoại lệ RuntimeException để mô phỏng lỗi.
                    throw new RuntimeException("Lỗi xảy ra trong tác vụ con");
                });

                // Giải thích: Chờ tác vụ hoàn thành (hoặc thất bại)
                scope.join();

                // Giải thích: Nếu tác vụ con ném ra ngoại lệ,
                // phương thức này sẽ ném ngoại lệ đó ra khối catch bên ngoài.
                scope.throwIfFailed();
            }

        } catch (Exception e) {
            // Giải thích: In ra thông báo lỗi đã được bắt.
            // Lỗi gốc thường được gói trong ExecutionException.
            System.out.println("Lỗi đã được bắt: " + e.getCause().getMessage());
        }
    }
}

/*

So sánh với các cách tiếp cận truyền thống:

- Trước Java 19 (dùng Thread, ExecutorService):
  Cách tiếp cận này thường phức tạp hơn và dễ dẫn đến các vấn đề
  như deadlock, race condition, hay rò rỉ tài nguyên.

- Structured Concurrency (JEP 428):
  Cung cấp một mô hình lập trình đơn giản hơn.
  Nó thể hiện nguyên tắc rằng nếu một tác vụ chia thành các nhiệm vụ con
  đồng thời thì tất cả chúng đều quay trở lại cùng một nơi
  (khối mã của tác vụ), giúp viết mã đa luồng an toàn và hiệu quả hơn.

Lưu ý khi chạy:
Đây là tính năng ươm tạo (Incubator), cần bật cờ preview:
Biên dịch: javac --enable-preview --add-modules jdk.incubator.concurrent JEP428_QuangTuan.java
Chạy:      java --enable-preview --add-modules jdk.incubator.concurrent JEP428_QuangTuan
*/
