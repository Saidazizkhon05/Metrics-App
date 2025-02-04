    package me.innerworks.capacitor.plugin.bluetooth

    import android.Manifest
    import android.bluetooth.BluetoothAdapter
    import android.bluetooth.BluetoothDevice
    import android.bluetooth.BluetoothManager
    import android.bluetooth.BluetoothProfile
    import android.content.Context
    import android.content.pm.PackageManager
    import android.hardware.camera2.CameraManager
    import android.os.Build
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import org.json.JSONArray
    import org.json.JSONObject
    import java.lang.ref.WeakReference

    class BluetoothMetricsManager(context: Context) {
        private val contextRef = WeakReference(context)
        private val bluetoothManager: BluetoothManager? by lazy {
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }
        private val bluetoothAdapter: BluetoothAdapter? by lazy {
            bluetoothManager?.adapter
        }

        fun getBluetoothMetrics(): JSONObject {
            val bluetoothMetrics = JSONObject()
            val context = contextRef.get() ?: return errorResult("Context is not available")
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return errorResult("Bluetooth permission not granted")
            } else {
                try {
                    // Adapter status
                    bluetoothMetrics.put("adapterInfo", getAdapterInfo())
                    // Connected devices
                    bluetoothMetrics.put("connectedDevices", getConnectedDeviceInfo())
                    // Bonded devices
                    bluetoothMetrics.put("bondedDevices", getBondedDevicesInfo())
                    // Scanning Status
                    bluetoothMetrics.put("scanningInfo", getScanningInfo())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return bluetoothMetrics
        }

        private fun getScanningInfo(): JSONObject {
            val scanInfo = JSONObject()
            val context = contextRef.get() ?: return errorResult("Context is not available")
            bluetoothAdapter?.let { adapter ->
                scanInfo.apply {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.BLUETOOTH_ADMIN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return errorResult("Bluetooth permission not granted")
                    }
                    put("isScanning", adapter.isDiscovering)
                    put("scanMode", getScanMode(adapter.scanMode))
                    put("isOffloadedScanBatchingSupported", adapter.isOffloadedScanBatchingSupported)
                    put("isOffloadedFilteringSupported", adapter.isOffloadedFilteringSupported)
                }
            }
            return scanInfo
        }

        private fun getAdapterInfo(): JSONObject {
            val adapterInfo = JSONObject()
            val context = contextRef.get() ?: return errorResult("Context is not available")
            bluetoothAdapter?.let { adapter ->
                adapterInfo.apply {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.BLUETOOTH
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        put("name", adapter.name)
                        put("address", adapter.address)
                        put("state", getAdapterState(adapter.state))
                        put("scanMode", getScanMode(adapter.scanMode))
                        put("isDiscovering", adapter.isDiscovering)
                        put("isEnabled", adapter.isEnabled)

                        //Extended Bluetooth Metrics
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            put("isLe2MPhySupported", adapter.isLe2MPhySupported)
                            put("isLeCodedPhySupported", adapter.isLeCodedPhySupported)
                            put(
                                "isLeExtendedAdvertisingSupported",
                                adapter.isLeExtendedAdvertisingSupported
                            )
                            put(
                                "isLePeriodicAdvertisingSupported",
                                adapter.isLePeriodicAdvertisingSupported
                            )
                            put(
                                "leMaximumAdvertisingDataLength",
                                adapter.leMaximumAdvertisingDataLength
                            )
                        }
                    } else {
                        return errorResult("Bluetooth permission granted")
                    }
                }
            }
            return adapterInfo
        }

        private fun getConnectedDeviceInfo(): Any {
            val devicesArray = JSONArray()
            val context = contextRef.get() ?: return errorResult("Context is not available")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return errorResult("Bluetooth permission not granted")
            }
            bluetoothManager?.getConnectedDevices(BluetoothProfile.GATT)?.forEach { device ->
                devicesArray.put(getDeviceInfo(device))
            }
            return devicesArray
        }

        private fun getBondedDevicesInfo(): Any {
            val devicesArray = JSONArray()
            val context = contextRef.get() ?: return errorResult("Context is not available")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return errorResult("Bluetooth permission not granted")
            }
            bluetoothAdapter?.bondedDevices?.forEach { device ->
                devicesArray.put(getDeviceInfo(device))
            }
            return devicesArray
        }

        private fun getDeviceInfo(device: BluetoothDevice?): JSONObject {
            val deviceInfo = JSONObject()
            val context = contextRef.get() ?: return errorResult("Context is not available")
            device?.let {
                deviceInfo.apply {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return errorResult("Bluetooth permission not granted")
                    }
                    put("name", device.name)
                    put("address", device.address)
                    put(
                        "type", when (device.type) {
                            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "CLASSIC"
                            BluetoothDevice.DEVICE_TYPE_LE -> "LE"
                            BluetoothDevice.DEVICE_TYPE_DUAL -> "DUAL"
                            else -> "UNKNOWN"
                        }
                    )
                    put(
                        "bondState", when (device.bondState) {
                            BluetoothDevice.BOND_NONE -> "NONE"
                            BluetoothDevice.BOND_BONDING -> "BONDING"
                            BluetoothDevice.BOND_BONDED -> "BONDED"
                            else -> "UNKNOWN"
                        }
                    )
                }
            }
            return deviceInfo

        }


        private fun getAdapterState(state: Int): String {
            return when (state) {
                BluetoothAdapter.STATE_OFF -> "OFF"
                BluetoothAdapter.STATE_TURNING_ON -> "TURNING_ON"
                BluetoothAdapter.STATE_ON -> "ON"
                BluetoothAdapter.STATE_TURNING_OFF -> "TURNING_OFF"
                else -> "UNKNOWN"
            }
        }

        private fun getScanMode(scanMode: Int): String {
            return when (scanMode) {
                BluetoothAdapter.SCAN_MODE_NONE -> "NONE"
                BluetoothAdapter.SCAN_MODE_CONNECTABLE -> "CONNECTABLE"
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> "CONNECTABLE_DISCOVERABLE"
                else -> "UNKNOWN"
            }
        }

        private fun errorResult(message: String): JSONObject {
            val errorJson = JSONObject()
            errorJson.put("error", message)
            return errorJson
        }
    }