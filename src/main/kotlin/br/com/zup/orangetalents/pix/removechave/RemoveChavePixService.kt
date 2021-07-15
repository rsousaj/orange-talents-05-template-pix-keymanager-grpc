package br.com.zup.orangetalents.pix.removechave

import br.com.zup.orangetalents.RemoveChavePixResponse
import br.com.zup.orangetalents.compartilhado.exception.ChavePixNaoEncontradaException
import br.com.zup.orangetalents.pix.ChavePixRepository
import br.com.zup.orangetalents.pix.novachave.ValidoUUID
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Singleton
@Validated
class RemoveChavePixService(
    private val chavePixRepository: ChavePixRepository
) {

    fun remove(
        @NotBlank @ValidoUUID id: String,
        @NotBlank @ValidoUUID clientId: String
    ) {
        val chaveEncontrada = chavePixRepository.findByIdAndClientId(id, clientId)
            .orElseThrow { ChavePixNaoEncontradaException("Não foi possível encontrar chave PIX com os dados informados.") }

        chavePixRepository.delete(chaveEncontrada)
    }
}