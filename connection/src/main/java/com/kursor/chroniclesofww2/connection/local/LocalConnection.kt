package com.kursor.chroniclesofww2.connection.local

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.kursor.chroniclesofww2.connection.interfaces.Connection
import com.kursor.chroniclesofww2.connection.Host
import com.kursor.chroniclesofww2.connection.interfaces.println
import java.io.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class LocalConnection(
    input: BufferedReader,
    output: BufferedWriter,
    override val host: Host,
    override var sendListener: Connection.SendListener? = null,
    override var receiveListener: Connection.ReceiveListener? = null,
    looper: Looper
) : Connection {

    override val shutdownListeners = mutableListOf<Connection.ShutdownListener>()

    override val handler = Handler(looper)


    private val sender = Sender(output)
    private val receiver = Receiver(input)

    init {
        Log.i("Connection", "Init connection to ${host.name}; ${host.inetAddress}; ${host.port}")
        receiver.startReceiving()
    }

    override fun send(string: String) {
        sender.send(string)
    }

    override fun shutdown() {
        Log.i("Connection", "Disposing")
        receiver.dispose()
        sender.stopSending()
        Log.i("Connection", "Disposed")
    }

    inner class Sender(private val output: BufferedWriter) {

        val messageQueue: BlockingQueue<String> = ArrayBlockingQueue(10)
        private var sending = true

        init {
            SenderThread().start()
        }

        fun send(string: String) {
            messageQueue.put(string)
        }

        private inner class SenderThread : Thread() {

            override fun run() {
                while (sending) {
                    try {
                        val msg = messageQueue.take()
                        sendMessage(msg)
                    } catch (ie: InterruptedException) {
                        Log.d("Sender", "Message sending loop interrupted, exiting")
                    }
                }
            }

            fun sendMessage(string: String) {
                try {
                    Log.e("Sender", "Connected, Sending: $string")
                    output.println(string)
                    output.flush()
                    if (sendListener != null) {
                        handler.post { sendListener!!.onSendSuccess() }
                    }
                    Log.e("Sender", "Send Successful: $string")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i("Sender", "_____")
                    if (sendListener != null) {
                        handler.post { sendListener!!.onSendFailure(e) }
                    }
                    e.printStackTrace()
                }
            }
        }

        fun stopSending() {
            Log.i("Sender", "Stopping")
            sending = false
        }
    }

    inner class Receiver(private val input: BufferedReader) {

        private val receiverThread = ReceiverThread()

        fun startReceiving() {
            receiverThread.start()
        }

        private var receiving = true

        private inner class ReceiverThread : Thread() {
            override fun run() {
                try {
                    while (receiving) {
                        sleep(50)
                        val string = input.readLine() ?: continue
                        Log.e("Receiver", "RECEIVED ==> $string")
                        if (receiveListener != null) {
                            handler.post { receiveListener!!.onReceive(string) }
                        }

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun dispose() {
            Log.i("Receiver", "Disposing")
            try {
                receiving = false
                input.close()
                Log.i("Receiver", "Thread stopped")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}