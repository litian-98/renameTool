package com.litian.plugin.service

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.RefactoringSettings
import com.intellij.refactoring.rename.RenameProcessor
import com.jetbrains.lang.dart.DartLanguage
import com.jetbrains.lang.dart.ide.refactoring.DartRenameService
import com.jetbrains.lang.dart.psi.DartVarAccessDeclaration
import com.litian.plugin.util.NameUtil
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import java.util.*


class RenameService(
    private val project: Project
) {

    private val log = Logger.getInstance("renameTool")
    private val psiManager = PsiManager.getInstance(project)

    private val nameGenerator = NameGenerator(project)


    fun processFile(file: VirtualFile) {
        val psiFile = psiManager.findFile(file) ?: return

        if (psiFile.language == DartLanguage.INSTANCE) {
            handleDartLanguage(psiFile)
        }
        if (psiFile.language == JavaLanguage.INSTANCE) {
            handleJavaLanguage(psiFile)
        }
        if (psiFile.language == KotlinLanguage.INSTANCE) {
            handleKotlinLanguage(psiFile)
        }
    }

    private fun handleDartLanguage(psiFile: PsiFile, variableSet: HashSet<String> = hashSetOf()) {
        //重命名变量
        val collectElements = PsiTreeUtil.collectElements(psiFile) { it is DartVarAccessDeclaration }

        collectElements.forEach { it ->
            val newName = NameUtil.generateVariableName().also { variableSet.add(it) }
            DartRenameService().rename(it as DartVarAccessDeclaration, newName, project, psiFile.virtualFile)
        }


        //重命名方法
//        val methods = PsiTreeUtil.collectElements(psiFile) { it is DartMethodDeclaration }
//
//        for (method in methods) {
//
//            val method1: DartMethodDeclaration = method as DartMethodDeclaration
//            val annotations = method1.metadataList
//            var isOverride = false
//            for (annotation in annotations) {
//                if (annotation.text == "@override") {
//                    isOverride = true
//                    break
//                }
//            }
//            if (!isOverride) {
//                ApplicationManager.getApplication().runWriteAction {
//                    val newName = NameUtil.generateVariableName().also { variableSet.add(it) }
//                    DartRenameService().rename(method1, newName, project, psiFile.virtualFile)
//                }
//            }
//        }
    }

    private fun handleJavaLanguage(psiFile: PsiFile, variableSet: HashSet<String> = hashSetOf()) {
        val collectElements = PsiTreeUtil.collectElements(psiFile) { it is PsiVariable }


        for (element in collectElements) {
            val oldName = (element as PsiNamedElement).name

            val newName = NameUtil.generateVariableName().also { variableSet.add(it) }

            log.info("重命名map: ${psiFile.virtualFile.path} ==> $oldName : $newName".trimIndent())

            // Disable popup
            RefactoringSettings.getInstance().RENAME_SHOW_AUTOMATIC_RENAMING_DIALOG = false
            RenameProcessor(project, element, newName, false, false).apply {
                setPreviewUsages(false)
                setCommandName("重命名")
                doRun()
            }
        }


    }

    private fun handleKotlinLanguage(psiFile: PsiFile, variableSet: HashSet<String> = hashSetOf()) {
        // 过滤并收集符合条件的元素
        val filteredElements = PsiTreeUtil.collectElements(psiFile) { element ->
            when (element) {
                is KtProperty, is KtParameter, is KtClass -> true
                is KtNamedFunction -> !element.hasModifier(KtTokens.OVERRIDE_KEYWORD)
                else -> false
            }
        }


        for (psiElement in filteredElements) {
            val oldName = (psiElement as PsiNamedElement).name

            val newName = if (psiElement is KtClass) {
                nameGenerator.generateClassName()
            } else {
                NameUtil.generateVariableName().also { variableSet.add(it) }
            }

            log.info("重命名map: ${psiFile.virtualFile.path} ==> $oldName : $newName".trimIndent())

            // Disable popup
            RefactoringSettings.getInstance().RENAME_SHOW_AUTOMATIC_RENAMING_DIALOG = false
            RenameProcessor(project, psiElement, newName, false, false).apply {
                setPreviewUsages(false)
                setCommandName("重命名")
                doRun()
            }
        }

    }
}