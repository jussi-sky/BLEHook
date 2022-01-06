package com.jussi.blehook;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BLEHook implements IXposedHookLoadPackage {

    String TAG = "jussiBLE";

    private static final String HEX = "0123456789abcdef";

    public static String byte2Hex(byte[] byteArray) {

        if (byteArray == null || byteArray.length == 0)
            return null;

        StringBuilder sb = new StringBuilder(byteArray.length * 2);

        sb.append("[");
        for (byte b : byteArray) {
            sb.append(HEX.charAt((b >> 4) & 0x0f));
            sb.append(HEX.charAt(b & 0x0f));
            sb.append(" ");
        }
        sb.append("]");

        return sb.toString();
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.contains("com.")) {
            Class bluetooth = lpparam.classLoader.loadClass("android.bluetooth.BluetoothGatt");
            try {
                if (bluetooth != null) {
                    XposedHelpers.findAndHookMethod(bluetooth, "writeCharacteristic", BluetoothGattCharacteristic.class, new XC_MethodHook() {
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            BluetoothGattCharacteristic bluetoothGattCharacteristic = (BluetoothGattCharacteristic) param.args[0];
                            byte[] mValue = bluetoothGattCharacteristic.getValue();
                            String str = byte2Hex(mValue);
                            Log.e(TAG, TAG + "#writeCharacteristic\tstr:" + str + "\tUUID:" + bluetoothGattCharacteristic.getUuid().toString());

                        }
                    });

                    XposedHelpers.findAndHookMethod(bluetooth, "readCharacteristic", BluetoothGattCharacteristic.class, new XC_MethodHook() {
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            BluetoothGattCharacteristic bluetoothGattCharacteristic = (BluetoothGattCharacteristic) param.args[0];
                            byte[] mValue = bluetoothGattCharacteristic.getValue();
                            String str = byte2Hex(mValue);
                            Log.e(TAG, TAG + "#readCharacteristic\tstr:" + str + "\tUUID:" + bluetoothGattCharacteristic.getUuid().toString());
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
