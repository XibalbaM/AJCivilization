package fr.xibalba.aj.civilization

fun main() {
    repeat(10) {
        val ais = listOf(NoAI(), SimpleAI1())
        val results = ais.associate { ai ->
            val gameEngine = GameEngine()
            val (happiness, population) = gameEngine.simulate(ai, 100)
            ai.name to Pair(happiness, population)
        }
        println(results)
    }
}