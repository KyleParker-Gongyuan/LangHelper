package com.example.langhelper

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import com.example.langhelper.Common.Companion.current_Text
import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var dialog: AlertDialog
    private lateinit var idHanziText: EditText
    private lateinit var idButtonOpenFloat: Button

    private lateinit var idButtonTranslate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        idHanziText = findViewById(R.id.idHanziText)

        idButtonOpenFloat = findViewById(R.id.idButtonOpenFloat)

        idButtonTranslate = findViewById(R.id.idButtonTranslate) // might not need this for the translator


        if (is_Service_Runing()){
            stopService(Intent(this@MainActivity,FloatingWindowApp::class.java))

        }
        idHanziText.setText(current_Text) // this gets the text from the "main" app and the floating window
        idHanziText.setSelection(idHanziText.text.toString().length) //(you probably want to continue where you left off) // sets the "writing position" (this " | " that blinks )

        idHanziText.addTextChangedListener(object : TextWatcher {// saving the text
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {// not needed probably
            //current_Text = idHanziText.text.toString() // sends the info back  and forth
        }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) { // might be needed

            }


        })

        idButtonOpenFloat.setOnClickListener {
            if (check_Overlay_Permission()) { // if its allowed
                startService(Intent(this@MainActivity, FloatingWindowApp::class.java))
                finish()
            }
            else{ // if not ask for permissions (SYSTEM_ALERT_WINDOW)
                request_Floating_Permission()
            }
        }


        //Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance())) // doesnt need 'this' (pinyin without tones (WHY-TF would i want this) )

        // translation button
        idButtonTranslate.setOnClickListener {
            var chiString: String = idHanziText.text.toString()

            // each character to pinyin then set a color

            //idPinyinText.text = Pinyin.toPinyin(chiString, "") // we want to pass a string from a editor text // I want to make/get diacritics

            //idPinyinText.text = jpinyin(chiString).toString()
            idPinyinText.text =
                PinyinHelper.convertToPinyinString(chiString, "", PinyinFormat.WITH_TONE_MARK)

        }


        /*
        isservicerunning()
        requestpermission()

        checkandroidVersion() each version has dif floating window

         */


        /*
        // Notes for building:
        make a button/icon: this will open a window that you can translate to pinyin

        */


    }

    private fun is_Service_Runing(): Boolean { // if the floating window is open/running
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            // if the window is running
            if (FloatingWindowApp::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun request_Floating_Permission() { // allows the "SYSTEM_ALERT_WINDOW" to be used
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle("screen overlay permission needed")
        builder.setMessage("enable 'display over the app' from settings")
        builder.setPositiveButton(
            "Open settings",
            DialogInterface.OnClickListener { dialog, which -> // goes to the settings for the allowing the floating window to be opened
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, RESULT_OK)
            })
        dialog = builder.create()
        dialog.show()


    }

    private fun check_Overlay_Permission(): Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){ // 'M' is marshmello
            Settings.canDrawOverlays(this) // false
        }
        else return true
    }


}