package com.sismico.openconversation.backend.adapters.out.transcription

/**
 * Abstraction over the ffmpeg invocation used by [FfmpegAudioConversionAdapter].
 *
 * The default implementation launches a real ffmpeg process. Tests can supply
 * a fake runner to avoid spawning external processes.
 */
interface FfmpegCommandRunner {
    /**
     * Runs ffmpeg with the provided arguments.
     *
     * @param args ffmpeg command-line arguments (not including the executable).
     * @return The result of the invocation, including exit code and outputs.
     */
    fun run(args: List<String>): FfmpegResult
}

/**
 * Result of an ffmpeg invocation.
 *
 * @property exitCode The process exit code.
 * @property output The bytes written to stdout (or the output file when redirected).
 * @property errorOutput The text written to stderr.
 */
data class FfmpegResult(
    val exitCode: Int,
    val output: ByteArray,
    val errorOutput: String,
)

/**
 * Default [FfmpegCommandRunner] that spawns a local ffmpeg process.
 */
class ProcessBuilderFfmpegCommandRunner : FfmpegCommandRunner {
    override fun run(args: List<String>): FfmpegResult {
        val command = listOf("ffmpeg") + args
        val process = ProcessBuilder(command).start()

        val output = process.inputStream.use { it.readAllBytes() }
        val errorOutput = process.errorStream.use { it.bufferedReader().readText() }
        val exitCode = process.waitFor()

        return FfmpegResult(exitCode, output, errorOutput)
    }
}
