package org.hacksugar.cryptor

import android.util.Base64
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher

class WeaselDemo {
    companion object {
        const val pubKey = "MIGeMA0GCSqGSIb3DQEBAQUAA4GMADCBiAKBgHwbTk8D9i5/JzZJHpjlR5e6SnyT\n" +
                "HOGuz7kWJqyinIQPVI10osbeVggjOM2BhM23OF7CYAgSuIdqZYAkHLwxaCuv7OHL\n" +
                "XlpWnWgTcbz84Xg3GthZ46nilGLEBtJqqboxwjTMiTF7ZgkGAquFFW9oiNN5GI/W\n" +
                "TObhaM02YpmFT/GTAgMBAAE="
        const val privKey = "MIICWwIBAAKBgHwbTk8D9i5/JzZJHpjlR5e6SnyTHOGuz7kWJqyinIQPVI10osbe\n" +
                "VggjOM2BhM23OF7CYAgSuIdqZYAkHLwxaCuv7OHLXlpWnWgTcbz84Xg3GthZ46ni\n" +
                "lGLEBtJqqboxwjTMiTF7ZgkGAquFFW9oiNN5GI/WTObhaM02YpmFT/GTAgMBAAEC\n" +
                "gYAFqAzypwCSb/MukziUyWZw8OmyMdZQJvKKwgqzNZoinrxA0j8VB08ugcR2AWA3\n" +
                "LBGiqANOeuP0MBI+O+cfYLUZr7bzspNeyQBcsV0LrOsTqs2cdRIs2EGN9eb+i+J8\n" +
                "yMxsjHPldCngJWXB0OeNzYa2UNOGahm/35XzK/jhKWf7OQJBAO20NFPUHqkfWL/z\n" +
                "G5IAxWn7mfsJbVV84c3ZFny5TzXx0Omg+IStfN2FJ2cNC6lLIeOQEwxx2sd+eYu4\n" +
                "9JdM+T8CQQCFqL56GvUcYbfCa0IH/ubrhfs0i7z8TWS5wrx0PEuBaOoyO0dFdI6e\n" +
                "xWdva6kpNq3wOO7Oz/5cGDMbBGTiQf6tAkAVYK2MFHmlcCJFMRH7sYIPpAcXIqPo\n" +
                "mlCceLejA+9xxIurV0TCee/O5FjE1dGEqjMkCiMMbXjllCROQpYMvWl1AkBWml/Q\n" +
                "/maTXT2T26uNQrydHtMF2QU69Wqucl9pcSf7Ud9tbLthZYSDm6TJrRiOe794R2t0\n" +
                "1ZAaXBPBDbfQYrKBAkEAwMPVIkttIERnQMb+Fi3l21P9JGWruy+tDclSarXtDQ5g\n" +
                "uW+6mGYiB5Ho8qqt4rUtPvcKnhWM+ikp0ql6JTn8zQ=="
        fun encrypt(data: String, key: PublicKey?): String {
            val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val bytes = cipher.doFinal(data.toByteArray())
            return Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
        fun decrypt(data: String, key: PrivateKey?): String {
            val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val encryptedData = Base64.decode(data, Base64.NO_WRAP)
            val decodedData = cipher.doFinal(encryptedData)
            return String(decodedData)
        }
    }
}