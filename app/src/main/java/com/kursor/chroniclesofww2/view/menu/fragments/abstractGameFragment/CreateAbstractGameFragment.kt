package com.kursor.chroniclesofww2.view.menu.fragments.abstractGameFragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kursor.chroniclesofww2.Const
import com.kursor.chroniclesofww2.Const.connection.CANCEL_CONNECTION
import com.kursor.chroniclesofww2.Const.connection.HOST_IS_WITH_PASSWORD
import com.kursor.chroniclesofww2.Const.connection.INVALID_JSON
import com.kursor.chroniclesofww2.Const.connection.PASSWORD
import com.kursor.chroniclesofww2.Const.connection.REQUEST_FOR_ACCEPT
import com.kursor.chroniclesofww2.Const.connection.REQUEST_GAME_DATA
import com.kursor.chroniclesofww2.R
import com.kursor.chroniclesofww2.Tools
import com.kursor.chroniclesofww2.connection.Host
import com.kursor.chroniclesofww2.connection.interfaces.Connection
import com.kursor.chroniclesofww2.connection.interfaces.Server
import com.kursor.chroniclesofww2.databinding.FragmentCreateGameBinding
import com.kursor.chroniclesofww2.model.GameData
import com.kursor.chroniclesofww2.model.Scenario
import com.kursor.chroniclesofww2.view.menu.activities.GameActivity
import com.kursor.chroniclesofww2.view.menu.activities.MainActivity
import com.kursor.chroniclesofww2.view.menu.fragments.SimpleDialogFragment
import com.phelat.navigationresult.BundleFragment

abstract class CreateAbstractGameFragment : BundleFragment() {

    lateinit var binding: FragmentCreateGameBinding

    var currentDialog: DialogFragment? = null

    var chosenScenarioJson = ""
    protected lateinit var server: Server
    lateinit var connection: Connection
    protected var isHostReady = false
    lateinit var gameData: GameData
    lateinit var scenario: Scenario

    protected val receiveListener: Connection.ReceiveListener =
        Connection.ReceiveListener { string ->
            when (string) {
                REQUEST_FOR_ACCEPT -> if (isHostReady) {
                    if (server.password != null) {
                        connection.send(HOST_IS_WITH_PASSWORD)
                    } else {
                        currentDialog?.dismiss()
                        buildMessageConnectionRequest(connection.host)
                    }
                }
                REQUEST_GAME_DATA -> {
                    Log.i("Server", "Client sent $REQUEST_GAME_DATA")
                    connection.send(chosenScenarioJson)
                    val intent = Intent(activity as MainActivity, GameActivity::class.java)
                    intent.putExtra(Const.connection.CONNECTED_DEVICE, connection.host)
                        .putExtra(Const.game.MULTIPLAYER_GAME_MODE, Const.connection.HOST)
                        .putExtra(Const.game.SCENARIO, chosenScenarioJson)
                    Tools.currentConnection = connection
                    server.stopListening()
                    startActivity(intent)
                }
                CANCEL_CONNECTION -> {
                    Log.i("Server", CANCEL_CONNECTION)
                    connection.dispose()
                    Log.i("Server", "Sent invalid json")
                }
                INVALID_JSON -> Log.i("Server", "Sent invalid json")
                else -> {
                    if (string.contains(PASSWORD)) {
                        val password = string.removePrefix(PASSWORD)
                        if (password == server.password) {
                            currentDialog?.dismiss()
                            buildMessageConnectionRequest(connection.host)
                        }
                    }
                }
            }
        }

    protected val serverListener = object : Server.Listener {
        override fun onConnectionEstablished(connection: Connection) {
            Tools.currentConnection = connection
        }

        override fun onRegistered(host: Host) {
            buildMessageWaitingForConnections()
        }

        override fun onListeningStartError(e: Exception) {
            Toast.makeText(activity, "Listening start error", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        parentFragmentManager.setFragmentResultListener(
//            SCENARIO_INFO, this
//        ) { key, bundle ->
//            chosenScenarioJson = bundle.getString(Const.game.SCENARIO) ?: ""
//            if (chosenScenarioJson.isBlank()) return@setFragmentResultListener
//            val scenario = Scenario.fromJson(chosenScenarioJson)
//            binding.chosenScenarioTextView.text = scenario.name
//        }
    }

    override fun onFragmentResult(requestCode: Int, bundle: Bundle) {
        super.onFragmentResult(requestCode, bundle)
        if (requestCode == SCENARIO_INFO) {
            chosenScenarioJson = bundle.getString(Const.game.SCENARIO)!!
            scenario = Scenario.fromJson(chosenScenarioJson)
            binding.chosenScenarioTextView.text = scenario.getLocalizedName(requireContext())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chooseScenarioButton.setOnClickListener {
            findNavController()
//            menuActivity.changeFragment(
//                JavaMissionFragment(),
//                true,
//                false
//            )
        }
        binding.readyButton.setOnClickListener { v ->
            initServer()
            v.isEnabled = false
        }
    }

    abstract fun initServer()

    private fun buildMessageWaitingForConnections(): SimpleDialogFragment {
        val dialog: SimpleDialogFragment =
            SimpleDialogFragment.Builder(activity).setCancelable(false)
                .setNegativeButton("Cancel") { dialog, which ->
                    server.stopListening()
                    binding.readyButton.isEnabled = true
                }.setMessage("Waiting for Connections...")
                .setOnCancelListener {
                    server.stopListening()
                    binding.readyButton.isEnabled = true
                }
                .build()
        dialog.show(parentFragmentManager, "Waiting for connections")
        return dialog
    }

    private fun buildMessageConnectionRequest(host: Host) {
        val dialog: SimpleDialogFragment = SimpleDialogFragment.Builder(activity)
            .setMessage(host.name + " wants to connect to this  device. Do you agree?")
            .setCancelable(false)
            .setNegativeButton("Refuse") { dialog, which ->
                connection.send(Const.connection.REJECTED)
            }
            .setPositiveButton("Allow") { dialog, which ->
                connection.send(Const.connection.ACCEPTED)
            }
            .setOnCancelListener { connection.send(Const.connection.REJECTED) }
            .build()
        dialog.show(parentFragmentManager, "Waiting for Connected")
        Toast.makeText(activity, R.string.waiting_for_connected, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stopListening()
    }

    companion object {
        const val SCENARIO_INFO = 202
    }

}