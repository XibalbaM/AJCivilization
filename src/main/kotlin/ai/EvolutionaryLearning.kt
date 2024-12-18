package fr.xibalba.aj.civilization.ai

import kotlin.random.Random

class EvolutionaryLearning {
    data class TrainingParameters(
        val populationSize: Int,
        val generationCount: Int,
        val inputSize: Int,
        val outputSize: Int,
        val minHiddenLayers: Int = 1,
        val maxHiddenLayers: Int = 4,
        val minNeuronsPerLayer: Int = 4,
        val maxNeuronsPerLayer: Int = 16,
        val mutationRate: Double = 0.1,
        val tournamentSize: Int = 3,
        val architectureMutationRate: Double = 0.2
    )

    data class NetworkHiddenLayers(
        val hiddenLayers: List<Int>
    ) {
        fun copy() = NetworkHiddenLayers(hiddenLayers.toList())
        
        fun mutate(params: TrainingParameters) {
            if (Random.nextDouble() < params.architectureMutationRate) {
                when (Random.nextInt(3)) {
                    0 -> addLayer(params)
                    1 -> removeLayer(params)
                    2 -> modifyLayer(params)
                }
            }
        }

        private fun addLayer(params: TrainingParameters) {
            if (hiddenLayers.size < params.maxHiddenLayers) {
                val newSize = Random.nextInt(
                    params.minNeuronsPerLayer,
                    params.maxNeuronsPerLayer + 1
                )
                val position = Random.nextInt(hiddenLayers.size + 1)
                (hiddenLayers as MutableList).add(position, newSize)
            }
        }

        private fun removeLayer(params: TrainingParameters) {
            if (hiddenLayers.size > params.minHiddenLayers) {
                val position = Random.nextInt(hiddenLayers.size)
                (hiddenLayers as MutableList).removeAt(position)
            }
        }

        private fun modifyLayer(params: TrainingParameters) {
            if (hiddenLayers.isNotEmpty()) {
                val position = Random.nextInt(hiddenLayers.size)
                val currentSize = hiddenLayers[position]
                var newSize = currentSize + Random.nextInt(-2, 3)
                newSize = newSize.coerceIn(params.minNeuronsPerLayer, params.maxNeuronsPerLayer)
                (hiddenLayers as MutableList)[position] = newSize
            }
        }
    }

    data class NetworkWithArchitecture(
        val network: NeuralNetwork,
        val architecture: NetworkHiddenLayers
    ) {
        fun copy(): NetworkWithArchitecture {
            return NetworkWithArchitecture(
                network.copy(),
                architecture.copy()
            )
        }
    }

    fun train(
        parameters: TrainingParameters,
        computeFeedback: (NeuralNetwork) -> Double,
        initialNetwork: NeuralNetwork? = null
    ): NeuralNetwork {
        // Initialize population with random architectures
        var population = if (initialNetwork != null) {
            // Create population based on the initial network
            List(parameters.populationSize) {
                if (it == 0) {
                    // Keep one exact copy of the initial network
                    NetworkWithArchitecture(
                        initialNetwork.copy(),
                        NetworkHiddenLayers(initialNetwork.hiddenLayers.toMutableList())
                    )
                } else {
                    // Create variations of the initial network
                    val architecture = NetworkHiddenLayers(initialNetwork.hiddenLayers.toMutableList())
                    val network = initialNetwork.copy()
                    val networkWithArch = NetworkWithArchitecture(network, architecture)
                    // Apply mutations to create diversity
                    mutate(networkWithArch, parameters)
                    networkWithArch
                }
            }
        } else {
            // Create random population if no initial network is provided
            List(parameters.populationSize) {
                createRandomNetwork(parameters)
            }
        }

        // Evolution loop
        repeat(parameters.generationCount) { generation ->
            // Evaluate fitness for each network
            val populationWithFitness = population.map { network ->
                network to computeFeedback(network.network)
            }

            // Sort by fitness (descending)
            val sortedPopulation = populationWithFitness.sortedByDescending { it.second }

            // Create new population
            val newPopulation = mutableListOf<NetworkWithArchitecture>()

            // Keep the best performing network (elitism)
            newPopulation.add(sortedPopulation.first().first.copy())

            // Fill rest of population with offspring
            while (newPopulation.size < parameters.populationSize) {
                val parent1 = selectParent(sortedPopulation, parameters.tournamentSize)
                val parent2 = selectParent(sortedPopulation, parameters.tournamentSize)
                val child = crossover(parent1, parent2)
                mutate(child, parameters)
                newPopulation.add(child)
            }

            population = newPopulation
        }

        // Return the best network from final population
        return population.maxBy { computeFeedback(it.network) }.network
    }

    private fun createRandomNetwork(params: TrainingParameters): NetworkWithArchitecture {
        val layerCount = Random.nextInt(
            params.minHiddenLayers,
            params.maxHiddenLayers + 1
        )
        
        val hiddenLayers = List(layerCount) {
            Random.nextInt(params.minNeuronsPerLayer, params.maxNeuronsPerLayer + 1)
        }
        
        val architecture = NetworkHiddenLayers(hiddenLayers.toMutableList())
        val network = NeuralNetwork(intArrayOf(params.inputSize) + hiddenLayers + params.outputSize)
        
        return NetworkWithArchitecture(network, architecture)
    }

    private fun selectParent(
        populationWithFitness: List<Pair<NetworkWithArchitecture, Double>>,
        tournamentSize: Int
    ): NetworkWithArchitecture {
        val tournament = populationWithFitness.shuffled().take(tournamentSize)
        return tournament.maxBy { it.second }.first
    }

    private fun crossover(
        parent1: NetworkWithArchitecture,
        parent2: NetworkWithArchitecture
    ): NetworkWithArchitecture {
        // Create new architecture by randomly selecting from either parent
        val childArchitecture = if (Random.nextBoolean()) parent1.architecture.copy() else parent2.architecture.copy()
        
        // Create new network with child architecture
        val childNetwork = NeuralNetwork(
            intArrayOf(parent1.network.layers[0]) + 
            childArchitecture.hiddenLayers + 
            parent1.network.layers.last()
        )
        
        val child = NetworkWithArchitecture(childNetwork, childArchitecture)
        
        // Copy weights where layers match in size
        for (layerIndex in child.network.weights.indices) {
            // Only copy if layer sizes match between child and both parents
            if (layerIndex < parent1.network.weights.size && 
                layerIndex < parent2.network.weights.size &&
                child.network.weights[layerIndex].size == parent1.network.weights[layerIndex].size &&
                child.network.weights[layerIndex].size == parent2.network.weights[layerIndex].size &&
                child.network.weights[layerIndex][0].size == parent1.network.weights[layerIndex][0].size &&
                child.network.weights[layerIndex][0].size == parent2.network.weights[layerIndex][0].size) {
                
                for (i in child.network.weights[layerIndex].indices) {
                    for (j in child.network.weights[layerIndex][i].indices) {
                        // Randomly select weight from either parent
                        child.network.weights[layerIndex][i][j] = if (Random.nextBoolean()) 
                            parent1.network.weights[layerIndex][i][j]
                        else
                            parent2.network.weights[layerIndex][i][j]
                    }
                }
            }
        }

        return child
    }

    private fun mutate(network: NetworkWithArchitecture, params: TrainingParameters) {
        // Mutate architecture
        network.architecture.mutate(params)
        
        // Mutate weights
        for (layerWeights in network.network.weights) {
            for (i in layerWeights.indices) {
                for (j in layerWeights[i].indices) {
                    if (Random.nextDouble() < params.mutationRate) {
                        layerWeights[i][j] += Random.nextDouble(-0.5, 0.5)
                    }
                }
            }
        }
    }
}

