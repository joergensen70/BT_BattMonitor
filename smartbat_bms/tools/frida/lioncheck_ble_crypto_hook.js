Java.perform(function () {
  var DateCls = Java.use('java.util.Date');

  function nowIso() {
    try {
      return DateCls.$new().toString();
    } catch (e) {
      return 'time_error';
    }
  }

  function toHex(byteArray) {
    if (!byteArray) return '';
    var out = [];
    for (var i = 0; i < byteArray.length; i++) {
      var v = byteArray[i];
      if (v < 0) v += 256;
      var h = v.toString(16).toUpperCase();
      if (h.length < 2) h = '0' + h;
      out.push(h);
    }
    return out.join(' ');
  }

  function safeUuid(obj) {
    try {
      return obj.getUuid().toString();
    } catch (e) {
      return 'uuid_error';
    }
  }

  function log(kind, msg) {
    console.log('[FRIDA][' + kind + '][' + nowIso() + '] ' + msg);
  }

  try {
    var Bgc = Java.use('android.bluetooth.BluetoothGattCharacteristic');
    var setValueBytes = Bgc.setValue.overload('[B');
    var getValueNoArg = Bgc.getValue.overload();

    setValueBytes.implementation = function (value) {
      log('CHAR_SET_VALUE', 'uuid=' + safeUuid(this) + ' bytes=' + toHex(value));
      return setValueBytes.call(this, value);
    };

    getValueNoArg.implementation = function () {
      var v = getValueNoArg.call(this);
      log('CHAR_GET_VALUE', 'uuid=' + safeUuid(this) + ' bytes=' + toHex(v));
      return v;
    };

    log('INIT', 'Hooked BluetoothGattCharacteristic setValue/getValue');
  } catch (e) {
    log('WARN', 'Characteristic hook failed: ' + e);
  }

  try {
    var Bga = Java.use('android.bluetooth.BluetoothGatt');
    var writeCharacteristicLegacy = Bga.writeCharacteristic.overload('android.bluetooth.BluetoothGattCharacteristic');
    var readCharacteristicLegacy = Bga.readCharacteristic.overload('android.bluetooth.BluetoothGattCharacteristic');

    writeCharacteristicLegacy.implementation = function (c) {
      var v = c.getValue();
      log('GATT_WRITE', 'uuid=' + safeUuid(c) + ' bytes=' + toHex(v));
      return writeCharacteristicLegacy.call(this, c);
    };

    readCharacteristicLegacy.implementation = function (c) {
      log('GATT_READ', 'uuid=' + safeUuid(c));
      return readCharacteristicLegacy.call(this, c);
    };

    log('INIT', 'Hooked BluetoothGatt writeCharacteristic/readCharacteristic');
  } catch (e) {
    log('WARN', 'BluetoothGatt hook failed: ' + e);
  }

  try {
    var Cipher = Java.use('javax.crypto.Cipher');
    var cipherDoFinalBytes = Cipher.doFinal.overload('[B');

    cipherDoFinalBytes.implementation = function (input) {
      log('CRYPTO_IN', 'Cipher.doFinal in=' + toHex(input));
      var out = cipherDoFinalBytes.call(this, input);
      log('CRYPTO_OUT', 'Cipher.doFinal out=' + toHex(out));
      return out;
    };

    log('INIT', 'Hooked Cipher.doFinal(byte[])');
  } catch (e) {
    log('WARN', 'Cipher hook failed: ' + e);
  }

  try {
    var Md = Java.use('java.security.MessageDigest');
    var mdDigestBytes = Md.digest.overload('[B');

    mdDigestBytes.implementation = function (input) {
      log('DIGEST_IN', 'MessageDigest.digest in=' + toHex(input));
      var out = mdDigestBytes.call(this, input);
      log('DIGEST_OUT', 'MessageDigest.digest out=' + toHex(out));
      return out;
    };

    log('INIT', 'Hooked MessageDigest.digest(byte[])');
  } catch (e) {
    log('WARN', 'MessageDigest hook failed: ' + e);
  }

  try {
    var Mac = Java.use('javax.crypto.Mac');
    var macDoFinalBytes = Mac.doFinal.overload('[B');

    macDoFinalBytes.implementation = function (input) {
      log('MAC_IN', 'Mac.doFinal in=' + toHex(input));
      var out = macDoFinalBytes.call(this, input);
      log('MAC_OUT', 'Mac.doFinal out=' + toHex(out));
      return out;
    };

    log('INIT', 'Hooked Mac.doFinal(byte[])');
  } catch (e) {
    log('WARN', 'Mac hook failed: ' + e);
  }

  log('READY', 'LionCheck BLE/Crypto hooks active');
});
