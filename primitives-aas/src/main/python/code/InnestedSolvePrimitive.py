import logging
import sys
sys.path.append('./generatedProto')

import generatedProto.basicMessages_pb2 as basicMsg
import PrimitiveServerWrapper
import DistribuitedElements
from typing import Generator

class InnestedPrimitive(DistribuitedElements.DistribuitedPrimitive):

    def solve(self, request: DistribuitedElements.DistribuitedRequest) -> Generator[DistribuitedElements.DistribuitedResponse, None, None]:
        arg0 = request.arguments[0]
        for solution in request.subSolve(arg0.struct):
            if(solution.type == solution.SUCCESS):
                yield request.replySuccess(solution.substitutions, hasNext=solution.hasNext)
            elif(solution.type == solution.FAIL):
                yield request.replyFail()
            else:
                yield request.replyError(solution.error)
    
if __name__ == '__main__':
    logging.basicConfig()
    PrimitiveServerWrapper.serve(InnestedPrimitive(), "solve", 1, 8080, "customLibrary")
    
        