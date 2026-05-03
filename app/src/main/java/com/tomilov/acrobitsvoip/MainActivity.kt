package com.tomilov.acrobitsvoip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.tomilov.acrobitsvoip.core.designsystem.theme.AcrobitsVoipTheme
import com.tomilov.acrobitsvoip.di.AppContainer
import com.tomilov.acrobitsvoip.feature.calling.CallingRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val services = remember { AppContainer.services }
            AcrobitsVoipTheme {
                CallingRoute(
                    softphoneClient = services.softphoneClient,
                    voipConfig = services.voipConfig
                )
            }
        }
    }
}
