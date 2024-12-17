package fr.xibalba.aj.civilization

sealed class GameEvent {
    abstract fun apply(state: GameState): GameState
    abstract val description: String
}

object Events {
    // Natural disasters
    class Earthquake : GameEvent() {
        override val description = "An earthquake has struck! Some buildings have been damaged."
        
        override fun apply(state: GameState): GameState {
            // Destroy ~20% of random buildings
            val buildingCount = state.buildings.values.sum()
            val buildingsToDestroy = (buildingCount * 0.2).toInt()
            val buildingsToKeep = state.buildings.filter { it.value > 0 }.toMutableMap()
            repeat(buildingsToDestroy) {
                val randomBuilding = buildingsToKeep.keys.random()
                buildingsToKeep[randomBuilding] = buildingsToKeep[randomBuilding]!! - 1
            }
            return state.copy(buildings = buildingsToKeep)
        }
    }

    class Drought : GameEvent() {
        override val description = "A severe drought has reduced water production!"
        
        override fun apply(state: GameState): GameState {
            // Store initial water production value
            val initialWaterProduction = Buildings.WATER_WELL.productions[Resource.WATER] ?: 0
            
            // Modify the template building
            Buildings.WATER_WELL.modifyProduction(Resource.WATER) { it / 2 }
            
            // Schedule recovery to exact initial value
            return state.scheduleAction(
                action = { 
                    Buildings.WATER_WELL.modifyProduction(Resource.WATER) { initialWaterProduction }
                    it
                },
                turnsDelay = 10
            )
        }
    }

    class Plague : GameEvent() {
        override val description = "A plague is spreading! Population and worker efficiency decreased."
        
        override fun apply(state: GameState): GameState {
            val newPopulation = (state.population * 0.8).toInt()
            
            // Modify all template buildings that use workers
            Buildings.ALL_BUILDINGS.forEach { building ->
                if (building.consumptions.containsKey(Resource.WORKERS)) {
                    building.modifyConsumption(Resource.WORKERS) { it * 2 }
                }
            }
            
            return state.copy(population = newPopulation)
                .scheduleAction(
                    action = { 
                        Buildings.ALL_BUILDINGS.forEach { building ->
                            if (building.consumptions.containsKey(Resource.WORKERS)) {
                                building.modifyConsumption(Resource.WORKERS) { it / 2 }
                            }
                        }
                        it
                    },
                    turnsDelay = 15
                )
        }
    }

    // Technical failures
    class PowerOutage : GameEvent() {
        override val description = "Power grid failure! Power production has ceased temporarily."
        
        override fun apply(state: GameState): GameState {
            // Store original power values
            val powerProducers = Buildings.ALL_BUILDINGS.filter { 
                it.productions.containsKey(Resource.POWER) 
            }
            val originalValues = powerProducers.associateWith { 
                it.productions[Resource.POWER] ?: 0 
            }
            
            // Disable power production
            powerProducers.forEach { building ->
                building.modifyProduction(Resource.POWER) { 0 }
            }
            
            return state.scheduleAction(
                action = { 
                    powerProducers.forEach { building ->
                        building.modifyProduction(Resource.POWER) { 
                            originalValues[building] ?: 0 
                        }
                    }
                    it
                },
                turnsDelay = 8
            )
        }
    }

    class MineCollapse : GameEvent() {
        override val description = "A mine has collapsed! Resource extraction is reduced."
        
        override fun apply(state: GameState): GameState {
            val mineTypes = listOf(Buildings.IRON_MINE, Buildings.COAL_MINE, Buildings.COPPER_MINE)
            val targetMine = mineTypes.random()
            
            // Remove one instance of the target mine
            return state.removeBuilding(targetMine)
        }
    }

    class EquipmentMalfunction : GameEvent() {
        override val description = "Equipment malfunction! Some factories are operating at reduced capacity."
        
        override fun apply(state: GameState): GameState {
            val factories = Buildings.ALL_BUILDINGS.filter { 
                it.name.contains("Factory") && Math.random() < 0.7 // 70% chance for each factory to be affected
            }
            
            // Store original production values
            val originalProductions = factories.associateWith { 
                it.productions.toMap() 
            }
            
            // Reduce all production by half
            factories.forEach { factory ->
                factory.productions = factory.productions.mapValues { it.value / 2 }
            }
            
            return state.scheduleAction(
                action = { 
                    factories.forEach { factory ->
                        factory.productions = originalProductions[factory] ?: factory.productions
                    }
                    it
                },
                turnsDelay = 5
            )
        }
    }

    // Social events
    class WorkerStrike : GameEvent() {
        override val description = "Workers are on strike! Production is affected across all sectors."
        
        override fun apply(state: GameState): GameState {
            // Store original worker consumption values
            val originalWorkerConsumptions = Buildings.ALL_BUILDINGS.associateWith { building ->
                building.consumptions[Resource.WORKERS] ?: 0
            }
            
            // Double worker consumption for all buildings
            Buildings.ALL_BUILDINGS.forEach { building ->
                building.modifyConsumption(Resource.WORKERS) { it * 2 }
            }
            
            return state.scheduleAction(
                action = {
                    Buildings.ALL_BUILDINGS.forEach { building ->
                        building.modifyConsumption(Resource.WORKERS) { originalWorkerConsumptions[building] ?: it }
                    }
                    it
                },
                turnsDelay = 5
            )
        }
    }

    class PopulationBoom : GameEvent() {
        override val description = "Population boom! More workers available but increased resource demand."
        
        override fun apply(state: GameState): GameState {
            // Increase population by 30%
            val newPopulation = (state.population * 1.3).toInt()
            return state.copy(population = newPopulation)
        }
    }

    private data class WeightedEvent(
        val event: () -> GameEvent,
        val weight: Int
    )

    private val ALL_EVENTS = listOf(
        WeightedEvent({ Earthquake() }, 10),
        WeightedEvent({ Drought() }, 15),
        WeightedEvent({ Plague() }, 5),
        WeightedEvent({ PowerOutage() }, 20),
        WeightedEvent({ MineCollapse() }, 10),
        WeightedEvent({ EquipmentMalfunction() }, 25),
        WeightedEvent({ WorkerStrike() }, 10),
        WeightedEvent({ PopulationBoom() }, 5)
    )

    fun randomEvent(): GameEvent {
        val totalWeight = ALL_EVENTS.sumOf { it.weight }
        var random = (0..totalWeight).random()
        
        for (weightedEvent in ALL_EVENTS) {
            random -= weightedEvent.weight
            if (random <= 0) {
                return weightedEvent.event()
            }
        }
        
        return ALL_EVENTS.first().event() // Fallback
    }
}

