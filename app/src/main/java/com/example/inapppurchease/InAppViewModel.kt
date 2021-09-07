package com.example.inapppurchease

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import kotlin.collections.ArrayList

class InAppViewModel(application: Application) : AndroidViewModel(application),
    PurchasesUpdatedListener {
    private val TAG = "InAppViewModel"
    private val billingClient: BillingClient =
        BillingClient.newBuilder(application).enablePendingPurchases().setListener(this).build()
    private val isBillingServiceConnected = MutableLiveData<Boolean>()
    private val errorMessage = MutableLiveData<String>()
    private val purchaseSkuList = MutableLiveData<List<String>>()
    private val allAvailableItemList = MutableLiveData<List<MySkuDetails>>()

    init {
        showLog("startConnection")
        connectBillingService()
    }

    private fun connectBillingService() {
        showLog("connectBillingService")
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isBillingServiceConnected.value = true
                    showLog("Billing Service Connected")
                } else {
                    errorMessage.value = "Error"
                    showLog("Billing Service Connected Failed Cause of ${billingResult.debugMessage} responseCode : ${billingResult.responseCode}")
                }
            }

            override fun onBillingServiceDisconnected() {
                isBillingServiceConnected.value = false
                showLog("Billing Service Disconnected")
            }
        })
    }

    fun requestPurchaseItem() {
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (list != null) {
                    showLog("Some Purchase Item Found")
                    val skusList = ArrayList<String>()
                    for (purchaseItem in list) {
                            skusList.add(purchaseItem.sku)
                    }
                    purchaseSkuList.value = skusList
                } else {
                    showLog("No Purchase Item Found.")
                }
            } else {
                showLog("Purchase history response failed")
            }
        }
    }

    fun requestAllAvailableItem() {
        //Hare You Need To Pass Your Product Sku Ids Which You Create In Your Play Console
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuIdList()).setType(BillingClient.SkuType.INAPP)

        billingClient.querySkuDetailsAsync(params.build()) { billingResult, detailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (detailsList != null) {
                    showLog("Item Available")
                    val mySkuDetailsList=ArrayList<MySkuDetails>()
                    detailsList.forEach { skuDetail ->mySkuDetailsList.add(MySkuDetails(false,skuDetail)) }
                    allAvailableItemList.value=mySkuDetailsList
                } else {
                    errorMessage.value = "No Item Available Right Now"
                    showLog("No Item Available Right Now")
                }
            } else {
                showLog("Failed To get Available Items")
            }
        }
    }

    fun purchaseAnItem(context: AppCompatActivity, flowParams: BillingFlowParams) {
        val responseCode = billingClient.launchBillingFlow(context, flowParams).responseCode
        if (responseCode == BillingClient.BillingResponseCode.OK)
            showLog("Purchase Flow Start")
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            if (list != null) {
                for (purchaseItem in list) {
                    //Purchase Status Change.So we Need to check which product are purchase or which not
                    val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchaseItem.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(params) { purchaseResult ->
                        if (purchaseResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            //hare a product is purchase so we need to update the purchaseList
                            requestPurchaseItem()
                            showLog("This Product Purchased : ${purchaseItem.sku}")
                        }else{
                            showLog("This Not Product Purchased : ${purchaseItem.sku}")
                        }
                    }
                }
            } else showLog("No Item Purchased")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
             showLog("User cancelled the Purchase process")
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE) {
            showLog("Purchase Service Unavailable")
        } else {
            showLog("Unknown Error")
        }
    }

    private fun skuIdList(): MutableList<String> {
        val productIdsList = ArrayList<String>()
        productIdsList.add(BANNER_ADD_SKU_ID)
        productIdsList.add(INTERSTITIAL_ADD_SKU_ID)
        return productIdsList
    }


    // getters
    fun isBillingServiceConnected(): MutableLiveData<Boolean> {
        return isBillingServiceConnected
    }
    fun getAllAvailableItemList(): MutableLiveData<List<MySkuDetails>> {
        return allAvailableItemList
    }
    fun getPurchaseSkuList(): MutableLiveData<List<String>> {
        return purchaseSkuList
    }
    fun getErrorMessage(): MutableLiveData<String> {
        return errorMessage
    }
    // getters

    //Sku Ids
    val BANNER_ADD_SKU_ID = "your sku id"
    val INTERSTITIAL_ADD_SKU_ID = "your sku id"

    private fun showLog(message: String) {
        Log.e(TAG, "Message : $message")
    }
}