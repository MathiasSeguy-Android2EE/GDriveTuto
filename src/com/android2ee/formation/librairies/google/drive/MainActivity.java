/**<ul>
 * <li>GDriveTuto</li>
 * <li>com.android2ee.formation.librairies.google.drive</li>
 * <li>1 août 2014</li>
 * 
 * <li>======================================================</li>
 *
 * <li>Projet : Mathias Seguy Project</li>
 * <li>Produit par MSE.</li>
 *
 /**
 * <ul>
 * Android Tutorial, An <strong>Android2EE</strong>'s project.</br> 
 * Produced by <strong>Dr. Mathias SEGUY</strong>.</br>
 * Delivered by <strong>http://android2ee.com/</strong></br>
 *  Belongs to <strong>Mathias Seguy</strong></br>
 ****************************************************************************************************************</br>
 * This code is free for any usage except training and can't be distribute.</br>
 * The distribution is reserved to the site <strong>http://android2ee.com</strong>.</br>
 * The intelectual property belongs to <strong>Mathias Seguy</strong>.</br>
 * <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * 
 * *****************************************************************************************************************</br>
 *  Ce code est libre de toute utilisation mais n'est pas distribuable.</br>
 *  Sa distribution est reservée au site <strong>http://android2ee.com</strong>.</br> 
 *  Sa propriété intellectuelle appartient à <strong>Mathias Seguy</strong>.</br>
 *  <em>http://mathias-seguy.developpez.com/</em></br> </br>
 * *****************************************************************************************************************</br>
 */
package com.android2ee.formation.librairies.google.drive;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Mathias Seguy (Android2EE)
 * @goals
 *        This class aims to show how to use GoogleDrive using the GooglePlayService
 *        This need to generate a OAuth new Client ID in the Google service console
 *        And also include the googlePlayService as a library
 */
public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
	/**
	 * The GoogleApiClient
	 */
	GoogleApiClient mGoogleApiClient;
	/**
	 * A boolean to know if we are trying to resolve a connection with GooglePlayService problem
	 */
	boolean mResolvingError = false;
	/**
	 * The request code when trying to resolve a problem
	 */
	private static final int REQUEST_RESOLVE_ERROR = 110274;

	/******************************************************************************************/
	/** Managing life cycle **************************************************************************/
	/******************************************************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// build the GoogleApiClient
		mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API)// Drive Api
				.addScope(Drive.SCOPE_FILE)// To see file of the app (only them)
				.addConnectionCallbacks(this)// this implements ConnectionCallbacks
				.addOnConnectionFailedListener(this)// this implements OnConnectionFailedListener
				.build();// and build
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect your GoogleApiClient (if not already trying to resolve a connection error)
		if (!mResolvingError) {
			Log.i("MainActivity", "onStart  mGoogleApiClient .connect() called" + mResolvingError);
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onStop() {
		// Disconnect your GoogleApiClient
		mGoogleApiClient.disconnect();
		super.onStop();
	}

	/******************************************************************************************/
	/** ConnectionCallbacks, OnConnectionFailedListener **************************************************************************/
	/******************************************************************************************/
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener#onConnectionFailed
	 * (com.google.android.gms.common.ConnectionResult)
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i("MainActivity", "onConnectionFailed " + result);
		if (mResolvingError) {
			Log.i("MainActivity", "onConnectionFailed mResolvingError " + mResolvingError);
			// Already attempting to resolve an error.
			return;
		} else if (result.hasResolution()) {
			// try to correct your exception
			Log.i("MainActivity", "onConnectionFailed result.hasResolution()" + mResolvingError);
			try {
				// tell you are trying
				mResolvingError = true;
				// try
				result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
			} catch (SendIntentException e) {
				// There was an error with the resolution intent. Try again.
				mGoogleApiClient.connect();
			}
		} else {
			// Show dialog using GooglePlayServicesUtil.getErrorDialog()
			Log.i("MainActivity", "onConnectionFailed No resolution " + result);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// called when the resolution is over
		Log.i("MainActivity", "onActivityResult  called");
		// ensure the call back is from the connection error
		if (requestCode == REQUEST_RESOLVE_ERROR) {
			// the resolution is over
			mResolvingError = false;
			if (resultCode == RESULT_OK) {
				Log.i("MainActivity", "onActivityResult  resultCode == RESULT_OK");
				// Make sure the app is not already connected or attempting to connect
				if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
					Log.i("MainActivity", "onActivityResult mGoogleApiClient.connect() called");
					// then connect (again, it should work)
					mGoogleApiClient.connect();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks#onConnected(android
	 * .os.Bundle)
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i("MainActivity", "onConnected called");
		// ok it's connected, let's test it
		new GDriveAsyncRestCall().execute(mGoogleApiClient);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks#onConnectionSuspended
	 * (int)
	 */
	@Override
	public void onConnectionSuspended(int cause) {
		Log.i("MainActivity", "onConnectionSuspended called");

	}

	/******************************************************************************************/
	/** Drive Methods **************************************************************************/
	/******************************************************************************************/

	/**
	 * @author Mathias Seguy (Android2EE)
	 * @goals
	 *        This class aims to make GDrive manipulations
	 *        It has to be asynchronous
	 */
	public static final class GDriveAsyncRestCall extends AsyncTask<GoogleApiClient, Void, Void> {

		/**
		 * The title of the folder that contains our files in the Drive of the user
		 */
		private static final String FOLDER_TITLE = "Android2EE GDriveTuto";

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected Void doInBackground(GoogleApiClient... params) {
			// This method creates a File, list all the file of the GDrive and read them
			GoogleApiClient mGoogleApiClient = params[0];
			Log.i("MainActivity", "will try to create a file");
			// Ask for a synch
			Drive.DriveApi.requestSync(mGoogleApiClient).await();
			// 1)Create a File
			// Define the content of the file
			/** It was before **/
			/**
			 * Contents contents =
			 * Drive.DriveApi.newContents(mGoogleApiClient).await().getContents();
			 * // Fill the content (can have a look at the method copyLocalFile(Contents contents)
			 * below)
			 * OutputStream outStream = contents.getOutputStream();
			 * BufferedWriter bufW = new BufferedWriter(new OutputStreamWriter(outStream));
			 * try {
			 * bufW.write("Android2EE DriveApiTutorial");
			 * bufW.flush();
			 * bufW.close();
			 * outStream.flush();
			 * outStream.close();
			 * } catch (IOException e) {
			 * e.printStackTrace();
			 * }
			 * //It was before
			 * //contents.close();
			 */
			DriveContentsResult driveContentResult = Drive.DriveApi.newDriveContents(mGoogleApiClient).await();
			// If the operation was not successful, we cannot do anything
			// and must
			// fail.
			if (!driveContentResult.getStatus().isSuccess()) {
				Log.i("MainActivity DriveApiTuto", "Failed to create new contents.");
				return null;
			}
			// Otherwise, we can write our data to the new contents.
			Log.i("MainActivity DriveApiTuto", "New contents created.");
			// Get an output stream for the contents.
			DriveContents content=driveContentResult.getDriveContents();
			OutputStream outStream = content.getOutputStream();
			// Write the bitmap data from it.
			BufferedWriter bufW = new BufferedWriter(new OutputStreamWriter(outStream));
			try {
				bufW.write("Android2EE DriveApiTutorial");
				bufW.flush();
				bufW.close();
				outStream.flush();
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Define the title of your file and its mime type
			MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle("Toto.txt")// Title
					.setMimeType("text/plain ")// Mime Type
					.build();// and build
			// Find the folder to create the file into
			DriveFolder driveFolder = findFolderOrCreateIt(mGoogleApiClient);
			// Create the file
			DriveId driveId;
			if (driveFolder != null) {
				// create the file in the folder Android2EE GDriveTuto of your user
				driveId = driveFolder// the folder in which create the file
						.createFile(mGoogleApiClient, changeSet, content)// create the file
						.await()// wait for the action finished
						.getDriveFile()// retrieve the file
						.getDriveId();// retrieve its id

			} else {// this code is never reached, it's just to show you
				// create the file in the root folder of your user
				driveId = Drive.DriveApi.getRootFolder(mGoogleApiClient)// find the root folder of
																		// the user's drive
						.createFile(mGoogleApiClient, changeSet, content)// create the file
						.await()// wait for the action finished
						.getDriveFile()// retrieve the file
						.getDriveId();// retrieve its id
			}
			Log.i("MainActivity", "creating a file" + driveId.getResourceId());

			// 2)List your file in the cloud
			// ---------------------------
			// request for the children into the root folder
			MetadataBufferResult result = Drive.DriveApi.getRootFolder(mGoogleApiClient)// the
																						// folder
					.listChildren(mGoogleApiClient)// request for your children
					.await();// wait the result
			Log.i("MainActivity", "onConnected called result status " + result.getStatus() + " size "
					+ result.getMetadataBuffer().getCount());
			// The Id of the file found
			DriveId fileDriveID;
			// browse the result
			for (Metadata data : result.getMetadataBuffer()) {
				Log.i("MainActivity", "data : " + data.getTitle());
				// find the Id
				fileDriveID = data.getDriveId();
				// 3) Retrieve and read the file
				if (!data.isFolder()) {
					// If it's a file, read it
					readGDriveFile(mGoogleApiClient, fileDriveID);
				}
				// 4) Then delete it => Don't know how it's possible
				// DriveFile driveFile =Drive.DriveApi.getFile(mGoogleApiClient, fileDriveID);
				// driveFile.???
				// 5)Pin a file (so it will be on the device and will be synchronized)
				// MetadataChangeSet metaDataSet = new
				// MetadataChangeSet.Builder().setPinned(true).build();
				// DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, fileDriveID);
				// driveFile.updateMetadata(mGoogleApiClient, metaDataSet);
			}
			return null;
		}

		private void createFile(DriveContentsResult result) {

		}

		/**
		 * Find or create a Folder based on its title
		 * 
		 * @param mGoogleApiClient
		 * @return The folder
		 */
		private DriveFolder findFolderOrCreateIt(GoogleApiClient mGoogleApiClient) {
			// The folder to return
			DriveFolder dfr = null;
			// build the query
			Query queryFolder = new Query.Builder()// query builder
					.addFilter(Filters.contains(SearchableField.TITLE, FOLDER_TITLE))// filter
					.build();// build
			// Run the query
			MetadataBufferResult driveFolderExistResult = Drive.DriveApi.getRootFolder(mGoogleApiClient)// the
																										// folder
					.queryChildren(mGoogleApiClient, queryFolder)// the query to run
					.await();// wait the result
			Log.i("MainActivity", "findFolderOrCreateIt query returns " + driveFolderExistResult);
			Log.i("MainActivity", "findFolderOrCreateIt query elements "
					+ driveFolderExistResult.getMetadataBuffer().getCount());
			// if a folder is found
			if (driveFolderExistResult.getStatus().isSuccess()
					&& driveFolderExistResult.getMetadataBuffer().getCount() > 0) {
				// retrieve its ID
				DriveId dId = driveFolderExistResult.getMetadataBuffer().get(0).getDriveId();
				// then retrieve it
				dfr = Drive.DriveApi.getFolder(mGoogleApiClient, dId);
			} else {
				// else create it
				// first define title and description
				MetadataChangeSet metaDataSetFolder = new MetadataChangeSet.Builder().setTitle(FOLDER_TITLE)
						.setDescription("A Place to store the file from the GDrive tutorial ").build();
				// then create
				dfr = Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient, metaDataSetFolder)
						.await().getDriveFolder();
			}
			return dfr;
		}

		/**
		 * Given a FileId, we open and read the file
		 * 
		 * @param mGoogleApiClient
		 * @param fileDriveID
		 */
		private void readGDriveFile(GoogleApiClient mGoogleApiClient, DriveId fileDriveID) {
			// if the file Id is not null
			if (fileDriveID != null) {
				// find it
				DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, fileDriveID);
				// open it ( third parameter is listener)
                DriveApi.DriveContentsResult driveFileResult = driveFile// the file
						.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)// the action
						.await();// wait for action completed
				// if it has worked
				if (driveFileResult.getStatus().isSuccess()) {
					// open the input stream
					InputStream is = driveFileResult.getDriveContents().getInputStream();
					// read it
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					String line;
					try {
						line = reader.readLine();
						while (line != null) {
							Log.i("MainActivity", "a new line has been read " + line);
							line = reader.readLine();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}


	}
}
