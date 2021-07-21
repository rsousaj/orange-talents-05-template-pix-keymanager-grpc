package br.com.zup.orangetalents.integracao

import br.com.zup.orangetalents.pix.TipoConta
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class AccountTypeTest {

    @ParameterizedTest
    @MethodSource("br.com.zup.orangetalents.integracao.AccountTypeTestKt#tiposConta")
    fun `deve transformar TipoConta (local) para AccountType (banco central)`(tipoConta: TipoConta) {
        assertDoesNotThrow {
            AccountType.convertAccountType(tipoConta)
        }
    }
}

private fun tiposConta(): Stream<Arguments> {
    val stream  = Stream.builder<Arguments>()

    TipoConta.values().forEach { stream.add(Arguments.of(it)) }

    return stream.build()
}
