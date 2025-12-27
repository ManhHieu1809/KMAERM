package com.example.kmaerm.data.model

import com.google.gson.annotations.SerializedName

data class DemoResult(
    @SerializedName("mode")
    val mode: String = "",

    @SerializedName("status")
    val status: String = "",

    @SerializedName("message")
    val message: String = "",

    @SerializedName("metrics")
    val metrics: DemoMetrics = DemoMetrics(),

    @SerializedName("infrastructure")
    val infrastructure: DemoInfrastructure = DemoInfrastructure(),

    @SerializedName("optimization_gain")
    val optimizationGain: OptimizationGain? = null
)

data class DemoMetrics(
    @SerializedName("total_files")
    val totalFiles: Int = 0,

    @SerializedName("total_time_ms")
    val totalTimeMs: Long = 0,

    @SerializedName("avg_latency")
    val avgLatency: String = "",

    @SerializedName("throughput")
    val throughput: String = ""
)

data class DemoInfrastructure(
    @SerializedName("cpu_utilization")
    val cpuUtilization: String = "",

    @SerializedName("io_strategy")
    val ioStrategy: String = "",

    @SerializedName("worker_pool")
    val workerPool: String = ""
)


data class OptimizationGain(
    @SerializedName("speed_up")
    val speedUp: String = "",

    @SerializedName("conclusion")
    val conclusion: String = ""
)


data class ComparisonUiModel(
    val sequentialResult: DemoResult?,
    val parallelResult: DemoResult?,
    val speedupRatio: Double?,
    val timeSaved: Long?,
    val efficiencyGain: Double?
)

