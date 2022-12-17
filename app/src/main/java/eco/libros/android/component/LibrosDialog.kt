package eco.libros.android.component

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import eco.libros.android.R
import kotlinx.android.synthetic.main.dialog.*

class LibrosDialog : Dialog {
    class LibrosBuilder(private val context: Context) {
        private val dialog = LibrosDialog(context).apply {
            setContentView(R.layout.dialog)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        fun setMessage(message: String): LibrosBuilder {
            dialog.tvMessage.text = message
            return this
        }

        fun setPositiveButton(textRes: Int, listener: ((dialog: DialogInterface, which: Int) -> Unit)? = null): LibrosBuilder {
            return setPositiveButton(context.getString(textRes), listener)
        }

        private fun setPositiveButton(text: String, listener: ((dialog: DialogInterface, which: Int) -> Unit)? = null): LibrosBuilder {
            dialog.tvAlertConfirm.apply {
                visibility = View.VISIBLE
                this.text = text
                setOnClickListener {
                    listener?.invoke(dialog, 0)
                    dialog.dismiss()
                }
            }
            return this
        }

        fun setNegativeButton(textRes: Int, listener: ((dialog: DialogInterface, which: Int) -> Unit)? = null): LibrosBuilder {
            return setNegativeButton(context.getString(textRes), listener)
        }

        private fun setNegativeButton(text: String, listener: ((dialog: DialogInterface, which: Int) -> Unit)? = null): LibrosBuilder {
            dialog.tvAlertCancel.apply {
                visibility = View.VISIBLE
                this.text = text
                setOnClickListener {
                    listener?.invoke(dialog, 1)
                    dialog.dismiss()
                }
            }
            return this
        }

        fun setCancelable(cancelable: Boolean): LibrosBuilder {
            dialog.setCancelable(cancelable)
            return this
        }

        private fun build(): LibrosDialog {
            if (dialog.tvMessage.text.isNullOrEmpty()) {
                dialog.tvMessage.visibility = View.GONE
            }

            if (!dialog.tvAlertConfirm.hasOnClickListeners()) {
                dialog.tvAlertConfirm.setOnClickListener {
                    dialog.dismiss()
                }
            }

            if (!dialog.tvAlertCancel.hasOnClickListeners()) {
                dialog.tvAlertCancel.setOnClickListener {
                    dialog.dismiss()
                }
            }
            return dialog
        }

        fun show(): LibrosDialog {
            build().show()
            return dialog
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)
    constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super(
        context,
        cancelable,
        cancelListener
    )
}