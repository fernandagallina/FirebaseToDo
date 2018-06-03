package fernandagallina.firebasetodo

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), ItemRowListener {

    private lateinit var database: DatabaseReference
    private lateinit var adapter: ToDoItemAdapter
    private lateinit var toDoItemList: MutableList<ToDoItem>
    private lateinit var listViewItems: ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            addNewItemDialog()
        }

        listViewItems = findViewById<View>(R.id.items_list) as ListView
        toDoItemList = mutableListOf()
        adapter = ToDoItemAdapter(this, toDoItemList!!)
        listViewItems!!.adapter = adapter

        database = FirebaseDatabase.getInstance().reference
        database.orderByKey().addListenerForSingleValueEvent(itemListener)
    }

    override fun modifyItemState(itemObjectId: String, isDone: Boolean) {

        val itemReference = database.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        itemReference.child("done").setValue(isDone);
    }

    //delete an item
    override fun onItemDelete(itemObjectId: String, position: Int) {

        //get child reference in database via the ObjectID
        val itemReference = database.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        //deletion can be done via removeValue() method
        itemReference.removeValue()
        adapter.removeItem(position)
    }

    private fun addNewItemDialog() {
        val alert = AlertDialog.Builder(this)
        val itemEditText = EditText(this)
        alert.setMessage("Add New Item")
        alert.setTitle("Enter To Do Item Text")
        alert.setView(itemEditText)
        alert.setPositiveButton("Submit") { dialog, positiveButton ->
            val todoItem = ToDoItem.create()
            todoItem.itemText = itemEditText.text.toString()
            todoItem.done = false
            //We first make a push so that a new item is made with a unique ID
            val newItem = database.child(Constants.FIREBASE_ITEM).push()
            todoItem.objectId = newItem.key
            //then, we used the reference to set the value on that ID
            newItem.setValue(todoItem)
            dialog.dismiss()
            Toast.makeText(this, "Item saved with ID " + todoItem.objectId, Toast.LENGTH_SHORT).show()
        }
        alert.show()
    }

    private var itemListener: ValueEventListener = object : ValueEventListener {

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Get Post object and use the values to update the UI
            addDataToList(dataSnapshot)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    private fun addDataToList(dataSnapshot: DataSnapshot) {
        val items = dataSnapshot.children.iterator()
        if (items.hasNext()) {
            val toDoListIndex = items.next()
            val itemsIterator = toDoListIndex.children.iterator()

            itemsIterator.forEach { it: DataSnapshot? ->
                run {
                    val todoItem = ToDoItem.create()
                    val map = it?.value as HashMap<*, *>

                    todoItem.objectId = it.key
                    todoItem.done = map.get("done") as Boolean?
                    todoItem.itemText = map.get("itemText") as String?
                    toDoItemList!!.add(todoItem)
                }
            }
        }
        //alert adapter that has changed
        adapter.notifyDataSetChanged()
    }
}
