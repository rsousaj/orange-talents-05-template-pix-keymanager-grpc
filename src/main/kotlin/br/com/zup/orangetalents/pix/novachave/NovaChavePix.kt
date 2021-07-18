package br.com.zup.orangetalents.pix.novachave

import br.com.zup.orangetalents.pix.ChavePix
import br.com.zup.orangetalents.pix.Conta
import br.com.zup.orangetalents.pix.TipoChave
import br.com.zup.orangetalents.pix.TipoConta
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
            tipoChave = tipoChave,
            chave = if (tipoChave == TipoChave.ALEATORIO) UUID.randomUUID().toString()
                    else this.chave,
            conta = conta
        )
    }
}
