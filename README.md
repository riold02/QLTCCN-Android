# Ứng dụng Quản lý Tài chính Cá nhân (FinWise)

## Tổng quan
FinWise là một ứng dụng Android được phát triển để giúp người dùng quản lý tài chính cá nhân một cách hiệu quả và trực quan. Ứng dụng cung cấp nhiều chức năng hỗ trợ người dùng theo dõi và kiểm soát nguồn tài chính của mình.

## Chức năng chính
- **Quản lý giao dịch thu chi**: Theo dõi chi tiết các khoản thu nhập và chi tiêu
- **Phân loại giao dịch theo danh mục**: Tổ chức giao dịch thành nhiều danh mục khác nhau
- **Phân tích tài chính**: Biểu đồ, thống kê và phân tích tình hình tài chính theo nhiều khung thời gian
- **Quản lý mục tiêu tiết kiệm**: Thiết lập và theo dõi tiến độ các mục tiêu tiết kiệm

## Hướng dẫn sử dụng
Xem hướng dẫn sử dụng đầy đủ tại [HUONG_DAN_SU_DUNG.md](./HUONG_DAN_SU_DUNG.md) để biết chi tiết về:
- Cài đặt và đăng ký tài khoản
- Quản lý giao dịch thu chi
- Phân tích tài chính
- Quản lý mục tiêu tiết kiệm
- Và nhiều thông tin hữu ích khác

## Cấu trúc dự án

### Backend
- **Firebase Platform**:
  - **Firestore Database**: Lưu trữ dữ liệu người dùng, giao dịch, danh mục...
  - **Firebase Authentication**: Quản lý đăng nhập, đăng ký, xác thực người dùng
  - **Firebase Storage**: Lưu trữ hình ảnh hồ sơ, biên lai giao dịch...

### Cấu trúc mã nguồn
- **models**: Chứa các lớp mô hình dữ liệu (User, Transaction, Category, SavingsGoal...)
- **activities**: Chứa các màn hình chính của ứng dụng
- **adapters**: Chứa các adapter cho RecyclerView để hiển thị danh sách
- **utils**: Chứa các tiện ích xử lý dữ liệu, tương tác với Firebase, định dạng tiền tệ và ngày tháng...

### Mô hình dữ liệu
- **User**: Thông tin người dùng (id, tên, email, số dư...)
- **Transaction**: Giao dịch thu chi (số tiền, danh mục, mô tả, ngày...)
- **Category**: Danh mục giao dịch (tên, loại, biểu tượng, màu sắc...)
- **SavingsGoal**: Mục tiêu tiết kiệm (tên, số tiền mục tiêu, ngày bắt đầu, ngày kết thúc...)
- **SavingsTransaction**: Giao dịch tiết kiệm (số tiền, ngày, ghi chú...)

## Các màn hình và chức năng

### Đăng nhập & Đăng ký
- **SplashActivity**: Màn hình chào khi khởi động ứng dụng, kiểm tra trạng thái đăng nhập
- **MainActivity**: Màn hình chính để điều hướng đến đăng nhập hoặc đăng ký
- **LoginActivity**: Đăng nhập với email và mật khẩu
- **SignUpActivity**: Đăng ký tài khoản mới
- **ForgotPasswordActivity**: Khôi phục mật khẩu qua email

### Quản lý tài chính
- **HomeActivity**: Hiển thị tổng quan tài chính, số dư hiện tại, các giao dịch gần đây và tùy chọn lọc theo thời gian (ngày, tuần, tháng)
- **TranActivity**: Quản lý tất cả giao dịch, xem, thêm, sửa, xóa giao dịch, lọc theo loại giao dịch
- **SearchActivity**: Tìm kiếm giao dịch theo từ khóa, danh mục hoặc ngày

### Phân tích tài chính
- **AnalysisActivity**: Cung cấp các biểu đồ và thống kê tài chính
  - Biểu đồ thu chi theo ngày, tuần, tháng
  - Tính toán tổng thu nhập, chi tiêu và số dư
  - So sánh tỷ lệ chi tiêu theo các danh mục
  - Phân tích xu hướng tài chính theo thời gian

### Quản lý danh mục
- **CategoryActivity**: Hiển thị tất cả danh mục giao dịch
- **CategoryAddActivity**: Thêm danh mục mới
- **CategoryDetailActivity**: Xem chi tiết và chỉnh sửa danh mục

### Quản lý tiết kiệm
- **SavingsActivity**: Hiển thị danh sách các mục tiêu tiết kiệm
- **SavingsAddActivity**: Tạo mục tiêu tiết kiệm mới
- **SavingsGoalDetailActivity**: Quản lý chi tiết mục tiêu tiết kiệm
  - Xem tiến độ tiết kiệm
  - Thêm giao dịch tiết kiệm mới
  - Theo dõi lịch sử giao dịch tiết kiệm

### Quản lý tài khoản
- **ProfileActivity**: Xem và quản lý thông tin cá nhân
- **EditProfileActivity**: Chỉnh sửa thông tin cá nhân
- **SettingsActivity**: Cài đặt chung của ứng dụng
- **SecurityActivity**: Cài đặt bảo mật
- **NotificationSettingsActivity**: Cài đặt thông báo
- **PasswordSettingsActivity**: Thay đổi mật khẩu

### Thông báo
- **NotiActivity**: Quản lý thông báo và nhắc nhở

## Tính năng phân tích tài chính chi tiết
Ứng dụng cung cấp các công cụ phân tích tài chính mạnh mẽ:
- **Phân tích theo thời gian**: Xem biểu đồ thu chi theo ngày, tuần hoặc tháng
- **Phân loại chi tiêu**: Hiển thị tỷ lệ chi tiêu theo từng danh mục
- **Phương thức tính toán**: 
  - Tự động tính toán số dư từ tổng thu nhập trừ đi chi tiêu và tiết kiệm
  - Phân tích giao dịch theo khoảng thời gian với phương thức `isTransactionInPeriod()`
  - Tính toán tổng thu chi với `calculateIncomeAndExpense()`

## Tính năng quản lý tiết kiệm chi tiết
- **Đa dạng mục tiêu**: Tạo nhiều mục tiêu tiết kiệm khác nhau (du lịch, mua nhà, học tập...)
- **Theo dõi tiến độ**: Hiển thị trực quan tiến độ tiết kiệm bằng progress bar
- **Quản lý giao dịch**: Thêm, sửa, xóa các giao dịch tiết kiệm cho từng mục tiêu
- **Thống kê tiết kiệm**: Xem tỷ lệ hoàn thành và thời gian còn lại của mục tiêu

## Công nghệ và thư viện
- **Firebase**: Firestore, Authentication, Storage
- **MPAndroidChart**: Hiển thị biểu đồ thu chi và phân tích
- **Glide**: Tải và hiển thị hình ảnh
- **ColorPicker**: Tùy chỉnh màu sắc cho danh mục
