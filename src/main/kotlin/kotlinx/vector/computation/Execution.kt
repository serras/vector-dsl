package kotlinx.vector.computation

import jdk.incubator.vector.*

fun VectorComputation<Float>.executeInto(result: FloatArray, species: VectorSpecies<Float> = FloatVector.SPECIES_PREFERRED): FloatArray {
    var i = 0
    val upperBound = species.loopBound(result.size)
    while (i < upperBound) {
        (this.executeLane(species, i) as FloatVector).intoArray(result, i)
        i += species.length()
    }
    while (i < result.size) {
        result[i] = this.executeSingle(i)
        i++
    }
    return result
}

fun VectorMaskComputation<Float>.executeInto(result: BooleanArray, species: VectorSpecies<Float> = FloatVector.SPECIES_PREFERRED): BooleanArray {
    var i = 0
    val upperBound = species.loopBound(result.size)
    while (i < upperBound) {
        this.executeLane(species, i).intoArray(result, i)
        i += species.length()
    }
    while (i < result.size) {
        result[i] = this.executeSingle(i)
        i++
    }
    return result
}

fun <E: Number> VectorComputation<E>.executeLane(species: VectorSpecies<E>, index: Int): Vector<E> = when (this) {
    is VectorComputation.Generate -> toVector(species, index)
    is VectorComputation.Broadcast -> species.broadcast(e)
    is VectorComputation.LanewiseUnary -> when (mask) {
        null -> argument.executeLane(species, index).lanewise(operator)
        else -> argument.executeLane(species, index).lanewise(operator, mask.executeLane(species, index))
    }
    is VectorComputation.LanewiseBinary -> when (mask) {
        null -> left.executeLane(species, index).lanewise(operator, right.executeLane(species, index))
        else -> left.executeLane(species, index).lanewise(operator, right.executeLane(species, index), mask.executeLane(species, index))
    }
}

fun <E: Number> VectorMaskComputation<E>.executeLane(species: VectorSpecies<E>, index: Int): VectorMask<E> = when (this) {
    is VectorMaskComputation.Not -> argument.executeLane(species, index).not()
    is VectorMaskComputation.ComparisonBinary -> left.executeLane(species, index).compare(operator, right.executeLane(species, index))
}

fun VectorComputation<Float>.executeSingle(index: Int): Float {
    return when (this) {
        is VectorComputation.Generate -> toElement(index)
        is VectorComputation.Broadcast -> e.toFloat()
        is VectorComputation.LanewiseUnary -> {
            val result = argument.executeSingle(index)
            if (mask?.executeSingle(index) == false) return result
            when (operator) {
                VectorOperators.NEG -> - result
                else -> error("Unsupported unary operator $operator")
            }
        }
        is VectorComputation.LanewiseBinary -> {
            val leftResult = left.executeSingle(index)
            if (mask?.executeSingle(index) == false) return leftResult
            val rightResult = right.executeSingle(index)
            when (operator) {
                VectorOperators.ADD -> leftResult + rightResult
                VectorOperators.MUL -> leftResult * rightResult
                else -> error("Unsupported binary operator $operator")
            }
        }
    }
}

fun VectorMaskComputation<Float>.executeSingle(index: Int): Boolean = when (this) {
    is VectorMaskComputation.Not -> argument.executeSingle(index).not()
    is VectorMaskComputation.ComparisonBinary -> {
        val leftResult = left.executeSingle(index)
        val rightResult = right.executeSingle(index)
        when (operator) {
            VectorOperators.EQ -> leftResult == rightResult
            VectorOperators.NE -> leftResult != rightResult
            else -> error("Unsupported binary operator $operator")
        }
    }
}
