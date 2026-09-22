package `in`.androidengineers.pocketcards
import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
class PocketCardsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.initializeApp(this) != null) {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
        }
    }
}
