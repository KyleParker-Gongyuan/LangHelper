package com.example.langhelper

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.langhelper.Common.Companion.current_Text
import com.github.stuxuhai.jpinyin.PinyinFormat
import com.github.stuxuhai.jpinyin.PinyinHelper
import kotlinx.android.synthetic.main.activity_main.*

class FloatingWindowApp : Service(){

    private lateinit var floatView: ViewGroup
    private lateinit var float_WindowLayoutParams: WindowManager.LayoutParams
    private var LAYOUT_TYPE: Int? = null
    private lateinit var windowManger: WindowManager

    private lateinit var idHanziText: EditText

    private lateinit var idPinyinText: TextView


    private lateinit var idButtonCloseFloat: Button

    private lateinit var idButtonTranslate: Button


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels


        windowManger = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater // this connects the code to the xml (i think)

        floatView = inflater.inflate(R.layout.floating_layout,null) as ViewGroup //(again a view is the buttons and edit text and all that )

        // all this is for "saving" the text in the main app and the flaoting window (not really needed for my app but its whats in the video)
        idButtonTranslate = floatView.findViewById(R.id.idButtonTranslate)
        idPinyinText = floatView.findViewById(R.id.idPinyinText)
        idButtonCloseFloat = floatView.findViewById(R.id.idButtonCloseFloat)

        idHanziText = floatView.findViewById(R.id.idHanziText)


        idHanziText.setText(current_Text) // this code that connects the main app and the flaoting window
        idHanziText.setSelection(idHanziText.text.toString().length) // at the end of the text set the " | "(blinks)
        idHanziText.isCursorVisible = false // so we cant see the cursor (self explanatory)




        // info if it's over or under "android oreo"
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        else LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST

        float_WindowLayoutParams = WindowManager.LayoutParams(
            (width * 0.55f).toInt(),// this sets the size of the window (probably make it smaller)
            (height * 0.55f).toInt(),
            LAYOUT_TYPE!!,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // this makes it so you can click on things in the background not just the floating window
            PixelFormat.TRANSLUCENT
        )
        float_WindowLayoutParams.gravity = Gravity.TOP // at the top of the screen
        float_WindowLayoutParams.x = 0
        float_WindowLayoutParams.y = 0

        windowManger.addView(floatView,float_WindowLayoutParams) // actually starts the window

        // close floating window
        idButtonCloseFloat.setOnClickListener{
            stopSelf() // stops
            windowManger.removeView(floatView) // gets rid of view I.E the floating window

            /*
            // stuff for
            val back = Intent(this@FloatingWindowApp,MainActivity::class.java)
            back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) // this brings the app to the "TOP" or it will open the app if its been closed
            startActivity(back)
            */
            // how to go back to the main app
            // We actually probably want to fully close the app when you go to the main
            // how to move the window????
        }

        // sending text between "MainActivity" and "FloatingWindowApp"
        idHanziText.addTextChangedListener(object : TextWatcher {// saving the text
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {// not needed probably
            current_Text = idHanziText.text.toString() // sends the data (strings) back  and forth
        }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) { // might be needed
            }


        })

        idButtonTranslate.setOnClickListener {
            var chiString: String = idHanziText.text.toString()

            // each character to pinyin then set a color

            //idPinyinText.text = Pinyin.toPinyin(chiString, "") // we want to pass a string from a editor text // I want to make/get diacritics

            //idPinyinText.text = jpinyin(chiString).toString()
            idPinyinText.text =
                PinyinHelper.convertToPinyinString(chiString, "", PinyinFormat.WITH_TONE_MARK)

        }

        // for moving the window
        floatView.setOnTouchListener(object : View.OnTouchListener{

            val updatedFloatwindowLayoutParam = float_WindowLayoutParams
            var oldx = 0.0
            var oldy = 0.0
            var newx = 0.0
            var newy = 0.0

            override fun onTouch(v: View?, event: MotionEvent?): Boolean { // moving the window

                when(event!!.action){
                    MotionEvent.ACTION_DOWN ->{
                        oldx = updatedFloatwindowLayoutParam.x.toDouble()
                        oldy = updatedFloatwindowLayoutParam.y.toDouble()

                        newx = event.rawX.toDouble()
                        newy = event.rawY.toDouble()

                    }
                    MotionEvent.ACTION_MOVE ->{
                        updatedFloatwindowLayoutParam.x = (oldx + event.rawX - newx).toInt()
                        updatedFloatwindowLayoutParam.y = (oldy + event.rawY - newy).toInt()
                        windowManger.updateViewLayout(floatView, updatedFloatwindowLayoutParam)
                    }

                }
                return false

            }

        } )

        //focus on window (allows typing/clicking)
        idHanziText.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                idHanziText.isCursorVisible = true
                val updatedFloatParamsFlag = float_WindowLayoutParams

                updatedFloatParamsFlag.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN //this sets the focus to the floating window

                windowManger.updateViewLayout(floatView,updatedFloatParamsFlag)
                return false
            }

        })

/*
        idButtonTranslate.setOnClickListener { // the fact that this is in the create bugs things out
            var chiString: String = idHanziText.text.toString()

            // each character to pinyin then set a color

            //idPinyinText.text = Pinyin.toPinyin(chiString, "") // we want to pass a string from a editor text // I want to make/get diacritics

            //idPinyinText.text = jpinyin(chiString).toString()
            //idPinyinText.text = PinyinHelper.convertToPinyinString(chiString, "", PinyinFormat.WITH_TONE_MARK)

            idpaintxt.text = PinyinHelper.convertToPinyinString(chiString, "", PinyinFormat.WITH_TONE_MARK)

        }
        */

    }

    override fun onDestroy() { //this removes everythign from the memory (REMEMBER YOU HAVE TO MANAGE MEMORY WITH ANDROID)
        super.onDestroy()
        stopSelf()
        windowManger.removeView(floatView)
    }
}