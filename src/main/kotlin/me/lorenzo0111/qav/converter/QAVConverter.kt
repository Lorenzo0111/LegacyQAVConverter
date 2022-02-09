package me.lorenzo0111.qav.converter

import com.google.common.io.Files
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.jvm.JvmStatic
import kotlin.system.exitProcess

class QAVConverter(private val folder: File) {
    private val logger: Logger = LoggerFactory.getLogger(QAVConverter::class.java)
    private val provider: ConfigurationProvider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)

    @Suppress("UnstableApiUsage")
    fun convert(newY: Double): Boolean {
        val start = Instant.now()
        if (!folder.exists()) {
            logger.error("Folder does not exist")
            return false
        }

        if (!folder.isDirectory) {
            logger.error("Folder is not a directory")
            return false
        }

        val files = folder.listFiles() ?: return false

        var converted = 0

        for (file in files) {
            if (file.isDirectory) continue
            if (!file.name.endsWith(".yml") && !file.name.endsWith(".yaml")) continue
            logger.info("Converting " + file.name)

            val backup = File(file.absolutePath + ".bak")

            try {
                if (!backup.exists())
                    Files.copy(file, backup)

                val config = provider.load(file)
                config.set("driverseat.Offset.y", newY)

                provider.save(config, file)
                converted++
            } catch (e: IOException) {
                logger.warn("An error has occurred while loading the file", e)
                logger.warn("Restoring backup")
                file.delete()
                Files.copy(backup, file)
            }

            logger.info("Successfully converted $converted files")
        }

        val end = Instant.now()
        logger.info("Conversion took ${Duration.between(start, end).toMillis()}ms")
        return true
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isEmpty()) {
                System.err.println("Usage: java -jar qav-converter.jar <folder> [newY]")
                exitProcess(1)
            }

            val converter = QAVConverter(File(args[0]))
            if (args.size > 1) {
                converter.convert(args[1].toDoubleOrNull() ?: -1.0)
            } else {
                converter.convert(-1.0)
            }
        }
    }
}