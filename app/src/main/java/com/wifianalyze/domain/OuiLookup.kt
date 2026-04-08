package com.wifianalyze.domain

/**
 * Resolves the first 3 octets of a BSSID (the OUI) to a manufacturer name.
 * Covers the most common consumer WiFi equipment manufacturers.
 */
object OuiLookup {

    fun lookup(bssid: String): String {
        if (bssid.length < 8) return ""
        val oui = bssid.take(8).uppercase().replace("-", ":")
        return TABLE[oui] ?: ""
    }

    private val TABLE = mapOf(
        // ─── Apple ───────────────────────────────────────────────────────────
        "00:17:F2" to "Apple", "00:1C:B3" to "Apple", "00:1D:4F" to "Apple",
        "00:1E:52" to "Apple", "00:1F:F3" to "Apple", "00:21:E9" to "Apple",
        "00:22:41" to "Apple", "00:23:12" to "Apple", "00:25:00" to "Apple",
        "00:25:4B" to "Apple", "00:26:08" to "Apple", "00:26:4A" to "Apple",
        "00:26:B9" to "Apple", "00:50:E4" to "Apple", "04:1E:64" to "Apple",
        "04:26:65" to "Apple", "04:48:9A" to "Apple", "08:74:02" to "Apple",
        "0C:74:C2" to "Apple", "14:10:9F" to "Apple", "14:99:E2" to "Apple",
        "18:34:51" to "Apple", "18:65:90" to "Apple", "1C:1A:C0" to "Apple",
        "20:A2:E4" to "Apple", "24:A0:74" to "Apple", "28:CF:DA" to "Apple",
        "2C:1F:23" to "Apple", "30:63:6B" to "Apple", "34:15:9E" to "Apple",
        "34:C0:59" to "Apple", "38:0F:4A" to "Apple", "3C:07:54" to "Apple",
        "40:30:04" to "Apple", "44:4C:0C" to "Apple", "48:60:BC" to "Apple",
        "4C:57:CA" to "Apple", "50:ED:3C" to "Apple", "54:26:96" to "Apple",
        "58:1F:AA" to "Apple", "5C:09:47" to "Apple", "60:03:08" to "Apple",
        "60:33:4B" to "Apple", "60:69:44" to "Apple", "64:A3:CB" to "Apple",
        "68:64:4B" to "Apple", "6C:40:08" to "Apple", "70:11:24" to "Apple",
        "70:CD:60" to "Apple", "74:81:14" to "Apple", "78:31:C1" to "Apple",
        "7C:11:BE" to "Apple", "80:19:34" to "Apple", "84:29:99" to "Apple",
        "88:19:08" to "Apple", "8C:FA:BA" to "Apple", "90:72:40" to "Apple",
        "94:94:26" to "Apple", "98:01:A7" to "Apple", "9C:04:EB" to "Apple",
        "A0:99:9B" to "Apple", "A4:67:06" to "Apple", "A8:BE:27" to "Apple",
        "AC:3C:0B" to "Apple", "AC:87:A3" to "Apple", "B0:19:C6" to "Apple",
        "B8:09:8A" to "Apple", "B8:17:C2" to "Apple", "BC:3B:AF" to "Apple",
        "BC:67:78" to "Apple", "C0:9F:42" to "Apple", "C4:2C:03" to "Apple",
        "C8:1E:E7" to "Apple", "CC:08:8D" to "Apple", "D0:03:4B" to "Apple",
        "D4:61:DA" to "Apple", "D8:00:4D" to "Apple", "DC:2B:2A" to "Apple",
        "E0:5F:45" to "Apple", "E4:CE:8F" to "Apple", "E8:06:88" to "Apple",
        "F0:18:98" to "Apple", "F4:0F:24" to "Apple", "F8:1E:DF" to "Apple",
        "FC:25:3F" to "Apple",

        // ─── TP-Link ──────────────────────────────────────────────────────────
        "14:CC:20" to "TP-Link", "18:D6:C7" to "TP-Link", "1C:61:B4" to "TP-Link",
        "20:F4:1B" to "TP-Link", "30:DE:4B" to "TP-Link", "50:3E:AA" to "TP-Link",
        "50:D4:F7" to "TP-Link", "54:C8:0F" to "TP-Link", "60:32:B1" to "TP-Link",
        "64:70:02" to "TP-Link", "6C:5A:B0" to "TP-Link", "74:DA:38" to "TP-Link",
        "78:44:76" to "TP-Link", "84:D8:1B" to "TP-Link", "8C:21:0A" to "TP-Link",
        "90:F6:52" to "TP-Link", "A0:F3:C1" to "TP-Link", "AC:84:C6" to "TP-Link",
        "B0:4E:26" to "TP-Link", "B0:BE:76" to "TP-Link", "C0:25:E9" to "TP-Link",
        "C4:6E:1F" to "TP-Link", "D4:6E:0A" to "TP-Link", "D8:07:B6" to "TP-Link",
        "E0:28:6D" to "TP-Link", "E8:DE:27" to "TP-Link", "EC:08:6B" to "TP-Link",
        "F4:F2:6D" to "TP-Link", "98:DA:C4" to "TP-Link", "50:C7:BF" to "TP-Link",

        // ─── Netgear ──────────────────────────────────────────────────────────
        "00:09:5B" to "Netgear", "00:14:6C" to "Netgear", "00:18:4D" to "Netgear",
        "00:1B:2F" to "Netgear", "00:1E:2A" to "Netgear", "00:22:3F" to "Netgear",
        "00:24:B2" to "Netgear", "00:26:F2" to "Netgear", "1C:1B:0D" to "Netgear",
        "20:4E:7F" to "Netgear", "28:C6:8E" to "Netgear", "2C:B0:5D" to "Netgear",
        "30:46:9A" to "Netgear", "3C:37:86" to "Netgear", "4C:60:DE" to "Netgear",
        "5C:D2:E4" to "Netgear", "6C:B0:CE" to "Netgear", "80:37:73" to "Netgear",
        "84:1B:5E" to "Netgear", "9C:3D:CF" to "Netgear", "A0:21:B7" to "Netgear",
        "A0:40:A0" to "Netgear", "B0:39:56" to "Netgear", "C0:3F:0E" to "Netgear",
        "C4:04:15" to "Netgear", "CC:40:D0" to "Netgear", "D4:78:56" to "Netgear",
        "E0:91:F5" to "Netgear", "E4:60:90" to "Netgear", "F8:1A:67" to "Netgear",

        // ─── ASUS ─────────────────────────────────────────────────────────────
        "00:0C:6E" to "Asus", "00:11:2F" to "Asus", "00:15:F2" to "Asus",
        "00:17:31" to "Asus", "00:18:F3" to "Asus", "00:1A:92" to "Asus",
        "00:1D:60" to "Asus", "00:1E:8C" to "Asus", "00:23:54" to "Asus",
        "00:24:8C" to "Asus", "04:92:26" to "Asus", "08:60:6E" to "Asus",
        "10:7B:44" to "Asus", "18:31:BF" to "Asus", "1C:87:2C" to "Asus",
        "20:CF:30" to "Asus", "2C:56:DC" to "Asus", "38:2C:4A" to "Asus",
        "40:16:7E" to "Asus", "48:5B:39" to "Asus", "54:04:A6" to "Asus",
        "60:45:CB" to "Asus", "6C:FD:B9" to "Asus", "70:85:C2" to "Asus",
        "74:D0:2B" to "Asus", "84:A9:C4" to "Asus", "88:D7:F6" to "Asus",
        "90:E6:BA" to "Asus", "AC:22:0B" to "Asus", "BC:EE:7B" to "Asus",
        "D0:17:C2" to "Asus", "D8:50:E6" to "Asus", "E4:70:B8" to "Asus",
        "F8:32:E4" to "Asus",

        // ─── D-Link ───────────────────────────────────────────────────────────
        "00:0D:88" to "D-Link", "00:11:95" to "D-Link", "00:15:E9" to "D-Link",
        "00:17:9A" to "D-Link", "00:19:5B" to "D-Link", "00:1B:11" to "D-Link",
        "00:1E:58" to "D-Link", "00:21:91" to "D-Link", "00:22:B0" to "D-Link",
        "00:24:01" to "D-Link", "00:26:5A" to "D-Link", "1C:7E:E5" to "D-Link",
        "28:10:7B" to "D-Link", "34:08:04" to "D-Link", "5C:D9:98" to "D-Link",
        "6C:72:20" to "D-Link", "78:54:2E" to "D-Link", "84:C9:B2" to "D-Link",
        "90:94:E4" to "D-Link", "9C:D6:43" to "D-Link", "A0:AB:1B" to "D-Link",
        "B8:A3:86" to "D-Link", "C8:D3:A3" to "D-Link", "D8:FE:E3" to "D-Link",
        "F0:7D:68" to "D-Link",

        // ─── Cisco ────────────────────────────────────────────────────────────
        "00:00:0C" to "Cisco", "00:02:16" to "Cisco", "00:02:17" to "Cisco",
        "00:04:9A" to "Cisco", "00:09:43" to "Cisco", "00:0B:46" to "Cisco",
        "00:0C:85" to "Cisco", "00:0D:29" to "Cisco", "00:0E:38" to "Cisco",
        "00:0F:23" to "Cisco", "00:1A:6C" to "Cisco", "00:1B:0D" to "Cisco",
        "34:DB:FD" to "Cisco", "58:97:BD" to "Cisco", "60:2A:D0" to "Cisco",
        "68:BC:0C" to "Cisco", "88:F0:31" to "Cisco", "A4:93:4C" to "Cisco",
        "B8:62:1F" to "Cisco", "C0:62:6B" to "Cisco", "D4:8C:B5" to "Cisco",
        "E8:65:D4" to "Cisco",

        // ─── Linksys / Belkin ────────────────────────────────────────────────
        "00:06:25" to "Linksys", "00:0C:41" to "Linksys", "00:0E:08" to "Linksys",
        "00:12:17" to "Linksys", "00:14:BF" to "Linksys", "00:16:B6" to "Linksys",
        "00:18:39" to "Linksys", "00:21:29" to "Linksys", "00:23:69" to "Linksys",
        "00:25:9C" to "Linksys", "20:AA:4B" to "Linksys", "58:6D:8F" to "Linksys",
        "94:10:3E" to "Linksys", "C0:56:27" to "Linksys", "14:91:82" to "Linksys",
        "E4:F4:C6" to "Belkin", "94:44:52" to "Belkin", "B4:75:0E" to "Belkin",

        // ─── Ubiquiti ─────────────────────────────────────────────────────────
        "00:15:6D" to "Ubiquiti", "00:27:22" to "Ubiquiti", "04:18:D6" to "Ubiquiti",
        "18:E8:29" to "Ubiquiti", "24:5A:4C" to "Ubiquiti", "24:A4:3C" to "Ubiquiti",
        "44:D9:E7" to "Ubiquiti", "68:72:51" to "Ubiquiti", "78:8A:20" to "Ubiquiti",
        "80:2A:A8" to "Ubiquiti", "8C:19:B5" to "Ubiquiti", "B4:FB:E4" to "Ubiquiti",
        "D8:38:FC" to "Ubiquiti", "DC:9F:DB" to "Ubiquiti", "E0:63:DA" to "Ubiquiti",
        "F4:92:BF" to "Ubiquiti", "FC:EC:DA" to "Ubiquiti",

        // ─── Google / Nest ───────────────────────────────────────────────────
        "00:1A:11" to "Google", "20:DF:B9" to "Google", "3C:5A:B4" to "Google",
        "40:4E:36" to "Google", "54:60:09" to "Google", "58:CB:52" to "Google",
        "6C:AD:F8" to "Google", "70:3A:CB" to "Google", "74:2F:68" to "Google",
        "A4:77:33" to "Google", "A4:DA:22" to "Google", "AC:91:A1" to "Google",
        "C8:56:61" to "Google", "CC:FA:00" to "Google", "D4:F5:47" to "Google",
        "F4:F5:D8" to "Google", "94:EB:2C" to "Google",

        // ─── Amazon / Echo / Ring ────────────────────────────────────────────
        "0C:47:C9" to "Amazon", "10:AE:60" to "Amazon", "18:74:2E" to "Amazon",
        "1C:12:B0" to "Amazon", "34:D2:70" to "Amazon", "38:F7:3D" to "Amazon",
        "40:B4:CD" to "Amazon", "44:65:0D" to "Amazon", "4C:EF:C0" to "Amazon",
        "50:DC:E7" to "Amazon", "50:F5:DA" to "Amazon", "68:37:E9" to "Amazon",
        "74:C2:46" to "Amazon", "78:E1:03" to "Amazon", "84:D6:D0" to "Amazon",
        "88:71:E5" to "Amazon", "8C:35:55" to "Amazon", "A0:02:DC" to "Amazon",
        "B4:7C:9C" to "Amazon", "BC:54:F2" to "Amazon", "CC:F7:35" to "Amazon",
        "D0:55:C8" to "Amazon", "FC:A6:67" to "Amazon",

        // ─── Samsung ──────────────────────────────────────────────────────────
        "00:12:FB" to "Samsung", "00:15:B9" to "Samsung", "00:16:6B" to "Samsung",
        "00:16:DB" to "Samsung", "00:17:C9" to "Samsung", "00:18:AF" to "Samsung",
        "00:1A:8A" to "Samsung", "00:1C:43" to "Samsung", "00:1D:25" to "Samsung",
        "00:1E:7D" to "Samsung", "00:21:D2" to "Samsung", "00:23:39" to "Samsung",
        "00:23:C2" to "Samsung", "00:24:54" to "Samsung", "00:26:5D" to "Samsung",
        "08:08:C2" to "Samsung", "08:D4:2B" to "Samsung", "0C:71:5D" to "Samsung",
        "0C:89:10" to "Samsung", "10:30:47" to "Samsung", "14:1F:BA" to "Samsung",
        "18:22:7E" to "Samsung", "1C:5A:6B" to "Samsung", "1C:62:B8" to "Samsung",
        "1C:66:AA" to "Samsung", "20:13:E0" to "Samsung", "28:CC:01" to "Samsung",
        "2C:AE:2B" to "Samsung", "30:CD:A7" to "Samsung", "34:23:BA" to "Samsung",
        "34:BE:00" to "Samsung", "38:01:97" to "Samsung", "38:AA:3C" to "Samsung",
        "40:0E:85" to "Samsung", "44:4E:1A" to "Samsung", "44:78:3E" to "Samsung",
        "4C:73:D5" to "Samsung", "50:01:BB" to "Samsung", "50:CC:F8" to "Samsung",
        "58:C3:8B" to "Samsung", "5C:51:81" to "Samsung", "60:6B:BD" to "Samsung",
        "6C:2F:2C" to "Samsung", "70:F9:27" to "Samsung", "74:45:8A" to "Samsung",
        "7C:91:22" to "Samsung", "80:65:6D" to "Samsung", "84:11:9E" to "Samsung",
        "88:32:9B" to "Samsung", "8C:77:12" to "Samsung", "90:18:7C" to "Samsung",
        "94:01:C2" to "Samsung", "98:52:B1" to "Samsung", "9C:02:98" to "Samsung",
        "A0:07:98" to "Samsung", "A0:82:1F" to "Samsung", "A4:23:05" to "Samsung",
        "A8:06:00" to "Samsung", "AC:5F:3E" to "Samsung", "B0:EC:71" to "Samsung",
        "B4:07:F9" to "Samsung", "B8:5A:73" to "Samsung", "BC:20:A4" to "Samsung",
        "C0:BD:D1" to "Samsung", "C4:42:02" to "Samsung", "C8:14:79" to "Samsung",
        "CC:07:AB" to "Samsung", "D0:59:E4" to "Samsung", "D0:87:E2" to "Samsung",
        "D0:DF:9A" to "Samsung", "E4:40:E2" to "Samsung", "E4:7C:F9" to "Samsung",
        "E8:03:9A" to "Samsung", "EC:1F:72" to "Samsung", "F0:E7:7E" to "Samsung",
        "F4:42:8F" to "Samsung", "F8:04:2E" to "Samsung",

        // ─── Arris / Pace (ISP routers / cable modems) ───────────────────────
        "00:17:10" to "Arris", "00:1C:C4" to "Arris", "18:68:CB" to "Arris",
        "24:2A:E4" to "Arris", "3C:DF:88" to "Arris", "44:A5:6E" to "Arris",
        "50:8F:4C" to "Arris", "60:E8:5B" to "Arris", "78:96:84" to "Arris",
        "98:3B:8F" to "Arris", "A4:73:9F" to "Arris", "B8:3E:59" to "Arris",
        "D4:05:98" to "Arris",

        // ─── Motorola ─────────────────────────────────────────────────────────
        "00:08:08" to "Motorola", "00:0A:12" to "Motorola", "00:13:B7" to "Motorola",
        "00:19:6A" to "Motorola", "00:1E:74" to "Motorola", "00:23:AB" to "Motorola",
        "04:80:C8" to "Motorola", "1C:09:04" to "Motorola", "28:A1:83" to "Motorola",
        "34:2E:B6" to "Motorola", "40:8C:18" to "Motorola", "54:A0:50" to "Motorola",
        "5C:8F:E0" to "Motorola", "64:F3:77" to "Motorola", "6C:5B:35" to "Motorola",
        "70:64:21" to "Motorola", "74:2C:8B" to "Motorola", "78:D9:9F" to "Motorola",

        // ─── Huawei ───────────────────────────────────────────────────────────
        "00:18:82" to "Huawei", "00:1E:10" to "Huawei", "00:25:9E" to "Huawei",
        "04:02:1F" to "Huawei", "04:B0:E7" to "Huawei", "04:C0:6F" to "Huawei",
        "08:19:A6" to "Huawei", "0C:37:DC" to "Huawei", "10:1B:54" to "Huawei",
        "14:B9:68" to "Huawei", "1C:8E:5C" to "Huawei", "20:08:ED" to "Huawei",
        "24:69:68" to "Huawei", "28:31:52" to "Huawei", "2C:AB:00" to "Huawei",
        "30:87:30" to "Huawei", "34:6B:D3" to "Huawei", "34:A8:4E" to "Huawei",
        "38:37:8B" to "Huawei", "3C:47:11" to "Huawei", "40:CB:A8" to "Huawei",
        "44:1C:A8" to "Huawei", "4C:1F:CC" to "Huawei", "50:9F:27" to "Huawei",
        "54:51:1B" to "Huawei", "58:2A:F7" to "Huawei", "5C:4C:A9" to "Huawei",
        "60:DE:44" to "Huawei", "64:3E:8C" to "Huawei", "6C:8D:C1" to "Huawei",
        "70:72:3C" to "Huawei", "74:6A:89" to "Huawei", "78:1D:BA" to "Huawei",
        "7C:B5:9B" to "Huawei", "80:FB:06" to "Huawei", "84:74:2A" to "Huawei",
        "88:53:D4" to "Huawei", "8C:34:FD" to "Huawei", "90:67:F3" to "Huawei",
        "94:04:9C" to "Huawei", "94:77:2B" to "Huawei", "98:E7:F4" to "Huawei",
        "9C:28:EF" to "Huawei", "A0:4C:5B" to "Huawei", "A4:CA:A0" to "Huawei",
        "AC:E8:7B" to "Huawei", "B0:F8:93" to "Huawei", "C8:51:95" to "Huawei",
        "CC:96:A0" to "Huawei", "D0:7A:B5" to "Huawei", "E4:A7:C5" to "Huawei",
        "E8:CD:2D" to "Huawei", "F4:4C:7F" to "Huawei", "F8:A4:5F" to "Huawei",

        // ─── Xiaomi / Mi ──────────────────────────────────────────────────────
        "00:9E:C8" to "Xiaomi", "04:CF:8C" to "Xiaomi", "10:2A:B3" to "Xiaomi",
        "18:59:36" to "Xiaomi", "28:6C:07" to "Xiaomi", "34:80:B3" to "Xiaomi",
        "38:A4:ED" to "Xiaomi", "3C:BD:D8" to "Xiaomi", "4C:49:E3" to "Xiaomi",
        "54:84:CF" to "Xiaomi", "58:44:98" to "Xiaomi", "64:09:80" to "Xiaomi",
        "6C:C7:EC" to "Xiaomi", "74:23:44" to "Xiaomi", "78:02:F8" to "Xiaomi",
        "7C:1D:D9" to "Xiaomi", "8C:BE:BE" to "Xiaomi", "98:FA:E3" to "Xiaomi",
        "9C:99:A0" to "Xiaomi", "A0:86:C6" to "Xiaomi", "AC:F7:F3" to "Xiaomi",
        "B0:E2:35" to "Xiaomi", "C4:0B:CB" to "Xiaomi", "D4:97:0B" to "Xiaomi",
        "F4:8B:32" to "Xiaomi", "F8:A4:5F" to "Xiaomi", "FC:64:BA" to "Xiaomi",

        // ─── Espressif (ESP32 / ESP8266 IoT) ─────────────────────────────────
        "18:FE:34" to "Espressif", "24:0A:C4" to "Espressif", "24:6F:28" to "Espressif",
        "2C:3A:E8" to "Espressif", "30:AE:A4" to "Espressif", "3C:61:05" to "Espressif",
        "48:3F:DA" to "Espressif", "54:32:04" to "Espressif", "5C:CF:7F" to "Espressif",
        "60:01:94" to "Espressif", "68:C6:3A" to "Espressif", "70:03:9F" to "Espressif",
        "7C:87:CE" to "Espressif", "80:7D:3A" to "Espressif", "84:0D:8E" to "Espressif",
        "84:F3:EB" to "Espressif", "A0:20:A6" to "Espressif", "A4:CF:12" to "Espressif",
        "AC:67:B2" to "Espressif", "B4:E6:2D" to "Espressif", "BC:DD:C2" to "Espressif",
        "C4:4F:33" to "Espressif", "C8:2B:96" to "Espressif", "CC:50:E3" to "Espressif",
        "D8:A0:1D" to "Espressif", "D8:BC:38" to "Espressif", "E0:98:06" to "Espressif",
        "E8:DB:84" to "Espressif", "EC:94:CB" to "Espressif", "F4:CF:A2" to "Espressif",
        "FC:F5:C4" to "Espressif",

        // ─── Raspberry Pi Foundation ─────────────────────────────────────────
        "B8:27:EB" to "Raspberry Pi", "DC:A6:32" to "Raspberry Pi",
        "E4:5F:01" to "Raspberry Pi", "28:CD:C1" to "Raspberry Pi",
        "D8:3A:DD" to "Raspberry Pi",

        // ─── Intel (Wi-Fi cards in laptops) ──────────────────────────────────
        "00:02:B3" to "Intel", "00:04:23" to "Intel", "00:07:E9" to "Intel",
        "00:0C:F1" to "Intel", "00:0E:0C" to "Intel", "00:11:11" to "Intel",
        "00:12:F0" to "Intel", "00:13:02" to "Intel", "00:13:E8" to "Intel",
        "00:15:17" to "Intel", "00:16:76" to "Intel", "00:16:EA" to "Intel",
        "00:18:DE" to "Intel", "00:19:D1" to "Intel", "00:1B:21" to "Intel",
        "00:1C:BF" to "Intel", "00:1D:E0" to "Intel", "00:1E:64" to "Intel",
        "00:21:5C" to "Intel", "00:21:6A" to "Intel", "00:23:14" to "Intel",
        "00:24:D6" to "Intel", "00:27:10" to "Intel", "04:0E:3C" to "Intel",
        "18:56:80" to "Intel", "34:E1:2D" to "Intel", "40:A8:F0" to "Intel",
        "48:45:20" to "Intel", "54:27:1E" to "Intel", "60:57:18" to "Intel",
        "68:05:CA" to "Intel", "7C:B0:C2" to "Intel", "80:19:34" to "Intel",
        "98:4F:EE" to "Intel", "A4:C3:F0" to "Intel", "AC:22:0B" to "Intel",
        "B4:6B:FC" to "Intel",

        // ─── Sonos ────────────────────────────────────────────────────────────
        "00:0E:58" to "Sonos", "34:7E:5C" to "Sonos", "48:A6:B8" to "Sonos",
        "5C:AA:FD" to "Sonos", "78:28:CA" to "Sonos", "94:9F:3E" to "Sonos",
        "B8:E9:37" to "Sonos", "D4:38:9C" to "Sonos",

        // ─── Philips / Signify (Hue) ─────────────────────────────────────────
        "00:17:88" to "Philips Hue", "EC:B5:FA" to "Philips Hue",

        // ─── Roku ─────────────────────────────────────────────────────────────
        "00:0D:4B" to "Roku", "AC:3A:7A" to "Roku", "CC:6D:A0" to "Roku",
        "D4:E8:80" to "Roku", "D8:31:CF" to "Roku", "D8:80:83" to "Roku",
        "B8:3E:59" to "Roku",

        // ─── Eero (Amazon) ───────────────────────────────────────────────────
        "F8:BB:BF" to "Eero", "50:8A:07" to "Eero", "C0:EE:FB" to "Eero",

        // ─── Synology ────────────────────────────────────────────────────────
        "00:11:32" to "Synology",

        // ─── Broadcom ────────────────────────────────────────────────────────
        "00:10:18" to "Broadcom", "00:90:4C" to "Broadcom",

        // ─── Qualcomm / Atheros ───────────────────────────────────────────────
        "00:03:7F" to "Atheros", "20:02:AF" to "Qualcomm",

        // ─── Microsoft (Surface, Xbox) ───────────────────────────────────────
        "00:50:F2" to "Microsoft", "28:18:78" to "Microsoft", "48:5B:39" to "Microsoft",
        "7C:1E:52" to "Microsoft", "C4:9D:ED" to "Microsoft",
    )
}
