# Lời thuyết minh bảo vệ sản phẩm Electro

## 1. Mở đầu

Kính thưa thầy/cô trong hội đồng, nhóm em xin trình bày đề tài **“Xây dựng cửa hàng trực tuyến Electro sử dụng JSP/Servlet và MySQL”** trong học phần Lập trình Web bằng Java.

Electro là website thương mại điện tử cho thiết bị công nghệ. Sản phẩm tập trung vào hai nhóm chức năng chính: hỗ trợ khách hàng mua sắm trực tuyến và hỗ trợ quản trị viên vận hành cửa hàng.

## 2. Bối cảnh và mục tiêu

Trong lĩnh vực bán lẻ thiết bị công nghệ, số lượng sản phẩm lớn, thông tin giá và tồn kho thay đổi liên tục, đơn hàng có nhiều trạng thái xử lý. Nếu quản lý thủ công, cửa hàng dễ gặp sai sót về tồn kho, khó tổng hợp doanh thu và khó theo dõi trạng thái đơn hàng.

Mục tiêu của đề tài là xây dựng một nền tảng web hoàn chỉnh, giúp khách hàng tìm kiếm sản phẩm, xem chi tiết, quản lý giỏ hàng, đặt hàng và theo dõi đơn. Ở phía quản trị, hệ thống hỗ trợ quản lý sản phẩm, đơn hàng, người dùng, đánh giá và thống kê doanh thu.

## 3. Phạm vi và tác nhân hệ thống

Phạm vi đề tài tập trung vào website bán lẻ thiết bị công nghệ cho khách hàng cá nhân và quy mô quản lý vừa, nhỏ. Phía khách hàng gồm đăng ký, đăng nhập, tìm kiếm, xem chi tiết, giỏ hàng, thanh toán, đơn hàng và đánh giá. Phía quản trị gồm dashboard, quản lý sản phẩm, người dùng, đơn hàng và đánh giá.

Hệ thống có ba nhóm tác nhân chính: khách hàng, quản trị viên và hệ thống xử lý dữ liệu. Khách hàng cần trải nghiệm mua sắm thuận tiện; quản trị viên cần kiểm soát vận hành; hệ thống cần đảm bảo phân quyền, tính toàn vẹn dữ liệu và quy trình nghiệp vụ đúng.

## 4. Sản phẩm phía khách hàng

Luồng khách hàng bắt đầu từ đăng ký hoặc đăng nhập. Hệ thống kiểm tra thông tin đầu vào, tạo phiên làm việc và xác định quyền truy cập. Sau khi đăng nhập, khách hàng có thể sử dụng đầy đủ các chức năng như giỏ hàng, đơn hàng và hồ sơ cá nhân.

Trang chủ đóng vai trò là điểm vào của trải nghiệm mua sắm. Người dùng có thể tìm kiếm sản phẩm, truy cập danh mục và xem các sản phẩm mới. Trang danh sách sản phẩm cho phép lọc theo phân loại, khoảng giá và hãng, phù hợp với đặc thù cửa hàng có nhiều thiết bị công nghệ.

Ở trang chi tiết sản phẩm, hệ thống hiển thị hình ảnh, giá, giá gốc, tồn kho, mô tả, thông số kỹ thuật và đánh giá. Đây là màn hình hỗ trợ khách hàng ra quyết định mua hàng.

Giỏ hàng cho phép chọn từng sản phẩm, chỉnh số lượng và xóa sản phẩm. Điểm quan trọng là hệ thống chỉ thanh toán các sản phẩm được chọn, tránh trường hợp người dùng mua nhầm toàn bộ giỏ.

Tại bước thanh toán, thông tin giao hàng và danh sách sản phẩm được ghi nhận để tạo đơn hàng. Hệ thống hỗ trợ các phương thức thanh toán như chuyển khoản, thẻ và COD. Sau khi đặt hàng, khách hàng có thể theo dõi trạng thái đơn, hủy đơn khi còn chờ xử lý và đánh giá sản phẩm khi đơn đã hoàn tất.

## 5. Sản phẩm phía quản trị

Khu vực quản trị là phần vận hành của hệ thống. Dashboard cung cấp các chỉ số như doanh thu, số đơn hàng, số sản phẩm, số khách hàng, biểu đồ doanh thu, sản phẩm mới, sản phẩm sắp hết và đơn hàng gần nhất.

Trang quản lý sản phẩm cho phép tìm kiếm, lọc, thêm, sửa và xóa mềm sản phẩm. Khi thêm hoặc cập nhật, hệ thống kiểm tra các dữ liệu quan trọng như giá, tồn kho, trường bắt buộc và hình ảnh sản phẩm.

Trang quản lý đơn hàng cho phép quản trị viên lọc đơn theo trạng thái, ngày tạo, tổng tiền và cập nhật trạng thái theo quy trình nghiệp vụ. Các trạng thái chính gồm PENDING, PAID, SHIPPED, COMPLETED và CANCELLED.

Trang quản lý người dùng cho phép theo dõi tài khoản, vai trò, trạng thái, xác thực và xóa mềm. Form chỉnh sửa người dùng hỗ trợ cập nhật thông tin cá nhân, vai trò, trạng thái và mật khẩu mới khi cần.

## 6. Thiết kế kỹ thuật

Hệ thống được xây dựng theo mô hình MVC. JSP đảm nhiệm tầng giao diện, Servlet xử lý điều hướng và request, service đóng gói nghiệp vụ, repository truy vấn dữ liệu MySQL. AuthFilter bảo vệ các route cần đăng nhập và phân quyền khu vực admin.

Cơ sở dữ liệu gồm các bảng chính như `users`, `addresses`, `products`, `product_images`, `carts`, `cart_items`, `wishlists`, `orders`, `order_items`, `reviews` và `verification_otps`. Các bảng này liên kết bằng khóa ngoại để duy trì quan hệ giữa người dùng, sản phẩm, giỏ hàng, đơn hàng và đánh giá.

Về yêu cầu phi chức năng, hệ thống hướng tới giao diện tiếng Việt thân thiện, phản hồi ổn định, hỗ trợ nhiều người dùng, bảo vệ thông tin người dùng và có cấu trúc dễ bảo trì.

## 7. Kiểm thử và kết quả

Nhóm đã kiểm thử các luồng nghiệp vụ quan trọng: đăng ký, đăng nhập, chống đăng ký trùng username/email, tìm kiếm và lọc sản phẩm, checkout COD, checkout thẻ, checkout theo sản phẩm được chọn, hủy đơn PENDING, admin hoàn tất đơn hàng và người mua đánh giá sản phẩm sau khi đơn hoàn tất.

Kết quả integration test: 9/9 test passed, không có failed, errors hoặc skipped. Điều này cho thấy các luồng nghiệp vụ cốt lõi đã hoạt động đúng theo yêu cầu đặt ra.

## 8. Kết luận

Electro đã đáp ứng phần lõi của một hệ thống thương mại điện tử: mua hàng, thanh toán, theo dõi đơn, quản trị sản phẩm, quản trị đơn hàng, quản trị người dùng và kiểm soát đánh giá.

Hệ thống vẫn còn một số hướng phát triển như tích hợp cổng thanh toán thật, kết nối đơn vị vận chuyển, gửi email/SMS thông báo, mã giảm giá, báo cáo nâng cao và bảo mật chuyên sâu.

Nhóm em xin chân thành cảm ơn thầy/cô đã lắng nghe và mong nhận được góp ý để tiếp tục hoàn thiện sản phẩm.
