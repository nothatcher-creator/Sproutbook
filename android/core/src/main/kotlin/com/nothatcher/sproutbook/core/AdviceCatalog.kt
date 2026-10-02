package com.nothatcher.sproutbook.core
data class HelpTopic(
    val title: String,
    val steps: List<String>,
    val detail: String,
    val source: String,
    val emergency: Boolean = false,
)

data class AdviceArticle(
    val audience: String,
    val category: String,
    val title: String,
    val body: String,
    val source: String = "",
)

object AdviceCatalog {
    val help =
        listOf(
            HelpTopic(
                "Choking / trouble breathing",
                listOf(
                    "Contact emergency services now; put the phone on speaker.",
                    "If coughing strongly, encourage coughing and stay close. If unable to breathe, cry or cough effectively, act immediately.",
                    "Follow the dispatcher and age-appropriate first aid. If unresponsive, start CPR as directed.",
                ),
                "For a conscious choking baby under 1 year, use up to 5 back blows, then up to 5 chest thrusts, checking for relief each time. For a child over 1 year, use back blows and abdominal thrusts. Repeat if needed while help is coming. Never use abdominal thrusts on an infant or sweep blindly inside the mouth. Get medical care after thrusts, even if the object clears. This brief guide does not replace hands-on first-aid training.",
                "https://www.nhs.uk/baby/first-aid-and-safety/first-aid/how-to-stop-a-child-from-choking/",
                true,
            ),
            HelpTopic(
                "Baby won't stop crying",
                listOf(
                    "Check feeding cues, nappy and temperature; look for signs of illness.",
                    "Try holding close, gentle rocking and a quieter room.",
                    "If you are getting frustrated, put baby safely down and get support.",
                ),
                "Some babies cry for long periods even with attentive care. Seek medical advice for unusual, persistent crying, poor feeding or an unwell baby. If there is difficulty breathing or the baby is hard to wake, seek emergency care.",
                "https://caringforkids.cps.ca/handouts/pregnancy-and-babies/colic_and_crying",
                false,
            ),
            HelpTopic(
                "Baby won't latch",
                listOf(
                    "Pause and soothe; try skin-to-skin while you are awake.",
                    "Support baby close, nose near the nipple; wait for a wide mouth.",
                    "Ask a lactation professional to observe a feed if difficulty continues.",
                ),
                "Persistent pain, poor milk transfer, fewer wet nappies or growth concerns need prompt professional support. If baby is too sleepy to feed or appears unwell, seek medical care.",
                "https://www.nhs.uk/baby/breastfeeding-and-bottle-feeding/breastfeeding/positioning-and-attachment/",
                false,
            ),
            HelpTopic(
                "Gas / spit-up",
                listOf(
                    "Pause the feed and hold baby upright while awake.",
                    "Try gentle burping and follow their fullness cues.",
                    "Check for concerning symptoms and ask for advice if worried.",
                ),
                "Seek urgent care for green or bloody vomit, forceful repeated vomiting, a swollen painful abdomen, dehydration or a very unwell baby. Always return baby to a flat, firm sleep surface on their back; do not prop up the cot.",
                "https://www.nhs.uk/conditions/reflux-in-babies/",
                false,
            ),
            HelpTopic(
                "Fever / seems unwell",
                listOf(
                    "Check responsiveness and breathing; emergency signs need emergency help.",
                    "Use a digital thermometer as directed and note the reading and method.",
                    "A baby under 3 months with a temperature of 38°C or higher needs urgent assessment.",
                ),
                "Babies under 6 months with fever should see a clinician. Seek urgent help at any age for trouble breathing, difficulty waking, a seizure, a concerning rash or dehydration. Appearance matters even without fever. Offer usual milk feeds or suitable fluids; ask a clinician or pharmacist about medicines rather than guessing doses.",
                "https://caringforkids.cps.ca/handouts/health-conditions-and-treatments/fever_and_temperature_taking",
                false,
            ),
            HelpTopic(
                "I'm overwhelmed",
                listOf(
                    "Place baby on their back in a safe, empty cot.",
                    "Step away briefly to breathe and steady yourself.",
                    "Ask another trusted adult to take over or call for support.",
                ),
                "Never shake or hit a baby. It is okay to take a short break while baby is safe. If you fear you may harm yourself or someone else, contact emergency services now. Tell a clinician if distress keeps returning; you deserve support.",
                "https://www.albertahealthservices.ca/injprev/Page4845.aspx",
                false,
            ),
            HelpTopic(
                "Won't sleep",
                listOf(
                    "Check hunger, nappy and comfort.",
                    "Keep the room calm and use a simple, repeatable settling routine.",
                    "Use a firm, flat, clear sleep space; place baby on their back.",
                ),
                "If you feel yourself falling asleep while holding baby, move them to their safe sleep space. Avoid sleeping with baby on a sofa or chair. Ask your care team about persistent feeding, breathing or sleep concerns.",
                "https://www.canada.ca/en/health-canada/services/safe-sleep/safe-sleep-tips.html",
                false,
            ),
            HelpTopic(
                "Won't take bottle",
                listOf(
                    "Offer when calm and showing early feeding cues.",
                    "Hold baby close and semi-upright; let them accept the teat.",
                    "Pause when they need a break; never force the bottle.",
                ),
                "Try again after a calm break. Never prop a bottle or leave baby feeding alone. Repeated refusal, fewer wet nappies or unusual sleepiness needs prompt clinical advice.",
                "https://www.nhs.uk/baby/breastfeeding-and-bottle-feeding/bottle-feeding/advice/",
                false,
            ),
            HelpTopic(
                "Toddler meltdown",
                listOf(
                    "Make the space safe and stay nearby.",
                    "Use a calm voice and a few words to name the feeling.",
                    "Keep a simple limit; reconnect when they settle.",
                ),
                "Hunger, tiredness and change can make emotions harder. Offer predictable routines and small choices at calmer times. Avoid hitting or shouting. Ask for support if you are worried about behaviour or your ability to cope.",
                "https://www.nhs.uk/baby/babys-development/behaviour/temper-tantrums/",
                false,
            ),
        )
    val articles =
        listOf(
            AdviceArticle(
                "Mom",
                "Breastfeeding",
                "A comfortable, deep latch",
                "Support your back and arms. Keep baby close, head and body aligned, with their nose near your nipple. Wait for a wide mouth, then bring baby in chin first. Let their head tip back; avoid pushing the back of the head. Rounded cheeks and audible swallowing are encouraging signs. A feed should not remain painful.",
                "https://www.nhs.uk/baby/breastfeeding-and-bottle-feeding/breastfeeding/positioning-and-attachment/",
            ),
            AdviceArticle(
                "Mom",
                "Breastfeeding",
                "Shallow latch, suction and comfort",
                "Pinching pain, a compressed nipple after feeding, or baby repeatedly slipping off can suggest attachment needs help. Slide a clean finger gently into the corner of the mouth to release suction before trying again. Do not pull baby off while suction holds. Persistent pain, damaged nipples, poor milk transfer, low wet nappies or growth concerns should be discussed with a clinician or lactation professional.",
                "https://www.nhs.uk/baby/breastfeeding-and-bottle-feeding/breastfeeding-problems/sore-nipples/",
            ),
            AdviceArticle(
                "Mom",
                "Breastfeeding",
                "Five positions to try",
                "Cross-cradle: support baby with the arm opposite the feeding breast, with a hand supporting neck and shoulders. Cradle: baby lies across your front, head resting on your forearm. Football/rugby: tuck baby beside you under your arm, feet pointing behind. Laid-back: recline comfortably and support baby tummy-to-tummy, keeping their airway clear. Side-lying: lie facing each other with baby aligned and their face clear. Stay awake; after feeding, return baby to their own safe sleep space. A lactation professional can help adapt positions after surgery or for twins.",
                "https://www.nhs.uk/best-start-in-life/baby/feeding-your-baby/breastfeeding/how-to-breastfeed/breastfeeding-positions/",
            ),
            AdviceArticle(
                "Mom",
                "Pumping",
                "A gentler pumping routine",
                "Wash hands and follow your pump instructions for assembly and cleaning. Centre the nipple in a comfortable flange and start with low suction. Pumping should not hurt; ask for help with fit or persistent discomfort. Log both sides in Feeding. For storage, use current local guidance and the product instructions.",
                "https://www.nhs.uk/best-start-in-life/baby/feeding-your-baby/breastfeeding/expressing-your-breast-milk/expressing-breast-milk-with-a-pump/",
            ),
            AdviceArticle(
                "Mom",
                "Formula",
                "Responsive bottle feeding",
                "Hold baby close, use a paced approach with pauses and watch fullness cues. Do not force them to finish. Follow the formula label and local health guidance for exact preparation; the planning calculator estimates supplies, not powder mixing or a feeding prescription.",
                "https://www.nhs.uk/baby/breastfeeding-and-bottle-feeding/bottle-feeding/advice/",
            ),
            AdviceArticle(
                "Mom",
                "Organization",
                "A small postpartum care station",
                "Keep water, a snack, nappies, wipes, a charger and care contacts within reach. Choose one person to coordinate meals or errands. Put appointment questions into Schedule as they arise; you do not need to remember everything.",
                "",
            ),
            AdviceArticle(
                "Mom",
                "Rest",
                "Getting through sleep deprivation",
                "Agree on a protected rest period with another adult when possible. Reduce nonessential tasks and accept practical help. If feeding plans allow, decide in advance who handles settling, nappies and cleaning. Avoid driving when dangerously tired.",
                "",
            ),
            AdviceArticle(
                "Mom",
                "Crying",
                "When soothing takes time",
                "Try feeding, a nappy check, a gentle cuddle and a quiet room. Some crying continues despite your efforts. If frustration rises, put baby safely in their cot, step away briefly and ask for help. Never shake a baby.",
                "https://caringforkids.cps.ca/handouts/pregnancy-and-babies/colic_and_crying",
            ),
            AdviceArticle(
                "Mom",
                "Feeding support",
                "Bring useful questions to a feed review",
                "Write down what feels difficult, how feeds are going and any wet-nappy or growth concerns. Ask someone qualified to watch a full feed. You can bring feeding records without treating every number as a target.",
                "https://www.nhs.uk/baby/breastfeeding-and-bottle-feeding/breastfeeding/positioning-and-attachment/",
            ),
            AdviceArticle(
                "Mom",
                "Work",
                "Planning a return to work",
                "Discuss break times and a private pumping space if needed. Try the care routine with your caregiver before the first day. Agree how they will log feeds and communicate concerns. Keep a written packing list so mornings require fewer decisions.",
                "",
            ),
            AdviceArticle(
                "Mom",
                "Self-care",
                "Support belongs in your care plan",
                "Choose one small need to name today: food, a shower, a walk, company or uninterrupted rest. Ask for a specific task rather than waiting for someone to guess. Persistent anxiety, low mood or difficulty coping deserves a conversation with a clinician.",
                "",
            ),
            AdviceArticle(
                "Mom",
                "Newborn basics",
                "Start with the essentials",
                "A safe sleep space, responsive feeds, clean nappies and close attention to changes are enough to focus on. Keep the emergency card current. Ask your care team to show any care task you are unsure about.",
                "https://www.canada.ca/en/health-canada/services/safe-sleep/safe-sleep-tips.html",
            ),
            AdviceArticle(
                "Dad",
                "Feeding",
                "Supporting breastfeeding",
                "Ask what support is wanted. Bring water, help position cushions and take responsibility for burping, nappies and settling afterward. Protect time for feeding and rest. Encourage skilled help for pain or feeding concerns without pressuring a particular feeding choice.",
                "https://www.nhs.uk/baby/breastfeeding-and-bottle-feeding/breastfeeding/positioning-and-attachment/",
            ),
            AdviceArticle(
                "Dad",
                "Feeding",
                "Burping and bottle support",
                "Hold baby upright with head and neck supported and gently rub or pat their back. With a bottle, keep baby semi-upright, pause often and stop at fullness cues. Never prop a bottle or force a feed.",
                "https://www.nhs.uk/baby/breastfeeding-and-bottle-feeding/bottle-feeding/advice/",
            ),
            AdviceArticle(
                "Dad",
                "Everyday care",
                "A confident nappy change",
                "Gather supplies before you start. Keep a hand on baby on a raised changing surface, or use a safe floor mat. Clean gently, dry the skin and wash your hands. Note a rash or an unusual change and ask for advice if concerned.",
                "https://www.nhs.uk/baby/caring-for-a-newborn/how-to-change-your-babys-nappy/",
            ),
            AdviceArticle(
                "Dad",
                "Crying",
                "Colic and prolonged crying",
                "Check basic needs, try a quiet cuddle or gentle rocking, then trade shifts. If frustration becomes overwhelming, put baby in a safe cot, step away briefly and get another adult. Never shake a baby. Ask a clinician about prolonged crying or signs of illness.",
                "https://www.albertahealthservices.ca/injprev/Page4845.aspx",
            ),
            AdviceArticle(
                "Dad",
                "Rest",
                "Night shifts that protect both parents",
                "Agree on who is on duty and when before bedtime. Even if one parent handles feeds, the other can manage nappies, water, clean equipment and resettling. Keep handovers short and clear, and protect a rest period for each adult.",
                "",
            ),
            AdviceArticle(
                "Dad",
                "Bonding",
                "Connection in ordinary moments",
                "Talk, sing, read and respond to your child. Bonding grows through repeated care, not one perfect activity. Learn their cues by doing everyday tasks yourself. Ask a caregiver to show you a task, then practise it.",
                "",
            ),
            AdviceArticle(
                "Dad",
                "Bonding",
                "Skin-to-skin, safely",
                "While fully awake, hold baby against your bare chest with their face visible and airway clear; support the head and neck. If you feel sleepy, move baby to their safe sleep space. Ask your care team for help positioning a newborn.",
                "https://www.canada.ca/en/health-canada/services/safe-sleep/safe-sleep-tips.html",
            ),
            AdviceArticle(
                "Dad",
                "Support",
                "Recognizing when to ask for help",
                "Tell someone when exhaustion or anger is affecting how you cope. A trusted adult can take over while you rest. If anyone may be harmed, contact emergency services. Persistent distress deserves professional support.",
                "https://www.albertahealthservices.ca/injprev/Page4845.aspx",
            ),
            AdviceArticle(
                "Dad",
                "Household",
                "Own the whole task",
                "Choose a recurring job—laundry, supplies, meals or appointments—and handle planning through completion. Check low inventory before it runs out. A shared schedule and specific handovers reduce the mental load.",
                "",
            ),
            AdviceArticle(
                "Dad",
                "Calming",
                "A steadier settling routine",
                "Lower noise and light, use a gentle voice and try a slow cuddle. Notice whether baby needs a break from stimulation. Settling may take several tries; switch caregivers before frustration builds.",
                "https://caringforkids.cps.ca/handouts/pregnancy-and-babies/colic_and_crying",
            ),
        ) + MoreAdvice.articles

    fun search(audience: String, category: String, query: String) = articles.filter {
        it.audience == audience &&
            (category == "All" || it.category == category) &&
            ("${it.title} ${it.body}".contains(query.trim(), ignoreCase = true))
    }
}
