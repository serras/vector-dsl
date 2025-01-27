package kotlinx.vector.element

import jdk.incubator.vector.FloatVector
import kotlin.math.max
import kotlinx.vector.computation.*

fun onEach(arg: FloatArray, block: (ElementComputation<Float>) -> ElementComputation<Float>): FloatArray {
    val result = FloatArray(arg.size)
    return block(arg.e).toComputation().executeInto(result)
}

fun onEach(arg1: FloatArray, arg2: FloatArray, block: (ElementComputation<Float>, ElementComputation<Float>) -> ElementComputation<Float>): FloatArray {
    val result = FloatArray(max(arg1.size, arg2.size))
    return block(arg1.e, arg2.e).toComputation().executeInto(result)
}

val FloatArray.e: ElementComputation<Float> get() = ElementComputation.Variable(
    toVector = { species, offset -> FloatVector.fromArray(species, this, offset) },
    toElement = { this[it] }
)

fun <E: Number> ElementComputation<E>.toComputation(): VectorComputation<E> = when (this) {
    is ElementComputation.Variable ->
        VectorComputation.Generate(toVector, toElement)
    is ElementComputation.Constant ->
        VectorComputation.Broadcast(e)
    is ElementComputation.UnaryOp ->
        VectorComputation.LanewiseUnary(operator, argument.toComputation(), mask?.toComputation())
    is ElementComputation.BinaryOp ->
        VectorComputation.LanewiseBinary(operator, left.toComputation(), right.toComputation(), mask?.toComputation())
}

fun <E: Number> ElementMaskComputation<E>.toComputation(): VectorMaskComputation<E> = when (this) {
    is ElementMaskComputation.Not ->
        VectorMaskComputation.Not(argument.toComputation())
    is ElementMaskComputation.Comparison ->
        VectorMaskComputation.ComparisonBinary(operator, left.toComputation(), right.toComputation())
}