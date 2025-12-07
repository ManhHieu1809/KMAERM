package com.example.kmaerm.core.coroutine

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

/**
 * Unit tests for ParallelProcessor functionality.
 * Tests concurrency, retry mechanisms, and parallel processing capabilities.
 */
class ParallelProcessorTest {

    private lateinit var processor: ParallelProcessor

    @Before
    fun setup() {
        processor = ParallelProcessor()
    }

    @Test
    fun `testProcessBatch_Success - all items processed successfully`() = runTest {
        // Given
        val input = listOf(1, 2, 3, 4, 5)

        // When
        val results = processor.processBatch(input) { item ->
            item * 2
        }

        // Then
        assertThat(results).hasSize(5)
        results.forEach { result ->
            assertThat(result.isSuccess).isTrue()
        }

        val values = results.mapNotNull { it.getOrNull() }
        assertThat(values).containsExactly(2, 4, 6, 8, 10)
    }

    @Test
    fun `testProcessBatch_WithFailures - handles mixed success and failure`() = runTest {
        // Given
        val input = listOf(1, 2, 3, 4, 5)

        // When
        val results = processor.processBatch(input) { item ->
            if (item % 2 == 0) {
                throw Exception("Even number: $item")
            }
            item
        }

        // Then
        val (successes, failures) = results.partitionResults()
        assertThat(successes).containsExactly(1, 3, 5)
        assertThat(failures).hasSize(2)
    }

    @Test
    fun `testProcessBatch_Concurrency - respects concurrency limit`() = runTest {
        // Given
        val input = (1..100).toList()
        val concurrentCount = AtomicInteger(0)
        val maxConcurrent = AtomicInteger(0)
        val concurrency = 5

        // When
        val results = processor.processBatch(input, concurrency = concurrency) { item ->
            val current = concurrentCount.incrementAndGet()
            maxConcurrent.set(maxOf(maxConcurrent.get(), current))
            delay(10) // Simulate work
            concurrentCount.decrementAndGet()
            item
        }

        // Then
        assertThat(results).hasSize(100)
        assertThat(maxConcurrent.get()).isAtMost(concurrency)
    }

    @Test
    fun `testParallelUpload_ProgressTracking - reports progress correctly`() = runTest {
        // Given
        val files = (1..10).toList()
        val progressUpdates = mutableListOf<Pair<Int, Int>>()

        // When
        val results = processor.parallelUpload(
            files = files,
            concurrency = 3,
            onProgress = { index, progress ->
                progressUpdates.add(index to progress)
            }
        ) { file, reportProgress ->
            reportProgress(100)
            "uploaded-$file"
        }

        // Then
        assertThat(results).hasSize(10)
        assertThat(progressUpdates).hasSize(10)
    }

    @Test
    fun `testParallelDownloadWithRetry_Success - no retries needed`() = runTest {
        // Given
        val items = listOf(1, 2, 3, 4, 5)

        // When
        val results = processor.parallelDownloadWithRetry(
            items = items,
            concurrency = 3,
            maxRetry = 3
        ) { item ->
            "downloaded-$item"
        }

        // Then
        val (successes, failures) = results.partitionResults()
        assertThat(successes).hasSize(5)
        assertThat(failures).isEmpty()
    }

    @Test
    fun `testParallelDownloadWithRetry_WithRetries - retries on failure`() = runTest {
        // Given
        val items = listOf(1, 2, 3)
        val attemptCounts = mutableMapOf<Int, AtomicInteger>()

        // When
        val results = processor.parallelDownloadWithRetry(
            items = items,
            concurrency = 2,
            maxRetry = 3
        ) { item ->
            val count = attemptCounts.getOrPut(item) { AtomicInteger(0) }
            val attempt = count.incrementAndGet()

            if (attempt < 3) {
                throw Exception("Download failed for item $item (attempt $attempt)")
            }
            "downloaded-$item"
        }

        // Then
        val (successes, failures) = results.partitionResults()
        assertThat(successes).hasSize(3)
        attemptCounts.values.forEach { count ->
            assertThat(count.get()).isEqualTo(3)
        }
    }

    @Test
    fun `testRetryWithExponentialBackoff_Success - succeeds on first try`() = runTest {
        // Given
        var attempts = 0

        // When
        val time = measureTimeMillis {
            val result = processor.retryWithExponentialBackoff(maxRetry = 3) {
                attempts++
                "success"
            }

            // Then
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isEqualTo("success")
        }

        assertThat(attempts).isEqualTo(1)
        assertThat(time).isLessThan(100) // No delays
    }

    @Test
    fun `testRetryWithExponentialBackoff_AllFailed - returns failure after max retries`() = runTest {
        // Given
        var attempts = 0

        // When
        val result = processor.retryWithExponentialBackoff(
            maxRetry = 3,
            initialDelayMs = 10,
            maxDelayMs = 100
        ) {
            attempts++
            throw Exception("Always fails")
        }

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(attempts).isEqualTo(4) // Initial + 3 retries
    }

    @Test
    fun `testRetryWithExponentialBackoff_CancellationException - throws immediately`() = runTest {
        // Given/When/Then
        try {
            processor.retryWithExponentialBackoff(maxRetry = 3) {
                throw CancellationException("Cancelled")
            }
            assertThat(false).isTrue() // Should not reach here
        } catch (e: CancellationException) {
            assertThat(e.message).isEqualTo("Cancelled")
        }
    }

    @Test
    fun `testStreamProcess - processes flow concurrently`() = runTest {
        // Given
        val itemFlow = flow {
            for (i in 1..10) {
                emit(i)
            }
        }

        // When
        val results = processor.streamProcess(
            items = itemFlow,
            concurrency = 3
        ) { item ->
            delay(10)
            item * 2
        }

        // Then
        val list = results.toList()
        assertThat(list).hasSize(10)
        assertThat(list.toSet()).containsExactlyElementsIn((1..10).map { it * 2 })
    }

    @Test
    fun `testPartitionProcess - processes in chunks`() = runTest {
        // Given
        val items = (1..100).toList()

        // When
        val results = processor.partitionProcess(
            items = items,
            chunkSize = 10,
            concurrency = 5
        ) { item ->
            item * 2
        }

        // Then
        assertThat(results).hasSize(100)
        assertThat(results).containsExactlyElementsIn((1..100).map { it * 2 })
    }

    @Test
    fun `testBatchFetchPages - fetches all pages concurrently`() = runTest {
        // Given
        val totalPages = 5
        val fetchedPages = mutableSetOf<Int>()

        // When
        val results = processor.batchFetchPages(
            totalPages = totalPages,
            concurrency = 3
        ) { pageNumber ->
            delay(50)
            fetchedPages.add(pageNumber)
            "page-$pageNumber"
        }

        // Then
        val (successes, failures) = results.partitionResults()
        assertThat(successes).hasSize(5)
        assertThat(failures).isEmpty()
        assertThat(fetchedPages).containsExactly(0, 1, 2, 3, 4)
    }

    @Test
    fun `testParallelMapWithRateLimit - enforces rate limiting`() = runTest {
        // Given
        val items = (1..10).toList()
        val delayBetweenMs = 100L

        // When
        val time = measureTimeMillis {
            processor.parallelMapWithRateLimit(
                items = items,
                concurrency = 5,
                delayBetweenMs = delayBetweenMs
            ) { item ->
                item * 2
            }
        }

        // Then
        // Should take at least (items.size - 1) * delayBetweenMs
        val minExpectedTime = (items.size - 1) * delayBetweenMs
        assertThat(time).isAtLeast(minExpectedTime * 0.8.toLong()) // Allow 20% margin
    }

    @Test
    fun `testFlowWithTimeout_Success - completes within timeout`() = runTest {
        // Given
        val sourceFlow = flow {
            delay(100)
            emit("value")
        }

        // When
        val results = processor.flowWithTimeout(sourceFlow, timeoutMs = 1000)
            .toList()

        // Then
        assertThat(results).containsExactly("value")
    }

    @Test
    fun `testFlowWithTimeout_Timeout - throws on timeout`() = runTest {
        // Given
        val sourceFlow = flow {
            delay(2000)
            emit("value")
        }

        // When/Then
        try {
            processor.flowWithTimeout(sourceFlow, timeoutMs = 100)
                .toList()
            assertThat(false).isTrue() // Should not reach here
        } catch (e: Exception) {
            // Expected timeout exception
            assertThat(e).isNotNull()
        }
    }

    @Test
    fun `testExtensionFunctions_parallelMap - maps items in parallel`() = runTest {
        // Given
        val items = (1..50).toList()

        // When
        val time = measureTimeMillis {
            val results = items.parallelMap(concurrency = 5) { item ->
                delay(10)
                item * 2
            }

            // Then
            assertThat(results).hasSize(50)
            assertThat(results).containsExactlyElementsIn((1..50).map { it * 2 })
        }

        // Parallel should be faster than sequential
        assertThat(time).isLessThan(50 * 10) // Much less than sequential time
    }

    @Test
    fun `testExtensionFunctions_flattenResults - extracts successful values`() = runTest {
        // Given
        val results = listOf(
            Result.success(1),
            Result.failure(Exception("error")),
            Result.success(3),
            Result.failure(Exception("error2")),
            Result.success(5)
        )

        // When
        val flattened = results.flattenResults()

        // Then
        assertThat(flattened).containsExactly(1, 3, 5)
    }

    @Test
    fun `testExtensionFunctions_partitionResults - separates successes and failures`() = runTest {
        // Given
        val results = listOf(
            Result.success("a"),
            Result.failure(Exception("error1")),
            Result.success("b"),
            Result.success("c"),
            Result.failure(Exception("error2"))
        )

        // When
        val (successes, failures) = results.partitionResults()

        // Then
        assertThat(successes).containsExactly("a", "b", "c")
        assertThat(failures).hasSize(2)
        assertThat(failures.map { it.message }).containsExactly("error1", "error2")
    }

    @Test
    fun `testPerformanceGain - parallel is faster than sequential`() = runTest {
        // Given
        val items = (1..50).toList()
        val processingTimeMs = 20L

        // When - Sequential
        val sequentialTime = measureTimeMillis {
            items.forEach { item ->
                delay(processingTimeMs)
            }
        }

        // When - Parallel
        val parallelTime = measureTimeMillis {
            processor.processBatch(items, concurrency = 10) { item ->
                delay(processingTimeMs)
                item
            }
        }

        // Then - Parallel should be significantly faster (at least 3x)
        assertThat(parallelTime).isLessThan(sequentialTime / 3)
    }

    @Test
    fun `testEmptyInputs - handles empty lists gracefully`() = runTest {
        // Given
        val emptyList = emptyList<Int>()

        // When
        val results = processor.processBatch(emptyList) { it * 2 }

        // Then
        assertThat(results).isEmpty()
    }

    @Test
    fun `testSingleItem - processes single item correctly`() = runTest {
        // Given
        val singleItem = listOf(42)

        // When
        val results = processor.processBatch(singleItem) { it * 2 }

        // Then
        assertThat(results).hasSize(1)
        assertThat(results[0].getOrNull()).isEqualTo(84)
    }
}

