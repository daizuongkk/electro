# Hướng dẫn cài đặt và chạy dự án `web`

Tài liệu này hướng dẫn nhanh các bước để:

- Clone project về máy
- Khởi tạo cơ sở dữ liệu từ file `data/electro_db.sql`
- Cấu hình và chạy ứng dụng bằng Tomcat Server trong IntelliJ IDEA

## 1. Yêu cầu môi trường

- Git
- JDK 21
- MySQL 8.x
- IntelliJ IDEA
- Apache Tomcat 10.1+

Kiểm tra nhanh trong PowerShell:

```powershell
java -version
.\mvnw.cmd -v
```

## 2. Clone project

```powershell
git clone https://github.com/daizuongkk/javawebhaui.git
cd web
```

## 3. Khởi tạo database từ `electro_db.sql`

### 3.1 Tạo database

Mở MySQL:

```powershell
mysql -u root -p
```

Trong MySQL shell:

```sql
CREATE DATABASE cellphoneS CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

> Tên database `cellphoneS` đang khớp với cấu hình hiện tại trong `src/main/java/com/daizuongkk/web/util/JDBCUtils.java`.

### 3.2 Import file SQL

Tại thư mục gốc project (`web`), chạy:

```powershell
mysql -u root -p cellphoneS < .\data\electro_db.sql
```

## 4. Cấu hình kết nối database

Project đang cấu hình kết nối tại `src/main/java/com/daizuongkk/web/util/JDBCUtils.java`.

Bạn cần kiểm tra và cập nhật các giá trị sau cho phù hợp với máy local:

- `DB_URL`
- `USER`
- `PASSWORD`

Ví dụ:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/cellphoneS?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

## 5. Mở project trong IntelliJ IDEA

1. Mở IntelliJ IDEA.
2. Chọn **Open** và trỏ đến thư mục `web`.
3. Đợi IntelliJ import Maven project.
4. Kiểm tra JDK đang dùng là Java 21 tại **File -> Project Structure -> Project SDK**.

## 6. Cấu hình Tomcat Server trong IntelliJ

### 6.1 Khai báo Tomcat local

1. Vào **Run -> Edit Configurations...**
2. Nhấn **+** -> chọn **Tomcat Server -> Local**
3. Ở tab **Server**, bấm **Configure...**
4. Chọn thư mục Tomcat Home (ví dụ: `C:\tools\apache-tomcat-10.1.x`)
5. Chọn JRE/JDK là Java 21

### 6.2 Tạo artifact để deploy

1. Vào **File -> Project Structure... -> Artifacts**
2. Nhấn **+** -> **Web Application: Exploded**
3. Chọn module `web`
4. Nhấn **Apply**

### 6.3 Gán artifact vào Tomcat

1. Quay lại **Run -> Edit Configurations...**
2. Chọn cấu hình Tomcat vừa tạo
3. Mở tab **Deployment**
4. Nhấn **+** và chọn artifact (ví dụ: `web:war exploded`)
5. Đặt **Application context** là `/web` (hoặc `/` nếu muốn chạy ở root)

### 6.4 Chạy server

- Nhấn **Run** với cấu hình Tomcat
- Mở trình duyệt:
  - `http://localhost:8080/web` (nếu context là `/web`)
  - `http://localhost:8080/` (nếu context là `/`)

## 7. Build WAR bằng Maven (tùy chọn)

```powershell
.\mvnw.cmd clean package
```

Sau khi build thành công, file WAR nằm trong thư mục `target`.

## 8. Lỗi thường gặp

- **Port 8080 bị trùng**
  - Đổi port trong cấu hình Tomcat (ví dụ sang `8081`).

- **MySQL Access denied**
  - Kiểm tra lại `USER` và `PASSWORD` trong `JDBCUtils`.

- **404 khi truy cập ứng dụng**
  - Kiểm tra `Application context` và artifact đã được deploy đúng chưa.

- **Không thấy artifact trong Deployment**
  - Tạo lại artifact tại **Project Structure -> Artifacts**.
