package fr.xibalba.aj.civilization

import fr.xibalba.aj.civilization.ai.EvolutionaryLearning
import fr.xibalba.aj.civilization.ai.NeuralNetwork
import java.io.File


fun trainEvolutionary(evaluationFunction: (NeuralNetwork) -> Double) {
    val computationCountForEvaluation = 5
    val parameters = EvolutionaryLearning.TrainingParameters(
        populationSize = 100,
        generationCount = 50,
        inputSize = NEURAL_NETWORK_INPUTS,
        outputSize = NEURAL_NETWORK_OUTPUTS,
        minHiddenLayers = 2,
        maxHiddenLayers = 4,
        minNeuronsPerLayer = 8,
        maxNeuronsPerLayer = 16,
        mutationRate = 0.1,
        tournamentSize = 3,
        architectureMutationRate = 0.1
    )

    // Load previous network if exists
    val initialNetwork = try {
        File("trained_network.nn").inputStream().use {
            NeuralNetwork.load(it)
        }
    } catch (_: Exception) {
        null
    }

    val evolutionaryLearning = EvolutionaryLearning()

    val computeFeedback = { network: NeuralNetwork ->
        var score = 0.0
        repeat(computationCountForEvaluation) {
            score += evaluationFunction(network)
        }
        score / computationCountForEvaluation.toDouble()
    }

    println("Starting training...")
    val trainedNetwork = evolutionaryLearning.train(
        parameters = parameters,
        computeFeedback = computeFeedback,
        initialNetwork = initialNetwork
    )

    // Save the trained network
    println("Training complete. Saving network...")
    File("trained_network.nn").outputStream().use {
        trainedNetwork.save(it)
    }
    println("Network saved to trained_network.nn")
}