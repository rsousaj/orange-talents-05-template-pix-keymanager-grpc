package br.com.zup.orangetalents.pix.consulta

import br.com.zup.orangetalents.compartilhado.exception.ChavePixNaoEncontradaException
import br.com.zup.orangetalents.integracao.BacenClient
import br.com.zup.orangetalents.integracao.PixKeyDetailsResponse
import br.com.zup.orangetalents.pix.ChavePixRepository
import br.com.zup.orangetalents.pix.novachave.ValidoUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class FiltroConsulta {

    abstract fun executa(chavePixRepository: ChavePixRepository, bacenClient: BacenClient): DetalheChavePix

    @Introspected
    data class FiltroPixId(
        @field:NotBlank
        @field:ValidoUUID
        val clienteId: String,

        @field:NotBlank
        @field:ValidoUUID
        val pixId: String
    ) : FiltroConsulta() {

        override fun executa(chavePixRepository: ChavePixRepository, bacenClient: BacenClient): DetalheChavePix {
            return chavePixRepository
                .findByIdAndClientId(id = pixId, clientId = clienteId)
                .filter { it.isSincronizadaBacen() }
                .map(DetalheChavePix::de)
                .orElseThrow { ChavePixNaoEncontradaException("A chave informada não foi localizada ou encontra-se em processamento.") }
        }
    }

    @Introspected
    data class FiltroChave(
        @field:NotBlank
        @Size(max = 77)
        val chave: String
    ) : FiltroConsulta() {

        override fun executa(chavePixRepository: ChavePixRepository, bacenClient: BacenClient): DetalheChavePix {
            return chavePixRepository
                .findByChave(chave)
                .map(DetalheChavePix::de)
                .orElseGet {
                    val consultaChavePix: HttpResponse<PixKeyDetailsResponse> = bacenClient.consultaChavePix(chave)

                    when (consultaChavePix.status) {
                        HttpStatus.OK -> consultaChavePix.body()?.paraChavePix()?.let { DetalheChavePix.de(it) }
                        else -> throw ChavePixNaoEncontradaException("A chave informada não foi localizada.")
                    }
                }
        }
    }
}