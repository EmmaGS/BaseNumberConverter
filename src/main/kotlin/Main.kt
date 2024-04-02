package converter

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

fun main() {
    // Level 1:
    println("Enter two numbers in format: {source base} {target base} (To quit type /exit)")
    when (val firstRead = readln()) {
        "/exit" -> {}
        else -> {
            val sourceBase = firstRead.split(' ').first().toBigInteger()
            val targetBase = firstRead.split(' ').last().toBigInteger()
            enterLevelTwo(sourceBase, targetBase)
            main()
        }
    }
}

fun enterLevelTwo(sourceBase: BigInteger, targetBase: BigInteger) {
    println("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back)")
    when (val secondRead = readln()) {
        "/back" -> {}
        else -> {
            convertAnyBase(sourceBase, targetBase, secondRead)
            enterLevelTwo(sourceBase, targetBase)
        }
    }
}

fun convertAnyBase(sourceBase: BigInteger, targetBase: BigInteger, originalNumber: String) {
    // Check if fraction or whole
    val finalNumber = if (originalNumber.contains(".")) {
        convertFullFraction(sourceBase, targetBase, originalNumber)
    } else {
        convertWholeNumber(sourceBase, targetBase, originalNumber)
    }
    println("Conversion result: $finalNumber")
}

fun convertWholeNumber(sourceBase: BigInteger, targetBase: BigInteger, originalNumber: String): String {
    // Convert to decimal
    val decimalNumber: BigInteger = if (sourceBase == BigInteger.TEN) {
        originalNumber.toBigInteger()
    } else {
        calculateBaseToDecimal(originalNumber, sourceBase)
    }

    // Convert to actual base
    val finalNumber = if (targetBase == BigInteger.TEN || decimalNumber == BigInteger.ZERO) decimalNumber else calculateDecimalToBase(decimalNumber, targetBase)
    return finalNumber.toString()
}

fun convertFractionNumber(sourceBase: BigInteger, targetBase: BigInteger, originalNumber: String): String {
    // Convert to decimal
    val decimalNumber: BigDecimal = if (sourceBase == BigInteger.TEN) {
        originalNumber.toBigDecimal()
    } else {
        calculateBaseToDecimalFractional(originalNumber, sourceBase)
    }

    // Convert to actual base
    val finalNumber = if (targetBase == BigInteger.TEN) decimalNumber.setScale(5, RoundingMode.HALF_EVEN) else calculateDecimalToBaseFractional(decimalNumber, targetBase)
    return finalNumber.toString()
}

fun convertFullFraction(sourceBase: BigInteger, targetBase: BigInteger, originalNumber: String): String {
    // Split into two parts
    val firstPart = originalNumber.split(".").first()
    val secondPart = originalNumber.split(".").last()
    var finalNumber = convertWholeNumber(sourceBase, targetBase, firstPart)
    finalNumber += "."
    val fractionPart = convertFractionNumber(sourceBase, targetBase, secondPart)
    finalNumber += if (fractionPart.contains(".")) fractionPart.split(".").last() else fractionPart
    return finalNumber
}

//region convert to base with fractions
fun calculateDecimalToBaseFractional(number: BigDecimal, targetBase: BigInteger): String {
    var remainder = formatDecimalNumber(number)
    val scale = remainder.scale() // number of digits after decimal point
    var convertedNumber = ""
    var index = 0
    while ((remainder != BigDecimal.ONE.setScale(scale)) && (index < 5)) {
        remainder *= targetBase.toBigDecimal()
        val integerRemainder = remainder.setScale(0, RoundingMode.FLOOR)

        if (remainder < BigDecimal.TEN) {
            convertedNumber += integerRemainder
        } else {
            convertedNumber += remainder.toInt().digitToChar(radix = targetBase.toInt())
        }

        if (remainder != BigDecimal.ONE.setScale(scale)) remainder -= integerRemainder
        index++
    }
    return convertedNumber
}

fun formatDecimalNumber(number: BigDecimal): BigDecimal {
    return if (number.toString().contains(".")) number else ("0.$number").toBigDecimal()
}
//endregion

//region convert decimal to base
fun calculateDecimalToBase(number: BigInteger, targetBase: BigInteger): String {
    var tempNumber = number
    var convertedNumber = ""
    while (tempNumber > BigInteger.ZERO) {
        val remainder = remainderCalculation(tempNumber, targetBase)
        tempNumber /= targetBase
        convertedNumber = "$remainder$convertedNumber"
    }
    return convertedNumber
}

fun remainderCalculation(number: BigInteger, targetBase: BigInteger): String {
    val remainder = number % targetBase
    return if (remainder < BigInteger.TEN) {
        remainder.toString()
    } else {
        remainder.toInt().digitToChar(radix = targetBase.toInt()).toString()
    }
}
//endregion

//region convert base to decimal
fun calculateBaseToDecimal(number: String, sourceBase: BigInteger): BigInteger {
    val reversedNumber = number.reversed()
    var convertedNumber = BigInteger.ZERO

    for (index in reversedNumber.lastIndex downTo 0) {
        val trueNumber = when (reversedNumber[index]) {
            in 'a'..'z' -> reversedNumber[index].digitToInt(sourceBase.toInt())
            in 'A'..'Z' -> reversedNumber[index].digitToInt(sourceBase.toInt())
            in '0'..'9' -> Character.digit(reversedNumber[index], 10)
            else -> throw IllegalArgumentException("Expected valid decimal, octal or hexadecimal values. Received: $reversedNumber[index]")
        }
        convertedNumber += trueNumber.toBigInteger() * sourceBase.pow(index)
    }
    return convertedNumber
}
//endregion

//region convert base to decimal fractional
fun calculateBaseToDecimalFractional(number: String, sourceBase: BigInteger): BigDecimal {
    var convertedNumber = BigDecimal.ZERO

    for (index in 0..number.lastIndex) {
        val trueIndex = index + 1
        val trueNumber = when (number[index]) {
            in 'a'..'z' -> number[index].digitToInt(sourceBase.toInt())
            in 'A'..'Z' -> number[index].digitToInt(sourceBase.toInt())
            in '0'..'9' -> Character.digit(number[index], 10)
            else -> throw IllegalArgumentException("Expected valid decimal, octal or hexadecimal values. Received: $number[index]")
        }
        val firstCalc = BigDecimal.ONE.divide(sourceBase.pow(trueIndex).toBigDecimal(), 20, RoundingMode.HALF_EVEN)
        val secondCalc = trueNumber.toBigDecimal() * firstCalc
        convertedNumber += secondCalc
    }
    return convertedNumber
}
//endregion
