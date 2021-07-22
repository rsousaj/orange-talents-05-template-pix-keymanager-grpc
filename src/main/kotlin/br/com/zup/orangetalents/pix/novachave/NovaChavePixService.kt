package br.com.zup.orangetalents.pix.novachave

import br.com.zup.orangetalents.compartilhado.exception.ChavePixExistenteException
import br.com.zup.orangetalents.integracao.BacenClient
import br.com.zup.orangetalents.integracao.ConsultaContaResponse
import br.com.zup.orangetalents.integracao.ContasDeClientesItauClient
import br.com.zup.orangetalents.integracao.CreatePixKeyRequest
import br.com.zup.orangetalents.pix.ChavePix
import br.com.zup.orangetalents.pix.ChavePixRepository
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneId
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class NovaChavePixService(
    private val itauClient: ContasDeClientesItauClient,
    private val bacenClient: BacenClient,
    private val chavePixRepository: ChavePixRepository
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix): ChavePix {
        if (chavePixRepository.existsByChave(novaChavePix.chave))
            throw ChavePixExistenteException("A chave escolhida (${novaChavePix.chave}) já existe!")

        logger.info("Consultando conta no ERP ITAU para o cliente ${novaChavePix.codigoCliente}")
        val contaResponse: ConsultaContaResponse? = itauClient
            .consultaContaPorClienteETipo(
                novaChavePix.codigoCliente,
                novaChavePix.tipoConta!!.name
            )
        val conta = contaResponse?.paraConta() ?: throw IllegalStateException("A conta informada não foi encontrada.")

        val chavePix = novaChavePix.paraModelo(conta).let { chavePixRepository.save(it) }

        logger.info("Registrando chave Pix (ID: ${chavePix.id}) no sistema do BACEN")
        bacenClient
            .registraChavePix(CreatePixKeyRequest.deChavePix(chavePix))
            .body()
            ?.let {
                chavePix.createdAt = it.createdAt.atZone(ZoneId.of("UTC")).toLocalDateTime()
                chavePix.atualiza(it.key)
            }

        return chavePix
    }
}
