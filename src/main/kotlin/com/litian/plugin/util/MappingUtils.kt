package com.litian.plugin.util

import java.io.File

object MappingUtils {

    private lateinit var mappingFile: File

    private val classNameSet = hashSetOf<String>()

    fun createMappingFile(parentDir: String?) {
        if (parentDir.isNullOrBlank()) {
            return
        }
        mappingFile = File("$parentDir/RenameMapping.txt")

        val createNewFile = mappingFile.createNewFile()
        if (!createNewFile) {
            mappingFile.readLines().forEach {
                classNameSet.add(it)
            }
        }
    }

    fun classNameExist(className: String): Boolean {
        return classNameSet.contains(className)
    }

    //写了一个最简单的方式，将类名存到一个文件中，后续可以把类名和新生成的类名都存起来
    fun saveClassName(className: String) {
        if (!this::mappingFile.isInitialized) {
            return
        }
        classNameSet.add(className)
        mappingFile.appendText("$className\n")
    }
}