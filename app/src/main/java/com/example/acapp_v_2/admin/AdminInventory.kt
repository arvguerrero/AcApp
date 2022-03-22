package com.example.acapp_v_2.activities

import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acapp_v_2.R
import com.example.acapp_v_2.admin.AdminHome
import com.example.acapp_v_2.models.Expense
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
import kotlinx.android.synthetic.main.activity_admin_inventory.*
import kotlinx.android.synthetic.main.activity_inventory.*
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


class AdminInventory : AppCompatActivity() {
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
        setContentView(R.layout.activity_admin_inventory)

        val back = findViewById<View>(R.id.back)
        back.setOnClickListener {
            val intent = Intent(this, AdminHome::class.java)
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

        val deleteProduct = findViewById<Button>(R.id.deleteProducts)
        deleteProduct.setOnClickListener {
            val uid = auth.currentUser!!.uid
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Records")
            builder.setMessage("Are you sure you wants to delete all the records?")
            builder.setPositiveButton("Yes") { dialogInterface, which ->
                db.collection("users").document(uid).collection("products")
                    .whereEqualTo("uid", uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            if (document != null) {
                                val code = document.getString("productCode").toString()
                                db.collection("users").document(uid).collection("products")
                                    .document(code).delete()
                            } else {
                                Log.d(ContentValues.TAG, "No such document")
                            }
                        }
                    }
                    .addOnFailureListener {
                        Log.w(ContentValues.TAG, "Error getting documents:")
                    }
                dialogInterface.dismiss()
                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtra("activity", "AdminInventory")
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()// Dialog will be dismissed
            }
            builder.setNegativeButton("No") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
        val deleteMaterial = findViewById<Button>(R.id.deleteMaterials)
        deleteMaterial.setOnClickListener {
            val uid = auth.currentUser!!.uid
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Records")
            builder.setMessage("Are you sure you wants to delete all the records?")
            builder.setPositiveButton("Yes") { dialogInterface, which ->
                db.collection("users").document(uid).collection("materials")
                    .whereEqualTo("uid", uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            if (document != null) {
                                val code = document.getString("materialName").toString()
                                db.collection("users").document(uid).collection("materials")
                                    .document(code).delete()
                            } else {
                                Log.d(ContentValues.TAG, "No such document")
                            }
                        }
                    }
                    .addOnFailureListener {
                        Log.w(ContentValues.TAG, "Error getting documents:")
                    }
                dialogInterface.dismiss()
                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtra("activity", "AdminInventory")
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()// Dialog will be dismissed
            }
            builder.setNegativeButton("No") { dialogInterface, which ->
                dialogInterface.dismiss()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
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
            LayoutInflater.from(this).inflate(R.layout.admin_product, null)
        val productBuilder = AlertDialog.Builder(this)
            .setView(productDialogView)
        val productAlertDialog = productBuilder.show()
        val productCode = productDialogView.findViewById<TextView>(R.id.productCode)
        val productName = productDialogView.findViewById<EditText>(R.id.productName)
        val priceNumber = productDialogView.findViewById<EditText>(R.id.priceNumber)
        val soldNumber = productDialogView.findViewById<EditText>(R.id.soldNumber)
        val stockNumber = productDialogView.findViewById<EditText>(R.id.stockNumber)

        productCode.text = product.productCode
        productName.hint = product.productName.toString()
        productName.setText(product.productName.toString())
        stockNumber.hint = product.stockLevel.toString()
        stockNumber.setText(product.stockLevel.toString())
        priceNumber.hint = product.price.toString()
        priceNumber.setText(product.price.toString())
        soldNumber.hint = product.soldItems.toString()
        soldNumber.setText(product.soldItems.toString())
        //UPDATE PRODUCT
        productDialogView.saveBtn.setOnClickListener(View.OnClickListener {
            val price = priceNumber.text.toString()
            val name = productName.text.toString()
            val code = product.productCode.toString()
            var soldNumber = soldNumber.text.toString()
            var stockNumber = stockNumber.text.toString()
            val uid = auth.currentUser!!.uid
            val updateProducts= db.collection("users").document(uid).collection("products").document(code)
            updateProducts
                .get()
                .addOnSuccessListener { document ->
                    if (document != null){
                        updateProducts
                            .update(mapOf(
                                "productName" to name,
                                "price" to price,
                                "soldItems" to soldNumber,
                                "stockLevel" to stockNumber
                            ))
                        }
                    }
            productAlertDialog.dismiss()
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("activity", "AdminInventory")
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
            myAdapter.notifyDataSetChanged()
            //TO DO: AUTOMATIC LIST UPDATE
        })

        productDialogView.deleteBtn.setOnClickListener(View.OnClickListener {
            productAlertDialog.dismiss()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Record")
            builder.setMessage("Are you sure you wants to delete this item?")
            builder.setPositiveButton("Yes") { dialogInterface, which ->
                val uid = auth.currentUser!!.uid
                val code = product.productCode.toString()
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
            LayoutInflater.from(this).inflate(R.layout.admin_material, null)
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
        priceNumber.hint = material.price.toString()
        priceNumber.setText(material.price.toString())
        stockNumber.hint = material.stockLevel.toString()
        stockNumber.setText(material.stockLevel.toString())
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
                    if (document != null) {
                        updateMaterial
                            .update(
                                mapOf(
                                    "materialCode" to code,
                                    "price" to price,
                                    "stockLevel" to addItems,
                                    "thresholdLevel" to thresholdLevel
                                )
                            )
                    }
                }
            rawAlertDialog.dismiss()
            val intent = Intent(this, LoadingActivity::class.java)
            intent.putExtra("activity", "AdminInventory")
            startActivity(intent)
            overridePendingTransition(0, 0)
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
                val code = materialName.text.toString()
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

 /*   fun expenseInfoDialog(expense: Expense) {
    }*/
}

