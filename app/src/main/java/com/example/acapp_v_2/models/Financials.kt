package com.example.acapp_v_2.models

import com.google.firebase.Timestamp

data class Financials(
    var profit: String ?= null,
    var name: String ?= null,
    var uid: String ?= null,
    var date: Timestamp ?= null
)
