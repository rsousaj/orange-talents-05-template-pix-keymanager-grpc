package br.com.zup.orangetalents.pix.novachave

import br.com.zup.orangetalents.ChavePixRequest
import br.com.zup.orangetalents.KeyManagerRegisterGrpcServiceGrpc
import br.com.zup.orangetalents.TipoDeChave
import br.com.zup.orangetalents.TipoDeConta
import br.com.zup.orangetalents.compartilhado.exception.ChavePixExistenteException
import br.com.zup.orangetalents.integracao.*
import br.com.zup.orangetalents.pix.*
import com.google.rpc.BadRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.*
import java.time.LocalDateTime
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    @Inject private val grpcClient: KeyManagerRegisterGrpcServiceGrpc.KeyManagerRegisterGrpcServiceBlockingStub,
    @Inject private val chavePixRepository: ChavePixRepository,
) {

    @Inject
    lateinit var itauClient: ContasDeClientesItauClient

    @Inject
    lateinit var bacenClient: BacenClient

    @BeforeEach
    fun setUp() {
        chavePixRepository.deleteAll()
    }

    val chavePixTest = criaChavePix()

    val chavePixRequestTest = criaChavePixRequest(
        chave = chavePixTest.chave,
        codigoCliente = chavePixTest.clienteId,
        tipoChave = TipoDeChave.EMAIL,
        tipoConta = TipoDeConta.CONTA_CORRENTE
    )

    val createPixKeyRequestTest = criaCreatePixKeyRequest(
        pixKeyType = PixKeyType.of(chavePixTest.tipoChave),
        chave = chavePixTest.chave,
        agencia = chavePixTest.conta.agencia,
        conta = chavePixTest.conta.numero,
        tipoConta = AccountType.convertAccountType(chavePixTest.conta.tipoConta),
        nomeTitular = chavePixTest.conta.titular.nome,
        cpfTitular = chavePixTest.conta.titular.cpf
    )

    val createPixKeyResponseTest = criaCreatePixKeyResponse(
        pixKeyType = PixKeyType.of(chavePixTest.tipoChave),
        chave = chavePixTest.chave,
        agencia = chavePixTest.conta.agencia,
        conta = chavePixTest.conta.numero,
        tipoConta = AccountType.convertAccountType(chavePixTest.conta.tipoConta),
        nomeTitular = chavePixTest.conta.titular.nome,
        cpfTitular = chavePixTest.conta.titular.cpf
    )

    @Test
    fun `deve registrar chave pix`() {
        val consultaContaResponse = ConsultaContaResponse(
            chavePixRequestTest.tipoConta.toString(),
            chavePixTest.conta.agencia,
            chavePixTest.conta.numero,
            TitularContaResponse(
                chavePixTest.clienteId,
                chavePixTest.conta.titular.nome,
                chavePixTest.conta.titular.cpf
            )
        )

        `when`(
            itauClient.consultaContaPorClienteETipo(
                chavePixRequestTest.codigoCliente,
                chavePixRequestTest.tipoConta.toString()
            )
        )
            .thenReturn(consultaContaResponse)

        `when`(bacenClient.registraChavePix(createPixKeyRequestTest))
            .thenReturn(HttpResponse.created(createPixKeyResponseTest))

        val chavePixResponse = grpcClient.registraChavePix(chavePixRequestTest)
        val chavePixCadastrada = chavePixRepository.findById(chavePixResponse.pixId)

        assertTrue(chavePixResponse.pixId.isNotBlank())
        assertTrue(chavePixCadastrada.isPresent)
    }

    @Test
    fun `nao deve registrar se chave já existir no Banco`() {
        val chaveExistente = "email@teste.com"
        val chavePixCadastrada = chavePixTest.apply { chave = chaveExistente }
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
    fun `nao deve registrar se chave já existir no Banco Central`() {
        val consultaContaResponse = ConsultaContaResponse(
            chavePixRequestTest.tipoConta.toString(),
            chavePixTest.conta.agencia,
            chavePixTest.conta.numero,
            TitularContaResponse(
                chavePixTest.clienteId,
                chavePixTest.conta.titular.nome,
                chavePixTest.conta.titular.cpf
            )
        )

        `when`(
            itauClient.consultaContaPorClienteETipo(
                chavePixRequestTest.codigoCliente,
                chavePixRequestTest.tipoConta.toString()
            )
        )
            .thenReturn(consultaContaResponse)

        `when`(bacenClient.registraChavePix(createPixKeyRequestTest))
            .thenThrow(ChavePixExistenteException("A chave pix informada já existe no Banco Central do Brasil."))

        assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registraChavePix(chavePixRequestTest)
        }.also {
            val chaveFoiCadastrada = chavePixRepository.existsByChave(chavePixRequestTest.chave)

            assertFalse(chaveFoiCadastrada)
            assertEquals(Status.Code.ALREADY_EXISTS, it.status.code)
            assertEquals("A chave pix informada já existe no Banco Central do Brasil.", it.status.description)
        }
    }

    @Test
    fun `nao deve registrar se conta não for localizada`() {
        `when`(
            itauClient.consultaContaPorClienteETipo(
                chavePixRequestTest.codigoCliente,
                chavePixRequestTest.tipoConta.toString()
            )
        ).thenReturn(null)

        assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registraChavePix(chavePixRequestTest)
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

            assertEquals(1, statusProto?.violations()?.size)
            assertEquals("INVALID_ARGUMENT: Requisição com parâmetros inválidos", it.message)
        }
    }

    @Test
    fun `deve converter parametro para DTO com dados inválidos se TipoChave e TipoConta nao informados`() {
        val chavePixRequest = criaChavePixRequest(
            tipoChave = TipoDeChave.TIPO_CHAVE_DESCONHECIDA,
            tipoConta = TipoDeConta.TIPO_CONTA_DESCONHECIDO
        )

        val novaChavePix = chavePixRequest.paraChave()

        assertNull(novaChavePix.tipoConta)
        assertNull(novaChavePix.tipoChave)
    }

    @Factory
    class clientGrpcFactory() {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRegisterGrpcServiceGrpc.KeyManagerRegisterGrpcServiceBlockingStub {
            return KeyManagerRegisterGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ContasDeClientesItauClient::class)
    fun itauClient(): ContasDeClientesItauClient = mock(ContasDeClientesItauClient::class.java)

    @MockBean(BacenClient::class)
    fun bacenClient(): BacenClient = mock(BacenClient::class.java)

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
}

internal fun criaChavePix(
    chave: String = "email@email.com",
    clienteId: String = "b0a9fde7-ce93-4a5c-8485-d93c98f0e0a2"
): ChavePix {
    return ChavePix(
        TipoChave.EMAIL,
        chave,
        Conta(TipoConta.CONTA_CORRENTE, "1234", "456789", TitularConta(clienteId, "nome", "99089748075"))
    )
}

internal fun criaCreatePixKeyRequest(
    pixKeyType: PixKeyType = PixKeyType.EMAIL,
    chave: String = "email@email.com",
    agencia: String = "1234",
    conta: String = "456789",
    tipoConta: AccountType = AccountType.CACC,
    nomeTitular: String = "Nome do Titular",
    cpfTitular: String = "99089748075"
): CreatePixKeyRequest {
    return CreatePixKeyRequest(
        pixKeyType,
        chave,
        CreatePixKeyContaRequest(
            agencia,
            conta,
            tipoConta
        ),
        CreatePixKeyTitularRequest(
            name = nomeTitular,
            taxIdNumber = cpfTitular
        )
    )
}

internal fun criaCreatePixKeyResponse(
    pixKeyType: PixKeyType,
    chave: String,
    agencia: String,
    conta: String,
    tipoConta: AccountType,
    nomeTitular: String,
    cpfTitular: String
): CreatePixKeyResponse {
    return CreatePixKeyResponse(
        pixKeyType.toString(),
        chave,
        ContaResponse(
            "",
            agencia,
            conta,
            tipoConta.toString()
        ),
        TitularResponse(
            name = nomeTitular,
            taxIdNumber = cpfTitular
        ),
        LocalDateTime.now()
    )
}

internal fun com.google.rpc.Status.violations(): MutableList<*> {
    return this.detailsList[0].unpack(BadRequest::class.java).fieldViolationsList
}