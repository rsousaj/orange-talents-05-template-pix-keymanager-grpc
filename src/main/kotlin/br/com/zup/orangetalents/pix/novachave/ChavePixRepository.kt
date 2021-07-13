package br.com.zup.orangetalents.pix.novachave

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, String> {

    fun existsByChave(chave: String) : Boolean
}