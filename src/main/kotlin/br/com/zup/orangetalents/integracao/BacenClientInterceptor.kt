package br.com.zup.orangetalents.integracao

import br.com.zup.orangetalents.compartilhado.exception.ChavePixExistenteException
import br.com.zup.orangetalents.compartilhado.exception.RemocaoNaoAutorizadaException
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

@Singleton
@InterceptorBean(ErrorHandlerBCB::class)
class bacenClientInterceptor : MethodInterceptor<BacenClient, Any> {

    override fun intercept(context: MethodInvocationContext<BacenClient, Any>): Any? {
        try {
            return context.proceed()
        } catch (ex: HttpClientResponseException) {
            when (ex.status) {
                HttpStatus.UNPROCESSABLE_ENTITY -> throw ChavePixExistenteException("A chave pix informada já existe no Banco Central do Brasil.")
                HttpStatus.FORBIDDEN -> throw RemocaoNaoAutorizadaException("Sem autorização para remover a chave no Banco Central.")
                else -> throw ex
            }
        }
    }
}