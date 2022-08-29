package com.muratcakin.yemektariflerikitabi

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_recipe.*
import java.io.ByteArrayOutputStream
import kotlin.Exception

class RecipeFragment : Fragment() {

    var choosenImage : Uri? = null
    var choosenBitmap : Bitmap? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener{
            save(it)
        }
        imageView.setOnClickListener{
            chooseImage(it)
        }

        arguments?.let {
            var receivedInformation = RecipeFragmentArgs.fromBundle(it).information
            var receivedId = RecipeFragmentArgs.fromBundle(it).id

            if (receivedInformation.equals("frommenu")) {
                // yeni yemek ekleniyor
                mealNameText.setText("")
                mealIngredientsText.setText("")
                button.visibility = View.VISIBLE

                val choosingBackgroundOfImage =BitmapFactory.decodeResource(context?.resources, R.drawable.gorselsecimi)
                imageView.setImageBitmap(choosingBackgroundOfImage)
            } else {
                // olan yemeklerden biri görüntülenecek
                button.visibility = View.INVISIBLE
                val choosenId = RecipeFragmentArgs.fromBundle(it).id

                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("Meals", Context.MODE_PRIVATE, null)
                        val cursor = db.rawQuery("SELECT * FROM meals WHERE id = ?", arrayOf(choosenId.toString()))
                        val mealNameIndex = cursor.getColumnIndex("mealname")
                        val mealIngredientsIndex = cursor.getColumnIndex("mealingredients")
                        val mealImage = cursor.getColumnIndex("image")

                        while (cursor.moveToNext()) {
                            mealNameText.setText(cursor.getString(mealNameIndex))
                            mealIngredientsText.setText((cursor.getString(mealIngredientsIndex)))

                            val byteArray = cursor.getBlob(mealImage)
                            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                            imageView.setImageBitmap(bitmap)
                        }
                        cursor.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // save to SQLite
    fun save(view: View) {
        val mealName = mealNameText.text.toString()
        val mealIngredients = mealIngredientsText.text.toString()

        if (choosenBitmap != null) {
            val smallerBitmap = convertBitmapToSmaller(choosenBitmap!!, maximumSize =  300)

            val outputStream = ByteArrayOutputStream()
            smallerBitmap.compress(Bitmap.CompressFormat.PNG, 50,outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                context?.let {
                    val database =it.openOrCreateDatabase("Meals", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS meals (id INTEGER PRIMARY KEY, mealname VARCHAR, mealingredients VARCHAR, image BLOB)")

                    val sqlString = "INSERT INTO meals (mealname, mealingredients, image) VALUES (?, ?, ?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1, mealName)
                    statement.bindString(2, mealIngredients)
                    statement.bindBlob(3, byteArray)
                    statement.execute()

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val action =RecipeFragmentDirections.actionRecipeFragmentToListFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }


    fun chooseImage(view: View) {

        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // izin verilmedi, izin istememiz gerekiyor
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            } else {
                // önceden izin verilmiş, galeriye git
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent, 2)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // izni aldık
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            choosenImage = data.data

            try {
                context?.let {
                    if (choosenImage != null){
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(it.contentResolver, choosenImage!!)
                            choosenBitmap = ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(choosenBitmap)
                        } else {
                            choosenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,choosenImage)
                            imageView.setImageBitmap(choosenBitmap)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun convertBitmapToSmaller(bitmapFromUser: Bitmap, maximumSize: Int): Bitmap {

        var width = bitmapFromUser.width
        var height = bitmapFromUser.height
        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1) {
            // görsel yatay
            width = maximumSize
            val convertedHeight = width / bitmapRatio
            height = convertedHeight.toInt()
        } else {
            height = maximumSize
            val convertedWidth = height * bitmapRatio
            width = convertedWidth.toInt()
        }

        return Bitmap.createScaledBitmap(bitmapFromUser,width,height,true)

    }
}