package fr.xibalba.aj.civilization

data class Building(
    val name: String,
    var productions: Map<Resource, Int>,
    var consumptions: Map<Resource, Int>,
    val buildCost: Map<Resource, Int>
) {
    fun useOf(resource: Resource): Int {
        val produced = productions[resource] ?: 0
        val consumed = consumptions[resource] ?: 0
        return produced - consumed
    }

    // Helper functions to modify and restore production/consumption
    fun modifyProduction(resource: Resource, modifier: (Int) -> Int) {
        productions = productions.toMutableMap().apply {
            compute(resource) { _, value -> modifier(value ?: 0) }
        }
    }

    fun modifyConsumption(resource: Resource, modifier: (Int) -> Int) {
        consumptions = consumptions.toMutableMap().apply {
            compute(resource) { _, value -> modifier(value ?: 0) }
        }
    }
}

object Buildings {
    // Basic resource production
    val WATER_WELL = Building(
        name = "Water Well",
        productions = mapOf(Resource.WATER to 10),
        consumptions = mapOf(Resource.WORKERS to 1),
        buildCost = mapOf(Resource.WOOD to 10, Resource.ROCK to 20)
    )

    val FARM = Building(
        name = "Farm",
        productions = mapOf(Resource.FOOD to 8),
        consumptions = mapOf(
            Resource.WATER to 2,
            Resource.WORKERS to 2
        ),
        buildCost = mapOf(Resource.WOOD to 15, Resource.ROCK to 10)
    )

    val SOLAR_PANEL = Building(
        name = "Solar Panel",
        productions = mapOf(Resource.POWER to 5),
        consumptions = mapOf(Resource.WORKERS to 1),
        buildCost = mapOf(Resource.GLASS to 10, Resource.COPPER to 5)
    )

    // Raw resource extraction
    val IRON_MINE = Building(
        name = "Iron Mine",
        productions = mapOf(Resource.IRON to 3),
        consumptions = mapOf(
            Resource.WORKERS to 3,
            Resource.POWER to 2
        ),
        buildCost = mapOf(Resource.WOOD to 20, Resource.ROCK to 30)
    )

    val COAL_MINE = Building(
        name = "Coal Mine",
        productions = mapOf(Resource.COAL to 4),
        consumptions = mapOf(
            Resource.WORKERS to 3,
            Resource.POWER to 2
        ),
        buildCost = mapOf(Resource.WOOD to 20, Resource.ROCK to 30)
    )

    val COPPER_MINE = Building(
        name = "Copper Mine",
        productions = mapOf(Resource.COPPER to 3),
        consumptions = mapOf(
            Resource.WORKERS to 3,
            Resource.POWER to 2
        ),
        buildCost = mapOf(Resource.WOOD to 20, Resource.ROCK to 30)
    )

    val LUMBER_MILL = Building(
        name = "Lumber Mill",
        productions = mapOf(Resource.WOOD to 5),
        consumptions = mapOf(Resource.WORKERS to 2),
        buildCost = mapOf(Resource.IRON to 10, Resource.ROCK to 15)
    )

    val QUARRY = Building(
        name = "Quarry",
        productions = mapOf(Resource.ROCK to 6),
        consumptions = mapOf(Resource.WORKERS to 2),
        buildCost = mapOf(Resource.IRON to 15, Resource.WOOD to 10)
    )

    val GLASS_FACTORY = Building(
        name = "Glass Factory",
        productions = mapOf(Resource.GLASS to 3),
        consumptions = mapOf(
            Resource.WORKERS to 2,
            Resource.POWER to 3,
            Resource.COAL to 1
        ),
        buildCost = mapOf(
            Resource.IRON to 20,
            Resource.ROCK to 30,
            Resource.COPPER to 10
        )
    )

    // Services
    val HOSPITAL = Building(
        name = "Hospital",
        productions = mapOf(Resource.HEALTH to 10),
        consumptions = mapOf(
            Resource.WORKERS to 5,
            Resource.POWER to 3,
            Resource.WATER to 2
        ),
        buildCost = mapOf(
            Resource.IRON to 30,
            Resource.GLASS to 20,
            Resource.ROCK to 40
        )
    )

    val SCHOOL = Building(
        name = "School",
        productions = mapOf(Resource.EDUCATION to 8),
        consumptions = mapOf(
            Resource.WORKERS to 3,
            Resource.POWER to 1,
            Resource.WATER to 1
        ),
        buildCost = mapOf(
            Resource.WOOD to 30,
            Resource.GLASS to 15,
            Resource.ROCK to 25
        )
    )

    val HOUSE = Building(
        name = "House",
        productions = mapOf(Resource.HOUSES to 4),
        consumptions = mapOf(
            Resource.POWER to 1,
            Resource.WATER to 1
        ),
        buildCost = mapOf(
            Resource.WOOD to 20,
            Resource.ROCK to 15,
            Resource.GLASS to 5
        )
    )

    val PARK = Building(
        name = "Park",
        productions = mapOf(Resource.HOBBIES to 5),
        consumptions = mapOf(
            Resource.WORKERS to 1,
            Resource.WATER to 2
        ),
        buildCost = mapOf(
            Resource.WOOD to 15,
            Resource.ROCK to 10
        )
    )

    val ALL_BUILDINGS = listOf(
        // Basic needs (survival)
        WATER_WELL,
        FARM,
        HOUSE,
        
        // Infrastructure
        SOLAR_PANEL,
        
        // Essential services
        HOSPITAL,
        SCHOOL,
        
        // Quality of life
        PARK,
        
        // Raw resource production
        IRON_MINE,
        COAL_MINE, 
        COPPER_MINE,
        LUMBER_MILL,
        QUARRY,
        
        // Manufacturing
        GLASS_FACTORY
    )
}

