package com.my.chery.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject

/** 统一车辆状态，隔离不同车型云端字段名的差异。 */
data class VehicleData(
    val carName: String = "奇瑞汽车",
    val vin: String = "",
    val rangeKm: Int? = null,
    val fuelRangeKm: Int? = null,
    val electricRangeKm: Int? = null,
    val fuelPercent: Double? = null,
    val batteryPercent: Double? = null,
    val odometerKm: Double? = null,
    val fuelConsumption: Double? = null,
    val speed: Double? = null,
    val locked: Boolean? = null,
    val anyDoorOpen: Boolean? = null,
    val engineOn: Boolean? = null,
    val online: Boolean? = null,
    val charging: Boolean? = null,
    val interiorTemperature: Double? = null,
    val exteriorTemperature: Double? = null,
    val frontLeftTyre: Double? = null,
    val frontRightTyre: Double? = null,
    val rearLeftTyre: Double? = null,
    val rearRightTyre: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val updateTime: String? = null,
    val rawJson: String = ""
)

data class VehicleItem(val vin: String, val name: String, val plate: String = "", val isDefault: Boolean = false)

object VehicleParser {
    fun parse(root: JsonObject, fallbackName: String = "奇瑞汽车", fallbackVin: String = ""): VehicleData {
        val scalarFields = LinkedHashMap<String, JsonElement>()
        flatten(root, scalarFields)

        fun find(vararg names: String): JsonElement? {
            for (name in names) {
                scalarFields[normalize(name)]?.let { return it }
            }
            for (name in names) {
                val normalized = normalize(name)
                scalarFields.entries.firstOrNull {
                    val key = it.key
                    (normalized.length >= 5 && (key.endsWith(normalized) || normalized.endsWith(key))) ||
                        (normalized.length >= 8 && (key.contains(normalized) || normalized.contains(key)))
                }?.let { return it.value }
            }
            return null
        }

        fun text(vararg names: String) = find(*names)
            ?.takeUnless { it.isJsonNull }?.asString?.takeUnless { it.isBlank() }

        fun number(vararg names: String) = text(*names)?.replace(',', '.')?.toDoubleOrNull()
        // 纯电车油量为无效哨兵值（65535 / 0xFFFF），超出 0..100 一律视为无数据。
        fun percent(vararg names: String) = number(*names)?.takeIf { it in 0.0..100.0 }
        fun int(vararg names: String) = number(*names)?.toInt()
        fun bool(vararg names: String): Boolean? {
            val value = text(*names)?.trim()?.lowercase() ?: return null
            return when (value) {
                "true", "1", "yes", "on", "open", "开启", "是", "在线" -> true
                "false", "0", "no", "off", "close", "closed", "关闭", "否", "离线" -> false
                else -> null
            }
        }

        // LionCloud 车况里 doorLock: 0=已锁, 1=未锁；车门字段 0=关闭。
        val doorLock = int("doorLock")
        val locked = when (doorLock) {
            0 -> true
            1 -> false
            else -> bool("locked", "lockStatus", "centralLockStatus", "doorLockStatus")
        }
        val doorKeys = listOf("frontLeftDoor", "frontRightDoor", "backLeftDoor", "backRightDoor", "trunkDoor", "hood")
        val doorStates = doorKeys.mapNotNull { int(it) }
        val anyDoorOpen = if (doorStates.isNotEmpty()) doorStates.any { it != 0 }
        else bool("doorOpen", "anyDoorOpen", "doorStatus")

        return VehicleData(
            carName = text("carName", "vehicleName", "nickName", "modelName") ?: fallbackName,
            vin = text("vin") ?: fallbackVin,
            rangeKm = int("comprehensiveMileageCltc", "electricitymileageCltc", "comprehensiveMileage", "comprehensiveMileageWltc", "drivingRange", "enduranceMileage", "remainingMileage", "totalRange"),
            fuelRangeKm = int("fuelRange", "fuelEnduranceMileage", "oilEnduranceMileage"),
            electricRangeKm = int("electricitymileageCltc", "pureElectricRange", "electricitymileageWltc", "electricRange", "evRange", "batteryRange", "electricEnduranceMileage"),
            fuelPercent = percent("fuelPercent", "fuelLevel", "remainingFuel", "oilPercent", "oilSurplus", "fuelLevelPrc"),
            batteryPercent = percent("electricitySurplus", "soc", "batteryPercent", "electricPercent", "remainingPower", "batterySoc", "batteryPackPrc"),
            odometerKm = number("odometer", "totalMileage", "mileage", "travelMileage"),
            fuelConsumption = number("avgFuelConsumption", "averageFuelConsumption", "fuelConsumption")?.takeIf { it > 0.0 },
            speed = number("speedCar", "speed", "vehicleSpeed"),
            locked = locked,
            anyDoorOpen = anyDoorOpen,
            engineOn = when (int("engineState", "powerOnSts")) {
                1 -> true
                0 -> false
                else -> bool("engineOn", "engineStatus")
            },
            online = bool("online", "onlineStatus", "isOnline"),
            charging = when (int("chargingGun")) {
                1 -> true
                0 -> false
                else -> bool("charging", "chargeStatus", "chargingStatus", "isCharging")
            },
            interiorTemperature = number("inCarTemperature", "interiorTemperature", "insideTemperature"),
            exteriorTemperature = number("exteriorTemperature", "outdoorTemperature", "outsideTemperature"),
            frontLeftTyre = number("leftFrontTyrekPa", "frontLeftTyrePressure", "flTirePressure", "leftFrontTirePressure", "tirePressureFl"),
            frontRightTyre = number("rightFrontTyrekPa", "frontRightTyrePressure", "frTirePressure", "rightFrontTirePressure", "tirePressureFr"),
            rearLeftTyre = number("leftRearTyrekPa", "rearLeftTyrePressure", "rlTirePressure", "leftRearTirePressure", "tirePressureRl"),
            rearRightTyre = number("rightRearTyrekPa", "rearRightTyrePressure", "rrTirePressure", "rightRearTirePressure", "tirePressureRr"),
            latitude = number("lat", "latitude"),
            longitude = number("lon", "lng", "longitude"),
            updateTime = text("time", "updateTime", "dataTime", "lastUpdateTime", "uploadTime"),
            rawJson = root.toString()
        )
    }

    private fun flatten(element: JsonElement, fields: MutableMap<String, JsonElement>) {
        when {
            element.isJsonObject -> element.asJsonObject.entrySet().forEach { (key, value) ->
                if (value.isJsonPrimitive) fields.putIfAbsent(normalize(key), value)
                else flatten(value, fields)
            }
            element.isJsonArray -> element.asJsonArray.forEach { flatten(it, fields) }
        }
    }

    private fun normalize(value: String) = value.filter { it.isLetterOrDigit() }.lowercase()
}
