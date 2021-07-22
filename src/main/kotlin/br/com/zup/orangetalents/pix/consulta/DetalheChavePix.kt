package br.com.zup.orangetalents.pix.consulta

import br.com.zup.orangetalents.TipoDeChave
import br.com.zup.orangetalents.TipoDeConta
import br.com.zup.orangetalents.pix.ChavePix
import br.com.zup.orangetalents.pix.Conta
import br.com.zup.orangetalents.pix.TitularConta
import java.time.LocalDateTime

class DetalheChavePix(
    val pixId: String? = null,
    val clienteId: String? = null,
    val tipoChave: TipoDeChave,
    val tipoConta: TipoDeConta,
    val chave: String,
    val conta: Conta,
    val titularConta: TitularConta,
    val criadoEm: LocalDateTime
) {

    companion object {
        fun de(chavePix: ChavePix): DetalheChavePix {
            return DetalheChavePix(
                pixId = chavePix.id,
                clienteId = chavePix.clienteId,
                tipoChave = TipoDeChave.valueOf(chavePix.tipoChave.toString()),
                tipoConta = TipoDeConta.valueOf(chavePix.conta.tipoConta.toString()),
                chave = chavePix.chave,
                conta = chavePix.conta,
                titularConta = chavePix.conta.titular,
                criadoEm = chavePix.createdAt
            )
        }
    }
}