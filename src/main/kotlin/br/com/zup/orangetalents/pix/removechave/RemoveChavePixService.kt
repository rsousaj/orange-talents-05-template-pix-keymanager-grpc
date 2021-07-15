package br.com.zup.orangetalents.pix.removechave

import br.com.zup.orangetalents.RemoveChavePixResponse
import br.com.zup.orangetalents.compartilhado.exception.ChavePixNaoEncontradaException
import br.com.zup.orangetalents.pix.ChavePixRepository
import br.com.zup.orangetalents.pix.novachave.ValidoUUID
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class RemoveChavePixService(
    private val chavePixRepository: ChavePixRepository
) {

    fun remove(@ValidoUUID id: String, @ValidoUUID clientId: String) {
        val chaveEncontrada = chavePixRepository.findByIdAndClientId(id, clientId)
            .orElseThrow { ChavePixNaoEncontradaException("Não foi possível encontrar chave PIX com os dados informados.") }

        chavePixRepository.delete(chaveEncontrada)
    }
}