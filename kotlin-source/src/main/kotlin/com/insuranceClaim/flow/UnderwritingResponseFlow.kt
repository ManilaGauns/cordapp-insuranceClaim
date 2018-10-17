package com.insuranceClaim.flow

import co.paralleluniverse.fibers.Suspendable
import com.insuranceClaim.contract.UnderwritingContract
import com.insuranceClaim.state.UnderwritingState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object UnderwritingResponseFlow {
    @InitiatingFlow
    @StartableByRPC
    class UnderwritingEvaluationInitiator(val insurerNode: Party,
                                          val referenceID: String,
                                          val insuranceStatus: String,
                                          val fname: String,
                                          val lname: String,
                                          val value: Int,
                                          val approvedAmount: Int) : FlowLogic<SignedTransaction>() {

        companion object {
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
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
            progressTracker.currentStep = INSURANCE_UNDERWRITER_EVALUATION
            // Generate an unsigned transaction.
            val referenceId = UniqueIdentifier.fromString(referenceID)
            var inputUnderwritingState = serviceHub.vaultService.queryBy<UnderwritingState>().states.singleOrNull{ it.state.data.insuranceStatus == "PENDING" && it.state.data.linearId.id.toString() == referenceId.id.toString() } ?: throw FlowException("No state found in the vault")
            val insuranceID=inputUnderwritingState.state.data.insuranceID
            val type=inputUnderwritingState.state.data.type
            var reason=inputUnderwritingState.state.data.reason

            val underwritingState = UnderwritingState(insurerNode,serviceHub.myInfo.legalIdentities.first(), fname,lname,insuranceID,type,value,reason,approvedAmount,insuranceStatus,referenceId)
            val ApplicantName: String = "$fname $lname"
            //Check for Defaulter list
            val defaulter = arrayListOf("Borrower One", "Borrower Two","Borrower Three")
            if(defaulter.contains(ApplicantName)) {
                underwritingState.insuranceStatus="REJECTED"
            }
            else {
                underwritingState.insuranceStatus="ACCEPTED"
            }


            val txCommand = Command(UnderwritingContract.Commands.UnderwritingEvaluation(), underwritingState.participants.map { it.owningKey })
            val txBuilder = TransactionBuilder(notary)
                    .addOutputState(underwritingState, UnderwritingContract.UNDERWRITING_CONTRACT_ID)
                    .addInputState(inputUnderwritingState)
                    .addCommand(txCommand)

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
            val otherPartyFlow = initiateFlow(insurerNode)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(otherPartyFlow), GATHERING_SIGS.childProgressTracker()))

            // Stage 5.
            progressTracker.currentStep = FINALISING_TRANSACTION
            // Notarise and record the transaction in both parties' vaults.
            return subFlow(FinalityFlow(fullySignedTx, FINALISING_TRANSACTION.childProgressTracker()))

        }
    }

    @InitiatedBy(UnderwritingEvaluationInitiator::class)
    class UnderwritingEvaluationAcceptor(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be a Insurance Company and Underwriter transaction." using (output is UnderwritingState)
                }
            }
            return subFlow(signTransactionFlow)
        }
    }
}