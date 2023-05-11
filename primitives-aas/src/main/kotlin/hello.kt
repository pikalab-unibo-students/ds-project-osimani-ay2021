import java.util.concurrent.Executors

fun main() {
    println(subSolve())
}

fun subSolve(): String {
    val executor = Executors.newCachedThreadPool()
    println(Thread.currentThread())
    val result =  executor.submit<String> {
        sendRequestToSolver()
    }.get()
    return result
}

fun sendRequestToSolver(): String {
    Thread.sleep(2000)
    return "hello"
}

