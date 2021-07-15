package br.com.zup.orangetalents.pix

import org.hibernate.validator.constraints.br.CPF
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Embeddable
class Conta(
    @field:NotBlank
    @Column(nullable = false)
    val tipoConta: String,

    @field:NotBlank
    @Column(nullable = false)
    val numero: String,

    @field:NotBlank
    @Column(nullable = false)
    val agencia: String,

    @Embedded
    @Valid
    @field:NotNull
    val titular: TitularConta
)

@Embeddable
class TitularConta(
    @field:NotBlank
    @Column(name = "id_titular", nullable = false)
    val id: String,

    @field:NotBlank
    @Column(name = "nome_titular", nullable = false)
    val nome: String,

    @field:NotBlank @field:CPF
    @Column(name = "cpf_titular", nullable = false)
    val cpf: String
)