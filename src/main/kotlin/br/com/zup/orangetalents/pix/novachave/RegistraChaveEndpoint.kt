package br.com.zup.orangetalents.pix.novachave

import br.com.zup.orangetalents.*
import br.com.zup.orangetalents.compartilhado.ErrorHandler
import br.com.zup.orangetalents.pix.TipoChave
import br.com.zup.orangetalents.pix.TipoConta
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Singleton

@Singleton
class RegistraChaveEndpoint(
    private val novaChavePixService: NovaChavePixService
) : KeyManagerRegisterGrpcServiceGrpc.KeyManagerRegisterGrpcServiceImplBase() {

    @ErrorHandler
    override fun registraChavePix(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>?) {
        // faz o binding de ChavePixRequest para NovaChavePix com o objetivo de utilizar a BeanValidation
        val novaChavePix = request.paraChave()

        // registra a chave através do NovaChavePixService
        val chavePixCriada = novaChavePixService.registra(novaChavePix)

        // finaliza o processamento com o retorno do ChavePixResponse
        responseObserver?.onNext(
            ChavePixResponse.newBuilder()
                .setPixId(chavePixCriada.id)
                .build()
        )
        responseObserver?.onCompleted()
    }
}

fun ChavePixRequest.paraChave(): NovaChavePix {
    return NovaChavePix(
        codigoCliente = this.codigoCliente,
        tipoChave = when (this.tipoChave) {
            TipoDeChave.TIPO_CHAVE_DESCONHECIDA -> null
            else -> TipoChave.valueOf(this.tipoChave.name)
        },
        chave = this.chave,
        tipoConta = when (this.tipoConta) {
            TipoDeConta.TIPO_CONTA_DESCONHECIDO -> null
            else -> TipoConta.valueOf(this.tipoConta.name)
        }
    )
}