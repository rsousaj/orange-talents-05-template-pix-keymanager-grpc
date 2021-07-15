package br.com.zup.orangetalents.pix.novachave

import br.com.zup.orangetalents.pix.ChavePix
import br.com.zup.orangetalents.pix.Conta
import io.micronaut.core.annotation.Introspected
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidaPix
@Introspected
data class NovaChavePix(
    @field:NotBlank
    @field:ValidoUUID
    val codigoCliente: String,

    @field:NotNull
    val tipoChave: TipoChave?,

    @field:Size(max = 77)
    val chave: String,

    @field:NotNull
    val tipoConta: TipoConta?
) {

    fun paraModelo(conta: Conta): ChavePix {
        return ChavePix(
            tipoChave = tipoChave.toString(),
            chave = if (tipoChave == TipoChave.ALEATORIO) UUID.randomUUID().toString()
                    else this.chave,
            conta = conta
        )
    }
}

enum class TipoConta {
    CONTA_CORRENTE, CONTA_POUPANCA
}

enum class TipoChave {
    CPF {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    }, CELULAR {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    }, EMAIL {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    }, ALEATORIO {
        override fun valida(chave: String?): Boolean {
            return chave.isNullOrBlank()
        }
    };

    abstract fun valida(chave: String?): Boolean
}
