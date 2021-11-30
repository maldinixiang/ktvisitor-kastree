package main

import analysis.KtVisitor
import analysis.Parser
import kastree.ast.Node
import listener.ISourceClassCallback
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtFile
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


class KtSourceAnalysisVisitor(callback: ISourceClassCallback) {

    private var dataCallback: ISourceClassCallback = callback
    private var classList: ArrayList<String> = arrayListOf()
    private var methodList: ArrayList<String> = arrayListOf()
    private lateinit var proj: Project

    init {
        val configuration = CompilerConfiguration()
        configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, PrintingMessageCollector(System.out, MessageRenderer.PLAIN_FULL_PATHS, true))
        configuration.put(JVMConfigurationKeys.JVM_TARGET, JvmTarget.JVM_1_8)
        proj = KotlinCoreEnvironment.createForProduction(Disposer.newDisposable(),
                configuration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES).project

    }

    fun doVisitor(file: File) {
        if (file.isDirectory) {
            doVisitor(file)
        } else {
            val fileData = StringBuilder()
            return try {
                val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(file)))
                var line: String? = ""
                while (bufferedReader.readLine().also { line = it } != null) {
                    fileData.append(line).append("\n")
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            doPackageVisitor(fileData.toString())
            doFunVisitor(fileData.toString())
        }

    }

    fun doVisitorEnd() {
        dataCallback.callbackAnalysisInfo(classList)
        dataCallback.callbackAnalysisInfo(methodList)
    }

    private fun doPackageVisitor(inputFile: String) {

        var ktFile = PsiManager.getInstance(proj).findFile(LightVirtualFile("temp.kt",
                KotlinFileType.INSTANCE, inputFile)) as KtFile

        ktFile.packageDirective?.takeIf { it.packageNames.isNotEmpty() }
        var directive = ktFile.packageDirective
        if (directive != null) {
            if (directive.packageNames.isNotEmpty()) {
                println(directive.packageNames.get(0).getReferencedNameElement())

            }
            var pkg = ktFile.takeIf { directive.packageNames.isNotEmpty() }.let { directive.packageNames.map { it.getReferencedNameAsName() } }
            var pkgFullName = StringBuilder()
            pkg.forEach() { item ->
                pkgFullName.append(item).append("/")
            }
            val ktVisitor = KtVisitor()
            ktFile.accept(ktVisitor)
            ktFile.acceptChildren(ktVisitor)

            var packageList = ktVisitor.visitEnd()
            packageList.forEach() { packageName ->
                classList.add(pkgFullName.append(packageName).toString())
            }
        }
    }

    private fun doFunVisitor(inputFile: String) {
        var classParser = Parser()
        var classFile = classParser.parseFile(inputFile)
        classFile.decls.forEach {
            when (it) {
                is Node.Decl.Structured -> {
                    getFunInfo(it)
                }
                is Node.Decl.Func -> {
                    it.name?.let { funName -> methodList.add(funName) }
                }
            }
        }
    }

    private fun getFunInfo(structured: Node.Decl.Structured) {
        structured.members.forEach {
            when (it) {
                is Node.Decl.Structured -> {
                    getFunInfo(it)
                }
                is Node.Decl.Func -> {
                    it.name?.let { funName -> methodList.add(funName) }
                }
            }
        }
    }

}
