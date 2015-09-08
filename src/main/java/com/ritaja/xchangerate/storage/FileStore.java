package com.ritaja.xchangerate.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rsengupta on 03/09/15.
 */
public class FileStore extends DiskStore {
	protected String ratesFilename;

	public FileStore(String filenameAppender) {
		this.ratesFilename = System.getProperty("file.separator") + filenameAppender + "XchangeRates.json";
	}

	/**
	 * saves the exchange rates in a stored resource file
	 *
	 * @throws StorageException
	 */
	public void saveRates(JSONObject exchangeRates) throws StorageException {
		if (exchangeRates == null) {
			throw new StorageException("Cannot save null exchangeRates!");
		}
		try {
			FileWriter file = new FileWriter(resourceFilepath + ratesFilename);
			file.write(exchangeRates.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			throw new StorageException(e);
		}
	}

	/**
	 * Parses the exchange rates from the stored resource file
	 * and stores them as a JSONObject
	 *
	 * @throws StorageException
	 */
	public JSONObject loadRates() throws StorageException {
		// parse the JSON string from the resource file
		String jsonData = "";
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader(resourceFilepath + ratesFilename));
			while ((line = br.readLine()) != null) {
				jsonData += line + "\n";
			}
		} catch (IOException e) {
			throw new StorageException(e);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		// covert the parsed string to a JSON object
		try {
			return new JSONObject(jsonData);
		} catch (JSONException e) {
			throw new StorageException(e);
		}
	}

	/**
	 * check if this resource file exists
	 *
	 * @return boolean truth value
	 */
	public boolean resourceExists() {
		File f = new File(resourceFilepath + ratesFilename);
		if (f.exists() && !f.isDirectory()) {
			return true;
		}
		return false;
	}
}
