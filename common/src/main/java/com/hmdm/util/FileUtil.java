/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.util;

import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Customer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>An utility class for managing the files on local file system.</p>
 *
 * @author isv
 */
public final class FileUtil {
    private static final String DELIMITER = "1111111";

    /**
     * <p>Constructs new <code>FileUtil</code> instance. This implementation does nothing.</p>
     */
    private FileUtil() {
    }

    public static void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
        try (FileOutputStream out = new FileOutputStream(new File(uploadedFileLocation))) {
            byte[] bytes = new byte[1024];

            int read;
            while((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            out.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Moves the specified uploaded file to desired location related to specified customer account.</p>
     *
     * @param customer a customer account which the file belongs to.
     * @param filesDirectory a directory which holds all the files maintained by the application.
     * @param localPath an optional local path to move file to.
     * @param tmpFilePath a path to a temorary file to be moved.
     * @return a file referencing the moved file if operation was successful; <code>null</code> otherwise.
     * @throws FileExistsException if file already exists in
     */
    public static File moveFile(Customer customer, String filesDirectory, String localPath, String tmpFilePath) {
        File localFile = new File(tmpFilePath);
        String fileName = localFile.getName().split(DELIMITER)[0];

        String filePath;
        if (localPath == null || localPath.isEmpty()) {
            filePath = String.format("%s/%s/%s", filesDirectory, customer.getFilesDir(), fileName);
        } else {
            filePath = String.format("%s/%s/%s/%s", filesDirectory, customer.getFilesDir(), localPath, fileName);
        }
        File file = new File(filePath.replace("/", File.separator));
        file.getParentFile().mkdirs();

        if (file.exists()) {
            throw new FileExistsException(customer, file.getName());
        }

        final boolean success = localFile.renameTo(file);
        if (success) {
            return file;
        } else {
            return null;
        }

    }

    public static String translateURLToLocalFilePath(Customer customer, Application application, String baseUrl) {
        // this.baseUrl + "/files/" + customer.getFilesDir() + "/" + movedFile.getName()
        final String url = application.getUrl();
        return translateURLToLocalFilePath(customer, url, baseUrl);
    }

    public static String translateURLToLocalFilePath(Customer customer, String url, String baseUrl) {
        final String prefixWithoutCustomer = baseUrl + "/files/";
        final String prefix = prefixWithoutCustomer + customer.getFilesDir() + "/";
        if (url.startsWith(prefix)) {
            final String path = url.substring(prefixWithoutCustomer.length());
            return path.replace("/", File.separator);
        }

        return null;
    }

    /**
     * <p>Deletes the specified file on local file system.</p>
     *
     * @param baseDirectory a path to a base directory where all application files are stored..
     * @param path a path to a file to delete.
     * @return <code>true</code> if file was deleted successfully; <code>false</code> otherwise.
     */
    public static boolean deleteFile(String baseDirectory, String path) {
        final File fileToDelete = new File(baseDirectory, path);
        return fileToDelete.delete();
    }
}
