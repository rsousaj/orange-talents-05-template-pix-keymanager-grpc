package br.com.zup.orangetalents.integracao

import io.micronaut.aop.Around

@Around
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ErrorHandlerBCB()
