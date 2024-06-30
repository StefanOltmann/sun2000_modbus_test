import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster

const val DELAY_BETWEEN_TRANSACTIONS_MS: Long = 250
const val DELAY_BETWEEN_POLLS_MS: Long = 10000
const val DELAY_AFTER_CONNECTION_MS: Long = 3000

const val DEFAULT_MODBUS_PORT: Int = 502

fun main() {

    pollModbus(
        ipAddress = "192.168.0.100",
        responseHandler = { plantStatus ->

            println(plantStatus.toPrettyString())
        }
    )
}

private fun pollModbus(
    ipAddress: String,
    port: Int = DEFAULT_MODBUS_PORT,
    responseHandler: (PlantStatus) -> Unit
) {

    while (true) {

        val master = ModbusTCPMaster(ipAddress, port);

        master.connect()

        /*
         * Important: We need to wait some time here to avoid timeouts.
         * At least 2 seconds.
         */
        Thread.sleep(DELAY_AFTER_CONNECTION_MS)

        while (master.isConnected) {

            val inputPowerAnswer = master.readMultipleRegisters(32064, 18)

            Thread.sleep(DELAY_BETWEEN_TRANSACTIONS_MS)

            val batteryAnswer = master.readMultipleRegisters(37000, 5)

            Thread.sleep(DELAY_BETWEEN_TRANSACTIONS_MS)

            val powerMeterAnswer = master.readMultipleRegisters(37113, 2)

            /* Convert the answers */

            val inputPower = convertToInt32(
                highWord = inputPowerAnswer[0].value,
                lowWord = inputPowerAnswer[1].value
            ) / 1000.0

            val activePower = convertToInt32(
                highWord = inputPowerAnswer[16].value,
                lowWord = inputPowerAnswer[17].value
            ) / 1000.0

            val batteryStatus = batteryAnswer[0].value

            val batteryPower = convertToInt32(
                highWord = batteryAnswer[1].value,
                lowWord = batteryAnswer[2].value
            ) / 1000.0

            val batteryVoltage = batteryAnswer[3].value / 10.0

            val batteryStateOfCharge = batteryAnswer[4].value / 10.0

            val powerMeterActivePower = convertToInt32(
                highWord = powerMeterAnswer[0].value,
                lowWord = powerMeterAnswer[1].value
            ) / 1000.0

            /* Create data object */

            val plantStatus = PlantStatus(

                inputPower = inputPower,
                activePower = activePower,
                powerMeterActivePower = powerMeterActivePower,

                batteryStatusCode = batteryStatus,
                batteryPower = batteryPower,
                batteryVoltage = batteryVoltage,
                batteryStateOfCharge = batteryStateOfCharge
            )

            responseHandler(plantStatus)

            Thread.sleep(DELAY_BETWEEN_POLLS_MS)
        }

        /* Wait until new connect attempt. */
        Thread.sleep(DELAY_BETWEEN_POLLS_MS)
    }
}

private fun convertToInt32(
    highWord: Int,
    lowWord: Int
): Int = (highWord shl 16) or (lowWord and 0xFFFF)