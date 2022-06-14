package com.example.mds

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

const val EXTRA_USER_MAP = "EXTRA_USER_MAP"
class MainActivity : AppCompatActivity() {

    private  var layoutManager: RecyclerView.LayoutManager? = null
    private  var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null
    private  lateinit var complaintList: ArrayList<ComplaintModel>
    private  lateinit var dbref : DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var allComplaint: Button
    private lateinit var myComplaint: Button
    private lateinit var btnLogout: android.view.View
    private lateinit var addComplaint: android.view.View
    private lateinit var displayMap: android.view.View
    private lateinit var roleUser: String
    private lateinit var institutionName: String
    private lateinit var searchBar: SearchView
    private val cameraRequestId  = 1222
    private val FINE_LOCATION_RQ = 101
    private val COARSE_LOCATION_RQ = 102
    private val CAMERA_RQ = 103


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recylerView)
        allComplaint = findViewById(R.id.allComplaints)
        myComplaint = findViewById(R.id.myComplaints)
        addComplaint = findViewById(R.id.addComplaint)
        btnLogout = findViewById(R.id.logout)
        searchBar = findViewById(R.id.search_bar)
        displayMap = findViewById(R.id.displayMap)

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        complaintList = arrayListOf<ComplaintModel>()
        complaintList.clear()

        val user = mAuth.currentUser
        dbref = FirebaseDatabase.getInstance().getReference("Users")
        dbref.child(user?.uid.toString()).addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    roleUser =  snapshot.child("role").value.toString()
                    if(snapshot.child("role").value.toString() == "user") {
                        getInitialUserData()


                    }
                    else if(snapshot.child("role").value.toString() == "institution")
                    {
                        institutionName = snapshot.child("username").value.toString()
                        getInitialInstitutionData(snapshot.child("username").value.toString())


                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "User not found", Toast.LENGTH_SHORT).show()
            }
        })



        allComplaint.setOnClickListener {
            getAllComplaintsData()


        }
        myComplaint.setOnClickListener {
            if(roleUser == "user")
            {
                getUserComplaintsData()

            }
            else if(roleUser == "institution")
            {
                getInstitutionComplaintsData(institutionName)

            }
        }
        addComplaint.setOnClickListener {
             val intent = Intent(this, ComplaintActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            );
            finish()
        }
        btnLogout.setOnClickListener {
            mAuth = FirebaseAuth.getInstance()
            val user = mAuth.currentUser
            val token:String = ""
            FirebaseDatabase.getInstance().getReference("Users").child(user?.uid.toString()).child("deviceToken").setValue(token).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    Log.w("User ", user?.uid.toString())
                } else {
                    Log.w("User ", "Failed ")
                }
            }
            mAuth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left,
                R.anim.slide_out_right);
            finish()
        }
        displayMap.setOnClickListener{
            if(complaintList.isEmpty())
            {
                Toast.makeText(this,"You don't have any complaint",Toast.LENGTH_SHORT).show()
            }
            else {
                val intent = Intent(this@MainActivity, MapsActivity::class.java)
                intent.putExtra(EXTRA_USER_MAP, complaintList)
                startActivity(intent);
                overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                );
            }
        }

        adapter = RecyclerAdapter(complaintList)

        setUpSearchView()

    }


    private fun setUpSearchView() {
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextSubmit(query: String?): Boolean {
                (adapter as RecyclerAdapter).getFilter().filter(query)
                recyclerView.adapter?.notifyDataSetChanged()
                return true
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                (adapter as RecyclerAdapter).getFilter().filter(newText);
                recyclerView.adapter?.notifyDataSetChanged()
                return true
            }

        })
    }




    private fun getInitialInstitutionData(institutionName: String)
    {
        complaintList.clear()
        val user = mAuth.currentUser
        dbref = FirebaseDatabase.getInstance().getReference("Complaints")
        dbref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    Log.w("database start","Ok")
                    for(complaintsSnapshot in snapshot.children){

                        val complaint = complaintsSnapshot.getValue(ComplaintModel::class.java)
                        if (complaint != null) {
                            if(complaint.institutionName == institutionName || complaint.userId == user?.uid.toString())
                                complaintList.add(complaint!!)
                        }

                    }

                    adapter = RecyclerAdapter(complaintList)
                    if(recyclerView.adapter?.itemCount == null)
                    {
                        recyclerView.adapter  = adapter
                        setUpSearchView()

                    }
                    else {
                        getInstitutionComplaintsData(institutionName)
                    }
                    (adapter as RecyclerAdapter).onItemClick =
                        {
                            val intent = Intent(this@MainActivity, SingleComplaintActivity::class.java)
                            intent.putExtra("complaint",it)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right,
                                R.anim.slide_out_left);
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getInitialUserData()
    {
        complaintList.clear()
        val user = mAuth.currentUser
        dbref = FirebaseDatabase.getInstance().getReference("Complaints")
        dbref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    Log.w("database start","Ok")
                    for(complaintsSnapshot in snapshot.children){

                        val complaint = complaintsSnapshot.getValue(ComplaintModel::class.java)
                        if (complaint != null) {
                            if(complaint.userId.toString() == user?.uid.toString())
                                complaintList.add(complaint!!)
                        }

                    }

                    adapter = RecyclerAdapter(complaintList)
                    if(recyclerView.adapter?.itemCount == null)
                    {
                        recyclerView.adapter  = adapter
                        setUpSearchView()

                    }
                    else {
                        getUserComplaintsData()

                    }
                    (adapter as RecyclerAdapter).onItemClick =
                        {
                            val intent = Intent(this@MainActivity, SingleComplaintActivity::class.java)
                            intent.putExtra("complaint",it)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right,
                                R.anim.slide_out_left);
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getInstitutionComplaintsData(institutionName: String)
    {
        complaintList.clear()
        val user = mAuth.currentUser
        dbref = FirebaseDatabase.getInstance().getReference("Complaints")
        dbref.addValueEventListener(object :ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    Log.w("database start",institutionName)
                    for(complaintsSnapshot in snapshot.children){

                        val complaint = complaintsSnapshot.getValue(ComplaintModel::class.java)
                        if (complaint != null) {
                            if(complaint.institutionName == institutionName || complaint.userId == user?.uid.toString())
                                complaintList.add(complaint!!)
                        }

                    }

                    adapter = RecyclerAdapter(complaintList)
                    recyclerView.adapter?.notifyDataSetChanged()
                    setUpSearchView()
                    (adapter as RecyclerAdapter).onItemClick =
                        {
                            val intent = Intent(this@MainActivity, SingleComplaintActivity::class.java)
                            intent.putExtra("complaint",it)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right,
                                R.anim.slide_out_left);
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getUserComplaintsData()
    {
        complaintList.clear()
        val user = mAuth.currentUser
        dbref = FirebaseDatabase.getInstance().getReference("Complaints")
        dbref.addValueEventListener(object :ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    Log.w("database start","Ok")
                    for(complaintsSnapshot in snapshot.children){

                        val complaint = complaintsSnapshot.getValue(ComplaintModel::class.java)
                        if (complaint != null) {
                            if(complaint.userId.toString() == user?.uid.toString())
                                complaintList.add(complaint!!)
                        }

                    }

                    adapter = RecyclerAdapter(complaintList)
                    recyclerView.adapter?.notifyDataSetChanged()
                    setUpSearchView()
                    (adapter as RecyclerAdapter).onItemClick =
                        {
                            val intent = Intent(this@MainActivity, SingleComplaintActivity::class.java)
                            intent.putExtra("complaint",it)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right,
                                R.anim.slide_out_left);
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getAllComplaintsData()
    {
        complaintList.clear()
        dbref = FirebaseDatabase.getInstance().getReference("Complaints")
        dbref.addValueEventListener(object :ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    Log.w("database start","Ok")
                    for(complaintsSnapshot in snapshot.children){

                        val complaint = complaintsSnapshot.getValue(ComplaintModel::class.java)
                        complaintList.add(complaint!!)

                    }

                    adapter = RecyclerAdapter(complaintList)
                    recyclerView.adapter?.notifyDataSetChanged()
                    setUpSearchView()
                    (adapter as RecyclerAdapter).onItemClick =
                        {
                            val intent = Intent(this@MainActivity, SingleComplaintActivity::class.java)
                            intent.putExtra("complaint",it)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_right,
                                R.anim.slide_out_left);
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

  
    }



}