package com.tasks

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class CustomFlowOperator {
     var flowA= flowOf(T)
     var flowB= flowOf(T)

    private fun <A, B, C> Flow<A>.combineLatest(
        other: Flow<B>,
        transform: suspend (A, B) -> C
    ): Flow<C> = combine(other) { a, b -> transform(a, b) }

//  flowA and flowB, using the combineLatest operator to get the latest values from both of them
//  and pass them to a transform function:
    fun main(){
        flowA.combineLatest(flowB) { a, b ->
            // Do something with the values of a and b
        }
    }


}