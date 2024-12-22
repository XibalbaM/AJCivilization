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
    private val lastLayerIndex = layers.size - 1
    val hiddenLayers = layers.sliceArray(1 until lastLayerIndex)
    
    private val activations = Array(layers.size) { i -> DoubleArray(layers[i]) }
    
    fun feedForward(input: DoubleArray): DoubleArray {
        require(input.size == layers[0]) { "Input size ${input.size} does not match first layer size ${layers[0]}" }

        System.arraycopy(input, 0, activations[0], 0, input.size)

        for (i in 1..lastLayerIndex) {
            val currentLayer = activations[i]
            val previousLayer = activations[i - 1]
            val layerWeights = weights[i - 1]
            val layerBiases = biases[i - 1]
            
            for (j in currentLayer.indices) {
                var sum = layerBiases[j]
                val neuronWeights = layerWeights[j]
                
                var k = 0
                while (k <= previousLayer.size - 4) {
                    sum += previousLayer[k] * neuronWeights[k] +
                           previousLayer[k + 1] * neuronWeights[k + 1] +
                           previousLayer[k + 2] * neuronWeights[k + 2] +
                           previousLayer[k + 3] * neuronWeights[k + 3]
                    k += 4
                }
                
                while (k < previousLayer.size) {
                    sum += previousLayer[k] * neuronWeights[k]
                    k++
                }
                
                currentLayer[j] = sigmoid(sum)
            }
        }

        return activations[lastLayerIndex]
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
        ObjectOutputStream(output).use { writer ->
            writer.writeObject(layers)
            
            weights.forEach { layer ->
                layer.forEach { writer.writeObject(it) }
            }
            
            biases.forEach { writer.writeObject(it) }
        }
    }
    companion object {
        fun load(input: InputStream): NeuralNetwork {
            ObjectInputStream(input).use { reader ->
                val layers = reader.readObject() as IntArray
                
                val weights = Array(layers.size - 1) { i ->
                    Array(layers[i + 1]) { reader.readObject() as DoubleArray }
                }
                
                val biases = Array(layers.size - 1) { reader.readObject() as DoubleArray }
                
                return NeuralNetwork(layers, weights, biases)
            }
        }

        fun load(file: File): NeuralNetwork = file.inputStream().use { load(it) }
    }
}

