/*
 * Copyright (C) 2023 Linnea Gräf
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.autosubscribe

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import java.util.function.Consumer
import java.util.function.Supplier

internal class NEUAutoSymbolProcessor(val codeGenerator: CodeGenerator, val logger: KSPLogger) : SymbolProcessor {
    fun collectSubscribers(elements: List<KSAnnotated>): List<NEUEventSubscriber> = buildList {
        for (element in elements) {
            if (element !is KSClassDeclaration) {
                logger.error("@NEUAutoSubscribe is only valid on class or object declarations", element)
                continue
            }
            if (element.typeParameters.isNotEmpty()) {
                logger.error("@NEUAutoSubscribe is not valid on generic classes", element)
                continue
            }
            val name = element.qualifiedName
            if (name == null) {
                logger.error("@NEUAutoSubscribe could not find name", element)
                continue
            }
            when (element.classKind) {
                ClassKind.CLASS -> {
                    val instanceGetter = element.getDeclaredFunctions().find {
                        it.simpleName.asString() == "getInstance"
                    }
                    val instanceVariable = element.getDeclaredProperties().find {
                        it.simpleName.asString() == "INSTANCE"
                    }
                    if (instanceGetter != null) {
                        val returnType = instanceGetter.returnType
                        if (returnType == null || !element.asStarProjectedType().isAssignableFrom(returnType.resolve())) {
                            logger.error(
                                "getInstance() does not have the expected return type ${element.asStarProjectedType()}",
                                instanceGetter
                            )
                            continue
                        }
                        add(NEUEventSubscriber(InvocationKind.GET_INSTANCE, element))
                    } else if (instanceVariable != null) {
                        val variableType = instanceVariable.type
                        if (!element.asStarProjectedType().isAssignableFrom(variableType.resolve())) {
                            logger.error(
                                "INSTANCE does not have expected type ${element.asStarProjectedType()}",
                                instanceVariable
                            )
                            continue
                        }
                        add(NEUEventSubscriber(InvocationKind.ACCESS_INSTANCE, element))
                    } else {
                        add(NEUEventSubscriber(InvocationKind.DEFAULT_CONSTRUCTOR, element))
                    }
                }

                ClassKind.OBJECT -> {
                    add(NEUEventSubscriber(InvocationKind.OBJECT_INSTANCE, element))
                }

                else -> {
                    logger.error(
                        "@NEUAutoSubscribe is only valid on classes and objects, not on ${element.classKind}",
                        element
                    )
                    continue
                }
            }
        }

    }

    val subscribers = mutableListOf<NEUEventSubscriber>()
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val candidates = resolver.getSymbolsWithAnnotation(NEUAutoSubscribe::class.qualifiedName!!).toList()
        val valid = candidates.filter { it.validate() }
        val invalid = candidates.filter { !it.validate() }

        subscribers.addAll(collectSubscribers(valid))
        return invalid
    }

    override fun finish() {
        if (subscribers.isEmpty()) return
        val deps = subscribers.mapNotNull { it.declaration.containingFile }
        logger.info("Dependencies: $deps")
        FileSpec.builder("io.github.moulberry.notenoughupdates.autosubscribe", "AutoLoad")
            .addFileComment("@generated by ${NEUAutoSymbolProcessor::class.simpleName}")
            .addType(
                TypeSpec.objectBuilder("AutoLoad")
                    .addFunction(
                        FunSpec.builder("provide")
                            .addParameter(
                                "consumer",
                                Consumer::class.asTypeName()
                                    .parameterizedBy(Supplier::class.parameterizedBy(Any::class))
                            )
                            .apply {
                                subscribers.sortedBy { it.declaration.simpleName.asString() }.forEach { (invocationKind, declaration) ->
                                    when (invocationKind) {
                                        InvocationKind.GET_INSTANCE -> addStatement(
                                            "consumer.accept { %T.getInstance() }",
                                            declaration.toClassName()
                                        )

                                        InvocationKind.OBJECT_INSTANCE -> addStatement(
                                            "consumer.accept { %T }",
                                            declaration.toClassName()
                                        )

                                        InvocationKind.DEFAULT_CONSTRUCTOR -> addStatement(
                                            "consumer.accept { %T() }",
                                            declaration.toClassName()
                                        )

                                        InvocationKind.ACCESS_INSTANCE -> addStatement(
                                            "consumer.accept { %T.INSTANCE }",
                                            declaration.toClassName()
                                        )
                                    }
                                }
                            }
                            .build()
                    )
                    .build()
            )
            .build()
            .writeTo(codeGenerator, aggregating = true, originatingKSFiles = deps)
    }
}
