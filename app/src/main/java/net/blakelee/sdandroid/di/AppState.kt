package net.blakelee.sdandroid.di

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface AppState {
    var processing: Boolean
    var progress: Float
    var onProcess: () -> Unit
    var onCancel: () -> Unit
}

@Singleton
class RealAppState @Inject constructor() : AppState {
    override var processing: Boolean by mutableStateOf(false)
    override var progress: Float by mutableStateOf(0F)
    override var onCancel: () -> Unit = {}
    override var onProcess: () -> Unit = {}
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {

    @Binds
    abstract fun bindAppState(realAppState: RealAppState): AppState
}