package br.com.zup.orangetalents.pix.removechave

import br.com.zup.orangetalents.KeyManagerRegisterGrpcServiceGrpc
import br.com.zup.orangetalents.KeyManagerRemoveGrpcServiceGrpc
import br.com.zup.orangetalents.RemoveChavePixRequest
import br.com.zup.orangetalents.pix.ChavePixRepository
import br.com.zup.orangetalents.pix.novachave.criaChavePix
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val grpcClient: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub
) {

    @BeforeEach
    fun setUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve remover uma chave pix existente`() {
        val clienteId = UUID.randomUUID().toString()
        val chavePixCriada = chavePixRepository.save(criaChavePix(clienteId = clienteId))

        grpcClient.removerChavePix(RemoveChavePixRequest.newBuilder()
            .setPixId(chavePixCriada.id)
            .setCodigoCliente(clienteId)
            .build())

        val chaveDeletada = chavePixRepository.findById(chavePixCriada.id)

        assertTrue(chaveDeletada.isEmpty)
    }

    @Test
    fun `nao deve remover uma chave pix se nao for requisitada pelo cliente dono`() {
        val clienteDonoChave = UUID.randomUUID().toString()
        val outroCliente = UUID.randomUUID().toString()
        val chavePixCriada = chavePixRepository.save(criaChavePix(clienteId = clienteDonoChave))

        assertThrows(StatusRuntimeException::class.java) {
            grpcClient.removerChavePix(RemoveChavePixRequest.newBuilder()
                .setPixId(chavePixCriada.id)
                .setCodigoCliente(outroCliente)
                .build())
        }.also {
            assertEquals(Status.Code.NOT_FOUND, it.status.code)
        }

        val chaveExiste = chavePixRepository.existsById(chavePixCriada.id)
        assertTrue(chaveExiste)
    }

    @Factory
    class clientFactory {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub {
            return KeyManagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}