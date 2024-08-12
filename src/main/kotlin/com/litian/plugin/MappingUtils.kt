package com.litian.plugin

import java.io.File

object MappingUtils {

    private lateinit var mappingFile: File

    fun createMappingFile(parentDir: String?) {
        if (parentDir.isNullOrBlank()) {
            return
        }
        mappingFile = File("$parentDir/RenameMapping.txt")
        if (!mappingFile.exists()) {
            mappingFile.createNewFile()
        }
    }

    fun readContent(): String {
        if (!this::mappingFile.isInitialized) {
            return ""
        }
        return mappingFile.readText()
    }

    //写了一个最简单的方式，将类名存到一个文件中，后续可以把类名和新生成的类名都存起来
    fun saveClassName(className: String) {
        if (!this::mappingFile.isInitialized) {
            return
        }
        mappingFile.appendText("$className\n")
    }
}