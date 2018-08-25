package io.octo.bear.processor

import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement


const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

fun ProcessingEnvironment.getOutputFile(classElement: TypeElement) = getOutputDir()?.let {
    File(
            getOutputFileDirectory(
                    it,
                    elementUtils.getPackageOf(classElement).qualifiedName.toString()
            ),
            "${classElement.simpleName}Builder.kt"
    ).apply { parentFile.mkdirs() }
}

private fun ProcessingEnvironment.getOutputDir() =
        options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            error("Can't find the target directory for generated Kotlin files.")
            null
        }

private fun getOutputFileDirectory(baseDir: String, packageName: String) =
        "$baseDir${File.separator}${packageName.replace(".", File.separator)}"