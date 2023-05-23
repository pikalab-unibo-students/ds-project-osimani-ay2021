package it.unibo.tuprolog.primitives.server.distribuited.solve

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