package `in`.androidengineers.cookbook

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class CookbookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Missing google-services.json is expected in fixture mode.
        if (FirebaseApp.initializeApp(this) != null) {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        }
    }
}
