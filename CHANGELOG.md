# Changelog - Cập nhật hệ thống

## Các tính năng đã hoàn thành

### 1. Hệ thống đăng nhập cho Cán bộ
- ✅ Tạo 2 tài khoản test:
  - **Doanh nghiệp**: `test@kma.edu.vn` / `123456`
  - **Cán bộ**: `canbo@bca.gov.vn` / `canbo123`
- ✅ Điều hướng tự động theo role (Doanh nghiệp → MainScreen, Cán bộ → OfficerMainScreen)
- ✅ Lưu role vào DataStore để quản lý phiên đăng nhập

### 2. Màn hình cho Cán bộ (OfficerMainScreen)
- ✅ 4 tab: Trang chủ, Hồ sơ, Giấy phép, Tài khoản
- ✅ **Tab Hồ sơ**: Hiển thị tất cả hồ sơ cần xử lý
- ✅ Click vào hồ sơ → điều hướng đến ProcessProfileScreen

### 3. Màn hình xử lý hồ sơ (ProcessProfileScreen)
- ✅ Stepper 4 bước: Registered → Received → Processing → Approved
- ✅ Form xử lý gồm:
  - Receive Date (read-only)
  - Return Date (Ngay Hen Tra) - có date picker
  - License Reference (Số giấy phép)
  - Update Status (dropdown: BiTraLai, DangXuLy, DaDuyet)
- ✅ Nút "Update Profile" để cập nhật hồ sơ
- ✅ Nút quay lại trang trước
- ✅ Logic: Nếu bị trả lại → các bước tiếp theo bị vô hiệu hóa

### 4. API đã tích hợp
- ✅ `GET /ho-so` - Lấy tất cả hồ sơ (cho cán bộ)
- ✅ `PUT /ho-so/{id}` - Cập nhật hồ sơ
- ✅ `POST /giay-phep` - Tạo giấy phép mới

### 5. Cập nhật màn hình Giấy phép
- ✅ Bỏ hiển thị Hash Blockchain
- ✅ Thiết kế lại card giấy phép theo dạng gradient đẹp
- ✅ Hiển thị thông tin:
  - Loại giấy phép
  - Số giấy phép
  - Trạng thái (Hiệu lực/Hết hạn/Thu hồi/Tạm dừng)
  - Ngày hiệu lực & ngày hết hạn
  - Hồ sơ liên quan
  - Tên doanh nghiệp

### 6. Xử lý xem file PDF
- ✅ Thay nút "Download" thành nút "Xem" (icon Visibility)
- ✅ Click vào sẽ mở file PDF bằng ứng dụng PDF viewer mặc định
- ✅ Xử lý lỗi khi không có app để mở PDF
- ✅ FileProvider đã được cấu hình đúng trong AndroidManifest

### 7. Quản lý nút xóa
- ✅ Giữ nguyên nút xóa ở phần Tài liệu (để xóa tài liệu đã upload)
- ✅ Có dialog xác nhận khi xóa tài liệu

## Cấu trúc file mới
```
app/src/main/java/com/example/kmaerm/
├── ui/
│   ├── screens/
│   │   ├── OfficerMainScreen.kt (MỚI)
│   │   ├── ProcessProfileScreen.kt (MỚI)
│   │   ├── GiayPhepScreen.kt (CẬP NHẬT)
│   │   └── LoginScreen.kt (CẬP NHẬT)
│   ├── viewmodel/
│   │   └── OfficerHoSoViewModel.kt (MỚI)
│   └── navigation/
│       └── NavGraph.kt (CẬP NHẬT)
├── data/
│   ├── model/
│   │   ├── AuthResponse.kt (CẬP NHẬT - thêm role)
│   │   ├── HoSo.kt (CẬP NHẬT - thêm các trường mới)
│   │   └── GiayPhep.kt (CẬP NHẬT - thêm CreateGiayPhepRequest)
│   ├── api/
│   │   ├── HoSoApiService.kt (CẬP NHẬT - thêm getAllHoSo)
│   │   └── GiayPhepApiService.kt (CẬP NHẬT - thêm createGiayPhep)
│   └── datastore/
│       └── TokenDataStore.kt (CẬP NHẬT - lưu role)
```

## Hướng dẫn sử dụng

### Đăng nhập với Cán bộ:
1. Mở app → Màn hình đăng nhập
2. Nhập email: `canbo@bca.gov.vn`
3. Nhập password: `canbo123`
4. Click "Đăng nhập"
5. Hệ thống sẽ điều hướng đến OfficerMainScreen

### Xử lý hồ sơ:
1. Chọn tab "Hồ sơ"
2. Click vào một hồ sơ cần xử lý
3. Màn hình ProcessProfile sẽ hiển thị với stepper
4. Điền thông tin:
   - Chọn Return Date (Ngày hẹn trả)
   - Nhập License Reference (Số giấy phép)
   - Chọn Update Status (Trạng thái)
5. Click "Update Profile" để lưu

### Xem file PDF:
1. Vào chi tiết hồ sơ
2. Tìm tài liệu đã upload
3. Click icon "mắt" (Visibility) để xem
4. File PDF sẽ mở bằng app mặc định trên điện thoại

## Lưu ý
- Đảm bảo có ứng dụng PDF reader trên thiết bị (Google PDF Viewer, Adobe Reader, v.v.)
- API cần có endpoint `/api/v1/ho-so` (không có query param) để lấy tất cả hồ sơ
- Cấu trúc response API phải khớp với model đã định nghĩa

## Next Steps (Tùy chọn)
- [ ] Thêm chức năng tìm kiếm và lọc hồ sơ cho cán bộ
- [ ] Thêm thống kê chi tiết ở tab Trang chủ
- [ ] Tích hợp date picker thật sự cho Return Date
- [ ] Thêm chức năng tạo giấy phép trực tiếp từ ProcessProfileScreen

