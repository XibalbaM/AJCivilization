package fr.xibalba.aj.civilization

import fr.xibalba.aj.civilization.ai.NeuralNetwork

private fun runSimulation(neuralNetwork: NeuralNetwork, turns: Int): GameState {
    val gameEngine = GameEngine()
    gameEngine.simulate(NeuralNetworkAI(neuralNetwork), turns)
    return gameEngine.state
}

fun happinessEvaluation(neuralNetwork: NeuralNetwork, turns: Int): Double {
    val state = runSimulation(neuralNetwork, turns)
    return state.happiness.toDouble()
}

fun populationEvaluation(neuralNetwork: NeuralNetwork, turns: Int): Double {
    val state = runSimulation(neuralNetwork, turns)
    return state.population.toDouble()
}

fun happinessAndPopulationEvaluation(neuralNetwork: NeuralNetwork, turns: Int): Double {
    val state = runSimulation(neuralNetwork, turns)
    return state.happiness.toDouble() * state.population.toDouble()
}