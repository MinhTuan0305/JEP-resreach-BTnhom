import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lớp này so sánh cách tạo Collections trước và sau khi có JEP 269 (Java 9).
 * JEP 269 giới thiệu các phương thức gốc tiện lợi (convenience factory methods)
 * như List.of(), Set.of(), Map.of() để tạo các collection bất biến (immutable).
 */
public class JEP269_MinhTuan {

    public static void main(String[] args) {
        System.out.println("--- 1. Cách làm cũ (Trước JEP 269 / Java 8) ---");
        demoPreJEP269();

        System.out.println("\n--- 2. Cách làm mới (Với JEP 269 / Java 9+) ---");
        demoPostJEP269();

        System.out.println("\n--- 3. Kiểm tra tính bất biến và các ràng buộc ---");
        demoImmutabilityAndRestrictions();
    }

    /**
     * Minh họa cách tạo Collections bất biến (immutable) trước Java 9.
     * Quá trình này dài dòng và phải qua nhiều bước.
     */
    public static void demoPreJEP269() {
        // --- Tạo một List bất biến ---
        // Bước 1: Khởi tạo một List có thể thay đổi (mutable)
        List<String> list = new ArrayList<>();
        // Bước 2: Thêm từng phần tử
        list.add("Java");
        list.add("C++");
        list.add("Python");
        // Bước 3: Bọc nó trong một wrapper bất biến
        List<String> unmodifiableList = Collections.unmodifiableList(list);
        System.out.println("List (cũ): " + unmodifiableList);

        // --- Tạo một Set bất biến ---
        Set<String> set = new HashSet<>();
        set.add("Red");
        set.add("Green");
        set.add("Blue");
        Set<String> unmodifiableSet = Collections.unmodifiableSet(set);
        System.out.println("Set (cũ): " + unmodifiableSet);

        // --- Tạo một Map bất biến ---
        Map<String, Integer> map = new HashMap<>();
        map.put("One", 1);
        map.put("Two", 2);
        Map<String, Integer> unmodifiableMap = Collections.unmodifiableMap(map);
        System.out.println("Map (cũ): " + unmodifiableMap);

        System.out.println("* Nhận xét: Code rất dài dòng, phải qua nhiều bước.");
    }

    /**
     * Minh họa cách tạo Collections bất biến (immutable) với JEP 269 (từ Java 9).
     * Quá trình này ngắn gọn và chỉ cần một dòng.
     */
    public static void demoPostJEP269() {
        // --- Tạo một List bất biến ---
        List<String> list = List.of("Java", "C++", "Python");
        System.out.println("List (mới): " + list);

        // --- Tạo một Set bất biến ---
        Set<String> set = Set.of("Red", "Green", "Blue");
        System.out.println("Set (mới): " + set);

        // --- Tạo một Map bất biến ---
        Map<String, Integer> map = Map.of("One", 1, "Two", 2);
        System.out.println("Map (mới): " + map);

        System.out.println("* Nhận xét: Code rất ngắn gọn, rõ ràng và trong 1 dòng.");
    }

    /**
     * Minh họa các đặc điểm của collection tạo từ JEP 269:
     * 1. Bất biến (Immutable)
     * 2. Không chấp nhận null
     * 3. Không chấp nhận key/phần tử trùng lặp (cho Set/Map)
     */
    public static void demoImmutabilityAndRestrictions() {
        List<String> list = List.of("a", "b", "c");

        // 1. Thử thay đổi (sẽ ném lỗi UnsupportedOperationException)
        try {
            list.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("Bắt lỗi: Không thể thêm phần tử. List là bất biến.");
        }

        // 2. Thử thêm phần tử null (sẽ ném lỗi NullPointerException)
        try {
            List.of("a", null, "c");
        } catch (NullPointerException e) {
            System.out.println("Bắt lỗi: Không thể tạo List chứa phần tử null.");
        }

        // 3. Thử tạo Set với phần tử trùng lặp (sẽ ném lỗi IllegalArgumentException)
        try {
            Set.of("A", "B", "A"); // "A" bị trùng lặp
        } catch (IllegalArgumentException e) {
            System.out.println("Bắt lỗi: Không thể tạo Set với phần tử trùng lặp.");
        }
        
        // 4. Thử tạo Map với key trùng lặp (sẽ ném lỗi IllegalArgumentException)
        try {
            Map.of("Key1", 1, "Key1", 2); // "Key1" bị trùng lặp
        } catch (IllegalArgumentException e) {
            System.out.println("Bắt lỗi: Không thể tạo Map với key trùng lặp.");
        }
    }
}

