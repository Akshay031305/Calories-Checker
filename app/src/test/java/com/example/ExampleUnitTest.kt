package com.example

import com.example.data.local.HealthEntryEntity
import com.example.data.model.HealthCalculators
import com.example.data.remote.ParsedHealthItem
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testDailySummaryCalculations() {
        val entries = listOf(
            HealthEntryEntity(
                id = "1",
                date = "2026-09-21",
                timestamp = "2026-09-21T08:00:00",
                name = "5 large eggs",
                type = "intake",
                calories = 360.0,
                proteinG = 31.0,
                carbsG = 2.0,
                fatG = 25.0
            ),
            HealthEntryEntity(
                id = "2",
                date = "2026-09-21",
                timestamp = "2026-09-21T08:15:00",
                name = "250ml toned milk",
                type = "intake",
                calories = 127.0,
                proteinG = 8.2,
                carbsG = 11.8,
                fatG = 5.0
            ),
            HealthEntryEntity(
                id = "3",
                date = "2026-09-21",
                timestamp = "2026-09-21T18:00:00",
                name = "Ran 5km in 35 minutes",
                type = "burn",
                calories = 368.0,
                proteinG = 0.0,
                carbsG = 0.0,
                fatG = 0.0
            )
        )

        val summary = HealthCalculators.calculateDailySummary("2026-09-21", entries)

        // Consumed: 360 + 127 = 487
        assertEquals(487.0, summary.consumedCalories, 0.01)
        // Burned: 368
        assertEquals(368.0, summary.burnedCalories, 0.01)
        // Net: 487 - 368 = 119
        assertEquals(119.0, summary.netCalories, 0.01)
        // Protein: 31 + 8.2 = 39.2
        assertEquals(39.2, summary.proteinG, 0.01)
        // Carbs: 2 + 11.8 = 13.8
        assertEquals(13.8, summary.carbsG, 0.01)
        // Fat: 25 + 5 = 30
        assertEquals(30.0, summary.fatG, 0.01)
        // Count: 3
        assertEquals(3, summary.entriesCount)
    }

    @Test
    fun testMonthlySummaryCalculations() {
        val entries = listOf(
            HealthEntryEntity(
                id = "1",
                date = "2026-09-20",
                timestamp = "2026-09-20T12:00:00",
                name = "Lunch",
                type = "intake",
                calories = 600.0,
                proteinG = 30.0,
                carbsG = 60.0,
                fatG = 20.0
            ),
            HealthEntryEntity(
                id = "2",
                date = "2026-09-21",
                timestamp = "2026-09-21T18:00:00",
                name = "Jogging",
                type = "burn",
                calories = 400.0,
                proteinG = 0.0,
                carbsG = 0.0,
                fatG = 0.0
            )
        )

        val monthly = HealthCalculators.calculateMonthlySummary("2026-09", entries)

        assertEquals(600.0, monthly.totalConsumed, 0.01)
        assertEquals(400.0, monthly.totalBurned, 0.01)
        assertEquals(2, monthly.daysLogged)
        assertTrue(monthly.monthTitle.contains("September"))
    }

    @Test
    fun testStructuredJsonValidation() {
        val jsonString = """
            {
              "entries": [
                {
                  "name": "3 large eggs",
                  "type": "intake",
                  "calories": 216,
                  "protein_g": 18.6,
                  "carbs_g": 1.2,
                  "fat_g": 15.0
                },
                {
                  "name": "2 small idlis",
                  "type": "intake",
                  "calories": 64,
                  "protein_g": 1.6,
                  "carbs_g": 13.8,
                  "fat_g": 0.2
                },
                {
                  "name": "Ran for 30 minutes",
                  "type": "burn",
                  "calories": 315,
                  "protein_g": 0,
                  "carbs_g": 0,
                  "fat_g": 0
                }
              ]
            }
        """.trimIndent()

        val root = JSONObject(jsonString)
        val entries = root.getJSONArray("entries")
        assertEquals(3, entries.length())

        val items = mutableListOf<ParsedHealthItem>()
        for (i in 0 until entries.length()) {
            val itemObj = entries.getJSONObject(i)
            val name = itemObj.getString("name")
            val type = itemObj.getString("type")
            val calories = itemObj.getDouble("calories")
            val proteinG = if (type == "burn") 0.0 else itemObj.getDouble("protein_g")
            val carbsG = if (type == "burn") 0.0 else itemObj.getDouble("carbs_g")
            val fatG = if (type == "burn") 0.0 else itemObj.getDouble("fat_g")

            items.add(
                ParsedHealthItem(
                    name = name,
                    type = type,
                    calories = calories,
                    proteinG = proteinG,
                    carbsG = carbsG,
                    fatG = fatG
                )
            )
        }

        assertEquals(3, items.size)
        assertEquals("intake", items[0].type)
        assertEquals(216.0, items[0].calories, 0.01)
        assertEquals("intake", items[1].type)
        assertEquals(64.0, items[1].calories, 0.01)
        assertEquals("burn", items[2].type)
        assertEquals(315.0, items[2].calories, 0.01)
        assertEquals(0.0, items[2].proteinG, 0.01)
    }
}
