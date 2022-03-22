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

class Finance : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var product: Product

    private lateinit var lineChartProfit: LineChart
    private lateinit var lineChart: LineChart


    private lateinit var barChartRevenue: BarChart
    private lateinit var barChartExpenses: BarChart



    private var productList: ArrayList<ProductBar> = ArrayList<ProductBar>()
    private var materialList: ArrayList<MaterialBar> = ArrayList<MaterialBar>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance)

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

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        val uid = auth.currentUser!!.uid

        //GetBusinessName

        val Name = db.collection("users").document(uid)
        Name.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    dashBusinessName.text = document.getString("businessName")
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }

        var revenue: String
        var expenses: String
/*
        val profitData = db.collection("users").document(uid)
        profitData.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val index = 0
                    lineChartProfit = findViewById(R.id.lineChartProfit)
                    //initLineChart()


                    //Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    revenue = document.getString("income")!!
                    expenses = document.getString("expenses")!!

                    val profit = ProfitChart(revenue = revenue, expenses = expenses)
                    //profitList.add()
                    profitList.add(profit)
                    Log.d(ContentValues.TAG, "Prof: ${profitList}")
                    //val profitVal: ArrayList<ProfitChart> = ArrayList()
                    val profitEntries: ArrayList<Entry> = ArrayList()
                    val profData = ArrayList<Float>()
                    for (i in profitList.indices) {
                        val profit = profitList[i]
                        val index = 0
                        val data = profit.revenue.toFloat() - profit.expenses.toFloat()
                        profData.add(data)
                        for(j in profData.indices) {
                            val profdat = profData[i]
                            profitEntries.add(
                                Entry(
                                    i.toFloat(),
                                    profdat)
                            )
                            val profitDataSet = LineDataSet(profitEntries, "")
                            //profitDataSet.setColors(*ColorTemplate.PASTEL_COLORS)
                            profitDataSet.color = resources.getColor(R.color.big_stone)

                            profitDataSet.circleRadius = 10f
                            profitDataSet.setDrawFilled(true)
                            profitDataSet.valueTextSize = 20F
                            profitDataSet.fillColor = resources.getColor(R.color.apache)
                            //profitDataSet.mode = profitDataSet.

                            val profitData = LineData(profitDataSet)
                            lineChartProfit.data = profitData
                            lineChartProfit.setBackgroundColor(resources.getColor(R.color.white))
                            lineChartProfit.animateXY(2000, 2000, Easing.EaseInCubic)
                            lineChartProfit.invalidate()
                        }
                    }
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error getting documents:")
            }
*/
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
                        lineChart = findViewById(R.id.lineChartProfit)
                        barChartRevenue = findViewById(R.id.barChartRevenue)

                        initBarChartRevenue()

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
                        val lineEntries: ArrayList<Entry> = ArrayList()

                        val name = ArrayList<String>()
                        Log.d(ContentValues.TAG, "List: ${productList}")
                        for (i in productList.indices) {
                            val score = productList[i]
                            name.add(score.name1)
                            SalesEntries.add(BarEntry(i.toFloat(), (score.price1.toFloat() * score.soldItems1.toFloat())))

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
                            RevenueDataSet.setColors(*ColorTemplate.JOYFUL_COLORS)

                            val Salesdata = BarData(RevenueDataSet)
                            barChartRevenue.data = Salesdata
                            barChartRevenue.invalidate()


                            //
                            //Line chart
                            //
                            lineEntries.add(Entry(i.toFloat(), (score.price1.toFloat() * score.soldItems1.toFloat())))
                            val profitDataSet = LineDataSet(lineEntries, "")
                            //profitDataSet.setColors(*ColorTemplate.PASTEL_COLORS)
                            profitDataSet.color = resources.getColor(R.color.big_stone)

                            profitDataSet.circleRadius = 10f
                            profitDataSet.setDrawFilled(true)
                            profitDataSet.valueTextSize = 20F
                            profitDataSet.fillColor = resources.getColor(R.color.apache)
                            //profitDataSet.mode = profitDataSet.


                            val profitData = LineData(profitDataSet)
                            lineChart.data = profitData
                            lineChart.setBackgroundColor(resources.getColor(R.color.white))
                            lineChart.animateXY(2000, 2000, Easing.EaseInCubic)
                            lineChart.invalidate()
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
                        Log.d(ContentValues.TAG, "List: ${productList}")
                        for (i in materialList.indices) {
                            val raw = materialList[i]
                            expensesEntries.add(BarEntry(i.toFloat(), (raw.stockLevel.toFloat() * raw.materialPrice.toFloat())))
                            rawName.add(raw.materialName)

                            //
                            //Expenses Bar Chart
                            //
                            barChartExpenses.xAxis.valueFormatter = IndexAxisValueFormatter(rawName)



                            val RawMaterialsDataSet = BarDataSet(expensesEntries, "")
                            RawMaterialsDataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
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

}