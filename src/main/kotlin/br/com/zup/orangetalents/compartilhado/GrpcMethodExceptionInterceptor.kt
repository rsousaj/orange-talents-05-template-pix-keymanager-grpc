package br.com.zup.orangetalents.compartilhado

import br.com.zup.orangetalents.compartilhado.exception.ChavePixExistenteException
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
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorHandler::class)
class GrpcMethodExceptionInterceptor : MethodInterceptor<Any, Any?> {

    override fun intercept(context: MethodInvocationContext<Any, Any?>): Any? {
        return try {
            context.proceed()
        } catch (ex: Exception) {
            ex.printStackTrace()

            val error: StatusRuntimeException = when (ex) {
                is ChavePixExistenteException -> Status.ALREADY_EXISTS.withCause(ex).withDescription(ex.message).asRuntimeException()
                is ConstraintViolationException -> constroiExcecaoArgumetosInvalidos(ex)
                is HttpClientException -> Status.UNAVAILABLE.withCause(ex).withDescription(ex.message).asRuntimeException()
                else -> Status.INTERNAL.withDescription(ex.message).asRuntimeException()
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
}