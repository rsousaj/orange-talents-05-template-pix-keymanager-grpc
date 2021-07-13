package br.com.zup.orangetalents

import br.com.zup.orangetalents.pix.novachave.Conta
import br.com.zup.orangetalents.pix.novachave.TitularConta
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:9091")
interface ContasDeClientesItauClient {

    @Get(value = "/api/v1/clientes/{clienteId}/contas")
    fun consultaContaPorClienteETipo(
        @PathVariable clienteId: String,
        @QueryValue(value = "tipo") tipoConta: String
    ) : ConsultaContaResponse?
}

data class ConsultaContaResponse(
    val tipo: String,
    val agencia: String,
    val numero: String,
    val titular: TitularContaResponse
) {
    fun paraConta(): Conta {
        return Conta(tipo, numero, agencia, TitularConta(titular.id, titular.nome, titular.cpf))
    }
}

data class TitularContaResponse(
    val id: String,
    val nome: String,
    val cpf: String
)