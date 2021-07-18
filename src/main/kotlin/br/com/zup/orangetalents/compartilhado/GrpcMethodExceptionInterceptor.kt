package br.com.zup.orangetalents.compartilhado

import br.com.zup.orangetalents.compartilhado.exception.ChavePixExistenteException
import br.com.zup.orangetalents.compartilhado.exception.ChavePixNaoEncontradaException
import com.google.rpc.BadRequest
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorHandler::class)
class GrpcMethodExceptionInterceptor : MethodInterceptor<Any, Any?> {

    val logger = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<Any, Any?>): Any? {
        return try {
            context.proceed()
        } catch (ex: Exception) {
            logger.info("Ocorreu o seguinte erro ao tentar executar o metodo ${context.executableMethod.methodName}: ${ex.message}")

            val error: StatusRuntimeException = when (ex) {
                is ChavePixExistenteException -> Status.ALREADY_EXISTS.withCause(ex).withDescription(ex.message).asRuntimeException()
                is ChavePixNaoEncontradaException ->  asRunTimeException(Status.NOT_FOUND, ex)
                is ConstraintViolationException -> constroiExcecaoArgumetosInvalidos(ex)
                is HttpClientResponseException -> asRunTimeException(Status.UNAVAILABLE, ex)
                is IllegalStateException -> asRunTimeException(Status.FAILED_PRECONDITION, ex)
                else -> asRunTimeException(Status.INTERNAL, ex)
            }

            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(error)
            null
        }
    }

    private fun constroiExcecaoArgumetosInvalidos(ex: ConstraintViolationException): StatusRuntimeException {
        val badRequestDetails = BadRequest.newBuilder().addAllFieldViolations(ex.constraintViolations.map {
            BadRequest.FieldViolation.newBuilder()
                .setField(it.propertyPath.last().name ?: "")
                .setDescription(it.message)
                .build()
        }).build()

        val statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage("Requisição com parâmetros inválidos")
            .addDetails(com.google.protobuf.Any.pack(badRequestDetails))
            .build()

        return StatusProto.toStatusRuntimeException(statusProto)
    }

    private fun asRunTimeException(status: Status, ex: Exception): StatusRuntimeException {
        return status.withDescription(ex.message).asRuntimeException()
    }
}