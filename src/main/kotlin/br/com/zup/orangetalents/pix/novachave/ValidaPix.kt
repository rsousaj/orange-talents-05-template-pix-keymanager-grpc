package br.com.zup.orangetalents.pix.novachave

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidaPixValidator::class])
annotation class ValidaPix(
    val message: String = "Chave inválida",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Singleton
class ValidaPixValidator : ConstraintValidator<ValidaPix, NovaChavePix> {
    override fun isValid(
        value: NovaChavePix?,
        annotationMetadata: AnnotationValue<ValidaPix>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value?.tipoChave == null) return false

        return value.tipoChave.valida(value.chave)
    }
}