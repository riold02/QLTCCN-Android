Tổng quan về dự án QLTCCN
Đây là một ứng dụng Android được phát triển để quản lý tài chính cá nhân với các chức năng chính sau:
Quản lý giao dịch thu chi: Theo dõi các khoản thu nhập và chi tiêu
Phân loại giao dịch theo danh mục: Cho phép phân loại và tổ chức các giao dịch
Phân tích tài chính: Cung cấp thống kê và phân tích về tình hình tài chính
Quản lý mục tiêu tiết kiệm: Theo dõi các mục tiêu tiết kiệm và tiến độ thực hiện
Cấu trúc dự án
Backend: Sử dụng Firebase làm nền tảng backend
Firestore Database: Lưu trữ dữ liệu người dùng, giao dịch, danh mục...
Firebase Authentication: Quản lý đăng nhập, đăng ký
Firebase Storage: Lưu trữ hình ảnh hồ sơ, biên lai...
Cấu trúc mã nguồn:
models: Chứa các lớp mô hình dữ liệu (User, Transaction, Category, SavingsGoal...)
activities: Chứa các màn hình chính của ứng dụng
adapters: Chứa các adapter cho RecyclerView để hiển thị danh sách
utils: Chứa các tiện ích xử lý dữ liệu, tương tác với Firebase, định dạng...
Mô hình dữ liệu:
User: Thông tin người dùng (id, tên, email, số dư...)
Transaction: Giao dịch thu chi (số tiền, danh mục, mô tả, ngày...)
Category: Danh mục giao dịch (tên, loại, biểu tượng, màu sắc...)
SavingsGoal: Mục tiêu tiết kiệm
SavingsTransaction: Giao dịch tiết kiệm
Các chức năng chính
Dựa vào các file trong thư mục activities, ứng dụng có các màn hình và chức năng sau:
Đăng nhập/Đăng ký: LoginActivity, SignUpActivity, ForgotPasswordActivity
Trang chủ: HomeActivity - hiển thị tổng quan tài chính
Quản lý giao dịch: TranActivity - xem, thêm, sửa, xóa giao dịch
Phân tích tài chính: AnalysisActivity - biểu đồ, thống kê
Quản lý danh mục: CategoryActivity, CategoryAddActivity, CategoryDetailActivity
Quản lý tiết kiệm: SavingsActivity, SavingsGoalDetailActivity, SaveToSavingsActivity
Quản lý hồ sơ: ProfileActivity, EditProfileActivity
Cài đặt: SettingsActivity, SecurityActivity, NotificationSettingsActivity, PasswordSettingsActivity
Tiện ích
Ứng dụng có nhiều lớp tiện ích hỗ trợ xử lý các tác vụ:
AuthUtils: Xử lý xác thực người dùng
TransactionUtils: Quản lý giao dịch
UserUtils: Quản lý người dùng
CategoryUtils: Quản lý danh mục
StatisticsUtils: Phân tích thống kê
DateUtils, CurrencyUtils, FileUtils: Hỗ trợ xử lý định dạng, file...
Đây là một ứng dụng Android khá đầy đủ chức năng cho việc quản lý tài chính cá nhân, sử dụng Firebase làm backend và tuân theo mô hình kiến trúc cơ bản của Android.