import sys
sys.path.append('./generatedProto')

import random
import string
import asyncio
import generatedProto.primitiveService_pb2 as primitivesMsg
import generatedProto.basicMessages_pb2 as basicMsg
import SubRequestEvent
import DistribuitedElements
from typing import Generator
from queue import Queue

class ServerSession:
    stream: Generator[primitivesMsg.GeneratorMsg, None, None]
    ongoingSubRequests: list[SubRequestEvent.SubRequestEvent] = list()
    msgQueue: Queue[primitivesMsg.GeneratorMsg]
    
    def __init__(self,
                 primitive: DistribuitedElements.DistribuitedPrimitive,
                 request: primitivesMsg.RequestMsg,
                 queue:  Queue[primitivesMsg.GeneratorMsg]):
        self.msgQueue = queue
        self.stream = primitive.solve(
            request = DistribuitedElements.DistribuitedRequest(
                functor = request.signature.name,
                arity = request.signature.arity,
                arguments = request.arguments,
                context = request.context, 
                session = self)
            )

    def handleMessage(self, msg: primitivesMsg.SolverMsg):
        #Compute Next Solution
        if msg.HasField("next"):
            nextValue: DistribuitedElements.DistribuitedResponse = next(self.stream)
            self.msgQueue.put(
                primitivesMsg.GeneratorMsg(
                    response=primitivesMsg.ResponseMsg(
                        solution = nextValue.solution, sideEffects= nextValue.sideEffects)
                )
            )
        #Handle response from subrequest
        elif msg.HasField("response"):
            subResponse: primitivesMsg.SubResponseMsg = msg.response
            if subResponse.HasField("line") or subResponse.HasField("solution"):
                event: SubRequestEvent.SubRequestEvent = list(
                    filter(lambda temp: temp.id == subResponse.id, self.ongoingSubRequests)).pop()
                event.signalResponse(subResponse)
                self.ongoingSubRequests.remove(event)
                
        
    def subSolve(self, query: basicMsg.StructMsg, timeout: int) -> Generator[primitivesMsg.SolutionMsg, None, None]: 
        id = self.idGenerator()
        hasNext = True
        while hasNext:
            event: SubRequestEvent.SingleSubSolveEvent = SubRequestEvent.SingleSubSolveEvent(
                id = id,
                query = query,
                timeout = timeout)
            self.ongoingSubRequests.append(event)
            self.msgQueue.put(item = event.msg)
            result = event.awaitResult()
            hasNext = result.hasNext
            yield result
        
    def readLine(self, channelName: str) -> str:
        event: SubRequestEvent.ReadLineEvent = SubRequestEvent.ReadLineEvent(self.idGenerator(), channelName)
        self.ongoingSubRequests.append(event)
        self.msgQueue.put(item = event.msg)
        result = event.awaitResult()
        return result
    
    def idGenerator(self) -> str:
        characters = string.ascii_letters + string.digits
        id = ''.join(random.choice(characters) for i in range(10)) 
        while(len(list(filter(lambda temp: temp.id == id, self.ongoingSubRequests))) != 0):
            id = ''.join(random.choice(characters) for i in range(10)) 
        return id 