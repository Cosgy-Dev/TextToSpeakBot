//////////////////////////////////////////////////////////////////////////////////////////
//     Copyright 2023 Cosgy Dev                                                             /
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
package dev.cosgy.textToSpeak.entities

import org.slf4j.LoggerFactory
import java.util.*
import javax.swing.JOptionPane

class Prompt @JvmOverloads constructor(
    private val title: String,
    noguiMessage: String? = null,
    var isNoGUI: Boolean = "true".equals(
        System.getProperty("nogui"),
        ignoreCase = true
    )
) {
    private val noguiMessage: String
    private var scanner: Scanner? = null

    init {
        this.noguiMessage = noguiMessage
            ?: "noguiモードに切り替えます。 -nogui=trueフラグを含めることで、手動でnoguiモードで起動できます。"
    }

    fun alert(level: Level?, context: String?, message: String) {
        if (isNoGUI) {
            val log = LoggerFactory.getLogger(context)
            when (level) {
                Level.WARNING -> log.warn(message)
                Level.ERROR -> log.error(message)
                Level.INFO -> log.info(message)
                else -> log.info(message)
            }
        } else {
            try {
                var option = 0
                when (level) {
                    Level.INFO -> option = JOptionPane.INFORMATION_MESSAGE
                    Level.WARNING -> option = JOptionPane.WARNING_MESSAGE
                    Level.ERROR -> {}
                    else -> option = JOptionPane.PLAIN_MESSAGE
                }
                JOptionPane.showMessageDialog(null, "<html><body><p style='width: 400px;'>$message", title, option)
            } catch (e: Exception) {
                isNoGUI = true
                alert(Level.WARNING, context, noguiMessage)
                alert(level, context, message)
            }
        }
    }

    fun prompt(content: String?): String? {
        return if (isNoGUI) {
            if (scanner == null) scanner = Scanner(System.`in`)
            try {
                println(content)
                if (scanner!!.hasNextLine()) scanner!!.nextLine() else null
            } catch (e: Exception) {
                alert(Level.ERROR, title, "コマンドラインから入力を読み込めません。")
                e.printStackTrace()
                null
            }
        } else {
            try {
                JOptionPane.showInputDialog(null, content, title, JOptionPane.QUESTION_MESSAGE)
            } catch (e: Exception) {
                isNoGUI = true
                alert(Level.WARNING, title, noguiMessage)
                prompt(content)
            }
        }
    }

    enum class Level {
        INFO, WARNING, ERROR
    }
}