//////////////////////////////////////////////////////////////////////////////////////////
//  Copyright 2023 Cosgy Dev                                                             /
//                                                                                       /
//     Licensed under the Apache License, Version 2.0 (the "License");                   /
//     you may not use this file except in compliance with the License.                  /
//     You may obtain a copy of the License at                                           /
//                                                                                       /
//        http://www.apache.org/licenses/LICENSE-2.0                                     /
//                                                                                       /
//     Unless required by applicable law or agreed to in writing, software               /
//     distributed under the License is distributed on an "AS IS" BASIS,                 /
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.          /
//     See the License for the specific language governing permissions and               /
//     limitations under the License.                                                    /
//////////////////////////////////////////////////////////////////////////////////////////
package dev.cosgy.textToSpeak.gui

import com.sun.management.OperatingSystemMXBean
import dev.cosgy.textToSpeak.Bot
import java.awt.Font
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.lang.management.*
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.swing.*
import kotlin.math.ln
import kotlin.math.pow
import kotlin.system.exitProcess


/**
 * @author Kosugi_kun
 */
class GUI(private val bot: Bot) : JFrame() {
    private val console: ConsolePanel = ConsolePanel()

    private var runtimeMx: RuntimeMXBean? = null
    private var compilationMx: CompilationMXBean? = null
    private var sunThreadMx: ThreadMXBean? = null
    private var memoryMx: MemoryMXBean? = null
    private var classLoadingMx: ClassLoadingMXBean? = null
    private var sunOsMx: OperatingSystemMXBean? = null
    private var garbageCollectors: Collection<GarbageCollectorMXBean>? = null

    fun init() {

        runtimeMx = ManagementFactory.getRuntimeMXBean()
        compilationMx = ManagementFactory.getCompilationMXBean()
        sunThreadMx = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
        memoryMx = ManagementFactory.getMemoryMXBean()
        classLoadingMx = ManagementFactory.getClassLoadingMXBean()
        sunOsMx = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans().filterIsInstance<GarbageCollectorMXBean>()


        defaultCloseOperation = EXIT_ON_CLOSE
        title = bot.getLang().getString("appName")
        val tabs = JTabbedPane()
        tabs.add("コンソール", console)

        val botInfoPanel = JPanel()

        botInfoPanel.layout = BoxLayout(botInfoPanel, BoxLayout.Y_AXIS)
        val scrollPane =
            JScrollPane(botInfoPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

        // Add bot info label to bot info panel
        val botInfoLabel = JLabel()

        val osName = sunOsMx?.name
        val osVersion = sunOsMx?.version
        val osArch = sunOsMx?.arch
        val processors = sunOsMx?.availableProcessors

        //val vmName = runtimeMx?.name
        //val vmVersion = runtimeMx?.vmVersion
        //val vmVendor = runtimeMx?.vmVendor
        val vmArguments = runtimeMx?.inputArguments?.joinToString(" ")

        //botInfoPanel.add(systemInfoLabel)
        botInfoLabel.font = Font("monospaced", Font.PLAIN, 12)
        botInfoPanel.add(botInfoLabel)

        tabs.add("システム情報", scrollPane)

        contentPane.add(tabs)
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent) {
                /* unused */
            }

            override fun windowClosing(e: WindowEvent) {
                try {
                    bot.shutdown()
                } catch (ex: Exception) {
                    exitProcess(0)
                }
            }

            override fun windowClosed(e: WindowEvent) { /* unused */
            }

            override fun windowIconified(e: WindowEvent) { /* unused */
            }

            override fun windowDeiconified(e: WindowEvent) { /* unused */
            }

            override fun windowActivated(e: WindowEvent) { /* unused */
            }

            override fun windowDeactivated(e: WindowEvent) { /* unused */
            }
        })

        // ボット情報を定期的に更新する
        Timer(1000) {
            val uptime = Duration.ofMillis(runtimeMx!!.uptime).toMinutes()

            // Convert start time to LocalDateTime
            val start = LocalDateTime.ofInstant(Instant.ofEpochMilli(runtimeMx!!.startTime), ZoneId.systemDefault())
            val vmName = runtimeMx!!.name
            val vmVendor = runtimeMx!!.vmVendor
            val vmVersion = runtimeMx!!.vmVersion
            //val gcCount = garbageCollectors!!.sumOf { it.collectionCount }

            val cpuUsage = sunOsMx!!.processCpuLoad * 100
            val systemCpuUsage = sunOsMx!!.cpuLoad * 100
            val loadAverage = sunOsMx!!.systemLoadAverage

            val physicalMemory = sunOsMx!!.totalMemorySize / 1024 / 1024
            val freePhysicalMemory = sunOsMx!!.freeMemorySize / 1024 / 1024
            val usedPhysicalMemory = physicalMemory - freePhysicalMemory

            val swapSpace = sunOsMx!!.totalSwapSpaceSize / 1024 / 1024
            val freeSwapSpace = sunOsMx!!.freeSwapSpaceSize / 1024 / 1024

            val heapMemory = memoryMx!!.heapMemoryUsage
            val heapInit = heapMemory.init / 1024 / 1024
            val heapMax = heapMemory.max / 1024 / 1024
            val heapCommitted = heapMemory.committed / 1024 / 1024
            val heapUsed = heapMemory.used / 1024 / 1024

            val nonHeapMemory = memoryMx!!.nonHeapMemoryUsage
            val nonHeapInit = nonHeapMemory.init / 1024 / 1024
            val nonHeapMax = nonHeapMemory.max / 1024 / 1024
            val nonHeapCommitted = nonHeapMemory.committed / 1024 / 1024
            val nonHeapUsed = nonHeapMemory.used / 1024 / 1024

            val threadCount = sunThreadMx!!.threadCount
            val daemonThreadCount = sunThreadMx!!.daemonThreadCount
            val peakThreadCount = sunThreadMx!!.peakThreadCount
            val totalStartedThreadCount = sunThreadMx!!.totalStartedThreadCount

            val loadedClassCount = classLoadingMx!!.loadedClassCount
            val totalLoadedClassCount = classLoadingMx!!.totalLoadedClassCount
            val unloadedClassCount = classLoadingMx!!.unloadedClassCount

            val gcStats = garbageCollectors!!.joinToString("\n") { gc ->
                val name = gc.name
                val count = gc.collectionCount
                val time = gc.collectionTime / 1000
                "$name: Count=$count, Time=$time sec"
            }

            val botInfoText = """
                :: System<br>
                    CPU usage: %.2f, Load average (last minute): %.5f<br>
                    Physical memory: %s, Free: %s, Used: %s<br>
                    Swap space: %s, Free: %s<br><br>
                :: VM Process<br>
                    Uptime: %s minutes, Started: %s<br>
                    CPU usage: %.2f, CPU time: %s ms, JIT compile time: %s ms<br><br>
                :: Heap<br>
                    Current: %s, Committed: %s, Init: %s, Max: %s<br><br>
                :: Non-Heap Memory<br>
                    Current: %d MB, Committed: %d MB, Init: %d MB, Max: %d MB<br><br>
                :: Thread Usage<br>
                    Live: %d, Peak: %d, Daemon: %d, Total Started: %d<br><br>
                :: Class Loading<br>
                    Current loaded: %d, Loaded (total): %d, Unloaded (total): %d<br><br>
                :: Garbage Collector<br>
                    $gcStats
            """.trimIndent()

            val sysInfoText = """
            :: System Information<br>
                Operating system: %s, Version: %s, Arch: %s<br>
                Number of processors: %d, Physical memory: %s, Virtual memory: %s<br><br>
            :: VM Information<br>
                VM: %s, Version: %s, Vendor: %s, JIT compiler: %s<br>
                Arguments: %s<br><br><br>
            """.trimIndent()

            val systemInfo = sysInfoText.format(
                osName, osVersion, osArch, processors,
                prettyBytes(sunOsMx!!.totalMemorySize), prettyBytes(sunOsMx!!.committedVirtualMemorySize),
                vmName, vmVersion, vmVendor, compilationMx?.name,
                vmArguments
            )

            botInfoLabel.text = "<html>$systemInfo" + botInfoText.format(
                cpuUsage,
                loadAverage,
                physicalMemory,
                freePhysicalMemory,
                usedPhysicalMemory,
                swapSpace,
                freeSwapSpace,
                uptime,
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                systemCpuUsage,
                sunOsMx!!.processCpuTime / 1000000,
                compilationMx!!.totalCompilationTime,
                heapUsed,
                heapCommitted,
                heapInit,
                heapMax,
                nonHeapUsed,
                nonHeapCommitted,
                nonHeapInit,
                nonHeapMax,
                threadCount,
                peakThreadCount,
                daemonThreadCount,
                totalStartedThreadCount,
                loadedClassCount,
                totalLoadedClassCount,
                unloadedClassCount
            ) + "</html>"

        }.start()

    }

    private fun prettyBytes(bytes: Long): String {
        return prettyBytes(bytes, true)
    }

    private fun prettyBytes(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

}