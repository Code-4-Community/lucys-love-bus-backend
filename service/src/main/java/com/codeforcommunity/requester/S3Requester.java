package com.codeforcommunity.requester;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.codeforcommunity.aws.EncodedImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class S3Requester {

  private static final String BUCKET_LLB_PUBLIC_URL = "https://lucys-love-bus-public.s3.us-east-2.amazonaws.com";
  private static final String BUCKET_LLB_PUBLIC = "lucys-love-bus-public";
  private static final String DIR_LLB_PUBLIC_EVENTS = "events";

  private static AmazonS3 s3Client;

  static {
    s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2).build();
  }

  /**
   * Validates whether or not the given String is a base64 encoded image
   * in the following format: data:image/{extension};base64,{imageData}.
   *
   * @param base64Image the potential encoding of the image.
   * @return null if the String is not an encoded base64 image, otherwise an {@link EncodedImage}.
   */
  private static EncodedImage validateBase64Image(String base64Image) {

    // Expected Base64 format: data:image/{extension};base64,{imageData}

    if (base64Image == null || base64Image.length() < 5) {
      return null;
    }

    String[] base64ImageSplit = base64Image.split(",", 2); // Split the metadata from the image data
    if (base64ImageSplit.length != 2) {
      return null;
    }

    String meta = base64ImageSplit[0];  // The image metadata (e.g. "data:image/png;base64")
    String[] metaSplit = meta.split(";", 2);  // Split the metadata into data type and encoding type

    if (metaSplit.length != 2 || !metaSplit[1].equals("base64")) {
      // Ensure the encoding type is base64
      return null;
    }

    String[] dataSplit = metaSplit[0].split(":", 2);  // Split the data type
    if (dataSplit.length != 2) {
      return null;
    }

    String[] data = dataSplit[1].split("/", 2);   // Split the image type here (e.g. "image/png")
    if (data.length != 2 || !data[0].equals("image")) {
      // Ensure the encoded data is an image
      return null;
    }

    String fileExtension = data[1];  // The image type (e.g. "png")
    String encodedImage = base64ImageSplit[1];  // The encoded image

    return new EncodedImage("image", fileExtension, encodedImage);
  }

  /**
   * Validate the given base64 encoding of an image and upload it to the LLB public S3 bucket.
   *
   * @param fileName       the desired name of the new file in S3 (without a file extension).
   * @param directoryName  the desired directory of the file in S3 (without leading or trailing '/').
   * @param base64Encoding the base64 encoding of the image to upload.
   * @return null if the encoding fails validation and image URL if the upload was successful.
   * @throws IOException            if the base64 decoding failed.
   * @throws AmazonServiceException if the upload to S3 failed.
   */
  private static String validateBase64ImageAndUploadToS3(String fileName, String directoryName, String base64Encoding) throws IOException, AmazonServiceException {
    EncodedImage encodedImage = validateBase64Image(base64Encoding);
    if (encodedImage == null) {
      return null;  // Image failed to validate
    }

    String fullFileName = String.format("%s.%s", fileName, encodedImage.getFileExtension());

    // Temporarily writes the image to disk to decode
    byte[] imageData = Base64.getDecoder().decode(encodedImage.getBase64ImageEncoding());
    File tempFile = File.createTempFile(fullFileName, null, null);
    FileOutputStream fos = new FileOutputStream(tempFile);
    fos.write(imageData);
    fos.flush();
    fos.close();

    // Create the request to upload the image
    PutObjectRequest awsRequest = new PutObjectRequest(BUCKET_LLB_PUBLIC, directoryName + "/" + fullFileName, tempFile);
    awsRequest.setCannedAcl(CannedAccessControlList.PublicRead);  // Set the image to be publicly available

    // Set the image file metadata
    ObjectMetadata awsObjectMetadata = new ObjectMetadata();
    awsObjectMetadata.setContentType(encodedImage.getFileType() + encodedImage.getFileExtension());  // Set file type to be an image
    awsRequest.setMetadata(awsObjectMetadata);

    // Perform the upload, throws AmazonServiceException if something goes wrong
    s3Client.putObject(awsRequest);

    // Delete the temporary file that was written to disk
    tempFile.delete();

    return String.format("%s/%s/%s", BUCKET_LLB_PUBLIC_URL, directoryName, fullFileName);
  }

  /**
   * Validate the given base64 encoding of an image and upload it to the LLB public S3 bucket for Events.
   *
   * @param eventTitle     the title of the Event.
   * @param base64Encoding the encoded image to upload.
   * @return null if the encoding fails validation and image URL if the upload was successful.
   * @throws IOException            if the base64 decoding failed.
   * @throws AmazonServiceException if the upload to S3 failed.
   */
  public static String validateUploadImageToS3LucyEvents(String eventTitle, String base64Encoding) throws IOException, AmazonServiceException {
    String fileName = getImageFileNameWithoutExtension(eventTitle);
    return validateBase64ImageAndUploadToS3(fileName, DIR_LLB_PUBLIC_EVENTS, base64Encoding);
  }

  /**
   * Removes special characters, replaces spaces, and appens "_thumbnail".
   *
   * @param eventTitle the title of the event.
   * @return the String for the image file name (without the file extension).
   */
  public static String getImageFileNameWithoutExtension(String eventTitle) {
    String title = eventTitle.replaceAll("[!@#$%^&*()=+./\\\\|<>`~\\[\\]{}?]", "");  // Remove special characters
    return title.replace(" ", "_").toLowerCase() + "_thumbnail";  // The desired name of the file in S3
  }
}