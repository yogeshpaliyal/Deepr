package com.yogeshpaliyal.deepr.util

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class QRScanner : ActivityResultContract<ScanOptions, ScanIntentResult>() {
    override fun createIntent(context: Context, input: ScanOptions): Intent {
        return Intent(context, CaptureActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ScanIntentResult {
        return ScanIntentResult.parseActivityResult(resultCode, intent)
    }
}
