package br.com.zup.orangetalents.integracao

import br.com.zup.orangetalents.pix.ChavePix
import br.com.zup.orangetalents.pix.Conta
import br.com.zup.orangetalents.pix.TipoChave
import br.com.zup.orangetalents.pix.TitularConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@ErrorHandlerBCB
@Client("\${integracao.bacen.url}")
interface BacenClient {

    @Post("/api/v1/pix/keys",
        consumes = [MediaType.APPLICATION_XML],
        produces = [MediaType.APPLICATION_XML]
        )
//    @Consumes(MediaType.APPLICATION_XML)
//    @Produces(MediaType.APPLICATION_XML)
    fun registraChavePix(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun removerChavePix(
        @PathVariable("key") chave: String,
        @Body request: DeletePixKeyRequest
    ): HttpResponse<DeletePixKeyResponse>

    fun teste() = "testando"

}

data class CreatePixKeyResponse(
    val keyType: String,
    val key: String,
    val bankAccount: CreatePixKeyContaResponse,
    val owner: CreatePixKeyTitularResponse,
    val createdAt: LocalDateTime
)

data class CreatePixKeyContaResponse(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: String,
)

data class CreatePixKeyTitularResponse(
    val type: String = "NATURAL_PERSON",
    val name: String,
    val taxIdNumber: String
)

data class CreatePixKeyRequest(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: CreatePixKeyContaRequest,
    val owner: CreatePixKeyTitularRequest
) {

    companion object {

        fun deChavePix(chavePix: ChavePix): CreatePixKeyRequest {
            return CreatePixKeyRequest(
                keyType = PixKeyType.of(chavePix.tipoChave),
                key = chavePix.chave,
                bankAccount = CreatePixKeyContaRequest.deConta(chavePix.conta),
                owner = CreatePixKeyTitularRequest.deTitular(chavePix.conta.titular)
            )
        }
    }
}

enum class PixKeyType(val tipoChave: TipoChave?) {
    CPF(TipoChave.CPF),
    CNPJ(null),
    PHONE(TipoChave.CELULAR),
    EMAIL(TipoChave.EMAIL),
    RANDOM(TipoChave.ALEATORIO);

    companion object {
        fun of(tipoChave: TipoChave?): PixKeyType {
            return PixKeyType
                .values()
                .associateBy(PixKeyType::tipoChave)
                .get(tipoChave) ?: throw IllegalStateException("Não foi possível encontrar referência para o Tipo de Chave: $tipoChave")
        }
    }
}

data class CreatePixKeyContaRequest(
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType,
) {
    val participant = "60701190"

    companion object {

        fun deConta(conta: Conta): CreatePixKeyContaRequest {
            return CreatePixKeyContaRequest(
                accountNumber = conta.numero,
                branch = conta.agencia,
                accountType = AccountType.convertAccountType(conta.tipoConta)
            )
        }
    }
}

data class CreatePixKeyTitularRequest(
    val type: String = "NATURAL_PERSON",
    val name: String,
    val taxIdNumber: String
) {
    companion object {

        fun deTitular(titular: TitularConta): CreatePixKeyTitularRequest {
            return CreatePixKeyTitularRequest(
                name = titular.nome,
                taxIdNumber = titular.cpf
            )
        }
    }
}

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

data class DeletePixKeyRequest(
    val key: String,
) {
    val participant = "60701190"
}

data class ProblemResponse(
    val type: String,
    val status: Int,
    val title: String,
    val detail: String,
//    val violations: List<Violation>
)

class Violation(
    val field: String,
    val message: String
)
