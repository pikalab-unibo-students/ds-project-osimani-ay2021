package it.unibo.tuprolog.primitives.server.distribuited

fun interface DistribuitedPrimitive {

    fun solve(request: DistribuitedRequest): Sequence<DistributedResponse>

    companion object {
        @JvmStatic
        fun of(
            function: (DistribuitedRequest) -> Sequence<DistributedResponse>
        ): DistribuitedPrimitive =
            DistribuitedPrimitive(function)
    }
}