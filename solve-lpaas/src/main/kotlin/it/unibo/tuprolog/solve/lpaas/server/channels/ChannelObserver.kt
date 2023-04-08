package it.unibo.tuprolog.solve.lpaas.server.channels

interface ChannelObserver<T : Any> {

    fun getCurrentContent(): List<T>
}
