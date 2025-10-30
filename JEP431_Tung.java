import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

/**
 * Ví dụ minh họa JEP 431: Sequenced Collections (Java 21+)
 *
 * Bối cảnh: Mô phỏng 1 ca làm việc của quán ăn bao gồm:
 * - Menu hôm nay: món đặc biệt cần được ưu tiên hiển thị đầu tiên -> List (SequencedCollection)
 * - Khách hàng: lưu giữ vài vị khách vừa ghé gần nhất, không trùng tên -> LinkedHashSet (SequencedSet)
 * - Hàng đợi đơn hàng: đơn gấp lên đầu, đơn thường xuống cuối, bếp xử lý hai đầu -> LinkedHashMap (SequencedMap)
 *
 * Ta sẽ xem cùng một luồng nghiệp vụ được viết:
 *  1) Trước JEP 431 (cách cũ, phải xoay thủ công)
 *  2) Sau JEP 431 (cách mới, API rõ ràng và tự nhiên)
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== 1. Trước JEP 431 (cách cũ) ===");
        truocJEP431();

        System.out.println("\n=== 2. Sau JEP 431 (cách mới) ===");
        sauJEP431();

        System.out.println("\n=== 3. Tổng kết ===");
        tongKet();
    }

    // ------------------- TRƯỚC JEP 431 -------------------
    public static void truocJEP431() {

        // --- Menu hôm nay (List) ---
        List<String> menu = new ArrayList<>();
        menu.add(0, "Phở bò đặc biệt");
        menu.add("Cơm gà");
        menu.add("Bún chả");

        System.out.println("[Cũ] Menu: " + menu);
        System.out.println("[Cũ] Món đầu tiên: " + menu.get(0));
        System.out.println("[Cũ] Món cuối cùng: " + menu.get(menu.size() - 1));

        List<String> dao = new ArrayList<>();
        for (int i = menu.size() - 1; i >= 0; i--) dao.add(menu.get(i));
        System.out.println("[Cũ] Menu (mới nhất trước): " + dao);

        // --- Khách hàng thân thiết (LinkedHashSet) ---
        LinkedHashSet<String> khachHang = new LinkedHashSet<>();
        capNhatKhachHang_Cu(khachHang, "Chị Lan");
        capNhatKhachHang_Cu(khachHang, "Anh Nam");
        capNhatKhachHang_Cu(khachHang, "Chị Hương");
        capNhatKhachHang_Cu(khachHang, "Anh Nam");

        while (khachHang.size() > 3) {
            String cuNhat = khachHang.iterator().next();
            khachHang.remove(cuNhat);
        }

        System.out.println("[Cũ] Khách hàng (mới -> cũ, tự duy trì): " + khachHang);

        List<String> temp = new ArrayList<>(khachHang);
        List<String> daoKH = new ArrayList<>();
        for (int i = temp.size() - 1; i >= 0; i--) daoKH.add(temp.get(i));
        System.out.println("[Cũ] Khách hàng (cũ -> mới): " + daoKH);

        // --- Hàng đợi đơn hàng (Map) ---
        LinkedHashMap<String, String> donHang = new LinkedHashMap<>();
        donHang.put("DH01", "Trà sữa trân châu");
        donHang.put("DH02", "Cơm gà xối mỡ");

        LinkedHashMap<String, String> tam = new LinkedHashMap<>();
        tam.put("DH-UU-TIEN", "Phở bò giao gấp");
        tam.putAll(donHang);
        donHang = tam;

        System.out.println("[Cũ] Hàng đợi: " + donHang);

        // Lấy đơn đầu tiên
        String firstKey = donHang.keySet().iterator().next();
        String firstVal = donHang.remove(firstKey);
        System.out.println("[Cũ] Xử lý ngay: " + firstKey + "=" + firstVal);

        // Lấy đơn cuối cùng
        String lastKey = null;
        for (String k : donHang.keySet()) lastKey = k;
        String lastVal = donHang.remove(lastKey);
        System.out.println("[Cũ] Xử lý sau: " + lastKey + "=" + lastVal);

        System.out.println("[Cũ] Còn lại trong bếp: " + donHang);
    }

    private static void capNhatKhachHang_Cu(LinkedHashSet<String> ds, String ten) {
        ds.remove(ten);
        ds.add(ten);
    }

    // ------------------- SAU JEP 431 -------------------
    /**
     * Điểm khác biệt chính:
     * - List, LinkedHashSet, LinkedHashMap giờ đều hiểu khái niệm "đầu" và "cuối".
     *   Có addFirst(), addLast(), getFirst(), getLast().
     *
     * - Có thể thao tác hàng đợi hai đầu với Map:
     *   putFirst(), putLast(), pollFirstEntry(), pollLastEntry().
     *
     * - reversed() trả về một view đảo chiều sống:
     *   có thể duyệt "mới nhất trước" và thậm chí thêm phần tử từ góc nhìn đó.
     */
    public static void sauJEP431() {

        // --- Menu hôm nay (List ~ SequencedCollection) ---
        List<String> menu = new ArrayList<>();
        menu.addFirst("Phở bò đặc biệt");
        menu.addLast("Cơm gà");
        menu.addLast("Bún chả");

        System.out.println("[Mới] Menu: " + menu);
        System.out.println("[Mới] Món đầu tiên: " + menu.getFirst());
        System.out.println("[Mới] Món cuối cùng: " + menu.getLast());

        List<String> dao = menu.reversed();
        System.out.println("[Mới] Menu (mới nhất trước): " + dao);

        dao.addFirst("Bánh cuốn nóng");
        System.out.println("[Mới] Sau khi thêm qua view đảo: " + menu);

        // --- Khách hàng gần nhất (Set ~ SequencedSet) ---
        LinkedHashSet<String> khachHang = new LinkedHashSet<>();
        capNhatKhachHang_Moi(khachHang, "Chị Lan");
        capNhatKhachHang_Moi(khachHang, "Anh Nam");
        capNhatKhachHang_Moi(khachHang, "Chị Hương");
        capNhatKhachHang_Moi(khachHang, "Anh Nam");

        while (khachHang.size() > 3) khachHang.removeLast();
        System.out.println("[Mới] Khách hàng (mới -> cũ): " + khachHang);
        System.out.println("[Mới] Khách hàng (cũ -> mới): " + khachHang.reversed());

        // --- Hàng đợi đơn hàng (Map ~ SequencedMap) ---
        LinkedHashMap<String, String> donHang = new LinkedHashMap<>();
        donHang.putLast("DH01", "Trà sữa trân châu");
        donHang.putLast("DH02", "Cơm gà xối mỡ");
        donHang.putFirst("DH-UU-TIEN", "Phở bò giao gấp");

        System.out.println("[Mới] Hàng đợi: " + donHang);

        Map.Entry<String, String> donGoiNgay = donHang.pollFirstEntry();
        Map.Entry<String, String> donDeSau  = donHang.pollLastEntry();

        System.out.println("[Mới] Xử lý ngay: " + donGoiNgay);
        System.out.println("[Mới] Xử lý sau: " + donDeSau);
        System.out.println("[Mới] Còn lại trong bếp: " + donHang);

        SequencedMap<String, String> daoDon = donHang.reversed();
        daoDon.putFirst("DH03", "Bún chả mang về");
        System.out.println("[Mới] Thêm đơn qua view đảo: " + donHang);
    }

    private static void capNhatKhachHang_Moi(LinkedHashSet<String> ds, String ten) {
        ds.remove(ten);
        ds.addFirst(ten);
    }

    // ------------------- TỔNG KẾT -------------------
    public static void tongKet() {
        System.out.println("Trước JEP 431:");
        System.out.println(" - Muốn thêm đầu/cuối -> tự chèn index 0, rebuild map, hoặc xoá-rồi-add lại.");
        System.out.println(" - Muốn lấy phần tử đầu/cuối -> phải dùng get(0), get(size-1), hoặc iterator.");
        System.out.println(" - Muốn đảo chiều -> phải clone và tự đảo.");
        System.out.println();

        System.out.println("Sau JEP 431:");
        System.out.println(" - addFirst(), addLast(), getFirst(), getLast() áp dụng cho List/Set/Map có thứ tự.");
        System.out.println(" - removeLast(), pollFirstEntry(), pollLastEntry() xử lý hai đầu dễ như hàng đợi thật.");
        System.out.println(" - reversed() tạo live view đảo chiều, có thể chỉnh từ góc nhìn ngược.");
        System.out.println();

        System.out.println("Ứng dụng thực tế:");
        System.out.println(" - Menu: đưa món đặc biệt lên đầu, show món mới ra trước.");
        System.out.println(" - Khách hàng: giữ danh sách khách vừa ghé, bỏ khách lâu ngày.");
        System.out.println(" - Đơn hàng: đơn gấp lên đầu, đơn chậm để cuối, quản lý gọn hơn hẳn.");
    }
}
