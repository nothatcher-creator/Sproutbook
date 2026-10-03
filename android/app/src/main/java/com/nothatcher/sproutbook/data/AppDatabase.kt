package com.nothatcher.sproutbook.data

import androidx.room.*

@Database(
    entities =
        [
            Routine::class,
            RoutineCompletion::class,
            PottyLog::class,
            MilkContainer::class,
            Diaper::class,
            BottlePrep::class,
            ShoppingItem::class,
            WishlistItem::class,
            HealthRecord::class,
            EmergencyCard::class,
            Inventory::class,
            PrepItem::class,
            PregnancyEvent::class,
            Milestone::class,
            Tooth::class,
            Meal::class,
            Food::class,
            Sleep::class,
            Feed::class,
            Appointment::class,
            Memory::class,
            Child::class,
        ],
    version = AppDatabase.VERSION,
    exportSchema = true,
    autoMigrations =
        [
            AutoMigration(from = 1, to = 2),
            AutoMigration(from = 2, to = 3),
            AutoMigration(from = 3, to = 4),
            AutoMigration(from = 4, to = 5),
            AutoMigration(from = 5, to = 6),
            AutoMigration(from = 6, to = 7),
            AutoMigration(from = 7, to = 8),
            AutoMigration(from = 8, to = 9),
            AutoMigration(from = 9, to = 10),
            AutoMigration(from = 10, to = 11),
        ],
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val VERSION = 12
    }

    abstract fun routines(): RoutineDao

    abstract fun routineCompletions(): RoutineCompletionDao

    abstract fun pottyLogs(): PottyLogDao

    abstract fun milkContainers(): MilkContainerDao

    abstract fun diapers(): DiaperDao

    abstract fun bottlePreps(): BottlePrepDao

    abstract fun shoppingItems(): ShoppingItemDao

    abstract fun wishlistItems(): WishlistItemDao

    abstract fun healthRecords(): HealthRecordDao

    abstract fun emergencyCards(): EmergencyCardDao

    abstract fun inventorys(): InventoryDao

    abstract fun prepItems(): PrepItemDao

    abstract fun pregnancyEvents(): PregnancyEventDao

    abstract fun milestones(): MilestoneDao

    abstract fun tooths(): ToothDao

    abstract fun meals(): MealDao

    abstract fun foods(): FoodDao

    abstract fun sleeps(): SleepDao

    abstract fun feeds(): FeedDao

    abstract fun appointments(): AppointmentDao

    abstract fun memorys(): MemoryDao

    abstract fun children(): ChildDao
}
