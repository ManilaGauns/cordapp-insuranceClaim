package com.insuranceClaim.state

import com.insuranceClaim.schema.UnderwritingSchemaVI
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * The state object recording Claim applications between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param value the amount of the Claim.
 * @param insurerNode the party issuing the Claim (Insurance Company).
 * @param underwriterNode the Undertaking party for the Claim.
 */

data class UnderwritingState(
                val insurerNode: Party,
                val underwriterNode: Party,
                val fname: String,
                val lname: String,
                val insuranceID: String,
                val type: String,
                val value:Int,
                val reason: String,
                var approvedAmount: Int,
                var insuranceStatus: String,
                override var linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState {
    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(insurerNode, underwriterNode)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is UnderwritingSchemaVI -> UnderwritingSchemaVI.PersistentUnderwriting(
                    this.insurerNode.name.toString(),
                    this.underwriterNode.name.toString(),
                    this.fname,
                    this.lname,
                    this.insuranceID,
                    this.type,
                    this.value,
                    this.reason,
                    this.approvedAmount,
                    this.insuranceStatus,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(UnderwritingSchemaVI)
}
