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

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.io.PrintStream
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.text.DefaultCaret

class ConsolePanel : JPanel() {
    private val textArea = JTextArea()
    private val scrollPane = JScrollPane()
    private val caret = textArea.caret as DefaultCaret

    init {
        textArea.lineWrap = true
        textArea.wrapStyleWord = true
        textArea.isEditable = false
        textArea.font = Font(Font.MONOSPACED, Font.PLAIN, 13)
        textArea.background = Color(18, 20, 27)
        textArea.foreground = Color(221, 226, 237)
        textArea.caretColor = Color(140, 153, 255)
        textArea.border = BorderFactory.createEmptyBorder(10, 12, 10, 12)

        caret.updatePolicy = DefaultCaret.ALWAYS_UPDATE
        val con = PrintStream(TextAreaOutputStream(textArea))
        System.setOut(con)
        System.setErr(con)

        scrollPane.setViewportView(textArea)
        scrollPane.border = BorderFactory.createEmptyBorder()

        super.setLayout(BorderLayout())
        super.add(scrollPane, BorderLayout.CENTER)
        super.setPreferredSize(Dimension(400, 300))
    }

    fun clear() {
        textArea.text = ""
    }

    fun setAutoScroll(enabled: Boolean) {
        caret.updatePolicy = if (enabled) DefaultCaret.ALWAYS_UPDATE else DefaultCaret.NEVER_UPDATE
    }
}
