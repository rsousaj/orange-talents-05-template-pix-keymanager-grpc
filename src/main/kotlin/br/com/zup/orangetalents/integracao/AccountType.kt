package br.com.zup.orangetalents.integracao

import br.com.zup.orangetalents.pix.TipoConta
import java.lang.IllegalStateException

enum class AccountType {
    CACC, SVGS;

    companion object {
        val mapToAccountType = mapOf(
            Pair(TipoConta.CONTA_CORRENTE, CACC),
            Pair(TipoConta.CONTA_POUPANCA, SVGS))

        fun convertAccountType(tipoConta: TipoConta): AccountType {
            return mapToAccountType[tipoConta] ?: throw IllegalStateException("Não foi possível converter conta ")
        }
    }
}
