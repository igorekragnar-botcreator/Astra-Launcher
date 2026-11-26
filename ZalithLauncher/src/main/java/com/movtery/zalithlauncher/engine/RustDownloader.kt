/*
 * Basalt Rust downloader bridge
 * Copyright (C) 2026 Leonxlnx (loak7993-code)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.engine

import android.os.Build
import com.movtery.zalithlauncher.game.download.engine.DownloadRequest
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Bridge to the Rust download engine: shared keep-alive agent, bounded worker
 * threads, 60s timeouts, 3-attempt retries with candidate rotation, SHA1
 * verification, and byte-accurate progress. Job-based: start, poll, wait.
 *
 * The JNI symbols themselves are declared in [RustEngine]: the native side
 * exports Java_<package>_RustEngine_*, so declaring them here would leave every
 * call unresolved at runtime. This object only marshals the JSON payloads.
 */
object RustDownloader {
    private const val TAG = "RustDownloader"

    /** True when the native engine is loaded; callers fall back to Kotlin otherwise. */
    val available: Boolean = RustEngine.isAvailable

    @Serializable
    data class Snapshot(
        val done: Int,
        val total: Int,
        val bytesDone: Long,
        val bytesTotal: Long,
        val finished: Boolean
    )

    @Serializable
    data class Failure(val path: String, val error: String)

    private val json = Json { ignoreUnknownKeys = true }

    /** The classpath plan is built for the device ABI, not the build host one. */
    private val platformArch: String by lazy {
        val abi = Build.SUPPORTED_ABIS.firstOrNull().orEmpty()
        if (abi.startsWith("arm64")) "arm64" else "x86_64"
    }

    /** Starts a Rust download job for the given tasks. Returns the job id. */
    fun start(requests: List<DownloadRequest>, workers: Int): Int {
        val array = JsonArray(
            requests.map { request ->
                buildJsonObject {
                    put(
                        "urls",
                        JsonArray(request.urls.map { JsonPrimitive(it) })
                    )
                    put("target", request.targetFile.absolutePath)
                    request.sha1?.let { put("sha1", it) }
                    if (request.expectedSize > 0) put("size", request.expectedSize)
                }
            }
        )
        val jobId = RustEngine.downloadStart(array.toString(), workers)
        check(jobId >= 0) { "Rust download engine refused the job" }
        return jobId
    }

    fun progress(jobId: Int): Snapshot? {
        val raw = RustEngine.downloadProgress(jobId) ?: return null
        return runCatching { json.decodeFromString(Snapshot.serializer(), raw) }.getOrNull()
    }

    /** Blocks until the job finishes. Returns failures keyed by path. */
    fun wait(jobId: Int): Map<String, String> {
        val raw = RustEngine.downloadWait(jobId)
            ?: error("Rust download engine is unavailable")
        // A decode failure must not be read as "everything downloaded", so it
        // propagates instead of degrading to an empty failure map.
        val failures = json.decodeFromString<List<Failure>>(raw)
        return failures.associate { it.path to it.error }
    }

    @Serializable
    data class LaunchPlan(
        val jvmArgs: List<String>,
        val gameArgs: List<String>,
        val mainClass: String,
        val classpath: List<String>,
        val classpathString: String,
        val wrapperClass: String? = null,
        val requiredJavaMajor: Long? = null
    )

    /**
     * Builds the manifest-derived argument plan through the Rust engine in
     * Android mode: string entries only, placeholders intact (the host
     * substitutes through its own varArgMap), default-user-jvm skipped,
     * memory/GC left to the host's own policy step. Returns null on any
     * failure so the caller falls back to the Kotlin assembly.
     */
    fun buildPlan(manifestJson: String, versionName: String): LaunchPlan? {
        if (!available) return null
        val raw = RustEngine.buildPlan(
            manifestJson = manifestJson,
            layoutJson = """{"clientJar":"/dev/null","librariesDir":"/lib","nativesDir":"/nat","gameDir":"/g","assetsDir":"/a","assetIndexId":"x","runtimeHome":"/"}""",
            optionsJson = """{"ignoreDefaultUserJvm":true,"stringsOnly":true,"substituteClasspath":false,"stripAlwaysPreTouch":false}""",
            authJson = """{"playerName":"x","uuid":"x"}""",
            quickPlayJson = "null",
            platformArch = platformArch,
            versionName = versionName
        ) ?: return null
        return runCatching {
            json.decodeFromString(LaunchPlan.serializer(), raw)
        }.onFailure {
            Logger.warning(TAG, "Rust plan payload was not a LaunchPlan", it)
        }.getOrNull()
    }
}
