package launcher;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dataflow.Dataflow;
import com.google.api.services.dataflow.model.Job;
import com.google.api.services.dataflow.model.ListJobsResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class LimitCheck {
	static int numOfJobs=100;
	public static int getRunningJobs() throws IOException, GeneralSecurityException {
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
		String projectId = "kishan-last-quota";

		Dataflow.Projects.Jobs.List request = dataflowService.projects().jobs().list(projectId)
				.setFilter("ACTIVE");

		ListJobsResponse response;
		do {
			response = request.execute();
			if (response.getJobs() == null)
				numOfJobs = 0;
			else{
				for(Job job : response.getJobs())
					System.out.println(job.toString());
				numOfJobs = response.getJobs().size();
			}	
			System.out.println("Current Number of Jobs:"+numOfJobs);
			request.setPageToken(response.getNextPageToken());
		} while (response.getNextPageToken() != null);

		return numOfJobs;
	}
}