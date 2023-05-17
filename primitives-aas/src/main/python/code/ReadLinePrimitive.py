import logging
import sys
sys.path.append('./generatedProto')

import generatedProto.basicMessages_pb2 as basicMsg
import PrimitiveServerWrapper
import DistribuitedElements
from typing import Generator

class ReadPrimitive(DistribuitedElements.DistribuitedPrimitive):

    def solve(self, request: DistribuitedElements.DistribuitedRequest) -> Generator[DistribuitedElements.DistribuitedResponse, None, None]:
        channelName = request.arguments[0].struct.functor
        while(True):
            char = request.readLine(channelName)
            if(char != ""):
                substitutions = {}
                substitutions[request.arguments[1].var] = basicMsg.ArgumentMsg(constant=char)
                yield request.replySuccess(substitutions = substitutions)
            else:
                yield request.replyFail()
    
if __name__ == '__main__':
    logging.basicConfig()
    PrimitiveServerWrapper.serve(ReadPrimitive(), "readLine", 2, 8082, "customLibrary")
    
        