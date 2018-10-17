package com.insuranceClaim.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for UnderwritingState.
 */
object UnderwritingSchema

/**
 * A UnderwritingState schema.
 */
object UnderwritingSchemaVI : MappedSchema(
        schemaFamily = UnderwritingSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentUnderwriting::class.java)) {
    @Entity
    @Table(name = "underwriting_states")
    class PersistentUnderwriting(
            @Column(name = "insurer")
            var insurer: String,

            @Column(name = "underwriter")
            var underwriter: String,

            @Column(name = "fname")
            var fname: String,

            @Column(name = "lname")
            var lname: String,

            @Column(name = "insuranceID")
            var insuranceID: String,

            @Column(name = "type")
            var type: String,

            @Column(name = "value")
            var value: Int,

            @Column(name = "reason")
            var reason: String,

            @Column(name = "approvedAmount")
            var approvedAmount: Int,

            @Column(name = "insuranceStatus")
            var insuranceStatus: String,

            @Column(name = "referenceID")
            var referenceID: UUID

    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", "", "","","","",0,"",0, "",UUID.randomUUID())
    }
}