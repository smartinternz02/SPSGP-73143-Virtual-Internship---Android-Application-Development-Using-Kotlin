package com.example.groceryapp

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() , GroceryRVAdapter.GroceryItemClickInterface{

    lateinit var itemRv: RecyclerView
    lateinit var addFAB: FloatingActionButton
    lateinit var list: List<GroceryItems>
    lateinit var groceryRVAdapter: GroceryRVAdapter
    lateinit var groceryViewModel: GroceryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        itemRv = findViewById(R.id.idRVItems)
        addFAB = findViewById(R.id.action_button)
        list = ArrayList()
        groceryRVAdapter = GroceryRVAdapter(list, this)
        itemRv.layoutManager = LinearLayoutManager(this)
        itemRv.adapter = groceryRVAdapter
        val groceryRepository = GroceryRepository(GroceryDatabase(this))
        val factory = GroceryViewModelFactory(groceryRepository)
        groceryViewModel = ViewModelProvider(this,factory).get(GroceryViewModel::class.java)
        groceryViewModel.getAllGroceryItems().observe(this,Observer{
            groceryRVAdapter.list = it
            groceryRVAdapter.notifyDataSetChanged()
        })

        addFAB.setOnClickListener {
            openDialog()
        }

    }

    private fun openDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.grocery_add_dialog)

        val cancel: Button = dialog.findViewById(R.id.idBtnCancel)
        val add: Button = dialog.findViewById(R.id.idBtnadd)
        val item: EditText = dialog.findViewById(R.id.idEdtItem)
        val price: EditText = dialog.findViewById(R.id.idEdtItemPrice)
        val quantity: EditText = dialog.findViewById(R.id.idEdtItemQuantity)

        cancel.setOnClickListener { dialog.dismiss() }

        add.setOnClickListener {
            if (validateInput(item, quantity, price)) {
                addItemToDB(item.text.toString(), quantity.text.toString(), price.text.toString())
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addItemToDB(itemName: String, quantity: String, price: String) {
        val items = GroceryItems(itemName, quantity.toInt(), price.toInt())
        groceryViewModel.insert(items)
        Toast.makeText(this, "Item Added!", Toast.LENGTH_SHORT).show()
        groceryRVAdapter.notifyDataSetChanged()
    }

    private fun validateInput(item: TextView, quantity: TextView, price: TextView): Boolean {
        if (item.text.isEmpty()) {
            item.error = "Name is empty."
            return false
        }
        if (quantity.text.isEmpty()) {
            quantity.error = "Quantity is empty."
            return false
        }
        if (price.text.isEmpty()) {
            price.error = "Price is empty."
            return false
        }
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemClick(groceryItems: GroceryItems) {
        groceryViewModel.delete(groceryItems)
        groceryRVAdapter.notifyDataSetChanged()
        Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show()
    }
}