package me.mitkovic.kmp.netpulse.data.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlOtherAttributes
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("settings", "", "")
data class ServersResponse(
    @XmlSerialName("servers", "", "")
    @XmlChildrenName("server")
    val servers: List<Server>,
)

@Serializable
@XmlSerialName("server", "", "")
data class Server(
    @XmlOtherAttributes
    val attrs: Map<String, String>,
)
