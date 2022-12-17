package eco.libros.android.common.http

object LibrosService {
    private const val LIBROPIA_API = "http://m.libropia.co.kr/LibropiaWebService/request/"
    private const val LIBROPIA_CARD_API = "http://m.libropia.co.kr/LibropiaWebService/request/"
    private const val LIBROS_API = "http://192.168.7.122:33405/"

    val libropia_client = BaseService().getClient(LIBROPIA_API)?.create(LibropiaApi::class.java)
    val libropiaCardClient = BaseService().getClient(LIBROPIA_API)?.create(LibrosCardApi::class.java)
    val libropiaLogInClient = BaseService().getClient(LIBROS_API)?.create(LibropiaApi::class.java)

    val libros_client = BaseService().getClient(LIBROS_API)?.create(LibrosApi::class.java)
}