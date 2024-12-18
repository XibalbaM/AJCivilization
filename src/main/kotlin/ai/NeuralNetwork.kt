package fr.xibalba.aj.civilization.ai

import java.io.File
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import kotlin.math.exp
import kotlin.random.Random

data class NeuralNetwork(
    val layers: IntArray,
    val weights: Array<Array<DoubleArray>> = Array(layers.size - 1) { i ->
        Array(layers[i + 1]) { DoubleArray(layers[i]) { Random.nextDouble(-1.0, 1.0) } }
    },
    val biases: Array<DoubleArray> = Array(layers.size - 1) { i ->
        DoubleArray(layers[i + 1]) { Random.nextDouble(-1.0, 1.0) }
    }
) {
    val hiddenLayers = layers.sliceArray(1 until layers.size - 1)
    val activations = Array(layers.size) { i -> DoubleArray(layers[i]) }

    fun feedForward(input: DoubleArray): DoubleArray {
        require(input.size == layers[0]) { "Input size must match the first layer size" }

        input.copyInto(activations[0])

        for (i in 1 until layers.size) {
            val prevLayer = i - 1
            for (j in activations[i].indices) {
                var sum = biases[prevLayer][j]
                for (k in activations[prevLayer].indices) {
                    sum += activations[prevLayer][k] * weights[prevLayer][j][k]
                }
                activations[i][j] = sigmoid(sum)
            }
        }

        return activations.last()
    }

    private fun sigmoid(x: Double): Double = 1.0 / (1.0 + exp(-x))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NeuralNetwork

        if (!layers.contentEquals(other.layers)) return false
        if (!weights.contentDeepEquals(other.weights)) return false
        if (!biases.contentDeepEquals(other.biases)) return false
        if (!activations.contentDeepEquals(other.activations)) return false

        return true
    }
    override fun hashCode(): Int {
        var result = layers.contentHashCode()
        result = 31 * result + weights.contentDeepHashCode()
        result = 31 * result + biases.contentDeepHashCode()
        result = 31 * result + activations.contentDeepHashCode()
        return result
    }

    fun save(output: OutputStream) {
        val writer = ObjectOutputStream(output)

        writer.writeObject(layers)

        // Write weights
        for (i in weights.indices) {
            for (j in weights[i].indices) {
                writer.writeObject(weights[i][j])
            }
        }

        // Write biases
        for (i in biases.indices) {
            writer.writeObject(biases[i])
        }
    }
    companion object {
        fun load(input: InputStream): NeuralNetwork {
            val reader = ObjectInputStream(input)

            val layers = reader.readObject() as IntArray
            
            // Read weights
            val weights = Array(layers.size - 1) { i ->
                Array(layers[i + 1]) { j ->
                    reader.readObject() as DoubleArray
                }
            }
            
            // Read biases 
            val biases = Array(layers.size - 1) { i ->
                reader.readObject() as DoubleArray
            }
            
            return NeuralNetwork(layers, weights, biases)
        }

        fun load(file: File): NeuralNetwork {
            return file.inputStream().use {
                load(it)
            }
        }
    }
}

