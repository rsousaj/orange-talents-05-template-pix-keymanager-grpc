package br.com.zup.orangetalents.pix

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String> {

    fun existsByChave(chave: String) : Boolean

    @Query("select c from ChavePix c where c.id = :id AND c.conta.titular.id = :clientId")
    fun findByIdAndClientId(id: String, clientId: String) : Optional<ChavePix>

    fun findByChave(chave: String) : Optional<ChavePix>
}