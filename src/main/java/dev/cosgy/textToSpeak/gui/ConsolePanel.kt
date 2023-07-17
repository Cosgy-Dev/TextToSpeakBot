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

import java.awt.Dimension
import java.awt.GridLayout
import java.io.PrintStream
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

class ConsolePanel : JPanel() {
    init {
        val text = JTextArea()
        text.lineWrap = true
        text.wrapStyleWord = true
        text.isEditable = false
        val con = PrintStream(TextAreaOutputStream(text))
        System.setOut(con)
        System.setErr(con)
        val pane = JScrollPane()
        pane.setViewportView(text)
        super.setLayout(GridLayout(1, 1))
        super.add(pane)
        super.setPreferredSize(Dimension(400, 300))
    }
}