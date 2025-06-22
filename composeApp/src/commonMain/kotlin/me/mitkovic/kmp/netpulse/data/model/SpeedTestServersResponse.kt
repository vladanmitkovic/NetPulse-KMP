package me.mitkovic.kmp.netpulse.data.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlOtherAttributes
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("settings", "", "")
data class SpeedTestServersResponse(
    /**
     * Matches the `<servers>` container, then
     * pulls out each `<server …/>` into this List.
     */
    @XmlSerialName("servers", "", "")
    @XmlChildrenName("server")
    val servers: List<Server>,
)

@Serializable
@XmlSerialName("server", "", "")
data class Server(
    /**
     * Collects _all_ XML attributes (url, lat, lon, name, country, cc, sponsor, id, host)
     * into a simple Map<localName, value>.
     */
    @XmlOtherAttributes
    val attrs: Map<String, String>,
)
