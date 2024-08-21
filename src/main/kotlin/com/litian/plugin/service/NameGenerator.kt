package com.litian.plugin.service

import com.github.javafaker.Faker
import com.intellij.openapi.project.Project
import com.litian.plugin.util.MappingUtils
import com.litian.plugin.util.NameUtil
import io.github.serpro69.kfaker.lorem.faker
import java.util.*

class NameGenerator(project: Project) {

    init {
        MappingUtils.createMappingFile(project.basePath)
    }


    fun generateClassName(): String {
        var newName = NameUtil.generateVariableName()

        if (MappingUtils.classNameExist(newName)) {
            newName = generateClassName()
        }

        return newName.also {
            MappingUtils.saveClassName(newName)
        }
    }


}