package com.smartscannerreactnative

import android.app.Activity
import android.content.Intent
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.google.gson.Gson
import org.idpass.smartscanner.lib.SmartScannerActivity
import org.idpass.smartscanner.lib.scanner.config.*

class SmartscannerReactNativeModule(reactContext: ReactApplicationContext) :
        ReactContextBaseJavaModule(reactContext) {

    private var scannerPromise: Promise? = null

    companion object {
        const val SCANNER_CANCELLED = "SCANNER_CANCELLED"
        const val SCANNER_OK = "SCANNER_OK"
        const val SCANNER_UNKNOWN_CODE = "SCANNER_UNKNOWN_CODE"
        const val SCANNER_FAILED = "SCANNER_FAILED"
        const val SCANNER_RESULTS_NOT_FOUND = "SCANNER_RESULTS_NOT_FOUND"
        const val SCANNER_ACTIVITY_DOES_NOT_EXIST = "SCANNER_ACTIVITY_DOES_NOT_EXIST"
        const val REQUEST_OP_SCANNER = 1001
        const val RESULT_SCAN_FAILED = 2
    }
    private val activityEventListener =
            object : BaseActivityEventListener() {
                override fun onActivityResult(
                        activity: Activity?,
                        requestCode: Int,
                        resultCode: Int,
                        intent: Intent?
                ) {
                    if (requestCode == REQUEST_OP_SCANNER) {
                        scannerPromise?.let { promise ->
                            when (resultCode) {
                                Activity.RESULT_CANCELED ->
                                        promise.reject(SCANNER_CANCELLED, "Cancelled")
                                Activity.RESULT_OK -> {
                                    val resultData =
                                            intent?.getStringExtra(
                                                    SmartScannerActivity.SCANNER_RESULT
                                            )

                                    resultData?.let { promise.resolve(resultData.toString()) }
                                            ?: promise.reject(
                                                    SCANNER_RESULTS_NOT_FOUND,
                                                    "No data found"
                                            )
                                }
                            }

                            scannerPromise = null
                        }
                    }
                }
            }

    init {
        reactContext.addActivityEventListener(activityEventListener)
    }

    override fun getName() = "SmartscannerReactNativeModule"

    @ReactMethod
    fun executeScanner(call: ReadableMap, promise: Promise) {
        scannerPromise = promise
        if (currentActivity == null) {
            promise.reject(SCANNER_ACTIVITY_DOES_NOT_EXIST, "No Activity")
            return
        }
        try {
            val action = call.getString("action")
            val options = call.getMap("options")

            if (action == "START_SCANNER") {
                val intent = Intent(currentActivity, SmartScannerActivity::class.java)
                val json = ReactNativeJson.convertMapToJson(options)
                val scannerOptions = Gson().fromJson(json.toString(), ScannerOptions::class.java)

                intent.putExtra(SmartScannerActivity.SCANNER_OPTIONS, scannerOptions)
                currentActivity?.startActivityForResult(intent, REQUEST_OP_SCANNER)
            }
        } catch (e: Exception) {
            scannerPromise?.reject(SCANNER_FAILED, e)
        }
    }
}
