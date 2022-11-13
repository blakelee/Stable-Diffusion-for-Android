package net.blakelee.sdandroid

import com.squareup.workflow1.BaseRenderContext
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.WorkflowAction
import com.squareup.workflow1.WorkflowAction.Companion.noAction
import javax.inject.Inject
import javax.inject.Provider

class WorkflowProvider @Inject constructor(
    private val map: Map<Class<*>, @JvmSuppressWildcards Provider<Workflow<*, *, *>>>
) {

    data class WorkflowProviderRenderContext<PropsT, StateT, OutputT, BaseRenderContextT : BaseRenderContext<PropsT, StateT, OutputT>>(
        val renderContext: BaseRenderContextT,
        private val map: Map<Class<*>, @JvmSuppressWildcards Provider<Workflow<*, *, *>>>
    ) {

        fun <ChildPropsT, ChildOutputT, ChildRenderingT, ChildWorkflowT : Workflow<ChildPropsT, ChildOutputT, ChildRenderingT>> renderChild(
            clazz: Class<ChildWorkflowT>,
            props: ChildPropsT,
            key: String = "",
            handler: (ChildOutputT) -> WorkflowAction<PropsT, StateT, OutputT> = { noAction() }
        ): ChildRenderingT {
            val instance = map[clazz]!!.get() as ChildWorkflowT
            return renderContext.renderChild(instance, props, key, handler)
        }
    }

    operator fun <PropsT, StateT, OutputT, BaseRenderContextT : BaseRenderContext<PropsT, StateT, OutputT>> invoke(
        renderContext: BaseRenderContextT
    ) = WorkflowProviderRenderContext(renderContext, map)
}