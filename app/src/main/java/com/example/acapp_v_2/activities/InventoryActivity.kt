package com.example.acapp_v_2.activities

import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
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
import kotlinx.android.synthetic.main.activity_inventory.addBtn
import kotlinx.android.synthetic.main.activity_inventory.productTitle
import kotlinx.android.synthetic.main.add_product_dialog.view.*
import kotlinx.android.synthetic.main.add_product_dialog.view.cancelBtn
import kotlinx.android.synthetic.main.add_product_dialog.view.saveBtn
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)
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
        addBtn.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.material_product, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
            val mAlertDialog = mBuilder.show()
            //PRODUCT
            mDialogView.prodBtn.setOnClickListener {
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
                    val stockedItems =
                        productDialogView.findViewById<EditText>(R.id.stockLevel).text.toString()
                    val uid = auth.currentUser!!.uid
                    val product = HashMap<String, Any>()
                    product["productName"] = name
                    product["price"] = price
                    product["productCode"] = code
                    product["stockLevel"] = stockedItems
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
                }
                productDialogView.cancelBtn.setOnClickListener {
                    productAlertDialog.dismiss()
                }
                mAlertDialog.dismiss()
            }
            mDialogView.rawBtn.setOnClickListener {
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
                    val expenses = HashMap<String, Any>()
                    expenses["cost"] = expense
                    expenses["name"] = name
                    expenses["uid"] = uid
                    db.collection("users").document(uid).collection("expenses").document()
                        .set(expenses)

                    rawAlertDialog.dismiss()
                }

                rawDialogView.cancelBtn.setOnClickListener {
                    rawAlertDialog.dismiss()
                }
                mAlertDialog.dismiss()
            }
        }

        val searchView = findViewById<SearchView>(R.id.searchEdt)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(query: String?): Boolean {
                return false
            }
        })
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
                    .update(mapOf(
                        "expenses" to cost
                    ))
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
                    .update(mapOf(
                        "income" to income
                    ))
            }
            .addOnFailureListener {
                Log.w(ContentValues.TAG, "Error getting documents:")
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
                    if (document != null){
                        val oldItems = document.getString("soldItems")
                        val old = oldItems!!.toInt()
                        if (soldNumber.text.toString() == ""){
                            addItems = "0"
                        }
                        val add = addItems!!.toInt()
                        var soldItems = old + add
                        val totalStock = document.getString("stockLevel")
                        if (stockNumber.text.toString() == ""){
                            addStock = "0"
                        }
                        var stock = totalStock!!.toInt() + addStock.toInt() - add
                        updateProducts
                            .update(mapOf(
                                "price" to price,
                                "soldItems" to soldItems.toString(),
                                "stockLevel" to stock.toString()
                            ))
                        val priceInt = price!!.toInt()
                        var profit = priceInt * add
                        val income = profit.toString()
                        if (add > 0) {
                            val profits = HashMap<String, Any>()
                            profits["profit"] = income
                            profits["name"] = document.getString("productName")!!
                            profits["uid"] = uid
                            profits["date"] = FieldValue.serverTimestamp()
                            db.collection("users").document(uid).collection("income").document()
                                .set(profits)
                        }

                        db.collection("users").document(uid).collection("products").document(code).collection("ingredients")
                            .whereEqualTo("productId", code)
                            .get()
                            .addOnSuccessListener { documents ->
                                for (document in documents) {
                                    if (document != null) {
                                        val quantity = document.getString("quantity")
                                        val usedQuantity = quantity!!.toInt() * addStock.toInt()
                                        val name = document.getString("ingredientName")
                                        db.collection("users").document(uid).collection("materials")
                                            .whereEqualTo("materialName", name)
                                            .get()
                                            .addOnSuccessListener { documents ->
                                                for (document in documents) {
                                                    val stockLevel =
                                                        document.getString("stockLevel")
                                                    val currentStockLevel =
                                                        stockLevel!!.toInt() - usedQuantity
                                                    val updateMaterial =
                                                        db.collection("users").document(uid)
                                                            .collection("materials").document(name.toString())
                                                    updateMaterial
                                                        .get()
                                                        .addOnSuccessListener { document ->
                                                            if (document != null) {
                                                                updateMaterial
                                                                    .update(
                                                                        mapOf(
                                                                            "stockLevel" to currentStockLevel.toString(),
                                                                        )
                                                                    )
                                                            }
                                                        }
                                                }
                                            }
                                    } else {
                                        Log.d(ContentValues.TAG, "No such document")
                                    }
                                }
                            }
                    }
                }
            productAlertDialog.dismiss()
            Toast.makeText(
                applicationContext,
                "Record updated successfully.",
                Toast.LENGTH_SHORT
            ).show()
            myAdapter.notifyDataSetChanged()
            //TO DO: AUTOMATIC LIST UPDATE
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
                dialogInterface.dismiss() // Dialog will be dismissed
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

        //UPDATE MATERIAL
        rawDialogView.saveBtn.setOnClickListener(View.OnClickListener {
            val name = materialName.text.toString()
            val price = priceNumber.text.toString()
            val code = codeNumber.text.toString()
            val addItems = stockNumber.text.toString()
            val thresholdLevel = thresholdNumber.text.toString()
            val uid = auth.currentUser!!.uid
            val updateMaterial = db.collection("users").document(uid).collection("materials").document(name)
            updateMaterial
                .get()
                .addOnSuccessListener { document ->
                    if (document != null){
                        val oldItems = document.getString("stockLevel")
                        val old = oldItems!!.toInt()
                        val add = addItems!!.toInt()
                        val stockLevel = old + add
                        updateMaterial
                            .update(mapOf(
                            "price" to price,
                            "stockLevel" to stockLevel.toString(),
                            "thresholdLevel" to thresholdLevel
                        ))

                        val priceInt = price!!.toInt()
                        val cost = priceInt * add
                        val expense = cost.toString()
                        val expenses = HashMap<String, Any>()
                        expenses["cost"] = expense
                        expenses["name"] = document.getString("materialName")!!
                        expenses["uid"] = uid
                        db.collection("users").document(uid).collection("expenses").document()
                            .set(expenses)
                    }
                }
            rawAlertDialog.dismiss()
            Toast.makeText(
                applicationContext,
                "Record updated successfully.",
                Toast.LENGTH_SHORT
            ).show()
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
                val code = codeNumber.text.toString()
                db.collection("users").document(uid).collection("materials").document(code)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                        applicationContext,
                        "Record deleted successfully.",
                        Toast.LENGTH_SHORT
                    ).show() }
                    .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                dialogInterface.dismiss() // Dialog will be dismissed
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
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setMessage("Are you sure you wants to delete this item?")
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            val uid = auth.currentUser!!.uid
            db.collection("users").document(uid).collection("products").document(code)
                .collection("ingredients").document(ingredient.ingredientName.toString())
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
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}

