package io.octo.bear.processor

import com.squareup.kotlinpoet.*
import io.octo.bear.annotation.Builder
import io.octo.bear.annotation.BuilderProperty
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

class BuilderProcessor : AbstractProcessor() {

    private lateinit var log: (String) -> Unit

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        log = { processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, it) }
    }

    override fun process(
            annotations: MutableSet<out TypeElement>,
            roundEnv: RoundEnvironment
    ): Boolean {
        val buildingClasses = roundEnv.getElementsAnnotatedWith(Builder::class.java)

        buildingClasses.forEach {
            val parentType = it as TypeElement
            val builderProperties: List<VariableElement> = parentType
                    .enclosedElements.toList()
                    .filter { it.getAnnotation(BuilderProperty::class.java) != null }
                    .map { method ->
                        it.enclosedElements.first { element ->
                            element is VariableElement && element.simpleName.toString() == method.propertyName()
                        } as VariableElement
                    }

            val baseBuilderName = "${it.simpleName}Builder"

            val builderObject = TypeSpec
                    .objectBuilder(baseBuilderName)
                    .addFunction(
                            createInitialFunction(
                                    parentType,
                                    builderProperties.first(),
                                    builderProperties.getOrNull(1)
                            )
                    )
                    .addTypes(
                            (1 until builderProperties.size).map {
                                val currentProperty = builderProperties[it]
                                val nextProperty = builderProperties.getOrNull(it + 1)
                                createClassWithStepFunction(parentType, currentProperty, nextProperty)
                            }
                    )
                    .addType(createFinalBuilderClass(parentType))

            val file = FileSpec
                    .builder(
                            processingEnv.elementUtils.getPackageOf(parentType).qualifiedName.toString(),
                            baseBuilderName
                    )
                    .addType(builderObject.build())

            val fixedCode = file.build().toString().replace("java.lang.String", "kotlin.String")

            processingEnv
                    .getOutputFile(it)
                    ?.writeText(fixedCode)

        }

        return true
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
            Builder::class.java.canonicalName,
            BuilderProperty::class.java.canonicalName
    )

    private fun createInitialFunction(
            parent: TypeElement,
            property: VariableElement,
            nextProperty: VariableElement?
    ): FunSpec {
        val propertyName = property.simpleName.toString()
        val returnTypeName = nextProperty?.let { "${baseBuilderName(parent)}${it.simpleName.toString().capitalize()}Step" }
                ?: "${baseBuilderName(parent)}Final"
        val returnType = ClassName("", returnTypeName)
        val targetType = parent.asType().asTypeName()

        return FunSpec
                .builder("with${propertyName.capitalize()}")
                .addParameter(propertyName, property.asType().asTypeName())
                .returns(returnType)
                .addCode("""
                    |return %T(%T().apply {
                    |    this.%N = %N
                    |})
                    |""".trimMargin(), returnType, targetType, propertyName, propertyName
                )
                .build()
    }

    private fun createClassWithStepFunction(
            parent: TypeElement,
            property: VariableElement,
            nextProperty: VariableElement?
    ): TypeSpec {
        val propertyName = property.simpleName.toString()
        val currentBuilderName = "${baseBuilderName(parent)}${propertyName.capitalize()}Step"
        val returnTypeName = nextProperty?.let { "${baseBuilderName(parent)}${it.simpleName.toString().capitalize()}Step" }
                ?: "${baseBuilderName(parent)}Final"
        val returnType = ClassName("", returnTypeName)
        val targetName = parent.simpleName.toString().decapitalize()

        return TypeSpec.classBuilder(currentBuilderName)
                // region constructor
                .primaryConstructor(
                        FunSpec
                                .constructorBuilder()
                                .addParameter(
                                        targetName,
                                        parent.asType().asTypeName(),
                                        KModifier.PRIVATE
                                )
                                .build()
                )
                .addProperty(
                        PropertySpec
                                .builder(targetName, parent.asType().asTypeName())
                                .initializer(targetName)
                                .build()
                )
                // endregion
                // region function
                .addFunction(
                        FunSpec
                                .builder("with${propertyName.capitalize()}")
                                .addParameter(propertyName, property.asType().asTypeName())
                                .returns(returnType)
                                .addCode("""
                                    |return %T(%N.apply {
                                    |    this.%N = %N
                                    |})
                                    |""".trimMargin(), returnType, targetName, propertyName, propertyName
                                )
                                .build()
                )
                //endregion
                .build()
    }

    private fun createFinalBuilderClass(parent: TypeElement): TypeSpec {
        val currentBuilderName = "${baseBuilderName(parent)}Final"
        val returnType = parent.asClassName()
        val targetName = parent.simpleName.toString().decapitalize()

        return TypeSpec.classBuilder(currentBuilderName)
                // region constructor
                .primaryConstructor(
                        FunSpec
                                .constructorBuilder()
                                .addParameter(
                                        targetName,
                                        parent.asType().asTypeName(),
                                        KModifier.PRIVATE
                                )
                                .build()
                )
                .addProperty(
                        PropertySpec
                                .builder(targetName, parent.asType().asTypeName())
                                .initializer(targetName)
                                .build()
                )
                // endregion
                // region function
                .addFunction(
                        FunSpec
                                .builder("build")
                                .returns(returnType)
                                .addCode("return %N\n", targetName)
                                .build()
                )
                //endregion
                .build()
    }

    private fun baseBuilderName(parent: TypeElement): String = "${parent.simpleName}Builder"

    private fun Element.propertyName() = simpleName.run { substring(0, indexOf("$")) }

}