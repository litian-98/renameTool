package com.jetbrains.lang.dart.ide.refactoring

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.jetbrains.lang.dart.assists.AssistUtils
import com.jetbrains.lang.dart.assists.DartSourceEditException
import com.jetbrains.lang.dart.psi.DartVarAccessDeclaration
import com.litian.plugin.util.NameUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DartRenameService {

    fun rename(psiElement: PsiElement, newName: String, project: Project, virtualFile: VirtualFile) {
        val offset: Int = psiElement.textOffset
        val textLength = psiElement.textLength

        //
        val refactoring = ServerRenameRefactoring(project, virtualFile, offset, 0)

        refactoring.checkInitialConditions()
        refactoring.setNewName(newName)
        refactoring.checkFinalConditions()
        val potentialEdits = refactoring.potentialEdits

        val change = refactoring.change
        if (change != null) {

            CoroutineScope(Dispatchers.EDT).launch {
                writeAction {
                    AssistUtils.applySourceChange(project, change, true)
                    VirtualFileManager.getInstance().syncRefresh();
                }
            }
        }
    }
}