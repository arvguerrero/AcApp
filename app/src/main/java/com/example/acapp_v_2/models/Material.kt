package com.example.acapp_v_2.models

data class Material(
    var materialName: String ?= null,
    var materialCode: String ?= null,
    var price: String ?= null,
    var stockLevel: String ?= null,
    var thresholdLevel: String ?= null,
    var uid: String ?= null
)
