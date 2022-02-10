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
    fun convert(Ymodifier: Double): Boolean {
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
                config.set("driverseat.Offset.y", config.getDouble("driverseat.Offset.y") - Ymodifier)

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

    fun restore() {
        val start = Instant.now()
        if (!folder.exists()) {
            logger.error("Folder does not exist")
            return
        }

        if (!folder.isDirectory) {
            logger.error("Folder is not a directory")
            return
        }

        val files = folder.listFiles() ?: return
        var restored = 0

        for (file in files) {
            if (file.isDirectory) continue
            if (!file.name.endsWith(".bak")) continue

            val substitute = File(file.absolutePath.replace(".bak", ""))
            if (substitute.exists()) substitute.delete()
            file.renameTo(substitute)

            restored++
        }

        val end = Instant.now()
        logger.info("Restored $restored files in ${Duration.between(start, end).toMillis()}ms")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage: java -jar qav-converter.jar <folder> [Y modifier]")
                exitProcess(1)
            }

            val converter = QAVConverter(File(args[1]))

            if (args[0].equals("convert", ignoreCase = true)) {
                if (args.size > 2) {
                    converter.convert(args[2].toDoubleOrNull() ?: 1.9)
                } else {
                    converter.convert(1.9)
                }
            } else if (args[0].equals("restore", ignoreCase = true)) {
                converter.restore()
            } else {
                System.err.println("Usage: java -jar qav-converter.jar <convert/restore> <folder> [Y modifier]")
                exitProcess(1)
            }

        }
    }
}