package launcher;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dataflow.Dataflow;
import com.google.api.services.dataflow.Dataflow.Projects.Jobs.Get;
import com.google.api.services.dataflow.model.Job;
import com.google.api.services.dataflow.model.ListJobsResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class WaitForDone {
	static String projectId = "kishan-last-quota";
	
	public static String getIdByName(String jobName,Dataflow service) throws IOException{
		String jobId = null;
		Dataflow.Projects.Jobs.List request = service.projects().jobs().list(projectId)
				.setFilter("ACTIVE");
		
		ListJobsResponse response;
		do {
			response = request.execute();
			if (response.getJobs() == null){
				System.out.println("Job not in Running State going:: Retrying");
 				continue;
			}
			else{
				for(Job job : response.getJobs())
					if(job.getName().equals(jobName)){
						jobId = job.getId();
						System.out.println("Job "+jobName+"Found : Returning Project ID:"+jobId);
					}
				
			}	
			request.setPageToken(response.getNextPageToken());
		} while (response.getNextPageToken() != null);

		return jobId;
	}

	public static void waitForDone(String jobName) throws IOException, GeneralSecurityException {
		// Authentication is provided by gcloud tool when running locally
		// and by built-in service accounts when running on GAE, GCE or GKE.
		GoogleCredential credential = GoogleCredential.getApplicationDefault();

		// The createScopedRequired method returns true when running on GAE or a local developer
		// machine. In that case, the desired scopes must be passed in manually. When the code is
		// running in GCE, GKE or a Managed VM, the scopes are pulled from the GCE metadata server.
		// See https://developers.google.com/identity/protocols/application-default-credentials for more information.
		if (credential.createScopedRequired()) {
			credential = credential.createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
		}

		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		Dataflow dataflowService = new Dataflow.Builder(httpTransport, jsonFactory, credential)
				.setApplicationName("Google Cloud Platform Sample")
				.build();

		// Add your code to assign values to parameters for the 'list' method:
		// * The project which owns the jobs.


		String jobId = getIdByName(jobName,dataflowService);
		Get request = dataflowService.projects().jobs().get(projectId, jobId);

		Job response;
		do {
			response = request.execute();
			System.out.println(String.format("Job %s CurrentStatus:%s",response.getName(),response.getCurrentState()));
		} while (!(response.getCurrentState().equals("JOB_STATE_DONE")
				||response.getCurrentState().equals("JOB_STATE_FAILED")
				||response.getCurrentState().equals("JOB_STATE_CANCELLED")));
		
		System.out.println("Job "+jobName+" completed with status:"+response.getCurrentState());
	}
}