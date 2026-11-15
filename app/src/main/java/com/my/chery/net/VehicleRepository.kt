package com.my.chery.net

import com.my.chery.data.AppConfig
import com.my.chery.data.VehicleData
import com.my.chery.data.VehicleParser

object VehicleRepository {
    suspend fun fetch(config: AppConfig): VehicleData {
        val body = CheryApi().condition(config.token, config.vin, config.secretKey)
        return VehicleParser.parse(body, config.carName, config.vin)
    }
}
