package br.com.zup.orangetalents.pix.novachave

import javax.validation.Payload
import javax.validation.constraints.Pattern
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Retention(RUNTIME)
@Target(FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Pattern(
    regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
    flags = [Pattern.Flag.CASE_INSENSITIVE]
)
annotation class ValidoUUID(
    val message: String = "Identificador UUID inválido",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

//@Singleton
//class ValidaUUIDValidator : ConstraintValidator<ValidoUUID, String> {
//    override fun isValid(
//        value: String,
//        annotationMetadata: AnnotationValue<ValidoUUID>,
//        context: ConstraintValidatorContext
//    ): Boolean {
//        if (value.isNullOrBlank()) return true
//
//        return try {
//            UUID.fromString(value)
//            true
//        } catch (ex: IllegalArgumentException) {
//            false
//        }
//    }
//}
