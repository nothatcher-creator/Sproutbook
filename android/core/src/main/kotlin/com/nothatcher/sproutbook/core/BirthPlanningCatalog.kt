package com.nothatcher.sproutbook.core

import java.security.MessageDigest

typealias BirthSource = AdviceSource
data class BirthChecklistItem(val key: String, val title: String, val notes: String)
data class BirthGuide(
    val id: String,
    val label: String,
    val title: String,
    val paragraphs: List<String>,
    val items: List<BirthChecklistItem>,
    val sources: List<BirthSource>,
)

/** Offline educational content. These checklists are personal plans, never clinical clearance. */
object BirthPlanningCatalog {
    val birthOptions = BirthSource("NHS · Where to give birth", "https://www.nhs.uk/pregnancy/labour-and-birth/where-to-give-birth-the-options/")
    val labour = BirthSource("NHS · Signs of labour", "https://www.nhs.uk/pregnancy/labour-and-birth/signs-that-labour-has-begun/")
    val intrapartum = BirthSource("NICE · Birth setting and transfer", "https://www.nice.org.uk/guidance/ng235/chapter/Recommendations")
    val postnatal = BirthSource("NICE · Postnatal care", "https://www.nice.org.uk/guidance/ng194/chapter/Recommendations")
    val warningSigns = BirthSource("CDC · Urgent maternal warning signs", "https://www.cdc.gov/hearher/maternal-warning-signs/index.html")
    val newborn = BirthSource("NHS · Urgent help for babies", "https://www.nhs.uk/baby/health/when-to-get-urgent-medical-help-for-babies-and-children-under-5/")
    val bag = BirthSource("NHS · Hospital bag checklist", "https://www.nhs.uk/best-start-in-life/pregnancy/preparing-for-labour-and-birth/hospital-bag-checklist/")

    const val emergencyTitle = "Urgent help comes first"
    const val emergency = "Call your local emergency number now for heavy bleeding, chest pain, trouble breathing, collapse or a seizure, or if a newborn is not breathing normally, turns blue or grey, or is unresponsive. If birth is imminent and no qualified attendant is there, call emergency services and follow the dispatcher's directions. Do not wait to finish a checklist or use the app to judge safety."
    const val urgent = "Contact your maternity team immediately for reduced or changed baby movements, vaginal bleeding, waters breaking, possible labour before 37 weeks, or if you feel unwell or worried. Severe headache, vision changes, fever or severe ongoing abdominal pain need urgent medical assessment during pregnancy or after birth. If symptoms are severe or life-threatening, call emergency services. This is not a complete list of warning signs."
    const val listNote = "Personal planning lists, not a safety assessment. Checking every box does not make an unassisted birth safe. Adapt plans with qualified maternity professionals; services and recommendations vary by country."

    val guides = listOf(
        BirthGuide(
            "home", "Homebirth", "Planning an attended homebirth",
            listOf(
                "Homebirth (home birth) with a qualified midwife or doctor includes clinical care, monitoring and a plan for hospital transfer. Discuss whether it is suitable for your pregnancy with your maternity team, and review the plan if your circumstances change.",
                "NHS guidance describes a small increase in serious problems for the baby for a first birth planned at home. For low-risk pregnancies after a previous birth, planned homebirth can have comparable baby outcomes. This evidence concerns attended births with access to transfer; it does not describe freebirth. Local services and your individual circumstances matter.",
                "Ask who will attend, what happens if the team is unavailable, what equipment they bring, when to call, and how transfer works. Complications can arise even in a pregnancy considered low risk. You can change your plan or ask for help at any time.",
            ),
            listOf(
                BirthChecklistItem("care", "Discuss suitability with the maternity team", "Bring your health history, previous birth experiences, care records and questions. Review the plan throughout pregnancy; this list cannot assess risk."),
                BirthChecklistItem("attendant", "Confirm qualified attendants and backup cover", "Confirm registration, who will attend, the on-call number and the plan if the team cannot come. A support person or doula does not replace a qualified birth attendant."),
                BirthChecklistItem("contact", "Save maternity and emergency contact details", "Keep the maternity team's day/night number and your local emergency number accessible. Write your full address and access instructions on paper as well as on your phone."),
                BirthChecklistItem("transfer", "Agree a hospital transfer plan", "Ask about reasons for transfer, destination, travel time, transport, handover and who can travel with you. Emergency transport needs professional coordination; do not rely on driving yourself."),
                BirthChecklistItem("space", "Prepare a comfortable space with the care team", "Ask the team about their space, water, warmth and supply requirements. Plan privacy, clear access and a place for handwashing. The team arranges clinical equipment."),
                BirthChecklistItem("comfort", "Gather agreed comfort supplies", "Prepare comfortable clothes, clean towels, protective bedding, maternity pads and a charged phone. Follow the care team's advice for any additional supplies."),
                BirthChecklistItem("support", "Arrange practical family support", "Agree a support person's role, care for other children and pets, transport and help with meals and rest. Share the transfer plan with those supporting you."),
                BirthChecklistItem("aftercare", "Arrange care for you and your newborn", "Agree postnatal visits, newborn checks and screening, feeding support and whom to contact with concerns. Keep a transfer bag ready even when planning to stay home."),
            ),
            listOf(birthOptions, intrapartum, bag, postnatal),
        ),
        BirthGuide(
            "free", "Freebirth", "Considering freebirth / unassisted birth",
            listOf(
                "Freebirth (free birth) means planning to give birth without a midwife or doctor attending. It differs from an attended homebirth and from an unexpected birth before help arrives. Having a partner, friend or doula present is not the same as care from a qualified birth attendant.",
                "Unassisted birth removes professional monitoring and immediate clinical help. Serious bleeding or a baby needing breathing support can arise unexpectedly, and getting treatment may be delayed. Attended-homebirth outcome figures must not be used to claim that freebirth is equally safe. Qualified maternity care is recommended; a checklist, birth kit or app cannot replace it.",
                "If privacy, consent, previous trauma or feeling unheard are part of your decision, ask about a senior midwife discussion, continuity of care, trauma-informed support and a personalised plan. You can accept antenatal, birth, postnatal or emergency care and change your mind at any time. Ask about options and rights in your own country.",
                "The list below helps you discuss concerns, arrange support and keep access to care. It is not instructions for managing a birth alone. Call your local emergency number for a life-threatening emergency or an imminent birth with no qualified attendant present; follow the dispatcher. Seek help promptly, even if a previous plan was to avoid medical attendance.",
            ),
            listOf(
                BirthChecklistItem("discussion", "Discuss your reasons and options with a qualified professional", "Write what matters to you: consent, privacy, previous experiences, continuity, who is present and how you want to be listened to. Ask about a plan that meets your needs with qualified care."),
                BirthChecklistItem("care", "Keep access to antenatal and postnatal care", "Arrange recommended appointments, tests and follow-up with your maternity team. Choosing a birth setting does not mean you must decline other care."),
                BirthChecklistItem("contacts", "Keep emergency and maternity numbers ready", "Use a charged phone and a written backup of your local emergency number, maternity contact, full address and access instructions. Tell your support person where they are."),
                BirthChecklistItem("help", "Agree that anyone can call for help promptly", "Share warning signs and the plan for contacting qualified help. Do not wait for an app, a checklist, a fixed contraction pattern or a distant supporter before calling in an emergency."),
                BirthChecklistItem("transfer", "Discuss transfer and an alternative birth setting", "Ask a maternity professional about a receiving hospital, emergency access, transport and care if plans change. A private car is not a substitute for emergency care."),
                BirthChecklistItem("records", "Keep care records and a transfer bag accessible", "Include birth preferences, medication/allergy information and maternity records. Prepare everyday essentials for you and baby and a safe transport plan."),
                BirthChecklistItem("support", "Arrange support that respects access to care", "Plan help for other children, transport, meals and recovery. A partner, friend or doula can support you but cannot replace a qualified birth attendant."),
                BirthChecklistItem("newborn", "Arrange prompt assessment after birth", "Agree professional assessment for you and the newborn, newborn checks and screening, feeding help and an emergency contact. Do not wait for visible illness to arrange care."),
            ),
            listOf(birthOptions, intrapartum, warningSigns, postnatal),
        ),
        BirthGuide(
            "transfer", "Transfer plan", "A plan for getting help and changing setting",
            listOf(
                "A transfer plan matters for any planned birth outside hospital. Ask your maternity team when to call, who arranges transport, which hospital receives you and how information is handed over. Transfer can be needed for pain relief, concerns during labour, or care for you or baby after birth.",
                "Discuss usual travel time and possible delays, but do not treat a journey estimate as a guarantee of emergency access. If there is a life-threatening emergency, call your local emergency number, give the exact address and follow the dispatcher. Do not drive yourself during an emergency.",
            ),
            listOf(
                BirthChecklistItem("numbers", "Write down day/night care and emergency numbers", "Keep a paper copy beside your care records. Include your full address, postcode or local equivalent, entry instructions and who can open the door."),
                BirthChecklistItem("destination", "Agree the receiving hospital and handover", "Ask your maternity team about the destination, availability of obstetric and newborn care, and who shares your care records. Plans may change if services are unavailable."),
                BirthChecklistItem("transport", "Discuss routine and emergency transport", "Ask who arranges each type of transfer, what delays are possible and how a companion can travel. Emergency transport is coordinated by professionals; do not drive yourself."),
                BirthChecklistItem("bag", "Pack an accessible transfer bag", "Include maternity records, birth preferences, regular medication information, comfortable clothes, maternity pads, toiletries, phone charger, baby clothes and nappies."),
                BirthChecklistItem("family", "Agree childcare, home access and companion roles", "Plan who looks after other children and pets, who comes with you, who brings the bag and who secures the home. Keep access clear for responders."),
                BirthChecklistItem("return", "Plan safe travel home and follow-up", "Arrange an appropriate infant car seat if travelling by car and agree postnatal follow-up. Review changes to your plan with the care team."),
            ),
            listOf(intrapartum, bag, warningSigns),
        ),
        BirthGuide(
            "after", "After birth", "Care and support after a birth at home",
            listOf(
                "Arrange professional assessment and ongoing care for both you and baby, including after an unassisted or unexpected birth. Discuss bleeding, pain, recovery, feeding, emotional wellbeing and any concerns. You remain entitled to ask for care when your plan changes.",
                "Plan newborn physical examination, hearing and blood-spot screening, and discuss vitamin K with a qualified clinician. UK guidance recommends the newborn physical examination within 72 hours; ask your local team about arrangements and timing. Arrange prompt feeding support if feeds are difficult or baby is unusually sleepy or unwell.",
                "Make a safe sleep space: baby on their back, on a firm, flat, clear sleep surface in their own cot or bassinet in your room. Ask your care team about local safer-sleep guidance. Recovery support and newborn screening do not replace urgent assessment when warning signs occur.",
            ),
            listOf(
                BirthChecklistItem("visit", "Arrange assessment and postnatal contacts", "Agree who will assess you and baby after birth, when they will visit and the day/night number for concerns. Tell the team if the birth happened without a clinician attending."),
                BirthChecklistItem("checks", "Book newborn examination and screening", "Confirm the physical examination, hearing and blood-spot screening plan. UK newborn physical examination is recommended within 72 hours; local arrangements may differ."),
                BirthChecklistItem("vitamin-k", "Discuss vitamin K with a qualified clinician", "Ask about benefits, options and the local plan. Clinical care and medicines belong with qualified professionals, not this checklist."),
                BirthChecklistItem("feeding", "Arrange early feeding support", "Know whom to contact for latch, milk transfer or bottle-feeding support. An unusually sleepy, unwell or poorly feeding newborn needs prompt medical assessment."),
                BirthChecklistItem("sleep", "Prepare a safer sleep space", "Use a firm, flat, clear cot or bassinet; place baby on their back in your room. Ask about current local safer-sleep advice."),
                BirthChecklistItem("recovery", "Plan meals, rest and emotional support", "Agree practical help, care for siblings, follow-up visits and someone you can talk to. If you may harm yourself or your baby, seek emergency help immediately."),
                BirthChecklistItem("warnings", "Share warning signs and how to get help", "Keep emergency contacts accessible. Heavy bleeding, breathing difficulty, chest pain, collapse or a baby not breathing normally need emergency help. Other concerning symptoms need prompt professional advice."),
            ),
            listOf(postnatal, warningSigns, birthOptions),
        ),
    )

    val articles = listOf("Mom", "Dad").flatMap { audience ->
        guides.map { guide ->
            AdviceArticle(audience, "Birth planning", guide.title,
                (listOf(emergency, urgent) + guide.paragraphs).joinToString("\n\n"), guide.sources.first().url,
                (guide.sources + listOf(labour, warningSigns, newborn)).distinctBy { it.url })
        }
    }

    // Hash the complete child ID so even a valid 160-character imported ID produces bounded IDs.
    fun prefix(childId: String, guideId: String): String {
        require(guides.any { it.id == guideId })
        val token = MessageDigest.getInstance("SHA-256").digest(childId.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return "birth-v1-$token-$guideId-"
    }

    fun guideForItem(childId: String, itemId: String): BirthGuide? =
        guides.firstOrNull { itemId.startsWith(prefix(childId, it.id)) }

    fun isBuiltIn(childId: String, itemId: String): Boolean = guides.any { guide ->
        guide.items.any { itemId == prefix(childId, guide.id) + it.key }
    }
}
