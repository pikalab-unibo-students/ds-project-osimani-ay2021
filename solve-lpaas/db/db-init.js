const mongo = new Mongo();
const db = mongo.getDB("lpaas");

// Elimino gli eventuali documenti della collection
try {
    db.serializedSolver.deleteMany({});
} catch (error) {
    print(error);
}

print("DB initialized correctly");