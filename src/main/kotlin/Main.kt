package fr.xibalba.aj.civilization

import fr.xibalba.aj.civilization.ai.NeuralNetwork
import java.io.File
import kotlin.time.measureTime

fun main() {
    trainEvolutionary {
        populationEvaluation(it, 100)
    }
    val trainedNetwork = NeuralNetwork.load(File("trained_network.nn"))
    val ais = listOf(NoAI(), SimpleAI1(), SimpleAI2(), NeuralNetworkAI(trainedNetwork))
    simulate(ais, 100)
}

fun simulate(ais: List<AI>, turns: Int) {
    measureTime {
        repeat(100) {
            val results = ais.associate { ai ->
                val gameEngine = GameEngine()
                val (happiness, population) = gameEngine.simulate(ai, turns)
                ai.name to Pair(happiness, population)
            }
            println(results)
        }
    }.also(::println)
}