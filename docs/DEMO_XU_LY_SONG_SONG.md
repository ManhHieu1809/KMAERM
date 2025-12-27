# 📚 Hướng dẫn Demo Xử lý Song song

## 🎯 Tổng quan

Demo này mô phỏng tình huống **50 doanh nghiệp nộp hồ sơ cùng lúc** để so sánh hiệu năng giữa:
- **Single-thread (Blocking I/O)**: Xử lý tuần tự từng hồ sơ
- **Worker Pool (Non-blocking)**: Xử lý song song với 5 workers

## 🚀 Cách sử dụng

### 1. Truy cập Demo

Từ **Màn hình chính** → Nhấn **Tab Tài khoản** → Chọn **"Demo Xử lý Song song"**

### 2. Chạy Demo

#### Nút Đỏ - Single-thread (Blocking I/O)
```
Tên: Single-thread (Blocking I/O)
Mô tả: Xử lý tuần tự 50 hồ sơ
Màu: Đỏ (#DC3545)
```
- Nhấn nút đỏ để chạy xử lý tuần tự
- Mỗi hồ sơ được xử lý lần lượt (1 → 2 → 3 → ... → 50)
- Thời gian xử lý: ~5-10 giây

#### Nút Xanh - Worker Pool (Non-blocking)
```
Tên: Worker Pool (Non-blocking)
Mô tả: Xử lý song song với 5 workers
Màu: Xanh lá (#28A745)
```
- Nhấn nút xanh để chạy xử lý song song
- 5 workers xử lý đồng thời (tối đa 5 hồ sơ cùng lúc)
- Thời gian xử lý: ~1-2 giây

### 3. Theo dõi Tiến trình

Trong quá trình xử lý, bạn sẽ thấy:
- **Progress Bar**: Thanh tiến trình trực quan
- **Số lượng**: X / 50 hồ sơ đã xử lý
- **Trạng thái**: "Đang xử lý..." với spinner animation

### 4. Xem Kết quả

Sau khi hoàn thành, màn hình hiển thị:

#### Kết quả So sánh Hiệu năng
```
┌─────────────────────────────────────┐
│  📊 Kết quả so sánh hiệu năng      │
├─────────────────────────────────────┤
│  🔴 Single-thread: 7,500ms         │
│  🟢 Worker Pool:   1,800ms         │
├─────────────────────────────────────┤
│        Tăng tốc: 4.17x             │
│  Worker Pool nhanh hơn             │
│      Single-thread                  │
└─────────────────────────────────────┘
```

## 🏗️ Kiến trúc Kỹ thuật

### Frontend (Kotlin + Jetpack Compose)

```
ParallelDemoScreen
    ↓
ParallelDemoViewModel
    ↓
HoSoDemoRepository
    ↓
ParallelProcessor + FakeHoSoApiService
```

### Xử lý Tuần tự (Sequential)
```kotlin
for (item in items) {
    process(item)  // Xử lý từng item một
}
```
- Thời gian = n × t (n items, mỗi item mất t ms)
- Ví dụ: 50 items × 150ms = 7,500ms

### Xử lý Song song (Parallel)
```kotlin
items.map { item ->
    async {
        semaphore.withPermit {
            process(item)  // 5 items chạy đồng thời
        }
    }
}.awaitAll()
```
- Thời gian = (n ÷ workers) × t
- Ví dụ: (50 ÷ 5) × 150ms = 1,500ms
- Tăng tốc ~ 5x (lý thuyết)

## 📊 Các Thông số Kỹ thuật

| Thông số | Giá trị |
|----------|---------|
| **Số lượng hồ sơ** | 50 |
| **Thời gian xử lý mỗi hồ sơ** | 100-200ms (random) |
| **Số workers** | 5 |
| **Dispatcher** | Dispatchers.IO |
| **Concurrency control** | Semaphore |

## 🔧 Fake API

Demo sử dụng **Fake API** để mô phỏng xử lý backend:

```kotlin
// FakeHoSoApiService.kt
suspend fun processHoSoSubmission(recordId: Int): HoSo {
    delay(Random.nextLong(100, 200))  // Giả lập latency
    return HoSo(
        id = "HS_${recordId.toString().padStart(3, '0')}",
        doanh_nghiep_id = "DN_${recordId.toString().padStart(3, '0')}",
        ten_doanh_nghiep_vi = "Doanh nghiệp #$recordId",
        ma_ho_so = "MASH_${recordId}",
        loai_thu_tuc = "Đăng ký kinh doanh",
        // ...
    )
}
```

## 🎓 Mục đích Học tập

Demo này minh họa:

### 1. Kotlin Coroutines
- `viewModelScope.launch`
- `async / await`
- `Semaphore` (concurrency control)
- `Dispatchers.IO`

### 2. Reactive State Management
- `StateFlow`
- `MutableStateFlow`
- `collectAsState()` trong Compose

### 3. MVVM Architecture
```
View (Compose UI)
    ↕
ViewModel (Business Logic)
    ↕
Repository (Data Layer)
    ↕
Data Source (Fake API)
```

### 4. Performance Optimization
- So sánh Blocking vs Non-blocking I/O
- Worker Pool pattern
- Parallel processing benefits

## 📝 Code Samples

### ViewModel - Run Sequential
```kotlin
fun runSequential() {
    viewModelScope.launch {
        try {
            _isProcessing.value = true
            val startTime = System.currentTimeMillis()
            
            val results = repository.submitSequential(
                count = 50,
                onProgress = { current, total ->
                    _progress.value = current to total
                }
            )
            
            val executionTime = System.currentTimeMillis() - startTime
            _sequentialTime.value = executionTime
            
        } finally {
            _isProcessing.value = false
        }
    }
}
```

### Repository - Sequential Processing
```kotlin
suspend fun submitSequential(
    count: Int = 50,
    onProgress: suspend (current: Int, total: Int) -> Unit
): List<HoSo> {
    val businessIds = (1..count).toList()
    
    return processor.processSequential(
        items = businessIds,
        onProgress = onProgress
    ) { recordId ->
        fakeApi.processHoSoSubmission(recordId)
    }
}
```

### Repository - Parallel Processing
```kotlin
suspend fun submitParallel(
    count: Int = 50,
    workers: Int = 5,
    onProgress: suspend (current: Int, total: Int) -> Unit
): List<HoSo> {
    val businessIds = (1..count).toList()
    
    return processor.processWithWorkerPool(
        items = businessIds,
        workerCount = workers,
        onProgress = onProgress
    ) { recordId ->
        fakeApi.processHoSoSubmission(recordId)
    }
}
```

## 🎨 UI Components

### Header
- Icon: `Icons.Default.AccountTree`
- Title: "Mô phỏng số hóa và xác thực hồ sơ quy mô lớn"

### Scenario Card
- Mô tả kịch bản: 50 doanh nghiệp nộp hồ sơ cùng lúc
- Chi tiết: Mỗi hồ sơ ~100-200ms, 5 workers

### Action Buttons
- **Red Button**: Single-thread
- **Green Button**: Worker Pool

### Progress Section
- Current / Total: X / 50
- Progress Bar: Linear với rounded corners
- Loading Spinner: Khi đang xử lý

### Results Card
- Execution Time: Sequential vs Parallel
- Speedup Ratio: Tỷ lệ tăng tốc
- Success Count: Số hồ sơ đã xử lý

## 🔍 Kết quả Mong đợi

### Single-thread
- Thời gian: 5,000 - 10,000ms (5-10 giây)
- Trung bình: ~7,500ms

### Worker Pool
- Thời gian: 1,000 - 2,000ms (1-2 giây)
- Trung bình: ~1,500ms

### Speedup Ratio
- Lý thuyết: 5x (do có 5 workers)
- Thực tế: 3-5x (do overhead của coroutine context switching)

## 💡 Tips

1. **Chạy nhiều lần** để thấy sự khác biệt rõ ràng
2. **Nhấn Reset** để xóa kết quả và chạy lại
3. **Quan sát Progress Bar** để thấy tốc độ xử lý
4. **So sánh thời gian** giữa 2 phương pháp

## 🚫 Lưu ý

- Demo này **chỉ mô phỏng**, không gọi backend thật
- Sử dụng **Fake API** với delay ngẫu nhiên
- **Không lưu** dữ liệu vào database
- Kết quả có thể **khác nhau** mỗi lần chạy do random delay

## 📖 Tài liệu Liên quan

- [PARALLEL_PROCESSING.md](PARALLEL_PROCESSING.md) - Chi tiết kỹ thuật
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

---

**Phiên bản**: 2.0  
**Ngày cập nhật**: 26/12/2025  
**Tác giả**: KMAERM Development Team

