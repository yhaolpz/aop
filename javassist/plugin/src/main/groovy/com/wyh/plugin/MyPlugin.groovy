package com.wyh.plugin

import com.android.build.api.transform.TransformException
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import com.android.SdkConstants
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.ClassPool
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class MyPlugin extends Transform implements Plugin<Project> {

    private static final def CLICK_LISTENER = "android.view.View\$OnClickListener"

    def isLibrary
    def pool = ClassPool.default
    def project

    @Override
    void apply(Project project) {
        println "wyh--- apply plugin"
        isLibrary = project.plugins.hasPlugin(LibraryPlugin)
        this.project = project
        def android
        if (isLibrary) {
            android = project.extensions.getByType(LibraryExtension)
        } else {
            android = project.extensions.getByType(AppExtension)
        }
        android.registerTransform(this)
    }

    @Override
    String getName() {
        //用来定义transform任务的名称
        return "ssist"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        //用来限定这个transform能处理的文件类型，一般来说我们要处理的都是class文件，
        // 就返回TransformManager.CONTENT_CLASS,当然如果你是想要处理资源文件，
        // 可以使用TransformManager.CONTENT_RESOURCES
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set getScopes() {
        //要处理那种文件，那么，这里我们要指定的的就是哪些文件了。比如说我们如果想处理class文件，
        // 但class文件可以是当前module的，也可以是子module的，
        // 还可以是第三方jar包中的，这里就是用来指定这个的
        if (isLibrary) {
            return TransformManager.PROJECT_ONLY
        }
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        //是否支持增量编译
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException,
            InterruptedException, IOException {
        super.transform(transformInvocation)
        println "wyh---plugin transform "
        project.android.bootClasspath.each {
            pool.appendClassPath(it.absolutePath)
        }
        transformInvocation.inputs.each {
            it.jarInputs.each {
                pool.insertClassPath(it.file.absolutePath)
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = it.name
                println "jarName"+jarName
                def md5Name = DigestUtils.md5Hex(it.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = transformInvocation.outputProvider.getContentLocation(
                        jarName + md5Name, it.contentTypes, it.scopes, Format.JAR)
                FileUtils.copyFile(it.file, dest)
            }
            it.directoryInputs.each {
                def preFileName = it.file.absolutePath
                println "preFileName"+preFileName
                pool.insertClassPath(preFileName)
                findTarget(it.file, preFileName)
                // 获取output目录
                def dest = transformInvocation.outputProvider.getContentLocation(
                        it.name,
                        it.contentTypes,
                        it.scopes,
                        Format.DIRECTORY)
                println "copy directory: " + it.file.absolutePath
                println "dest directory: " + dest.absolutePath
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(it.file, dest)
            }
        }
    }

    private void findTarget(File dir, String fileName) {
        if (dir.isDirectory()) {
            dir.listFiles().each {
                findTarget(it, fileName)
            }
        } else {
            modify(dir, fileName)
        }
    }

    private void modify(File dir, String fileName) {
        def filePath = dir.absolutePath

        if (!filePath.endsWith(SdkConstants.DOT_CLASS)) {
            return
        }
        if (filePath.contains('R$') || filePath.contains('R.class')
                || filePath.contains("BuildConfig.class")) {
            return
        }
        def className = filePath.replace(fileName, "")
                .replace("\\", ".")
                .replace("/", ".")
        def name = className.replace(SdkConstants.DOT_CLASS, "")
                .substring(1)
        CtClass ctClass = pool.get(name)
        CtClass[] interfaces = ctClass.getInterfaces()
        if (interfaces.contains(pool.get(CLICK_LISTENER))) {
            if (name.contains("\$")) {
                println "class is inner class：" + ctClass.name
                println "CtClass: " + ctClass
                CtClass outer = pool.get(name.substring(0, name.indexOf("\$")))

                CtField field = ctClass.getFields().find {
                    return it.type == outer
                }
                if (field != null) {
                    println "fieldStr: " + field.name
                    def body = "android.widget.Toast.makeText(" + field.name + "," +
                            "\"javassist\", android.widget.Toast.LENGTH_SHORT).show();"
                    addCode(ctClass, body, fileName)
                }
            } else {
                println "class is outer class: " + ctClass.name
                //更改onClick函数
                def body = "android.widget.Toast.makeText(\$1.getContext(), \"javassist\", android.widget.Toast.LENGTH_SHORT).show();"
                addCode(ctClass, body, fileName)
            }
        }
    }

    private void addCode(CtClass ctClass, String body, String fileName) {
        ctClass.defrost()
        CtMethod method = ctClass.getDeclaredMethod("onClick", pool.get("android.view.View"))
        method.insertAfter(body)
        ctClass.writeFile(fileName)
        ctClass.detach()
        println "write file: " + fileName + "\\" + ctClass.name
        println "modify method: " + method.name + " succeed"
    }
}