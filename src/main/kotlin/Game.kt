package fr.xibalba.aj.civilization

import kotlin.collections.map
import kotlin.math.abs
import kotlin.math.sin

const val MIN_WATER_FACTOR = 0.2f
const val MIN_FOOD_FACTOR = 0.2f 
const val MIN_POWER_FACTOR = 0.05f
const val MIN_HEALTH_FACTOR = 0.0f
const val MIN_EDUCATION_FACTOR = 0.0f
const val MIN_HOUSES_FACTOR = 0.05f
const val MIN_HOBBIES_FACTOR = 0.0f

data class ScheduledAction(
    val action: TurnAction,
    var turnsLeft: Int
)

class GameEngine {

    var state = GameState(
        turn = 0,
        buildings = mapOf(),
        population = 10,
        waterFactor = 0.5f,
        foodFactor = 0.4f,
        powerFactor = 0.0f,
        healthFactor = 0.0f,
        educationFactor = 0.0f,
        housesFactor = 0.1f,
        hobbiesFactor = 0.0f,
        ironStock = 100,
        coalStock = 100,
        copperStock = 100,
        woodStock = 100,
        rockStock = 100,
        glassStock = 100,
        scheduledActions = listOf()
    )

    fun tick(turnActions: List<TurnAction>): GameEvent? {
        state = populationGrowth(state)
        state = updateFactors(state)
        state = updateStockResources(state)
        
        var event: GameEvent? = null
        
        // Randomly trigger events (5% chance per turn)
        if (Math.random() < 0.05) {
            event = Events.randomEvent()
            state = event.apply(state)
        }
        
        // Process scheduled actions
        state = processScheduledActions(state)
        
        state = state.copy(turn = state.turn + 1)
        state = turnActions.fold(state) { acc, turnAction -> turnAction(acc) }
        return event
    }

    fun populationGrowth(state: GameState): GameState {
        // Population growth is based on happiness (0-1) and available food per person
        val foodPerPerson = state.food.toFloat() / state.population.toFloat()
        val growthRate = state.happiness * foodPerPerson * 0.1f // 10% max growth rate
        
        // Calculate new population with growth
        val populationIncrease = (state.population.toFloat() * growthRate).toInt()
        
        return state.copy(
            population = state.population + populationIncrease
        )
    }

    private fun updateFactors(state: GameState): GameState {
        // Different oscillation periods (in turns) for each factor
        val waterPeriod = 50.0
        val foodPeriod = 70.0
        val powerPeriod = 40.0
        val healthPeriod = 60.0
        val educationPeriod = 80.0
        val housesPeriod = 45.0
        val hobbiesPeriod = 65.0

        // Maximum factor change amplitude
        val amplitude = 0.2f

        // Calculate oscillating values for each factor based on turn with different periods
        val waterRadians = (state.turn.toDouble() * Math.TAU / waterPeriod)
        val foodRadians = (state.turn.toDouble() * Math.TAU / foodPeriod)
        val powerRadians = (state.turn.toDouble() * Math.TAU / powerPeriod)
        val healthRadians = (state.turn.toDouble() * Math.TAU / healthPeriod)
        val educationRadians = (state.turn.toDouble() * Math.TAU / educationPeriod)
        val housesRadians = (state.turn.toDouble() * Math.TAU / housesPeriod)
        val hobbiesRadians = (state.turn.toDouble() * Math.TAU / hobbiesPeriod)
        
        // Generate raw oscillating values centered around initial values
        val rawWater = state.waterFactor + (amplitude * sin(waterRadians)).toFloat()
        val rawFood = state.foodFactor + (amplitude * sin(foodRadians)).toFloat()
        val rawPower = state.powerFactor + (amplitude * sin(powerRadians)).toFloat()
        val rawHealth = state.healthFactor + (amplitude * sin(healthRadians)).toFloat()
        val rawEducation = state.educationFactor + (amplitude * sin(educationRadians)).toFloat()
        val rawHouses = state.housesFactor + (amplitude * sin(housesRadians)).toFloat()
        val rawHobbies = state.hobbiesFactor + (amplitude * sin(hobbiesRadians)).toFloat()

        // Clamp to minimum values
        val clampedWater = maxOf(MIN_WATER_FACTOR, rawWater)
        val clampedFood = maxOf(MIN_FOOD_FACTOR, rawFood)
        val clampedPower = maxOf(MIN_POWER_FACTOR, rawPower)
        val clampedHealth = maxOf(MIN_HEALTH_FACTOR, rawHealth)
        val clampedEducation = maxOf(MIN_EDUCATION_FACTOR, rawEducation)
        val clampedHouses = maxOf(MIN_HOUSES_FACTOR, rawHouses)
        val clampedHobbies = maxOf(MIN_HOBBIES_FACTOR, rawHobbies)

        // Normalize to ensure sum equals 1
        val sum = clampedWater + clampedFood + clampedPower + clampedHealth + 
                 clampedEducation + clampedHouses + clampedHobbies
        
        return state.copy(
            waterFactor = clampedWater / sum,
            foodFactor = clampedFood / sum,
            powerFactor = clampedPower / sum,
            healthFactor = clampedHealth / sum,
            educationFactor = clampedEducation / sum,
            housesFactor = clampedHouses / sum,
            hobbiesFactor = clampedHobbies / sum
        )
    }

    private fun updateStockResources(state: GameState): GameState {
        return state.copy(
            ironStock = maxOf(0, state.ironStock + state.iron),
            coalStock = maxOf(0, state.coalStock + state.coal),
            copperStock = maxOf(0, state.copperStock + state.copper),
            woodStock = maxOf(0, state.woodStock + state.wood),
            rockStock = maxOf(0, state.rockStock + state.rock),
            glassStock = maxOf(0, state.glassStock + state.glass)
        )
    }

    private fun processScheduledActions(state: GameState): GameState {
        // Get actions ready to execute (turnsLeft == 0)
        val (readyActions, remainingActions) = state.scheduledActions.partition { it.turnsLeft <= 0 }
        
        // Update turns left for remaining actions
        val updatedRemainingActions = remainingActions.map { 
            it.copy(turnsLeft = it.turnsLeft - 1) 
        }
        
        // Execute ready actions
        var newState = state
        readyActions.forEach { scheduled ->
            newState = scheduled.action(newState)
        }
        
        // Update state with remaining actions
        return newState.copy(scheduledActions = updatedRemainingActions)
    }
}

typealias TurnAction = (GameState) -> GameState

data class GameState(
    val turn: Int,
    val buildings: Map<Building, Int>,
    val population: Int,
    
    // Factors : how much each resource is important for the population, sum must equal 1
    val waterFactor: Float,
    val foodFactor: Float,
    val powerFactor: Float,
    val healthFactor: Float,
    val educationFactor: Float,
    val housesFactor: Float,
    val hobbiesFactor: Float,

    // Stock resources
    val ironStock: Int,
    val coalStock: Int,
    val copperStock: Int,
    val woodStock: Int,
    val rockStock: Int,
    val glassStock: Int,

    // Scheduled actions
    val scheduledActions: List<ScheduledAction> = listOf()
) {
    init {
        require(abs(waterFactor + foodFactor + powerFactor + healthFactor + educationFactor + housesFactor + hobbiesFactor - 1f) <= 0.01f) {
            "Sum of factors must equal 1"
        }
    }

    // Helper function to calculate total resource change from all buildings
    private fun totalResourceChange(resource: Resource): Int {
        return buildings.entries.sumOf { (building, count) ->
            building.useOf(resource) * count
        }
    }

    // Passive resources
    val water = totalResourceChange(Resource.WATER)
    val food = totalResourceChange(Resource.FOOD)
    val power = totalResourceChange(Resource.POWER)

    // Stock resources
    val iron = totalResourceChange(Resource.IRON)
    val coal = totalResourceChange(Resource.COAL)
    val copper = totalResourceChange(Resource.COPPER)
    val wood = totalResourceChange(Resource.WOOD)
    val rock = totalResourceChange(Resource.ROCK)
    val glass = totalResourceChange(Resource.GLASS)

    // Workers
    val workers = totalResourceChange(Resource.WORKERS)

    // Services
    val health = totalResourceChange(Resource.HEALTH)
    val education = totalResourceChange(Resource.EDUCATION)
    val houses = totalResourceChange(Resource.HOUSES)
    val hobbies = totalResourceChange(Resource.HOBBIES)

    val happiness = (water * waterFactor + food * foodFactor + power * powerFactor + 
                    health * healthFactor + education * educationFactor + 
                    houses * housesFactor + hobbies * hobbiesFactor) / population.toFloat()
}

enum class Resource {
    WATER,
    FOOD,
    POWER,
    IRON,
    COAL,
    COPPER,
    WOOD,
    ROCK,
    GLASS,
    WORKERS,
    HEALTH,
    EDUCATION,
    HOUSES,
    HOBBIES,
}

// Helper extension function to schedule actions
fun GameState.scheduleAction(action: TurnAction, turnsDelay: Int): GameState {
    val newAction = ScheduledAction(action, turnsDelay)
    return this.copy(
        scheduledActions = this.scheduledActions + newAction
    )
}

// Helper extension functions for building management
fun GameState.addBuilding(building: Building): GameState {
    val currentCount = buildings[building] ?: 0
    return copy(buildings = buildings + (building to currentCount + 1))
}

fun GameState.removeBuilding(building: Building): GameState {
    val currentCount = buildings[building] ?: return this
    return if (currentCount <= 1) {
        copy(buildings = buildings - building)
    } else {
        copy(buildings = buildings + (building to currentCount - 1))
    }
}

fun GameState.canAffordBuilding(building: Building): Boolean {
    return building.buildCost.all { (resource, cost) ->
        when (resource) {
            Resource.IRON -> ironStock >= cost
            Resource.COAL -> coalStock >= cost
            Resource.COPPER -> copperStock >= cost
            Resource.WOOD -> woodStock >= cost
            Resource.ROCK -> rockStock >= cost
            Resource.GLASS -> glassStock >= cost
            else -> true
        }
    }
}

fun GameState.build(building: Building): GameState {
    // Check if we have enough resources to build
    if (!canAffordBuilding(building)) {
        return this
    }

    // Consume build costs
    return copy(
        ironStock = ironStock - (building.buildCost[Resource.IRON] ?: 0),
        coalStock = coalStock - (building.buildCost[Resource.COAL] ?: 0), 
        copperStock = copperStock - (building.buildCost[Resource.COPPER] ?: 0),
        woodStock = woodStock - (building.buildCost[Resource.WOOD] ?: 0),
        rockStock = rockStock - (building.buildCost[Resource.ROCK] ?: 0),
        glassStock = glassStock - (building.buildCost[Resource.GLASS] ?: 0)
    ).addBuilding(building)
}

fun GameState.demolish(building: Building): GameState {
    return removeBuilding(building)
}