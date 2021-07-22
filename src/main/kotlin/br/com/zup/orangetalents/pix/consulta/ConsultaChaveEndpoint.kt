package br.com.zup.orangetalents.pix.consulta

import br.com.zup.orangetalents.ConsultarChavePixRequest
import br.com.zup.orangetalents.ConsultarChavePixRequest.FiltroCase.*
import br.com.zup.orangetalents.ConsultarChavePixResponse
import br.com.zup.orangetalents.KeyManagerConsultGrpcServiceGrpc
import br.com.zup.orangetalents.compartilhado.ErrorHandler
import br.com.zup.orangetalents.integracao.BacenClient
import br.com.zup.orangetalents.pix.ChavePixRepository
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ConsultaChaveEndpoint(
    private val chavePixRepository: ChavePixRepository,
    private val bacenClient: BacenClient,
    @Inject val validator: Validator
) : KeyManagerConsultGrpcServiceGrpc.KeyManagerConsultGrpcServiceImplBase() {

    @ErrorHandler
    override fun consultarChavePix(
        request: ConsultarChavePixRequest,
        responseObserver: StreamObserver<ConsultarChavePixResponse>
    ) {

        val filtro = request.paraFiltro(validator)
        val detalheChavePix = filtro.executa(chavePixRepository, bacenClient)

        responseObserver.onNext(ConversorDetalhaChavePixResponse.converte(detalheChavePix))
        responseObserver.onCompleted()
    }
}

fun ConsultarChavePixRequest.paraFiltro(validator: Validator): FiltroConsulta {
    val filtro = when (filtroCase) {
        PIXID -> FiltroConsulta.FiltroPixId(pixId = pixId.pixId, clienteId = pixId.clienteId)
        CHAVE -> FiltroConsulta.FiltroChave(chave)
        FILTRO_NOT_SET -> throw IllegalArgumentException("Parâmetros informados são inválidos")
    }

    val validations = validator.validate(filtro)
    if (validations.isNotEmpty()) {
        throw ConstraintViolationException(validations)
    }

    return filtro
}