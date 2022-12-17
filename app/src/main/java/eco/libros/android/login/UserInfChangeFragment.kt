package eco.libros.android.login

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import eco.libros.android.R
import eco.libros.android.common.CustomFragment
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.databinding.FragmentUserInfChangeBinding
import java.io.*

class UserInfChangeFragment : CustomFragment() {
    // TODO: Rename and change types of parameters
    private var userImagePath: String? = null

    private val PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val permissionList = mutableListOf<String>()
    private lateinit var binding: FragmentUserInfChangeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUserInfChangeBinding.inflate(inflater, container, false)
        val menu = arrayOf("사진찍기", "앨범에서 가져오기", "현재 사진 삭제하기")

        binding.editEmail.apply {
            setText(LibrosUtil.getUserId(requireContext(), false, needEncrypt = false).toString())
            isEnabled = false
            isFocusable = false
        }

        binding.profileImg.setOnClickListener {
            permissionList.clear()
            for (p in PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(requireContext(), p) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(p)
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), p)) {
                        LibrosUtil.showCustomMsgWindow(activity, "알림", resources.getString(R.string.deny_message_exec_permission), "확인", false,
                                DialogInterface.OnClickListener { dialogInterface, i ->
                                    dialogInterface.dismiss()
                                    requestPermission()
                                })
                    }
                }
            }

            if (permissionList.size == 0 && permissionList.isEmpty()) {
                AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog).setItems(menu) { dialog, which ->
                    when (which) {
                        0 -> startCapture()
                        1 -> openAlbum()
                        2 -> Toast.makeText(context, "서비스 준비중", Toast.LENGTH_LONG).show()
                    }
                    dialog.dismiss()}
                        .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
                        .create().show()
            } else {
                requestPermission()
            }
        }

        return binding.root
    }

    private fun requestPermission() {
        requestPermissions(permissionList.toTypedArray(), GlobalVariable.LIBORS_PERMISSION_CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == GlobalVariable.LIBORS_PERMISSION_CAMERA && (grantResults.isNotEmpty())) {
            grantResults.forEach { i ->
                if (i == PackageManager.PERMISSION_DENIED) {
                    AlertDialog.Builder(context).setTitle("권한 설정").setMessage("해당 기능은 권한이 필요합니다\n" +
                            " [설정] -> [권한]에서 사용으로 활성화 해주세요.")
                            .setPositiveButton("설정") { dialog, which ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    dialog.dismiss()
                                    startActivityForResult(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:${activity?.packageName}")), 2001)
                                }
                            }
                            .setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, _ ->
                                dialogInterface.dismiss()
                            }).create().show()
                    return
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun createImageFile(): File{
        val dir = File("${activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/libros/temp")
        if (!dir.exists()){
            dir.mkdirs()
        }
        val file = File("${activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/libros/temp", "tempUserImage.png")
        try{
            if (!file.exists()){
                file.delete()
            }
            file.createNewFile()
        } catch (e: IOException){
            e.printStackTrace()
        }
        userImagePath = file.absolutePath
        return file
    }

    private fun startCapture() {
        val file = createImageFile()
        val photoURI = FileProvider.getUriForFile(requireContext(), getString(R.string.file_provider_authority), file)
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            resolveActivity(activity?.packageManager!!)
            putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }, GlobalVariable.CAPTURE_IMAGE_REQUEST_CODE)
    }

    private fun saveCropImage(bitmap: Bitmap?, filePath: String){
        val copyFile = File(filePath)
        try {
            BufferedOutputStream(FileOutputStream(copyFile)).use { bos ->
                copyFile.createNewFile()
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, bos)
            }
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }

    private fun openAlbum() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(
                Intent.createChooser(intent, getString(R.string.chooser_photo_title)), GlobalVariable.CAPTURE_ALBUM_REQUEST_CODE
        )
    }

    private fun getFileUri(file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(requireContext(), getString(R.string.file_provider_authority), file)
        } else {
            Uri.fromFile(file)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val resultUri = CropImage.getActivityResult(data).uri
                    var bit : Bitmap? = null
                    try{
                        bit = if(Build.VERSION.SDK_INT < 28){
                            MediaStore.Images.Media.getBitmap(activity?.contentResolver, resultUri)
                        } else {
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(activity?.contentResolver!!, resultUri))
                        }
                        if( bit != null){
                            saveCropImage(bit, userImagePath.toString())
                        }
                    }catch (e: IOException){
                        Log.d("TESTERROR",e.toString())
                    }
                    if (userImagePath != null){
                        binding.profileImg.setImageBitmap(getBitmap())
                    }
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                Toast.makeText(requireContext(), getString(R.string.toast_crop_image_error_msg), Toast.LENGTH_SHORT).show()
            }

            GlobalVariable.CAPTURE_IMAGE_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK){
                    val photoUri = Uri.fromFile(File(userImagePath))
                    if(photoUri != null) {
                        openCropView(photoUri)
                    }
                }
            }
            GlobalVariable.CAPTURE_ALBUM_REQUEST_CODE -> {
                openCropView(data?.data)
            }
        }
    }

    private fun openCropView(imageUri: Uri?) {
        createImageFile()
        CropImage.activity(imageUri).apply {
            setGuidelinesColor(Color.WHITE)
            setBorderCornerColor(Color.BLACK)
            setGuidelines(CropImageView.Guidelines.ON)
            setActivityMenuIconColor(Color.GRAY)
            setCropMenuCropButtonTitle("확인")
            setAllowRotation(true)
            setAllowFlipping(false)
        }.start(requireActivity())
    }

    private fun getBitmap(): Bitmap? {
        return BitmapFactory.Options().run {

            inJustDecodeBounds = true
            BitmapFactory.decodeFile(userImagePath,this)
            if (this.outWidth in 501..999){
                inSampleSize = 2
            } else if ( 10000 < this.outWidth){
                inSampleSize = 4
            }
            inJustDecodeBounds = false

            BitmapFactory.decodeFile(userImagePath,this)
        }
    }
    companion object {

    }
}