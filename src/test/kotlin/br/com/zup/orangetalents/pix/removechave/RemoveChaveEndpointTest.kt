package br.com.zup.orangetalents.pix.removechave

import br.com.zup.orangetalents.KeyManagerRemoveGrpcServiceGrpc
import br.com.zup.orangetalents.RemoveChavePixRequest
import br.com.zup.orangetalents.integracao.BacenClient
import br.com.zup.orangetalents.integracao.DeletePixKeyRequest
import br.com.zup.orangetalents.integracao.DeletePixKeyResponse
import br.com.zup.orangetalents.pix.ChavePix
import br.com.zup.orangetalents.pix.ChavePixRepository
import br.com.zup.orangetalents.pix.novachave.criaChavePix
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val grpcClient: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub
) {

    @Inject
    lateinit var bacenClient: BacenClient

    lateinit var chaveExistente: ChavePix

    @BeforeEach
    fun setUp() {
        chaveExistente = chavePixRepository.save(criaChavePix(
            clienteId = UUID.randomUUID().toString()
        ))
    }

    @AfterEach
    fun tearDown() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve remover uma chave pix existente`() {
        `when`(bacenClient.removerChavePix(chaveExistente.chave, DeletePixKeyRequest(chaveExistente.chave)))
            .thenReturn(HttpResponse.ok(DeletePixKeyResponse(key = chaveExistente.chave, deletedAt = LocalDateTime.now(), participant = "")))

        val response = grpcClient.removerChavePix(
            RemoveChavePixRequest.newBuilder()
                .setPixId(chaveExistente.id)
                .setCodigoCliente(chaveExistente.clienteId)
                .build()
        )

        val chaveDeletada = chavePixRepository.findById(chaveExistente.id)

        assertTrue(chaveDeletada.isEmpty)
        assertTrue(response.status)
    }

    @Test
    fun `deve remover uma chave pix mesmo que ela não tenha sido encontrada no Banco Central do Brasil`() {
        `when`(bacenClient.removerChavePix(chaveExistente.chave, DeletePixKeyRequest(chaveExistente.chave)))
            .thenReturn(HttpResponse.notFound())

        val response = grpcClient.removerChavePix(
            RemoveChavePixRequest.newBuilder()
                .setPixId(chaveExistente.id)
                .setCodigoCliente(chaveExistente.clienteId)
                .build()
        )

        val chaveDeletada = chavePixRepository.findById(chaveExistente.id)

        assertTrue(chaveDeletada.isEmpty)
        assertTrue(response.status)
    }

    @Test
    fun `nao deve remover uma chave pix inexistente`() {

        assertThrows(StatusRuntimeException::class.java) {
            grpcClient.removerChavePix(RemoveChavePixRequest.newBuilder()
                .setPixId(UUID.randomUUID().toString())
                .setCodigoCliente(chaveExistente.clienteId)
                .build())
        }.also {
            assertEquals(Status.Code.NOT_FOUND, it.status.code)
            assertEquals("NOT_FOUND: Não foi possível encontrar chave PIX com os dados informados.", it.message)
        }
    }

    @Test
    fun `nao deve remover uma chave pix se nao for requisitada pelo cliente dono`() {
        val outroCliente = UUID.randomUUID().toString()

        assertThrows(StatusRuntimeException::class.java) {
            grpcClient.removerChavePix(RemoveChavePixRequest.newBuilder()
                .setPixId(chaveExistente.id)
                .setCodigoCliente(outroCliente)
                .build())
        }.also {
            assertEquals(Status.Code.NOT_FOUND, it.status.code)
        }

        val chaveExiste = chavePixRepository.existsById(chaveExistente.id)
        assertTrue(chaveExiste)
    }

    @MockBean(BacenClient::class)
    fun bacenClient(): BacenClient = mock(BacenClient::class.java)

    @Factory
    class clientFactory {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub {
            return KeyManagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}