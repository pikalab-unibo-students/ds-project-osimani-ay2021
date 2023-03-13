package it.unibo.tuprolog.solve.lpaas.util

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.lpaas.solveMessage.LineEvent
import it.unibo.tuprolog.solve.lpaas.solveMessage.OutputChannelEvent

sealed class GenericStreamObserver {

    data class OfLineEvent(val streamObserver: StreamObserver<LineEvent>): GenericStreamObserver() {
        override fun onCompleted() {
            streamObserver.onCompleted()
        }
    }

    data class OfOutputChannelEvent(val streamObserver: StreamObserver<OutputChannelEvent>): GenericStreamObserver() {
        override fun onCompleted() {
            streamObserver.onCompleted()
        }
    }

    abstract fun onCompleted()
}