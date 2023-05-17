import sys
sys.path.append('./generatedProto')

from abc import ABC, abstractmethod
from typing import Generator
import generatedProto.sideEffectsMessages_pb2 as sideEffectsMsg
import generatedProto.primitiveService_pb2 as primitivesMsg
import generatedProto.basicMessages_pb2 as basicMsg
import generatedProto.errorsMessages_pb2 as errorMsg
import math

class DistribuitedRequest:
    functor: str
    arity: int
    arguments: list[basicMsg.ArgumentMsg]
    context: basicMsg.ExecutionContextMsg
    session = None
    query: basicMsg.StructMsg
    
    def __init__(self, functor: str, arity: int, arguments: list[basicMsg.ArgumentMsg],
                 context: basicMsg.ExecutionContextMsg, session):
        self.functor = functor
        self.arity = arity
        self.arguments = arguments
        self.context = context
        self.session = session
        self.query = basicMsg.StructMsg(functor=functor, arguments=arguments)
        

    def replyWith(self, condition: bool, sideEffects: sideEffectsMsg.SideEffectMsg = None, hasNext = True): 
        if condition:
            type = primitivesMsg.SolutionMsg.SUCCESS
        else:
            type = primitivesMsg.SolutionMsg.FAIL
        return DistribuitedResponse(
            primitivesMsg.SolutionMsg(
                query = self.query,
                type = type,
                hasNext = hasNext),
            sideEffects=sideEffects
        )
        
    def replySuccess(self, substitutions = None,  sideEffects = None, hasNext = True):
        return DistribuitedResponse(
            primitivesMsg.SolutionMsg(
                query = self.query,
                substitutions = substitutions,
                type =  primitivesMsg.SolutionMsg.SUCCESS,
                hasNext = hasNext),
            sideEffects=sideEffects
        )
        
    def replyFail(self, sideEffects: sideEffectsMsg.SideEffectMsg = None, hasNext = False):
        return DistribuitedResponse(
            primitivesMsg.SolutionMsg(
                query = self.query, 
                type =  primitivesMsg.SolutionMsg.FAIL,
                hasNext = hasNext),
            sideEffects=sideEffects
        )
        
    def replyError(self, error: errorMsg.ResolutionExceptionMsg,  sideEffects: sideEffectsMsg.SideEffectMsg = None, hasNext = False):
        return DistribuitedResponse(
            primitivesMsg.SolutionMsg(
                query = self.query,
                error = error,
                type = primitivesMsg.SolutionMsg.HALT,
                hasNext = hasNext),
            sideEffects=sideEffects
        )
        
    def subSolve(self, query: basicMsg.StructMsg, timeout: int = int(math.log2(sys.maxsize * 2 + 2))) -> Generator[primitivesMsg.SolutionMsg, None, None]:
        return self.session.subSolve(query, timeout)
        
        
    def readLine(self, channelName: str) -> str:
        return self.session.readLine(channelName)    
        
    

class DistribuitedResponse:
    solution: primitivesMsg.SolutionMsg
    sideEffects: list[sideEffectsMsg.SideEffectMsg]
    
    def __init__(self, solution: primitivesMsg.SolutionMsg,  sideEffects: list[sideEffectsMsg.SideEffectMsg]):
        self.solution = solution
        self.sideEffects = sideEffects
        
        
class DistribuitedPrimitive(ABC):
    @abstractmethod
    def solve(self, request: DistribuitedRequest) -> Generator[DistribuitedResponse, None, None]:
        pass