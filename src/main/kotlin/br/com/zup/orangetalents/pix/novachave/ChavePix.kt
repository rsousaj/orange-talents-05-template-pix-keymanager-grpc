package br.com.zup.orangetalents.pix.novachave

import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(name = "chave_pix")
class ChavePix(
    @field:NotBlank
    @Column(nullable = false)
    val tipoChave: String,

    @field:NotBlank
    @Column(nullable = false)
    val chave: String,

    @Embedded
    @Valid
    @field:NotNull
    val conta: Conta,
) {

    @Id
    val id: String = UUID.randomUUID().toString()
}