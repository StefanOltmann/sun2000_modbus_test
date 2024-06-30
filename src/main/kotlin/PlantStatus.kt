data class PlantStatus(

    /** DC input power in kW */
    val inputPower: Double,

    /** AC active power in kW */
    val activePower: Double,

    val powerMeterActivePower: Double,

    val batteryStatusCode: Int,
    val batteryPower: Double,
    val batteryVoltage: Double,
    val batteryStateOfCharge: Double
) {

    fun toPrettyString(): String = """
            ### Plant status
            DC Input Power: $inputPower kW
            AC Active Power: $activePower kW
            Power Meter Active Power: $powerMeterActivePower kW
            Battery Status Code: $batteryStatusCode
            Battery Power: $batteryPower kW
            Battery Voltage: $batteryVoltage V
            Battery SOC: $batteryStateOfCharge %
        """.trimIndent()
}