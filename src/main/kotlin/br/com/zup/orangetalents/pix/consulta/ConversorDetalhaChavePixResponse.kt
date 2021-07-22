package br.com.zup.orangetalents.pix.consulta

import br.com.zup.orangetalents.ConsultarChavePixResponse
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

class ConversorDetalhaChavePixResponse {

    companion object {
        fun converte(detalheChavePix: DetalheChavePix): ConsultarChavePixResponse {
            return detalheChavePix.let {
                ConsultarChavePixResponse.newBuilder()
                    .setClienteId(it.clienteId)
                    .setPixId(it.pixId)
                    .setChave(
                        ConsultarChavePixResponse.ChavePixGrpc.newBuilder()
                            .setChave(it.chave)
                            .setTipo(it.tipoChave)
                            .setConta(ConsultarChavePixResponse.ChavePixGrpc.ContaInfo.newBuilder()
                                .setTipo(it.tipoConta)
                                .setNomeDoTitular(it.titularConta.nome)
                                .setCpfDoTitular(it.titularConta.cpf)
                                .setAgencia(it.conta.agencia)
                                .setNumeroDaConta(it.conta.numero)
                                .build()
                            )
                            .setCriadaEm(it.criadoEm.toGrpcTimestamp())
                            .build()
                    )
                    .build()
            }
        }
    }
}

internal fun LocalDateTime.toGrpcTimestamp(): Timestamp {
    val instant = this.atZone(ZoneId.of("UTC")).toInstant()
    return Timestamp.newBuilder()
        .setSeconds(instant.epochSecond)
        .setNanos(instant.nano)
        .build()
}