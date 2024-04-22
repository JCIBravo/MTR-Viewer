package com.jcibravo.mtr_viewer

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.util.Log
import android.widget.Toast
import com.jcibravo.mtr_viewer.classes.DataResponse
import com.jcibravo.mtr_viewer.classes.RouteData
import com.jcibravo.mtr_viewer.classes.StationData
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * World index
 **/
var DIMENSION = 0

/**
 * All data from MTR API endpoint is stored here
 **/
var GLOBAL: List<DataResponse>? = null

/**
 * Create an acronym if the string has more than 1 word
 *
 * - Example 1: text= "Local Train" returns "LT"
 * - Example 2: text= "Regional" returns "Regional"
 * @param text Sentence
 * **/
fun createAcronym(text: String): String {
    val sentence = text.split(" ")
    return if (sentence.size > 1) {
        val acronimo = StringBuilder()
        for (word in sentence) {
            acronimo.append(word[0])
        }

        acronimo.toString().uppercase()
    } else {
        text
    }
}


/**
 * Get the data of a station by entering its station ID.
 * @param world_index World index
 * @param stationID Station ID
 * @throws IndexOutOfBoundsException If world_index is bigger than dimensions of the world.
 * @throws NotFoundException If dimension does not contain any data related to MTR.
 * @throws NoSuchElementException If station ID is unknown.
 **/
fun getStationDataFromID(world_index: Int, stationID: String): StationData {
    if (world_index >= GLOBAL!!.size) {
        throw IndexOutOfBoundsException("The entered world index exceeds the amount the server has.")
    }

    if (GLOBAL!![world_index].stations.isEmpty() && GLOBAL!![world_index].routes.isEmpty() && GLOBAL!![world_index].types.isEmpty() && GLOBAL!![world_index].positions.isEmpty()){
        throw NotFoundException("The entered world index does not contain any data related to MTR infrastructure.")
    }

    return GLOBAL!![world_index].stations[stationID] ?: throw NoSuchElementException("Unknown station ID")
}

/**
 * Get the data of a station by entering its station ID.
 * @param world_index World index
 * @param routeName Route name
 * @throws IndexOutOfBoundsException If world_index is bigger than dimensions of the world.
 * @throws NotFoundException If dimension does not contain any data related to MTR.
 * @throws NoSuchElementException If station ID is unknown.
 **/
fun getRouteDataFromName(world_index: Int, routeName: String): RouteData {
    val result: RouteData
    if (world_index >= GLOBAL!!.size) {
        throw IndexOutOfBoundsException("The entered world index exceeds the amount the server has.")
    }

    if (GLOBAL!![world_index].stations.isEmpty() && GLOBAL!![world_index].routes.isEmpty() && GLOBAL!![world_index].types.isEmpty() && GLOBAL!![world_index].positions.isEmpty()){
        throw NotFoundException("The entered world index does not contain any data related to MTR infrastructure.")
    }

    try {
        result = GLOBAL!![world_index].routes.filter { it.name == routeName }[0]
    } catch (e: IndexOutOfBoundsException) {
        throw NoSuchElementException("Unknown route name")
    }

    return result
}

/**
 * Returns the quantity of worlds the server has
 **/
fun getDimensionCount(): Int {
    return GLOBAL!!.size
}
/**
 * Safely increment the dimension number
 * @param context (optional) Displays a visible warning the user
 **/
fun incrementDimension(context: Context? = null) {
    if (DIMENSION+1 >= getDimensionCount()) {
        Log.w("MTR DIMENSION", "Can't increment dimension. Already at its maximum.")
        if (context != null) Toast.makeText(context, context.getString(R.string.increment_dimension_error), Toast.LENGTH_SHORT).show()
    } else {
        DIMENSION++
    }
}

/**
 * Safely decrement the dimension number
 * @param context (optional) Displays a visible warning the user
 **/
fun decrementDimension(context: Context? = null) {
    if (DIMENSION-1 < 0) {
        Log.w("MTR DIMENSION", "Can't decrement dimension. Already at its minimum.")
        if (context != null) Toast.makeText(context, context.getString(R.string.decrement_dimension_error), Toast.LENGTH_SHORT).show()
    } else {
        DIMENSION--
    }
}

/**
 * Shows the first language of the destination name.
 *
 * Example: "秋葉原駅|Akihabara Station" --> "秋葉原駅"
 * @param destination Destination
 **/
fun getFirstLanguage(destination: String): String {
    return destination.substringBefore("|")
}

/**
 * Shows the first language of the station name and the others in parenthesis.
 *
 * - Example 1: station= "秋葉原駅" returns "秋葉原駅"
 * - Example 2: station= "秋葉原駅|Akihabara Station" returns "秋葉原駅 (Akihabara Station)"
 * - Example 3: station= "秋葉原駅|秋叶原站|Akihabara Station|..." returns "秋葉原駅 (秋叶原站 / Akihabara Station / ...)"
 * @param station Station name
 **/
fun getAllLanguages(station: String): String {
    var finalString = ""
    val names = station.split("|")
    when (names.size) {
        1 -> { finalString = names[0] }
        2 -> { finalString = "${names[0]} (${names[1]})" }
        else -> {
            for (i in 0..names.lastIndex) {
                finalString += when (i) {
                    0 -> names[i]
                    1 -> " (${names[i]}"
                    else -> " / ${names[i]}"
                }
            }

            finalString += ")"
        }
    }

    return finalString
}

/**
 * Returns all the languages of the station name as a list.
 *
 * - Example 1: station= "秋葉原駅" returns [["秋葉原駅"]]
 * - Example 2: station= "秋葉原駅|Akihabara Station" returns [["秋葉原駅", "Akihabara Station"]]
 * - Example 3: station= "秋葉原駅|秋叶原站|Akihabara Station|..." returns [["秋葉原駅", "秋叶原站", "Akihabara Station", "..."]]
 * @param station Station name
 **/
fun getAllLanguagesAsList(station: String): List<String> {
    return station.split("|").filter { it.isNotEmpty() && it.isNotBlank() }
}

/**
 * Removes the station ID's position ID from all items in the list.
 *
 * Example: "219654589_5815548" -> "219654589"
 * @param stationIDs List with station IDs with position ID
 **/
fun normalizeStationIDList(stationIDs: List<String>): List<String> {
    return stationIDs.map { it.substringBefore('_') }
}

/**
 * Get the list of all the stations of the one-way route.
 *
 * Use this if your route format is like this: A --> B --> C --> D --> C --> B --> A
 * @param stationIDs List with station IDs
 **/
fun getOneWayStationIDs(stationIDs: List<String>): List<String> {
    val results = mutableListOf<String>()
    for (i in 0..normalizeStationIDList(stationIDs).lastIndex){
        if (!results.contains(normalizeStationIDList(stationIDs)[i])) {
            results.add(normalizeStationIDList(stationIDs)[i])
        } else break
    }

    return results.toList()
}

/**
 * Get the ID of the route origin
 *
 * Use this if your route format is like this: A --> B --> C --> D --> C --> B --> A
 * @param stationIDs List with station IDs
 **/
fun getOriginID(stationIDs: List<String>): String {
    return try { getOneWayStationIDs(stationIDs).first() }
    catch (e: Exception){ "" }
}

/**
 * Get the ID of the route destination
 *
 * Use this if your route format is like this: A --> B --> C --> D --> C --> B --> A
 * @param stationIDs List with station IDs
 **/
fun getDestinationID(stationIDs: List<String>): String {
    return try { getOneWayStationIDs(stationIDs).last() }
    catch (e: Exception){ "" }
}

/**
 * Converts milliseconds of time to a user-readable format (HH:mm format).
 * @param millis Milliseconds
 * @param includeDate Specify if you want the date to be included (DD/MM/YYYY format)
 * @param includeSeconds Specify if you want the seconds to be included
 **/
fun convertMillisToTime(millis: Long, includeDate: Boolean, includeSeconds: Boolean): String {
    val pattern = when {
        includeDate && includeSeconds -> "dd/MM/yyyy HH:mm:ss"
        includeDate -> "dd/MM/yyyy HH:mm"
        includeSeconds -> "HH:mm:ss"
        else -> "HH:mm"
    }

    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    val date = Date(millis)
    return sdf.format(date)
}

/**
 * Converts milliseconds to to an user-readable format and checks if the estimated time of arrival is closer to actual time.
 *
 * If time is bigger than 30 minutes it returns time in HH:mm format.
 *
 * If time is smaller than 30 minutes it returns time in "X min."
 *
 * If time is smaller than 1 minute it returns "Now"
 * @param millis Milliseconds
 **/
fun convertMillisAndCheckTime(millis: Long): String {
    val timestampNow = System.currentTimeMillis()
    val differenceInMillis = millis - timestampNow
    val differenceInMinutes = differenceInMillis / 60000

    return when {
        differenceInMinutes <= 0 -> "Now"
        differenceInMinutes <= 30 -> "$differenceInMinutes min."
        else -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
    }
}


/**
 * Shorten the String, to display only x characters of user's choice.
 * @param stringToTruncate Original word
 * @param size String size to shorten
 **/
fun truncateString(stringToTruncate: String, size: Int): String {
    val subString = stringToTruncate.substringBefore('|')
    if (subString.length > size.absoluteValue) {
        val truncatedString = subString.substring(0, size.absoluteValue-1)
        return "$truncatedString…"
    }

    return subString
}

/**
 * Convert "color" variable to hex color.
 * @param decimal Decimal color code
 **/
fun convertDecimalToHex(decimal: Int): String {
    return "#" + String.format("%06X", decimal)
}

/**
 * Convert dp to pixels
 * @param context context
 **/
fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density + 0.5f).toInt()

