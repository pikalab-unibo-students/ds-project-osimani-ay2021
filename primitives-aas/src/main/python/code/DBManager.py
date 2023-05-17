from pymongo import MongoClient

def __initConnection(url: str):
    client = MongoClient(url)
    return client['primitives']["serializedPrimitive"]

__get = __initConnection("mongodb://localhost:27017")

def addPrimitive(functor: str, arity: int, libraryName: str, url: str, port: int):
    if(getPrimitive(functor, arity) == None):
        signature = {
            "functor" : functor,
            "arity" : arity,
            "libraryName" : libraryName,
            "url" : url,
            "port" : port,
        }
        __get.insert_one(signature)
    else:
        #Choose between error and update
        old_signature = {
            "functor" : functor,
            "arity" : arity
        }
        new_signature = {
            "$set": {
                "libraryName" : libraryName,
                "url" : url,
                "port" : port,
            }
        }
        __get.update_one(old_signature, new_signature)
    
def getPrimitive(functor: str, arity: int):
    signature = {
        "functor" : functor,
        "arity" : arity,
    }
    return __get.find_one(signature)
    
def deletePrimitive(functor: str, arity: int, libraryName: str):
    signature = {
            "functor" : functor,
            "arity" : arity,
            "libraryName" : libraryName
        }
    __get.delete_one(signature)