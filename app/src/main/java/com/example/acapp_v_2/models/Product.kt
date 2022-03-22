package com.example.acapp_v_2.models

data class Product(
    var productName: String ?= null,
    var productCode: String ?= null,
    var stockLevel: String ?= null,
    var price: String ?= null,
    var soldItems: String ?= null,
    var uid: String ?= null
)
