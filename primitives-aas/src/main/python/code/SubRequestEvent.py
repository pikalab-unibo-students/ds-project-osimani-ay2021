import sys
sys.path.append('../generatedProto')
from abc import ABC, abstractmethod

import generatedProto.primitiveService_pb2 as primitiveMsg
import generatedProto.basicMessages_pb2 as basicMsg
from concurrent.futures import Future

class SubRequestEvent(ABC):
    
    id: str
    msg: primitiveMsg.GeneratorMsg
    
    @abstractmethod
    def signalResponse(self, msg: primitiveMsg.SubResponseMsg):
        pass
    
    def awaitResult(self):
        pass

class ReadLineEvent(SubRequestEvent):
    id: str
    msg: primitiveMsg.GeneratorMsg
    future: Future[primitiveMsg.LineMsg]
    
    def __init__(self, id: str, channelName: str):
        self.id = id
        self.msg = primitiveMsg.GeneratorMsg(
            request = primitiveMsg.SubRequestMsg(
                id = id,
                readLine = primitiveMsg.ReadLineMsg(
                    channelName=channelName
                    )))
        self.future = Future()
    
    def signalResponse(self, msg: primitiveMsg.SubResponseMsg):
        self.future.set_result(msg.line)
    
    def awaitResult(self) -> str:
        return self.future.result().content
    
class SingleSubSolveEvent(SubRequestEvent):
    id: str
    msg: primitiveMsg.GeneratorMsg
    future: Future[primitiveMsg.SolutionMsg]
    
    def __init__(self, id: str, query: basicMsg.StructMsg, timeout: int):
        self.id = id
        self.msg = primitiveMsg.GeneratorMsg(
            request = primitiveMsg.SubRequestMsg(
                id = id,
                subSolve = primitiveMsg.SubSolveRequest(
                    query = query,
                    timeout = timeout
                    )))
        self.future = Future()
    
    def signalResponse(self, msg: primitiveMsg.SubResponseMsg):
        self.future.set_result(msg.solution)
    
    def awaitResult(self) -> primitiveMsg.SolutionMsg:
        return self.future.result()
        