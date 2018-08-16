/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.customvision.samples;


import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.*;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.*;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.ImagePrediction;

import com.microsoft.azure.cognitiveservices.vision.customvision.training.*;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Samples {
    /**
     * The key for the bing api.
     */
    public static String trainingKey = "<your training key here>";
    private static final String basePath = "<enter-image-path>";
    
    private static List<File> hemlockImages; 
    private static List<File> japaneseCherryImages; 
    private static byte[] testImage;
    
    public static void main(String[] args) 
    {

    
    	//Authenticate Train API w/ training key
    	TrainingApi client = CustomVisionTrainingManager.authenticate(trainingKey);
    	
    	// Create a new project 
    	System.out.println("Creating new project:"); 
    	Trainings trainings = client.trainings();
    	Project project = trainings.createProject().withName("Flower Test").execute();
    	
    	
    	//create classification Tags
    	System.out.println("Create Tags"); 
    	Tag hemlockTag = trainings.createTag().withProjectId(project.id()).withName("Hemlock").execute();
    	Tag japaneseCherryTag = trainings.createTag().withProjectId(project.id()).withName("Japanese Cherry").execute();
    			
    	//Add some images to the tags 
    	System.out.println("Uploading images to tags"); 
    	Samples.LoadImagesFromDirectory();
    	
    	
    	//Upload images one at a time to Tag
    	for(File image : hemlockImages)
    	{
    		List<String> hemlockTagIds = new ArrayList<String>();
    		hemlockTagIds.add(hemlockTag.id().toString());
    		
    		try {
    			
        		byte[] fileContent = Files.readAllBytes(image.toPath());
        		trainings.createImagesFromData()
        		.withProjectId(project.id())
        		.withImageData(fileContent)
        		.withTagIds(hemlockTagIds )
        		.execute();
        		
    		} catch (Exception e) {
    			
    			System.out.println("Exception occurred: " + e );
    		}
   	    
    	}
    	
    	for(File img : japaneseCherryImages)
    	{
    		List<String> japaneseCherryTagIds = new ArrayList<String>();
    		japaneseCherryTagIds.add(japaneseCherryTag.id().toString());
    		
    		try {
    			
        		byte[] content = Files.readAllBytes(img.toPath());
        		trainings.createImagesFromData()
        		.withProjectId(project.id())
        		.withImageData(content)
        		.withTagIds(japaneseCherryTagIds )
        		.execute();
        		
    		} catch (Exception e) {
    			
    			System.out.println("Exception occurred: " + e );
    		}
   	    
    	}
    	
        // Now there are images with tags start training the project 
        System.out.println("Training"); 
        Iteration iteration = trainings.trainProject(project.id());

        // The returned iteration will be in progress, and can be queried periodically to see when it has completed 
        while (iteration.status().equals("Training") )
        { 
            try {
            	
				Thread.sleep(1000);
				
	            // Re-query the iteration to get it's updated status 
	            iteration = trainings.getIteration(project.id(), iteration.id());
				
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} 

            		 
        } 

         // The iteration is now trained. Make it the default project endpoint 
         iteration.withIsDefault(true);
         System.out.println("Done!\n"); 

    	
    	/*********************************************************************/
        /* 				PREDICTION						                     */
        /*********************************************************************/
         
		 // Now there is a trained endpoint, it can be used to make a prediction 
         

         // Add your prediction key from the settings page of the portal 
         // The prediction key is used in place of the training key when making predictions 
     	final String predictionKey = "<your prediction key here>";;

         // Create a prediction endpoint, passing in obtained prediction key 
     	PredictionEndpoint endpoint = CustomVisionPredictionManager.authenticate(predictionKey);


         // Make a prediction against the new project 
     	 System.out.println("Making a prediction:"); 
         ImagePrediction result = endpoint.predictions().predictImage().withProjectId(project.id()).withImageData(testImage).withIterationId(iteration.id()).execute();

         // Loop over each prediction and write out the results 
         for(com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.Prediction c : result.predictions()) 
         { 
             System.out.println("Tag: " + c.tagName() + ", Probability: " + c.probability()); 
         } 
            	
    }
    
    
    public static void LoadImagesFromDirectory() 
    { 
    	// this loads the images to be uploaded from Local directory
    	  File fileA = new File(basePath + "Hemlock");
    	  if (fileA.isDirectory()) 
    	  {
    		  File [] fileList = fileA.listFiles();
    		  hemlockImages = Arrays.asList(fileList);
    		  
    	  }
    	  
    	  //this loads the images to be uploaded from Local directory
    	  File fileB = new File(basePath + "Japanese Cherry");
    	  if (fileB.isDirectory()) 
    	  {
    		  File [] files = fileB.listFiles();
    		  japaneseCherryImages = Arrays.asList(files);

    	  }
    	  
    	  //this loads an image to test against the trained model
    	  File testFile = new File(basePath + "Test\\test_image.jpg");
    	  try {
    		  
			testImage = Files.readAllBytes(testFile.toPath());
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    } 

}
