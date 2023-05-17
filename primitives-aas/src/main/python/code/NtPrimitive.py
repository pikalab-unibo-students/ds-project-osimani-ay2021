import logging
import sys
sys.path.append('./generatedProto')

import generatedProto.basicMessages_pb2 as basicMsg
import PrimitiveServerWrapper
import DistribuitedElements
from typing import Generator

class NtPrimitive(DistribuitedElements.DistribuitedPrimitive):
    
    def solve(self, request: DistribuitedElements.DistribuitedRequest) -> Generator[DistribuitedElements.DistribuitedResponse, None, None]:
        arg0 = request.arguments[0]
        if(arg0.HasField("var")):
            n = 0
            while(True):
                substitutions = {}
                substitutions[arg0.var] = basicMsg.ArgumentMsg(constant=str(n))
                yield request.replySuccess(substitutions = substitutions)
                n += 1
        elif(arg0.HasField("constant") and int(arg0.constant)):
            yield request.replySuccess(hasNext = False)
        else:
            yield request.replyFail()
    
if __name__ == '__main__':
    logging.basicConfig()
    PrimitiveServerWrapper.serve(NtPrimitive(), "nt", 1, 8081, "customLibrary")
    
        