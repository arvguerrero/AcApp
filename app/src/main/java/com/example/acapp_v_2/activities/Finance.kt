package com.example.acapp_v_2.activities

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.acapp_v_2.R
import com.example.acapp_v_2.models.MaterialBar
import com.example.acapp_v_2.models.Product
import com.example.acapp_v_2.models.ProductBar
import com.example.acapp_v_2.models.ProfitChart
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dashboard.*
import android.widget.EditText
import android.widget.TextView
import kotlin.Throws
import android.content.res.AssetFileDescriptor
import android.os.Build
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.components.YAxis
import com.google.gson.internal.bind.util.ISO8601Utils.format
import com.squareup.okhttp.internal.http.HttpDate.format
import kotlinx.android.synthetic.main.activity_dashboard.bottomNavigationBar
import kotlinx.android.synthetic.main.activity_dashboard.dashBusinessName
import kotlinx.android.synthetic.main.activity_finance.*
import okhttp3.internal.http.HttpDate.format
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception
import java.lang.String.format
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.DateFormat
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList

class Finance : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var product: Product

    private lateinit var calendar: Calendar
    var tflite: Interpreter? = null
    private lateinit var ai: TextView
    private lateinit var per: TextView
    private lateinit var outp: TextView
    private lateinit var month: TextView
    private lateinit var pred: Button

    private lateinit var lineChartProfit: LineChart
    private lateinit var lineChart: LineChart


    private lateinit var barChartRevenue: BarChart
    private lateinit var barChartFutureRevenue: BarChart
    private lateinit var barChartExpenses: BarChart
    private lateinit var barChartProject: BarChart


    private var productList: ArrayList<ProductBar> = ArrayList<ProductBar>()
    private var materialList: ArrayList<MaterialBar> = ArrayList<MaterialBar>()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance)
        overridePendingTransition(0,0)

        val back = findViewById<View>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0,0)
            finish()
        }

        outp = findViewById(R.id.hw)
        ai = findViewById(R.id.ai)
        per = findViewById(R.id.per)
        month = findViewById(R.id.Month)

        val menu: Menu = bottomNavigationBar.menu
        val menuItem: MenuItem = menu.getItem(1)
        menuItem.setChecked(true)

        bottomNavigationBar.setOnNavigationItemSelectedListener {
            if (it.itemId == R.id.ic_inventory) {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
            if (it.itemId == R.id.ic_demo) {
                val intent = Intent(this, Demographics::class.java)
                startActivity(intent)
                finish()
            } else {
            }
            true
        }

        getMonth()

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val uid = auth.currentUser!!.uid

        //GetBusinessName
        val barEntries1: ArrayList<BarEntry> = ArrayList()
        val barEntries2: ArrayList<BarEntry> = ArrayList()
        val Name = db.collection("users").document(uid)
        Name.get()
            .addOnSuccessListener { document ->
                if (document != null) {

                    barChartProject = findViewById(R.id.barChartProjected)
                    initBarChartProject()
                    dashBusinessName.text = document.getString("businessName")
                    val income = document.getString("income")
                    val expenses = document.getString("expenses")

                    val inc = income.toString().toFloat()

                    try {
                        tflite = Interpreter(loadModelFile())
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }

                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("MM")
                    val formatted = current.format(formatter)

                    val df = DecimalFormat("#.##")
                    //pred.setOnClickListener(View.OnClickListener {
                    val prediction = doInference(expenses.toString())
                    println(prediction)
                    val target = df.format(prediction)
                    outp.setText("Target Income: ₱" + target.toString())
                    ai.setText("Current Income: ₱" + income.toString())

                    val pert = (inc / prediction) * 100
                    val percentup = df.format(pert)
                    per.setText(percentup.toString() + "% Achieved")


                    val prd = prediction.toFloat()

                    if(formatted == "01") {
                        barEntries1.add(BarEntry(0f, inc))
                        barEntries2.add(BarEntry(1f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        barDataSet1.valueTextSize = 15f
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        barDataSet2.valueTextSize = 15f
                        val Projection = BarData(barDataSet1, barDataSet2)
                        Projection.setValueTextSize(15f)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("January")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.2f
                        Projection.barWidth = 0.5f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }
                    if(formatted == "02") {
                        barEntries1.add(BarEntry(0f, inc))
                        barEntries2.add(BarEntry(1f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        val Projection = BarData(barDataSet1, barDataSet2)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("February")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.2f
                        Projection.barWidth = 0.15f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }
                    if(formatted == "03") {
                        barEntries1.add(BarEntry(0f, inc))
                        barEntries2.add(BarEntry(1f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        val Projection = BarData(barDataSet1, barDataSet2)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("March")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.5f
                        Projection.barWidth = 0.15f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }
                    if(formatted == "04") {
                        barEntries1.add(BarEntry(0f, inc))
                        barEntries2.add(BarEntry(1f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        val Projection = BarData(barDataSet1, barDataSet2)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("April")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.5f
                        Projection.barWidth = 0.15f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }
                    if(formatted == "05") {
                        barEntries1.add(BarEntry(0f, inc))
                        barEntries2.add(BarEntry(1f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        val Projection = BarData(barDataSet1, barDataSet2)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("May")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.5f
                        Projection.barWidth = 0.15f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }
                    if(formatted == "06") {
                        barEntries1.add(BarEntry(6f, inc))
                        barEntries2.add(BarEntry(7f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        val Projection = BarData(barDataSet1, barDataSet2)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("June")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.5f
                        Projection.barWidth = 0.15f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }
                    if(formatted == "07") {
                        barEntries1.add(BarEntry(0f, inc))
                        barEntries2.add(BarEntry(1f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        val Projection = BarData(barDataSet1, barDataSet2)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("July")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.5f
                        Projection.barWidth = 0.15f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }
                    if(formatted == "08") {
                        barEntries1.add(BarEntry(0f, inc))
                        barEntries2.add(BarEntry(1f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        val Projection = BarData(barDataSet1, barDataSet2)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("August")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.5f
                        Projection.barWidth = 0.15f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }
                    if(formatted == "09") {
                        barEntries1.add(BarEntry(0f, inc))
                        barEntries2.add(BarEntry(1f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        val Projection = BarData(barDataSet1, barDataSet2)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("September")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.5f
                        Projection.barWidth = 0.15f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }
                    if(formatted == "10") {
                        barEntries1.add(BarEntry(0f, inc))
                        barEntries2.add(BarEntry(1f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        val Projection = BarData(barDataSet1, barDataSet2)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("October")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.5f
                        Projection.barWidth = 0.15f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }
                    if(formatted == "11") {
                        barEntries1.add(BarEntry(0f, inc))
                        barEntries2.add(BarEntry(1f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        val Projection = BarData(barDataSet1, barDataSet2)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("November")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.5f
                        Projection.barWidth = 0.15f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }
                    if(formatted == "12") {
                        barEntries1.add(BarEntry(0f, inc))
                        barEntries2.add(BarEntry(1f, prediction))

                        val barDataSet1 = BarDataSet(barEntries1, "Current Income")
                        barDataSet1.setColor(Color.BLUE)
                        val barDataSet2 = BarDataSet(barEntries2, "Projected Income")
                        barDataSet2.setColor(Color.RED)
                        val Projection = BarData(barDataSet1, barDataSet2)
                        barChartProject.data = Projection

                        val months = ArrayList<String>()
                        months.add("December")
                        val xAxis = barChartProject.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(months)
                        xAxis.setCenterAxisLabels(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.isGranularityEnabled = true
                        barChartProject.isDragEnabled = true
                        barChartProject.setVisibleXRangeMaximum(3f)

                        val barSpace = 0.1f
                        val groupSpace = 0.5f
                        Projection.barWidth = 0.15f
                        barChartProject.xAxis.axisMinimum = 0f
                        barChartProject.groupBars(0f,groupSpace,barSpace)
                        barChartProject.invalidate()
                    }

                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }

        var revenue: String
        var expenses: String

        val productList: ArrayList<ProductBar> = ArrayList<ProductBar>()
        var productName: String
        var productCode: String
        var price: String
        var soldItems: String
        var stockLevel: String



        val productsData = db.collection("users").document(uid).collection("products")
        productsData.whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document != null) {
                        val index = 0
                        //lineChart = findViewById(R.id.lineChartProfit)
                        barChartRevenue = findViewById(R.id.barChartRevenue)
                        barChartFutureRevenue = findViewById(R.id.barChartFutureRevenue)

                        initBarChartRevenue()
                        initBarChartFutureRevenue()

                        //Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        productName = document.getString("productName")!!
                        productCode = document.getString("productCode")!!
                        price = document.getString("price")!!
                        soldItems = document.getString("soldItems")!!
                        stockLevel = document.getString("stockLevel")!!

                        val product = ProductBar(
                            name1 = productName,
                            code = productCode,
                            price1 = price,
                            soldItems1 = soldItems,
                            stockLevel = stockLevel
                        )
                        productList.add(product)

                        val SalesEntries: ArrayList<BarEntry> = ArrayList()
                        val futureSalesEntries: ArrayList<BarEntry> = ArrayList()

                        val name = ArrayList<String>()
                        val prof = ArrayList<Float>()
                        val sum = ArrayList<Float>()
                        Log.d(ContentValues.TAG, "List: ${productList}")
                        for (i in productList.indices) {
                            val score = productList[i]
                            name.add(score.name1)
                            SalesEntries.add(BarEntry(i.toFloat(), (score.price1.toFloat() * score.soldItems1.toFloat())))
                            futureSalesEntries.add(BarEntry(i.toFloat(), (score.price1.toFloat() * score.stockLevel.toFloat())))

                            //
                            //SALES PER PRODUCT BARCHART (barChartSales)
                            //
                            barChartRevenue.xAxis.valueFormatter = IndexAxisValueFormatter(name)

                            //barChartRevenue.legend.isEnabled = false
                            //barChartRevenue.description.isEnabled = false
                            //barChartRevenue.axisLeft.setDrawGridLines(false)
                            //barChartRevenue.xAxis.setDrawGridLines(false)
                            //barChartRevenue.xAxis.setDrawAxisLine(false)


                            val RevenueDataSet = BarDataSet(SalesEntries, score.name1)
                            RevenueDataSet.valueTextSize = 15f
                            RevenueDataSet.setColors(*ColorTemplate.JOYFUL_COLORS)

                            val Salesdata = BarData(RevenueDataSet)
                            barChartRevenue.data = Salesdata
                            barChartRevenue.invalidate()


                            barChartFutureRevenue.xAxis.valueFormatter = IndexAxisValueFormatter(name)
                            val futureRevenueDataSet = BarDataSet(futureSalesEntries, score.name1)
                            futureRevenueDataSet.valueTextSize = 15f
                            futureRevenueDataSet.setColors(*ColorTemplate.JOYFUL_COLORS)

                            val futureSalesData = BarData(futureRevenueDataSet)
                            barChartFutureRevenue.data = futureSalesData
                            barChartFutureRevenue.invalidate()

/*
                            prof.add((score.price1.toFloat() * score.soldItems1.toFloat()))
                            sum.add(prof.sum())
                            Log.d(ContentValues.TAG, "sum: ${sum}")
                            for(j in sum.indices) {
                                val ctr = sum[j]
                                //
                                //Line chart
                                //
                                //lineEntries.add(Entry(i.toFloat(), (score.price1.toFloat() * score.soldItems1.toFloat())))
                                lineEntries.add(Entry(j.toFloat(), ctr ))

                                val profitDataSet = LineDataSet(lineEntries, "Income")
                                //profitDataSet.setColors(*ColorTemplate.PASTEL_COLORS)
                                profitDataSet.color = resources.getColor(R.color.big_stone)

                                profitDataSet.circleRadius = 10f
                                profitDataSet.setDrawFilled(true)
                                profitDataSet.valueTextSize = 10F
                                profitDataSet.fillColor = resources.getColor(R.color.apache)


                                val profitData = LineData(profitDataSet)
                                lineChart.data = profitData
                                lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(name)
                            }
*/
                            //profitDataSet.mode = profitDataSet.


                            //lineChart.setBackgroundColor(resources.getColor(R.color.white))
                            //lineChart.animateXY(2000, 2000, Easing.EaseInCubic)
                            //lineChart.invalidate()
                        }
                    } else {
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error getting documents:")
            }
        val materialList: ArrayList<MaterialBar> = ArrayList<MaterialBar>()
        var materialName: String
        var materialCode: String
        var materialPrice: String
        var matStockLevel: String
        var threshholdLevel: String


        val rawMaterialsData= db.collection("users").document(uid).collection("materials")
        rawMaterialsData.whereEqualTo("uid", uid)
            .get()

            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document != null) {
                        val index = 0
                        barChartExpenses = findViewById(R.id.barChartExpenses)

                        // Initialize Raw Materials BarChart
                        initBarChartExpenses()

                        //Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        materialName = document.getString("materialName")!!
                        materialCode = document.getString("materialCode")!!
                        materialPrice = document.getString("price")!!
                        matStockLevel = document.getString("stockLevel")!!
                        threshholdLevel = document.getString("thresholdLevel")!!

                        db.collection("users").document(uid).collection("materials")
                            .whereEqualTo("uid", uid)

                        val material = MaterialBar(
                            materialName = materialName,
                            materialCode = materialCode,
                            materialPrice = materialPrice,
                            stockLevel = matStockLevel,
                            thresholdLevel = threshholdLevel
                        )
                        materialList.add(material)

                        val expensesEntries: ArrayList<BarEntry> = ArrayList()
                        val rawName = ArrayList<String>()
                        val sum = ArrayList<Float>()
                        Log.d(ContentValues.TAG, "List: ${productList}")
                        for (i in materialList.indices) {
                            val raw = materialList[i]
                            expensesEntries.add(BarEntry(i.toFloat(), (raw.stockLevel.toFloat() * raw.materialPrice.toFloat())))
                            rawName.add(raw.materialName)

                            //sum.add(raw.stockLevel.toFloat() * raw.materialPrice.toFloat())
                            val exp = sum.sum()


                            //
                            //Expenses Bar Chart
                            //
                            barChartExpenses.xAxis.valueFormatter = IndexAxisValueFormatter(rawName)



                            val RawMaterialsDataSet = BarDataSet(expensesEntries, "")
                            RawMaterialsDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
                            RawMaterialsDataSet.valueTextSize = 15f
                            Log.d(ContentValues.TAG, "ArrayL: ${productList}")
                            val RawMaterialsData = BarData(RawMaterialsDataSet)
                            barChartExpenses.data = RawMaterialsData
                            barChartExpenses.invalidate()
                        }
                    } else {
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error getting documents:")
            }
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = this.assets.openFd("analytics.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declareLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength)
    }

    private fun doInference(inputString: String): Float {
        val inputVal = FloatArray(1)
        inputVal[0] = inputString.toFloat()
        val output = Array(1) { FloatArray(1) }
        tflite!!.run(inputVal, output)
        return output[0][0]
    }

    private fun initBarChartRevenue() {

//        hide grid lines
        barChartRevenue.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = barChartRevenue.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        barChartRevenue.axisRight.isEnabled = false

        //remove legend
        barChartRevenue.legend.isEnabled = false

        //remove description label
        barChartRevenue.description.isEnabled = false

        //add animation
        barChartRevenue.animateY(3000)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        //xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = 0f

    }

    private fun initBarChartFutureRevenue() {

//        hide grid lines
        barChartFutureRevenue.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = barChartFutureRevenue.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        barChartFutureRevenue.axisRight.isEnabled = false

        //remove legend
        barChartFutureRevenue.legend.isEnabled = false

        //remove description label
        barChartFutureRevenue.description.isEnabled = false

        //add animation
        barChartFutureRevenue.animateY(3000)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        //xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = 0f

    }

    private fun initBarChartExpenses() {

//        hide grid lines
        barChartExpenses.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = barChartExpenses.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)


        //remove right y-axis
        barChartExpenses.axisRight.isEnabled = false

        //remove legend
        barChartExpenses.legend.isEnabled = false

        //remove description label
        barChartExpenses.description.isEnabled = false

        //add animation
        barChartExpenses.animateY(3000)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        //xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = 0f


    }

    private fun initBarChartProject() {

//        hide grid lines
        barChartProject.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = barChartProject.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)


        //remove right y-axis
        barChartProject.axisRight.isEnabled = false

        //remove legend
        barChartProject.legend.isEnabled = false

        //remove description label
        barChartProject.description.isEnabled = false

        //add animation
        barChartProject.animateY(3000)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        //xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = 0f
    }

    inner class MyAxisFormatter : IndexAxisValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            Log.d(ContentValues.TAG, "getAxisLabel: index $index")
            return if (index < productList.size) {
                productList[index].name1
            } else {
                ""
            }
        }
    }

    private fun initLineChart() {

//        hide grid lines
        lineChart.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = lineChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        lineChart.axisRight.isEnabled = false

        //remove legend
        lineChart.legend.isEnabled = false


        //remove description label
        lineChart.description.isEnabled = false


        //add animation
        lineChart.animateX(1000, Easing.EaseInSine)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        //xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = +0f

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMonth(){

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MM")
        val formatted = current.format(formatter)
        if (formatted == "01") {
            month.setText("January")
        }
        if (formatted == "02") {
            month.setText("February")
        }
        if (formatted == "03") {
            month.setText("March")
        }
        if (formatted == "04") {
            month.setText("April")
        }
        if (formatted == "05") {
            month.setText("May")
        }
        if (formatted == "06") {
            month.setText("June")
        }
        if (formatted == "07") {
            month.setText("July")
        }
        if (formatted == "08") {
            month.setText("Augusut")
        }
        if (formatted == "09") {
            month.setText("September")
        }
        if (formatted == "10") {
            month.setText("October")
        }
        if (formatted == "11") {
            month.setText("November")
        }
        if (formatted == "12") {
            month.setText("December")
        }
    }

}