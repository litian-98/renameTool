package com.litian.plugin.service

import com.github.javafaker.Faker
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.RefactoringSettings
import com.intellij.refactoring.rename.RenameProcessor
import com.jetbrains.lang.dart.DartLanguage
import com.jetbrains.lang.dart.psi.DartVarAccessDeclaration
import com.litian.plugin.MappingUtils
import io.github.serpro69.kfaker.lorem.faker
import org.jetbrains.kotlin.descriptors.isOverridableOrOverrides
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import java.util.*


class RenameService {

    private val log = Logger.getInstance("RenameMaster")
    private val githubFaker = Faker(Locale.US)
    private val faker = faker {}

    /**
     * 处理文件内的所有变量,方法名
     * 如果是文件夹,则遍历该文件夹中的所有文件,并处理
     */
    fun processVirtualFile(virtualFile: VirtualFile, project: Project) {

        log.info("开始处理文件: ${virtualFile.path}")
        val psiManager = PsiManager.getInstance(project)

        fun processFile(file: VirtualFile) {
            if (file.isDirectory) {
                file.children.forEach(::processFile)
            } else {
                psiManager.findFile(file)?.let {
                    CommandProcessor.getInstance().executeCommand(
                        project,
                        { processSingleFile(it, project) },
                        "Rename Variables",
                        null
                    )
                }
            }
        }

        processFile(virtualFile)
    }

    /**
     * 处理文件内的所有变量,方法名
     */
    private fun processSingleFile(psiFile: PsiFile, project: Project) {
        val startTime = System.currentTimeMillis()
        log.info("开始处理 ${psiFile.virtualFile.path}")
        val variableSet = HashSet<String>()


        //找出所有需要修改的变量名和方法
        filterPsiElementByFile(psiFile)
            .forEach {
                // 重命名
                renameElement(it, variableSet, psiFile, project)
            }

        log.info("处理结束 ${psiFile.virtualFile.path},耗时: ${System.currentTimeMillis() - startTime}")
    }

    /**
     * 重命名变量
     */
    private fun renameElement(
        psiElement: PsiElement,
        variableSet: HashSet<String>,
        psiFile: PsiFile,
        project: Project
    ) {
        val name = (psiElement as PsiNamedElement).name

        val newVariable = if (psiElement is KtClass){
            generateNewClassName()
        }else{
            generateNewVariable(variableSet).also { variableSet.add(it) }
        }

        log.info("重命名map: ${psiFile.virtualFile.path} ==> $name : $newVariable".trimIndent())

        // Disable popup
        RefactoringSettings.getInstance().RENAME_SHOW_AUTOMATIC_RENAMING_DIALOG = false
        RenameProcessor(project, psiElement, newVariable, false, false).apply {
            setPreviewUsages(false)
            setCommandName("重命名")
            doRun()
        }
    }

    private val handlers = mapOf(
        JavaLanguage.INSTANCE.displayName to ::handleJavaLanguage,
        DartLanguage.INSTANCE.displayName to ::handleDartLanguage,
        KotlinLanguage.INSTANCE.displayName to ::handleKotlinLanguage
    )

    private fun filterPsiElementByFile(psiFile: PsiFile): Array<PsiElement> {
        val language = psiFile.language.displayName
        return handlers[language]?.invoke(psiFile) ?: emptyArray()
    }

    private fun handleJavaLanguage(psiFile: PsiFile): Array<PsiElement> {
        return PsiTreeUtil.collectElements(psiFile) { it is PsiVariable }
    }

    private fun handleDartLanguage(psiFile: PsiFile): Array<PsiElement> {
        return PsiTreeUtil.collectElements(psiFile) { it is DartVarAccessDeclaration }
    }

    private fun handleKotlinLanguage(psiFile: PsiFile): Array<PsiElement> {
        return PsiTreeUtil.collectElements(psiFile) { element ->
            when (element) {
                is KtProperty, is KtParameter,is KtClass -> true
                is KtNamedFunction -> {
                    val functionDescriptor = element.resolveToDescriptorIfAny(BodyResolveMode.FULL)
                    functionDescriptor?.isOverridableOrOverrides == false
                }
                else -> false
            }
        }.filterIsInstance<PsiElement>().toTypedArray()
    }

    private fun generateNewVariable(set: HashSet<String>): String {
        fun toCamelCase(words: List<String>): String {
            return words.mapIndexed { index, word ->
                if (index == 0) word.lowercase()
                else word.replaceFirstChar { it.titlecase(Locale.getDefault()) }
            }.joinToString("")
        }

        val newVariable = toCamelCase(
            arrayListOf(
                faker.adjective.positive(),
                toCamelCase(githubFaker.color().name().split(" "))
            )
        )

        return if (set.contains(newVariable)) {
            generateNewVariable(set)
        } else {
            newVariable
        }
    }

    private fun generateNewClassName(): String {
        fun toCamelCase(words: List<String>): String {
            return words.mapIndexed { _, word ->
                word.replaceFirstChar { it.titlecase(Locale.getDefault()) }
            }.joinToString("")
        }

        var newName = toCamelCase(
            arrayListOf(
                faker.adjective.positive(),
                toCamelCase(githubFaker.color().name().split(" "))
            )
        )

        val content = MappingUtils.readContent()
        if (content.contains(newName,true)){
            newName = generateNewClassName()
        }

        return newName.also {
            MappingUtils.saveClassName(newName)
        }
    }

}