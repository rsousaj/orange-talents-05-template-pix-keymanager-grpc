package br.com.zup.orangetalents.compartilhado

import io.micronaut.aop.Around
import io.micronaut.aop.InterceptorBean
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*

@Around
@MustBeDocumented
@Target(FUNCTION)
@Retention(RUNTIME)
annotation class ErrorHandler()
