package it.unibo.tuprolog.solve.channel

import it.unibo.tuprolog.solve.exception.Warning

internal expect fun stdin(): InputChannel<String>

internal expect fun <T : Any> stdout(): OutputChannel<T>

internal expect fun <T : Any> stderr(): OutputChannel<T>

internal expect fun warning(): OutputChannel<Warning>

internal expect fun stringInputChannel(string: String): InputChannel<String>
