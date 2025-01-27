package kotlinx.vector.element

import jdk.incubator.vector.Vector
import jdk.incubator.vector.VectorOperators
import jdk.incubator.vector.VectorSpecies

sealed interface ElementComputation<E: Number> {
    data class Variable<E: Number>(
        val toVector: (species: VectorSpecies<E>, offset: Int) -> Vector<E>,
        val toElement: (offset: Int) -> E
    ): ElementComputation<E>
    data class Constant<E: Number>(val e: Long): ElementComputation<E>
    data class UnaryOp<E: Number>(
        val operator: VectorOperators.Unary,
        val argument: ElementComputation<E>,
        val mask: ElementMaskComputation<E>?
    ) : ElementComputation<E>
    data class BinaryOp<E: Number>(
        val operator: VectorOperators.Binary,
        val left: ElementComputation<E>,
        val right: ElementComputation<E>,
        val mask: ElementMaskComputation<E>?
    ) : ElementComputation<E>
}

sealed interface ElementMaskComputation<E: Number> {
    data class Not<E: Number>(
        val argument: ElementMaskComputation<E>
    ) : ElementMaskComputation<E>
    data class Comparison<E: Number>(
        val operator: VectorOperators.Comparison,
        val left: ElementComputation<E>,
        val right: ElementComputation<E>
    ) : ElementMaskComputation<E>
}

operator fun <E: Number> VectorOperators.Unary.invoke(argument: ElementComputation<E>): ElementComputation<E> =
    ElementComputation.UnaryOp(this, argument, null)
operator fun <E: Number> VectorOperators.Unary.invoke(argument: ElementComputation<E>, mask: ElementMaskComputation<E>): ElementComputation<E> =
    ElementComputation.UnaryOp(this, argument, mask)
operator fun <E: Number> VectorOperators.Binary.invoke(left: ElementComputation<E>, right: ElementComputation<E>): ElementComputation<E> =
    ElementComputation.BinaryOp(this, left, right, null)
operator fun <E: Number> VectorOperators.Binary.invoke(left: ElementComputation<E>, right: ElementComputation<E>, mask: ElementMaskComputation<E>): ElementComputation<E> =
    ElementComputation.BinaryOp(this, left, right, mask)
operator fun <E: Number> VectorOperators.Comparison.invoke(left: ElementComputation<E>, right: ElementComputation<E>): ElementMaskComputation<E> =
    ElementMaskComputation.Comparison(this, left, right)

operator fun <E: Number> ElementComputation<E>.plus(other: ElementComputation<E>): ElementComputation<E> = VectorOperators.ADD(this, other)
operator fun <E: Number> ElementComputation<E>.times(other: ElementComputation<E>): ElementComputation<E> = VectorOperators.MUL(this, other)
operator fun <E: Number> ElementComputation<E>.unaryMinus(): ElementComputation<E> = VectorOperators.NEG(this)

operator fun <E: Number> ElementMaskComputation<E>.not(): ElementMaskComputation<E> = ElementMaskComputation.Not(this)
infix fun <E: Number> ElementComputation<E>.`==`(other: ElementComputation<E>): ElementMaskComputation<E> = VectorOperators.EQ(this, other)
infix fun <E: Number> ElementComputation<E>.`!=`(other: ElementComputation<E>): ElementMaskComputation<E> = VectorOperators.NE(this, other)