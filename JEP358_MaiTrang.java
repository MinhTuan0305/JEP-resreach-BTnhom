/*
==========================================================
JEP 358 - Helpful NullPointerExceptions
Mục tiêu: Cung cấp thông báo lỗi chi tiết khi xảy ra NullPointerException
Phiên bản áp dụng: Java 14 trở lên
==========================================================
*/

class Person {
    String name;
    Address address;
}

class Address {
    String city;
}

public class JEP358_Example {
    public static void main(String[] args) {
        beforeJEP358();
        afterJEP358();
    }

    // ===================== Trước JEP 358 =====================
    static void beforeJEP358() {
        System.out.println("=== Trước JEP 358 ===");
        try {
            Person p = new Person();
            // p.address chưa được khởi tạo -> null
            System.out.println(p.address.city.toUpperCase());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    // ===================== Sau JEP 358 =====================
    static void afterJEP358() {
        System.out.println("\n=== Sau JEP 358 ===");
        try {
            Person p = new Person();
            // p.address vẫn là null
            System.out.println(p.address.city.toUpperCase());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}

/*

So sánh kết quả:

Trước JEP 358:
Ngoại lệ trong luồng "main" java.lang.NullPointerException
    tại JEP358_Example.beforeJEP358(JEP358_Example.java:22)


Sau JEP 358:
Ngoại lệ trong luồng "main" java.lang.NullPointerException:
Không thể đọc trường "city" vì "p.address" có giá trị null
    tại JEP358_Example.afterJEP358(JEP358_Example.java:33)
*/
