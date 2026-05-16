# Test Report - Integration Tests

Ngày kiểm thử: 17/05/2026

## Phạm vi kiểm thử

Đã bổ sung và thực hiện kiểm thử tích hợp cho các luồng nghiệp vụ quan trọng:

1. Đăng ký tài khoản rồi đăng nhập thành công.
2. Từ chối đăng ký khi trùng username hoặc email.
3. Tìm kiếm/lọc sản phẩm tại trang shop và forward đúng view.
4. Checkout COD tạo đơn hàng trạng thái `PENDING` với đúng tổng tiền và danh sách sản phẩm.
5. Checkout thanh toán thẻ hợp lệ tạo đơn hàng trạng thái `PAID` và sinh mã giao dịch.
6. Checkout chỉ các sản phẩm được chọn trong giỏ hàng.
7. Khách hàng hủy đơn `PENDING`, hệ thống chuyển trạng thái sang `CANCELLED`.
8. Admin đánh dấu đơn hàng hoàn tất, hệ thống chuyển trạng thái sang `COMPLETED`.
9. Người mua có đơn hoàn tất được tạo đánh giá sản phẩm một lần.

## File kiểm thử

- `src/test/java/com/daizuongkk/web/service/AuthFlowIT.java`
- `src/test/java/com/daizuongkk/web/controller/web/ShopSearchIT.java`
- `src/test/java/com/daizuongkk/web/service/BusinessCriticalFlowsIT.java`

## Kết quả Integration Test

Lệnh chạy:

```powershell
.\mvnw.cmd -q failsafe:integration-test failsafe:verify
```

Kết quả:

- Tổng số integration tests: 9
- Passed: 9
- Failed: 0
- Errors: 0
- Skipped: 0

Report sinh ra tại:

- `target/failsafe-reports/failsafe-summary.xml`
- `target/failsafe-reports/TEST-com.daizuongkk.web.service.BusinessCriticalFlowsIT.xml`

## Lỗi phát hiện

Khi chạy bộ unit test hiện hữu bằng:

```powershell
.\mvnw.cmd -q test
```

Phát hiện 3 lỗi trong các test cũ:

1. `LoginControllerTest.doPostShouldSetSessionAndRedirectWhenLoginSuccess`
   - Lỗi: `NullPointerException` vì `LoginController.orderService` là `null`.
   - Nguyên nhân: test gọi trực tiếp `doPost()` nhưng không gọi `init()` hoặc không inject/mock `OrderService`.
   - Hướng khắc phục: trong test, gọi `controller.init()` hoặc inject mock `OrderService` bằng reflection trước khi gọi `doPost()`.

2. `LoginControllerTest.doPostShouldForwardWithErrorWhenMissingCredentials`
   - Lỗi: `NullPointerException` vì `request.getSession()` trả về `null` khi `FlashUtils.put()` ghi flash message.
   - Nguyên nhân: mock request chưa cấu hình session.
   - Hướng khắc phục: mock `HttpSession` và cấu hình `when(request.getSession()).thenReturn(session)`.

3. `RegisterControllerTest.doPostShouldForwardWhenPasswordConfirmMismatch`
   - Lỗi: `NullPointerException` vì `request.getSession()` trả về `null` khi `FlashUtils.put()` ghi flash message.
   - Nguyên nhân: mock request chưa cấu hình session.
   - Hướng khắc phục: mock `HttpSession` và cấu hình `when(request.getSession()).thenReturn(session)`.

## Ghi chú

- Theo yêu cầu, chưa sửa lỗi trong production source code.
- Các lỗi unit test trên thuộc cấu hình test/mock hiện hữu, không xuất hiện trong bộ integration test mới.
