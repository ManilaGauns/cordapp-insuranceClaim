package com.insuranceClaim.flow

import co.paralleluniverse.fibers.Suspendable
import com.insuranceClaim.contract.ClaimContract
import com.insuranceClaim.state.ClaimState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object ApplyClaimFlow {
    @InitiatingFlow
    @StartableByRPC
    class ClaimInitiator(val insurerNode: Party,
                             val value: Int,
                             val reason: String,
                             val fname: String,
                             val lname: String,
                             val address: String,
                             val insuranceID: String,
                             val type: String,
                             val insuranceStatus: String) : FlowLogic<SignedTransaction>() {

        /**
         * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
         * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
         */
        companion object {
            object CLAIM_APPLY : ProgressTracker.Step("Applicant sends Insurance application to the Company")
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object INSURANCE_UNDERWRITER : ProgressTracker.Step("Insurance Company forwards application to their Underwriter ")
            object INSURANCE_UNDERWRITER_EVALUATION : ProgressTracker.Step("Underwriter evaluates the Application and responds to Insurance Company")
            object COMPANY_RESPONSE : ProgressTracker.Step("Insurance Company responds to Applicant")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    CLAIM_APPLY,
                    VERIFYING_TRANSACTION,
                    INSURANCE_UNDERWRITER,
                    INSURANCE_UNDERWRITER_EVALUATION,
                    COMPANY_RESPONSE,
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
            progressTracker.currentStep = CLAIM_APPLY
            // Generate an unsigned transaction.
            val referenceID:String = "Not Defined"
            val approvedAmount=0
            val claimState = ClaimState(serviceHub.myInfo.legalIdentities.first(),insurerNode,fname,lname,address,insuranceID,type,value,reason,approvedAmount,insuranceStatus,referenceID)
            val initiateClaimCommand = Command(ClaimContract.Commands.ClaimApplication(),claimState.participants.map { it.owningKey })
            val txBuilder = TransactionBuilder(notary)
                    .addOutputState(claimState, ClaimContract.CLAIM_CONTRACT_ID)
                    .addCommand(initiateClaimCommand)

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
            // Send the state to the Insurance Company, and receive it back with their signature.
            val otherPartyFlow = initiateFlow(insurerNode)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(otherPartyFlow), GATHERING_SIGS.childProgressTracker()))

            // Stage 5.
            progressTracker.currentStep = FINALISING_TRANSACTION
            // Notarise and record the transaction in both parties' vaults.
            return subFlow(FinalityFlow(fullySignedTx, FINALISING_TRANSACTION.childProgressTracker()))
        }
    }

    @InitiatedBy(ClaimInitiator::class)
    class Acceptor(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be a Claim Application transaction." using (output is ClaimState)
                    val out = output as ClaimState
                    "Policy Amount should be greater than 0." using (out.value > 0)
                }
            }
            return subFlow(signTransactionFlow)
        }
    }
}