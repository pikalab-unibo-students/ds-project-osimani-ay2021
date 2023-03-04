package it.unibo.tuprolog.solve.lpaas.server.utils

import it.unibo.tuprolog.solve.Solution

class CursorSolutions(private val iterator: Iterator<Solution>) :
    Iterator<Solution> {

    private val solutionsCache: MutableList<Solution> = mutableListOf()

    /** Generate all solutions till the requested one **/
    fun getSolution(index: Int): Solution {
        while(solutionsCache.size <= index) {
            next()
        }
        return solutionsCache[index]
    }

    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun next(): Solution {
        if(hasNext()) {
            val solution = iterator.next()
            solutionsCache.add(solution)
            return solution
        } else throw IndexOutOfBoundsException()
    }
}