package br.com.zup.orangetalents.pix

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(name = "chave_pix")
class ChavePix(
    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave?,

    @field:NotBlank
    @Column(nullable = false)
    var chave: String,

    @Embedded
    @Valid
    @field:NotNull
    val conta: Conta,
) {

    @Id
    val id: String = UUID.randomUUID().toString()

    var createdAt = LocalDateTime.now()

    val clienteId
        get() = conta.titular.id

    @Enumerated(EnumType.STRING)
    var status = ChavePixStatus.SINCRONIZADA_BCB

    fun atualiza(chave: String) {
        if (isAleatoria()) {
            this.chave = chave
        }
    }

    fun isAleatoria(): Boolean {
        return tipoChave == TipoChave.ALEATORIO
    }

    fun isSincronizadaBacen(): Boolean {
        return status == ChavePixStatus.SINCRONIZADA_BCB
    }
}

enum class ChavePixStatus {
    EM_PROCESSAMENTO, SINCRONIZADA_BCB
}