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

object ClaimResponseFlow {
    @InitiatingFlow
    @StartableByRPC
    class ClaimResponseInitiator(val applicantNode: Party,
                                   val fname: String,
                                   val lname: String,
                                   val approvedAmount: Int,
                                   val insuranceStatus: String,
                                   val insuranceID: String) : FlowLogic<SignedTransaction>() {

        companion object {
            object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
            object COMPANY_RESPONSE : ProgressTracker.Step("Insurance Company responds to Applicant")
            object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
            object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    VERIFYING_TRANSACTION,
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
            progressTracker.currentStep = COMPANY_RESPONSE
            // Generate an unsigned transaction.
            val inputCompanyResponseState = serviceHub.vaultService.queryBy<ClaimState>().states.singleOrNull{ it.state.data.insuranceID == insuranceID } ?: throw FlowException("No state found in the vault")
            val value= inputCompanyResponseState.state.data.value
            val address = inputCompanyResponseState.state.data.address
            val claimID =inputCompanyResponseState.state.data.linearId
            val type=inputCompanyResponseState.state.data.type
            val reason=inputCompanyResponseState.state.data.reason

            val inputUnderwritingState = serviceHub.vaultService.queryBy<UnderwritingState>().states.singleOrNull{ it.state.data.insuranceID == insuranceID } ?: throw FlowException("No state found in the vault")
            val referenceID=inputUnderwritingState.state.data.linearId.id.toString()
            val underwriterNode= inputUnderwritingState.state.data.underwriterNode
            val id= inputUnderwritingState.state.data.linearId
            val status= "$insuranceStatus ,Sent"
            val outputUnderwritingStateRef= UnderwritingState(serviceHub.myInfo.legalIdentities.first(),underwriterNode,fname,lname,insuranceID,type,value,reason,approvedAmount,status,id)
            val claimState = ClaimState(applicantNode,serviceHub.myInfo.legalIdentities.first(),fname,lname,address,insuranceID,type,value,reason,approvedAmount, insuranceStatus, referenceID,claimID)
            val txCommand = Command(ClaimContract.Commands.ClaimResponse(), claimState.participants.map { it.owningKey })
            val statusCommand=Command(UnderwritingContract.Commands.UnderwritingStatus(),outputUnderwritingStateRef.participants.map { it.owningKey })

            val txBuilder = TransactionBuilder(notary)
                    .addOutputState(claimState, ClaimContract.CLAIM_CONTRACT_ID)
                    .addOutputState(outputUnderwritingStateRef,UnderwritingContract.UNDERWRITING_CONTRACT_ID)
                    .addInputState(inputCompanyResponseState)
                    .addInputState(inputUnderwritingState)
                    .addCommand(txCommand)
                    .addCommand(statusCommand)

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
            val otherPartyFlow = initiateFlow(applicantNode)
            val underwriterPartyFlow=initiateFlow(underwriterNode)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(otherPartyFlow,underwriterPartyFlow), GATHERING_SIGS.childProgressTracker()))

            // Stage 5.
            progressTracker.currentStep = FINALISING_TRANSACTION
            // Notarise and record the transaction in both parties' vaults.
            return subFlow(FinalityFlow(fullySignedTx, FINALISING_TRANSACTION.childProgressTracker()))

        }
    }

    @InitiatedBy(ClaimResponseInitiator::class)
    class ClaimResponseAcceptor(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
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