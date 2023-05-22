package it.unibo.tuprolog.primitives.server.distribuited

fun interface DistributedPrimitive {

    fun solve(request: DistributedRequest): Sequence<DistributedResponse>

    companion object {
        @JvmStatic
        fun of(
            function: (DistributedRequest) -> Sequence<DistributedResponse>
        ): DistributedPrimitive =
            DistributedPrimitive(function)
    }
}