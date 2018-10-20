# The Insurance Claim CorDapp

This CorDapp is an example of how Insurance Claim can be settled confidentially. For example, an Applicant can submit an Insurance claim
with the Insurance Company giving required details and claim Amount. The Insurance Company can then forward the Claim request to the Underwriting agency.
Communication between any of the Parties or any data exchanged remains private to Parties. Applicant does not hear what Insurance Company and Underwriter does.

The CorDapp includes:

* A Claim State definition that records the Applicant details required by the Insurance Company.
* An Underwriter State definition to handle the underwriting process.
* Two contracts, one that facilitates the verification of Insurance Claim and other that verifies the Underwriting process.
* Four flows for creating Insurance Claim Application, Forwarding the Application to Underwriting, Underwriter verifying it and responding with the Approved claim amount and finally Insurance Companny reverting the approval details to the Applicant

The CorDapp allows you to create an Insurance Claim and get it approved by the Underwriter. The Insurance Company can further carry out the Underwriting Process.
The CorDapp also comes with an API and UI layer that allows you to do all of the aforementioned things.

# Instructions for setting up

1. `git clone https://github.com/Manila-Gauns/Cordapp-InsuranceClaim
2. `cd cordapp-insuranceClaim`
3. `./gradlew deployNodes` - building may take upto a minute (it's much quicker if you already have the Corda binaries)./r
4. `cd kotlin-source/build/nodes`
5. `./runnodes`

At this point you will have a notary node running as well as three other nodes and their corresponding webservers. There should be 7 console windows in total. One for the notary and two for each of the three nodes. The nodes take about 20-30 seconds to finish booting up.

# Using the CorDapp via the web front-end, navigate to:

1. Applicant: `http://localhost:10009`
2. Insurance Company: `http://localhost:10012`
3. Underwriter: `http://localhost:10015`

You'll see a basic page, listing all the API end-points and static web content. Click on the "insuranceClaimWeb" link under
"static web content". The dashboard shows you a number of things:

1. All Insurance Application till date
2. A button to Apply for new Insurance


## Create New Insurance

1. Click on the "Apply" button.
2. Fill in all the required details (Guidelines to fill the form given below)
3. Click "Apply"
4. Wait for the transaction confirmation
5. After the transaction message pops up, click anywhere
6. The UI should update to reflect the new Insurance Application details
7. Navigate to the Insurance Company's dashboard. You should see the same Application details there

## Guidelines to fill Claim Application Form

The Claim Application Form consists of following fields : 

1. FirstName - first name of the Applicant. (String) e.g. Alex
2. LastName - last name of the Applicant. (String) e.g. Gomez
3. Address - Residential Address of the Applicant. (String) e.g. Street 16/A, LA. 
4. Insurance ID - ID of the insurance for which the Applicant wants to apply the Claim for. (AlphaNumeric) e.g. ID345667
5. Insurance Type - Type of Insurance (Choose from the drop-down)
6. Claim Amount - Insurance amount to be claimed. (Integer) e.g 30000
7. Claim Reason - The reason for which the Applicant wants to Claim the insurance (String) e.g Car Accident

## Insurance Company forwarding Application to Underwriter

1. Navigate to the Insurance Company's dashboard
2. Select the Application to start the Underwriting Process
3. Click on the "Underwriting" button
4. Details popup will appear where the Applicant's details are displayed
5. Click "Send"
6. Wait for the transaction confirmation
7. After transaction message popup, click anywhere
8. The UI should update to reflect the new Application under 'Insurance Applications Pending for Underwriting'
9. Navigate to the Underwriter's dashboard. You should see the same Application details there

## Underwriting Evaluation and Responding to Company

1. Navigate to the Underwriter's dashboard
2. Select the Application to view the details
3. Click on the "Send Response" button
4. Application details pop-up will appear
5. Click "Send"
6. Wait for the transaction confirmation
7. After transaction message popup, click anywhere
8. The UI should update to reflect the Approved Amount and Insurance Status for the selected Application under 'Insurance Applications'
9. Navigate to the Insurance Company's dashboard. You should see the same Application details there

## Insurance Company's Response to Applicant

1. Navigate to the Insurance Company's dashboard
2. Select the Application to view the details
3. Click on the "Send Response" button
4. Application details pop-up will appear
5. Click "Send"
6. Wait for the transaction confirmation
7. After transaction message popup, click anywhere
8. The UI should update to reflect the changes under 'Insurance Applications'
9. Navigate to the Applicant's dashboard. You should see the same Application details there

That's it!

Feel free to submit a PR.
