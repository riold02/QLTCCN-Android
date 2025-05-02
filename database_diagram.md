# Sơ đồ Cơ sở dữ liệu Quản lý Tài chính Cá nhân

Dự án sử dụng Firebase Firestore làm cơ sở dữ liệu NoSQL chính và Firebase Realtime Database làm cơ sở dữ liệu dự phòng/đồng bộ. Dưới đây là sơ đồ cấu trúc dữ liệu:

```mermaid
erDiagram
    USER {
        string id PK
        string uid
        string name
        string email
        string phone
        string profileImageUrl
        date dateOfBirth
        double balance
        date createdAt
        date updatedAt
    }

    CATEGORY {
        string id PK
        string name
        string type
        string iconName
        boolean isDefault
        string userId FK
        string parentId
        long createdAt
        long updatedAt
    }

    TRANSACTION {
        string id PK
        string userId FK
        string category FK
        double amount
        string description
        string note
        string type
        long date
        string goalId FK
        long createdAt
        long updatedAt
    }
    
    SAVINGS_GOAL {
        string id PK
        string userId FK
        string title
        string description
        double targetAmount
        double currentAmount
        string iconName
        long startDate
        long endDate
        string categoryType
        long createdAt
        long updatedAt
    }
    
    SAVINGS_TRANSACTION {
        string id PK
        string userId FK
        string goalId FK
        double amount
        string description
        string note
        string transactionType
        long date
        long createdAt
        long updatedAt
    }

    USER ||--o{ TRANSACTION : "creates"
    USER ||--o{ CATEGORY : "owns"
    CATEGORY ||--o{ TRANSACTION : "categorizes"
    USER ||--o{ SAVINGS_GOAL : "sets"
    SAVINGS_GOAL ||--o{ SAVINGS_TRANSACTION : "records"
    USER ||--o{ SAVINGS_TRANSACTION : "performs"
```

## Mô tả các Collection

### 1. Users Collection

- **id**: Định danh duy nhất của người dùng (từ Firebase Authentication)
- **uid**: Định danh người dùng (đồng nhất với id)
- **name**: Tên người dùng
- **email**: Email đăng nhập
- **phone**: Số điện thoại
- **profileImageUrl**: URL ảnh hồ sơ
- **dateOfBirth**: Ngày sinh
- **balance**: Số dư tài khoản
- **createdAt**: Thời điểm tạo tài khoản
- **updatedAt**: Thời điểm cập nhật thông tin

### 2. Categories Collection

- **id**: Định danh duy nhất của danh mục
- **name**: Tên danh mục
- **type**: Loại danh mục ("income" - thu nhập hoặc "expense" - chi tiêu)
- **iconName**: Tên biểu tượng của danh mục
- **isDefault**: Cờ đánh dấu danh mục mặc định
- **userId**: ID người dùng sở hữu danh mục (null/system cho danh mục mặc định)
- **parentId**: ID của danh mục cha (null nếu là danh mục gốc)
- **createdAt**: Thời điểm tạo danh mục
- **updatedAt**: Thời điểm cập nhật danh mục

### 3. Transactions Collection

- **id**: Định danh duy nhất của giao dịch
- **userId**: ID người dùng thực hiện giao dịch (tham chiếu đến Users)
- **category**: ID của danh mục giao dịch (tham chiếu đến Categories)
- **amount**: Số tiền giao dịch
- **description**: Mô tả chi tiết giao dịch
- **note**: Ghi chú thêm cho giao dịch
- **type**: Loại giao dịch ("income" - thu nhập hoặc "expense" - chi tiêu)
- **date**: Thời gian thực hiện giao dịch (timestamp)
- **goalId**: ID của mục tiêu tiết kiệm (nếu giao dịch liên quan đến mục tiêu)
- **createdAt**: Thời điểm tạo giao dịch (timestamp)
- **updatedAt**: Thời điểm cập nhật giao dịch (timestamp)

### 4. SavingsGoals Collection

- **id**: Định danh duy nhất của mục tiêu tiết kiệm
- **userId**: ID người dùng sở hữu mục tiêu (tham chiếu đến Users)
- **title**: Tiêu đề mục tiêu
- **description**: Mô tả chi tiết mục tiêu
- **targetAmount**: Số tiền mục tiêu cần đạt được
- **currentAmount**: Số tiền hiện tại đã tiết kiệm
- **iconName**: Tên biểu tượng của mục tiêu
- **startDate**: Ngày bắt đầu mục tiêu (timestamp)
- **endDate**: Ngày kết thúc mục tiêu (timestamp)
- **categoryType**: Loại danh mục mục tiêu (travel, house, car, wedding, v.v.)
- **createdAt**: Thời điểm tạo mục tiêu (timestamp)
- **updatedAt**: Thời điểm cập nhật mục tiêu (timestamp)

### 5. SavingsTransactions Collection

- **id**: Định danh duy nhất của giao dịch tiết kiệm
- **userId**: ID người dùng thực hiện giao dịch (tham chiếu đến Users)
- **goalId**: ID của mục tiêu tiết kiệm (tham chiếu đến SavingsGoals)
- **amount**: Số tiền giao dịch
- **description**: Mô tả chi tiết giao dịch
- **note**: Ghi chú thêm cho giao dịch
- **transactionType**: Loại giao dịch ("deposit" - gửi tiền hoặc "withdraw" - rút tiền)
- **date**: Thời gian thực hiện giao dịch (timestamp)
- **createdAt**: Thời điểm tạo giao dịch (timestamp)
- **updatedAt**: Thời điểm cập nhật giao dịch (timestamp)

## Các mối quan hệ

1. Một User có thể có nhiều Transaction (1-n)
2. Một User có thể sở hữu nhiều Category (1-n)
3. Một Category có thể được sử dụng trong nhiều Transaction (1-n)
4. Một User có thể có nhiều SavingsGoal (1-n)
5. Một SavingsGoal có thể có nhiều SavingsTransaction (1-n)
6. Một User thực hiện nhiều SavingsTransaction (1-n)

## Lưu ý về Firebase Firestore

Firestore là cơ sở dữ liệu NoSQL, nên không có khái niệm về khóa ngoại (foreign key) như trong cơ sở dữ liệu quan hệ. Thay vào đó, các tham chiếu được thực hiện bằng cách lưu trữ ID của document trong một collection khác.

Việc quản lý tính toàn vẹn dữ liệu phải được thực hiện ở tầng ứng dụng thông qua code Java trong các lớp utility như `DataUtils`, `UserUtils`, `CategoryUtils`, `TransactionUtils`, `SavingsGoalUtils`, và `SavingsTransactionUtils`.

## Firebase Realtime Database

Dự án cũng sử dụng Firebase Realtime Database làm cơ sở dữ liệu dự phòng/đồng bộ, chủ yếu cho các thông tin người dùng. Cấu trúc dữ liệu tương tự như Firestore.

### Cấu trúc Realtime Database:

```
/users
  /userId
    id: "userId"
    uid: "userId"
    name: "Tên người dùng"
    email: "email@example.com"
    phone: "0123456789"
    balance: 0.0
    createdAt: timestamp
    updatedAt: timestamp
    dateOfBirth: timestamp (tùy chọn)
    metadata: {
      lastLogin: timestamp
      device: "Android"
      appVersion: "1.0"
    }

/savings_goals
  /goalId
    id: "goalId"
    userId: "userId"
    title: "Tên mục tiêu"
    currentAmount: 0.0
    targetAmount: 5000000.0
    startDate: timestamp
    endDate: timestamp
    categoryType: "travel"
```

## Rules và Quyền Truy Cập

### Firestore Rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /transactions/{transactionId} {
      allow read, write: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    match /categories/{categoryId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && (
        resource == null || 
        resource.data.userId == null || 
        resource.data.userId == "system" || 
        resource.data.userId == request.auth.uid
      );
    }
    match /savings_goals/{goalId} {
      allow read, write: if request.auth != null && resource.data.userId == request.auth.uid;
    }
    match /savings_transactions/{transactionId} {
      allow read, write: if request.auth != null && resource.data.userId == request.auth.uid;
    }
  }
}
```

### Realtime Database Rules

```
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null",
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null && auth.uid == $uid"
      }
    },
    "savings_goals": {
      "$goalId": {
        ".read": "auth != null && data.child('userId').val() == auth.uid",
        ".write": "auth != null && data.child('userId').val() == auth.uid"
      }
    }
  }
}
```

## Lưu Trữ Offline và Đồng Bộ Hóa

Ứng dụng hỗ trợ lưu trữ offline (cache) cho cả Firestore và Realtime Database. Dữ liệu sẽ được đồng bộ hóa khi có kết nối mạng. Để kích hoạt tính năng này:

```java
// Trong Application class (onCreate)
FirebaseDatabase.getInstance().setPersistenceEnabled(true);
FirebaseFirestore.getInstance().setFirestoreSettings(
    new FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .build());
``` 