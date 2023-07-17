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
package dev.cosgy.textToSpeak.queue

class FairQueue<T : Queueable?> {
    private val list: MutableList<T> = ArrayList()
    private val set: MutableSet<Long> = HashSet()
    fun add(item: T): Int {
        var lastIndex: Int = list.size - 1
        while (lastIndex > -1) {
            if (list[lastIndex]!!.identifier == item!!.identifier) break
            lastIndex--
        }
        lastIndex++
        set.clear()
        while (lastIndex < list.size) {
            if (set.contains(list[lastIndex]!!.identifier)) break
            set.add(list[lastIndex]!!.identifier)
            lastIndex++
        }
        list.add(lastIndex, item)
        return lastIndex
    }

    fun addAt(index: Int, item: T) {
        if (index >= list.size) list.add(item) else list.add(index, item)
    }

    fun size(): Int {
        return list.size
    }

    fun pull(): T {
        return list.removeAt(0)
    }

    val isEmpty: Boolean
        get() = list.isEmpty()

    fun getList(): List<T> {
        return list
    }

    operator fun get(index: Int): T {
        return list[index]
    }

    fun remove(index: Int): T {
        return list.removeAt(index)
    }

    fun removeAll(identifier: Long): Int {
        var count = 0
        for (i in list.indices.reversed()) {
            if (list[i]!!.identifier == identifier) {
                list.removeAt(i)
                count++
            }
        }
        return count
    }

    fun clear() {
        list.clear()
    }
}