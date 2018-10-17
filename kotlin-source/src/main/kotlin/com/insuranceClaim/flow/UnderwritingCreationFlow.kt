package com.insuranceClaim.flow

import co.paralleluniverse.fibers.Suspendable
import com.insuranceClaim.contract.ClaimContract
import com.insuranceClaim.contract.UnderwritingContract
import com.insuranceClaim.state.ClaimState
import com.insuranceClaim.state.UnderwritingState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker


object UnderwritingCreationFlow {
    @InitiatingFlow
    @StartableByRPC
    class UnderwritingInitiator(val underwriterNode: Party,
                                val fname: String,
                                val lname: String,
                                val insuranceID: String,
                                val type: String,
                                val value:Int,
                                val reason: String,
                                var insuranceStatus: String,
                                val claimID: String) : FlowLogic<SignedTransaction>() {


        companion object {
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object INSURANCE_UNDERWRITER : ProgressTracker.Step("Insurance Company forwards application to their Underwriter ")
            object INSURANCE_UNDERWRITER_EVALUATION : ProgressTracker.Step("Underwriter evaluates the Application and responds to Insurance Company")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    VERIFYING_TRANSACTION,
                    INSURANCE_UNDERWRITER,
                    INSURANCE_UNDERWRITER_EVALUATION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGS,
                    FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        override fun call(): SignedTransaction {
            // Obtain a reference to the notary we want to use.
            val notary = serviceHub.networkMapCache.notaryIdentities[0];


            // Stage 1.
            progressTracker.currentStep = INSURANCE_UNDERWRITER
            // Generate an unsigned transaction.
            var approvedAmount=0
            insuranceStatus="PENDING"

            val inputClaimState =serviceHub.vaultService.queryBy<ClaimState>()
                    .states.singleOrNull{it.state.data.linearId.id.toString()==claimID} ?: throw FlowException("No state found in the vault")
            val applicantNode=inputClaimState.state.data.applicantNode
            val address=inputClaimState.state.data.address
            val referenceID=inputClaimState.state.data.referenceID
            val id=inputClaimState.state.data.linearId
            val newApplicationState= ClaimState(applicantNode,serviceHub.myInfo.legalIdentities.first(),fname,lname,address,insuranceID,type,value,reason,approvedAmount,"PENDING",referenceID,id)

            val underwritingState = UnderwritingState(serviceHub.myInfo.legalIdentities.first(), underwriterNode, fname,lname,insuranceID,type,value,reason,approvedAmount,insuranceStatus)

            val inputCommand = Command(ClaimContract.Commands.ClaimTest(),newApplicationState.participants.map{it.owningKey})
            val txCommand = Command(UnderwritingContract.Commands.Underwriting(), underwritingState.participants.map { it.owningKey })
            val txBuilder = TransactionBuilder(notary)
                    .addInputState(inputClaimState)
                    .addOutputState(underwritingState, UnderwritingContract.UNDERWRITING_CONTRACT_ID)
                    .addOutputState(newApplicationState,ClaimContract.CLAIM_CONTRACT_ID)
                    .addCommand(txCommand)
                    .addCommand(inputCommand)

            // Stage 2.
            progressTracker.currentStep = VERIFYING_TRANSACTION
            // Verify that the transaction is valid.
            txBuilder.verify(serviceHub)

            // Stage 3.
            progressTracker.currentStep = SIGNING_TRANSACTION
            // Sign the transaction.
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            // Stage 4.
            progressTracker.currentStep = GATHERING_SIGS
            // Send the state to the counterparty, and receive it back with their signature.
            val underwriterPartyFlow = initiateFlow(underwriterNode)
            val applicantPartyFlow=initiateFlow(applicantNode)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(underwriterPartyFlow,applicantPartyFlow),GATHERING_SIGS.childProgressTracker()))

            // Stage 5.
            progressTracker.currentStep = FINALISING_TRANSACTION
            // Notarise and record the transaction in both parties' vaults.
            return subFlow(FinalityFlow(fullySignedTx, FINALISING_TRANSACTION.childProgressTracker()))

        }
    }

    @InitiatedBy(UnderwritingInitiator::class)
    class UnderwritingAcceptor(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                }
            }
            return subFlow(signTransactionFlow)
        }
    }
}