package com.example.myapp

import android.content.Intent
import android.nfc.Tag
import android.widget.Button
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CalculatorPage : AppCompatActivity() {

    private lateinit var viewTable: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator_page)

        viewTable = findViewById(R.id.viewTable)

        findViewById<Button>(R.id.buttonZero).setOnClickListener { appendToResult("0") }
        findViewById<Button>(R.id.buttonOne).setOnClickListener { appendToResult("1") }
        findViewById<Button>(R.id.buttonTwo).setOnClickListener { appendToResult("2") }
        findViewById<Button>(R.id.buttonThree).setOnClickListener { appendToResult("3") }
        findViewById<Button>(R.id.buttonFour).setOnClickListener { appendToResult("4") }
        findViewById<Button>(R.id.buttonFive).setOnClickListener { appendToResult("5") }
        findViewById<Button>(R.id.buttonSix).setOnClickListener { appendToResult("6") }
        findViewById<Button>(R.id.buttonSeven).setOnClickListener { appendToResult("7") }
        findViewById<Button>(R.id.buttonEight).setOnClickListener { appendToResult("8") }
        findViewById<Button>(R.id.buttonNine).setOnClickListener { appendToResult("9") }
        findViewById<Button>(R.id.buttonPlus).setOnClickListener { appendToResult("+") }
        findViewById<Button>(R.id.buttonMinus).setOnClickListener { appendToResult("-") }
        findViewById<Button>(R.id.buttonMultiple).setOnClickListener { appendToResult("*") }
        findViewById<Button>(R.id.buttonDivision).setOnClickListener { appendToResult("/") }

        findViewById<Button>(R.id.buttonDelete).setOnClickListener { deleteResult() }

        findViewById<Button>(R.id.buttonEqual).setOnClickListener { getEqual() }

        findViewById<Button>(R.id.B_back).setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun appendToResult(value: String) {
        viewTable.text = "${viewTable.text}$value"
    }

    private fun deleteResult() {
        viewTable.text = ""
    }

    private fun getEqual() {
        val text = viewTable.text.toString()
        val regex = Regex("(\\d+)([+\\-*/])(\\d+)")
        val result = regex.find(text)

        if (result != null)                                                     {
            val c = result.groupValues[2]
            val a = result.groupValues[1].toInt()
            val b = result.groupValues[3].toInt()
            if( result.groupValues[1] == "" ||  result.groupValues[3] == "" ){
                viewTable.text = "0"

            }
            else{
                Log.d ("LOG_TAG", a.toString() +  b.toString()+ c.toString() )
                val output: Int = when (c) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> a / b
                    else -> throw Error("error")
                }
                viewTable.text = "$output"
            }

            /*Log.d ("LOG_TAG", a.toString() +  b.toString()+ c.toString() )
            val output: Int = when (c) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> a / b
                else -> throw Error("error")
            }
            viewTable.text = "$output"*/
        } else{             viewTable.text = "0"
        }
    }
}