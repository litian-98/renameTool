package com.litian.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import com.litian.plugin.service.RenameService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RenameAction : AnAction() {
    private val log = Logger.getInstance("renameTool")

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        log.info("RenameAction触发成功!")

        val projectManager = ProjectRootManager.getInstance(project)

       
        renameChild(virtualFile, project)


    }

    private fun renameChild(virtualFile: VirtualFile, project: Project) {
        if (virtualFile.isDirectory) {
            virtualFile.children.forEach {
                renameChild(it, project)
            }
        } else {
            RenameService(project).processFile(virtualFile)
        }
    }
}