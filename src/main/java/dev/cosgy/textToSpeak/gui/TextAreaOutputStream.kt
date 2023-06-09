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

import java.awt.EventQueue
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*
import javax.swing.JTextArea

/**
 * @author Kosugi_kun
 */
class TextAreaOutputStream @JvmOverloads constructor(txtara: JTextArea, maxlin: Int = 1000) : OutputStream() {
    private val oneByte: ByteArray
    private var appender: Appender?

    init {
        require(maxlin >= 1) { "TextAreaOutputStreamの最大行数は正数でなければなりません(value=$maxlin)" }
        oneByte = ByteArray(1)
        appender = Appender(txtara, maxlin)
    }

    @Synchronized
    fun clear() {
        if (appender != null) {
            appender!!.clear()
        }
    }

    @Synchronized
    override fun close() {
        appender = null
    }

    @Synchronized
    override fun flush() {
        /* empty */
    }

    @Synchronized
    override fun write(`val`: Int) {
        oneByte[0] = `val`.toByte()
        write(oneByte, 0, 1)
    }

    @Synchronized
    override fun write(ba: ByteArray) {
        write(ba, 0, ba.size)
    }

    @Synchronized
    override fun write(ba: ByteArray, str: Int, len: Int) {
        if (appender != null) {
            appender!!.append(bytesToString(ba, str, len))
        }
    }

    internal class Appender(private val textArea: JTextArea, private val maxLines: Int) : Runnable {
        private val lengths: LinkedList<Int> = LinkedList()
        private val values: MutableList<String>
        private var curLength = 0
        private var clear = false
        private var queue = true

        init {
            values = ArrayList()
        }

        @Synchronized
        fun append(`val`: String) {
            values.add(`val`)
            if (queue) {
                queue = false
                EventQueue.invokeLater(this)
            }
        }

        @Synchronized
        fun clear() {
            clear = true
            curLength = 0
            lengths.clear()
            values.clear()
            if (queue) {
                queue = false
                EventQueue.invokeLater(this)
            }
        }

        @Synchronized
        override fun run() {
            if (clear) {
                textArea.text = ""
            }
            values.stream()
                .peek { `val`: String -> curLength += `val`.length }
                .peek { `val`: String ->
                    if (`val`.endsWith(EOL1) || `val`.endsWith(EOL2)) {
                        if (lengths.size >= maxLines) {
                            textArea.replaceRange("", 0, lengths.removeFirst())
                        }
                        lengths.addLast(curLength)
                        curLength = 0
                    }
                }.forEach { str: String? -> textArea.append(str) }
            values.clear()
            clear = false
            queue = true
        }

        companion object {
            private const val EOL1 = "\n"
            private val EOL2 = System.getProperty("line.separator", EOL1)
        }
    }

    companion object {
        private fun bytesToString(ba: ByteArray, str: Int, len: Int): String {
            return try {
                String(ba, str, len, Charset.defaultCharset())
            } catch (thr: UnsupportedEncodingException) {
                String(ba, str, len)
            }
        }
    }
}