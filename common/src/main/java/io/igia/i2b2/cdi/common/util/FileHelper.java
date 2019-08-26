/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.
 * 2.0 with a Healthcare Disclaimer.
 * A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
 * be found under the top level directory, named LICENSE.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * If a copy of the Healthcare Disclaimer was not distributed with this file, You
 * can obtain one at the project website https://github.com/igia.
 *
 * Copyright (C) 2018-2019 Persistent Systems, Inc.
 */
package io.igia.i2b2.cdi.common.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import liquibase.util.csv.opencsv.CSVWriter;

public class FileHelper {

	private static final Logger log = LoggerFactory.getLogger(FileHelper.class);

	private FileHelper() {

	}

	public static String[] getFilesInFolder(String folder) {
		File file = new File(folder);
		return file.list();
	}

	public static String unpack(ArrayList<Object> payload) {
		String zipDirectoryPath = getSourceZipPath(payload);

		try {
			ZipUtil.unpack(new File(zipDirectoryPath), new File(getDestinationPath(payload)));
			
			// Delete zip file after unpack
			File deleteFile = new File(zipDirectoryPath);
			if (!deleteFile.delete()) {
				log.error("Error while deleting zip file");
			}
		} catch (Exception e) {
			log.error("Error while unpacking zip file {}", e);
		}
		return zipDirectoryPath;
	}
	
	public static String toZipFolder(String sourceFolderPath, String errorLogFileExtn) {
		
		String file = new File(sourceFolderPath).getParent();
		File destZipDir = new File(file + ".zip");
		File newFileWithTxtExtn = new File(destZipDir + errorLogFileExtn);
		try {
			ZipUtil.pack(new File(file), destZipDir);
			
			//Appending .txt extension to .zip, as we are sending back file to source
			if(!destZipDir.renameTo(newFileWithTxtExtn)) {
				log.error("Error while appending log extn to .zip");
			}
			
			//Delete original folder after zip
			File deleteDir = new File(file);
			FileUtils.deleteDirectory(deleteDir);
		} catch (Exception e) {
			log.error("Error while packing as zip file {}", e);
		}
		return newFileWithTxtExtn.getAbsolutePath();
	}

	private static String getSourceZipPath(ArrayList<Object> payLoad) {
		File file = (File) payLoad.get(0);
		return file.getAbsolutePath();
	}

	private static String getDestinationPath(ArrayList<Object> payLoad) {
		File file = (File) payLoad.get(0);
		return file.getParent();
	}

	public static AppJobContextProperties getJobContextProperties(String path) {
		String errorRecordsDirPath = "";
		
		AppJobContextProperties properties = new AppJobContextProperties();
		path = path.substring(0, path.lastIndexOf('.'));
		
		File dir = new File(path);
		
		// For creating error records directory
		errorRecordsDirPath = path + "_report" + File.separator + dir.getName() + "_report" + File.separator;
		
		// Set project id
		properties.setProjectId(dir.getName());
		if (dir.exists() && dir.isDirectory()) {
			File[] arr = dir.listFiles();

			// Set source system cd
			properties.setSourceSystemCd(arr[0].getName());
			errorRecordsDirPath += arr[0].getName() + File.separator;
			properties.setErrorRecordsDirectoryPath(errorRecordsDirPath);
			properties.setLocalDataDirectoryPath(arr[0].getPath() + File.separator);
		}
		return properties;
	}

	public static void createMissingFiles(String baseDirectoryPath, Map<String, Boolean> fileMap) {

		File baseDir = new File(baseDirectoryPath);
		File[] files = baseDir.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (fileMap.containsKey(fileName)) {
				fileMap.put(fileName, true);
			}
		}
		for (Map.Entry<String, Boolean> entry : fileMap.entrySet()) {
			if (!entry.getValue()) {
				try (CSVWriter csvWriter = new CSVWriter(new FileWriter(baseDirectoryPath + entry.getKey()))) {
					/**
					 * This is empty because, we are creating missing (empty)
					 * files
					 */
				} catch (IOException e) {
					log.error("Error while creating missing files. {} ", e);
				}
			}
		}
	}

	public static void createErrorRecordsDirectory(String errorRecordsDirectoryPath) {
		File file = new File(errorRecordsDirectoryPath);
		file.mkdirs();
	}
}
