package com.litian.plugin.util

import io.netty.util.internal.ThreadLocalRandom
import java.io.InputStream
import java.util.HashSet

object NameUtil {

    private val methodNameSet = hashSetOf<String>()
    private val varNameSet = hashSetOf<String>()
    private val random = ThreadLocalRandom.current()

    init {
        fun loadNames(fileName: String, nameSet: MutableSet<String>) {
            val resourceStream: InputStream? = this::class.java.classLoader.getResourceAsStream(fileName)
            resourceStream?.bufferedReader()?.useLines { lines ->
                lines.forEach { nameSet.add(it) }
            }
        }

        loadNames("method.name", methodNameSet)
        loadNames("var.name", varNameSet)
    }


    fun generateVariableName(): String {
        return generateName(varNameSet)
    }

    fun generateMethodName(): String {
        return generateName(methodNameSet)
    }

    private fun generateName(set: HashSet<String>): String {
        var name = set.random()
        for (i in 0 until random.nextInt(2)) {
            name += set.random().capitalizeFirstLetter()
        }
        return name
    }


    private fun String.capitalizeFirstLetter(): String {
        if (this.isEmpty()) return this
        return this[0].uppercaseChar() + this.substring(1)
    }
}