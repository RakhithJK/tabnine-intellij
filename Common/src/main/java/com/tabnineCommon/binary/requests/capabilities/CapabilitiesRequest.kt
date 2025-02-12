package com.tabnineCommon.binary.requests.capabilities

import com.tabnineCommon.binary.BinaryRequest

class CapabilitiesRequest : BinaryRequest<CapabilitiesResponse> {
    override fun response(): Class<CapabilitiesResponse> {
        return CapabilitiesResponse::class.java
    }

    override fun serialize(): Any {
        return mapOf("Features" to emptyMap<Any, Any>())
    }
}
