package br.com.zup.orangetalents.pix.novachave

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@MicronautTest
internal class TipoChaveTest {

    @Nested
    inner class Aleatorio {

        @Test
        fun `deve validar se chave não for informada`() {
            TipoChave.ALEATORIO.apply {
                assertTrue(valida(""))
                assertTrue(valida(null))
            }
        }

        @Test
        fun `nao deve validar se chave for informada`() {
            TipoChave.ALEATORIO.apply {
                assertFalse(valida("chave"))
            }
        }
    }

    @Nested
    inner class CPF {

        val cpfInvalido = "766002910"
        val cpfValido = "76600291001"

        @Test
        fun `nao deve validar se CPF nao for informado`() {
            TipoChave.CPF.apply {
                assertFalse(valida(""))
                assertFalse(valida(null))
            }
        }

        @Test
        fun `nao deve validar se CPF for invalido`() {
            TipoChave.CPF.apply {
                assertFalse(valida(cpfInvalido))
            }
        }

        @Test
        fun `deve validar se CPF for valido`() {
            TipoChave.CPF.apply {
                assertTrue(valida(cpfValido))
            }
        }
    }

    @Nested
    inner class Email {

        @Test
        fun `nao deve validar se email nao informado`() {
            TipoChave.EMAIL.apply {
                assertFalse(valida(""))
                assertFalse(valida(null))
            }
        }

        @Test
        fun `nao deve validar se email em formato invalido`() {
            TipoChave.EMAIL.apply {
                assertFalse(valida("email.com"))
            }
        }

        @Test
        fun `deve validar se email estiver em formato valido`() {
            TipoChave.EMAIL.apply {
                assertTrue(valida("usuario@email.com"))
            }
        }
    }

    @Nested
    inner class CELULAR {

        @ParameterizedTest
        @MethodSource("br.com.zup.orangetalents.pix.novachave.TipoChaveTestKt#celularesValidos")
        fun `deve verificar se numero celular eh valido`(celular: String, ehValido: Boolean) {
            TipoChave.CELULAR.apply {
                assertEquals(ehValido, valida(celular))
            }
        }
    }
}

private fun celularesValidos(): Stream<Arguments> {
    return Stream.of(
        Arguments.of("+5511999992222", true),
        Arguments.of("5511999992222", false),
        Arguments.of("+A999992222", false)
    )
}