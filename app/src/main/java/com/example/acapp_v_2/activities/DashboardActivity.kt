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
import android.widget.TextView
import com.example.acapp_v_2.R
import com.example.acapp_v_2.models.MaterialBar
import com.example.acapp_v_2.models.Product
import com.example.acapp_v_2.models.ProductBar
import com.example.acapp_v_2.util.MyAdapter
import com.example.acapp_v_2.util.ProductViewHolder
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
import com.google.firestore.admin.v1.Index
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.lang.Exception

class DashboardActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var product: Product

    private lateinit var pieChart: PieChart
    private lateinit var pieChartTotal: PieChart

    private lateinit var barChartSales: BarChart
    private lateinit var barChartTotal: BarChart
    private lateinit var barChartRawMaterials: BarChart


    private var productList: ArrayList<ProductBar> = ArrayList<ProductBar>()
    private lateinit var materialList: ArrayList<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val menu: Menu = bottomNavigationBar.menu
        val menuItem: MenuItem = menu.getItem(0)
        menuItem.setChecked(true)

        bottomNavigationBar.setOnNavigationItemSelectedListener {
            if (it.itemId == R.id.ic_finance){
                val intent = Intent(this, Finance::class.java)
                startActivity(intent)
                finish()
            }
            if (it.itemId == R.id.ic_demo){
                val intent = Intent(this, Demographics::class.java)
                startActivity(intent)
                finish()
            } else{
            }
            true
        }

        val dashBusinessName = findViewById<TextView>(R.id.dashBusinessName)
        //val sumText = findViewById<TextView>(R.id.sum)
        val productList: ArrayList<ProductBar> = ArrayList<ProductBar>()
        var productName: String
        var productCode: String
        var price: String
        var soldItems: String
        var stockLevel: String

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
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        //
        //Get Products BarChartData from firestore
        //
        val productsData = db.collection("users").document(uid).collection("products")
        productsData.whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document != null) {
                        val index = 0

                        //Connect barChart to UI
                        barChartSales = findViewById(R.id.barChartSales)
                        barChartRawMaterials = findViewById(R.id.barChartRawMaterials)
                        //lineChart = findViewById(R.id.lineChart)

                        pieChart = findViewById(R.id.pieChartSales)
                        initPieChart()
                        pieChart.setUsePercentValues(false)

                        // Initialize Sales BarChart
                        initBarChart()

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
                        //val TotalEntries: ArrayList<BarEntry> = ArrayList()
                        val RawMaterialEntries: ArrayList<BarEntry> = ArrayList()
                        val lineEntries: ArrayList<Entry> = ArrayList()

                        val pieEntries = ArrayList<PieEntry>()
                        val name = ArrayList<String>()
                        val prof = ArrayList<Float>()

                        val sum = ArrayList<Float>()
                        Log.d(ContentValues.TAG, "List: ${productList}")
                        for (i in productList.indices) {
                            val score = productList[i]
                            SalesEntries.add(BarEntry(i.toFloat(), score.stockLevel.toFloat()))
                            //TotalEntries.add(BarEntry(i.toFloat(), score.soldItems1.toFloat()))
                            RawMaterialEntries.add(BarEntry(i.toFloat(), score.soldItems1.toFloat()))
                            name.add(score.name1)
                            pieEntries.add(PieEntry(score.soldItems1.toFloat(), score.name1))

                            //prof.add((score.price1.toFloat() * score.soldItems1.toFloat()))
                            //sum.add(prof.sum())
                            //Log.d(ContentValues.TAG, "sum1: ${sum}")
                            //sumText.text = sum.toString()
                            ///
                            // MOST SOLD PRODUCT PIE CHART
                            ///
                            val dataSet = PieDataSet(pieEntries, "")
                            dataSet.setColors(*ColorTemplate.PASTEL_COLORS)
                            val data = PieData(dataSet)

                            pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
                            pieChart.animateY(1400, Easing.EaseInOutQuad)
                            pieChart.data = data

                            //create hole in center
                            pieChart.holeRadius = 58f
                            pieChart.transparentCircleRadius = 61f
                            pieChart.isDrawHoleEnabled = true
                            pieChart.setHoleColor(Color.WHITE)

                            //add text in center
                            pieChart.setDrawCenterText(true);
                            pieChart.centerText = "Products"
                            pieChart.invalidate()

                            //
                            //SALES PER PRODUCT BARCHART (barChartSales)
                            //
                            barChartSales.xAxis.valueFormatter = IndexAxisValueFormatter(name)
                            val SalesDataSet = BarDataSet(SalesEntries, score.name1)
                            SalesDataSet.setColors(*ColorTemplate.PASTEL_COLORS)
                            Log.d(ContentValues.TAG, "ArrayL: ${productList}")
                            val Salesdata = BarData(SalesDataSet)
                            barChartSales.data = Salesdata
                            barChartSales.invalidate()

                        }
                    } else {
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error getting documents:")
            }

        //
        //Get Raw Materials BarChartData from firestore
        //

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
                        barChartRawMaterials = findViewById(R.id.barChartRawMaterials)

                        // Initialize Raw Materials BarChart
                        initBarChartRawMaterials()

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

                        val RawMaterialEntries: ArrayList<BarEntry> = ArrayList()
                        val rawName = ArrayList<String>()
                        Log.d(ContentValues.TAG, "List: ${productList}")
                        for (i in materialList.indices) {
                            val raw = materialList[i]
                            RawMaterialEntries.add(BarEntry(i.toFloat(), raw.stockLevel.toFloat()))
                            rawName.add(raw.materialName)

                            //
                            //Raw Materials Bar Chart (barChartRawMaterials)
                            //
                            barChartRawMaterials.xAxis.valueFormatter = IndexAxisValueFormatter(rawName)
                            val RawMaterialsDataSet = BarDataSet(RawMaterialEntries, raw.materialName)
                            RawMaterialsDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
                            Log.d(ContentValues.TAG, "ArrayL: ${productList}")
                            val RawMaterialsData = BarData(RawMaterialsDataSet)
                            barChartRawMaterials.data = RawMaterialsData
                            barChartRawMaterials.invalidate()
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
    private fun initBarChart() {

//        hide grid lines
        barChartSales.axisLeft.setDrawGridLines(true)
        val xAxis: XAxis = barChartSales.xAxis
        xAxis.setDrawGridLines(true)
        xAxis.setDrawAxisLine(true)

        //remove right y-axis
        barChartSales.axisRight.isEnabled = false

        //remove legend
        barChartSales.legend.isEnabled = false

        //remove description label
        barChartSales.description.isEnabled = false

        //add animation
        barChartSales.animateY(3000)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        //xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = +0f

    }

    private fun initPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.text = ""
        //hollow pie chart
        pieChart.isDrawHoleEnabled = false
        pieChart.setTouchEnabled(false)
        pieChart.setDrawEntryLabels(false)
        //adding padding
        pieChart.setExtraOffsets(20f, 0f, 20f, 20f)
        pieChart.setUsePercentValues(true)
        pieChart.isRotationEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.legend.textSize = 10f
        pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL
        pieChart.legend.isWordWrapEnabled = true

    }


    private fun initPieChartTotal() {
        pieChart.setUsePercentValues(true)
        pieChart.description.text = ""
        //hollow pie chart
        pieChart.isDrawHoleEnabled = false
        pieChart.setTouchEnabled(false)
        pieChart.setDrawEntryLabels(false)
        //adding padding
        pieChart.setExtraOffsets(20f, 0f, 20f, 20f)
        pieChart.setUsePercentValues(true)
        pieChart.isRotationEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL
        pieChart.legend.isWordWrapEnabled = true

    }

    private fun initBarChartRawMaterials() {

//        hide grid lines
        barChartRawMaterials.axisLeft.setDrawGridLines(true)
        val xAxis: XAxis = barChartRawMaterials.xAxis
        xAxis.setDrawGridLines(true)
        xAxis.setDrawAxisLine(true)

        //remove right y-axis
        barChartRawMaterials.axisRight.isEnabled = false

        //remove legend
        barChartRawMaterials.legend.isEnabled = false

        //remove description label
        barChartRawMaterials.description.isEnabled = false

        //add animation
        barChartRawMaterials.animateY(3000)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        //xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = +0f
    }
}


