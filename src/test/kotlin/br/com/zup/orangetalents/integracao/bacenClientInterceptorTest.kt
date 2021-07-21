package br.com.zup.orangetalents.integracao

import br.com.zup.orangetalents.compartilhado.GrpcMethodExceptionInterceptor
import br.com.zup.orangetalents.compartilhado.exception.ChavePixExistenteException
import br.com.zup.orangetalents.compartilhado.exception.RemocaoNaoAutorizadaException
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.inject.ExecutableMethod
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class bacenClientInterceptorTest {
    @Mock
    lateinit var context: MethodInvocationContext<BacenClient, Any>

    val bacenCliInterceptor = BacenClientInterceptor()

    @BeforeEach
    fun setup() {
        Mockito.reset(context)
    }

    @Test
    fun `deve lancar ChavePixExistenteException se o status da execucao for 422`() {
        `when`(context.proceed()).thenThrow(HttpClientResponseException("", HttpResponse.unprocessableEntity<HttpStatus>()))

        assertThrows(ChavePixExistenteException::class.java) {
            bacenCliInterceptor.intercept(context)
        }
    }

    @Test
    fun `deve lancar RemocaoNaoAutorizadaException se o status da execucao for 403`() {
        `when`(context.proceed()).thenThrow(HttpClientResponseException("", HttpResponse.status<HttpStatus>(HttpStatus.FORBIDDEN)))

        assertThrows(RemocaoNaoAutorizadaException::class.java) {
            bacenCliInterceptor.intercept(context)
        }
    }
}