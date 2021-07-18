package br.com.zup.orangetalents.pix.removechave

import br.com.zup.orangetalents.RemoveChavePixResponse
import br.com.zup.orangetalents.compartilhado.exception.ChavePixNaoEncontradaException
import br.com.zup.orangetalents.integracao.BacenClient
import br.com.zup.orangetalents.integracao.DeletePixKeyRequest
import br.com.zup.orangetalents.pix.ChavePixRepository
import br.com.zup.orangetalents.pix.novachave.ValidoUUID
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Singleton
@Validated
class RemoveChavePixService(
    private val chavePixRepository: ChavePixRepository,
    private val bacenClient: BacenClient
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun remove(
        @NotBlank @ValidoUUID id: String,
        @NotBlank @ValidoUUID clientId: String
    ) {
        val chaveEncontrada = chavePixRepository.findByIdAndClientId(id, clientId)
            .orElseThrow { ChavePixNaoEncontradaException("Não foi possível encontrar chave PIX com os dados informados.") }

        chavePixRepository.delete(chaveEncontrada)

        logger.info("Tentativa de remover chave (ID: ${chaveEncontrada.id}) do BACEN")
        bacenClient.removerChavePix(chaveEncontrada.chave, DeletePixKeyRequest(key = chaveEncontrada.chave))

        logger.info("Chave pix com ID ${chaveEncontrada.id} removida com sucesso!")
    }
}