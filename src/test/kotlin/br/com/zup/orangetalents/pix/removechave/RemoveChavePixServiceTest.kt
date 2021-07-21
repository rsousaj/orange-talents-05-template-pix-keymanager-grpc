package br.com.zup.orangetalents.pix.removechave

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsInAnyOrder
import org.hibernate.annotations.Source
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
//import org.junit.jupiter.params.ParameterizedTest
//import org.junit.jupiter.params.provider.CsvSource
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
            assertThat(it.message, containsString("remove.id: must not be blank"))
            assertThat(it.message, containsString("remove.clientId: must not be blank"))
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