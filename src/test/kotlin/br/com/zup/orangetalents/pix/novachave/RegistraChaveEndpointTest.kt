package br.com.zup.orangetalents.pix.novachave

import br.com.zup.orangetalents.*
import br.com.zup.orangetalents.pix.ChavePix
import br.com.zup.orangetalents.pix.ChavePixRepository
import com.google.rpc.BadRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    @Inject private val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    @Inject private val chavePixRepository: ChavePixRepository
) {

    @Inject
    lateinit var itauClient: ContasDeClientesItauClient

    @BeforeEach
    fun setUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve registrar chave pix`() {
        val chavePixRequest = criaChavePixRequest()

        val consultaContaResponse = ConsultaContaResponse(
            chavePixRequest.tipoConta.toString(),
            "agencia",
            "numero",
            TitularContaResponse("1", "nome", "99089748075")
        )

        `when`(itauClient.consultaContaPorClienteETipo(chavePixRequest.codigoCliente, chavePixRequest.tipoConta.toString()))
            .thenReturn(consultaContaResponse)

        val chavePixResponse = grpcClient.registraChavePix(chavePixRequest)

        assertTrue(chavePixResponse.pixId.isNotBlank())
    }

    @Test
    fun `nao deve registrar se chave já existir`() {
        val chaveExistente = "email@teste.com"
        val chavePixCadastrada = criaChavePix(chave = chaveExistente)
        chavePixRepository.save(chavePixCadastrada)

        val chavePixRequest = criaChavePixRequest(chave = chaveExistente)

        assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registraChavePix(chavePixRequest)
        }.also {
            assertEquals(Status.Code.ALREADY_EXISTS, it.status.code)
            assertEquals("A chave escolhida (${chaveExistente}) já existe!", it.status.description)
        }
    }

    @Test
    fun `nao deve registrar se conta não for localizada`() {
        val chavePixRequest = criaChavePixRequest()

        `when`(
            itauClient.consultaContaPorClienteETipo(
                chavePixRequest.codigoCliente,
                chavePixRequest.tipoConta.toString()
            )
        ).thenReturn(null)

        assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registraChavePix(chavePixRequest)
        }.also {
            assertEquals(Status.Code.FAILED_PRECONDITION, it.status.code)
            assertEquals("A conta informada não foi encontrada.", it.status.description)
        }
    }

    @Test
    fun `nao deve registrar se codigo UUID invalido`() {
        val chavePixRequest = criaChavePixRequest(codigoCliente = "codigo-invalido")

        assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registraChavePix(chavePixRequest)
        }.also {
            val statusProto = StatusProto.fromThrowable(it)
            val violationsList = statusProto?.let { status ->
                status
                    .detailsList[0]
                    .unpack(BadRequest::class.java)
                    .fieldViolationsList
            }

            assertEquals(1, violationsList?.size)
            assertEquals("INVALID_ARGUMENT: Requisição com parâmetros inválidos", it.message)
        }
    }

    @Test
    fun `deve converter parametro para DTO com dados inválidos se TipoChave e TipoConta nao informados`() {
        val chavePixRequest = criaChavePixRequest(tipoChave = TipoDeChave.TIPO_CHAVE_DESCONHECIDA, tipoConta = TipoDeConta.TIPO_CONTA_DESCONHECIDO)

        val novaChavePix = chavePixRequest.paraChave()

        assertNull(novaChavePix.tipoConta)
        assertNull(novaChavePix.tipoChave)
    }

    @Factory
    class clientGrpcFactory() {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub {
            return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ContasDeClientesItauClient::class)
    fun itauClient(): ContasDeClientesItauClient = mock(ContasDeClientesItauClient::class.java)

    private fun criaChavePixRequest(
        chave: String = "email@email.com",
        codigoCliente: String = "7f572b33-8ef4-493f-8ac0-9c2b181cf9a9",
        tipoChave: TipoDeChave = TipoDeChave.EMAIL,
        tipoConta: TipoDeConta = TipoDeConta.CONTA_CORRENTE
    ): ChavePixRequest {

        return ChavePixRequest.newBuilder()
            .setChave(chave)
            .setCodigoCliente(codigoCliente)
            .setTipoChave(tipoChave)
            .setTipoConta(tipoConta)
            .build()
    }

    private fun criaChavePix(chave: String = "email@email.com") : ChavePix {
        return ChavePix(
            TipoChave.EMAIL.toString(),
            chave,
            Conta(TipoConta.CONTA_CORRENTE.toString(), "numero", "agencia", TitularConta("1", "nome", "99089748075"))
        )
    }
}