package ru.inncreator.vezdekod

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import ru.inncreator.vezdekod.utils.FFmpegRepo
import timber.log.Timber

class App :Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            // включаем тимбер
            Timber.plant(Timber.DebugTree())
        }


        val vmModule = module {
            // страница авторизации
           // viewModel { AuthorizationViewModel() }
        }

        // модуль приложения
        val appModule = module {
            // подключаем SharedPreferences
//            single { SharedPrefManager(get()) }
            single { FFmpegRepo() }
        }


        // коин
        startKoin {
            androidContext(this@App)
            androidLogger(Level.DEBUG)
            androidFileProperties()
            modules(listOf(appModule,/* remoteModule, databaseModule, repositoryModule,*/ vmModule))
        }
    }
}