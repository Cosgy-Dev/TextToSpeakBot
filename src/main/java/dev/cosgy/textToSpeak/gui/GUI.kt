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

import dev.cosgy.textToSpeak.Bot
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.JFrame
import javax.swing.JTabbedPane
import kotlin.system.exitProcess

/**
 * @author Kosugi_kun
 */
class GUI(private val bot: Bot) : JFrame() {
    private val console: ConsolePanel = ConsolePanel()

    fun init() {
        defaultCloseOperation = EXIT_ON_CLOSE
        title = bot.GetLang().getString("appName")
        val tabs = JTabbedPane()
        tabs.add("コンソール", console)
        contentPane.add(tabs)
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent) { /* unused */
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
    }
}