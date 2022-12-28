package eco.libros.android.common.api

object LibrosService {
    private const val LIBROS_API = "http://192.168.7.122:33405/"

    val libros_client = BaseService().getClient(LIBROS_API)?.create(LibrosApi::class.java)
}