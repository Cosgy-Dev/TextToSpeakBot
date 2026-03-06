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
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.lang.management.ClassLoadingMXBean
import java.lang.management.CompilationMXBean
import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.RuntimeMXBean
import java.lang.management.ThreadMXBean
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JEditorPane
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTabbedPane
import javax.swing.JToggleButton
import javax.swing.Timer
import javax.swing.UIManager
import kotlin.math.ln
import kotlin.math.pow
import kotlin.system.exitProcess

/**
 * @author Kosugi_kun
 */
class GUI(private val bot: Bot) : JFrame() {
    private val console = ConsolePanel()

    private var runtimeMx: RuntimeMXBean? = null
    private var compilationMx: CompilationMXBean? = null
    private var sunThreadMx: ThreadMXBean? = null
    private var memoryMx: MemoryMXBean? = null
    private var classLoadingMx: ClassLoadingMXBean? = null
    private var sunOsMx: OperatingSystemMXBean? = null
    private var garbageCollectors: Collection<GarbageCollectorMXBean>? = null

    private lateinit var statusValueLabel: JLabel
    private lateinit var guildsValueLabel: JLabel
    private lateinit var pingValueLabel: JLabel
    private lateinit var uptimeValueLabel: JLabel
    private lateinit var cpuProgress: JProgressBar
    private lateinit var memoryProgress: JProgressBar
    private lateinit var runtimeInfoPane: JEditorPane
    private lateinit var jvmInfoPane: JEditorPane

    private var infoRefreshTimer: Timer? = null

    fun init() {
        installLookAndFeel()

        runtimeMx = ManagementFactory.getRuntimeMXBean()
        compilationMx = ManagementFactory.getCompilationMXBean()
        sunThreadMx = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
        memoryMx = ManagementFactory.getMemoryMXBean()
        classLoadingMx = ManagementFactory.getClassLoadingMXBean()
        sunOsMx = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans().filterIsInstance<GarbageCollectorMXBean>()

        title = "TextToSpeak Bot"
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        contentPane.layout = BorderLayout()
        contentPane.background = Color(246, 247, 251)

        val shell = JPanel(BorderLayout(0, 12))
        shell.border = BorderFactory.createEmptyBorder(14, 14, 14, 14)
        shell.background = Color(246, 247, 251)

        shell.add(createTopSection(), BorderLayout.NORTH)
        shell.add(createBody(), BorderLayout.CENTER)

        contentPane.add(shell, BorderLayout.CENTER)

        minimumSize = Dimension(960, 640)
        setSize(1120, 760)
        setLocationRelativeTo(null)
        isVisible = true

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                infoRefreshTimer?.stop()
                try {
                    bot.shutdown()
                } catch (ex: Exception) {
                    exitProcess(0)
                }
            }
        })

        infoRefreshTimer = Timer(1000) {
            refreshDashboard()
        }
        infoRefreshTimer?.start()
        refreshDashboard()
    }

    private fun createTopSection(): JPanel {
        val panel = JPanel(BorderLayout(12, 0))
        panel.background = Color(246, 247, 251)

        val titleBlock = JPanel()
        titleBlock.layout = BoxLayout(titleBlock, BoxLayout.Y_AXIS)
        titleBlock.background = Color(246, 247, 251)

        val titleLabel = JLabel("TextToSpeak Bot")
        titleLabel.font = Font(Font.SANS_SERIF, Font.BOLD, 26)

        val subtitleLabel = JLabel("リアルタイムモニター / コンソール")
        subtitleLabel.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
        subtitleLabel.foreground = Color(84, 95, 113)

        titleBlock.add(titleLabel)
        titleBlock.add(Box.createVerticalStrut(4))
        titleBlock.add(subtitleLabel)

        val actionPanel = JPanel()
        actionPanel.background = Color(246, 247, 251)

        val autoScroll = JToggleButton("自動スクロール")
        autoScroll.isSelected = true
        autoScroll.addActionListener {
            console.setAutoScroll(autoScroll.isSelected)
        }

        val clearConsole = JButton("コンソールをクリア")
        clearConsole.addActionListener {
            console.clear()
        }

        val shutdown = JButton("シャットダウン")
        shutdown.background = Color(214, 76, 76)
        shutdown.foreground = Color.WHITE
        shutdown.addActionListener {
            dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        }

        actionPanel.add(autoScroll)
        actionPanel.add(clearConsole)
        actionPanel.add(shutdown)

        panel.add(titleBlock, BorderLayout.WEST)
        panel.add(actionPanel, BorderLayout.EAST)
        return panel
    }

    private fun createBody(): JPanel {
        val panel = JPanel(BorderLayout(0, 12))
        panel.background = Color(246, 247, 251)

        panel.add(createSummaryCards(), BorderLayout.NORTH)

        val tabs = JTabbedPane()
        tabs.add("ダッシュボード", createDashboardPanel())
        tabs.add("コンソール", console)

        panel.add(tabs, BorderLayout.CENTER)
        return panel
    }

    private fun createSummaryCards(): JPanel {
        val panel = JPanel(GridLayout(1, 4, 12, 0))
        panel.background = Color(246, 247, 251)

        statusValueLabel = JLabel("-", JLabel.CENTER)
        guildsValueLabel = JLabel("-", JLabel.CENTER)
        pingValueLabel = JLabel("-", JLabel.CENTER)
        uptimeValueLabel = JLabel("-", JLabel.CENTER)

        panel.add(createMetricCard("接続状態", statusValueLabel))
        panel.add(createMetricCard("サーバー数", guildsValueLabel))
        panel.add(createMetricCard("Gateway Ping", pingValueLabel))
        panel.add(createMetricCard("稼働時間", uptimeValueLabel))

        return panel
    }

    private fun createMetricCard(title: String, value: JLabel): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.background = Color.WHITE
        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color(224, 229, 238), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        )

        val titleLabel = JLabel(title, JLabel.CENTER)
        titleLabel.alignmentX = CENTER_ALIGNMENT
        titleLabel.font = Font(Font.SANS_SERIF, Font.PLAIN, 12)
        titleLabel.foreground = Color(84, 95, 113)

        value.alignmentX = CENTER_ALIGNMENT
        value.font = Font(Font.SANS_SERIF, Font.BOLD, 18)
        value.foreground = Color(33, 43, 59)

        panel.add(titleLabel)
        panel.add(Box.createVerticalStrut(6))
        panel.add(value)

        return panel
    }

    private fun createDashboardPanel(): JPanel {
        val panel = JPanel(BorderLayout(0, 12))
        panel.background = Color(246, 247, 251)

        val meterPanel = JPanel(GridLayout(2, 1, 0, 8))
        meterPanel.background = Color(246, 247, 251)

        cpuProgress = JProgressBar(0, 100)
        cpuProgress.isStringPainted = true
        cpuProgress.border = BorderFactory.createTitledBorder("CPU使用率")

        memoryProgress = JProgressBar(0, 100)
        memoryProgress.isStringPainted = true
        memoryProgress.border = BorderFactory.createTitledBorder("ヒープ使用率")

        meterPanel.add(cpuProgress)
        meterPanel.add(memoryProgress)

        runtimeInfoPane = createInfoPane()
        jvmInfoPane = createInfoPane()

        val splitPane = JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            JScrollPane(runtimeInfoPane),
            JScrollPane(jvmInfoPane)
        )
        splitPane.resizeWeight = 0.52
        splitPane.border = BorderFactory.createEmptyBorder()

        panel.add(meterPanel, BorderLayout.NORTH)
        panel.add(splitPane, BorderLayout.CENTER)

        return panel
    }

    private fun createInfoPane(): JEditorPane {
        val pane = JEditorPane()
        pane.contentType = "text/html"
        pane.isEditable = false
        pane.background = Color.WHITE
        pane.border = BorderFactory.createEmptyBorder(8, 10, 8, 10)
        return pane
    }

    private fun refreshDashboard() {
        val runtime = runtimeMx ?: return
        val os = sunOsMx ?: return
        val memory = memoryMx ?: return
        val threads = sunThreadMx ?: return
        val classes = classLoadingMx ?: return

        val jda = bot.jda
        statusValueLabel.text = jda?.status?.name ?: "起動中"
        guildsValueLabel.text = (jda?.guilds?.size ?: 0).toString()
        pingValueLabel.text = if (jda == null) "-" else "${jda.gatewayPing}ms"

        val uptimeMin = Duration.ofMillis(runtime.uptime).toMinutes()
        uptimeValueLabel.text = "${uptimeMin}分"

        val processCpuUsage = normalizePercent(os.processCpuLoad * 100)
        val heapUsage = memory.heapMemoryUsage
        val heapPercent = if (heapUsage.max > 0) (heapUsage.used * 100 / heapUsage.max).toInt() else 0

        cpuProgress.value = processCpuUsage.toInt()
        cpuProgress.string = String.format("%.1f%%", processCpuUsage)

        memoryProgress.value = heapPercent
        memoryProgress.string = "${prettyBytes(heapUsage.used)} / ${prettyBytes(heapUsage.max)}"

        val start = LocalDateTime.ofInstant(Instant.ofEpochMilli(runtime.startTime), ZoneId.systemDefault())

        val gcStats = garbageCollectors
            ?.joinToString("<br>") { gc -> "${gc.name}: ${gc.collectionCount}回 / ${gc.collectionTime}ms" }
            ?: "取得不可"

        val runtimeHtml = """
            <html>
            <body style='font-family:sans-serif; font-size:12px; color:#243041;'>
            <h3 style='margin:0 0 10px 0;'>システム</h3>
            <b>OS:</b> ${os.name} (${os.version}, ${os.arch})<br>
            <b>CPU論理コア:</b> ${os.availableProcessors}<br>
            <b>システムCPU:</b> ${formatPercent(os.cpuLoad * 100)}<br>
            <b>ロードアベレージ:</b> ${String.format("%.3f", os.systemLoadAverage)}<br><br>

            <h3 style='margin:0 0 10px 0;'>メモリ</h3>
            <b>物理メモリ:</b> ${prettyBytes(os.totalMemorySize)}<br>
            <b>使用中(物理):</b> ${prettyBytes(os.totalMemorySize - os.freeMemorySize)}<br>
            <b>空き(物理):</b> ${prettyBytes(os.freeMemorySize)}<br>
            <b>スワップ:</b> ${prettyBytes(os.totalSwapSpaceSize - os.freeSwapSpaceSize)} / ${prettyBytes(os.totalSwapSpaceSize)}<br>

            </body>
            </html>
        """.trimIndent()

        val jvmHtml = """
            <html>
            <body style='font-family:sans-serif; font-size:12px; color:#243041;'>
            <h3 style='margin:0 0 10px 0;'>JVM / Bot</h3>
            <b>起動時刻:</b> ${start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}<br>
            <b>稼働時間:</b> ${uptimeMin}分<br>
            <b>JVM:</b> ${runtime.name}<br>
            <b>ベンダー:</b> ${runtime.vmVendor} (${runtime.vmVersion})<br>
            <b>JIT:</b> ${compilationMx?.name ?: "N/A"}<br>
            <b>JIT時間:</b> ${compilationMx?.totalCompilationTime ?: -1}ms<br><br>

            <h3 style='margin:0 0 10px 0;'>スレッド / クラス</h3>
            <b>スレッド:</b> ${threads.threadCount} (Daemon: ${threads.daemonThreadCount}, Peak: ${threads.peakThreadCount})<br>
            <b>クラスロード:</b> ${classes.loadedClassCount} (累計: ${classes.totalLoadedClassCount}, Unload: ${classes.unloadedClassCount})<br><br>

            <h3 style='margin:0 0 10px 0;'>GC統計</h3>
            $gcStats
            </body>
            </html>
        """.trimIndent()

        runtimeInfoPane.text = runtimeHtml
        runtimeInfoPane.caretPosition = 0

        jvmInfoPane.text = jvmHtml
        jvmInfoPane.caretPosition = 0
    }

    private fun installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (_: Exception) {
            // fallback to default L&F
        }
    }

    private fun normalizePercent(value: Double): Double {
        if (value.isNaN() || value < 0.0) return 0.0
        return value.coerceIn(0.0, 100.0)
    }

    private fun formatPercent(value: Double): String {
        val normalized = normalizePercent(value)
        return String.format("%.2f%%", normalized)
    }

    private fun prettyBytes(bytes: Long): String {
        return prettyBytes(bytes, true)
    }

    private fun prettyBytes(bytes: Long, si: Boolean): String {
        if (bytes < 0) return "N/A"
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }
}
