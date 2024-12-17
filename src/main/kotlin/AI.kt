package fr.xibalba.aj.civilization

interface AI {
    val name: String
    fun playTurn(gameState: GameState): List<TurnAction>
}
fun GameEngine.simulate(ia: AI, turns: Int, print: Boolean = false): Pair<Float, Int> {
    repeat(turns) {
        val turnActions = ia.playTurn(this.state)
        val event = tick(turnActions)
        
        if (print) {
            println("Turn ${it + 1}")
            println("Population: ${this.state.population}")
            println("Happiness: ${this.state.happiness}")
            println("Resources:")
            println("  Water: ${this.state.water}")
            println("  Food: ${this.state.food}")
            println("  Power: ${this.state.power}")
            
            if (event != null) {
                println("Event: ${event.description}")
            }
            
            println()
        }
    }
    
    return Pair(this.state.happiness, this.state.population)
}


class NoAI : AI {
    override val name = "No AI"
    override fun playTurn(gameState: GameState): List<TurnAction> = listOf()
}

open class SimpleAI1 : AI {
    override val name = "Simple AI v1"
    override fun playTurn(gameState: GameState): List<TurnAction> {
        val actions = mutableListOf<TurnAction>()

        // Try to build buildings that produce resources with high factors
        Buildings.ALL_BUILDINGS.forEach { building ->
            if (gameState.canAffordBuilding(building)) {
                // Calculate building value based on production and factors
                val value = calculateBuildingValue(gameState, building)
                
                if (value > 0) {
                    actions.add { state ->
                        state.build(building)
                    }
                }
            }
        }

        return actions
    }

    private fun calculateBuildingValue(state: GameState, building: Building): Float {
        var value = 0f

        // Add value for each resource produced based on factors and current production
        building.productions.forEach { (resource, amount) ->
            val currentProduction = when (resource) {
                Resource.WATER -> state.water
                Resource.FOOD -> state.food
                Resource.POWER -> state.power
                Resource.HEALTH -> state.health
                Resource.EDUCATION -> state.education
                Resource.HOUSES -> state.houses
                Resource.HOBBIES -> state.hobbies
                Resource.IRON -> state.iron
                Resource.COAL -> state.coal
                Resource.COPPER -> state.copper
                Resource.WOOD -> state.wood
                Resource.ROCK -> state.rock
                Resource.GLASS -> state.glass
                else -> 0
            }
            
            // Calculate need factor - higher value if current production is low
            val needFactor = 1.0f / (1.0f + currentProduction.toFloat() / state.population)
            
            value += when (resource) {
                Resource.WATER -> amount * state.waterFactor * needFactor
                Resource.FOOD -> amount * state.foodFactor * needFactor
                Resource.POWER -> amount * state.powerFactor * needFactor
                Resource.HEALTH -> amount * state.healthFactor * needFactor
                Resource.EDUCATION -> amount * state.educationFactor * needFactor
                Resource.HOUSES -> amount * state.housesFactor * needFactor
                Resource.HOBBIES -> amount * state.hobbiesFactor * needFactor
                else -> amount * 0.1f * needFactor // Small value for raw resources
            }
        }

        // Subtract value for resources consumed, considering current availability
        building.consumptions.forEach { (resource, amount) ->
            val currentAmount = when (resource) {
                Resource.WORKERS -> state.workers
                Resource.POWER -> state.power
                Resource.WATER -> state.water
                else -> 0
            }
            
            // Higher penalty if resource is scarce
            val scarcityFactor = if (currentAmount <= 0) 2.0f else 1.0f
            value -= amount * 0.2f * scarcityFactor
        }

        return value
    }
}

