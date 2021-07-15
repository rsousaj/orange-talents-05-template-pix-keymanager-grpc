package br.com.zup.orangetalents.compartilhado

import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInvocationContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class GrpcMethodExceptionInterceptorTest {
    @Mock
    lateinit var context: MethodInvocationContext<Any, Any>

    @Mock
    lateinit var streamObserver: StreamObserver<*>

    val grpcMethodInterceptor = GrpcMethodExceptionInterceptor()

    @Test
    fun `deve retornar Status INTERNAL se ocorrer algum erro desconhecido`() {
        `when`(context.proceed()).thenThrow(RuntimeException::class.java)
        `when`(context.parameterValues).thenReturn(arrayOf(null, streamObserver))

        grpcMethodInterceptor.intercept(context)

        verify(streamObserver).onError(any(StatusRuntimeException::class.java))
    }
}