package com.example.acapp_v_2.activities

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.acapp_v_2.R
import com.example.acapp_v_2.models.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_dashboard.*

class Demographics : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var DemographicsBar: DemographicsBar

    private lateinit var barChartMale: BarChart
    private lateinit var barChartFemale: BarChart

    private var demogListFemale: ArrayList<DemographicsBar> = ArrayList<DemographicsBar>()
    private var demogListMale: ArrayList<DemographicsBar> = ArrayList<DemographicsBar>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demographics)
        overridePendingTransition(0,0)

        val back = findViewById<View>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0,0)
            finish()
        }

        val menu: Menu = bottomNavigationBar.menu
        val menuItem: MenuItem = menu.getItem(2)
        menuItem.setChecked(true)

        bottomNavigationBar.setOnNavigationItemSelectedListener {
            if (it.itemId == R.id.ic_inventory){
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
            if (it.itemId == R.id.ic_finance){
                val intent = Intent(this, Finance::class.java)
                startActivity(intent)
                finish()
            } else{
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
        //
        // MALE DEMOGRAPHICS
        //

        barChartMale = findViewById(R.id.barChartAge)
        demogListMale = getDemogListMale()

        initBarChartMale()

        //now draw bar chart with dynamic data
        val entries: ArrayList<BarEntry> = ArrayList()

        //you can replace this data object with  your custom object
        for (i in demogListMale.indices) {
            val male = demogListMale[i]
            entries.add(BarEntry(i.toFloat(), male.total.toFloat()))
        }

        val barDataSet = BarDataSet(entries, "")
        barDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)

        val data = BarData(barDataSet)
        barChartMale.data = data

        barChartMale.invalidate()

        //
        // FEMALE DEMOGRAPHICS
        //

        barChartFemale = findViewById(R.id.barChartGender)
        demogListFemale = getDemogListFemale()

        initBarChartFemale()
        Log.d(TAG, "female: ${demogListFemale}")
        //now draw bar chart with dynamic data
        val femaleEntries: ArrayList<BarEntry> = ArrayList()

        //you can replace this data object with  your custom object
        for (i in demogListFemale.indices) {
            val female = demogListFemale[i]
            femaleEntries.add(BarEntry(i.toFloat(), female.total.toFloat()))
        }

        val femaleBarDataSet = BarDataSet(femaleEntries, "")
        femaleBarDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)

        val femaleData = BarData(femaleBarDataSet)
        barChartFemale.data = femaleData

        barChartFemale.invalidate()

    }
    private fun initBarChartMale() {

//        hide grid lines
        barChartMale.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = barChartMale.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        barChartMale.axisRight.isEnabled = false

        //remove legend
        barChartMale.legend.isEnabled = false

        //remove description label
        barChartMale.description.isEnabled = false

        //add animation
        barChartMale.animateY(3000)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = +90f

    }

    private fun initBarChartFemale() {

//        hide grid lines
        barChartFemale.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = barChartFemale.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        barChartFemale.axisRight.isEnabled = false

        //remove legend
        barChartFemale.legend.isEnabled = false

        //remove description label
        barChartFemale.description.isEnabled = false

        //add animation
        barChartFemale.animateY(3000)

        // to draw label on xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.valueFormatter = MyAxisFormatter2()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = +90f

    }

    inner class MyAxisFormatter : IndexAxisValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            Log.d(ContentValues.TAG, "getAxisLabel: index $index")
            return if (index < demogListMale.size) {
                demogListMale[index].age
            } else {
                ""
            }
        }
    }
    inner class MyAxisFormatter2 : IndexAxisValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            Log.d(ContentValues.TAG, "getAxisLabel: index $index")
            return if (index < demogListFemale.size) {
                demogListFemale[index].age
            } else {
                ""
            }
        }
    }

    // simulate api call
    // we are initialising it directly
    private fun getDemogListMale(): ArrayList<DemographicsBar> {
        demogListMale.add(DemographicsBar("Under 1", 17657))
        demogListMale.add(DemographicsBar("1 - 4", 74533))
        demogListMale.add(DemographicsBar("5 - 9", 92663))
        demogListMale.add(DemographicsBar("10 - 14", 90553))
        demogListMale.add(DemographicsBar("15 - 19", 90554))
        demogListMale.add(DemographicsBar("20 - 24", 89439))
        demogListMale.add(DemographicsBar("25 - 29", 78417))
        demogListMale.add(DemographicsBar("30 - 34", 66665))
        demogListMale.add(DemographicsBar("35 - 39", 56770))
        demogListMale.add(DemographicsBar("40 - 44", 47483))
        demogListMale.add(DemographicsBar("45 - 49", 43923))
        demogListMale.add(DemographicsBar("50 - 54", 38590))
        demogListMale.add(DemographicsBar("55 - 59", 31440))
        demogListMale.add(DemographicsBar("60 - 64", 22803))
        demogListMale.add(DemographicsBar("65 - 69", 15026))
        demogListMale.add(DemographicsBar("70 - 74", 9310))
        demogListMale.add(DemographicsBar("75 - 79", 6924))
        demogListMale.add(DemographicsBar("80 years and over", 6590))

        //productList.add(ProdModelClass(empList))
        return demogListMale
    }

    private fun getDemogListFemale(): ArrayList<DemographicsBar> {
        demogListFemale.add(DemographicsBar("Under 1", 16932))
        demogListFemale.add(DemographicsBar("1 - 4", 70310))
        demogListFemale.add(DemographicsBar("5 - 9", 88141))
        demogListFemale.add(DemographicsBar("10 - 14", 85835))
        demogListFemale.add(DemographicsBar("15 - 19", 88795))
        demogListFemale.add(DemographicsBar("20 - 24", 86011))
        demogListFemale.add(DemographicsBar("25 - 29", 72009))
        demogListFemale.add(DemographicsBar("30 - 34", 59520))
        demogListFemale.add(DemographicsBar("35 - 39", 51554))
        demogListFemale.add(DemographicsBar("40 - 44", 43076))
        demogListFemale.add(DemographicsBar("45 - 49", 41160))
        demogListFemale.add(DemographicsBar("50 - 54", 36255))
        demogListFemale.add(DemographicsBar("55 - 59", 30628))
        demogListFemale.add(DemographicsBar("60 - 64", 23391))
        demogListFemale.add(DemographicsBar("65 - 69", 16649))
        demogListFemale.add(DemographicsBar("70 - 74", 11749))
        demogListFemale.add(DemographicsBar("75 - 79", 9667))
        demogListFemale.add(DemographicsBar("80 years and over", 10984))

        //productList.add(ProdModelClass(empList))
        return demogListFemale
    }
}