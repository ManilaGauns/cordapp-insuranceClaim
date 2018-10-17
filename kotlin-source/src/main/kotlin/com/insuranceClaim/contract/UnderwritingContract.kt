package com.insuranceClaim.contract

import com.insuranceClaim.state.ClaimState
import com.insuranceClaim.state.UnderwritingState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

/**
 * This contract enforces rules regarding the creation of a valid [BorrowerAndBankState] and [BankAndCreditAgencyState].
 *
 * For a new [Claim] to be created onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [Claim].
 * - An Create() command with the public keys of both the party.
 *
 *  For verified [Claim] to be created onto the ledger, a transaction is required which takes:
 * - One input states: the old [Claim].
 * - One output state: the new [Claim].
 *
 * All contracts must sub-class the [Contract] interface.
 */

class UnderwritingContract : Contract {
    companion object {
        @JvmStatic
            val UNDERWRITING_CONTRACT_ID = "com.insuranceClaim.contract.UnderwritingContract"
    }

        /**
         * The verify() function of all the states' contracts must not throw an exception for a transaction to be
         * considered valid.
         */

        override fun verify(tx: LedgerTransaction) {
            val command = tx.commands.requireSingleCommand<UnderwritingContract.Commands>()
            when (command.value) {
                is UnderwritingContract.Commands.Underwriting -> {
                    requireThat {
                        // Generic constraints around the Underwriter transaction.
                        "Underwriting Transaction should have one input." using (tx.inputs.size==1)
                        "Two output states should be created." using (tx.outputs.size == 2)
                        val out = tx.outputsOfType<UnderwritingState>().single()
                        "The Insurance Company and the Underwriter Party cannot be same entity." using (out.insurerNode != out.underwriterNode)
                        "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                    }
                }

                is UnderwritingContract.Commands.UnderwritingEvaluation -> {
                    requireThat {
                        // Generic constraints around the Underwriter transaction.
                        "Only one output state should be created." using (tx.outputs.size == 1)
                        val input = tx.inputsOfType<UnderwritingState>().single()
                        val out = tx.outputsOfType<UnderwritingState>().single()
                        "The Insurance Company and the Underwriter Party cannot be same entity." using (out.insurerNode != out.underwriterNode)
                        "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                    }
                }
                is UnderwritingContract.Commands.UnderwritingStatus -> {
                    requireThat {
                        // Generic constraints around the Underwriter transaction.
                        "Underwriting Status Transaction should have Two input." using (tx.inputs.size==2)
                    }
                }
            }
        }

        /**
         * This contract implements two commands.
         */
        interface Commands : CommandData {
            class Underwriting : Commands
            class UnderwritingEvaluation : Commands
            class UnderwritingStatus : Commands
        }
    }
