package net.blakelee.sdandroid.di

import com.squareup.workflow1.Workflow
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import net.blakelee.sdandroid.*
import net.blakelee.sdandroid.img2img.Image2ImageConfig
import net.blakelee.sdandroid.img2img.RealImage2ImageConfig
import net.blakelee.sdandroid.landing.LoginWorkflow
import net.blakelee.sdandroid.text2image.RealText2ImageConfig
import net.blakelee.sdandroid.text2image.Text2ImageConfig
import net.blakelee.sdandroid.text2image.Text2ImageWorkflow

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {

    @Binds
    abstract fun bindAppState(appState: RealAppState): AppState

    @Binds
    abstract fun bindText2ImageConfig(config: RealText2ImageConfig): Text2ImageConfig

    @Binds
    abstract fun bindImage2ImageConfig(config: RealImage2ImageConfig): Image2ImageConfig

    @Binds
    @IntoMap
    @ClassKey(LoginWorkflow::class)
    abstract fun bindLoginWorkflow(workflow: LoginWorkflow): Workflow<*, *, *>

    @Binds
    @IntoMap
    @ClassKey(PrimaryWorkflow::class)
    abstract fun bindPrimaryWorkflow(workflow: PrimaryWorkflow): Workflow<*, *, *>

    @Binds
    @IntoMap
    @ClassKey(Text2ImageWorkflow::class)
    abstract fun bindText2ImageWorkflow(workflow: Text2ImageWorkflow): Workflow<*, *, *>
}