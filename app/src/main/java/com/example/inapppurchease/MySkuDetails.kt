package com.example.inapppurchease

import com.android.billingclient.api.SkuDetails

data class MySkuDetails(
    var isAlreadyPurchased: Boolean = false,
    var skuDetails: SkuDetails
)