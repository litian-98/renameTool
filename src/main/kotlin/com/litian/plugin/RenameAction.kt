package com.litian.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.litian.plugin.service.RenameService

class RenameAction : AnAction() {
    private val log = Logger.getInstance("RenameMaster")

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        log.info("RenameAction触发成功!")

        var projectPath = project.basePath
        log.info("projectPath:$projectPath")
        MappingUtils.createMappingFile(projectPath)

        RenameService().processVirtualFile(virtualFile, project)
    }

}
