package be.ap.student.mobiledev_rently.dataClasses

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("reversegeocode")
data class LocationXml(
    @set:JsonProperty("addressparts")
    var addressparts: Addressparts? = null,
)
@JsonRootName("addressparts")
data class Addressparts(
    @set:JsonProperty("city")
    var city: String? = null,
    @set:JsonProperty("road")
    var road: String? = null,
    @set:JsonProperty("house_number")
    var number: String? = null,
)
