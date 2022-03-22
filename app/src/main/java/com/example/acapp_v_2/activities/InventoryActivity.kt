package com.example.acapp_v_2.activities

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acapp_v_2.R
import com.example.acapp_v_2.models.Ingredients
import com.example.acapp_v_2.models.Material
import com.example.acapp_v_2.models.Product
import com.example.acapp_v_2.util.IngredientAdapter
import com.example.acapp_v_2.util.MyAdapter
import com.example.acapp_v_2.util.MyMaterialAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_inventory.*
import kotlinx.android.synthetic.main.activity_inventory.productTitle
import kotlinx.android.synthetic.main.add_product_dialog.view.*
import kotlinx.android.synthetic.main.add_product_dialog.view.cancelBtn
import kotlinx.android.synthetic.main.add_product_dialog.view.saveBtn
import kotlinx.android.synthetic.main.alert_dialog.view.*
import kotlinx.android.synthetic.main.ingredients_list.*
import kotlinx.android.synthetic.main.ingredients_list.view.*
import kotlinx.android.synthetic.main.material_product.view.*
import kotlinx.android.synthetic.main.product_popup.*
import kotlinx.android.synthetic.main.product_popup.view.*
import java.text.DateFormat


class InventoryActivity : AppCompatActivity() {
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var materialRecyclerView: RecyclerView
    private lateinit var ingredientRecyclerView: RecyclerView
    private lateinit var productArrayList: ArrayList<Product>
    private lateinit var materialArrayList: ArrayList<Material>
    private lateinit var ingredientArrayList: ArrayList<Ingredients>
    private lateinit var myAdapter: MyAdapter
    private lateinit var myMaterialAdapter: MyMaterialAdapter
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var ctr: Int = 0
    private var ctr1: Int = 0
    private val CHANNEL_ID = "channel_id_example_01"
    private val notificationId = 101
    private var notificationText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)
        this.overridePendingTransition(0, 0)

        val back = findViewById<View>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

        val reload = findViewById<View>(R.id.reload)
        reload.setOnClickListener {
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("activity", "InventoryActivity")
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

        db = FirebaseFirestore.getInstance()

        productRecyclerView = findViewById(R.id.products)
        productRecyclerView.layoutManager = LinearLayoutManager(this)
        productRecyclerView.setHasFixedSize(true)
        productArrayList = arrayListOf()
        myAdapter = MyAdapter(this, productArrayList)
        productRecyclerView.adapter = myAdapter

        materialRecyclerView = findViewById(R.id.materials)
        materialRecyclerView.layoutManager = LinearLayoutManager(this)
        materialRecyclerView.setHasFixedSize(true)
        materialArrayList = arrayListOf()
        myMaterialAdapter = MyMaterialAdapter(this, materialArrayList)
        materialRecyclerView.adapter = myMaterialAdapter
        auth = Firebase.auth
        AddListener()
        RawAddListener()
        prodBtn.setOnClickListener {
            val productDialogView =
                LayoutInflater.from(this).inflate(R.layout.add_product_dialog, null)
            val productBuilder = AlertDialog.Builder(this)
                .setView(productDialogView)
            val productAlertDialog = productBuilder.show()
            productDialogView.saveBtn.setOnClickListener {
                val name =
                    productDialogView.findViewById<EditText>(R.id.productName).text.toString()
                val price =
                    productDialogView.findViewById<EditText>(R.id.prodPrice).text.toString()
                val code =
                    productDialogView.findViewById<EditText>(R.id.prodCode).text.toString()
                val uid = auth.currentUser!!.uid
                if (name != "" && price != "" && code != "") {
                    val product = HashMap<String, Any>()
                    product["productName"] = name
                    product["price"] = price
                    product["productCode"] = code
                    product["stockLevel"] = "0"
                    product["soldItems"] = "0"
                    product["uid"] = uid
                    db.collection("users").document(uid).collection("products").document(code)
                        .set(product)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT)
                                .show()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed to create product", Toast.LENGTH_SHORT)
                                .show()
                        }
                    productAlertDialog.dismiss()
                }else{
                    Toast.makeText(this, "Incomplete product details.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            productDialogView.cancelBtn.setOnClickListener {
                productAlertDialog.dismiss()
            }
        }
        rawBtn.setOnClickListener {
            val rawDialogView =
                LayoutInflater.from(this).inflate(R.layout.add_material_dialog, null)
            val rawBuilder = AlertDialog.Builder(this)
                .setView(rawDialogView)
            val rawAlertDialog = rawBuilder.show()
            rawDialogView.saveBtn.setOnClickListener {
                val name =
                    rawDialogView.findViewById<EditText>(R.id.materialName).text.toString()
                val price =
                    rawDialogView.findViewById<EditText>(R.id.matPrice).text.toString()
                val code =
                    rawDialogView.findViewById<EditText>(R.id.materialCode).text.toString()
                val stockLevel =
                    rawDialogView.findViewById<EditText>(R.id.stockLevel).text.toString()
                val thresholdLevel =
                    rawDialogView.findViewById<EditText>(R.id.thresholdLevel).text.toString()
                val uid = auth.currentUser!!.uid
                if (name != "" && price != "" && code !="" && stockLevel != "" && thresholdLevel != "") {
                    val raw = HashMap<String, Any>()
                    raw["materialName"] = name
                    raw["price"] = price
                    raw["materialCode"] = code
                    raw["stockLevel"] = stockLevel
                    raw["thresholdLevel"] = thresholdLevel
                    raw["uid"] = uid
                    db.collection("users").document(uid).collection("materials").document(name)
                        .set(raw)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Material added successfully", Toast.LENGTH_SHORT)
                                .show()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed to create material", Toast.LENGTH_SHORT)
                                .show()
                        }
                    val priceInt = price.toInt()
                    val stockInt = stockLevel.toInt()
                    var cost = priceInt * stockInt
                    val expense = cost.toString()
                    if (stockInt > 0) {
                        var number = 0
                        db.collection("users").document(uid).collection("expenses").get()
                            .addOnSuccessListener { documents ->
                                number = documents.size()
                                var item = number.toString()
                                for (document in documents) {
                                    if (document.getString("item") == item) {
                                        number += 1
                                        item = number.toString()
                                        val expenses = HashMap<String, Any>()
                                        expenses["cost"] = expense
                                        expenses["name"] = name
                                        expenses["uid"] = uid
                                        expenses["item"] = item
                                        expenses["date"] = FieldValue.serverTimestamp()
                                        db.collection("users").document(uid).collection("expenses")
                                            .document(item)
                                            .set(expenses)
                                    }
                                }
                                val expenses = HashMap<String, Any>()
                                expenses["cost"] = expense
                                expenses["name"] = name
                                expenses["uid"] = uid
                                expenses["item"] = item
                                expenses["date"] = FieldValue.serverTimestamp()
                                db.collection("users").document(uid).collection("expenses")
                                    .document(item)
                                    .set(expenses)
                            }
                    }
                    rawAlertDialog.dismiss()
                }else{
                    Toast.makeText(this, "Incomplete material details.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            rawDialogView.cancelBtn.setOnClickListener {
                rawAlertDialog.dismiss()
            }
        }

        var totalExpense = 0.0
        var totalIncome = 0.0
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).collection("expenses")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document != null) {
                        val cost = document.getString("cost")
                        totalExpense += cost!!.toDouble()
                    } else {
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
                val cost = totalExpense.toString()
                db.collection("users").document(uid)
                    .update(
                        mapOf(
                            "expenses" to cost
                        )
                    )
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error getting documents:")
            }

        db.collection("users").document(uid).collection("income")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document != null) {
                        val profit = document.getString("profit")
                        totalIncome += profit!!.toDouble()
                    } else {
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
                val income = totalIncome.toString()
                db.collection("users").document(uid)
                    .update(
                        mapOf(
                            "income" to income
                        )
                    )
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error getting documents:")
            }
        createNotificationChannel()
        var ctr = 0
        db.collection("users").document(uid).collection("materials")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents) {
                    if(document != null){
                        val threshold = document.getString("thresholdLevel")!!.toInt()
                        val stock = document.getString("stockLevel")!!.toInt()
                        if(threshold > stock){
                            ctr += 1
                            val count = ctr.toString()
                            notificationText = "$count item/s went below their threshold level."
                            sendNotification()
                        }
                    }
                }
            }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun sendNotification(){
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Threshold Level Reached!")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }

    private fun AddListener() {
        db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).collection("products").orderBy("productName", Query.Direction.ASCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            productArrayList.add(dc.document.toObject(Product::class.java))
                        }
                    }
                    myAdapter.notifyDataSetChanged()
                }
            })
    }
    private fun RawAddListener() {
        db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).collection("materials").orderBy("materialName", Query.Direction.ASCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            materialArrayList.add(dc.document.toObject(Material::class.java))
                        }
                    }
                    myMaterialAdapter.notifyDataSetChanged()
                }
            })
    }

    private fun DeleteListener() {
        db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).collection("products").orderBy("productName", Query.Direction.ASCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.REMOVED) {
                            productArrayList.remove(dc.document.toObject(Product::class.java))
                        }
                    }
                    myAdapter.notifyDataSetChanged()
                }
            })
    }
    private fun RawDeleteListener() {
        db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).collection("materials").orderBy("materialName", Query.Direction.ASCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.REMOVED) {
                            materialArrayList.remove(dc.document.toObject(Material::class.java))
                        }
                    }
                    myMaterialAdapter.notifyDataSetChanged()
                }
            })
    }

    fun productInfoDialog(product: Product) {
        val productDialogView =
            LayoutInflater.from(this).inflate(R.layout.product_popup, null)
        val productBuilder = AlertDialog.Builder(this)
            .setView(productDialogView)
        val productAlertDialog = productBuilder.show()
        val productName = productDialogView.findViewById<TextView>(R.id.productName)
        val codeNumber = productDialogView.findViewById<EditText>(R.id.codeNumber)
        val priceNumber = productDialogView.findViewById<EditText>(R.id.priceNumber)
        val soldNumber = productDialogView.findViewById<EditText>(R.id.soldNumber)
        val stockNumber = productDialogView.findViewById<EditText>(R.id.stockNumber)

        productName.text = product.productName
        codeNumber.hint = product.productCode.toString()
        codeNumber.setText(product.productCode.toString())
        codeNumber.isEnabled = false
        stockNumber.hint = product.stockLevel.toString()
        priceNumber.hint = product.price.toString()
        priceNumber.setText(product.price.toString())
        soldNumber.hint = product.soldItems.toString()
        //UPDATE PRODUCT
        productDialogView.saveBtn.setOnClickListener(View.OnClickListener {
            val price = priceNumber.text.toString()
            val code = product.productCode.toString()
            var addItems = soldNumber.text.toString()
            var addStock = stockNumber.text.toString()
            val uid = auth.currentUser!!.uid
            val updateProducts= db.collection("users").document(uid).collection("products").document(code)
            updateProducts
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val oldItems = document.getString("soldItems")
                        val old = oldItems!!.toInt()
                        if (soldNumber.text.toString() == "") {
                            addItems = "0"
                        }
                        var add = addItems!!.toInt()
                        var soldItems: Int

                        val totalStock = document.getString("stockLevel")
                        if (stockNumber.text.toString() == "") {
                            addStock = "0"
                        }
                        var stock: Int
                        stock = totalStock!!.toInt() - add
                        var priceInt = price.toInt()
                        updateProducts
                            .update(
                                mapOf(
                                    "price" to price,
                                )
                            )

                        var profit = priceInt * add
                        val income = profit.toString()
                        val name = document.getString("productName")!!
                        if (add > 0) {
                            var number = 0
                            db.collection("users").document(uid).collection("income").get()
                                .addOnSuccessListener { documents ->
                                    number = documents.size()
                                    var item = number.toString()
                                    for (document in documents) {
                                        if (document.getString("item") == item) {
                                            number += 1
                                            item = number.toString()
                                            val profits = HashMap<String, Any>()
                                            profits["profit"] = income
                                            profits["name"] = name
                                            profits["uid"] = uid
                                            profits["date"] = FieldValue.serverTimestamp()
                                            profits["item"] = item
                                            db.collection("users").document(uid)
                                                .collection("income").document(item)
                                                .set(profits)
                                        }
                                    }
                                    val profits = HashMap<String, Any>()
                                    profits["profit"] = income
                                    profits["name"] = document.getString("productName")!!
                                    profits["uid"] = uid
                                    profits["date"] = FieldValue.serverTimestamp()
                                    profits["item"] = item
                                    db.collection("users").document(uid).collection("income")
                                        .document(item)
                                        .set(profits)
                                }
                        }
                        db.collection("users").document(uid).collection("materials")
                            .whereEqualTo("uid", uid)
                            .get()
                            .addOnSuccessListener { documents ->
                                for (material in documents) {
                                    val stockLevel =
                                        material.getString("stockLevel")!!
                                            .toInt()
                                    val threshold =
                                        material.getString("thresholdLevel")!!
                                            .toInt()
                                    if (stockLevel < threshold) {
                                        ctr += 1
                                    }
                                }
                            }

                        db.collection("users").document(uid).collection("products").document(code).collection("ingredients")
                            .whereEqualTo("productId", code)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (ctr == 0 && totalStock!!.toInt() >= add){
                                    for (document in documents) {
                                        if (document != null) {
                                            val quantity = document.getString("quantity")
                                            var usedQuantity = quantity!!.toInt() * addStock.toInt()
                                            val name = document.getString("ingredientName")
                                            db.collection("users").document(uid)
                                                .collection("materials")
                                                .whereEqualTo("materialName", name)
                                                .get()
                                                .addOnSuccessListener { materials ->
                                                    if (materials.isEmpty){
                                                        Toast.makeText(
                                                            applicationContext,
                                                            "$name not found in material inventory. Product made with insufficient materials.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                    else {
                                                        for (material in materials) {
                                                            if (material != null) {
                                                                val stockLevel =
                                                                    material.getString("stockLevel")!!
                                                                        .toInt()
                                                                var currentStockLevel = stockLevel
                                                                if (stockLevel > usedQuantity) {
                                                                    currentStockLevel =
                                                                        stockLevel - usedQuantity
                                                                } else {
                                                                    currentStockLevel = 0
                                                                    Toast.makeText(
                                                                        applicationContext,
                                                                        "$name reached 0. Product made with insufficient materials.",
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                }
                                                                val updateMaterial =
                                                                    db.collection("users")
                                                                        .document(uid)
                                                                        .collection("materials")
                                                                        .document(name.toString())
                                                                updateMaterial
                                                                    .get()
                                                                    .addOnSuccessListener { material ->
                                                                        if (material != null) {
                                                                            updateMaterial
                                                                                .update(
                                                                                    mapOf(
                                                                                        "stockLevel" to currentStockLevel.toString()
                                                                                    )
                                                                                )
                                                                        }
                                                                    }
                                                                stock =
                                                                    totalStock.toInt() + addStock.toInt() - add
                                                                soldItems = old + add
                                                                updateProducts
                                                                    .update(
                                                                        mapOf(
                                                                            "stockLevel" to stock.toString(),
                                                                            "soldItems" to soldItems.toString()
                                                                        )
                                                                    )
                                                                productAlertDialog.dismiss()
                                                                val intent = Intent(
                                                                    this,
                                                                    LoadingActivity::class.java
                                                                )
                                                                intent.putExtra(
                                                                    "activity",
                                                                    "InventoryActivity"
                                                                )
                                                                startActivity(intent)
                                                                overridePendingTransition(0, 0)
                                                                finish()
                                                            }
                                                        }
                                                    }
                                                }
                                        }else{
                                            Log.d(ContentValues.TAG, "No such document")
                                        }
                                    }
                                }else{
                                    val alertDialogView = LayoutInflater.from(this).inflate(R.layout.product_alert, null)
                                    val alertBuilder = AlertDialog.Builder(this).setView(alertDialogView)
                                    val alertAlertDialog = alertBuilder.show()
                                    alertDialogView.cancelBtn.setOnClickListener {
                                        alertAlertDialog.dismiss()
                                    }
                                    alertAlertDialog.show()
                                }
                            }
                    }
            //TO DO: AUTOMATIC LIST UPDATE
            }
        })

        //DELETE PRODUCT
        productDialogView.deleteBtn.setOnClickListener(View.OnClickListener {
            productAlertDialog.dismiss()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Record")
            builder.setMessage("Are you sure you wants to delete this item?")
            builder.setPositiveButton("Yes") { dialogInterface, which ->
                val uid = auth.currentUser!!.uid
                val code = codeNumber.text.toString()
                db.collection("users").document(uid).collection("products").document(code)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                        applicationContext,
                        "Record deleted successfully.",
                        Toast.LENGTH_SHORT
                    ).show() }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                dialogInterface.dismiss()
                DeleteListener()
            }
            builder.setNegativeButton("No") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
            DeleteListener()
        })

        //VIEW INGREDIENTS
        val ingredients = productDialogView.findViewById<TextView>(R.id.viewIngredientList)
        ingredients.setOnClickListener {
            val ingredientDialogView =
                LayoutInflater.from(this).inflate(R.layout.ingredients_list, null)
            val ingredientBuilder = AlertDialog.Builder(this)
                .setView(ingredientDialogView)
            val ingredientAlertDialog = ingredientBuilder.show()

            ingredientRecyclerView = ingredientDialogView.findViewById(R.id.ingredientList)
            ingredientRecyclerView.layoutManager = LinearLayoutManager(this)
            ingredientRecyclerView.setHasFixedSize(true)
            ingredientArrayList = arrayListOf()
            ingredientAdapter = IngredientAdapter(this, ingredientArrayList)
            ingredientRecyclerView.adapter = ingredientAdapter

            db = FirebaseFirestore.getInstance()
            val uid = auth.currentUser!!.uid
            db.collection("users").document(uid).collection("products").document(product.productCode.toString()).collection("ingredients")
                .addSnapshotListener(object : EventListener<QuerySnapshot> {
                    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                        if (error != null) {
                            Log.e("Firestore Error", error.message.toString())
                            return
                        }
                        for (dc: DocumentChange in value?.documentChanges!!) {
                            if (dc.type == DocumentChange.Type.ADDED) {
                                ingredientArrayList.add(dc.document.toObject(Ingredients::class.java))
                            }
                        }
                        ingredientAdapter.notifyDataSetChanged()
                    }
                })
            val productTitle = ingredientDialogView.findViewById<TextView>(R.id.productTitle)
            productTitle.text = product.productName
            ingredientDialogView.addBtn.setOnClickListener {
                val ingredientName = ingredientDialogView.findViewById<EditText>(R.id.ingredientName).text.toString()
                val quantity = ingredientDialogView.findViewById<EditText>(R.id.quantity).text.toString()
                val uid = auth.currentUser!!.uid
                val code = product.productCode.toString()
                val ingredient = HashMap<String, Any>()
                ingredient["ingredientName"] = ingredientName
                ingredient["quantity"] = quantity
                ingredient["productId"] = code
                db.collection("users").document(uid).collection("products")
                    .document(product.productCode.toString()).collection("ingredients")
                    .document(ingredientName).set(ingredient)

            }
            ingredientDialogView.cancelBtn.setOnClickListener {
                ingredientAlertDialog.dismiss()
            }
        }
        productDialogView.cancelBtn.setOnClickListener(View.OnClickListener {
            productAlertDialog.dismiss()
        })
        productAlertDialog.show()
    }
    fun materialInfoDialog(material: Material) {
        val rawDialogView =
            LayoutInflater.from(this).inflate(R.layout.material_popup, null)
        val rawBuilder = AlertDialog.Builder(this)
            .setView(rawDialogView)
        val rawAlertDialog = rawBuilder.show()
        val materialName = rawDialogView.findViewById<TextView>(R.id.materialName)
        val codeNumber = rawDialogView.findViewById<EditText>(R.id.matCodeNumber)
        val priceNumber = rawDialogView.findViewById<EditText>(R.id.materialPriceNumber)
        val stockNumber = rawDialogView.findViewById<EditText>(R.id.stockNumber)
        val thresholdNumber = rawDialogView.findViewById<EditText>(R.id.thresholdLevel)

        materialName.text = material.materialName
        codeNumber.hint = material.materialCode.toString()
        codeNumber.setText(material.materialCode.toString())
        codeNumber.isEnabled = false
        priceNumber.hint = material.price.toString()
        priceNumber.setText(material.price.toString())
        stockNumber.hint = material.stockLevel.toString()
        thresholdNumber.hint = material.thresholdLevel.toString()
        thresholdNumber.setText(material.thresholdLevel.toString())

        if (material.thresholdLevel!!.toInt() > material.stockLevel!!.toInt()){
            val alertDialogView =
                LayoutInflater.from(this).inflate(R.layout.alert_dialog, null)
            val alertBuilder = AlertDialog.Builder(this)
                .setView(alertDialogView)
            val alertAlertDialog = alertBuilder.show()
            val stock = material.stockLevel.toString()
            val message = "You have $stock item/s left for this material. Please restock!"
            val text = alertDialogView.findViewById<TextView>(R.id.message)
            text.text = message
            alertDialogView.cancelBtn.setOnClickListener ( {
                alertAlertDialog.dismiss()
            })
            alertAlertDialog.show()
        }

        //UPDATE MATERIAL
        rawDialogView.saveBtn.setOnClickListener(View.OnClickListener {
            val name = materialName.text.toString()
            val price = priceNumber.text.toString()
            val code = codeNumber.text.toString()
            var addItems = stockNumber.text.toString()
            val thresholdLevel = thresholdNumber.text.toString()
            val uid = auth.currentUser!!.uid
            val updateMaterial = db.collection("users").document(uid).collection("materials").document(name)
            updateMaterial
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val oldItems = document.getString("stockLevel")
                        val old = oldItems!!.toInt()
                        if (addItems == ""){
                            addItems = "0"
                        }
                        val add = addItems!!.toInt()
                        val stockLevel = old + add
                        updateMaterial
                            .update(
                                mapOf(
                                    "price" to price,
                                    "stockLevel" to stockLevel.toString(),
                                    "thresholdLevel" to thresholdLevel
                                )
                            )

                        val priceInt = price!!.toInt()
                        val cost = priceInt * addItems.toInt()
                        val expense = cost.toString()
                        if (add > 0) {
                            var number = 0
                            db.collection("users").document(uid).collection("expenses").get()
                                .addOnSuccessListener { documents ->
                                    number = documents.size()
                                    var item = number.toString()
                                    for (document in documents) {
                                        if (document.getString("item") == item) {
                                            number += 1
                                            item = number.toString()
                                            val expenses = HashMap<String, Any>()
                                            expenses["cost"] = expense
                                            expenses["name"] = name
                                            expenses["uid"] = uid
                                            expenses["item"] = item
                                            expenses["date"] = FieldValue.serverTimestamp()
                                            db.collection("users").document(uid)
                                                .collection("expenses")
                                                .document(item)
                                                .set(expenses)
                                        }
                                    }
                                    val expenses = HashMap<String, Any>()
                                    expenses["cost"] = expense
                                    expenses["name"] = document.getString("materialName")!!
                                    expenses["uid"] = uid
                                    expenses["item"] = item
                                    expenses["date"] = FieldValue.serverTimestamp()
                                    db.collection("users").document(uid).collection("expenses")
                                        .document(item)
                                        .set(expenses)
                                }
                        }
                    }

                }
            rawAlertDialog.dismiss()
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("activity", "InventoryActivity")
            startActivity(intent)
            overridePendingTransition(0,0)
            finish()
            myMaterialAdapter.notifyDataSetChanged()
            //TO DO: AUTOMATIC LIST UPDATE
        })

        //DELETE MATERIAL
        rawDialogView.deleteBtn.setOnClickListener(View.OnClickListener {
            rawAlertDialog.dismiss()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Record")
            builder.setMessage("Are you sure you wants to delete this item?")
            builder.setPositiveButton("Yes") { dialogInterface, which ->
                val uid = auth.currentUser!!.uid
                val name = materialName.text.toString()
                db.collection("users").document(uid).collection("materials").document(name)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                        applicationContext,
                        "Record deleted successfully.",
                        Toast.LENGTH_SHORT
                    ).show() }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                dialogInterface.dismiss()
            // Dialog will be dismissed
            }
            builder.setNegativeButton("No") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
            RawDeleteListener()
        })
        rawDialogView.cancelBtn.setOnClickListener(View.OnClickListener {
            rawAlertDialog.dismiss()
        })
        rawAlertDialog.show()
    }

    fun ingredientInfoDialog(ingredient: Ingredients) {
        val code = ingredient.productId.toString()
        val name = ingredient.ingredientName.toString()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setMessage("Are you sure you wants to delete this item?")
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            val uid = auth.currentUser!!.uid
            db.collection("users").document(uid).collection("products").document(code)
                .collection("ingredients").document(name)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(
                        applicationContext,
                        "Record deleted successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                    db = FirebaseFirestore.getInstance()
                    val uid = auth.currentUser!!.uid
                    db.collection("users").document(uid).collection("products").document(code)
                        .collection("ingredients")
                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                                if (error != null) {
                                    Log.e("Firestore Error", error.message.toString())
                                    return
                                }
                                for (dc: DocumentChange in value?.documentChanges!!) {
                                    if (dc.type == DocumentChange.Type.REMOVED) {
                                        ingredientArrayList.remove(dc.document.toObject(Ingredients::class.java))
                                    }
                                }
                                ingredientAdapter.notifyDataSetChanged()
                            }
                        })
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            dialogInterface.dismiss()
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("activity", "InventoryActivity")
            startActivity(intent)
            overridePendingTransition(0,0)
            finish()
        // Dialog will be dismissed
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}