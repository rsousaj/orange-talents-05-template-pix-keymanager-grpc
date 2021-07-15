package br.com.zup.orangetalents.pix.novachave

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import java.util.*
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@Retention(RUNTIME)
@Target(FIELD, PROPERTY, VALUE_PARAMETER)
@Constraint(validatedBy = [ValidaUUIDValidator::class])
annotation class ValidoUUID(
    val message: String = "Identificador UUID inválido",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Singleton
class ValidaUUIDValidator : ConstraintValidator<ValidoUUID, String> {
    override fun isValid(
        value: String,
        annotationMetadata: AnnotationValue<ValidoUUID>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value.isNullOrBlank()) return true

        return try {
            UUID.fromString(value)
            true
        } catch (ex: IllegalArgumentException) {
            false
        }
    }
}
