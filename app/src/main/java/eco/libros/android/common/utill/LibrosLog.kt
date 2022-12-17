package eco.libros.android.common.utill

import android.util.Log
import eco.libros.android.common.variable.GlobalVariable

object LibrosLog {

    fun print(msg : String){
        if (GlobalVariable.SHOW_LOG){
            Log.d("LIBROSLOG","LOBROS========")
            Log.d("LIBROSTESTLOG",msg)
            Log.d("LIBROSLOG","LOBROS========")
        }
    }

    fun print(msg: String, title: String){
        if (GlobalVariable.SHOW_LOG){
            Log.d("LIBROSLOG","LOBROS========")
            Log.d("LIBROSLOG",title)
            Log.d("LIBROSTESTLOG",msg)
            Log.d("LIBROSLOG","LOBROS========")
        }
    }
}