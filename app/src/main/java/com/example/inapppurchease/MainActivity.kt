package com.example.inapppurchease

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.BillingFlowParams
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: InAppViewModel
    private lateinit var adapter: InAppAdapter
    private lateinit var availableProduct: List<MySkuDetails>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(InAppViewModel::class.java)


        viewModel.isBillingServiceConnected().observe(this, Observer { connected ->
            if (connected) {
                viewModel.requestAllAvailableItem()
            }
        })
        viewModel.getAllAvailableItemList().observe(this, Observer {
            availableProduct = it
            adapter = InAppAdapter(object : InAppItemClickListner {
                //Its start purchase flow
                override fun click(position: Int) {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(it[position].skuDetails)
                        .build()
                    viewModel.purchaseAnItem(this@MainActivity, flowParams)
                }
            })

            //Call Purchased Item after available item.
            viewModel.requestPurchaseItem()

            //Set Adapter
            in_app_recycler_view.layoutManager = LinearLayoutManager(this)
            in_app_recycler_view.setHasFixedSize(true)
            in_app_recycler_view.adapter = adapter
            adapter.differ.submitList(availableProduct)
        })

        viewModel.getPurchaseSkuList().observe(this, Observer { purchaseSkuList ->
            if (this::adapter.isInitialized && this::availableProduct.isInitialized) {
                //Update Purchase List
                for (purchaseSku in purchaseSkuList)
                    availableProduct.find { it.skuDetails.sku.equals(purchaseSku, true)}?.isAlreadyPurchased = true

                //Update Adapter
                in_app_recycler_view.adapter = adapter
                adapter.differ.submitList(availableProduct)
            }
        })
    }
}