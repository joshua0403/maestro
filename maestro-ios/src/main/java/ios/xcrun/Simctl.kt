package ios.xcrun

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ios.xcrun.CommandLineUtils.runCommand
import maestro.utils.MaestroTimer
import java.io.File

object Simctl {
    data class SimctlError(override val message: String): Throwable(message)

    fun list(): SimctlList {
        val command = listOf("xcrun", "simctl", "list", "-j")

        val process = ProcessBuilder(command).start()
        val json = String(process.inputStream.readBytes())

        return jacksonObjectMapper().readValue(json)
    }

    fun awaitLaunch(deviceId: String) {
        MaestroTimer.withTimeout(30000) {
            if (list()
                    .devices
                    .values
                    .flatten()
                    .find { it.udid == deviceId }
                    ?.state == "Booted"
            ) true else null
        } ?: throw SimctlError("Device $deviceId did not boot in time")
    }

    fun awaitShutdown(deviceId: String) {
        MaestroTimer.withTimeout(30000) {
            if (list()
                    .devices
                    .values
                    .flatten()
                    .find { it.udid == deviceId }
                    ?.state != "Booted"
            ) true else null
        } ?: throw SimctlError("Device $deviceId did not boot in time")
    }

    fun launchSimulator(deviceId: String) {
        runCommand("xcrun simctl boot $deviceId")

        var exceptionToThrow: Exception? = null

        // Up to 10 iterations => max wait time of 1 second
        repeat(10) {
            try {
                runCommand("open -a /Applications/Xcode.app/Contents/Developer/Applications/Simulator.app --args -CurrentDeviceUDID $deviceId")
                return
            } catch (e: Exception) {
                exceptionToThrow = e
                Thread.sleep(100)
            }
        }

        exceptionToThrow?.let { throw it }
    }

    fun reboot(
        deviceId: String,
    ) {
        runCommand(
            listOf(
                "xcrun",
                "simctl",
                "shutdown",
                deviceId
            ),
            waitForCompletion = true
        )
        awaitShutdown(deviceId)

        runCommand(
            listOf(
                "xcrun",
                "simctl",
                "boot",
                deviceId
            ),
            waitForCompletion = true
        )
        awaitLaunch(deviceId)
    }

    fun addTrustedCertificate(
        deviceId: String,
        certificate: File,
    ) {
        runCommand(
            listOf(
                "xcrun",
                "simctl",
                "keychain",
                deviceId,
                "add-root-cert",
                certificate.absolutePath,
            ),
            waitForCompletion = true
        )

        reboot(deviceId)
    }

}