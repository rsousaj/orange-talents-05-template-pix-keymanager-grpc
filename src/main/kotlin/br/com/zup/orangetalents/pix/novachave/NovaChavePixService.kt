package br.com.zup.orangetalents.pix.novachave

import br.com.zup.orangetalents.ConsultaContaResponse
import br.com.zup.orangetalents.ContasDeClientesItauClient
import br.com.zup.orangetalents.compartilhado.exception.ChavePixExistenteException
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class NovaChavePixService(
    val itauClient: ContasDeClientesItauClient,
    val chavePixRepository: ChavePixRepository
) {

    fun registra(@Valid novaChavePix: NovaChavePix): ChavePix {
        // verificar se já existe
        if (chavePixRepository.existsByChave(novaChavePix.chave))
            throw ChavePixExistenteException("A chave escolhida (${novaChavePix.chave}) já existe!")

        // consultar o servico externo do banco
        val contaResponse: ConsultaContaResponse? = itauClient
            .consultaContaPorClienteETipo(
                novaChavePix.codigoCliente.toString(),
                novaChavePix.tipoConta.name
            )
        val conta = contaResponse?.paraConta() ?: throw IllegalStateException("A conta informada não foi encontrada.")

        return novaChavePix.paraModelo(conta).let { chavePixRepository.save(it) }
    }
}
