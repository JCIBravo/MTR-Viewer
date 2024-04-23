package com.jcibravo.mtr_viewer

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.forEach
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.processphoenix.ProcessPhoenix
import com.jcibravo.mtr_viewer.api.RetrofitSingleton
import com.jcibravo.mtr_viewer.classes.MTRAddress
import com.jcibravo.mtr_viewer.ui.OnListClickListener
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class ConnectionFragment : Fragment(), OnListClickListener {
    private lateinit var navigation : BottomNavigationView
    private lateinit var loading : ProgressBar
    private lateinit var connectedDiv : LinearLayout
    private lateinit var disconnectedDiv : LinearLayout
    private lateinit var hostnameField : EditText
    private lateinit var hostnameTextView : TextView
    private lateinit var addListButton : ImageButton
    private lateinit var listButton : ImageButton
    private lateinit var connectToHostButton : Button
    private lateinit var reconnectToHostButton : Button
    private lateinit var disconnectHostButton : Button
    private lateinit var openURLWebButton : Button

    private lateinit var listButtonAlertDialog : AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navigation = requireActivity().findViewById(R.id.nav_view)
        return inflater.inflate(R.layout.fragment_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Stuff
        loading = view.findViewById(R.id.progressBarStatus)
        connectedDiv = view.findViewById(R.id.connectedHostDiv)
        disconnectedDiv = view.findViewById(R.id.hostDiv)
        hostnameField = view.findViewById(R.id.hostnameEditText)
        hostnameTextView = view.findViewById(R.id.hostnameTextView)
        addListButton = view.findViewById(R.id.addBtn)
        listButton = view.findViewById(R.id.listBtn)
        connectToHostButton = view.findViewById(R.id.hostnameConnectBtn)
        reconnectToHostButton = view.findViewById(R.id.reloadBtn)
        disconnectHostButton = view.findViewById(R.id.disconnectBtn)
        openURLWebButton = view.findViewById(R.id.openURLBtn)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner)
                { /* Don't do anything. */ }

        addListButton.setOnClickListener {
            val addListDialogBuilder = AlertDialog.Builder(requireContext())
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_addhost, null)
            addListDialogBuilder.setView(dialogView)

            val hostName = dialogView.findViewById<EditText>(R.id.hostNameEdit)
            val hostUri = dialogView.findViewById<EditText>(R.id.hostURIEdit)
            if (hostnameField.text.isNotEmpty()) hostUri.text = hostnameField.text

            addListDialogBuilder.setPositiveButton(requireContext().getString(R.string.add)) { dialog, _ ->
                if (hostName.text.isNotEmpty() && hostUri.text.isNotEmpty()) {
                    hostName.text.toString()
                    hostUri.text.toString().lowercase()

                    savedAddresses.add(MTRAddress(hostName.text.toString(), hostUri.text.toString()))
                    val json = gson.toJson(savedAddresses)
                    val filePath = File(requireContext().applicationContext.filesDir, "hosts.json")
                    FileOutputStream(filePath).use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(json)
                        }
                    }

                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.fields_required), Toast.LENGTH_SHORT).show()
                }
            }

            addListDialogBuilder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }

            // Crear y mostrar el AlertDialog
            val addToListDialog = addListDialogBuilder.create()
            addToListDialog.show()
        }

        listButton.setOnClickListener {
            if (savedAddresses.isNotEmpty()) {
                val adapter = AdressesListAdapter(requireContext(), savedAddresses, this)
                val listView = ListView(requireContext())
                listView.adapter = adapter

                listButtonAlertDialog = AlertDialog.Builder(requireContext())
                    .setTitle(requireContext().getString(R.string.choose_address))
                    .setView(listView)
                    .create()

                listView.setOnItemClickListener { _, _, position, _ ->
                    onClick(savedAddresses[position])
                }

                listButtonAlertDialog.show()
            } else {
                Toast.makeText(requireContext(), requireContext().getText(R.string.no_hosts), Toast.LENGTH_SHORT).show()
            }
        }

        //Add the behavior to the "stuff"
        connectToHostButton.setOnClickListener {
            lifecycleScope.launch {
                connectToHost(hostnameField.text.toString())
            }
        }

        disconnectHostButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(requireContext().getString(R.string.connection))
            builder.setMessage(requireContext().getString(R.string.disconnect_dialogue))
            builder.setPositiveButton(requireContext().getString(R.string.yes)) { _, _ ->
                ProcessPhoenix.triggerRebirth(requireContext());
            }

            builder.setNegativeButton(requireContext().getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }

            builder.show()

            //hideMenu()
            //RetrofitSingleton.isConnectedToHost = false
            //RetrofitSingleton.HOST = ""
        }

        //reconnectToHostButton.setOnClickListener {
        //    navigation.disableAll()
        //    RetrofitSingleton.isConnectedToHost = false
        //    lifecycleScope.launch {
        //        connectToHost(RetrofitSingleton.HOST)
        //    }
        //}

        openURLWebButton.setOnClickListener {
            val uri = Uri.parse(RetrofitSingleton.HOST)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        navigation.post {
            navigation.menu.findItem(R.id.navigation_connect).isChecked = true
        }
    }

    override fun onResume() {
        super.onResume()

        if (RetrofitSingleton.isConnectedToHost) {
            showMenu()
        } else {
            hideMenu()
        }
    }

    private suspend fun connectToHost(URI: String) {
        GLOBAL = null
        loading.visibility = View.VISIBLE
        hostnameField.isEnabled = false
        connectToHostButton.isEnabled = false
        addListButton.isEnabled = false
        listButton.isEnabled = false

        RetrofitSingleton.HOST = if (!URI.startsWith("http://", true) && !URI.startsWith("https://", true)) { "http://$URI" } else URI
        Log.d("MTR API", "Data load started for ${RetrofitSingleton.HOST}…")

        try {
            GLOBAL = RetrofitSingleton.api.getData()

            hostnameTextView.text = requireContext().getString(R.string.fragment_connection_hostname, RetrofitSingleton.HOST)
            Log.i("MTR API", "Data got loaded!")
            Toast.makeText(requireContext(), "API data loaded succefully!", Toast.LENGTH_SHORT).show()
            RetrofitSingleton.isConnectedToHost = true
            showMenu()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "There was an error loading the API data.", Toast.LENGTH_SHORT).show()
            Log.e("MTR API", "There was an error loading the API data: ${e.message}")
            RetrofitSingleton.isConnectedToHost = false
            hideMenu()
        }

        loading.visibility = View.GONE
        hostnameField.isEnabled = true
        connectToHostButton.isEnabled = true
        addListButton.isEnabled = true
        listButton.isEnabled = true
        Log.d("MTR API", "Data load finished!")
    }

    private fun showMenu() {
        hostnameTextView.text = requireContext().getString(R.string.fragment_connection_hostname, RetrofitSingleton.HOST)
        connectedDiv.visibility = View.VISIBLE
        disconnectedDiv.visibility = View.GONE
        navigation.enableAll()
        navigation.visibility = View.VISIBLE
    }

    private fun hideMenu() {
        connectedDiv.visibility = View.GONE
        disconnectedDiv.visibility = View.VISIBLE
        navigation.disableAll()
        navigation.visibility = View.GONE
    }

    private fun BottomNavigationView.enableAll(){
        this.menu.forEach { it.isEnabled = true }
    }

    private fun BottomNavigationView.disableAll(){
        this.menu.forEach { it.isEnabled = false }
    }

    override fun onClick(address: MTRAddress) {
        hostnameField.setText(address.uri)
        listButtonAlertDialog.dismiss()
    }
}