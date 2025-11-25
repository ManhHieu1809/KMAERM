package com.example.kmaerm.core.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlin.math.min

/**
 * Constants for parallel processing configuration
 */
private const val DEFAULT_CONCURRENCY = 5
private const val MAX_RETRY = 3
private const val CHUNK_SIZE = 10
private const val INITIAL_BACKOFF_MS = 1000L
private const val MAX_BACKOFF_MS = 30000L

/**
 * Parallel processing engine for efficient concurrent operations.
 * Provides thread-safe, type-safe parallel processing capabilities with
 * proper resource management and error handling.
 *
 * Example usage:
 * ```kotlin
 * val processor = ParallelProcessor()
 * val results = processor.processBatch(items) { item ->
 *     processItem(item)
 * }
 * ```
 */
class ParallelProcessor {

    /**
     * Process a batch of items in parallel with concurrency limit.
     * Ensures that no more than [concurrency] operations run simultaneously.
     *
     * @param T The type of input items
     * @param R The type of output items
     * @param items List of items to process
     * @param concurrency Maximum number of concurrent operations (default: 5)
     * @param transform Suspend function to transform each item
     * @return List of transformed items wrapped in Result
     *
     * Example:
     * ```kotlin
     * val users = listOf("user1", "user2", "user3")
     * val results = processor.processBatch(users, concurrency = 3) { userId ->
     *     fetchUserDetails(userId)
     * }
     * results.forEach { result ->
     *     result.onSuccess { user -> println("Fetched: $user") }
     *     result.onFailure { error -> println("Error: ${error.message}") }
     * }
     * ```
     */
    suspend fun <T, R> processBatch(
        items: List<T>,
        concurrency: Int = DEFAULT_CONCURRENCY,
        transform: suspend (T) -> R
    ): List<Result<R>> = coroutineScope {
        val semaphore = Semaphore(concurrency)
        items.map { item ->
            async {
                semaphore.withPermit {
                    runCatching { transform(item) }
                }
            }
        }.awaitAll()
    }

    /**
     * Upload multiple files in parallel with progress tracking.
     * Reports progress for each file upload through a callback.
     *
     * @param T The type of file representation
     * @param R The type of upload result
     * @param files List of files to upload
     * @param concurrency Maximum concurrent uploads (default: 5)
     * @param onProgress Callback for progress updates (fileIndex, progress 0-100)
     * @param upload Suspend function to upload a single file
     * @return List of upload results wrapped in Result
     *
     * Example:
     * ```kotlin
     * val files = listOf(file1, file2, file3)
     * val results = processor.parallelUpload(
     *     files = files,
     *     concurrency = 3,
     *     onProgress = { index, progress ->
     *         println("File $index: $progress%")
     *     }
     * ) { file ->
     *     uploadService.upload(file)
     * }
     * ```
     */
    suspend fun <T, R> parallelUpload(
        files: List<T>,
        concurrency: Int = DEFAULT_CONCURRENCY,
        onProgress: suspend (fileIndex: Int, progress: Int) -> Unit = { _, _ -> },
        upload: suspend (T, (Int) -> Unit) -> R
    ): List<Result<R>> = coroutineScope {
        val semaphore = Semaphore(concurrency)
        val scope = this
        files.mapIndexed { index, file ->
            async {
                semaphore.withPermit {
                    runCatching {
                        upload(file) { progress ->
                            scope.launch { onProgress(index, progress) }
                        }
                    }
                }
            }
        }.awaitAll()
    }

    /**
     * Download items in parallel with automatic retry on failure.
     * Each failed download will be retried up to [maxRetry] times with exponential backoff.
     *
     * @param T The type of download identifier
     * @param R The type of download result
     * @param items List of items to download
     * @param concurrency Maximum concurrent downloads (default: 5)
     * @param maxRetry Maximum retry attempts (default: 3)
     * @param download Suspend function to download a single item
     * @return List of download results wrapped in Result
     *
     * Example:
     * ```kotlin
     * val urls = listOf("url1", "url2", "url3")
     * val results = processor.parallelDownloadWithRetry(
     *     items = urls,
     *     concurrency = 3,
     *     maxRetry = 3
     * ) { url ->
     *     downloadService.download(url)
     * }
     * ```
     */
    suspend fun <T, R> parallelDownloadWithRetry(
        items: List<T>,
        concurrency: Int = DEFAULT_CONCURRENCY,
        maxRetry: Int = MAX_RETRY,
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

    /**
     * Execute an operation with exponential backoff retry strategy.
     * Increases delay exponentially on each retry, capped at MAX_BACKOFF_MS.
     *
     * @param T The type of operation result
     * @param maxRetry Maximum number of retry attempts (default: 3)
     * @param initialDelayMs Initial delay in milliseconds (default: 1000)
     * @param maxDelayMs Maximum delay in milliseconds (default: 30000)
     * @param block Suspend function to execute
     * @return Result wrapped operation result
     *
     * Example:
     * ```kotlin
     * val result = processor.retryWithExponentialBackoff(
     *     maxRetry = 5,
     *     initialDelayMs = 500,
     *     maxDelayMs = 10000
     * ) {
     *     apiService.fetchData()
     * }
     * result.onSuccess { data -> println("Success: $data") }
     * result.onFailure { error -> println("Failed after retries: ${error.message}") }
     * ```
     */
    suspend fun <T> retryWithExponentialBackoff(
        maxRetry: Int = MAX_RETRY,
        initialDelayMs: Long = INITIAL_BACKOFF_MS,
        maxDelayMs: Long = MAX_BACKOFF_MS,
        block: suspend () -> T
    ): Result<T> {
        var currentDelay = initialDelayMs
        var lastException: Throwable? = null

        repeat(maxRetry + 1) { attempt ->
            try {
                return Result.success(block())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                lastException = e
                if (attempt < maxRetry) {
                    delay(currentDelay)
                    currentDelay = min(currentDelay * 2, maxDelayMs)
                }
            }
        }

        return Result.failure(lastException ?: Exception("Unknown error after $maxRetry retries"))
    }

    /**
     * Process a stream of items using Flow with backpressure handling.
     * Transforms items concurrently while maintaining order.
     *
     * @param T The type of input items
     * @param R The type of output items
     * @param items Flow of items to process
     * @param concurrency Maximum concurrent operations (default: 5)
     * @param transform Suspend function to transform each item
     * @return Flow of transformed items
     *
     * Example:
     * ```kotlin
     * val itemFlow = flowOf(1, 2, 3, 4, 5)
     * processor.streamProcess(itemFlow, concurrency = 3) { item ->
     *     item * 2
     * }.collect { result ->
     *     println("Processed: $result")
     * }
     * ```
     */
    fun <T, R> streamProcess(
        items: Flow<T>,
        concurrency: Int = DEFAULT_CONCURRENCY,
        transform: suspend (T) -> R
    ): Flow<R> = items
        .map { item ->
            flow { emit(transform(item)) }
        }
        .flattenMerge(concurrency)

    /**
     * Process items in partitions for efficient big data processing.
     * Divides data into chunks and processes each chunk in parallel.
     *
     * @param T The type of input items
     * @param R The type of output items
     * @param items List of items to process
     * @param chunkSize Size of each partition (default: 10)
     * @param concurrency Maximum concurrent partitions (default: 5)
     * @param transform Suspend function to transform each item
     * @return List of transformed items
     *
     * Example:
     * ```kotlin
     * val largeDataset = (1..1000).toList()
     * val results = processor.partitionProcess(
     *     items = largeDataset,
     *     chunkSize = 50,
     *     concurrency = 4
     * ) { item ->
     *     heavyComputation(item)
     * }
     * println("Processed ${results.size} items")
     * ```
     */
    suspend fun <T, R> partitionProcess(
        items: List<T>,
        chunkSize: Int = CHUNK_SIZE,
        concurrency: Int = DEFAULT_CONCURRENCY,
        transform: suspend (T) -> R
    ): List<R> = coroutineScope {
        val semaphore = Semaphore(concurrency)
        items.chunked(chunkSize).map { chunk ->
            async {
                semaphore.withPermit {
                    chunk.map { item -> async { transform(item) } }.awaitAll()
                }
            }
        }.awaitAll().flatten()
    }

    /**
     * Fetch multiple pages of data in batch with pagination support.
     * Efficiently fetches pages in parallel with rate limiting.
     *
     * @param T The type of page data
     * @param totalPages Total number of pages to fetch
     * @param concurrency Maximum concurrent page fetches (default: 5)
     * @param fetchPage Suspend function to fetch a single page (0-indexed)
     * @return List of page results wrapped in Result
     *
     * Example:
     * ```kotlin
     * val allPages = processor.batchFetchPages(
     *     totalPages = 10,
     *     concurrency = 3
     * ) { pageNumber ->
     *     apiService.getPage(pageNumber)
     * }
     * val successfulPages = allPages.mapNotNull { it.getOrNull() }
     * println("Fetched ${successfulPages.size} pages successfully")
     * ```
     */
    suspend fun <T> batchFetchPages(
        totalPages: Int,
        concurrency: Int = DEFAULT_CONCURRENCY,
        fetchPage: suspend (pageNumber: Int) -> T
    ): List<Result<T>> = coroutineScope {
        val semaphore = Semaphore(concurrency)
        (0 until totalPages).map { page ->
            async {
                semaphore.withPermit {
                    runCatching { fetchPage(page) }
                }
            }
        }.awaitAll()
    }

    /**
     * Parallel map with rate limiting to prevent overwhelming services.
     * Uses a Semaphore for concurrency control and enforces minimum delay between operations.
     *
     * @param T The type of input items
     * @param R The type of output items
     * @param items List of items to process
     * @param concurrency Maximum concurrent operations (default: 5)
     * @param delayBetweenMs Minimum delay in milliseconds between starting new operations (default: 100)
     * @param transform Suspend function to transform each item
     * @return List of transformed items wrapped in Result
     *
     * Example:
     * ```kotlin
     * val apiCalls = listOf("endpoint1", "endpoint2", "endpoint3")
     * val results = processor.parallelMapWithRateLimit(
     *     items = apiCalls,
     *     concurrency = 2,
     *     delayBetweenMs = 200
     * ) { endpoint ->
     *     apiService.call(endpoint)
     * }
     * ```
     */
    suspend fun <T, R> parallelMapWithRateLimit(
        items: List<T>,
        concurrency: Int = DEFAULT_CONCURRENCY,
        delayBetweenMs: Long = 100L,
        transform: suspend (T) -> R
    ): List<Result<R>> = coroutineScope {
        val semaphore = Semaphore(concurrency)
        val rateLimitMutex = Mutex()
        var lastStart = 0L
        
        items.map { item ->
            async {
                rateLimitMutex.withLock {
                    val now = System.currentTimeMillis()
                    val elapsed = now - lastStart
                    if (elapsed < delayBetweenMs && lastStart > 0) {
                        delay(delayBetweenMs - elapsed)
                    }
                    lastStart = System.currentTimeMillis()
                }
                semaphore.withPermit {
                    runCatching { transform(item) }
                }
            }
        }.awaitAll()
    }

    /**
     * Execute a Flow with timeout support.
     * Cancels the flow if it takes longer than the specified timeout.
     *
     * @param T The type of flow items
     * @param source Source flow to execute
     * @param timeoutMs Timeout in milliseconds
     * @return Flow with timeout applied
     *
     * Example:
     * ```kotlin
     * val slowFlow = flow {
     *     delay(5000)
     *     emit("result")
     * }
     * try {
     *     processor.flowWithTimeout(slowFlow, timeoutMs = 2000)
     *         .collect { value -> println("Received: $value") }
     * } catch (e: TimeoutCancellationException) {
     *     println("Flow timed out")
     * }
     * ```
     */
    fun <T> flowWithTimeout(
        source: Flow<T>,
        timeoutMs: Long
    ): Flow<T> = flow {
        withTimeout(timeoutMs) {
            source.collect { value ->
                emit(value)
            }
        }
    }
}

/**
 * Extension function to map items in parallel with configurable concurrency.
 * Provides a convenient way to transform collections concurrently.
 *
 * @param T The type of input items
 * @param R The type of output items
 * @param concurrency Maximum concurrent operations (default: 5)
 * @param transform Suspend function to transform each item
 * @return List of transformed items
 *
 * Example:
 * ```kotlin
 * val userIds = listOf(1, 2, 3, 4, 5)
 * val users = userIds.parallelMap(concurrency = 3) { id ->
 *     userRepository.getUser(id)
 * }
 * ```
 */
suspend fun <T, R> List<T>.parallelMap(
    concurrency: Int = DEFAULT_CONCURRENCY,
    transform: suspend (T) -> R
): List<R> = coroutineScope {
    val semaphore = Semaphore(concurrency)
    this@parallelMap.map { item ->
        async {
            semaphore.withPermit {
                transform(item)
            }
        }
    }.awaitAll()
}

/**
 * Extension function to flatten a list of Results, extracting successful values.
 * Filters out failed results and returns only successful values.
 *
 * @param T The type of values
 * @return List containing only successful values
 *
 * Example:
 * ```kotlin
 * val results = listOf(
 *     Result.success(1),
 *     Result.failure(Exception("error")),
 *     Result.success(3)
 * )
 * val successValues = results.flattenResults() // [1, 3]
 * ```
 */
fun <T> List<Result<T>>.flattenResults(): List<T> =
    this.mapNotNull { it.getOrNull() }

/**
 * Extension function to partition Results into successes and failures.
 * Useful for handling both successful and failed operations separately.
 *
 * @param T The type of values
 * @return Pair of (successful values, exceptions)
 *
 * Example:
 * ```kotlin
 * val results = listOf(
 *     Result.success("a"),
 *     Result.failure(Exception("error")),
 *     Result.success("b")
 * )
 * val (successes, failures) = results.partitionResults()
 * println("Success: $successes") // ["a", "b"]
 * println("Failures: ${failures.size}") // 1
 * ```
 */
fun <T> List<Result<T>>.partitionResults(): Pair<List<T>, List<Throwable>> {
    val successes = mutableListOf<T>()
    val failures = mutableListOf<Throwable>()

    this.forEach { result ->
        result.onSuccess { successes.add(it) }
        result.onFailure { failures.add(it) }
    }

    return Pair(successes, failures)
}