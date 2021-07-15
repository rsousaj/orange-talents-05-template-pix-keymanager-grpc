package br.com.zup.orangetalents.pix.removechave

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hibernate.annotations.Source
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import javax.validation.ConstraintViolationException

@MicronautTest
internal class RemoveChavePixServiceTest(
    private val removeChavePixService: RemoveChavePixService
) {

    @Test
    fun `deve validar se dados estao vazios`() {
        assertThrows(ConstraintViolationException::class.java) {
            removeChavePixService.remove("", "")
        } .also {
            assertEquals("remove.id: must not be blank, remove.clientId: must not be blank", it.message)
        }
    }

    @Test
    fun `deve validar se codigos UUID sao validos`() {
        assertThrows(ConstraintViolationException::class.java) {
            removeChavePixService.remove("codigo-invalido", "codigo-invalido")
        }.also {
            assertEquals(2, it.constraintViolations.size)
        }
    }
}