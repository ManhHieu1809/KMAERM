# Quick Reference Guide - Parallel Processing

## 🚀 Quick Start

### 1. Import Dependencies
```kotlin
import com.example.kmaerm.core.coroutine.ParallelProcessor
import com.example.kmaerm.core.result.ApiResult
import com.example.kmaerm.data.repository.ParallelHoSoRepository
```

### 2. Initialize
```kotlin
val processor = ParallelProcessor()
val repository = ParallelHoSoRepository()
```

---

## 📋 Common Operations

### Parallel Batch Processing
```kotlin
val items = listOf(1, 2, 3, 4, 5)
val results = processor.processBatch(
    items = items,
    concurrency = 5
) { item ->
    // Process each item
    processItem(item)
}

// Handle results
val (successes, failures) = results.partitionResults()
println("Success: ${successes.size}, Failed: ${failures.size}")
```

### Upload Multiple Files
```kotlin
repository.uploadMultipleDocuments(
    files = fileList,
    hoSoTaiLieuId = "123",
    onProgress = { current, total ->
        println("Progress: $current/$total")
    }
).collect { result ->
    when (result) {
        is ApiResult.Loading -> showLoading()
        is ApiResult.Success -> showSuccess(result.data)
        is ApiResult.Error -> showError(result.message)
    }
}
```

### Batch Approve
```kotlin
repository.batchApproveHoSo(
    hoSoIds = listOf("id1", "id2", "id3")
).collect { result ->
    result.onSuccess { approvedList ->
        println("Approved ${approvedList.size} hồ sơ")
    }
}
```

### Download with Retry
```kotlin
repository.downloadMultipleDocuments(
    taiLieuList = documents,
    outputDir = File(context.cacheDir, "downloads")
).collect { result ->
    result.onSuccess { files ->
        println("Downloaded ${files.size} files")
    }
}
```

---

## 🎨 UI Integration

### ViewModel
```kotlin
class MyViewModel : ViewModel() {
    private val repository = ParallelHoSoRepository()
    
    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState
    
    fun loadParallel() {
        viewModelScope.launch {
            repository.fetchAllHoSoParallel().collect { result ->
                when (result) {
                    is ApiResult.Loading -> _loadingState.value = true
                    is ApiResult.Success -> {
                        _loadingState.value = false
                        // Update UI
                    }
                    is ApiResult.Error -> {
                        _loadingState.value = false
                        // Show error
                    }
                }
            }
        }
    }
}
```

### Compose UI
```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = viewModel()) {
    val loading by viewModel.loadingState.collectAsState()
    
    Button(
        onClick = { viewModel.loadParallel() },
        enabled = !loading
    ) {
        Text("Load Parallel")
    }
    
    if (loading) {
        CircularProgressIndicator()
    }
}
```

---

## 🔧 Configuration

### Adjust Concurrency
```kotlin
// Low concurrency (safer)
processor.processBatch(items, concurrency = 3) { ... }

// Medium concurrency (balanced)
processor.processBatch(items, concurrency = 5) { ... }

// High concurrency (faster but more resources)
processor.processBatch(items, concurrency = 10) { ... }
```

### Retry Configuration
```kotlin
processor.retryWithExponentialBackoff(
    maxRetry = 5,              // Try 5 times
    initialDelayMs = 500,      // Start with 500ms
    maxDelayMs = 10000         // Max 10s delay
) {
    unstableOperation()
}
```

### Rate Limiting
```kotlin
processor.parallelMapWithRateLimit(
    items = items,
    concurrency = 3,
    delayBetweenMs = 200       // 200ms between requests
) { item ->
    apiCall(item)
}
```

---

## 🎯 Best Practices

### ✅ DO
```kotlin
// Use coroutineScope for structured concurrency
suspend fun myFunction() = coroutineScope {
    processor.processBatch(items) { ... }
}

// Handle partial failures
val (successes, failures) = results.partitionResults()
if (failures.isNotEmpty()) {
    logErrors(failures)
}

// Emit loading state first
flow {
    emit(ApiResult.Loading)
    // ... do work
    emit(ApiResult.Success(data))
}
```

### ❌ DON'T
```kotlin
// Don't use GlobalScope
GlobalScope.launch { ... }  // BAD!

// Don't ignore errors
processor.processBatch(items) { ... }  // No error handling!

// Don't set concurrency too high
processor.processBatch(items, concurrency = 100) { ... }  // TOO HIGH!
```

---

## 📊 Performance Tips

### For Network Operations
- Concurrency: 3-5
- Add retry mechanism
- Use rate limiting if needed

### For File Operations
- Concurrency: 5-10
- Use partition processing for large files
- Monitor memory usage

### For CPU-Intensive Tasks
- Concurrency: 2-4 (based on CPU cores)
- Use Dispatchers.Default
- Consider chunking

---

## 🐛 Troubleshooting

### Out of Memory
```kotlin
// Use partition processing
processor.partitionProcess(
    items = largeList,
    chunkSize = 50,
    concurrency = 3
) { item -> processItem(item) }
```

### Too Slow
```kotlin
// Increase concurrency
processor.processBatch(items, concurrency = 10) { ... }

// Or use parallel map extension
items.parallelMap(concurrency = 10) { ... }
```

### Server Overload (429 errors)
```kotlin
// Reduce concurrency + rate limit
processor.parallelMapWithRateLimit(
    items = items,
    concurrency = 2,
    delayBetweenMs = 500
) { item -> apiCall(item) }
```

---

## 📱 Demo Screen

Navigate to demo:
```kotlin
navController.navigate(Screen.ParallelDemo.route)
```

Features:
- ✅ Sequential vs Parallel comparison
- ✅ Real-time performance metrics
- ✅ 4 batch operations
- ✅ Multi-select list

---

## 📚 More Resources

- Full Documentation: [docs/PARALLEL_PROCESSING.md](docs/PARALLEL_PROCESSING.md)
- Implementation Summary: [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
- Unit Tests: [app/src/test/.../ParallelProcessorTest.kt](app/src/test/java/com/example/kmaerm/core/coroutine/ParallelProcessorTest.kt)

---

## 💡 Code Snippets

### Custom Parallel Operation
```kotlin
suspend fun myParallelOperation(items: List<String>) {
    val processor = ParallelProcessor()
    
    val results = processor.processBatch(
        items = items,
        concurrency = 5
    ) { item ->
        // Your custom logic
        myApi.process(item)
    }
    
    // Handle results
    results.forEach { result ->
        result.onSuccess { data -> println("Success: $data") }
        result.onFailure { error -> println("Error: ${error.message}") }
    }
}
```

### Progress Tracking
```kotlin
var progress by mutableStateOf(0f)

repository.uploadMultipleDocuments(
    files = files,
    hoSoTaiLieuId = id,
    onProgress = { current, total ->
        progress = current.toFloat() / total
    }
).collect { result ->
    // Handle result
}

// In UI
LinearProgressIndicator(progress = { progress })
```

### Error Recovery
```kotlin
val results = processor.processBatch(items) { item ->
    apiCall(item)
}

val (successes, failures) = results.partitionResults()

// Retry failures
if (failures.isNotEmpty()) {
    val failedItems = items.filterIndexed { i, _ ->
        results[i].isFailure
    }
    
    // Retry with exponential backoff
    failedItems.forEach { item ->
        processor.retryWithExponentialBackoff { 
            apiCall(item) 
        }
    }
}
```

---

**Quick Reference Version**: 1.0.0  
**Last Updated**: January 25, 2025

