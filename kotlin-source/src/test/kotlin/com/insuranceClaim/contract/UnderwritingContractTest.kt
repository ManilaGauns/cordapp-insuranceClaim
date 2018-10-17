package com.insuranceClaim.contract

import com.insuranceClaim.state.ClaimState
import com.insuranceClaim.state.UnderwritingState
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class UnderwritingContractTest {
    private val ledgerServices = MockServices()
    private val underwriterA = TestIdentity(CordaX500Name("UnderwriterA", "Paris", "FR"))
    private val InsurerA = TestIdentity(CordaX500Name("InsurerA", "New York", "US"))
    private val applicantA = TestIdentity(CordaX500Name("ApplicantA", "London", "GB"))
    val fname= "PartyA"
    val lname= "Test"
    val address="Mumbai, India"
    val insuranceID = "ID123"
    val type="Life Insurance"
    val value = 30000
    val reason = "Test application"
    val approvedamount=0
    val insuranceStatus="RECEIVED"
    val referenceID="testID"

    @Test
    fun `Transaction must include Underwriting command`(){
        ledgerServices.ledger{
            transaction{
                input(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.Underwriting())
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimTest())
                verifies()
            }
        }
    }

    @Test
    fun `Underwriting transaction must have zero input`(){
        ledgerServices.ledger{
            transaction{
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                input(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.Underwriting())
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimTest())
                `fails with`("Underwriting Transaction should have one input.")
            }
        }
    }

    @Test
    fun `Underwriting transaction must have Two output states`(){
        ledgerServices.ledger{
            transaction{
                input(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.Underwriting())
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimTest())
                `fails with`("Two output states should be created.")
            }
        }
    }

    @Test
    fun `Insurance Company and Underwriter Party cannot be same entity`(){
        ledgerServices.ledger{
            transaction{
                input(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,InsurerA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.Underwriting())
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimTest())
                `fails with`("The Insurance Company and the Underwriter Party cannot be same entity")
            }
            transaction{
                input(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(underwriterA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.Underwriting())
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimTest())
                `fails with`("The Insurance Company and the Underwriter Party cannot be same entity")
            }
        }
    }

    @Test
    fun `All Participants must sign the transaction`(){
        ledgerServices.ledger{
            transaction{
                input(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(underwriterA.publicKey, UnderwritingContract.Commands.Underwriting())
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimTest())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(InsurerA.publicKey, UnderwritingContract.Commands.Underwriting())
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimTest())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(InsurerA.publicKey,InsurerA.publicKey), UnderwritingContract.Commands.Underwriting())
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimTest())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(underwriterA.publicKey,underwriterA.publicKey), UnderwritingContract.Commands.Underwriting())
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimTest())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(ClaimContract.CLAIM_CONTRACT_ID, ClaimState(applicantA.party,InsurerA.party,fname,lname,address,insuranceID,type,value,reason,approvedamount,insuranceStatus,referenceID))
                command(listOf(InsurerA.publicKey,underwriterA.publicKey), UnderwritingContract.Commands.Underwriting())
                command(listOf(applicantA.publicKey, InsurerA.publicKey), ClaimContract.Commands.ClaimTest())
                verifies()
            }
        }
    }


    @Test
    fun `Transaction must include UnderwritingEvaluation command`(){
        ledgerServices.ledger{
            transaction{
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingEvaluation())
                verifies()
            }
        }
    }

    @Test
    fun `Underwriting Evaluation transaction must have only one output state`(){
        ledgerServices.ledger{
            transaction{
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingEvaluation())
                `fails with`("Only one output state should be created.")
            }
        }
    }

    @Test
    fun `Insurance Company and Underwriter Party cannot be the same entity`(){
        ledgerServices.ledger{
            transaction{
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,InsurerA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,InsurerA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingEvaluation())
                `fails with`("The Insurance Company and the Underwriter Party cannot be same entity")
            }
            transaction{
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,InsurerA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(underwriterA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                command(listOf(underwriterA.publicKey, InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingEvaluation())
                `fails with`("The Insurance Company and the Underwriter Party cannot be same entity")
            }
        }
    }

    @Test
    fun `All Participants must sign transaction`(){
        ledgerServices.ledger{
            transaction{
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,InsurerA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                command(underwriterA.publicKey, UnderwritingContract.Commands.UnderwritingEvaluation())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,InsurerA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                command(InsurerA.publicKey, UnderwritingContract.Commands.UnderwritingEvaluation())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,InsurerA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                command(listOf(InsurerA.publicKey,InsurerA.publicKey), UnderwritingContract.Commands.UnderwritingEvaluation())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,InsurerA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                command(listOf(underwriterA.publicKey,underwriterA.publicKey), UnderwritingContract.Commands.UnderwritingEvaluation())
                `fails with`("All of the participants must be signers.")
            }
            transaction{
                input(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,InsurerA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                output(UnderwritingContract.UNDERWRITING_CONTRACT_ID, UnderwritingState(InsurerA.party,underwriterA.party,fname,lname,insuranceID,type,value,reason,approvedamount,insuranceStatus))
                command(listOf(InsurerA.publicKey,underwriterA.publicKey), UnderwritingContract.Commands.UnderwritingEvaluation())
                verifies()
            }
        }
    }
}