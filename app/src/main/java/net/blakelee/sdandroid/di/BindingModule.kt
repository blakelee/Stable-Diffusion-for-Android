package net.blakelee.sdandroid.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.blakelee.sdandroid.*
import net.blakelee.sdandroid.img2img.Image2ImageConfig
import net.blakelee.sdandroid.img2img.RealImage2ImageConfig
import net.blakelee.sdandroid.text2image.RealText2ImageConfig
import net.blakelee.sdandroid.text2image.Text2ImageConfig

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {

    @Binds
    abstract fun bindAppState(appState: RealAppState): AppState

    @Binds
    abstract fun bindText2ImageConfig(config: RealText2ImageConfig): Text2ImageConfig

    @Binds
    abstract fun bindImage2ImageConfig(config: RealImage2ImageConfig): Image2ImageConfig
}