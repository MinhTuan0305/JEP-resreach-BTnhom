import java.lang.ThreadLocal;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import jdk.incubator.concurrent.ScopedValue;

/**
 Lớp này so sánh cách chia sẻ dữ liệu giữa các luồng (thread)
 trước và sau khi có JEP 429 (Scoped Values – Java 20).
 
JEP 429 giới thiệu lớp ScopedValue để chia sẻ dữ liệu bất biến (immutable)
trong phạm vi (scope) cụ thể, thay thế cho ThreadLocal truyền thống.
 */
public class ScopedValueComparison {

    // ScopedValue được khai báo là static final (giống ThreadLocal)
    private static final ScopedValue<String> USER = ScopedValue.newInstance();

    // ThreadLocal để so sánh với cách cũ
    private static final ThreadLocal<String> OLD_USER = new ThreadLocal<>();

    public static void main(String[] args) {
        System.out.println("--- 1. Cách làm cũ (Trước JEP 429 / ThreadLocal) ---");
        demoPreJEP429();

        System.out.println("\n--- 2. Cách làm mới (Với JEP 429 / Java 20+) ---");
        demoPostJEP429();

        System.out.println("\n--- 3. Kiểm tra tính an toàn và phạm vi (Scope) ---");
        demoScopeBehavior();
    }

    /*
     Minh họa cách chia sẻ dữ liệu bằng ThreadLocal (cách cũ).
     Dữ liệu được gắn với thread và có thể bị thay đổi hoặc rò rỉ.
     */
    public static void demoPreJEP429() {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable task = () -> {
            OLD_USER.set(Thread.currentThread().getName());
            simulateWork("Trước JEP 429 (ThreadLocal): " + OLD_USER.get());
        };

        executor.submit(task);
        executor.submit(task);
        executor.shutdown();

        System.out.println("* Nhận xét: ThreadLocal mutable, cần cleanup (remove) để tránh rò rỉ.");
    }

    /*
     Minh họa cách chia sẻ dữ liệu bất biến bằng ScopedValue (JEP 429).
     Dữ liệu được bind (gắn) chỉ trong phạm vi where(...).run(...)
     và tự động giải phóng sau khi kết thúc scope.
     */
    public static void demoPostJEP429() {
        // Dữ liệu chỉ tồn tại trong scope này
        ScopedValue.where(USER, "Alice").run(() -> {
            simulateWork("Trong scope: USER = " + USER.get());

            // Có thể tạo scope lồng nhau (nested scope)
            ScopedValue.where(USER, "Bob").run(() -> {
                simulateWork("Scope lồng nhau: USER = " + USER.get());
            });

            simulateWork("Quay lại scope ngoài: USER = " + USER.get());
        });

        System.out.println("* Sau khi scope kết thúc: Không còn giá trị USER hợp lệ.");
    }

    /*
    Minh họa tính an toàn và giới hạn phạm vi của ScopedValue.
     */
    public static void demoScopeBehavior() {
        try {
            // Gọi USER.get() ngoài phạm vi sẽ ném lỗi
            System.out.println(USER.get());
        } catch (IllegalStateException e) {
            System.out.println("Bắt lỗi: Không thể truy cập ScopedValue ngoài phạm vi của nó.");
        }

        // ScopedValue luôn immutable – không thể thay đổi giá trị sau khi bind
        ScopedValue.where(USER, "ImmutableUser").run(() -> {
            System.out.println("USER trong scope: " + USER.get());
            // Không có phương thức set() như ThreadLocal.set()
        });
    }

    private static void simulateWork(String msg) {
        System.out.println(msg);
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}