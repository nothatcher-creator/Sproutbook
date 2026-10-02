package com.nothatcher.sproutbook.features.solids

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.nothatcher.sproutbook.ui.*

@Composable
fun SolidsGuide() {
    var stage by remember { mutableStateOf("Around 6 months") }
    Page("Ready for little tastes", "Readiness varies; your child's care team can help") {
        item {
            Choices(listOf("Around 6 months", "6–9 months", "9–12 months", "12+ months"), stage) {
                stage = it
            }
            Panel {
                Section(stage)
                Text(
                    when (stage) {
                        "Around 6 months" ->
                            "Look for steady head control, sitting upright, bringing food to the mouth and swallowing it. Interest alone is not enough. Begin with small tastes of iron-rich foods, such as mashed lentils or iron-fortified cereal."
                        "6–9 months" ->
                            "Explore mashed and soft lumpy textures and soft finger foods as skills develop. Breast milk or infant formula remains important. Let your baby stop when full."
                        "9–12 months" ->
                            "Build variety with soft chopped family foods and supervised practice with a cup or spoon. Adapt texture and size to your baby's skills."
                        else ->
                            "Share regular family meals and snacks with safe textures. Keep offering variety without pressure; appetite changes from day to day."
                    }
                )
            }
        }
        item {
            Panel {
                Section("Allergens, thoughtfully")
                Text(
                    "Introduce common allergens in safe forms when solids begin. Record each introduction and any symptoms. If your child has severe eczema, an existing allergy or a previous reaction, ask their clinician for an individual plan. A tolerated food can stay part of their usual varied diet."
                )
                Muted("Honey waits until after 12 months, including honey mixed into food.")
                SourceLink(
                    "Canadian Paediatric Society · Feeding",
                    "https://caringforkids.cps.ca/handouts/healthy-living/feeding_your_baby_in_the_first_year",
                )
            }
        }
        item {
            Panel {
                Section("Prepare for safe eating")
                Text(
                    "Stay close and watch while your child sits upright to eat. Cook hard foods until soft; remove bones, stones and tough skins. Quarter grapes lengthways and make pieces suitable for your child's skills. Thin smooth nut butter; never offer thick spoonfuls."
                )
                Text(
                    "Avoid whole nuts, popcorn, whole grapes, hard raw chunks, hard sweets and round sausage slices for babies and young children."
                )
                SourceLink(
                    "NHS · Food preparation",
                    "https://www.nhs.uk/best-start-in-life/baby/weaning/safe-weaning/preparing-food-safely/",
                )
            }
        }
        item {
            Panel {
                Section("Gagging or choking?")
                Text(
                    "Gagging is a protective reflex and is usually noisy; a baby may cough or retch while still breathing. Choking may be quiet, with an ineffective cough or inability to breathe or cry. Colour change is a late sign—do not wait for it."
                )
                Text(
                    "If breathing is blocked, contact emergency services immediately and follow age-appropriate choking first aid and the dispatcher's directions. Never blindly sweep inside the mouth.",
                    color = MaterialTheme.colorScheme.error,
                )
                SourceLink(
                    "NHS · Choking and gagging",
                    "https://www.nhs.uk/best-start-in-life/baby/weaning/safe-weaning/choking-and-gagging-on-food/",
                )
            }
        }
    }
}
