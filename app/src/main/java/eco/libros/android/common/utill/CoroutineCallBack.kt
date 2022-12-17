package eco.libros.android.common.utill

interface CoroutineCallBack<T> {
    fun onSuccess(msg: String)
    fun onLoadFailed()
}