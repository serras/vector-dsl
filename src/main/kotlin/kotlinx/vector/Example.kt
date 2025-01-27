package kotlinx.vector

import jdk.incubator.vector.VectorOperators
import kotlinx.vector.computation.*
import kotlinx.vector.element.*

val a = floatArrayOf(1f, 2f, 3f, 1f, 2f, 3f)
val b = floatArrayOf(3f, 2f, 1f, 3f, 2f, 1f)

fun exampleWithComputation() {
    val computation1 = - (a.v * a.v + b.v * b.v)
    val c1 = computation1.executeInto(FloatArray(6))
    println(c1.toList())

    val computation2 = VectorOperators.ADD(a.v, b.v,  mask = a.v `!=` b.v)
    val c2 = computation2.executeInto(FloatArray(6))
    println(c2.toList())
}

fun exampleByElement() {
    val c1 = onEach(a, b) { i, j -> - (i * i + j * j) }
    println(c1.toList())

    val c2 = onEach(a, b) { i, j -> VectorOperators.ADD(i, j, mask = i `!=` j) }
    println(c2.toList())
}

fun main() {
    exampleWithComputation()
    exampleByElement()
}