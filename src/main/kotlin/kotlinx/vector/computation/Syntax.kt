package kotlinx.vector.computation

import jdk.incubator.vector.FloatVector
import jdk.incubator.vector.Vector
import jdk.incubator.vector.VectorOperators
import jdk.incubator.vector.VectorSpecies

sealed interface VectorComputation<E: Number> {
    data class Generate<E: Number>(
        val toVector: (species: VectorSpecies<E>, offset: Int) -> Vector<E>,
        val toElement: (offset: Int) -> E
    ): VectorComputation<E>
    data class Broadcast<E: Number>(val e: Long): VectorComputation<E>
    data class LanewiseUnary<E: Number>(
        val operator: VectorOperators.Unary,
        val argument: VectorComputation<E>,
        val mask: VectorMaskComputation<E>?
    ) : VectorComputation<E>
    data class LanewiseBinary<E: Number>(
        val operator: VectorOperators.Binary,
        val left: VectorComputation<E>,
        val right: VectorComputation<E>,
        val mask: VectorMaskComputation<E>?
    ) : VectorComputation<E>
}

sealed interface VectorMaskComputation<E: Number> {
    data class Not<E: Number>(
        val argument: VectorMaskComputation<E>
    ) : VectorMaskComputation<E>
    data class ComparisonBinary<E: Number>(
        val operator: VectorOperators.Comparison,
        val left: VectorComputation<E>,
        val right: VectorComputation<E>
    ) : VectorMaskComputation<E>
}

val FloatArray.v: VectorComputation<Float> get() = VectorComputation.Generate(
    toVector = { species, offset -> FloatVector.fromArray(species, this, offset) },
    toElement = { this[it] }
)

fun <E: Number> constant(value: Long): VectorComputation<E> = VectorComputation.Broadcast(value)

operator fun <E: Number> VectorOperators.Unary.invoke(argument: VectorComputation<E>): VectorComputation<E> =
    VectorComputation.LanewiseUnary(this, argument, null)

operator fun <E: Number> VectorOperators.Unary.invoke(argument: VectorComputation<E>, mask: VectorMaskComputation<E>): VectorComputation<E> =
    VectorComputation.LanewiseUnary(this, argument, mask)

operator fun <E: Number> VectorOperators.Binary.invoke(left: VectorComputation<E>, right: VectorComputation<E>): VectorComputation<E> =
    VectorComputation.LanewiseBinary(this, left, right, null)

operator fun <E: Number> VectorOperators.Binary.invoke(left: VectorComputation<E>, right: VectorComputation<E>, mask: VectorMaskComputation<E>): VectorComputation<E> =
    VectorComputation.LanewiseBinary(this, left, right, mask)

operator fun <E: Number> VectorOperators.Comparison.invoke(left: VectorComputation<E>, right: VectorComputation<E>): VectorMaskComputation<E> =
    VectorMaskComputation.ComparisonBinary(this, left, right)

operator fun <E: Number> VectorComputation<E>.plus(other: VectorComputation<E>): VectorComputation<E> = VectorOperators.ADD(this, other)
operator fun <E: Number> VectorComputation<E>.times(other: VectorComputation<E>): VectorComputation<E> = VectorOperators.MUL(this, other)
operator fun <E: Number> VectorComputation<E>.unaryMinus(): VectorComputation<E> = VectorOperators.NEG(this)

operator fun <E: Number> VectorMaskComputation<E>.not(): VectorMaskComputation<E> = VectorMaskComputation.Not(this)
infix fun <E: Number> VectorComputation<E>.`==`(other: VectorComputation<E>): VectorMaskComputation<E> = VectorOperators.EQ(this, other)
infix fun <E: Number> VectorComputation<E>.`!=`(other: VectorComputation<E>): VectorMaskComputation<E> = VectorOperators.NE(this, other)