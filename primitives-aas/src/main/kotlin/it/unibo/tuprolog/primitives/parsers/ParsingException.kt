package it.unibo.tuprolog.primitives.parsers

import com.google.protobuf.GeneratedMessageV3

class ParsingException: RuntimeException {

    constructor(message: GeneratedMessageV3) :
        super("Couldn't deserialize $message")

    constructor(obj: Any) :
        super("Couldn't serialize $obj")
}