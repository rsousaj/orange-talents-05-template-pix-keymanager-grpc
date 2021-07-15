package br.com.zup.orangetalents.pix.removechave

import br.com.zup.orangetalents.KeyManagerRemoveGrpcServiceGrpc
import br.com.zup.orangetalents.RemoveChavePixRequest
import br.com.zup.orangetalents.RemoveChavePixResponse
import br.com.zup.orangetalents.compartilhado.ErrorHandler
import br.com.zup.orangetalents.compartilhado.exception.ChavePixNaoEncontradaException
import br.com.zup.orangetalents.pix.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class RemoveChaveEndpoint(
    private val service: RemoveChavePixService
) :
    KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceImplBase() {

    @ErrorHandler
    override fun removerChavePix(
        request: RemoveChavePixRequest,
        responseObserver: StreamObserver<RemoveChavePixResponse>?
    ) {
        service.remove(id = request.pixId, clientId = request.codigoCliente)

        responseObserver?.onNext(RemoveChavePixResponse.newBuilder()
            .setStatus(true)
            .build())
        responseObserver?.onCompleted()
    }
}