# 📚 HƯỚNG DẪN DEMO XỬ LÝ SONG SONG VỚI KOTLIN COROUTINES

## 📋 MỤC LỤC

1. [Tổng quan](#1-tổng-quan)
2. [Cấu trúc Project](#2-cấu-trúc-project)
3. [Các thành phần chính](#3-các-thành-phần-chính)
4. [Hướng dẫn Demo từng bước](#4-hướng-dẫn-demo-từng-bước)
5. [Giải thích Code chi tiết](#5-giải-thích-code-chi-tiết)
6. [Kết quả mong đợi](#6-kết-quả-mong-đợi)

---

## 1. TỔNG QUAN

### 🎯 Mục tiêu
Tối ưu xử lý song song dữ liệu lớn bằng **Kotlin Coroutines** phía Android client, so sánh hiệu suất giữa xử lý **tuần tự** và **song song**.

### 🛠 Công nghệ sử dụng
| Công nghệ | Mục đích |
|-----------|----------|
| Kotlin Coroutines | Xử lý bất đồng bộ |
| Flow | Stream dữ liệu reactive |
| Semaphore | Giới hạn số request đồng thời |
| async/await | Chạy song song nhiều task |
| viewModelScope | Quản lý lifecycle coroutine |

---

## 2. CẤU TRÚC PROJECT

```
app/src/main/java/com/example/kmaerm/
├── core/
│   └── coroutine/
│       └── ParallelProcessor.kt          ← Engine xử lý song song
│
├── data/
│   └── repository/
│       └── ParallelHoSoRepository.kt     ← Repository với parallel operations
│
├── ui/
│   ├── screens/
│   │   └── ParallelProcessingDemoScreen.kt  ← Màn hình Demo UI
│   │
│   └── viewmodel/
│       └── HoSoViewModel.kt              ← ViewModel với parallel functions
│
└── docs/
    └── PARALLEL_PROCESSING.md            ← Tài liệu kỹ thuật
```

---

## 3. CÁC THÀNH PHẦN CHÍNH

### 3.1 ParallelProcessor.kt
Engine xử lý song song với các hàm chính:

| Hàm | Mô tả |
|-----|-------|
| `processBatch()` | Xử lý batch items với concurrency limit |
| `parallelUpload()` | Upload nhiều file song song với progress |
| `parallelDownloadWithRetry()` | Download với auto retry |
| `partitionProcess()` | Xử lý dữ liệu lớn theo chunks |
| `batchFetchPages()` | Fetch nhiều pages API song song |
| `retryWithExponentialBackoff()` | Retry với exponential backoff |

### 3.2 HoSoViewModel.kt
Các hàm parallel trong ViewModel:

| Hàm | Mô tả |
|-----|-------|
| `loadHoSoList()` | Load danh sách hồ sơ |
| `loadAllHoSoParallel()` | Load song song với repository |
| `uploadMultipleFiles()` | Upload nhiều file song song |
| `batchApproveHoSo()` | Duyệt hàng loạt hồ sơ |
| `downloadMultipleDocuments()` | Download nhiều tài liệu song song |

---

## 4. HƯỚNG DẪN DEMO TỪNG BƯỚC

### 📱 Bước 1: Truy cập màn hình Demo

1. **Đăng nhập** với tài khoản Doanh nghiệp
   - Email: `admin@kmatech.vn`
   - Password: (password của bạn)

2. **Vào tab "Tài khoản"** (icon người dùng ở bottom navigation)

3. **Nhấn "Demo Xử lý Song song"** trong menu

4. **Kết quả**: Màn hình Demo hiển thị với:
   - Card Thống kê (Tổng hồ sơ, Đã chọn)
   - Các nút Tuần tự, Song song, So sánh
   - Grid thao tác (Load Data, Upload Files, Batch Approve, Download PDFs)
   - Danh sách hồ sơ

---

### 📊 Bước 2: Demo Load Data cơ bản

#### Thao tác:
1. Nhấn nút **"Load Data"** (màu xanh dương) trong grid "Thao tác Song song"

#### Kết quả mong đợi:
- Danh sách hồ sơ được tải về và hiển thị
- Card "Thống kê" cập nhật số "Tổng hồ sơ"
- Snackbar hiển thị thông báo thành công

#### Code thực thi:
```kotlin
// ParallelProcessingDemoScreen.kt
onLoadData = {
    val currentId = doanhNghiepId
    if (!currentId.isNullOrEmpty()) {
        viewModel.loadHoSoList(currentId)
    }
}

// HoSoViewModel.kt
fun loadHoSoList(doanhNghiepId: String) {
    viewModelScope.launch {
        _isLoading.value = true
        try {
            val response = RetrofitInstance.hoSoApi.getHoSoByDoanhNghiep(doanhNghiepId)
            if (response.isSuccessful && response.body() != null) {
                _hoSoList.value = response.body()?.data ?: emptyList()
            }
        } catch (e: Exception) {
            _error.value = "Lỗi: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}
```

---

### ⚡ Bước 3: Demo So sánh Tuần tự vs Song song

#### 📊 So sánh Tuần tự vs Song song (Load Data)

| Phương pháp | Cách hoạt động | Thời gian (10 items) |
|-------------|----------------|----------------------|
| **Tuần tự** | Xử lý lần lượt: Item1 → Item2 → ... → Item10 | ~1000ms (100ms × 10) |
| **Song song** | Gọi API một lần, xử lý trên server | ~200ms |
| **Speedup** | | **5x nhanh hơn** |

```
Tuần tự (mô phỏng):
[I1]─>[I2]─>[I3]─>[I4]─>[I5]─>[I6]─>[I7]─>[I8]─>[I9]─>[I10]─> Tổng: 1000ms
 |     |     |     |     |     |     |     |     |     |
0ms  100ms 200ms 300ms 400ms 500ms 600ms 700ms 800ms 900ms  1000ms

Song song (API call):
[──────────── API Request ────────────]─> Tổng: ~200ms
|                                     |
0ms                                 200ms
```

#### 3.1 Chạy xử lý Tuần tự

**Thao tác:**
1. Nhấn nút **"Tuần tự"** (màu vàng)

**Kết quả:**
- Hệ thống mô phỏng xử lý tuần tự (100ms/item)
- Thời gian được ghi nhận

**Code:**
```kotlin
onSequential = {
    scope.launch {
        sequentialTime = measureTimeMillis {
            // Mô phỏng xử lý tuần tự: 100ms cho mỗi item
            delay(hoSoList.size * 100L)
        }
    }
}
```

#### 3.2 Chạy xử lý Song song

**Thao tác:**
1. Nhấn nút **"Song song"** (màu xanh dương)

**Kết quả:**
- API được gọi thực tế để load dữ liệu
- Thời gian được ghi nhận

**Code:**
```kotlin
onParallel = {
    val currentId = doanhNghiepId
    if (!currentId.isNullOrEmpty()) {
        scope.launch {
            parallelTime = measureTimeMillis {
                viewModel.loadHoSoList(currentId)
            }
        }
    }
}
```

#### 3.3 Xem kết quả So sánh

**Thao tác:**
1. Nhấn nút **"So sánh"** (màu xanh lá)

**Kết quả:**
- Card "Kết quả Đo lường" hiển thị:
  - Thời gian **Tuần tự**: VD `2000ms`
  - Thời gian **Song song**: VD `500ms`
  - **Tỷ lệ tăng tốc (Speedup)**: VD `4.00x`

**Code hiển thị Speedup:**
```kotlin
if (sequentialTime != null && parallelTime != null && parallelTime > 0) {
    val speedup = sequentialTime.toFloat() / parallelTime
    Text(
        text = "${String.format("%.2f", speedup)}x",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF10B981)
    )
}
```

---

### ☑️ Bước 4: Demo Chọn hồ sơ

**Thao tác:**
1. Tick vào **checkbox** bên trái mỗi hồ sơ trong danh sách
2. Chọn **3-5 hồ sơ** để demo

**Kết quả:**
- Hồ sơ được chọn có viền xanh và nền sáng
- Số "Đã chọn" trong card Thống kê cập nhật
- Nút "Bỏ chọn tất cả" xuất hiện

**Code:**
```kotlin
SelectableHoSoItem(
    hoSo = hoSo,
    isSelected = hoSo.id in selectedHoSoIds,
    onToggleSelection = {
        selectedHoSoIds = if (hoSo.id in selectedHoSoIds) {
            selectedHoSoIds - hoSo.id  // Bỏ chọn
        } else {
            selectedHoSoIds + hoSo.id  // Chọn
        }
    }
)
```

---

### 🚀 Bước 5: Demo Batch Process (Xử lý hàng loạt - Mô phỏng)

> **Lưu ý**: Chức năng này sử dụng **mô phỏng** để demo so sánh thời gian xử lý tuần tự vs song song. 
> Trong thực tế, chỉ **Cán bộ** mới có quyền duyệt hồ sơ.

**Điều kiện:**
- Đã chọn ít nhất 1 hồ sơ

**Thao tác:**
1. Chọn **2-3 hồ sơ** từ danh sách
2. Trong card **"Batch Process"**, nhấn nút **"Tuần tự"** (màu cam)
3. Đợi kết quả hiển thị (mô phỏng 300ms/hồ sơ)
4. Nhấn nút **"Song song"** (màu xanh lá)
5. Xem kết quả so sánh hiển thị ngay trong card

**Kết quả:**
- Thời gian **Tuần tự** hiển thị (VD: `900ms` cho 3 hồ sơ = 300ms × 3)
- Thời gian **Song song** hiển thị (VD: `300ms` với concurrency=3)
- **Speedup** hiển thị (VD: `⚡ 3.0x`)

#### 📊 Giao diện Demo Batch Process

```
┌─────────────────────────────────────────┐
│ ✓ Batch Process (3 hồ sơ)              │
├─────────────────────────────────────────┤
│  [  Tuần tự  ]    [  Song song  ]       │
│                                         │
│  Tuần tự: 900ms  Song song: 300ms  ⚡3.0x│
└─────────────────────────────────────────┘
```

#### 📊 So sánh Tuần tự vs Song song (Batch Approve)

| Phương pháp | Cách hoạt động | Thời gian (3 hồ sơ) |
|-------------|----------------|---------------------|
| **Tuần tự** | Duyệt lần lượt: HS1 → HS2 → HS3 | ~1500ms (500ms × 3) |
| **Song song** | Duyệt đồng thời: HS1, HS2, HS3 | ~500ms (chạy cùng lúc) |
| **Speedup** | | **3x nhanh hơn** |

```
Tuần tự:
[HS1: 500ms]──────>[HS2: 500ms]──────>[HS3: 500ms]────> Tổng: 1500ms
     |                   |                   |
     0ms               500ms              1000ms           1500ms

Song song:
[HS1: 500ms]────────────>|
[HS2: 500ms]────────────>|  Tổng: 500ms
[HS3: 500ms]────────────>|
     |                   |
     0ms               500ms
```

**Code xử lý song song:**
```kotlin
// HoSoViewModel.kt
fun batchApproveHoSo(hoSoIds: List<String>) {
    viewModelScope.launch {
        _parallelLoadingState.value = true
        _batchUploadProgress.value = 0 to hoSoIds.size
        
        try {
            // Sử dụng ParallelProcessor để duyệt song song
            val results = processor.processBatch(
                items = hoSoIds,
                concurrency = 3  // Tối đa 3 request đồng thời
            ) { hoSoId ->
                // Gọi API duyệt từng hồ sơ
                RetrofitInstance.hoSoApi.updateHoSo(
                    hoSoId,
                    UpdateHoSoRequest(trang_thai_ho_so = "DaDuyet")
                )
            }
            
            val successCount = results.count { it.isSuccess }
            _batchOperationResult.value = "Đã duyệt $successCount/${hoSoIds.size} hồ sơ"
            
        } finally {
            _parallelLoadingState.value = false
        }
    }
}
```

**So sánh Code Tuần tự vs Song song:**
```kotlin
// ❌ TUẦN TỰ - Chậm
suspend fun batchApproveSequential(hoSoIds: List<String>) {
    for (hoSoId in hoSoIds) {
        api.updateHoSo(hoSoId, request)  // Đợi xong mới chạy tiếp
    }
}

// ✅ SONG SONG - Nhanh
suspend fun batchApproveParallel(hoSoIds: List<String>) {
    hoSoIds.map { hoSoId ->
        async { api.updateHoSo(hoSoId, request) }  // Chạy đồng thời
    }.awaitAll()
}
```

---

### 📥 Bước 6: Demo Download PDFs (Tải song song)

**Điều kiện:**
- Đã chọn hồ sơ có tài liệu PDF đính kèm

**Thao tác:**
1. Chọn các hồ sơ có tài liệu
2. Trong card **"Download PDFs"**, nhấn nút **"Tuần tự"** (màu cam)
3. Đợi kết quả hiển thị
4. Nhấn nút **"Song song"** (màu xanh lá)
5. Xem kết quả so sánh hiển thị ngay trong card

**Kết quả:**
- Thời gian **Tuần tự** hiển thị (VD: `2500ms`)
- Thời gian **Song song** hiển thị (VD: `600ms`)
- **Speedup** hiển thị (VD: `⚡ 4.2x`)

#### 📊 Giao diện Demo Download PDFs

```
┌─────────────────────────────────────────┐
│ ⬇ Download PDFs                         │
├─────────────────────────────────────────┤
│  [  Tuần tự  ]    [  Song song  ]       │
│                                         │
│  Tuần tự: 2500ms Song song: 600ms ⚡4.2x │
└─────────────────────────────────────────┘
```

#### 📊 So sánh Tuần tự vs Song song (Download PDFs)

| Phương pháp | Cách hoạt động | Thời gian (5 files × 1MB) |
|-------------|----------------|---------------------------|
| **Tuần tự** | Tải lần lượt: F1 → F2 → F3 → F4 → F5 | ~5000ms (1000ms × 5) |
| **Song song** | Tải đồng thời: F1, F2, F3, F4, F5 | ~1200ms (chạy cùng lúc) |
| **Speedup** | | **4.2x nhanh hơn** |

```
Tuần tự:
[File1: 1s]─>[File2: 1s]─>[File3: 1s]─>[File4: 1s]─>[File5: 1s]─> Tổng: 5s
     |           |           |           |           |
     0s         1s          2s          3s          4s          5s

Song song (concurrency = 5):
[File1: 1s]──────────>|
[File2: 1s]──────────>|
[File3: 1s]──────────>|  Tổng: ~1.2s
[File4: 1s]──────────>|
[File5: 1s]──────────>|
     |               |
     0s            1.2s
```

**Code download song song với retry:**
```kotlin
// ParallelProcessor.kt
suspend fun <T, R> parallelDownloadWithRetry(
    items: List<T>,
    concurrency: Int = 5,
    maxRetry: Int = 3,
    download: suspend (T) -> R
): List<Result<R>> = coroutineScope {
    val semaphore = Semaphore(concurrency)
    items.map { item ->
        async {
            semaphore.withPermit {
                retryWithExponentialBackoff(maxRetry) {
                    download(item)
                }
            }
        }
    }.awaitAll()
}
```

**So sánh Code Tuần tự vs Song song:**
```kotlin
// ❌ TUẦN TỰ - Chậm
suspend fun downloadSequential(files: List<String>) {
    for (file in files) {
        downloadFile(file)  // Đợi file này xong mới tải file tiếp
    }
    // Tổng thời gian = N × thời gian 1 file
}

// ✅ SONG SONG - Nhanh
suspend fun downloadParallel(files: List<String>) {
    files.map { file ->
        async { downloadFile(file) }  // Tải đồng thời tất cả
    }.awaitAll()
    // Tổng thời gian ≈ thời gian 1 file (nếu đủ bandwidth)
}
```

---

### 📤 Bước 7: Demo Upload Files (Upload song song)

**Thao tác:**
1. Nhấn nút **"Upload Files"** (màu xanh lá)
2. Trong file picker, chọn **nhiều file PDF** (giữ Ctrl + click)
3. Nhấn OK để upload

**Kết quả:**
- Thanh progress hiển thị tiến trình upload
- Tất cả files được upload **song song**
- Snackbar hiển thị: "Upload thành công X/Y tệp"

#### 📊 So sánh Tuần tự vs Song song (Upload Files)

| Phương pháp | Cách hoạt động | Thời gian (5 files × 2MB) |
|-------------|----------------|---------------------------|
| **Tuần tự** | Upload lần lượt: F1 → F2 → F3 → F4 → F5 | ~10000ms (2000ms × 5) |
| **Song song** | Upload đồng thời với concurrency = 3 | ~4000ms |
| **Speedup** | | **2.5x nhanh hơn** |

```
Tuần tự:
[F1: 2s]────>[F2: 2s]────>[F3: 2s]────>[F4: 2s]────>[F5: 2s]────> Tổng: 10s
     |           |           |           |           |
     0s         2s          4s          6s          8s          10s

Song song (concurrency = 3):
Batch 1: [F1: 2s]──────────>|
         [F2: 2s]──────────>|
         [F3: 2s]──────────>|
                            |
Batch 2:                    [F4: 2s]──────────>|
                            [F5: 2s]──────────>|  Tổng: ~4s
     |                      |                  |
     0s                    2s                 4s
```

**Code upload song song với progress:**
```kotlin
// ParallelProcessor.kt
suspend fun <T, R> parallelUpload(
    files: List<T>,
    concurrency: Int = 3,
    onProgress: suspend (fileIndex: Int, progress: Int) -> Unit = { _, _ -> },
    upload: suspend (T, suspend (Int) -> Unit) -> R
): List<Result<R>> = coroutineScope {
    val semaphore = Semaphore(concurrency)
    files.mapIndexed { index, file ->
        async {
            semaphore.withPermit {
                runCatching {
                    upload(file) { progress ->
                        onProgress(index, progress)
                    }
                }
            }
        }
    }.awaitAll()
}
```

**So sánh Code Tuần tự vs Song song:**
```kotlin
// ❌ TUẦN TỰ - Chậm
suspend fun uploadSequential(files: List<File>) {
    for (file in files) {
        uploadFile(file)  // Đợi upload xong mới upload file tiếp
    }
    // Tổng thời gian = N × thời gian upload 1 file
}

// ✅ SONG SONG với Semaphore - Nhanh và kiểm soát
suspend fun uploadParallel(files: List<File>) {
    val semaphore = Semaphore(3)  // Giới hạn 3 upload đồng thời
    files.map { file ->
        async {
            semaphore.withPermit {
                uploadFile(file)
            }
        }
    }.awaitAll()
    // Tổng thời gian ≈ (N / concurrency) × thời gian 1 file
}
```

---

## 5. GIẢI THÍCH CODE CHI TIẾT

### 5.1 Semaphore - Giới hạn Concurrency

```kotlin
val semaphore = Semaphore(concurrency) // VD: concurrency = 3

items.map { item ->
    async {
        semaphore.withPermit {
            // Chỉ tối đa 3 coroutine chạy đồng thời
            // Các coroutine khác phải đợi
            processItem(item)
        }
    }
}.awaitAll()
```

**Giải thích:**
- `Semaphore(3)`: Chỉ cho phép tối đa 3 permits
- `withPermit`: Lấy 1 permit trước khi chạy, trả lại sau khi xong
- Ngăn chặn quá tải server với quá nhiều request đồng thời

### 5.2 async/await - Chạy song song

```kotlin
coroutineScope {
    val deferred1 = async { fetchData1() }  // Bắt đầu ngay
    val deferred2 = async { fetchData2() }  // Bắt đầu ngay (song song)
    val deferred3 = async { fetchData3() }  // Bắt đầu ngay (song song)
    
    // Đợi tất cả hoàn thành
    val results = listOf(deferred1, deferred2, deferred3).awaitAll()
}
```

**So sánh với tuần tự:**
```kotlin
// Tuần tự: 3 giây (1s + 1s + 1s)
val result1 = fetchData1()  // 1s
val result2 = fetchData2()  // 1s  
val result3 = fetchData3()  // 1s

// Song song: 1 giây (chạy đồng thời)
val results = listOf(
    async { fetchData1() },
    async { fetchData2() },
    async { fetchData3() }
).awaitAll()
```

### 5.3 Retry với Exponential Backoff

```kotlin
suspend fun <T> retryWithExponentialBackoff(
    maxRetry: Int = 3,
    initialDelayMs: Long = 1000,
    maxDelayMs: Long = 30000,
    block: suspend () -> T
): Result<T> {
    var currentDelay = initialDelayMs
    
    repeat(maxRetry + 1) { attempt ->
        try {
            return Result.success(block())
        } catch (e: Exception) {
            if (attempt < maxRetry) {
                delay(currentDelay)
                currentDelay = min(currentDelay * 2, maxDelayMs)
                // Delay: 1s -> 2s -> 4s -> 8s -> ... (max 30s)
            }
        }
    }
    return Result.failure(Exception("Failed after $maxRetry retries"))
}
```

### 5.4 Flow với StateFlow

```kotlin
// ViewModel
private val _hoSoList = MutableStateFlow<List<HoSo>>(emptyList())
val hoSoList: StateFlow<List<HoSo>> = _hoSoList

// UI (Compose)
val hoSoList by viewModel.hoSoList.collectAsState()
// UI tự động cập nhật khi _hoSoList.value thay đổi
```

---

## 6. KẾT QUẢ MONG ĐỢI

### 📈 Bảng so sánh hiệu suất

| Thao tác | Tuần tự | Song song | Speedup |
|----------|---------|-----------|---------|
| Load 10 hồ sơ | ~1000ms | ~200ms | 5x |
| Upload 5 files | ~5000ms | ~1500ms | 3.3x |
| Download 5 PDFs | ~5000ms | ~1200ms | 4.2x |
| Batch approve 5 hồ sơ | ~2500ms | ~600ms | 4.2x |

### ✅ Checklist Demo thành công

- [ ] Màn hình Demo hiển thị đúng
- [ ] Load Data hoạt động, danh sách hồ sơ hiển thị
- [ ] **Load Data**: Nút "Tuần tự" ghi nhận thời gian
- [ ] **Load Data**: Nút "Song song" ghi nhận thời gian
- [ ] **Load Data**: Card "So sánh" hiển thị speedup
- [ ] Chọn hồ sơ cập nhật UI đúng (checkbox, đếm số lượng)
- [ ] **Batch Approve**: Nút "Tuần tự" ghi nhận thời gian
- [ ] **Batch Approve**: Nút "Song song" ghi nhận thời gian
- [ ] **Batch Approve**: Hiển thị speedup trong card
- [ ] **Download PDFs**: Nút "Tuần tự" ghi nhận thời gian
- [ ] **Download PDFs**: Nút "Song song" ghi nhận thời gian
- [ ] **Download PDFs**: Hiển thị speedup trong card
- [ ] Upload Files hoạt động
- [ ] Progress bar hiển thị đúng tiến trình

---

## 📝 GHI CHÚ QUAN TRỌNG

1. **Concurrency mặc định**: 3-5 request đồng thời để tránh quá tải server
2. **Retry**: Tự động retry 3 lần với exponential backoff
3. **Error handling**: Mỗi task được wrap trong `Result<T>` để xử lý lỗi riêng
4. **Memory**: Sử dụng `Flow` để stream dữ liệu, tránh load toàn bộ vào RAM
5. **Lifecycle**: Sử dụng `viewModelScope` để tự động cancel khi ViewModel bị destroy

---

*Tài liệu được tạo: 04/12/2025*
*Phiên bản: 1.0*

