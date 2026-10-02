package com.nothatcher.sproutbook.core

/** Short, offline articles. Medical education links to the primary guidance reviewed 2026-10-01. */
internal object MoreAdvice {
    private const val positions = "https://www.nhs.uk/best-start-in-life/baby/feeding-your-baby/breastfeeding/how-to-breastfeed/breastfeeding-positions/"
    private const val latch = "https://www.nhs.uk/baby/breastfeeding-and-bottle-feeding/breastfeeding-problems/sore-nipples/"
    private const val pump = "https://www.cdc.gov/hygiene/about/about-breast-pump-hygiene.html"
    private const val work = "https://www.nhs.uk/baby/breastfeeding-and-bottle-feeding/breastfeeding-and-lifestyle/back-to-work/"
    private const val teeth = "https://caringforkids.cps.ca/handouts/healthy-living/healthy_teeth_for_children"
    private const val crying = "https://caringforkids.cps.ca/handouts/pregnancy-and-babies/colic_and_crying"
    val articles = listOf(
        AdviceArticle("Mom", "Breastfeeding", "Cradle position",
            "Sit supported and bring baby across your lap, facing you. Their head rests on your forearm and your hand supports their body. Keep ear, shoulder and hip aligned, with their nose near your nipple.", positions),
        AdviceArticle("Mom", "Breastfeeding", "Football / rugby position",
            "Place baby beside you under the arm on the feeding side. Support their neck and shoulders, with their nose level with your nipple. A cushion can support your arm. Ask for help adapting the hold if you are recovering from surgery.", positions),
        AdviceArticle("Mom", "Breastfeeding", "Laid-back position",
            "Recline comfortably with your back and shoulders supported. Bring baby tummy-to-tummy and support them as they approach the breast. Keep their face visible and airway clear. Stay awake throughout feeding.", positions),
        AdviceArticle("Mom", "Breastfeeding", "Side-lying position",
            "Lie facing baby, with their body aligned rather than twisted. Keep pillows away from their face. Stay awake; after the feed, return baby to their own firm, flat, clear sleep space.", positions),
        AdviceArticle("Mom", "Breastfeeding", "When latch pain persists",
            "Pain at every feed, cracks or bleeding deserve early support. Ask a midwife or lactation professional to observe attachment and positioning. You do not have to push through pain or diagnose its cause yourself.", latch),
        AdviceArticle("Mom", "Pumping", "Cleaning pump parts",
            "Wash your hands before assembling clean parts. After every use, separate and clean parts that contact milk, following the manufacturer's instructions. Allow them to air-dry fully before protected storage.\n\nAsk your care team about extra precautions for a premature, very young or unwell baby. Refrigerating used parts does not replace cleaning.", pump),
        AdviceArticle("Mom", "Work", "A pumping bag for work",
            "Write a short checklist: pump, clean parts, containers, labels and the power supply. Discuss a private space and breaks with your employer or tutor. Agree how milk will be transported and stored using current local guidance.", work),
        AdviceArticle("Mom", "Work", "A trial caregiver day",
            "Practise the childcare routine before your first workday. Label and date expressed milk. Agree who will share feeding updates and who to contact if there are concerns. Leave time to change the plan after the trial.", work),
        AdviceArticle("Mom", "Organization", "Visitors who actually help",
            "Choose a visiting window that fits your rest. Name one practical task: bring a meal, put on laundry or collect supplies. A short visit can be enough. Write your preferences down so another adult can coordinate them."),
        AdviceArticle("Mom", "Newborn basics", "The first little tooth",
            "Record what you notice in Little teeth. Teeth appear at different times. Once a tooth appears, use a soft baby toothbrush and ask a dental professional about suitable care. Do not leave baby in bed with a bottle.", teeth),
        AdviceArticle("Mom", "Organization", "A school-day landing zone",
            "Keep bags, activity equipment and tomorrow's essentials in one place. Check the shared schedule together. Let your child choose one task they can own, then make the routine easy to repeat."),
        AdviceArticle("Mom", "Self-care", "Listening to an older child",
            "Choose a quiet moment and give them room to speak. Ask whether they want listening, help deciding or practical action. Agree the next step together. Keep important health or school questions in the schedule notes."),
        AdviceArticle("Dad", "Household", "Caregiver handover",
            "Share the last feed, last sleep, next planned activity and any important notes. Show the Emergency card and care contacts. Agree how to reach you and when another adult will take over. Use Grandparent mode when read-only access is helpful."),
        AdviceArticle("Dad", "Feeding", "Taking over after a feed",
            "If wanted, take the next nappy change or settling turn. For burping, support baby's head and neck while holding them upright and gently rubbing their back. Follow fullness cues rather than aiming to empty a bottle.", "https://www.nhs.uk/baby/breastfeeding-and-bottle-feeding/bottle-feeding/advice/"),
        AdviceArticle("Dad", "Crying", "When crying overwhelms you",
            "Put baby safely in their cot. Step away briefly to steady yourself and ask another trusted adult to take over. Never shake a baby. If you fear anyone may be harmed, contact emergency services. Ask a clinician about prolonged crying or an unwell baby.", crying),
        AdviceArticle("Dad", "Rest", "When holding baby feels too sleepy",
            "Move baby to their own firm, flat, clear sleep space, on their back. Do not fall asleep holding them on a sofa or chair. Ask another adult to take over so you can rest.", "https://www.canada.ca/en/health-canada/services/safe-sleep/safe-sleep-tips.html"),
        AdviceArticle("Dad", "Everyday care", "Changing on a floor mat",
            "Gather supplies first and wash your hands. A changing mat on the floor avoids a fall from a raised surface. Clean gently from front to back and dry the skin. Stay with baby throughout the change.", "https://www.nhs.uk/baby/caring-for-a-newborn/how-to-change-your-babys-nappy/"),
        AdviceArticle("Dad", "Household", "Make an offer specific",
            "Try: 'I can handle dinner and the dishes tonight' or 'I'll restock nappies before the weekend.' Own the planning and completion too. Check whether the offer helps, then follow through without needing repeated reminders."),
        AdviceArticle("Dad", "Everyday care", "Plan the first dental visit",
            "Ask your dental team about a first visit within six months of the first tooth, or by the first birthday. Bring tooth dates and any questions. The tracker is a journal, not an assessment of whether development is normal.", teeth),
        AdviceArticle("Dad", "Support", "Prepare for a health visit",
            "Keep a few questions in the appointment notes. Bring relevant records and the current medicine list. Ask the clinician to explain the next step in plain language. Afterwards, save the plan and follow-up date so every caregiver has the same information."),
    )
}
